# Onboarding Instrumentation + Shortened-Flow A/B Test Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fully instrument the onboarding paywall funnel and ship a Remote-Config-driven `short` onboarding variant (Welcome → Notification permission → Paywall) A/B-testable via Firebase.

**Architecture:** Phase 1 (Tasks 1–3) adds the missing analytics: `source` on purchase events, `paywall_dismissed`, and tier-tap tracking on the onboarding paywall — all through the existing spec-014 `trackFunnelStep` dual-pipeline. Phase 2 (Tasks 4–6) adds an `onboarding_variant` Remote Config parameter (fetched during Preload with a 3s cap, persisted to DataStore so it can never flip mid-flow) and refactors `LiveOnboardingScreen` to render a variant-driven page list. Task 7 is the manual console checklist.

**Tech Stack:** Kotlin Multiplatform, Compose Multiplatform, Koin (`viewModelOf` — constructor changes need no DI edits), GitLive `firebase-config` (already a dependency), DataStore, kotlin.time.TimeSource.

**Spec:** `docs/superpowers/specs/2026-08-24-onboarding-ab-test-design.md`

## Global Constraints

- JDK 21; run tests with `./gradlew :composeApp:desktopTest` (NOT `jvmTest` — that task does not exist). Filter: `--tests "me.calebjones.spacelaunchnow.analytics.AnalyticsEventTest"`.
- Conventional Commits; do NOT add Claude as co-author.
- **Commit protocol (Caleb's standing preference, see 014 precedent):** each task's final step STAGES a commit message in this file's checklist — do not run `git commit` until the Task 7 checkpoint where Caleb verifies in Firebase DebugView and confirms.
- Event param names are load-bearing (GA4 registrations match them exactly): `source`, `tier`, `product_id`, `step`, `error_code`, `success`, `page`, `variant`, `granted`, `seconds_on_screen`, `subscription_type`, `is_trial`, `active_entitlements`, `platform`, `revenue`.
- `me.calebjones.spacelaunchnow.api.*.models.*` must not be imported by anything touched here (ADR-0001) — all changes live in `analytics/`, `ui/`, `data/`.

---

### Task 1: `source` on the four purchase events

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt:190-240`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt:229-360`
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModelTest.kt`

**Interfaces:**
- Consumes: existing `AnalyticsEvent` sealed class, `trackFunnelStep`, `FakeAnalyticsProvider` + `analyticsWith(fake)` test helpers (`SubscriptionViewModelTest.kt:414`).
- Produces: `PurchaseStarted/Completed/Failed/Restored` each with `source: String = "support_us"` as a `put("source", source)` param; `SubscriptionViewModel.purchaseProduct(productId, basePlanId, priceAmountMicros, source)`, `purchaseProduct(product: ProductInfo, source: String = "support_us")`, `restorePurchases(source: String = "support_us")`. Task 3 relies on these exact signatures.

- [ ] **Step 1: Write the failing test** — add to `SubscriptionViewModelTest.kt` (Conversion Funnel section, after line 541):

```kotlin
@Test
fun `purchase events carry the source they were started with`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.purchaseProduct("yearly_sub", "yearly-base", 39990000L, source = "onboarding")
    advanceUntilIdle()

    val started = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseStarted>().single()
    val completed = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseCompleted>().single()
    assertEquals("onboarding", started.source)
    assertEquals("onboarding", completed.source)
    assertEquals("onboarding", started.toParameters()["source"])
}

@Test
fun `purchase source defaults to support_us`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.purchaseProduct("yearly_sub", "yearly-base")
    advanceUntilIdle()

    val started = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseStarted>().single()
    assertEquals("support_us", started.source)
}

@Test
fun `restorePurchases stamps source on purchase_restored`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.restorePurchases(source = "onboarding")
    advanceUntilIdle()

    val restored = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseRestored>().single()
    assertEquals("onboarding", restored.source)
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest"`
Expected: compile error — `source` has no value / unresolved reference.

- [ ] **Step 3: Add `source` to the four events** in `AnalyticsEvent.kt`. Pattern for all four (default keeps unrelated call sites compiling; the VM always passes it explicitly):

```kotlin
data class PurchaseStarted(
    val productId: String,
    val source: String = "support_us",
    val dimensions: FunnelDimensions? = null
) : AnalyticsEvent("purchase_started") {
    override fun toParameters() = buildMap {
        put("product_id", productId)
        put("source", source)
        dimensions?.let { putAll(it.toParameters()) }
    }
}
```

Apply identically to `PurchaseCompleted` (keep `revenue`), `PurchaseFailed` (keep `step`/`errorCode`), `PurchaseRestored` (keep `success`) — add the constructor param and the `put("source", source)` line to each `toParameters()`.

- [ ] **Step 4: Thread source through `SubscriptionViewModel`**:

```kotlin
fun purchaseProduct(
    productId: String,
    basePlanId: String? = null,
    priceAmountMicros: Long? = null,
    source: String = "support_us"
) {
```

Inside, pass `source = source` to the `PurchaseStarted`, `PurchaseCompleted`, and `PurchaseFailed` constructions (it is captured by the closure — no member field). Update the convenience overload:

```kotlin
fun purchaseProduct(product: ProductInfo, source: String = "support_us") {
    purchaseProduct(product.productId, product.basePlanId, product.priceAmountMicros, source)
}
```

And `restorePurchases`:

```kotlin
fun restorePurchases(source: String = "support_us") {
```

passing `source = source` to both `PurchaseRestored` constructions (success and failure branches).

- [ ] **Step 5: Run tests to verify pass**

Run: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest" --tests "me.calebjones.spacelaunchnow.analytics.AnalyticsEventTest"`
Expected: PASS (including all pre-existing funnel tests).

- [ ] **Step 6: Stage commit message** — `feat(analytics): attribute purchase events to their paywall source`

---

### Task 2: `paywall_dismissed` event + tracker

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt` (after `PaywallTierSelected`, ~line 189)
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt` (analytics section, after `trackTierSelected`, ~line 79)
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/AnalyticsEventTest.kt`, `.../ui/viewmodel/SubscriptionViewModelTest.kt`

**Interfaces:**
- Consumes: `trackFunnelStep`, `funnelDimensions()` (SubscriptionViewModel.kt:45-59).
- Produces: `AnalyticsEvent.PaywallDismissed(source, secondsOnScreen, dimensions)` named `"paywall_dismissed"`; `SubscriptionViewModel.trackPaywallDismissed(source: String, secondsOnScreen: Long)`. Task 3 calls the tracker.

- [ ] **Step 1: Write the failing tests** — in `AnalyticsEventTest.kt`:

```kotlin
@Test fun `PaywallDismissed has correct name and params`() {
    val event = AnalyticsEvent.PaywallDismissed(source = "onboarding", secondsOnScreen = 12L)
    assertEquals("paywall_dismissed", event.name)
    assertEquals("onboarding", event.toParameters()["source"])
    assertEquals(12L, event.toParameters()["seconds_on_screen"])
}
```

In `SubscriptionViewModelTest.kt` (funnel section):

```kotlin
@Test
fun `trackPaywallDismissed emits paywall_dismissed with dimensions`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.trackPaywallDismissed("onboarding", secondsOnScreen = 20L)
    advanceUntilIdle()

    val event = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PaywallDismissed>().single()
    assertEquals("onboarding", event.source)
    assertEquals(20L, event.secondsOnScreen)
    assertNotNull(event.dimensions)
}
```

- [ ] **Step 2: Run to verify failure** — same gradle command as Task 1. Expected: unresolved reference `PaywallDismissed`.

- [ ] **Step 3: Implement.** In `AnalyticsEvent.kt`, directly after `PaywallTierSelected`:

```kotlin
/**
 * User left a paywall without purchasing ("Continue for free" / dismiss).
 * secondsOnScreen measures view-to-dismiss dwell for the onboarding A/B test.
 */
data class PaywallDismissed(
    val source: String,
    val secondsOnScreen: Long,
    val dimensions: FunnelDimensions? = null
) : AnalyticsEvent("paywall_dismissed") {
    override fun toParameters() = buildMap {
        put("source", source)
        put("seconds_on_screen", secondsOnScreen)
        dimensions?.let { putAll(it.toParameters()) }
    }
}
```

In `SubscriptionViewModel.kt`, after `trackTierSelected`:

```kotlin
fun trackPaywallDismissed(source: String, secondsOnScreen: Long) {
    trackFunnelStep(
        AnalyticsEvent.PaywallDismissed(
            source = source,
            secondsOnScreen = secondsOnScreen,
            dimensions = funnelDimensions()
        )
    )
}
```

- [ ] **Step 4: Run tests to verify pass** — same command. Expected: PASS.

- [ ] **Step 5: Stage commit message** — `feat(analytics): add paywall_dismissed with time-on-screen`

---

### Task 3: Wire the onboarding paywall (tier tap, source, dismissal)

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingPaywallScreen.kt`

**Interfaces:**
- Consumes: `trackTierSelected(type, productId, source)` (SubscriptionViewModel.kt:65), `trackPaywallDismissed` (Task 2), `purchaseProduct(product, source)` / `restorePurchases(source)` (Task 1), `ProductType` (SubscriptionViewModel.kt:387).
- Produces: `OnboardingContent(onSubscribe: (ProductType, ProductInfo) -> Unit, ...)` — no downstream consumers beyond this file's previews.

- [ ] **Step 1: Change the `OnboardingContent` callback signature** (line 185):

```kotlin
onSubscribe: (ProductType, ProductInfo) -> Unit = { _, _ -> },
```

Update its two invocation sites: line 317 → `onClick = { onSubscribe(ProductType.ANNUAL, annualProduct) }`, line 378 → `onClick = { onSubscribe(ProductType.MONTHLY, monthlyProduct) }`. Add `import me.calebjones.spacelaunchnow.ui.viewmodel.ProductType` if not present. Fix any preview in this file that passes `onSubscribe = {}` to `onSubscribe = { _, _ -> }`.

- [ ] **Step 2: Wire tracking in the stateful `OnboardingPaywallScreen`** (lines 103-167). Add a monotonic mark next to the existing state (after line 107):

```kotlin
val shownAt = remember { TimeSource.Monotonic.markNow() }
```

with `import kotlin.time.TimeSource`. Replace the `OnboardingContent` call's three callbacks (lines 158-166):

```kotlin
onSubscribe = { type, product ->
    viewModel.trackTierSelected(type, product.productId, source = "onboarding")
    viewModel.purchaseProduct(product, source = "onboarding")
},
onRestorePurchases = { viewModel.restorePurchases(source = "onboarding") },
onDismiss = {
    viewModel.trackPaywallDismissed("onboarding", shownAt.elapsedNow().inWholeSeconds)
    coroutineScope.launch {
        appPreferences.setOnboardingCompleted(true)
        appPreferences.setOnboardingPaywallV1Shown(true)
        onComplete()
    }
}
```

- [ ] **Step 3: Verify it compiles and nothing regressed**

Run: `./gradlew :composeApp:compileKotlinDesktop :composeApp:desktopTest`
Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 4: Stage commit message** — `feat(analytics): instrument onboarding paywall tier taps, source, and dismissal`

---

### Task 4: `OnboardingVariant` — model, storage, Remote Config getter

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/OnboardingVariant.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/RemoteConfigRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/RemoteConfigRepositoryImpl.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/AppPreferences.kt`
- Modify: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/MockRemoteConfigRepository.kt`
- Test: Create `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/OnboardingVariantTest.kt`

**Interfaces:**
- Consumes: GitLive `config.getValue(key).asString()` pattern (RemoteConfigRepositoryImpl.kt:92), DataStore key pattern (AppPreferences.kt:22-64, flow/setter pattern at 193-199).
- Produces: `enum class OnboardingVariant(val value: String) { CONTROL("control"), SHORT("short") }` with `companion fun fromString(raw: String?): OnboardingVariant`; top-level `suspend fun resolveOnboardingVariant(persisted: String?, fetchRemote: suspend () -> OnboardingVariant, persist: suspend (String) -> Unit): OnboardingVariant`; `RemoteConfigRepository.getOnboardingVariant(): OnboardingVariant`; `AppPreferences.onboardingVariantFlow: Flow<String?>` + `suspend fun setOnboardingVariant(variant: String)`. Tasks 5–6 consume all of these.

- [ ] **Step 1: Write the failing tests** — `OnboardingVariantTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.data.model

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OnboardingVariantTest {

    @Test fun `fromString maps known values`() {
        assertEquals(OnboardingVariant.SHORT, OnboardingVariant.fromString("short"))
        assertEquals(OnboardingVariant.CONTROL, OnboardingVariant.fromString("control"))
    }

    @Test fun `fromString falls back to CONTROL on unknown or null`() {
        assertEquals(OnboardingVariant.CONTROL, OnboardingVariant.fromString("experimental_v9"))
        assertEquals(OnboardingVariant.CONTROL, OnboardingVariant.fromString(null))
    }

    @Test fun `resolve returns persisted variant without fetching`() = runTest {
        var fetched = false
        val result = resolveOnboardingVariant(
            persisted = "short",
            fetchRemote = { fetched = true; OnboardingVariant.CONTROL },
            persist = { }
        )
        assertEquals(OnboardingVariant.SHORT, result)
        assertFalse(fetched)
    }

    @Test fun `resolve fetches and persists when nothing stored`() = runTest {
        var persisted: String? = null
        val result = resolveOnboardingVariant(
            persisted = null,
            fetchRemote = { OnboardingVariant.SHORT },
            persist = { persisted = it }
        )
        assertEquals(OnboardingVariant.SHORT, result)
        assertEquals("short", persisted)
    }
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.model.OnboardingVariantTest"`
Expected: compile error — unresolved `OnboardingVariant`.

- [ ] **Step 3: Create `OnboardingVariant.kt`**:

```kotlin
package me.calebjones.spacelaunchnow.data.model

/**
 * Onboarding flow variant for the shortened-onboarding A/B test
 * (docs/superpowers/specs/2026-08-24-onboarding-ab-test-design.md).
 * Served by the `onboarding_variant` Remote Config parameter.
 */
enum class OnboardingVariant(val value: String) {
    CONTROL("control"),
    SHORT("short");

    companion object {
        fun fromString(raw: String?): OnboardingVariant =
            entries.find { it.value == raw } ?: CONTROL
    }
}

/**
 * The variant actually shown must never change mid-flow: first resolution wins
 * and is persisted; later Remote Config updates are ignored for this install.
 */
suspend fun resolveOnboardingVariant(
    persisted: String?,
    fetchRemote: suspend () -> OnboardingVariant,
    persist: suspend (String) -> Unit
): OnboardingVariant {
    persisted?.let { return OnboardingVariant.fromString(it) }
    val resolved = fetchRemote()
    persist(resolved.value)
    return resolved
}
```

- [ ] **Step 4: Extend `RemoteConfigRepository`** — add to the interface (after `getDiagnosticsConfigJson`):

```kotlin
/**
 * The onboarding A/B variant from the 'onboarding_variant' parameter.
 * CONTROL when unset, unknown, or Firebase is unavailable.
 */
suspend fun getOnboardingVariant(): OnboardingVariant
```

with `import me.calebjones.spacelaunchnow.data.model.OnboardingVariant`. In `RemoteConfigRepositoryImpl`: add `private const val ONBOARDING_VARIANT_KEY = "onboarding_variant"` to the companion, add `ONBOARDING_VARIANT_KEY to "control"` to the `setDefaults()` call (line 134-138), and implement:

```kotlin
override suspend fun getOnboardingVariant(): OnboardingVariant {
    val config = remoteConfig ?: return OnboardingVariant.CONTROL
    return try {
        OnboardingVariant.fromString(config.getValue(ONBOARDING_VARIANT_KEY).asString())
    } catch (e: Exception) {
        log.w(e) { "Failed to read onboarding variant - defaulting to control" }
        OnboardingVariant.CONTROL
    }
}
```

In `MockRemoteConfigRepository` (commonTest): add

```kotlin
var onboardingVariant: OnboardingVariant = OnboardingVariant.CONTROL
override suspend fun getOnboardingVariant(): OnboardingVariant = onboardingVariant
```

- [ ] **Step 5: Extend `AppPreferences`** — companion key (next to `LIVE_ONBOARDING_COMPLETED`, line 48):

```kotlin
// Onboarding A/B variant actually shown to this install ("control"/"short") — sticky once set
private val ONBOARDING_VARIANT = stringPreferencesKey("onboarding_variant")
```

Accessors (next to `liveOnboardingCompletedFlow`, line 193-199 pattern):

```kotlin
val onboardingVariantFlow: Flow<String?> = dataStore.data.map { preferences ->
    preferences[ONBOARDING_VARIANT]
}

suspend fun setOnboardingVariant(variant: String) {
    dataStore.edit { preferences -> preferences[ONBOARDING_VARIANT] = variant }
}
```

- [ ] **Step 6: Run tests to verify pass**

Run: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.data.model.OnboardingVariantTest"` then the full `./gradlew :composeApp:desktopTest`
Expected: PASS.

- [ ] **Step 7: Stage commit message** — `feat(onboarding): add OnboardingVariant model, storage, and remote config plumbing`

---

### Task 5: Variant-driven pager + step/permission tracking

**Files:**
- Create: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingPages.kt`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt:310-311`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/OnboardingViewModel.kt:35-37`
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/LiveOnboardingScreen.kt`
- Test: Create `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/onboarding/OnboardingPagesTest.kt`; modify `.../analytics/AnalyticsEventTest.kt`

**Interfaces:**
- Consumes: `OnboardingVariant`, `resolveOnboardingVariant`, `AppPreferences.onboardingVariantFlow`/`setOnboardingVariant`, `RemoteConfigRepository.getOnboardingVariant` (Task 4).
- Produces: `enum class OnboardingPage(val analyticsName: String)`; `fun pagesFor(variant: OnboardingVariant): List<OnboardingPage>`; `AnalyticsEvent.OnboardingStep(step, page, variant, completed)`; `AnalyticsEvent.NotificationPermissionResult(granted, source, variant)` named `"notification_permission_result"`; `OnboardingViewModel.trackOnboardingStep(step, page, variant, completed)` and `trackNotificationPermissionResult(granted, variant)`. Task 7's DebugView checklist verifies these fire.

- [ ] **Step 1: Write the failing tests.** `OnboardingPagesTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.ui.onboarding

import me.calebjones.spacelaunchnow.data.model.OnboardingVariant
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingPagesTest {

    @Test fun `control shows all five pages in order`() {
        assertEquals(
            listOf(
                OnboardingPage.WELCOME,
                OnboardingPage.LAUNCH_CARD,
                OnboardingPage.NEWS_EVENTS,
                OnboardingPage.WIDGETS,
                OnboardingPage.NOTIFICATION_PERMISSION
            ),
            pagesFor(OnboardingVariant.CONTROL)
        )
    }

    @Test fun `short shows welcome then notification permission only`() {
        assertEquals(
            listOf(OnboardingPage.WELCOME, OnboardingPage.NOTIFICATION_PERMISSION),
            pagesFor(OnboardingVariant.SHORT)
        )
    }

    @Test fun `analytics names are stable across variants`() {
        assertEquals("welcome", OnboardingPage.WELCOME.analyticsName)
        assertEquals("notification_permission", OnboardingPage.NOTIFICATION_PERMISSION.analyticsName)
    }
}
```

In `AnalyticsEventTest.kt`:

```kotlin
@Test fun `OnboardingStep carries page and variant params`() {
    val event = AnalyticsEvent.OnboardingStep(step = 1, page = "notification_permission", variant = "short", completed = true)
    assertEquals("onboarding_step", event.name)
    assertEquals(1, event.toParameters()["step"])
    assertEquals("notification_permission", event.toParameters()["page"])
    assertEquals("short", event.toParameters()["variant"])
    assertEquals(true, event.toParameters()["completed"])
}

@Test fun `NotificationPermissionResult has correct name and params`() {
    val event = AnalyticsEvent.NotificationPermissionResult(granted = false, source = "onboarding", variant = "control")
    assertEquals("notification_permission_result", event.name)
    assertEquals(false, event.toParameters()["granted"])
    assertEquals("onboarding", event.toParameters()["source"])
    assertEquals("control", event.toParameters()["variant"])
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPagesTest" --tests "me.calebjones.spacelaunchnow.analytics.AnalyticsEventTest"`
Expected: compile errors — unresolved `OnboardingPage`, wrong `OnboardingStep` arity.

- [ ] **Step 3: Create `OnboardingPages.kt`**:

```kotlin
package me.calebjones.spacelaunchnow.ui.onboarding

import me.calebjones.spacelaunchnow.data.model.OnboardingVariant

/** Pager pages, with stable analytics names — `step` indices renumber between variants; `page` does not. */
enum class OnboardingPage(val analyticsName: String) {
    WELCOME("welcome"),
    LAUNCH_CARD("launch_card"),
    NEWS_EVENTS("news_events"),
    WIDGETS("widgets"),
    NOTIFICATION_PERMISSION("notification_permission")
}

fun pagesFor(variant: OnboardingVariant): List<OnboardingPage> = when (variant) {
    OnboardingVariant.CONTROL -> listOf(
        OnboardingPage.WELCOME,
        OnboardingPage.LAUNCH_CARD,
        OnboardingPage.NEWS_EVENTS,
        OnboardingPage.WIDGETS,
        OnboardingPage.NOTIFICATION_PERMISSION
    )
    OnboardingVariant.SHORT -> listOf(
        OnboardingPage.WELCOME,
        OnboardingPage.NOTIFICATION_PERMISSION
    )
}
```

- [ ] **Step 4: Update the events** in `AnalyticsEvent.kt` — replace the existing `OnboardingStep` (line 310-311):

```kotlin
data class OnboardingStep(
    val step: Int,
    val page: String,
    val variant: String,
    val completed: Boolean
) : AnalyticsEvent("onboarding_step") {
    override fun toParameters() = buildMap {
        put("step", step)
        put("page", page)
        put("variant", variant)
        put("completed", completed)
    }
}
```

(Check how the old `OnboardingStep` built parameters first — if it overrode `toParameters`, replace it; keep the event name `"onboarding_step"` exactly.) Add next to it:

```kotlin
/** Outcome of the onboarding notification-permission page (grant, deny, or "Maybe Later" = false). */
data class NotificationPermissionResult(
    val granted: Boolean,
    val source: String,
    val variant: String
) : AnalyticsEvent("notification_permission_result") {
    override fun toParameters() = buildMap {
        put("granted", granted)
        put("source", source)
        put("variant", variant)
    }
}
```

- [ ] **Step 5: Update `OnboardingViewModel`** (lines 35-37):

```kotlin
fun trackOnboardingStep(step: Int, page: String, variant: String, completed: Boolean) {
    analyticsManager.track(
        AnalyticsEvent.OnboardingStep(step = step, page = page, variant = variant, completed = completed)
    )
}

fun trackNotificationPermissionResult(granted: Boolean, variant: String) {
    analyticsManager.track(
        AnalyticsEvent.NotificationPermissionResult(granted = granted, source = "onboarding", variant = variant)
    )
}
```

- [ ] **Step 6: Refactor `LiveOnboardingScreen`.** All edits in `LiveOnboardingScreen.kt`:

a. Delete `private const val PAGE_COUNT = 5` (line 56). Fix the KDoc (lines 60-70) to describe the two variants (control: 5 pages; short: Welcome → Notification permission).

b. Add injections/imports: `koinInject<RemoteConfigRepository>()`, `produceState`, `first`, `OnboardingVariant`, `resolveOnboardingVariant`, `me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository`.

c. At the top of the composable body (before `rememberPagerState`):

```kotlin
val remoteConfigRepository: RemoteConfigRepository = koinInject()
val variant by produceState<OnboardingVariant?>(initialValue = null) {
    value = resolveOnboardingVariant(
        persisted = appPreferences.onboardingVariantFlow.first(),
        fetchRemote = { remoteConfigRepository.getOnboardingVariant() },
        persist = { appPreferences.setOnboardingVariant(it) }
    )
}
val resolvedVariant = variant
if (resolvedVariant == null) {
    // One frame of bare gradient while DataStore + activated config resolve (both local, no network)
    Box(modifier = modifier.fillMaxSize().background(spaceGradient))
    return
}
val pages = remember(resolvedVariant) { pagesFor(resolvedVariant) }
val pagerState = rememberPagerState(pageCount = { pages.size })
```

d. Replace every `PAGE_COUNT` use: `isLastPage = pagerState.currentPage == pages.lastIndex`; skip button target `pagerState.animateScrollToPage(pages.lastIndex)`; progress `((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (pages.size - 1).toFloat())`.

e. Data prefetch (lines 98-103) — only control needs the content-page data:

```kotlin
LaunchedEffect(resolvedVariant) {
    nextUpViewModel.fetchNextLaunch()
    if (resolvedVariant == OnboardingVariant.CONTROL) {
        onboardingViewModel.fetchScheduleData()
        onboardingViewModel.fetchArticles()
        onboardingViewModel.fetchExploreData()
    }
}
```

f. Step tracking (lines 105-111):

```kotlin
LaunchedEffect(pagerState.currentPage) {
    onboardingViewModel.trackOnboardingStep(
        step = pagerState.currentPage,
        page = pages[pagerState.currentPage].analyticsName,
        variant = resolvedVariant.value,
        completed = pagerState.currentPage == pages.lastIndex
    )
}
```

g. Pager content (lines 159-186) — switch on page identity, not index:

```kotlin
HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth().weight(1f)) { pageIndex ->
    when (pages[pageIndex]) {
        OnboardingPage.WELCOME -> WelcomePage(modifier = Modifier.fillMaxSize(), nextLaunch = nextLaunch)
        OnboardingPage.LAUNCH_CARD -> LaunchCardPage(modifier = Modifier.fillMaxSize(), nextLaunch = nextLaunch)
        OnboardingPage.NEWS_EVENTS -> NewsEventsPage(modifier = Modifier.fillMaxSize(), articles = articles)
        OnboardingPage.WIDGETS -> WidgetsPage(modifier = Modifier.fillMaxSize())
        OnboardingPage.NOTIFICATION_PERMISSION -> NotificationPermissionPage(
            onPermissionResult = { granted ->
                onboardingViewModel.trackNotificationPermissionResult(granted, resolvedVariant.value)
                if (granted) completeOnboarding()
            },
            onSkip = {
                onboardingViewModel.trackNotificationPermissionResult(false, resolvedVariant.value)
                completeOnboarding()
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
```

(Note: a user who denies then grants emits two results; analysis uses the per-user last value — acceptable, do not dedupe.) The enum order matches today's on-screen sequence exactly (index 2 = `NewsEventsPage`, index 3 = `WidgetsPage`). The screen also collects `upcomingLaunches`/`astronauts`/`rockets`/`agencies` state that no page currently renders — leftover from the March restructure; leave that dead state alone, it is out of scope.

h. The preview near line 242 uses its own `rememberPagerState(pageCount = { PAGE_COUNT })` — change to `pagesFor(OnboardingVariant.CONTROL).size`.

- [ ] **Step 7: Run all tests + compile**

Run: `./gradlew :composeApp:desktopTest :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 8: Stage commit message** — `feat(onboarding): variant-driven pager with page-level and permission-outcome analytics`

---

### Task 6: Preload fetch gate

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/PreloadViewModel.kt`
- Test: Create `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/PreloadDestinationTest.kt`

**Interfaces:**
- Consumes: `RemoteConfigRepository.fetchAndActivate()` (existing), navigation objects `LiveOnboarding`/`Onboarding`/`Home` (already imported in PreloadViewModel.kt:33-35).
- Produces: top-level `fun preloadDestination(liveOnboardingCompleted: Boolean, onboardingPaywallShown: Boolean): Any` in `PreloadViewModel.kt`; `PreloadViewModel` constructor gains `private val remoteConfigRepository: RemoteConfigRepository` (Koin `viewModelOf(::PreloadViewModel)` at AppModule.kt:175 resolves it automatically — no DI edit).

- [ ] **Step 1: Write the failing test** — `PreloadDestinationTest.kt`:

```kotlin
package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LiveOnboarding
import me.calebjones.spacelaunchnow.navigation.Onboarding
import kotlin.test.Test
import kotlin.test.assertEquals

class PreloadDestinationTest {

    @Test fun `fresh install goes to live onboarding`() {
        assertEquals(LiveOnboarding, preloadDestination(liveOnboardingCompleted = false, onboardingPaywallShown = false))
    }

    @Test fun `pager done but paywall unseen goes to paywall`() {
        assertEquals(Onboarding, preloadDestination(liveOnboardingCompleted = true, onboardingPaywallShown = false))
    }

    @Test fun `fully onboarded goes home`() {
        assertEquals(Home, preloadDestination(liveOnboardingCompleted = true, onboardingPaywallShown = true))
    }
}
```

- [ ] **Step 2: Run to verify failure**

Run: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.PreloadDestinationTest"`
Expected: compile error — unresolved `preloadDestination`.

- [ ] **Step 3: Extract the destination decision.** In `PreloadViewModel.kt`, add a top-level function (below the data classes, mirroring the `when` at lines 89-93):

```kotlin
/** Single source of truth for where Preload navigates — extracted for testability. */
fun preloadDestination(liveOnboardingCompleted: Boolean, onboardingPaywallShown: Boolean): Any = when {
    !liveOnboardingCompleted -> LiveOnboarding
    onboardingPaywallShown -> Home
    else -> Onboarding
}
```

Replace lines 89-93 with:

```kotlin
val nextDestination: Any = preloadDestination(liveOnboardingCompleted, onboardingPaywallShown)
```

- [ ] **Step 4: Add the constructor dependency and the gated fetch.** Constructor (after `appPreferences`, line 72): `private val remoteConfigRepository: RemoteConfigRepository` with `import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository`. Then in `startPreload`, right after `nextDestination` is computed, start the fetch concurrently with tier 1:

```kotlin
// Onboarding A/B: the variant must be activated before LiveOnboarding renders.
// Runs alongside tier 1; capped so an offline first launch costs at most 3s (variant falls back to control).
val onboardingConfigFetch = if (nextDestination == LiveOnboarding) {
    async {
        withTimeoutOrNull(3_000L) { remoteConfigRepository.fetchAndActivate() }
            ?: log.w { "Onboarding variant fetch timed out — control will be used" }
    }
} else null
```

Then make completion wait for it. In the per-task `finally` block (lines 131-137), stop setting `isComplete` — only bump the counter:

```kotlin
_preloadState.update { state ->
    state.copy(completedTasks = state.completedTasks + 1)
}
```

And replace the "Ensure navigation happens" block (lines 151-154) with:

```kotlin
onboardingConfigFetch?.await()

// Navigation is released only here — after tier 1 (or its timeout) AND the variant fetch.
_preloadState.update { it.copy(isComplete = true) }
```

- [ ] **Step 5: Run all tests + compile**

Run: `./gradlew :composeApp:desktopTest :composeApp:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 6: Stage commit message** — `feat(onboarding): gate preload navigation on onboarding variant fetch`

---

### Task 7: CHECKPOINT — DebugView verification, commits, console setup

**Files:** none (verification + manual console work).

- [ ] **Step 1: STOP — hand to Caleb.** Build a debug APK (`./gradlew installDebug`), clear app data so onboarding runs, and verify in Firebase DebugView (`adb shell setprop debug.firebase.analytics.app me.calebjones.spacelaunchnow.kmpdebug`):
  - Control flow: `onboarding_step{step,page,variant=control,completed}` per page; on the paywall `paywall_viewed{source=onboarding}`; tap a plan → `paywall_tier_selected{tier,product_id,source=onboarding}` then `purchase_started{source=onboarding}`; cancel → `purchase_failed{source=onboarding}`; "Continue for free" → `paywall_dismissed{source=onboarding,seconds_on_screen}`; permission page → `notification_permission_result{granted,variant}`.
  - Short flow: in Firebase console set `onboarding_variant` parameter default to `short`, fetch on a cleared install → pager shows exactly Welcome → Notification permission, events carry `variant=short`.
  - Support Us regression: tier tap still emits `source=support_us`.
- [ ] **Step 2: On Caleb's confirmation, land the staged commits** from Tasks 1–6 in order (one commit per task, messages as staged; branch decision at execution time — do not commit to `fix/v6-client-filter-passthrough`).
- [ ] **Step 3: Console checklist (Caleb, manual):**
  - GA4 property `164090905`: register event-scoped dimensions `page`, `variant`, `completed`, `granted` (plus the earlier agreed set if not yet done: `source`, `tier`, `product_id`, `step`, `error_code`, `success`, `subscription_type`, `is_trial`, `active_entitlements`, `platform`); custom metrics `revenue` (Currency), `seconds_on_screen` (Standard).
  - Firebase Remote Config: create parameter `onboarding_variant`, default `"control"`.
  - Firebase A/B Testing, after the release train is live per store: two experiments (Android app, iOS app), 50/50 `control`/`short` on `onboarding_variant`, activation event `onboarding_step`, goal `purchase_completed`, secondary metrics retention + `notification_permission_result`.
- [ ] **Step 4:** Update the spec's Status line to `Implemented — experiments pending console setup`.
