# Quickstart: Fix Subscription Free Trial Disclosure

**Branch**: `004-fix-subscription-trial-disclosure`  
**Priority**: CRITICAL — Google Play policy violation

## Problem
Google Play flagged the app for violating the Subscriptions policy: the yearly plan has a 3-day free trial configured in Play Console/RevenueCat, but the app UI doesn't disclose trial terms.

## Solution Overview

5 files modified, 1 file created:

### Layer 1: Data Model
1. **Modify `ProductInfo.kt`** — Add trial fields (`hasFreeTrial`, `freeTrialPeriodDisplay`, etc.)

### Layer 2: Platform Billing (extract trial data)
2. **Modify `AndroidBillingManager.kt`** — Extract trial from `StoreProduct.subscriptionOptions.freeTrial`
3. **Modify `IosBillingManager.kt`** — Extract trial from `StoreProduct.introductoryDiscount`

### Layer 3: Utility
4. **Create `PeriodFormatUtil.kt`** — `Period.toDisplayString()` extension for human-readable trial periods

### Layer 4: UI (display trial info)
5. **Modify `SupportUsScreen.kt`** — Update `PricingCard` and `FinePrint` composables with trial disclosure
6. **Modify `SubscriptionViewModel.kt`** — Pass trial info through to UI

## Key Files

| File | Change |
|------|--------|
| `data/model/ProductInfo.kt` | Add 7 new fields with backward-compatible defaults |
| `data/billing/AndroidBillingManager.kt` | Read `subscriptionOptions.freeTrial` in `getAvailableProducts()` |
| `data/billing/IosBillingManager.kt` | Read `introductoryDiscount` in `getAvailableProducts()` |
| `util/PeriodFormatUtil.kt` | New file — `Period.toDisplayString()` and `Period.toReadableString()` |
| `ui/subscription/SupportUsScreen.kt` | Add trial badge/text to `PricingCard`, trial terms to `FinePrint` |
| `ui/viewmodel/SubscriptionViewModel.kt` | Compute `hasAnyTrialOffer` for FinePrint |

## Build & Test

```bash
# Standard build
./gradlew compileKotlinDesktop

# Run on Android device
./gradlew installDebug

# Run tests
./gradlew test
```

## Verification Checklist

- [ ] Open subscription screen → yearly card shows "3-day free trial"
- [ ] Button says "Start Free Trial" (not "Subscribe Now")
- [ ] Fine print includes trial-specific cancellation language
- [ ] Monthly card still shows "Subscribe Now" (no trial on monthly)
- [ ] Lifetime card unchanged
- [ ] Both light and dark preview render correctly
- [ ] Submit updated build to Google Play for policy re-review
