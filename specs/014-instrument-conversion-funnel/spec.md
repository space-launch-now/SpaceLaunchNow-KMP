# Feature Spec: Instrument the Subscription Conversion Funnel

**Branch**: `014-instrument-conversion-funnel` | **Priority**: P0 (Monetization) | **Status**: Draft

**Source**: `docs/business/MONETIZATION_TODO.md` → P0. Business-analysis run on live RevenueCat
data (project `projbe17841f`, trailing 28 days): free→paid conversion ≈ **0.88%** (below the 1–3%
freemium norm), blended ARPPU ≈ **$1.82/mo**. A 0.1pt conversion lift ≈ **+52 subscriptions**.

## Problem Statement

The `SupportUsScreen` paywall is the app's primary conversion surface, yet its funnel is only
partially instrumented — so every pricing, copy, or tier-ordering change below it is currently
**unmeasurable**. The analytics pipeline itself already exists (spec `011-analytics-module`:
`AnalyticsManager` fan-out → Firebase Analytics on Android/iOS, Console on desktop), and
`SubscriptionViewModel` already emits some purchase events. What's missing is complete, consistent
coverage of the funnel and the dimensions needed to slice it.

Concretely, four gaps prevent a usable funnel:

1. **No paywall-view event from `SupportUsScreen`.** `PaywallViewed` is emitted only from
   `OnboardingPaywallScreen` (`OnboardingPaywallScreen.kt:109`). The standalone Support-Us paywall —
   the highest-intent surface — fires nothing on view. The funnel has no top.
2. **No tier-tap event.** The first event today fires *inside* `purchaseProduct` (`PurchaseStarted`),
   after the user has already committed to the system purchase sheet. There is no event at the moment
   a user taps a specific tier card, so "viewed → considered a tier → started purchase" is invisible.
   No `purchase_*` taxonomy entry covers this.
3. **No purchase-failure event.** `SubscriptionViewModel.purchaseProduct` tracks `PurchaseStarted`
   and, on success, `PurchaseCompleted`; on failure it only logs. The funnel cannot distinguish
   "user abandoned the purchase sheet / payment failed" from "user never started," so drop-off
   between start and completion is unattributable.
4. **No funnel dimensions on events.** Funnel events carry minimal params and no
   subscription-state context. We cannot segment conversion by `subscription_type`, trial status,
   active entitlements, or `platform`, and `PurchaseCompleted` carries no `revenue`, so per-tier
   revenue can't be derived from the event stream.

## Goals

1. **Complete, four-step funnel** on the Support-Us paywall:
   **paywall viewed → tier tapped → purchase started → purchase completed / failed / restored**,
   every step emitted through the existing `AnalyticsManager`.
2. **Sliceable funnel** — every funnel event carries a consistent set of subscription-state
   dimensions (`subscription_type`, `is_trial`, `active_entitlements`, `platform`, `source`) so
   conversion can be segmented without new instrumentation.
3. **Per-tier revenue** — `PurchaseCompleted` carries the product's price so the funnel doubles as a
   revenue-by-tier view.
4. **Dual pipeline** — funnel steps reach **Firebase Analytics** (via `AnalyticsManager.track`) and
   **Datadog** (via `DatadogLogger.info`), mirroring the established `OnboardingPaywallScreen` idiom,
   so the funnel is visible in both tools.
5. **A working funnel view** defined in the analytics backend (Firebase / Datadog) so that
   paywall and pricing changes (P1 items in the TODO) become measurable the moment they ship.

## Non-Goals

- Building or replacing analytics infrastructure — `011-analytics-module` already delivered it.
- Instrumenting other paywall surfaces beyond `SupportUsScreen` (onboarding is already covered;
  `PremiumFeatureGate` trial-CTA instrumentation is **P1**, a separate spec).
- Changing pricing, tier ordering, or paywall copy — those are the P1 decisions this work *enables*,
  not part of it.
- A/B testing or experimentation framework.
- Surfacing AdMob revenue (TODO P2) or RevenueCat config hygiene (TODO P2).
- Building in-app dashboards — the funnel view lives in Firebase/Datadog.

## Current State (verified)

| Element | Status | Location |
|---|---|---|
| `AnalyticsManager.track(event)` fan-out | ✅ exists | `analytics/core/AnalyticsManager.kt` |
| `PaywallViewed(source)` event | ✅ exists | `analytics/events/AnalyticsEvent.kt` |
| `PurchaseStarted(productId)` event | ✅ exists | `AnalyticsEvent.kt` |
| `PurchaseCompleted(productId, revenue?)` event | ✅ exists (revenue unused) | `AnalyticsEvent.kt` |
| `PurchaseRestored(success)` event | ✅ exists | `AnalyticsEvent.kt` |
| `SubscriptionViewModel.trackPaywallViewed(source)` | ✅ exists, **not called by SupportUsScreen** | `ui/viewmodel/SubscriptionViewModel.kt` |
| Tier-tap event | ❌ **missing** | — |
| Purchase-failure event | ❌ **missing** (failure only logged) | `SubscriptionViewModel.purchaseProduct` |
| Subscription-state attributes on events | ❌ **missing** | — |
| `activeEntitlements` source | ✅ available | `data/model/PurchaseState.kt`, `BillingManager.getActiveEntitlements()` |
| `subscription_type` / `is_trial` source | ✅ available | `data/model/SubscriptionState.kt` |
| `platform` derivation | ✅ available | `RevenueCatAttributesSyncer.platformString()` / `getPlatform().type` |

## Functional Requirements

### FR-1: Paywall-view tracking on `SupportUsScreen`

When `SupportUsScreen` becomes visible, emit the paywall-view step exactly once per presentation,
with `source = "support_us"`.

- Use a `LaunchedEffect(Unit)` in `SupportUsScreen.kt` calling `viewModel.trackPaywallViewed("support_us")`,
  mirroring `OnboardingPaywallScreen.kt:109`.
- The event must carry the FR-4 subscription-state dimensions.

### FR-2: Tier-tap tracking

Add a new typed event fired when a user taps a specific tier card, **before** the system purchase
flow launches.

- New `AnalyticsEvent` subclass — `PaywallTierSelected(tier: String, productId: String, source: String)`
  → event name `"paywall_tier_selected"`, params `{tier, product_id, source}` plus FR-4 dimensions.
  `tier` ∈ {`"annual"`, `"monthly"`, `"lifetime"`}.
- Fired from the three tier lambdas in `SupportUsScreen.kt` (Yearly `PricingCard` ~line 313, Monthly
  ~336, `ProLifetimeCard` ~370) — routed through a single `viewModel.trackTierSelected(tier, productId)`
  method to keep call sites thin and consistent (matching the existing ViewModel-method idiom, e.g.
  `EventViewModel.trackLinkOpened`).

### FR-3: Purchase-failure tracking

Add a failure leg so start→outcome is always closed.

- New `AnalyticsEvent` subclass — `PurchaseFailed(productId: String, reason: String? = null)` →
  event name `"purchase_failed"`, params `{product_id, reason?}` plus FR-4 dimensions. `reason` is a
  coarse, **non-PII** classification (e.g. `"cancelled"`, `"payment_error"`, `"network"`,
  `"already_owned"`, `"unknown"`) derived from the billing result — never a raw error string that
  could carry user data.
- Emitted from the failure branch of `SubscriptionViewModel.purchaseProduct` (the branch that today
  only logs). User-cancellation must be classified distinctly from payment/network errors.

### FR-4: Subscription-state dimensions on every funnel event

Every funnel event (FR-1, FR-2, FR-3, and the existing `PurchaseStarted` / `PurchaseCompleted` /
`PurchaseRestored`) must be sliceable by current subscriber context.

- Dimensions: `subscription_type` (`free`/`legacy`/`premium`/`lifetime`), `is_trial` (bool),
  `active_entitlements` (joined, stable string), `platform` (`android`/`ios`/`desktop`).
- Source of truth: `viewModel.subscriptionState.value` (`SubscriptionState`) and
  `billingManager.purchaseState.value.activeEntitlements` (or `billingManager.getActiveEntitlements()`);
  platform via `getPlatform().type`, mirroring `RevenueCatAttributesSyncer.platformString()`.
- **Implementation approach (recommended):** set these as **Firebase user properties** via
  `analyticsManager.setUserProperty(...)` whenever `SubscriptionState` changes, so *all* events
  (not just funnel events) become segmentable and event payloads stay minimal — **and** additionally
  stamp them as params on the funnel events themselves, so Datadog logs (which don't read user
  properties) are equally sliceable. Centralize the read in one ViewModel helper to avoid drift.
  *(Alternative — params only, no user properties — is simpler but leaves non-funnel events
  unsegmentable and duplicates the attribute read across call sites. See Open Question Q1.)*
- Keep the param/attribute **keys identical** to those already pushed by
  `RevenueCatAttributesSyncer` (`platform`, `subscription_state`, …) where they overlap, so the three
  systems (RevenueCat attributes, Firebase, Datadog) line up.

### FR-5: Revenue on purchase completion

`SubscriptionViewModel.purchaseProduct` must pass the purchased product's price to
`PurchaseCompleted(productId, revenue)` (currently called with revenue omitted). Source the price
from the `ProductInfo`/`ProductType` the purchase was launched with. Revenue is reported in the
product's price units; document the currency assumption in the event (single-currency for now —
flag multi-currency normalization as a follow-up, not a blocker).

### FR-6: Dual-pipeline emission for funnel steps

Each funnel step reaches both backends, matching `OnboardingPaywallScreen.kt:109`:

- **Firebase**: `analyticsManager.track(event)` (existing path; Datadog is *not* in the
  `AnalyticsProvider` fan-out, so this alone does not reach Datadog).
- **Datadog**: an explicit `DatadogLogger.info("<step>", mapOf(...))` for the view, tier-tap, and
  completed/failed steps, carrying the same FR-4 dimensions.
- The dual call is encapsulated in the relevant `SubscriptionViewModel` `trackXxx` method (one place),
  not duplicated across composable call sites.

### FR-7: Funnel view in the analytics backend

A funnel report is defined and saved in the analytics backend (deliverable, not app code):

- Ordered steps: `paywall_viewed` → `paywall_tier_selected` → `purchase_started` →
  `purchase_completed` (with `purchase_failed` / `purchase_restored` visible as exits), filtered to
  `source = "support_us"`, segmentable by the FR-4 dimensions.
- Documented in `quickstart.md` (how to read it, where it lives) so P1 paywall changes can be
  evaluated against it.

## Technical Constraints

- Adheres to `011-analytics-module`: new events are `AnalyticsEvent` subclasses with snake_case
  `name` and `toParameters()`; **no magic strings at call sites**; no DI changes required for new
  events (only new providers would touch DI).
- Respects ADR-0001 layering: ViewModels depend on `AnalyticsManager` / repositories, never on
  providers or mappers directly. Composables call thin `viewModel.trackXxx(...)` methods.
- Event dispatch stays fire-and-forget on the existing `SupervisorJob + Dispatchers.Default` scope —
  **zero added main-thread work**, no added latency to the purchase flow.
- **Privacy/PII**: `reason` strings and all attributes are coarse classifications only — no raw error
  text, emails, user ids, or order ids in event params. Respect existing per-provider consent
  (`AnalyticsPreferences`); funnel events are subject to the same opt-out as all analytics.
- Must not regress existing Datadog RUM/logging or the existing `PurchaseStarted`/`Completed`/
  `Restored` events.
- Builds on all three platforms (Android, iOS, Desktop); desktop uses the Console provider (no-op for
  Firebase) and must compile and run.

## Testing

Per the analytics-module precedent (Constitution Principle VII), event taxonomy and ViewModel
tracking are unit-tested in `commonTest`:

- `AnalyticsEventTest` extended: `PaywallTierSelected` and `PurchaseFailed` produce the correct
  `name` and `toParameters()` (including null-omission for optional params).
- `SubscriptionViewModelTest` (using a `FakeAnalyticsProvider` / fake `AnalyticsManager` + fake
  `BillingManager`): asserts the full sequence — view, tier-tap, start, and on the billing result
  either completed-with-revenue **or** failed-with-classified-reason; asserts restore tracks
  success/failure; asserts FR-4 dimensions are attached.
- Manual: verify all four steps appear in Firebase **DebugView** and in Datadog for a real
  Support-Us purchase attempt on Android and iOS, and that the saved funnel (FR-7) populates.

## Success Criteria

- [ ] Opening `SupportUsScreen` emits `paywall_viewed` with `source = "support_us"` exactly once.
- [ ] Tapping each tier emits `paywall_tier_selected` with the correct `tier` + `product_id` before
      the purchase sheet launches.
- [ ] A failed/cancelled purchase emits `purchase_failed` with a coarse, non-PII `reason`;
      cancellation is distinguishable from payment/network failure.
- [ ] A successful purchase emits `purchase_completed` carrying non-zero `revenue`.
- [ ] All funnel events carry `subscription_type`, `is_trial`, `active_entitlements`, `platform`.
- [ ] Each funnel step is visible in **both** Firebase Analytics and Datadog.
- [ ] A saved funnel view exists in the analytics backend, filtered to `source = "support_us"` and
      segmentable by the FR-4 dimensions.
- [ ] `commonTest` covers the new events and the ViewModel funnel sequence; all platforms build;
      existing analytics + Datadog RUM unaffected.

## Open Questions

- **Q1 (FR-4 approach):** User properties + event params (recommended, segments *all* events) vs.
  event params only (simpler, funnel-only). Decision affects effort and breadth.
- **Q2 (FR-5 currency):** Single-currency revenue now with multi-currency normalization deferred —
  acceptable for the funnel's first iteration?
- **Q3 (FR-7 backend):** Build the primary funnel view in Firebase, Datadog, or both? (Both are
  wired; Firebase has native funnel reports, Datadog has the log pipeline + RUM.)

## References

- TODO: `docs/business/MONETIZATION_TODO.md` (P0)
- Analytics infra: `specs/011-analytics-module/` (spec, tasks all ✅), `analytics/events/AnalyticsEvent.kt`,
  `analytics/core/AnalyticsManager.kt`
- Paywall: `ui/subscription/SupportUsScreen.kt`, `ui/viewmodel/SubscriptionViewModel.kt`
- Dual-pipeline idiom: `ui/subscription/OnboardingPaywallScreen.kt:109`
- State sources: `data/model/SubscriptionState.kt`, `data/model/PurchaseState.kt`,
  `data/billing/RevenueCatAttributesSyncer.kt`
- Datadog: `analytics/DatadogConfig.kt`
