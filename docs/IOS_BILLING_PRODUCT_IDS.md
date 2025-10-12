# iOS Billing - Product ID Quick Reference

## Product IDs Used in App Store Connect

Configure these exact product IDs in App Store Connect:

| Product ID | Type | Duration | Description |
|-----------|------|----------|-------------|
| `base_plan` | Auto-Renewable Subscription | 1 Month | Monthly subscription |
| `yearly` | Auto-Renewable Subscription | 1 Year | Yearly subscription |
| `spacelaunchnow_pro` | Non-Consumable | N/A | Lifetime purchase |

## How Product IDs Map Between Platforms

### Shared Code Usage (Platform Agnostic)
```kotlin
// Purchase monthly subscription
billingClient.launchPurchaseFlow(
    productId = "sln_production_yearly",  // Android product ID
    basePlanId = "base-plan"              // Tells which plan
)

// Purchase yearly subscription  
billingClient.launchPurchaseFlow(
    productId = "sln_production_yearly",  // Android product ID
    basePlanId = "yearly"                 // Tells which plan
)

// Purchase lifetime
billingClient.launchPurchaseFlow(
    productId = "spacelaunchnow_pro",     // Same on both platforms
    basePlanId = null
)
```

### What Happens on Each Platform

#### Android (Google Play)
```
Shared Code              Google Play Billing
──────────────────────   ────────────────────
productId: "sln_production_yearly"
basePlanId: "base-plan"  → Product: "sln_production_yearly"
                           Base Plan: "base-plan"
                           
productId: "sln_production_yearly"  
basePlanId: "yearly"     → Product: "sln_production_yearly"
                           Base Plan: "yearly"

productId: "spacelaunchnow_pro"
basePlanId: null         → Product: "spacelaunchnow_pro"
                           Type: INAPP (one-time)
```

#### iOS (App Store)
```
Shared Code              StoreKit
──────────────────────   ────────────────────
productId: "sln_production_yearly"
basePlanId: "base-plan"  → Product ID: "base_plan"
                           (uses basePlanId directly)

productId: "sln_production_yearly"
basePlanId: "yearly"     → Product ID: "yearly"
                           (uses basePlanId directly)

productId: "spacelaunchnow_pro"
basePlanId: null         → Product ID: "spacelaunchnow_pro"
                           (same as Android)
```

## Why This Mapping?

### Android Model
Google Play uses **one product with multiple base plans**:
- Product: `sln_production_yearly`
  - Base Plan 1: `base-plan` (monthly billing)
  - Base Plan 2: `yearly` (yearly billing)

### iOS Model  
App Store requires **separate product per subscription tier**:
- Product 1: `base_plan` (monthly)
- Product 2: `yearly` (yearly)

### Solution
The iOS BillingClient takes the `basePlanId` parameter and uses it as the iOS product ID:

```kotlin
// In IosBillingClient.kt
private fun mapAndroidToIosProductId(productId: String, basePlanId: String?): String {
    return when {
        // For subscriptions, use base plan ID as iOS product ID
        productId == SubscriptionProducts.PRODUCT_ID && basePlanId != null -> basePlanId
        
        // For one-time purchases, keep the same ID
        productId == SubscriptionProducts.PRO_LIFETIME -> SubscriptionProducts.PRO_LIFETIME
        
        else -> productId
    }
}
```

## Testing Quick Reference

### iOS Sandbox Test Products
When setting up StoreKit Configuration file for testing:

```json
{
  "products": [
    {
      "identifier": "base_plan",
      "type": "auto-renewable-subscription",
      "displayName": "Monthly Premium",
      "price": "4.99",
      "subscriptionGroupId": "premium",
      "subscriptionDuration": "P1M"
    },
    {
      "identifier": "yearly",
      "type": "auto-renewable-subscription", 
      "displayName": "Yearly Premium",
      "price": "49.99",
      "subscriptionGroupId": "premium",
      "subscriptionDuration": "P1Y"
    },
    {
      "identifier": "spacelaunchnow_pro",
      "type": "non-consumable",
      "displayName": "Lifetime Pro",
      "price": "99.99"
    }
  ]
}
```

## Constants Reference

From `BillingClient.kt`:

```kotlin
object SubscriptionProducts {
    // Android subscription product ID
    const val PRODUCT_ID = "sln_production_yearly"
    
    // One-time purchase (same on both platforms)
    const val PRO_LIFETIME = "spacelaunchnow_pro"
    
    // Base plan IDs (used as iOS product IDs)
    const val BASE_PLAN_MONTHLY = "base-plan"  // iOS: "base_plan" product
    const val BASE_PLAN_YEARLY = "yearly"      // iOS: "yearly" product
}
```

## Troubleshooting

### "Product not found" error on iOS
✅ **Check**: Product IDs in App Store Connect match exactly:
- `base_plan` (NOT `base-plan` with hyphen)
- `yearly`
- `spacelaunchnow_pro`

### Products not loading on iOS
✅ **Wait**: New products can take 2-24 hours to propagate
✅ **Status**: Ensure products are "Ready to Submit" in App Store Connect
✅ **Bundle ID**: Verify Bundle ID matches app registration

### Wrong subscription tier after purchase
✅ **Mapping**: Check `mapAndroidToIosProductId()` is using basePlanId correctly
✅ **Receipt**: Verify transaction.payment.productIdentifier matches expected value

## See Also

- [IOS_BILLING_SETUP.md](./IOS_BILLING_SETUP.md) - Complete setup guide
- [BILLING_USAGE_EXAMPLES.md](./BILLING_USAGE_EXAMPLES.md) - Code examples
- [BillingClient.kt](../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/BillingClient.kt) - Interface definition
