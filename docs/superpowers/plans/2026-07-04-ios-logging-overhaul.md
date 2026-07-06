# iOS Logging Overhaul Implementation Plan

> **STATUS: IMPLEMENTED** (July 2026) — all 10 tasks landed on `feat/ios-logging-overhaul`. Deviations from plan: kotlin.time.Clock instead of kotlinx.datetime; Dispatchers.Default retained (Dispatchers.IO unavailable in this commonMain); SpaceLogger.observePreferences retired entirely (DiagnosticLevelController is sole severity authority); legacy LoggingPreferences accessors deleted in final sweep; FCM token log redacted.

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make diagnostic logging actually work for real users: Datadog governed by SDK consent instead of an init gate, one user-facing control, a local log file with share/export, a diagnostics screen showing NSE App Group truth, non-destructive breadcrumb draining, and a single structured startup event.

**Architecture:** A new `DiagnosticLevel` (OFF/STANDARD/VERBOSE) stored in `LoggingPreferences` becomes the single source of truth; a `DiagnosticLevelController` maps it to Datadog `TrackingConsent`, remote/console/file severities. Datadog is always initialized (with `PENDING` consent) so `DatadogLogger` calls are never silent no-ops. A `FileLogWriter` (Kermit writer over a char-capped ring buffer persisted to one file) provides the share-logs escape hatch via the existing text-share plumbing (iOS Swift `ShareHelper` via NSNotificationCenter — **no pbxproj changes**). A Diagnostics screen (commonMain Compose, platform data via expect/actual) renders live state vs. NSE App Group state vs. breadcrumbs.

**Tech Stack:** Kotlin Multiplatform (androidMain/iosMain/desktopMain), Compose Multiplatform, Kermit, Datadog KMP SDK 1.4.0 (`Datadog.setTrackingConsent`/`isInitialized` verified present), DataStore preferences, kotlinx-datetime, Koin.

**Repo state warning:** The working tree has UNCOMMITTED changes in `iosApp/NotificationServiceExtension/NotificationService.swift`, `iosApp/iosApp/AppDelegate.swift`, `iosApp/iosApp.xcodeproj/project.pbxproj`, `composeApp/.../HomeScreen.kt` plus untracked re-alert-policy files. **Do not touch those files.** Stage files explicitly (`git add <paths>`) — never `git add -A`.

**Verification environment (Windows):** `$env:JAVA_HOME = "D:\tools\Android Studio\jbr"` then `.\gradlew.bat` from `d:\code\SpaceLaunchNow\SpaceLaunchNow-KMP-Main`. Tasks:
- commonMain: `.\gradlew.bat :composeApp:compileCommonMainKotlinMetadata`
- androidMain: `.\gradlew.bat :composeApp:compileDebugKotlinAndroid`
- iosMain: `.\gradlew.bat :composeApp:compileKotlinIosArm64` (klib compile works on Windows; if the task is absent/fails for host reasons, note it and rely on CI)
- unit tests: `.\gradlew.bat :composeApp:testDebugUnitTest`
Pre-existing benign warnings: `kotlin.native.cacheKind` deprecation, kotlinx `Instant` deprecations, SettingsViewModel unchecked casts.

**Commit style:** conventional commits, NO `Co-Authored-By` trailers.

---

### Task 1: `DiagnosticLevel` enum + policy (pure, tested)

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticLevel.kt`
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticLevelTest.kt`

- [ ] **Step 1: Write the failing test**

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiagnosticLevelTest {

    @Test
    fun fromStorage_prefersStoredLevel() {
        assertEquals(DiagnosticLevel.VERBOSE, DiagnosticLevel.fromStorage("VERBOSE", legacyDatadogEnabled = false))
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage("OFF", legacyDatadogEnabled = true))
    }

    @Test
    fun fromStorage_migratesLegacyEnabledToStandard() {
        assertEquals(DiagnosticLevel.STANDARD, DiagnosticLevel.fromStorage(null, legacyDatadogEnabled = true))
    }

    @Test
    fun fromStorage_defaultsToOff() {
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage(null, legacyDatadogEnabled = null))
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage(null, legacyDatadogEnabled = false))
        assertEquals(DiagnosticLevel.OFF, DiagnosticLevel.fromStorage("garbage", legacyDatadogEnabled = false))
    }

    @Test
    fun policy_offRevokesConsentAndSilencesRemote() {
        val p = DiagnosticLevel.OFF.policy()
        assertFalse(p.remoteConsentGranted)
        assertEquals(Severity.Assert, p.remoteSeverity)
        assertEquals(Severity.Warn, p.consoleSeverity)
        assertEquals(Severity.Info, p.fileSeverity) // file log ALWAYS captures Info+
    }

    @Test
    fun policy_standardGrantsConsentAtWarn() {
        val p = DiagnosticLevel.STANDARD.policy()
        assertTrue(p.remoteConsentGranted)
        assertEquals(Severity.Warn, p.remoteSeverity)
        assertEquals(Severity.Info, p.fileSeverity)
    }

    @Test
    fun policy_verboseGrantsConsentAtDebugEverywhere() {
        val p = DiagnosticLevel.VERBOSE.policy()
        assertTrue(p.remoteConsentGranted)
        assertEquals(Severity.Debug, p.remoteSeverity)
        assertEquals(Severity.Debug, p.consoleSeverity)
        assertEquals(Severity.Debug, p.fileSeverity)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*DiagnosticLevelTest*"`
Expected: FAIL — unresolved reference `DiagnosticLevel`.

- [ ] **Step 3: Write the implementation**

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.Severity

/**
 * Single user-facing diagnostic control. Replaces the previous four independent
 * knobs (console severity, Datadog severity, Datadog toggle, sample rate).
 */
enum class DiagnosticLevel {
    OFF, STANDARD, VERBOSE;

    companion object {
        /**
         * Resolve level from storage. Falls back to the legacy `datadog_enabled`
         * boolean for users upgrading from the old toggle (true -> STANDARD).
         */
        fun fromStorage(stored: String?, legacyDatadogEnabled: Boolean?): DiagnosticLevel {
            stored?.let { name -> entries.firstOrNull { it.name == name }?.let { return it } }
            return if (legacyDatadogEnabled == true) STANDARD else OFF
        }
    }
}

/** What a diagnostic level means for each log sink. Single source of truth. */
data class DiagnosticPolicy(
    /** Datadog TrackingConsent GRANTED (true) vs NOT_GRANTED (false). */
    val remoteConsentGranted: Boolean,
    /** DataDogLogWriter minSeverity. */
    val remoteSeverity: Severity,
    /** Console/logcat writer minSeverity. */
    val consoleSeverity: Severity,
    /** Local diagnostics file writer minSeverity — file capture stays on even at OFF
     *  so the "Share logs" escape hatch has history from before the user opted in. */
    val fileSeverity: Severity,
)

fun DiagnosticLevel.policy(): DiagnosticPolicy = when (this) {
    DiagnosticLevel.OFF -> DiagnosticPolicy(
        remoteConsentGranted = false,
        remoteSeverity = Severity.Assert,
        consoleSeverity = Severity.Warn,
        fileSeverity = Severity.Info,
    )
    DiagnosticLevel.STANDARD -> DiagnosticPolicy(
        remoteConsentGranted = true,
        remoteSeverity = Severity.Warn,
        consoleSeverity = Severity.Warn,
        fileSeverity = Severity.Info,
    )
    DiagnosticLevel.VERBOSE -> DiagnosticPolicy(
        remoteConsentGranted = true,
        remoteSeverity = Severity.Debug,
        consoleSeverity = Severity.Debug,
        fileSeverity = Severity.Debug,
    )
}

fun DiagnosticLevel.displayLabel(): String = when (this) {
    DiagnosticLevel.OFF -> "Off"
    DiagnosticLevel.STANDARD -> "Standard"
    DiagnosticLevel.VERBOSE -> "Verbose"
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*DiagnosticLevelTest*"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticLevel.kt composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticLevelTest.kt
git commit -m "feat(logging): add DiagnosticLevel with per-sink policy mapping"
```

---

### Task 2: Persist `DiagnosticLevel` in LoggingPreferences

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/LoggingPreferences.kt`

- [ ] **Step 1: Add key, getter flow, and setter**

Add to the companion object:

```kotlin
private val DIAGNOSTIC_LEVEL = stringPreferencesKey("diagnostic_level")
```

Add these methods to the class (keep all existing methods — legacy readers still use them):

```kotlin
/**
 * Single diagnostic control. Migrates from the legacy datadog_enabled boolean
 * when the new key is absent (old toggle ON -> STANDARD).
 */
fun getDiagnosticLevel(): Flow<DiagnosticLevel> = dataStore.data.map { prefs ->
    DiagnosticLevel.fromStorage(prefs[DIAGNOSTIC_LEVEL], prefs[DATADOG_ENABLED])
}

/**
 * Set the diagnostic level and keep the legacy keys coherent so any remaining
 * reader of the old knobs observes the same effective configuration.
 */
suspend fun setDiagnosticLevel(level: DiagnosticLevel) {
    val policy = level.policy()
    dataStore.edit { prefs ->
        prefs[DIAGNOSTIC_LEVEL] = level.name
        prefs[DATADOG_ENABLED] = level != DiagnosticLevel.OFF
        prefs[CONSOLE_SEVERITY] = policy.consoleSeverity.name
        prefs[DATADOG_SEVERITY] = policy.remoteSeverity.name
    }
}
```

(`DiagnosticLevel` is in the same package — no import needed. `Flow`/`map`/`edit`/`stringPreferencesKey` are already imported.)

- [ ] **Step 2: Compile**

Run: `.\gradlew.bat :composeApp:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/LoggingPreferences.kt
git commit -m "feat(logging): persist DiagnosticLevel with legacy-key migration"
```

---

### Task 3: File ring buffer log + share-logs backend

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/FileLog.kt`
- Create: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsFileStore.ios.kt`
- Create: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsFileStore.android.kt`
- Create: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsFileStore.desktop.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/SpaceLogger.kt`
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt` (wire store before `SpaceLogger.initialize()`)
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt` (same)
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/FileLogTest.kt`

- [ ] **Step 1: Write the failing tests** (pure `FileLogCore`/`LogRingBuffer`, no coroutines needed)

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeStore : DiagnosticsFileStore {
    var content: String? = null
    var writes = 0
    override fun read(): String? = content
    override fun write(content: String) { this.content = content; writes++ }
}

class FileLogTest {

    @Test
    fun ringBuffer_dropsOldestWhenOverCap() {
        val buf = LogRingBuffer(maxChars = 30)
        buf.append("aaaaaaaaaa") // 10 + 1
        buf.append("bbbbbbbbbb")
        buf.append("cccccccccc")
        // 3 lines x 11 chars = 33 > 30 -> oldest dropped
        assertEquals("bbbbbbbbbb\ncccccccccc", buf.snapshot())
    }

    @Test
    fun ringBuffer_tailCapsFromEnd() {
        val buf = LogRingBuffer(maxChars = 1000)
        buf.append("hello")
        buf.append("world")
        assertEquals("world", buf.tail(5))
        assertEquals("hello\nworld", buf.tail(999))
    }

    @Test
    fun core_loadsExistingFileOnFirstUse() {
        val store = FakeStore().apply { content = "old line 1\nold line 2" }
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 100)
        assertTrue(core.export(1000).startsWith("old line 1"))
    }

    @Test
    fun core_flushesEveryNLines() {
        val store = FakeStore()
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 3)
        core.append("a", urgent = false)
        core.append("b", urgent = false)
        assertEquals(0, store.writes)
        core.append("c", urgent = false)
        assertEquals(1, store.writes)
        assertEquals("a\nb\nc", store.content)
    }

    @Test
    fun core_flushesImmediatelyWhenUrgent() {
        val store = FakeStore()
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 100)
        core.append("warn line", urgent = true)
        assertEquals(1, store.writes)
    }

    @Test
    fun core_exportFlushesAndCaps() {
        val store = FakeStore()
        val core = FileLogCore(store, maxChars = 1000, flushEveryLines = 100)
        core.append("0123456789", urgent = false)
        val exported = core.export(maxChars = 4)
        assertEquals("6789", exported)
        assertEquals(1, store.writes) // export forced a flush
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*FileLogTest*"`
Expected: FAIL — unresolved references.

- [ ] **Step 3: Implement `FileLog.kt` (commonMain)**

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/** Minimal persistence abstraction so the file log is unit-testable. */
interface DiagnosticsFileStore {
    fun read(): String?
    fun write(content: String)
}

/** Char-capped line ring buffer. Not thread-safe — callers serialize access. */
class LogRingBuffer(private val maxChars: Int) {
    private val lines = ArrayDeque<String>()
    private var totalChars = 0

    fun append(line: String) {
        lines.addLast(line)
        totalChars += line.length + 1
        while (totalChars > maxChars && lines.size > 1) {
            totalChars -= lines.removeFirst().length + 1
        }
    }

    fun load(content: String) {
        content.lineSequence().filter { it.isNotEmpty() }.forEach { append(it) }
    }

    fun snapshot(): String = lines.joinToString("\n")

    fun tail(maxChars: Int): String {
        val s = snapshot()
        return if (s.length <= maxChars) s else s.substring(s.length - maxChars)
    }
}

/**
 * Pure synchronous core of the diagnostics file log: capped buffer + flush policy.
 * Warn+ lines flush immediately (crash durability); Info/Debug batch.
 */
class FileLogCore(
    private val store: DiagnosticsFileStore,
    private val maxChars: Int = MAX_CHARS,
    private val flushEveryLines: Int = FLUSH_EVERY_LINES,
) {
    companion object {
        const val MAX_CHARS = 300_000
        const val FLUSH_EVERY_LINES = 25
        const val EXPORT_MAX_CHARS = 200_000
    }

    private val buffer = LogRingBuffer(maxChars)
    private var unflushed = 0
    private var loaded = false

    private fun ensureLoaded() {
        if (!loaded) {
            loaded = true
            try {
                store.read()?.let { buffer.load(it) }
            } catch (_: Exception) {
                // Corrupt/unreadable file: start fresh rather than fail logging.
            }
        }
    }

    fun append(line: String, urgent: Boolean) {
        ensureLoaded()
        buffer.append(line)
        unflushed++
        if (urgent || unflushed >= flushEveryLines) flush()
    }

    fun flush() {
        if (unflushed > 0) {
            try {
                store.write(buffer.snapshot())
            } catch (_: Exception) {
                // Persistence failure must never break the app; buffer stays in memory.
            }
            unflushed = 0
        }
    }

    fun export(maxChars: Int = EXPORT_MAX_CHARS): String {
        ensureLoaded()
        flush()
        return buffer.tail(maxChars)
    }
}

/**
 * Kermit writer that mirrors logs into the on-device diagnostics file.
 * All buffer access is confined to a single-parallelism dispatcher.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FileLogWriter(store: DiagnosticsFileStore) : LogWriter(), ConfigurableLogWriter {

    override var minSeverity: Severity = Severity.Info

    private val core = FileLogCore(store)
    private val dispatcher = Dispatchers.Default.limitedParallelism(1)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < minSeverity) return
        val line = buildString {
            append(Clock.System.now())
            append(' ')
            append(severity.name.uppercase())
            append(" [")
            append(tag)
            append("] ")
            append(message)
            throwable?.let {
                append(" | ")
                append(it::class.simpleName)
                append(": ")
                append(it.message)
            }
        }
        val urgent = severity >= Severity.Warn
        scope.launch { core.append(line, urgent) }
    }

    suspend fun export(maxChars: Int = FileLogCore.EXPORT_MAX_CHARS): String =
        withContext(dispatcher) { core.export(maxChars) }
}

/**
 * Process-wide handle to the diagnostics file log. Platform startup installs the
 * store BEFORE SpaceLogger.initialize() so the writer joins the writer list.
 */
object DiagnosticsLog {
    var writer: FileLogWriter? = null
        private set

    fun initialize(store: DiagnosticsFileStore) {
        if (writer == null) writer = FileLogWriter(store)
    }

    suspend fun export(maxChars: Int = FileLogCore.EXPORT_MAX_CHARS): String =
        writer?.export(maxChars) ?: "(diagnostics file log not initialized on this platform)"
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*FileLogTest*"`
Expected: PASS

- [ ] **Step 5: Platform stores**

`DiagnosticsFileStore.ios.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

class IosDiagnosticsFileStore : DiagnosticsFileStore {
    private val path: String by lazy {
        val dir = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, true)
            .firstOrNull() as? String ?: ""
        "$dir/sln_diagnostics.log"
    }

    override fun read(): String? =
        NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun write(content: String) {
        (content as NSString).writeToFile(path, atomically = true, encoding = NSUTF8StringEncoding, error = null)
    }
}
```

(If `stringWithContentsOfFile`/`writeToFile` overload names differ in this Kotlin version, mirror the exact `NSString` extension signatures used elsewhere in iosMain or resolve via IDE autocomplete against `platform.Foundation`; the semantics stay: read file as UTF-8 string or null, atomic UTF-8 write.)

`DiagnosticsFileStore.android.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import android.content.Context
import java.io.File

class AndroidDiagnosticsFileStore(context: Context) : DiagnosticsFileStore {
    private val file = File(context.filesDir, "sln_diagnostics.log")

    override fun read(): String? = try {
        if (file.exists()) file.readText() else null
    } catch (_: Exception) {
        null
    }

    override fun write(content: String) {
        try {
            file.writeText(content)
        } catch (_: Exception) {
            // best-effort
        }
    }
}
```

`DiagnosticsFileStore.desktop.kt`:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import java.io.File

class DesktopDiagnosticsFileStore : DiagnosticsFileStore {
    private val file = File(System.getProperty("user.home"), ".spacelaunchnow/sln_diagnostics.log")

    override fun read(): String? = try {
        if (file.exists()) file.readText() else null
    } catch (_: Exception) {
        null
    }

    override fun write(content: String) {
        try {
            file.parentFile?.mkdirs()
            file.writeText(content)
        } catch (_: Exception) {
            // best-effort
        }
    }
}
```

- [ ] **Step 6: Attach writer in `SpaceLogger.initialize`**

In `SpaceLogger.kt` replace the body of `initialize` writer handling:

```kotlin
fun initialize(
    logConfig: LogConfig = platformLogConfig(),
    loggingPreferences: LoggingPreferences? = null
) {
    // Mirror everything into the local diagnostics file when the platform installed one.
    val allWriters = logConfig.writers + listOfNotNull(DiagnosticsLog.writer)

    // Separate writers by type. The file writer manages its own severity via
    // DiagnosticLevelController — exclude it from console severity fan-out.
    consoleWriters = allWriters.filter { it !is DataDogLogWriter && it !is FileLogWriter }
    dataDogWriter = allWriters.filterIsInstance<DataDogLogWriter>().firstOrNull()

    val staticConfig = StaticConfig(
        minSeverity = Severity.Verbose,
        logWriterList = allWriters
    )
    baseLogger = Logger(staticConfig, BASE_TAG)

    loggingPreferences?.let { prefs ->
        observePreferences(prefs)
    }
}
```

- [ ] **Step 7: Wire platform stores at startup**

`MainViewController.kt` — immediately BEFORE the existing `SpaceLogger.initialize()` call (line ~113):

```kotlin
me.calebjones.spacelaunchnow.util.logging.DiagnosticsLog.initialize(
    me.calebjones.spacelaunchnow.util.logging.IosDiagnosticsFileStore()
)
```

`MainApplication.kt` — find its `SpaceLogger.initialize(...)` call in `onCreate` and insert immediately before it:

```kotlin
me.calebjones.spacelaunchnow.util.logging.DiagnosticsLog.initialize(
    me.calebjones.spacelaunchnow.util.logging.AndroidDiagnosticsFileStore(this)
)
```

(Desktop: no wiring — `DiagnosticsLog.writer` stays null and everything degrades gracefully.)

- [ ] **Step 8: Compile all targets**

Run: `.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64 :composeApp:testDebugUnitTest`
Expected: BUILD SUCCESSFUL, tests pass.

- [ ] **Step 9: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/FileLog.kt composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/FileLogTest.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsFileStore.ios.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsFileStore.android.kt composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsFileStore.desktop.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/SpaceLogger.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt
git commit -m "feat(logging): on-device diagnostics file log with capped ring buffer"
```

---

### Task 4: Datadog consent flow — always initialize, consent governs upload

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.kt` (add `DatadogRuntime` expect)
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.ios.kt`
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.android.kt`
- Modify: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.desktop.kt`
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticLevelController.kt`
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt` (remove gate)
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt` (remove gate)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/DebugPreferences.kt` (sample default 1f -> 100f)

- [ ] **Step 1: Add `DatadogRuntime` expect to `DatadogConfig.kt`**

```kotlin
/**
 * Runtime consent + status for Datadog. Upload is governed by TrackingConsent,
 * not by whether the SDK was initialized — the SDK is ALWAYS initialized at
 * startup (with PENDING consent) so log calls are never silent no-ops.
 */
expect object DatadogRuntime {
    /** Apply user consent for remote upload. Safe to call before initialize. */
    fun setConsentGranted(granted: Boolean)
    /** True when SDK is initialized AND consent granted — logs will actually upload. */
    fun isRemoteLoggingActive(): Boolean
    /** True when the underlying SDK instance is initialized. */
    fun isSdkInitialized(): Boolean
}
```

- [ ] **Step 2: iOS actual** (append to `DatadogConfig.ios.kt`; also change `TrackingConsent.GRANTED` to `TrackingConsent.PENDING` in the existing `Datadog.initialize` call and delete the `datadogEnabled`-early-return's sibling gate comment if stale — the plist `datadogEnabled` check itself STAYS):

```kotlin
actual object DatadogRuntime {
    private var consentGranted: Boolean = false

    actual fun setConsentGranted(granted: Boolean) {
        consentGranted = granted
        if (Datadog.isInitialized()) {
            Datadog.setTrackingConsent(
                if (granted) TrackingConsent.GRANTED else TrackingConsent.NOT_GRANTED
            )
        }
        log.i { "Datadog consent set: granted=$granted (sdkInitialized=${Datadog.isInitialized()})" }
    }

    actual fun isRemoteLoggingActive(): Boolean = Datadog.isInitialized() && consentGranted

    actual fun isSdkInitialized(): Boolean = Datadog.isInitialized()
}
```

Android actual: identical code in `DatadogConfig.android.kt` (same imports already present), and change its `Datadog.initialize(context, configuration, TrackingConsent.GRANTED)` to `TrackingConsent.PENDING` as well.

Desktop actual (append to `DatadogConfig.desktop.kt`):

```kotlin
actual object DatadogRuntime {
    actual fun setConsentGranted(granted: Boolean) = Unit
    actual fun isRemoteLoggingActive(): Boolean = false
    actual fun isSdkInitialized(): Boolean = false
}
```

- [ ] **Step 3: Create `DiagnosticLevelController.kt`**

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.DatadogRuntime

/**
 * Applies the user's DiagnosticLevel to every log sink: Datadog consent,
 * remote writer severity, console severity, and the diagnostics file writer.
 * Started once at app startup; also invoked directly on user toggle for
 * immediate effect.
 */
object DiagnosticLevelController {
    private var job: Job? = null

    fun start(prefs: LoggingPreferences, scope: CoroutineScope = CoroutineScope(Dispatchers.Default)) {
        job?.cancel()
        job = scope.launch {
            prefs.getDiagnosticLevel().collect { level -> apply(level) }
        }
    }

    fun apply(level: DiagnosticLevel) {
        val policy = level.policy()
        DatadogRuntime.setConsentGranted(policy.remoteConsentGranted)
        SpaceLogger.setConsoleSeverity(policy.consoleSeverity)
        SpaceLogger.setDataDogSeverity(policy.remoteSeverity)
        DiagnosticsLog.writer?.minSeverity = policy.fileSeverity
    }
}
```

(Note: `SpaceLogger.observePreferences` still exists and observes the legacy keys; `setDiagnosticLevel` writes those keys coherently, so both observers converge on identical severities. Leave it in place.)

- [ ] **Step 4: Replace the iOS init gate**

In `MainViewController.kt`, replace the whole `if (consoleSeverity <= Severity.Debug || BuildConfig.IS_DEBUG) { ... } else { ... }` block (keep the surrounding try/catch and the koin lookups for `loggingPrefs`/`debugPrefs`; the `consoleSeverity` runBlocking read can be deleted):

```kotlin
// Always initialize Datadog; whether logs UPLOAD is governed by TrackingConsent,
// which DiagnosticLevelController derives from the user's Diagnostic Logging setting.
val sampleRate = runBlocking {
    debugPrefs.debugSettingsFlow.first().datadogSampleRate
}
log.d { "Initializing Datadog (consent-based) with ${sampleRate.toInt()}% sample rate..." }
initializeDatadog(
    context = null,
    sampleRate = sampleRate,
    debugPreferences = debugPrefs
)
DiagnosticLevelController.start(loggingPrefs)
log.d { "✅ Datadog initialized; consent controller started" }
```

Add imports: `me.calebjones.spacelaunchnow.util.logging.DiagnosticLevelController`. Remove the now-unused `Severity` import if nothing else uses it in this file.

- [ ] **Step 5: Replace the Android init gate**

In `MainApplication.kt`, replace the entire `if (BuildConfig.IS_DEBUG) { ... } else { GlobalScope.launch { ... } }` block (keep the outer try/catch and the `loggingPrefs`/`debugPrefs` lookups; the `defaultSeverity` val can be deleted):

```kotlin
// Always initialize Datadog; upload is governed by TrackingConsent applied by
// DiagnosticLevelController from the user's Diagnostic Logging setting.
initializeDatadog(
    context = this,
    sampleRate = null, // resolved to 100% default inside; debug slider still overrides via observer
    debugPreferences = debugPrefs
)
me.calebjones.spacelaunchnow.util.logging.DiagnosticLevelController.start(loggingPrefs)
log.d { "✅ Datadog initialized (consent-based)" }
```

Check `DatadogConfig.android.kt`'s `initializeDatadog` — it has the same `val effectiveSampleRate = sampleRate ?: 1f` line as iOS; change BOTH platforms' fallback to `sampleRate ?: 100f`.

- [ ] **Step 6: Change sample-rate defaults in `DebugPreferences.kt`**

Line 40: `datadogSampleRate = preferences[DATADOG_SAMPLE_RATE] ?: 100f`
Line 167 (data class default): `val datadogSampleRate: Float = 100f`
Rationale comment above the key declaration:

```kotlin
// Sampling is a debug-only cost knob now; user-facing cost control is the
// DiagnosticLevel consent (OFF = NOT_GRANTED = nothing uploads).
```

- [ ] **Step 7: Compile all targets**

Run: `.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64 :composeApp:compileCommonMainKotlinMetadata`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.ios.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.android.kt composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.desktop.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticLevelController.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/MainViewController.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/DebugPreferences.kt
git commit -m "feat(logging): replace Datadog init gate with SDK TrackingConsent flow"
```

---

### Task 5: Breadcrumb parser + non-destructive drain + prefs snapshot

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NseBreadcrumb.kt`
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/NseBreadcrumbTest.kt`
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NSEPreferenceBridge.kt`

- [ ] **Step 1: Write the failing parser test**

```kotlin
package me.calebjones.spacelaunchnow.data.notifications

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NseBreadcrumbTest {

    @Test
    fun parse_validEntry() {
        val b = NseBreadcrumb.parse("1751600000|oneHour|suppressed|v5_launch_filter")
        assertEquals(1751600000L, b?.timestampEpochSeconds)
        assertEquals("oneHour", b?.type)
        assertEquals("suppressed", b?.decision)
        assertEquals("v5_launch_filter", b?.reason)
    }

    @Test
    fun parse_rejectsMalformed() {
        assertNull(NseBreadcrumb.parse("not-a-breadcrumb"))
        assertNull(NseBreadcrumb.parse("abc|type|decision|reason")) // bad timestamp
        assertNull(NseBreadcrumb.parse("123|only|three"))
    }

    @Test
    fun parse_toleratesExtraSeparatorsInReason() {
        val b = NseBreadcrumb.parse("123|t|d|reason/with/slashes")
        assertEquals("reason/with/slashes", b?.reason)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*NseBreadcrumbTest*"`
Expected: FAIL — unresolved reference.

- [ ] **Step 3: Implement `NseBreadcrumb.kt`**

```kotlin
package me.calebjones.spacelaunchnow.data.notifications

/**
 * One NSE delivery-decision breadcrumb, written by NotificationService.swift as
 * a pipe-delimited "ts|type|decision|reason" string in App Group UserDefaults.
 */
data class NseBreadcrumb(
    val timestampEpochSeconds: Long,
    val type: String,
    val decision: String,
    val reason: String,
) {
    companion object {
        fun parse(entry: String): NseBreadcrumb? {
            val parts = entry.split("|")
            if (parts.size < 4) return null
            val ts = parts[0].toLongOrNull() ?: return null
            return NseBreadcrumb(ts, parts[1], parts[2], parts[3])
        }
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat :composeApp:testDebugUnitTest --tests "*NseBreadcrumbTest*"`
Expected: PASS

- [ ] **Step 5: Guard the drain + add peek + snapshot in `NSEPreferenceBridge.kt`**

(a) At the very top of `drainNseEventLog()` — before the `draining.compareAndSet` — add:

```kotlin
// CRITICAL: never read-then-clear the buffer unless the logs will actually
// upload. Draining into an uninitialized/non-consented logger DESTROYS the
// only evidence of killed-app delivery decisions.
if (!DatadogRuntime.isRemoteLoggingActive()) {
    log.d { "NSE drain skipped — remote logging inactive; breadcrumbs preserved" }
    return
}
```

Import: `me.calebjones.spacelaunchnow.analytics.DatadogRuntime`.

(b) Inside the drain loop, replace the manual `entry.split("|")` block with the shared parser:

```kotlin
entries.forEach { entry ->
    val crumb = NseBreadcrumb.parse(entry)
    val ts = crumb?.timestampEpochSeconds?.toString() ?: ""
    val type = crumb?.type ?: "unknown"
    val decision = crumb?.decision ?: "unknown"
    val reason = crumb?.reason ?: ""
    // ... existing message/attributes/emit code unchanged ...
}
```

(c) Add peek (non-destructive read, for the Diagnostics screen):

```kotlin
/** Read breadcrumbs WITHOUT clearing them — for the Diagnostics screen. */
fun peekNseEventLog(): List<NseBreadcrumb> {
    val userDefaults = NSUserDefaults(suiteName = APP_GROUP) ?: return emptyList()
    @Suppress("UNCHECKED_CAST")
    val entries = userDefaults.arrayForKey(KEY_NSE_EVENT_LOG) as? List<String> ?: return emptyList()
    return entries.mapNotNull { NseBreadcrumb.parse(it) }
}
```

(d) Add a structured snapshot and rebase `logStoredPrefs()` on it:

```kotlin
/** Snapshot of what the NSE reads from the App Group. null field = key MISSING. */
data class NsePrefsSnapshot(
    val appGroupAvailable: Boolean,
    val enableNotifications: Boolean?,
    val followAllLaunches: Boolean?,
    val useStrictMatching: Boolean?,
    val subscribedAgencies: List<String>?,
    val subscribedLocations: List<String>?,
) {
    val anyKeyMissing: Boolean
        get() = !appGroupAvailable || enableNotifications == null || followAllLaunches == null ||
            useStrictMatching == null || subscribedAgencies == null || subscribedLocations == null
}

fun readStoredPrefs(): NsePrefsSnapshot {
    val userDefaults = NSUserDefaults(suiteName = APP_GROUP)
        ?: return NsePrefsSnapshot(false, null, null, null, null, null)

    fun boolOrNull(key: String): Boolean? =
        if (userDefaults.objectForKey(key) != null) userDefaults.boolForKey(key) else null

    @Suppress("UNCHECKED_CAST")
    fun listOrNull(key: String): List<String>? = userDefaults.arrayForKey(key) as? List<String>

    return NsePrefsSnapshot(
        appGroupAvailable = true,
        enableNotifications = boolOrNull(KEY_ENABLE_NOTIFICATIONS),
        followAllLaunches = boolOrNull(KEY_FOLLOW_ALL_LAUNCHES),
        useStrictMatching = boolOrNull(KEY_USE_STRICT_MATCHING),
        subscribedAgencies = listOrNull(KEY_SUBSCRIBED_AGENCIES),
        subscribedLocations = listOrNull(KEY_SUBSCRIBED_LOCATIONS),
    )
}
```

Rewrite `logStoredPrefs()` to a 3-line debug summary using the snapshot (the verbose 15-line dump is replaced by the structured startup event in Task 6), and FIX the stale comment claiming the NSE "falls back to allow-all (followAllLaunches=true)" — since v5.31.0 the NSE fails CLOSED:

```kotlin
/**
 * Log a compact summary of the App Group prefs the NSE reads when the app is
 * killed. Missing keys mean the NSE fails CLOSED (suppresses launch pushes).
 */
fun logStoredPrefs() {
    val snap = readStoredPrefs()
    log.i {
        "[NSE-PREFS] appGroup=${snap.appGroupAvailable} keysMissing=${snap.anyKeyMissing} " +
            "followAll=${snap.followAllLaunches} strict=${snap.useStrictMatching} " +
            "agencies=${snap.subscribedAgencies?.size} locations=${snap.subscribedLocations?.size}"
    }
    if (snap.anyKeyMissing) {
        log.w { "[NSE-PREFS] keys MISSING — NSE fails closed and will SUPPRESS launch pushes until synced" }
    }
}
```

- [ ] **Step 6: Compile iOS + tests**

Run: `.\gradlew.bat :composeApp:compileKotlinIosArm64 :composeApp:testDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NseBreadcrumb.kt composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/notifications/NseBreadcrumbTest.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NSEPreferenceBridge.kt
git commit -m "fix(logging): non-destructive NSE breadcrumb drain + peek + prefs snapshot"
```

---

### Task 6: Structured single-event startup state

**Files:**
- Modify: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosNotificationBridge.kt:104-127` (`logStartupState`)

- [ ] **Step 1: Replace `logStartupState()` body**

```kotlin
/**
 * Emit ONE structured startup event comparing live Kotlin filter state with the
 * App Group prefs the NSE reads when the app is killed. Replaces the previous
 * ~15-line prose dump. Queryable in Datadog via log_kind:startup_state.
 * DatadogLogger buffers under PENDING consent and no-ops when uninitialized,
 * so this is always safe to call.
 */
fun logStartupState() {
    runBlocking {
        try {
            val state = notificationStateStorage.getState()
            val nse = NSEPreferenceBridge.readStoredPrefs()
            val attributes = UserContext.getLogAttributes() + mapOf(
                "log_kind" to "startup_state",
                "platform" to "ios",
                "live_enable_notifications" to state.enableNotifications,
                "live_follow_all" to state.followAllLaunches,
                "live_strict" to state.useStrictMatching,
                "live_agency_count" to state.subscribedAgencies.size,
                "live_location_count" to state.subscribedLocations.size,
                "nse_app_group_available" to nse.appGroupAvailable,
                "nse_keys_missing" to nse.anyKeyMissing,
                "nse_enable_notifications" to nse.enableNotifications,
                "nse_follow_all" to nse.followAllLaunches,
                "nse_strict" to nse.useStrictMatching,
                "nse_agency_count" to (nse.subscribedAgencies?.size ?: -1),
                "nse_location_count" to (nse.subscribedLocations?.size ?: -1),
            )
            DatadogLogger.info("[STARTUP] notification_state", attributes)
            log.d {
                "startup_state live(followAll=${state.followAllLaunches}, strict=${state.useStrictMatching}, " +
                    "agencies=${state.subscribedAgencies.size}, locations=${state.subscribedLocations.size}) " +
                    "nse(keysMissing=${nse.anyKeyMissing})"
            }
        } catch (e: Exception) {
            log.e { "Failed to emit startup state: ${e.message}" }
        }
    }
    // Drain NSE breadcrumbs (no-op preserve when remote logging inactive — Task 5).
    NSEPreferenceBridge.drainNseEventLog()
}
```

Imports to add: `me.calebjones.spacelaunchnow.analytics.DatadogLogger`, `me.calebjones.spacelaunchnow.util.logging.UserContext` (check actual package of `UserContext` — it is `me.calebjones.spacelaunchnow.util.logging.UserContext` per NSEPreferenceBridge's existing import). Delete the now-unused `NSEPreferenceBridge.logStoredPrefs()` call.

- [ ] **Step 2: Compile iOS**

Run: `.\gradlew.bat :composeApp:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/IosNotificationBridge.kt
git commit -m "feat(logging): structured single-event startup state for Datadog"
```

---

### Task 7: `sharePlainText` expect/actual

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.kt`
- Create: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.ios.kt`
- Create: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.android.kt`
- Create: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.desktop.kt`
- Modify: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt` (context holder init)

- [ ] **Step 1: Common expect**

```kotlin
package me.calebjones.spacelaunchnow.util

/**
 * Share arbitrary plain text via the platform share sheet.
 * iOS: existing Swift ShareHelper via NSNotificationCenter (iPad popover safe).
 * Android: ACTION_SEND chooser. Desktop: clipboard.
 */
expect fun sharePlainText(text: String, subject: String)
```

- [ ] **Step 2: iOS actual** (reuses the existing Swift ShareHelper — no Xcode project changes)

```kotlin
package me.calebjones.spacelaunchnow.util

import platform.Foundation.NSNotificationCenter

actual fun sharePlainText(text: String, subject: String) {
    // Must match ShareHelper.swift (same channel PlatformSharingService uses).
    NSNotificationCenter.defaultCenter.postNotificationName(
        aName = "SpaceLaunchNow.ShareText",
        `object` = null,
        userInfo = mapOf("text" to text)
    )
}
```

- [ ] **Step 3: Android actual + context holder**

```kotlin
package me.calebjones.spacelaunchnow.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

/** Application context for share intents fired from common code. Set in MainApplication.onCreate. */
@SuppressLint("StaticFieldLeak")
object ShareContextHolder {
    var appContext: Context? = null
}

actual fun sharePlainText(text: String, subject: String) {
    val context = ShareContextHolder.appContext ?: return
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_SUBJECT, subject)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    val chooser = Intent.createChooser(intent, subject).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(chooser)
}
```

In `MainApplication.onCreate` (right after `super.onCreate()`): `me.calebjones.spacelaunchnow.util.ShareContextHolder.appContext = applicationContext`

- [ ] **Step 4: Desktop actual** (clipboard, mirroring desktop `PlatformSharingService`'s approach)

```kotlin
package me.calebjones.spacelaunchnow.util

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun sharePlainText(text: String, subject: String) {
    Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(text), null)
}
```

- [ ] **Step 5: Compile all targets**

Run: `.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.ios.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.android.kt composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/SharePlainText.desktop.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/MainApplication.kt
git commit -m "feat(share): platform sharePlainText for diagnostics export"
```

---

### Task 8: Single-control UI — rewrite LoggingSettingsSection, remove old knobs

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/LoggingSettingsSection.kt` (full rewrite)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/SettingsScreen.kt:394` (pass new callback)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DebugSettingsScreen.kt` (remove `DebugLoggingSettings` + `DatadogSampleRateControl` usages ~line 384)
- Delete: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DebugLoggingSettings.kt`

- [ ] **Step 1: Rewrite `LoggingSettingsSection.kt`**

```kotlin
package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.logging.DiagnosticLevel
import me.calebjones.spacelaunchnow.util.logging.DiagnosticLevelController
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.displayLabel

/**
 * Single diagnostic-logging control (replaces the old enable toggle + console
 * severity + Datadog severity + sample-rate knobs).
 *
 * Off      — nothing uploads (Datadog consent NOT_GRANTED); local file log stays on.
 * Standard — warnings, delivery decisions, and structured events upload.
 * Verbose  — detailed debug logs upload; console goes to Debug too.
 */
@Composable
fun LoggingSettingsSection(
    loggingPreferences: LoggingPreferences,
    onOpenDiagnostics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level by loggingPreferences.getDiagnosticLevel()
        .collectAsState(initial = DiagnosticLevel.OFF)
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Diagnostic Logging", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Help us debug issues by sharing diagnostic logs. Standard sends warnings " +
                    "and notification delivery decisions; Verbose sends detailed logs.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagnosticLevel.entries.forEach { option ->
                    FilterChip(
                        selected = level == option,
                        onClick = {
                            coroutineScope.launch {
                                loggingPreferences.setDiagnosticLevel(option)
                                // Apply immediately; the startup observer also converges.
                                DiagnosticLevelController.apply(option)
                            }
                        },
                        label = { Text(option.displayLabel()) }
                    )
                }
            }
            TextButton(onClick = onOpenDiagnostics) {
                Text("Open Diagnostics")
            }
        }
    }
}
```

- [ ] **Step 2: Update the `SettingsScreen.kt:394` call site**

Add the callback parameter. Find how this screen already navigates (search `DebugSettings` within `SettingsScreen.kt` — it will either call a passed-in navigation lambda or a navController; mirror that exact mechanism):

```kotlin
LoggingSettingsSection(
    loggingPreferences = loggingPreferences, // existing arg unchanged
    onOpenDiagnostics = { /* same navigation mechanism the screen uses for DebugSettings, targeting Diagnostics */ }
)
```

If `SettingsScreen` receives navigation lambdas from `App.kt`, add `onNavigateToDiagnostics: () -> Unit` to its signature and thread it from `App.kt`'s `composableWithCompositionLocal<Settings>` block as `navController.navigate(Diagnostics)` (exactly parallel to how the DebugSettings entry is wired). `Diagnostics` route is created in Task 9 — if implementing tasks in order, temporarily pass `onOpenDiagnostics = {}` here and complete the wiring in Task 9 Step 4.

- [ ] **Step 3: Remove old knobs from `DebugSettingsScreen.kt`**

- Delete the `DatadogSampleRateControl(...)` invocation around line 384 (keep `DatadogSampleRateControl`'s composable definition ONLY if it lives in another file and has other callers — check with grep; if it's defined in `DebugSettingsScreen.kt` and now unused, delete the definition too).
- Grep for `DebugLoggingSettings(` usages and delete the invocation(s).
- Delete file `DebugLoggingSettings.kt`.

- [ ] **Step 4: Compile**

Run: `.\gradlew.bat :composeApp:compileCommonMainKotlinMetadata :composeApp:compileDebugKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -u composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/LoggingSettingsSection.kt
git commit -m "feat(settings): collapse logging knobs into single Off/Standard/Verbose control"
```

---

### Task 9: Diagnostics screen

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/Screen.kt` (add route)
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.kt` (expects)
- Create: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.ios.kt`
- Create: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.android.kt`
- Create: `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.desktop.kt`
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DiagnosticsScreen.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt` (route registration, after the `DebugSettings` block at ~line 498)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DebugSettingsScreen.kt` (add "Open Diagnostics" button)

- [ ] **Step 1: Add route to `Screen.kt`** (next to `DebugSettings`)

```kotlin
@Serializable
data object Diagnostics
```

- [ ] **Step 2: Providers — common expects**

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb

/** Label/value rows describing platform notification-delivery state (NSE App Group on iOS). */
expect fun platformNotificationDiagnostics(): List<Pair<String, String>>

/** Recent NSE delivery breadcrumbs, non-destructive (iOS only; empty elsewhere). */
expect fun recentNseBreadcrumbs(): List<NseBreadcrumb>
```

iOS actual:

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.NSEPreferenceBridge
import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb

actual fun platformNotificationDiagnostics(): List<Pair<String, String>> {
    val snap = NSEPreferenceBridge.readStoredPrefs()
    fun present(v: Any?): String = v?.toString() ?: "MISSING"
    return listOf(
        "App Group available" to snap.appGroupAvailable.toString(),
        "Any key missing" to snap.anyKeyMissing.toString(),
        "NSE enableNotifications" to present(snap.enableNotifications),
        "NSE followAllLaunches" to present(snap.followAllLaunches),
        "NSE useStrictMatching" to present(snap.useStrictMatching),
        "NSE agencies (expanded)" to (snap.subscribedAgencies?.let { "${it.size}: ${it.take(12).joinToString(",")}" } ?: "MISSING"),
        "NSE locations (expanded)" to (snap.subscribedLocations?.let { "${it.size}: ${it.take(12).joinToString(",")}" } ?: "MISSING"),
    )
}

actual fun recentNseBreadcrumbs(): List<NseBreadcrumb> =
    NSEPreferenceBridge.peekNseEventLog().takeLast(20)
```

Android and desktop actuals (two files, identical bodies):

```kotlin
package me.calebjones.spacelaunchnow.util.logging

import me.calebjones.spacelaunchnow.data.notifications.NseBreadcrumb

actual fun platformNotificationDiagnostics(): List<Pair<String, String>> = emptyList()
actual fun recentNseBreadcrumbs(): List<NseBreadcrumb> = emptyList()
```

- [ ] **Step 3: `DiagnosticsScreen.kt`**

Dependency acquisition: check the imports at the top of `NotificationSettingsScreen.kt` for the Koin-in-Compose pattern used in this repo (`org.koin.compose.koinInject` or explicit params from App.kt). The code below assumes `koinInject`; if the repo uses another pattern, mirror that instead.

```kotlin
package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.DatadogRuntime
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.DiagnosticLevel
import me.calebjones.spacelaunchnow.util.logging.DiagnosticsLog
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.platformNotificationDiagnostics
import me.calebjones.spacelaunchnow.util.logging.recentNseBreadcrumbs
import me.calebjones.spacelaunchnow.util.sharePlainText
import org.koin.compose.koinInject

/**
 * Ground-truth diagnostics: live filter state, NSE App Group state (iOS),
 * recent delivery decisions, logging status, and log export.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
) {
    val loggingPreferences = koinInject<LoggingPreferences>()
    val notificationStateStorage = koinInject<NotificationStateStorage>()
    val scope = rememberCoroutineScope()

    val level by loggingPreferences.getDiagnosticLevel()
        .collectAsState(initial = DiagnosticLevel.OFF)
    val liveState by produceState<NotificationState?>(initialValue = null) {
        value = notificationStateStorage.getState()
    }
    val platformRows = platformNotificationDiagnostics()
    val breadcrumbs = recentNseBreadcrumbs()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            item {
                DiagnosticsCard("App") {
                    DiagRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    DiagRow("Debug build", BuildConfig.IS_DEBUG.toString())
                }
            }
            item {
                DiagnosticsCard("Logging") {
                    DiagRow("Diagnostic level", level.name)
                    DiagRow("Datadog SDK initialized", DatadogRuntime.isSdkInitialized().toString())
                    DiagRow("Remote upload active", DatadogRuntime.isRemoteLoggingActive().toString())
                }
            }
            item {
                DiagnosticsCard("Live notification filters (in-app)") {
                    val s = liveState
                    if (s == null) {
                        DiagRow("State", "loading…")
                    } else {
                        DiagRow("Notifications enabled", s.enableNotifications.toString())
                        DiagRow("Follow all launches", s.followAllLaunches.toString())
                        DiagRow("Strict matching", s.useStrictMatching.toString())
                        DiagRow("Agencies", "${s.subscribedAgencies.size}: ${s.subscribedAgencies.take(12).joinToString(",")}")
                        DiagRow("Locations", "${s.subscribedLocations.size}: ${s.subscribedLocations.take(12).joinToString(",")}")
                    }
                }
            }
            if (platformRows.isNotEmpty()) {
                item {
                    DiagnosticsCard("NSE App Group (what filtering uses when app is killed)") {
                        platformRows.forEach { (label, value) -> DiagRow(label, value) }
                    }
                }
            }
            if (breadcrumbs.isNotEmpty()) {
                item {
                    DiagnosticsCard("Recent NSE delivery decisions") { }
                }
                items(breadcrumbs.reversed()) { crumb ->
                    Text(
                        "${crumb.timestampEpochSeconds} ${crumb.type} → ${crumb.decision} (${crumb.reason})",
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            val header = buildString {
                                appendLine("SpaceLaunchNow diagnostic logs")
                                appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                                appendLine("Diagnostic level: ${level.name}")
                                appendLine("----")
                            }
                            sharePlainText(header + DiagnosticsLog.export(), "SpaceLaunchNow Diagnostic Logs")
                        }
                    }
                ) { Text("Share logs") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val report = buildString {
                            appendLine("SpaceLaunchNow diagnostics report")
                            appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                            appendLine("Diagnostic level: ${level.name}")
                            appendLine("Datadog initialized: ${DatadogRuntime.isSdkInitialized()}, uploading: ${DatadogRuntime.isRemoteLoggingActive()}")
                            liveState?.let { s ->
                                appendLine("Live: enabled=${s.enableNotifications} followAll=${s.followAllLaunches} strict=${s.useStrictMatching} agencies=${s.subscribedAgencies.size} locations=${s.subscribedLocations.size}")
                                appendLine("Live agencies: ${s.subscribedAgencies.joinToString(",")}")
                                appendLine("Live locations: ${s.subscribedLocations.joinToString(",")}")
                            }
                            platformRows.forEach { (l, v) -> appendLine("$l: $v") }
                            recentNseBreadcrumbs().forEach { c ->
                                appendLine("NSE ${c.timestampEpochSeconds} ${c.type} ${c.decision} ${c.reason}")
                            }
                        }
                        sharePlainText(report, "SpaceLaunchNow Diagnostics Report")
                    }
                ) { Text("Share diagnostics report") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DiagnosticsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.45f)
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
    }
}
```

- [ ] **Step 4: Register the route in `App.kt`** (immediately after the `DebugSettings` block ending ~line 503, matching its exact call shape):

```kotlin
composableWithCompositionLocal<Diagnostics> {
    DiagnosticsScreen(
        onNavigateBack = { navController.popBackStack() }
    )
}
```

Add imports mirroring the neighbors (`me.calebjones.spacelaunchnow.navigation.Diagnostics`, `me.calebjones.spacelaunchnow.ui.settings.DiagnosticsScreen`). Then complete the Task 8 Step 2 wiring: `SettingsScreen`'s `onOpenDiagnostics` navigates to `Diagnostics` via the same mechanism used for `DebugSettings`.

- [ ] **Step 5: Add entry point in `DebugSettingsScreen.kt`**

Where the sample-rate control was removed (~line 384), add a navigation button following whatever navigation affordance DebugSettingsScreen already has (check its parameters; if it has none, skip this — the Settings entry point from Task 8 is sufficient and this step is optional).

- [ ] **Step 6: Compile all targets + full test run**

Run: `.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64 :composeApp:testDebugUnitTest`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/navigation/Screen.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.kt composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.ios.kt composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.android.kt composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticsProviders.desktop.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DiagnosticsScreen.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/SettingsScreen.kt composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/settings/DebugSettingsScreen.kt
git commit -m "feat(diagnostics): ground-truth diagnostics screen with log export"
```

---

### Task 10: Final verification sweep

- [ ] **Step 1: Full compile + tests, fresh**

Run: `.\gradlew.bat :composeApp:compileDebugKotlinAndroid :composeApp:compileKotlinIosArm64 :composeApp:compileCommonMainKotlinMetadata :composeApp:compileDebugUnitTestKotlinAndroid :composeApp:testDebugUnitTest --rerun-tasks`
Expected: BUILD SUCCESSFUL, all tests pass (per repo memory: compile the unit-test source set too, not just main).

- [ ] **Step 2: Confirm untouched files**

Run: `git status --short`
Expected: ONLY the pre-existing dirty files remain modified/untracked (`NotificationService.swift`, `AppDelegate.swift`, `project.pbxproj`, `HomeScreen.kt`, re-alert docs, `NotificationAlertPolicy.swift`). Nothing else uncommitted.

- [ ] **Step 3: Grep for leftovers**

Run: grep repo for `DebugLoggingSettings` (expect: no references), `consoleSeverity <= Severity.Debug` (expect: no init-gating occurrences remain in MainViewController/MainApplication), `TrackingConsent.GRANTED` (expect: only inside `DatadogRuntime.setConsentGranted`).

- [ ] **Step 4: Update docs**

If `docs/` contains logging/diagnostics docs referencing the old four knobs (search for "Datadog severity", "sample rate"), update or note. Add a short section to this plan's sibling: nothing else required.

---

## Behavior changes to communicate (release notes / self-reminder)

1. Datadog SDK now always initializes (PENDING consent). Upload requires the user setting Diagnostic Logging to Standard/Verbose. Debug builds no longer force-enable Datadog.
2. Old users with the legacy toggle ON migrate to STANDARD automatically; everyone else is OFF.
3. NSE breadcrumbs are no longer destroyed on launch when logging is off — they persist (bounded at 50) until a consented launch drains them.
4. `[NSE-PREFS]`-style startup dump replaced by one structured `log_kind:startup_state` event — update any Datadog queries/dashboards that matched the old message strings.
5. Local diagnostics file (`sln_diagnostics.log`, ≤300 KB) now always captures Info+ — exportable from Settings → Diagnostic Logging → Open Diagnostics → Share logs.
