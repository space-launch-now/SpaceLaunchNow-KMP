# Internal Contracts: Free Trial Disclosure

**Date**: 2026-03-03  
**Note**: This feature has no external API contracts (REST/GraphQL). All changes are internal to the app.
These contracts define the internal interfaces between layers.

## BillingManager Interface (Unchanged)

The `BillingManager` interface does NOT change. The trial information is carried through the
existing `ProductInfo` return type from `getAvailableProducts()`.

```kotlin
interface BillingManager {
    // ... existing methods unchanged ...
    suspend fun getAvailableProducts(): Result<List<ProductInfo>> // ProductInfo now includes trial fields
}
```

## ProductInfo Contract

The `ProductInfo` data class is the contract between the billing layer and the UI layer.

### New fields added to ProductInfo:

```kotlin
// New fields — all have defaults for backward compatibility
data class ProductInfo(
    // ... existing fields ...
    val hasFreeTrial: Boolean = false,
    val freeTrialPeriodDisplay: String? = null,      // e.g., "3-day"
    val freeTrialPeriodValue: Int? = null,            // e.g., 3
    val freeTrialPeriodUnit: String? = null,          // e.g., "DAY"
    val hasIntroOffer: Boolean = false,
    val introOfferPrice: String? = null,              // e.g., "$0.99"
    val introOfferPeriodDisplay: String? = null       // e.g., "1-month"
)
```

### Invariants:
- `hasFreeTrial == true` → `freeTrialPeriodDisplay != null`
- `hasIntroOffer == true` → `introOfferPrice != null`
- `hasFreeTrial` and `hasIntroOffer` are mutually exclusive (a product has one or the other)

## PricingCard Component Contract

The `PricingCard` composable gains new optional parameters:

```kotlin
@Composable
private fun PricingCard(
    title: String,
    price: String,
    period: String,
    savings: String? = null,
    isRecommended: Boolean = false,
    isProcessing: Boolean = false,
    onSubscribe: () -> Unit,
    enabled: Boolean = true,
    // New parameters for trial disclosure
    hasFreeTrial: Boolean = false,
    freeTrialPeriodDisplay: String? = null,    // e.g., "3-day"
    subscribeButtonText: String = "Subscribe Now"  // overridden to "Start Free Trial" when trial
)
```

### UI Rendering Rules:

| Condition | Trial Badge | Button Text | Price Display | Additional Text |
|-----------|-------------|-------------|---------------|-----------------|
| `hasFreeTrial == false` | Hidden | "Subscribe Now" | `$price/period` | None |
| `hasFreeTrial == true` | "{X}-day free trial" | "Start Free Trial" | `$price/period` | "then $price/period after trial" |

## FinePrint Component Contract

The `FinePrint` composable gains a parameter:

```kotlin
@Composable
private fun FinePrint(
    hasTrialOffer: Boolean = false,         // NEW: whether any visible product has a trial
    trialPeriodDisplay: String? = null,     // NEW: e.g., "3-day" 
    postTrialPrice: String? = null,         // NEW: e.g., "$9.99/year"
)
```

### Trial Fine Print Text:
When `hasTrialOffer == true`:
> "Free trial will automatically convert to a paid subscription at {postTrialPrice} unless canceled before the trial ends. You won't be charged if you cancel during the trial period."

This is appended to/replaces into the existing fine print text.
