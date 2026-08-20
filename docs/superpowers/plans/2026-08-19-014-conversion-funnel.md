# 014 Conversion Funnel Instrumentation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Complete the Support-Us paywall funnel — paywall_viewed → paywall_tier_selected → purchase_started → purchase_completed/failed/restored — with subscriber-context dimensions on every step and dual Firebase + Datadog emission.

**Architecture:** A shared `FunnelDimensions` value type is stamped as an optional param block on all funnel `AnalyticsEvent`s (existing call sites unaffected via default null). `SubscriptionViewModel` centralizes the dimension read (SubscriptionState + BillingManager entitlements + platform), mirrors the dimensions to Firebase **user properties** on state change, and encapsulates the Datadog `DatadogLogger.info` dual-pipeline in its `trackXxx` methods. `SupportUsScreen` gains a view-tracking `LaunchedEffect` and thin tier-tap calls.

**Tech Stack:** Kotlin Multiplatform, existing `AnalyticsManager` fan-out (spec 011), `DatadogLogger` expect/actual (no-op on desktop), kotlin.test + kotlinx-coroutines-test.

**Spec:** `specs/014-instrument-conversion-funnel/spec.md` (Draft).

**Already delivered by 018 (this branch):** FR-3 purchase-failure — shipped as `PurchaseFailed(productId, step, errorCode)`, a superset of the spec's `(productId, reason?)` shape: `errorCode = "user_cancelled"` distinguishes sheet dismissal, other values are coarse RevenueCat `PurchasesErrorCode` names (non-PII). Kept as-is. FR-5 revenue — shipped (`PurchaseCompleted.revenue` from price micros).

**Open questions resolved:**
- **Q1:** Recommended approach — user properties (all events segmentable) **and** params on funnel events (Datadog can't read user properties).
- **Q2:** Single-currency revenue now (as shipped in 018); multi-currency normalization is a flagged follow-up.
- **Q3:** Firebase is the primary funnel view (native funnel exploration); Datadog query documented alongside in quickstart.md.

## Global Constraints

- Conventional Commits, no Claude co-author. Work stays **uncommitted** at the end until Caleb tests (standing preference); commit messages staged in Task 5.
- ADR-0001 + spec-011 rules: composables call thin `viewModel.trackXxx` methods; no magic strings at call sites; events are `AnalyticsEvent` subclasses with snake_case names.
- No raw error strings/PII in params. No added main-thread work (dispatch stays on the manager's scope).
- Dimension keys per spec FR-4/success criteria: `subscription_type`, `is_trial`, `active_entitlements`, `platform`. Values of `subscription_type` match RevenueCat's `subscription_state` attribute (`subscriptionType.name.lowercase()`); the key name differs deliberately — the spec's success criteria name `subscription_type`.
- Verify: `./gradlew :composeApp:desktopTest` (NOT `jvmTest` — doesn't exist), `compileKotlinDesktop`, `:composeApp:compileDebugKotlinAndroid`. iOS compiles on the Mac.

---

### Task 1: FunnelDimensions + PaywallTierSelected + dimension params on funnel events

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/events/AnalyticsEvent.kt` (Subscription Events section)
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/AnalyticsEventTest.kt`

**Interfaces (produced, used by Task 2):**
```kotlin
data class FunnelDimensions(val subscriptionType: String, val isTrial: Boolean, val activeEntitlements: String, val platform: String)
data class PaywallTierSelected(val tier: String, val productId: String, val source: String, val dimensions: FunnelDimensions? = null) : AnalyticsEvent("paywall_tier_selected")
// PaywallViewed / PurchaseStarted / PurchaseCompleted / PurchaseFailed / PurchaseRestored each gain
// `val dimensions: FunnelDimensions? = null` as the LAST constructor param (existing call sites unaffected).
```

- [ ] **Step 1: Write the failing tests** (in `AnalyticsEventTest.kt`)

```kotlin
@Test fun `PaywallTierSelected has correct name`() =
    assertEquals("paywall_tier_selected", AnalyticsEvent.PaywallTierSelected("annual", "prod_1", "support_us").name)

@Test
fun `PaywallTierSelected exposes tier product and source params`() {
    val params = AnalyticsEvent.PaywallTierSelected("annual", "yearly_sub", "support_us").toParameters()
    assertEquals("annual", params["tier"])
    assertEquals("yearly_sub", params["product_id"])
    assertEquals("support_us", params["source"])
    assertFalse(params.containsKey("subscription_type"))
}

@Test
fun `funnel dimensions merge into event params when present`() {
    val dims = AnalyticsEvent.FunnelDimensions(
        subscriptionType = "free", isTrial = false,
        activeEntitlements = "", platform = "android"
    )
    val params = AnalyticsEvent.PaywallViewed("support_us", dims).toParameters()
    assertEquals("support_us", params["source"])
    assertEquals("free", params["subscription_type"])
    assertEquals(false, params["is_trial"])
    assertEquals("", params["active_entitlements"])
    assertEquals("android", params["platform"])
}

@Test
fun `purchase events carry dimensions when provided`() {
    val dims = AnalyticsEvent.FunnelDimensions("premium", true, "premium", "ios")
    assertEquals("premium", AnalyticsEvent.PurchaseCompleted("p", 39.99, dims).toParameters()["subscription_type"])
    assertEquals("ios", AnalyticsEvent.PurchaseFailed("p", "store_purchase", "NetworkError", dims).toParameters()["platform"])
    assertEquals(true, AnalyticsEvent.PurchaseRestored(true, dims).toParameters()["is_trial"])
    assertEquals("premium", AnalyticsEvent.PurchaseStarted("p", dims).toParameters()["active_entitlements"])
}
```

- [ ] **Step 2: Run to verify failure** — `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.analytics.AnalyticsEventTest"` → compile FAIL (unresolved symbols).

- [ ] **Step 3: Implement in `AnalyticsEvent.kt`**

Add inside the Subscription Events section:

```kotlin
/**
 * Subscriber-context dimensions stamped on every conversion-funnel event so the
 * funnel is sliceable in both Firebase and Datadog (spec 014 FR-4). Values of
 * [subscriptionType] match the RevenueCat `subscription_state` attribute
 * (SubscriptionType.name.lowercase()).
 */
data class FunnelDimensions(
    val subscriptionType: String,
    val isTrial: Boolean,
    val activeEntitlements: String,
    val platform: String
) {
    fun toParameters(): Map<String, Any> = mapOf(
        "subscription_type" to subscriptionType,
        "is_trial" to isTrial,
        "active_entitlements" to activeEntitlements,
        "platform" to platform
    )
}

/**
 * User tapped a specific tier card on a paywall, before the system purchase
 * sheet launches — the missing middle of the conversion funnel (spec 014 FR-2).
 */
data class PaywallTierSelected(
    val tier: String,
    val productId: String,
    val source: String,
    val dimensions: FunnelDimensions? = null
) : AnalyticsEvent("paywall_tier_selected") {
    override fun toParameters() = buildMap {
        put("tier", tier)
        put("product_id", productId)
        put("source", source)
        dimensions?.let { putAll(it.toParameters()) }
    }
}
```

Then extend the five existing funnel events — each gains `val dimensions: FunnelDimensions? = null` as the last constructor param and merges `dimensions?.let { putAll(it.toParameters()) }` into `toParameters()` (converting `mapOf`-style bodies to `buildMap`):

```kotlin
data class PaywallViewed(val source: String, val dimensions: FunnelDimensions? = null) :
    AnalyticsEvent("paywall_viewed") {
    override fun toParameters() = buildMap {
        put("source", source)
        dimensions?.let { putAll(it.toParameters()) }
    }
}

data class PurchaseStarted(val productId: String, val dimensions: FunnelDimensions? = null) :
    AnalyticsEvent("purchase_started") {
    override fun toParameters() = buildMap {
        put("product_id", productId)
        dimensions?.let { putAll(it.toParameters()) }
    }
}

data class PurchaseCompleted(
    val productId: String,
    val revenue: Double? = null,
    val dimensions: FunnelDimensions? = null
) : AnalyticsEvent("purchase_completed") {
    override fun toParameters() = buildMap {
        put("product_id", productId)
        revenue?.let { put("revenue", it) }
        dimensions?.let { putAll(it.toParameters()) }
    }
}

// PurchaseFailed: add `val dimensions: FunnelDimensions? = null` after errorCode, same merge.
// PurchaseRestored: add `val dimensions: FunnelDimensions? = null` after success, same merge.
```

- [ ] **Step 4: Run to verify pass** — same command → PASS (including all pre-existing event tests).

- [ ] **Step 5: Stage commit message** — `feat(analytics): add paywall_tier_selected and funnel dimensions to conversion events`

---

### Task 2: ViewModel funnel methods — dimensions, tier-tap, Datadog dual-pipeline, user properties

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt`
- Test: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModelTest.kt`

**Interfaces:**
- Consumes: Task 1 types; `BillingManager.getActiveEntitlements(): Set<String>`; `getPlatform().type` (`me.calebjones.spacelaunchnow.Platform.kt`); `DatadogLogger.info(message, attributes)` (`analytics/DatadogConfig.kt:50`, no-op on desktop).
- Produces (used by Task 3): `fun trackTierSelected(type: ProductType, productId: String, source: String = "support_us")`; `trackPaywallViewed(source)` unchanged signature.

- [ ] **Step 1: Write the failing tests** (append to the 018 analytics section of `SubscriptionViewModelTest.kt`; `ProductType` is in the same package)

```kotlin
@Test
fun `trackTierSelected emits paywall_tier_selected with dimensions`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.trackTierSelected(ProductType.ANNUAL, "yearly_sub")
    advanceUntilIdle()

    val event = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PaywallTierSelected>().single()
    assertEquals("annual", event.tier)
    assertEquals("yearly_sub", event.productId)
    assertEquals("support_us", event.source)
    assertNotNull(event.dimensions)
    assertEquals("desktop", event.dimensions!!.platform) // tests run on the desktop JVM target
}

@Test
fun `trackPaywallViewed carries dimensions`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.trackPaywallViewed("support_us")
    advanceUntilIdle()

    val event = fake.trackedEvents.filterIsInstance<AnalyticsEvent.PaywallViewed>().single()
    assertEquals("support_us", event.source)
    assertNotNull(event.dimensions)
}

@Test
fun `purchase outcome events carry dimensions`() = runTest {
    val fake = FakeAnalyticsProvider()
    billingManager.shouldLaunchPurchaseFail = true
    billingManager.purchaseFailureException =
        PurchaseFlowException("store_purchase", "user_cancelled", true, "cancelled")
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))

    vm.purchaseProduct("yearly_sub", "yearly-base")
    advanceUntilIdle()

    assertNotNull(fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseStarted>().single().dimensions)
    assertNotNull(fake.trackedEvents.filterIsInstance<AnalyticsEvent.PurchaseFailed>().single().dimensions)
}

@Test
fun `subscription state changes push funnel user properties`() = runTest {
    val fake = FakeAnalyticsProvider()
    val vm = SubscriptionViewModel(repository, billingManager, analyticsWith(fake))
    advanceUntilIdle()

    assertEquals("free", fake.userProperties["subscription_type"])
    assertEquals("false", fake.userProperties["is_trial"])
    assertNotNull(fake.userProperties["active_entitlements"])
    assertEquals("desktop", fake.userProperties["platform"])
}
```

Note: `FakeAnalyticsProvider.userProperties` and `setUserProperty` already exist. If `MockSubscriptionRepository`'s initial state isn't `FREE`, adjust the expected value to the mock's default — assert against the mock's actual initial `subscriptionType.name.lowercase()`.

- [ ] **Step 2: Run to verify failure** — `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest"` → compile FAIL (`trackTierSelected` unresolved).

- [ ] **Step 3: Implement in `SubscriptionViewModel.kt`**

Imports to add: `me.calebjones.spacelaunchnow.PlatformType`, `me.calebjones.spacelaunchnow.analytics.DatadogLogger`, `me.calebjones.spacelaunchnow.getPlatform` (verify exact names — `PlatformType` lives wherever `Platform.kt` declares it).

Replace the Analytics section (`trackPaywallViewed`) with:

```kotlin
// ========== Analytics (spec 014 conversion funnel) ==========

private fun platformName(): String = when (getPlatform().type) {
    PlatformType.ANDROID -> "android"
    PlatformType.IOS -> "ios"
    PlatformType.DESKTOP -> "desktop"
}

/** Single source for the FR-4 subscriber-context dimensions — do not read state at call sites. */
private fun funnelDimensions(): AnalyticsEvent.FunnelDimensions {
    val state = subscriptionState.value
    return AnalyticsEvent.FunnelDimensions(
        subscriptionType = state.subscriptionType.name.lowercase(),
        isTrial = state.isInTrialPeriod,
        activeEntitlements = billingManager.getActiveEntitlements().sorted().joinToString(","),
        platform = platformName()
    )
}

/** Dual-pipeline emission: Firebase via AnalyticsManager, Datadog explicitly (FR-6). */
private fun trackFunnelStep(event: AnalyticsEvent) {
    analyticsManager.track(event)
    DatadogLogger.info(event.name, event.toParameters())
}

fun trackPaywallViewed(source: String) {
    trackFunnelStep(AnalyticsEvent.PaywallViewed(source = source, dimensions = funnelDimensions()))
}

fun trackTierSelected(type: ProductType, productId: String, source: String = "support_us") {
    val tier = when (type) {
        ProductType.MONTHLY -> "monthly"
        ProductType.ANNUAL -> "annual"
        ProductType.LIFETIME -> "lifetime"
    }
    trackFunnelStep(
        AnalyticsEvent.PaywallTierSelected(
            tier = tier,
            productId = productId,
            source = source,
            dimensions = funnelDimensions()
        )
    )
}
```

In `init`, after the existing block, sync user properties (Q1 recommended approach):

```kotlin
// Mirror funnel dimensions to Firebase user properties so ALL events are
// segmentable, not just funnel steps (spec 014 FR-4, Q1 recommended approach).
analyticsManager.setUserProperty("platform", platformName())
viewModelScope.launch {
    subscriptionState.collect { state ->
        analyticsManager.setUserProperty("subscription_type", state.subscriptionType.name.lowercase())
        analyticsManager.setUserProperty("is_trial", state.isInTrialPeriod.toString())
        analyticsManager.setUserProperty(
            "active_entitlements",
            billingManager.getActiveEntitlements().sorted().joinToString(",")
        )
    }
}
```

In `purchaseProduct`, attach dimensions and route outcomes through the dual pipeline:
- `PurchaseStarted(productId = productId)` → `AnalyticsEvent.PurchaseStarted(productId = productId, dimensions = funnelDimensions())` (Firebase-only `analyticsManager.track` is fine — FR-6 requires Datadog for view/tier/completed/failed).
- The success-branch `PurchaseCompleted(...)` call → `trackFunnelStep(AnalyticsEvent.PurchaseCompleted(productId = productId, revenue = priceAmountMicros?.let { it / 1_000_000.0 }, dimensions = funnelDimensions()))`.
- The failure-branch `PurchaseFailed(...)` call → `trackFunnelStep(AnalyticsEvent.PurchaseFailed(productId = productId, step = step, errorCode = errorCode, dimensions = funnelDimensions()))`.

In `restorePurchases` (lines ~284/295), add `dimensions = funnelDimensions()` to both `PurchaseRestored` constructions (plain `analyticsManager.track` stays — restore is an exit, not a Datadog-required step).

- [ ] **Step 4: Run to verify pass** — full class: `./gradlew :composeApp:desktopTest --tests "me.calebjones.spacelaunchnow.ui.viewmodel.SubscriptionViewModelTest"` → PASS.

- [ ] **Step 5: Stage commit message** — `feat(analytics): stamp funnel dimensions and dual-pipeline emission in SubscriptionViewModel`

---

### Task 3: SupportUsScreen wiring — view tracking + tier taps

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`

**Interfaces:** Consumes Task 2's `trackPaywallViewed` / `trackTierSelected`. `LaunchedEffect` already imported (line 52); `ProductType` already imported (used at lines 105–107).

- [ ] **Step 1: Add the view event** — after the state declarations (~line 107):

```kotlin
// Funnel top (spec 014 FR-1): once per presentation, mirroring OnboardingPaywallScreen.
LaunchedEffect(Unit) {
    viewModel.trackPaywallViewed("support_us")
}
```

- [ ] **Step 2: Add tier-tap tracking to the three lambdas**

Yearly `PricingCard` `onSubscribe`:
```kotlin
onSubscribe = {
    if (annualProduct != null) {
        viewModel.trackTierSelected(ProductType.ANNUAL, annualProduct.productId)
        viewModel.purchaseProduct(annualProduct)
    }
},
```
Monthly `PricingCard` `onSubscribe`: same with `ProductType.MONTHLY` / `monthlyProduct`.
`ProLifetimeCard` `onPurchase`: same with `ProductType.LIFETIME` / `lifetimeProduct`.

- [ ] **Step 3: Compile + full test check** — `./gradlew compileKotlinDesktop :composeApp:desktopTest :composeApp:compileDebugKotlinAndroid` → PASS.

- [ ] **Step 4: Stage commit message** — `feat(analytics): instrument Support-Us paywall view and tier selection`

---

### Task 4: FR-7 funnel-view documentation (quickstart.md)

**Files:**
- Create: `specs/014-instrument-conversion-funnel/quickstart.md`

- [ ] **Step 1: Write the doc.** Contents: (a) the event taxonomy table (all six steps, params, dimensions); (b) GA4 funnel setup — Explore → Funnel exploration, steps `paywall_viewed` (param `source = support_us`) → `paywall_tier_selected` → `purchase_started` → `purchase_completed`, with `purchase_failed` as an exit breakdown, segmented by the four user properties; (c) the Datadog Logs query (`@type:paywall_viewed OR @type:paywall_tier_selected ...` — service/source per `DatadogConfig`) and note that Datadog reads event **params**, not user properties; (d) DebugView verification steps (`adb shell setprop debug.firebase.analytics.app me.calebjones.spacelaunchnow.kmpdebug`); (e) note that GA4 explorations cannot be created via API — one-time manual console setup, ~10 minutes. **No revenue/subscriber dollar figures in this file** — the repo is public.

- [ ] **Step 2: Stage commit message** — `docs(specs): add 014 funnel quickstart and mark spec implemented` (also flip `Status: Draft` → `Status: Implemented (code); funnel view pending console setup` in spec.md and check off the code-side Success Criteria boxes).

---

### Task 5: Verification gate + Caleb checkpoint

- [ ] **Step 1:** `./gradlew :composeApp:desktopTest compileKotlinDesktop :composeApp:compileDebugKotlinAndroid` — all PASS.
- [ ] **Step 2:** `./gradlew :composeApp:assembleDebug` — report APK path.
- [ ] **Step 3: STOP.** Hand to Caleb: open Support Us → DebugView shows `paywall_viewed{source=support_us}` with the four dimensions; tap a tier → `paywall_tier_selected{tier,product_id}` before the sheet; cancel → `purchase_failed` with dimensions; check Datadog Logs for the same steps. Commits (messages staged per task) happen only after his confirmation.

## Self-Review Notes

- Spec coverage: FR-1→Task 3, FR-2→Tasks 1–3, FR-3→done in 018 (deviation documented), FR-4→Tasks 1–2 (Q1 recommended), FR-5→done in 018, FR-6→Task 2 (`trackFunnelStep`), FR-7→Task 4 (doc; console setup is manual by design).
- Type consistency: `FunnelDimensions` nested in `AnalyticsEvent` (referenced as `AnalyticsEvent.FunnelDimensions` from the ViewModel); `trackTierSelected(type: ProductType, ...)` matches Task 3 call sites.
- Non-goals honored: no onboarding tier-tap, no PremiumFeatureGate changes, no pricing changes.
