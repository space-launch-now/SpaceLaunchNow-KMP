# Research: Fix Subscription Free Trial Disclosure

**Date**: 2026-03-03  
**Branch**: `004-fix-subscription-trial-disclosure`

## Research Tasks & Findings

### 1. How does RevenueCat KMP SDK expose free trial information?

**Decision**: Use platform-divergent APIs unified through `ProductInfo` model extension.

**Rationale**: The RevenueCat KMP SDK (v1.9.0+14.3.0) exposes trial/intro pricing through two different mechanisms depending on platform:

**Android (Google Play)**:
- `StoreProduct.subscriptionOptions: SubscriptionOptions?` — Play Store only, contains all subscription options
- `SubscriptionOptions.freeTrial: SubscriptionOption?` — First option with a free trial `PricingPhase`
- `SubscriptionOptions.introOffer: SubscriptionOption?` — First option with an intro pricing `PricingPhase`
- `SubscriptionOption.pricingPhases: List<PricingPhase>` — Ordered pricing phases; free trial phase has `price.amountMicros == 0`
- `PricingPhase.billingPeriod: Period` — Duration of trial (e.g., `Period(value=3, unit=DAY)`)
- `PricingPhase.price: Price` — Price for this phase (`amountMicros=0` for free trial)

**iOS (App Store)**:
- `StoreProduct.introductoryDiscount: StoreProductDiscount?` — App Store only, introductory price/trial info
- `StoreProductDiscount.paymentMode: DiscountPaymentMode` — `FREE_TRIAL`, `PAY_AS_YOU_GO`, or `PAY_UP_FRONT`
- `StoreProductDiscount.subscriptionPeriod: Period` — Duration of the trial
- `StoreProductDiscount.numberOfPeriods: Long` — Number of periods (always 1 for free trials)
- `StoreProductDiscount.price: Price` — Price during discount (0 for free trial)

**Alternatives considered**:
- RevenueCat Paywalls UI (`purchases-kmp-ui` / `revenuecatui`): Already a dependency but would require migrating the entire subscription screen to RevenueCat's paywall builder. Too invasive, doesn't match existing custom UI.
- Hardcoding trial info: Violates NFR-2, fragile when trial config changes in Play Console.

### 2. How to detect if a current user is in a trial period?

**Decision**: Use `EntitlementInfo.periodType` from customer info.

**Rationale**: The `EntitlementInfo` class has:
- `periodType: PeriodType` — Enum with values `NORMAL`, `TRIAL`, `INTRO`
- `expirationDateMillis: Long?` — When trial ends (or subscription expires)
- `willRenew: Boolean` — Whether subscription auto-renews after trial

When `periodType == PeriodType.TRIAL`, the user is currently in a free trial. The `expirationDateMillis` gives the trial end date.

**Alternatives considered**:
- Tracking trial state locally: Unreliable, not cross-device.
- Using purchase date + known trial duration: Fragile, doesn't account for trial extensions.

### 3. Cross-platform approach for trial data extraction

**Decision**: Add trial fields to common `ProductInfo` data class, with platform-specific extraction in `AndroidBillingManager` and `IosBillingManager`.

**Rationale**: The `ProductInfo` model is already platform-agnostic. Adding `hasFreeTrial: Boolean`, `freeTrialPeriod: String?`, and `freeTrialDuration: String?` keeps the common UI code unchanged. Each platform's `getAvailableProducts()` implementation already maps `StoreProduct` → `ProductInfo`; we just need to add the trial fields to the mapping.

**Alternatives considered**:
- Moving trial detection to common code: Not feasible because `subscriptionOptions` is Android-only and `introductoryDiscount` is iOS-only.
- Using `expect`/`actual` for trial extraction function: Adds complexity without benefit since the existing `getAvailableProducts()` is already platform-specific.

### 4. Google Play Subscriptions Policy Requirements

**Decision**: Implement all four disclosure requirements from the policy.

**Rationale**: Google Play's Subscriptions policy (https://support.google.com/googleplay/android-developer/answer/9900533/) requires:

1. **Make clear a trial exists**: Show "X-day free trial" prominently on the pricing card
2. **Show when trial ends**: "Starting today: 3-day free trial / Starting [date]: $9.99/year"
3. **Show post-trial cost**: "Then $9.99/year" or equivalent
4. **Show cancellation info**: "You won't be charged if you cancel before [date]"

All four items must appear both on the subscription offer card AND in the fine print.

**Alternatives considered**:
- Showing only in fine print: Insufficient per policy; must be on the offer card itself.
- Using Google Play's billing sheet disclosure only: Insufficient; the policy says "in your subscription offer" (i.e., in-app, not just the Play Store purchase dialog).

### 5. Button text and CTA when trial is available

**Decision**: Change "Subscribe Now" to "Start Free Trial" when a free trial is available.

**Rationale**: Google's SKU naming policy explicitly flags "Free Trial" as problematic if combined with auto-recurring charges. However, the button CTA "Start Free Trial" is acceptable when the trial terms are clearly disclosed on the same screen. This matches industry standard patterns (Netflix, Spotify, YouTube Premium all use "Start Free Trial" with clear terms).

**Alternatives considered**:
- "Try Free for X Days": Slightly more descriptive but longer. Could be used as an alternative.
- Keep "Subscribe Now": Doesn't clearly communicate the trial benefit and could be seen as misleading about the initial user experience.

### 6. Period formatting for display

**Decision**: Create a `formatTrialPeriod()` utility function to convert `Period(value, unit)` to human-readable strings.

**Rationale**: The `Period` class from RevenueCat has `value: Int` and `unit: PeriodUnit` (enum: `DAY`, `WEEK`, `MONTH`, `YEAR`, `UNKNOWN`). We need to display "3-day free trial", "1-week free trial", "1-month free trial" etc. A simple formatter handles pluralization and localization.

**Implementation**:
```kotlin
fun Period.toDisplayString(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "1-day" else "$value-day"
    PeriodUnit.WEEK -> if (value == 1) "1-week" else "$value-week"
    PeriodUnit.MONTH -> if (value == 1) "1-month" else "$value-month"
    PeriodUnit.YEAR -> if (value == 1) "1-year" else "$value-year"
    else -> "$value"
}
```

### 7. SDK Version Compatibility

**Decision**: The existing SDK version (v1.9.0+14.3.0) supports all required APIs. No upgrade needed.

**Rationale**: Verified against the SDK documentation at https://revenuecat.github.io/purchases-kmp/:
- `StoreProduct.subscriptionOptions` — Available since early versions
- `SubscriptionOptions.freeTrial` — Available since early versions  
- `StoreProduct.introductoryDiscount` — Available since early versions
- `EntitlementInfo.periodType` / `PeriodType.TRIAL` — Available since early versions
- `PricingPhase`, `Period`, `PeriodUnit` — All available in v1.9.0

**Alternatives considered**:
- Upgrading to v2.7.0: Latest version. Would require migration and testing. Not necessary for this feature and would increase scope/risk.

## Summary of Decisions

| Area | Decision |
|------|----------|
| Trial data extraction | Platform-specific in `AndroidBillingManager`/`IosBillingManager`, unified in `ProductInfo` |
| Current trial detection | Use `EntitlementInfo.periodType == PeriodType.TRIAL` |
| Data model changes | Add `hasFreeTrial`, `freeTrialPeriod`, `freeTrialDuration` to `ProductInfo` |
| UI changes | Modify `PricingCard` and `FinePrint` composables with trial disclosure |
| Button text | "Start Free Trial" when trial available, "Subscribe Now" otherwise |
| SDK version | No upgrade needed, v1.9.0+14.3.0 supports all APIs |
| Period formatting | New `Period.toDisplayString()` extension function |
