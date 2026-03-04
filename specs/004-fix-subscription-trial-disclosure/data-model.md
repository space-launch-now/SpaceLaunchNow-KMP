# Data Model: Fix Subscription Free Trial Disclosure

**Date**: 2026-03-03  
**Branch**: `004-fix-subscription-trial-disclosure`

## Entity Changes

### 1. ProductInfo (Modified)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/ProductInfo.kt`

**Current fields**: `productId`, `basePlanId`, `title`, `description`, `formattedPrice`, `priceAmountMicros`, `currencyCode`

**New fields**:

| Field | Type | Description |
|-------|------|-------------|
| `hasFreeTrial` | `Boolean` | Whether this product has a free trial offer |
| `freeTrialPeriodDisplay` | `String?` | Human-readable trial duration (e.g., "3-day") |
| `freeTrialPeriodValue` | `Int?` | Numeric trial duration value (e.g., 3) |
| `freeTrialPeriodUnit` | `String?` | Trial duration unit (e.g., "DAY", "WEEK", "MONTH") |
| `hasIntroOffer` | `Boolean` | Whether this product has an introductory pricing offer |
| `introOfferPrice` | `String?` | Formatted introductory price (e.g., "$0.99") |
| `introOfferPeriodDisplay` | `String?` | Human-readable intro offer period |

**Updated data class**:
```kotlin
data class ProductInfo(
    val productId: String,
    val basePlanId: String?,
    val title: String,
    val description: String,
    val formattedPrice: String,
    val priceAmountMicros: Long,
    val currencyCode: String,
    // Trial/intro offer fields
    val hasFreeTrial: Boolean = false,
    val freeTrialPeriodDisplay: String? = null,
    val freeTrialPeriodValue: Int? = null,
    val freeTrialPeriodUnit: String? = null,
    val hasIntroOffer: Boolean = false,
    val introOfferPrice: String? = null,
    val introOfferPeriodDisplay: String? = null
)
```

**Validation rules**:
- `hasFreeTrial == true` requires non-null `freeTrialPeriodDisplay`
- `hasIntroOffer == true` requires non-null `introOfferPrice`
- `freeTrialPeriodDisplay` format: "{value}-{unit}" (e.g., "3-day", "1-week")

### 2. SubscriptionUiState (Modified)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt`

**New field**:

| Field | Type | Description |
|-------|------|-------------|
| `hasAnyTrialOffer` | `Boolean` | Computed: whether any visible product has a trial |

**Derivation**: Computed from `availableProducts` — `true` if any product has `hasFreeTrial == true`.

### 3. SubscriptionState (Modified — minor)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/SubscriptionState.kt`

**New fields**:

| Field | Type | Description |
|-------|------|-------------|
| `isInTrialPeriod` | `Boolean` | Whether user's current entitlement is a trial |
| `trialExpiresAt` | `Long?` | Trial expiration timestamp (millis since epoch), null if not in trial |

**Source**: Derived from `EntitlementInfo.periodType == PeriodType.TRIAL` and `EntitlementInfo.expirationDateMillis`.

## Entity Relationships

```
┌─────────────────────┐
│     Offerings       │
│  (RevenueCat SDK)   │
└──────┬──────────────┘
       │ contains
       ▼
┌─────────────────────┐
│     Package         │
│  (RevenueCat SDK)   │
│ - storeProduct      │
└──────┬──────────────┘
       │ maps to
       ▼
┌─────────────────────────────────┐
│         ProductInfo             │
│       (app data model)          │
│ - productId                     │
│ - formattedPrice                │
│ - hasFreeTrial ← NEW            │
│ - freeTrialPeriodDisplay ← NEW  │
│ - hasIntroOffer ← NEW           │
│ - introOfferPrice ← NEW         │
└──────┬──────────────────────────┘
       │ displayed by
       ▼
┌─────────────────────┐     ┌──────────────────┐
│   PricingCard       │     │    FinePrint      │
│ - trial badge       │     │ - trial terms     │
│ - trial CTA button  │     │ - cancellation    │
│ - post-trial price  │     │   instructions    │
└─────────────────────┘     └──────────────────┘
```

## Platform-Specific Extraction Logic

### Android (AndroidBillingManager.getAvailableProducts)

```kotlin
// In the StoreProduct → ProductInfo mapping:
val freeTrialOption = pkg.storeProduct.subscriptionOptions?.freeTrial
val freePhase = freeTrialOption?.pricingPhases?.firstOrNull { 
    it.price.amountMicros == 0L 
}

ProductInfo(
    // ... existing fields ...
    hasFreeTrial = freePhase != null,
    freeTrialPeriodDisplay = freePhase?.billingPeriod?.toDisplayString(),
    freeTrialPeriodValue = freePhase?.billingPeriod?.value,
    freeTrialPeriodUnit = freePhase?.billingPeriod?.unit?.name
)
```

### iOS (IosBillingManager.getAvailableProducts)

```kotlin
// In the StoreProduct → ProductInfo mapping:
val introDiscount = pkg.storeProduct.introductoryDiscount
val isFreeTrial = introDiscount?.paymentMode == DiscountPaymentMode.FREE_TRIAL

ProductInfo(
    // ... existing fields ...
    hasFreeTrial = isFreeTrial,
    freeTrialPeriodDisplay = if (isFreeTrial) introDiscount?.subscriptionPeriod?.toDisplayString() else null,
    freeTrialPeriodValue = if (isFreeTrial) introDiscount?.subscriptionPeriod?.value else null,
    freeTrialPeriodUnit = if (isFreeTrial) introDiscount?.subscriptionPeriod?.unit?.name else null,
    hasIntroOffer = introDiscount != null && !isFreeTrial,
    introOfferPrice = if (!isFreeTrial) introDiscount?.price?.formatted else null,
    introOfferPeriodDisplay = if (!isFreeTrial) introDiscount?.subscriptionPeriod?.toDisplayString() else null
)
```

## New Utility

### Period.toDisplayString() Extension

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/PeriodFormatUtil.kt`

```kotlin
import com.revenuecat.purchases.kmp.models.Period
import com.revenuecat.purchases.kmp.models.PeriodUnit

fun Period.toDisplayString(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "1-day" else "$value-day"
    PeriodUnit.WEEK -> if (value == 1) "1-week" else "$value-week"
    PeriodUnit.MONTH -> if (value == 1) "1-month" else "$value-month"
    PeriodUnit.YEAR -> if (value == 1) "1-year" else "$value-year"
    PeriodUnit.UNKNOWN -> "$value"
}

fun Period.toReadableString(): String = when (unit) {
    PeriodUnit.DAY -> if (value == 1) "1 day" else "$value days"
    PeriodUnit.WEEK -> if (value == 1) "1 week" else "$value weeks"
    PeriodUnit.MONTH -> if (value == 1) "1 month" else "$value months"
    PeriodUnit.YEAR -> if (value == 1) "1 year" else "$value years"
    PeriodUnit.UNKNOWN -> "$value"
}
```
