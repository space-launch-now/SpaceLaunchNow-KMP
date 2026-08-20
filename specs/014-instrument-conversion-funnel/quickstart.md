# Quickstart: Reading the Support-Us Conversion Funnel

The funnel instrumented by this spec (plus the failure/revenue legs shipped with spec 018):

| Step | Event | Key params | Fired from |
|---|---|---|---|
| 1 View | `paywall_viewed` | `source` | `SupportUsScreen` (`source=support_us`), `OnboardingPaywallScreen` (`onboarding`), `PremiumFeatureGate`, `SubscriptionViewModel` |
| 2 Tier tap | `paywall_tier_selected` | `tier` (annual/monthly/lifetime), `product_id`, `source` | the three tier cards on `SupportUsScreen` |
| 3 Start | `purchase_started` | `product_id` | `SubscriptionViewModel.purchaseProduct`, before the store sheet |
| 4a Done | `purchase_completed` | `product_id`, `revenue` (price units, single-currency — multi-currency normalization is a flagged follow-up) | success branch |
| 4b Fail | `purchase_failed` | `product_id`, `step` (setup/store_purchase), `error_code` (`user_cancelled` or a RevenueCat `PurchasesErrorCode` name) | failure branch |
| exit | `purchase_restored` | `success` | restore flow |

**Dimensions (FR-4)** — stamped as params on every step above *and* mirrored as Firebase user properties whenever subscription state changes: `subscription_type` (free/legacy/premium/lifetime), `is_trial`, `active_entitlements` (sorted, comma-joined), `platform` (android/ios/desktop).

## Firebase (primary funnel view)

GA4 explorations cannot be created via API — one-time manual setup (~10 min), in the
**Firebase-linked GA4 property** (the app property, not the web one):

1. GA4 → **Explore → Funnel exploration**.
2. Steps, in order: `paywall_viewed` (add condition: event param `source` = `support_us`) →
   `paywall_tier_selected` → `purchase_started` → `purchase_completed`.
3. Set the funnel **open** (users can enter at any step) OFF — closed funnel.
4. Breakdown: user property `subscription_type` (or `platform`, `is_trial`).
5. To see exits: add `purchase_failed` as a segment or a trended-funnel comparison; slice by
   `error_code` to separate `user_cancelled` (sheet abandonment) from real billing errors.
6. Save as **"Support-Us Conversion Funnel"**.

Note: freshly used custom params/user properties need to be registered as **custom definitions**
(Admin → Custom definitions) before they appear as breakdown options; allow 24–48h for new
dimensions to populate.

## Datadog

Datadog reads event **params** (not Firebase user properties). The funnel steps arrive as logs
from logger `SLN` (message = event name, attributes = params). Log Explorer query:

```
service:* @logger.name:SLN (paywall_viewed OR paywall_tier_selected OR purchase_started OR purchase_completed OR purchase_failed)
```

Filter `@source:support_us`, group by message to get step counts, or build a saved view /
dashboard with one query per step. Slice with `@subscription_type`, `@is_trial`, `@platform`.

## Verifying on a device (DebugView)

```bash
adb shell setprop debug.firebase.analytics.app me.calebjones.spacelaunchnow.kmpdebug
```

Open Support Us → `paywall_viewed{source=support_us, subscription_type, is_trial, active_entitlements, platform}` appears once. Tap a tier → `paywall_tier_selected{tier, product_id}` fires **before** the store sheet. Cancel the sheet → `purchase_failed{error_code=user_cancelled}`. Complete a test purchase → `purchase_completed{revenue}`. Disable with `adb shell setprop debug.firebase.analytics.app .none.`.

On iOS: run with `-FIRDebugEnabled` launch argument in Xcode.
