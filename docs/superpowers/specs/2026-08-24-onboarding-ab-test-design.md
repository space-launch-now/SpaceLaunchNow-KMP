# Onboarding Instrumentation + Shortened-Flow A/B Test — Design

**Date:** 2026-08-24
**Status:** Approved approach (Firebase A/B on existing Remote Config); spec pending review
**Owner:** Caleb Jones

## Problem

The onboarding flow (Preload → 5-page LiveOnboarding pager → OnboardingPaywall → Home) loses
23% of entrants before the paywall (7,646 → 5,907 users / 28d), averages ~55s in the pager plus
~20s on the paywall, and ~70% of current onboarding traffic is returning users on the
legacy-migration wave — for whom a ~75s forced flow is high friction. Hypothesis: a shorter
onboarding increases paywall reach and purchase conversion.

Today the paywall's middle is unmeasured: no dismissal event, no tier-tap from the onboarding
paywall, purchase events carry no `source` (onboarding vs support_us purchases are
indistinguishable), and notification-permission outcomes are not tracked at all.

## Decisions (made 2026-08-24)

| Decision | Choice |
|---|---|
| Variant flow | **Welcome → Notification permission → Paywall → Home** (cut Launch card, News, Widgets pages; keep relative order) |
| Primary metric | **Purchase conversion** (`purchase_completed`) |
| Audience | **Everyone entering onboarding** (new + migrating); slice new-vs-returning post-hoc in GA4 |
| Mechanism | **Firebase A/B Testing** on the existing `RemoteConfigRepository` (GitLive KMP `firebase-config`) |

Rejected: local hash-based assignment (manual stats, no remote kill — a release-gated lever is
unacceptable at iOS release cost); RC percentage-conditions without Firebase A/B (same analysis
burden, none of the lifecycle).

## Phase 1 — Instrumentation (ships with or before the experiment; both arms need it)

New events are dual-pipeline (Firebase via `AnalyticsManager` + Datadog via `DatadogLogger`).
Funnel-step events are emitted through `SubscriptionViewModel.trackFunnelStep` and stamped with
`FunnelDimensions`; `notification_permission_result` (§1.5) is a guardrail metric — dual-pipeline
but deliberately without `FunnelDimensions`, emitted from `OnboardingViewModel`.

### 1.1 `paywall_tier_selected` from the onboarding paywall

`OnboardingContent.onSubscribe` currently calls `purchaseProduct(product)` directly
(`OnboardingPaywallScreen.kt:158`), skipping the tier event. Change the callback to carry the
tier (`(ProductType, ProductInfo) -> Unit`) and call
`trackTierSelected(type, product.productId, source = "onboarding")` before `purchaseProduct`.

### 1.2 New event: `paywall_dismissed`

```kotlin
data class PaywallDismissed(
    val source: String,            // "onboarding"
    val secondsOnScreen: Long,
    val dimensions: FunnelDimensions? = null
) : AnalyticsEvent("paywall_dismissed")
```

Emitted from `OnboardingPaywallScreen.onDismiss` via a new
`SubscriptionViewModel.trackPaywallDismissed(source, secondsOnScreen)`. Time on screen measured
with `TimeSource.Monotonic` marked when the screen's `LaunchedEffect(Unit)` fires. Scope is the
onboarding paywall only (Support Us has no explicit dismiss path; YAGNI).

### 1.3 `source` on all four purchase events

`PurchaseStarted`, `PurchaseCompleted`, `PurchaseFailed`, `PurchaseRestored` gain
`source: String`. Threaded through `purchaseProduct(productId, basePlanId, priceAmountMicros,
source = "support_us")` and `restorePurchases(source = "support_us")`; the onboarding paywall
passes `"onboarding"`. `purchase_completed` fires from the async success path, so the ViewModel
captures the source at call time (member field set in `purchaseProduct`), not at emission time.

### 1.4 `onboarding_step` gains `page` and `variant`

```kotlin
data class OnboardingStep(
    val step: Int,                 // index within the variant's page list
    val page: String,              // stable name: welcome | launch_card | news_events | widgets | notification_permission
    val variant: String,           // control | short
    val completed: Boolean
) : AnalyticsEvent("onboarding_step")
```

`step` indices renumber between variants; `page` is the cross-variant join key. Firebase-only
emission (unchanged from today).

### 1.5 New event: `notification_permission_result`

```kotlin
data class NotificationPermissionResult(
    val granted: Boolean,
    val source: String,            // "onboarding"
    val variant: String
) : AnalyticsEvent("notification_permission_result")
```

Emitted from `NotificationPermissionPage.onPermissionResult` / `onSkip` (skip = `granted=false`).
This is the experiment's guardrail: the variant moves the permission ask from page 5 to page 2,
which could depress opt-in. Also partially addresses backlog item #4 (no notification
denominator).

### 1.6 GA4 console registrations (manual, or via Admin API after `analytics.edit` re-auth)

In addition to the already-agreed list (`source`, `tier`, `product_id`, `step`, `error_code`,
`success`, `subscription_type`, `is_trial`, `active_entitlements`, `platform` + `revenue`
metric): event-scoped dimensions **`page`**, **`variant`**, **`completed`**, **`granted`**;
custom metric **`seconds_on_screen`** (standard unit). Registration is forward-only — do it
before the release train reaches production.

Firebase A/B result reporting does NOT depend on these registrations (experiment membership is
attached automatically); they serve GA4 Data API slicing and Datadog cross-checks.

## Phase 2 — Variant + experiment plumbing

### 2.1 Remote Config parameter

- Parameter: `onboarding_variant`, values `"control"` (default) / `"short"`.
- `RemoteConfigRepository.getOnboardingVariant(): OnboardingVariant` — enum `CONTROL`, `SHORT`;
  unknown strings fall back to `CONTROL`. Default registered in `setDefaults()`.

### 2.2 Fetch before onboarding (Preload gate)

Remote Config is currently fetched lazily by feature ViewModels — nothing runs before
onboarding. Change: when live onboarding has NOT been completed (`AppPreferences`), the Preload
step awaits `fetchAndActivate()` under `withTimeoutOrNull(3s)` before signaling
`onPreloadComplete`. When onboarding is already done, no fetch is added (existing lazy behavior
unchanged). On timeout/failure the variant resolves to `CONTROL`.

### 2.3 Variant persistence (assignment stability)

The variant actually used is written to `AppPreferences.onboardingVariant` at first onboarding
entry and read from there ever after — a process death mid-onboarding, or a late-arriving RC
fetch, must not flip the experience. Accepted limitation: first-launch-offline users run
`control` and (because Firebase only tags events fired after config activation) drop out of the
experiment rather than polluting it.

### 2.4 LiveOnboardingScreen refactor

Replace the hardcoded `PAGE_COUNT`/`when(page)` with a variant-driven page list:

```kotlin
enum class OnboardingPage { WELCOME, LAUNCH_CARD, NEWS_EVENTS, WIDGETS, NOTIFICATION_PERMISSION }

fun pagesFor(variant: OnboardingVariant): List<OnboardingPage> = when (variant) {
    CONTROL -> listOf(WELCOME, LAUNCH_CARD, NEWS_EVENTS, WIDGETS, NOTIFICATION_PERMISSION)
    SHORT   -> listOf(WELCOME, NOTIFICATION_PERMISSION)
}
```

Pager count, progress bar, and step tracking derive from the list. Skip button (hidden on
first/last page) naturally never renders in `short` — nothing to skip. `short` skips fetching
articles/schedule/explore content (only `nextLaunch` for the Welcome page). The paywall screen
itself is unchanged.

### 2.5 Firebase console setup (manual)

Two experiments (Android + iOS are separate Firebase apps), each: parameter `onboarding_variant`,
50/50 `control`/`short`, **activation event `onboarding_step`** (enrolls exactly the
everyone-in-onboarding audience), goal metric `purchase_completed`, secondary metrics: retention
(built-in) and `notification_permission_result`. Start both experiments only after the release
train containing Phases 1+2 is live on the respective store.

## Analysis plan & expectations

- **Primary read:** Firebase A/B console per platform (purchase_completed).
- **Guardrails (manual GA4/Datadog):** notification grant rate (`notification_permission_result`),
  pager completion (`onboarding_step` with `completed=true` per entrant), D1 retention.
- **Power honesty:** at ~7.8K entrants/28d and ~0.9% entrant→purchase baseline, detecting a +50%
  relative lift needs roughly 9K entrants per arm → **expect ~2–3 months** for purchase
  significance. Guardrails read within weeks. The migration wave will decay over the run —
  slice new-vs-returning before declaring a winner.
- Kill/pause is a Firebase console action (stop experiment → parameter serves default `control`).

## Files touched

| File | Change |
|---|---|
| `analytics/events/AnalyticsEvent.kt` | `PaywallDismissed`, `NotificationPermissionResult`, `source` on 4 purchase events, `page`/`variant` on `OnboardingStep` |
| `ui/viewmodel/SubscriptionViewModel.kt` | `trackPaywallDismissed`, source threading + capture |
| `ui/viewmodel/OnboardingViewModel.kt` | `trackOnboardingStep(step, page, variant, completed)` |
| `ui/onboarding/OnboardingPaywallScreen.kt` | tier-tap tracking, dismiss tracking, source, time-on-screen |
| `ui/onboarding/LiveOnboardingScreen.kt` | page-list refactor, variant wiring, permission-result event |
| `ui/subscription/SupportUsScreen.kt` | no change expected (source defaults to `support_us`) |
| `data/repository/RemoteConfigRepository(+Impl).kt` | `getOnboardingVariant()`, default value |
| Preload (`PreloadScreen`/`PreloadViewModel`) | conditional fetch-with-timeout gate |
| `AppPreferences` | `onboardingVariant` persistence |
| commonTest | event shape tests, VM tracking tests, variant parse/fallback, `pagesFor` per variant, Preload gating |

No DI changes (existing singletons). No new dependencies (`gitlive-firebase-config` already in
`libs.versions.toml`).

## Testing

TDD per project workflow; run with `./gradlew :composeApp:desktopTest`. Manual verification via
Firebase DebugView on a debug build: force each variant via console parameter override, walk both
flows, confirm every event of §1 fires with correct params — including the tier tap that spec
014's checkpoint never verified.

## Out of scope

- Support Us screen dismissal tracking.
- Any change to paywall content/pricing (this experiment varies flow length only).
- Renaming `notification_received` / notification funnel work (backlog #4, spec 018 US5).
- Datadog emission for `onboarding_step` (stays Firebase-only).
