package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlin.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
 * Warn+ lines flush immediately (best-effort durability; the flush still hops through the async shell); Info/Debug batch.
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
