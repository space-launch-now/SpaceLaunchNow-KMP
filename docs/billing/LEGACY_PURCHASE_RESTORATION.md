# Legacy Purchase Restoration Guide

## Problem

Users with older legacy purchases (made before RevenueCat integration) were not getting their proper rewards when restoring purchases. This affected users who purchased products like:
- `2018_founder` - 2018 Founder lifetime purchase
- Other legacy SKUs from 2019, 2020, etc.
- Any old product IDs not currently sold

## Root Cause

When RevenueCat's `restorePurchases()` is called, it:
1. Queries Google Play/App Store for ALL purchases
2. Syncs these purchases to RevenueCat's backend
3. RevenueCat checks if those product IDs are configured in the dashboard
4. Only grants entitlements if the product ID has an entitlement mapping

**The issue**: Legacy product IDs that aren't configured in RevenueCat dashboard don't grant any entitlements, even though the purchase is valid.

## Solution

The app now checks for legacy purchases in two ways:

### 1. Check RevenueCat Entitlements (Standard Flow)
- First checks if user has the `premium` entitlement from RevenueCat
- This works for all current products and any legacy products configured in RevenueCat dashboard

### 2. Check All Active Purchases (Legacy Flow)
- If no entitlement found, queries RevenueCat for **ALL** active product identifiers
- Includes:
  - Active subscriptions
  - Non-subscription transactions (lifetime purchases)
  - Products from active entitlements
- Checks each product ID against `SubscriptionProducts.getFeaturesForProduct()`
- Grants appropriate features based on product ID

## Implementation Details

### RevenueCatManager.kt Changes

```kotlin
/**
 * Get all active product identifiers (including non-subscription purchases)
 * This includes legacy purchases that may not have entitlements configured
 */
fun getActiveProductIdentifiers(): Set<String> {
    val customerInfo = _customerInfo.value ?: return emptySet()
    val productIds = mutableSetOf<String>()
    
    // Add all products from active entitlements
    customerInfo.entitlements.active.values.forEach { entitlementInfo ->
        entitlementInfo.productIdentifier?.let { productIds.add(it) }
    }
    
    // Add all non-subscription purchases (lifetime purchases)
    customerInfo.nonSubscriptionTransactions.forEach { transaction ->
        productIds.add(transaction.productIdentifier)
    }
    
    // Add all active subscription product identifiers
    customerInfo.activeSubscriptions.forEach { productId ->
        productIds.add(productId)
    }
    
    return productIds
}
```

### SubscriptionRepositoryImpl.kt Changes

```kotlin
override suspend fun hasFeature(feature: PremiumFeature): Boolean {
    // Priority 1: Temporary access
    // Priority 2: Debug simulation
    // Priority 3: RevenueCat premium entitlement
    
    // Priority 3.5: Check for legacy purchases without entitlements
    val activeProductIds = revenueCatManager.getActiveProductIdentifiers()
    if (activeProductIds.isNotEmpty()) {
        for (productId in activeProductIds) {
            val productFeatures = SubscriptionProducts.getFeaturesForProduct(productId)
            if (productFeatures.contains(feature)) {
                // Grant access and cache state
                updateCachedStateFromLegacyPurchase(productId, productFeatures)
                return true
            }
        }
    }
    
    // Priority 4: Cached state (offline)
}
```

## Product ID → Subscription Type Mapping

The app categorizes products into three tiers:

### PREMIUM (All Features)
- `spacelaunchnow_pro` - Lifetime purchase
- `sln_production_yearly` - Current subscription
- Products containing "pro", "yearly", "monthly", "base-plan"

### LEGACY (Basic Features)
- `2018_founder` - 2018 Founder purchase
- `2019_supporter`, `2020_premium`, etc.
- **Any unrecognized product ID** (assumes legacy)

### FREE (No Features)
- Debug/test products
- Products starting with "debug_"
- `expired_premium` test product

## Features by Tier

### Premium Features (All)
- ✅ Ad-free browsing
- ✅ Advanced widgets
- ✅ Widget customization
- ✅ Calendar sync
- ✅ Notification customization
- ✅ Custom themes

### Legacy Features (Basic)
- ❌ Ad-free browsing (removed to encourage upgrades)
- ✅ Advanced widgets
- ✅ Widget customization
- ✅ Calendar sync
- ✅ Notification customization
- ❌ Custom themes

## Restore Purchases Flow

1. User taps "Restore Purchases"
2. App calls `revenueCatManager.restorePurchases()`
3. RevenueCat queries Google Play/App Store
4. RevenueCat syncs all purchases to backend
5. App waits 1 second for sync to complete
6. App calls `verifySubscription(forceRefresh = true)`
7. Verification process:
   - Query billing client for purchases
   - If no purchases found, check RevenueCat for legacy purchases
   - Process verified purchases or legacy purchases
   - Update cached state
   - Trigger UI update

## Logging

Enhanced logging helps debug restoration issues:

```
RevenueCat: Starting restore purchases...
RevenueCat: ✅ Purchases restored successfully
  Active entitlements: [premium]
  Active subscriptions: [sln_production_yearly]
  Non-subscription transactions: [2018_founder]
  Total active products found: 2 - [sln_production_yearly, 2018_founder]

SubscriptionRepository: Found 2 active products in RevenueCat: [...]
SubscriptionRepository: Best product: 2018_founder with type LEGACY
SubscriptionRepository: Updating cached state from legacy purchase: 2018_founder -> LEGACY
```

## Testing

### Unit Tests

Run `LegacyPurchaseRestorationTest` to verify:
- Legacy product ID recognition
- Feature grants for different tiers
- Ad-free access logic
- Display name generation

### Manual Testing

1. **Setup**: User with `2018_founder` purchase
2. **Action**: Tap "Restore Purchases" in settings
3. **Expected**: 
   - Logging shows product found
   - User gets LEGACY subscription with basic features
   - Widgets become available
   - Calendar sync enabled
   - No ads shown (if ad-free included in legacy)

### Test Product IDs

Use these for testing different scenarios:
- `2018_founder` → Should grant LEGACY features
- `spacelaunchnow_pro` → Should grant PREMIUM features
- `debug_expired` → Should grant no features

## Configuration Options

### Option 1: Configure in RevenueCat (Recommended for New Users)

For new users or if you want to control entitlements server-side:

1. Go to RevenueCat dashboard → Products
2. Add each legacy product ID (e.g., `2018_founder`)
3. Create entitlement "legacy" or map to "premium"
4. All legacy purchases will grant the configured entitlement

**Pros**: Centralized control, can change entitlements without app update
**Cons**: Requires manual configuration for each legacy SKU

### Option 2: Handle in App (Current Implementation)

The current implementation handles legacy products in the app using `SubscriptionProducts.getFeaturesForProduct()`:

**Pros**: 
- Works immediately without RevenueCat configuration
- Handles unlimited number of legacy SKUs
- No manual work in dashboard

**Cons**: 
- Requires app update to change feature grants
- Harder to A/B test entitlements

## Troubleshooting

### Legacy purchase not detected after restore

Check logs for:
```
RevenueCat: Starting restore purchases...
```

If you see this but no products:
```
RevenueCat: ✅ Purchases restored successfully
  Total active products found: 0
```

**Causes**:
1. Purchase expired/refunded in Google Play
2. Purchase on different account
3. RevenueCat user ID mismatch

### Legacy purchase detected but no features granted

Check logs for:
```
SubscriptionRepository: Found X active products in RevenueCat: [product_id]
SubscriptionRepository: ⚠️ Active products found but none grant FEATURE_NAME
```

**Causes**:
1. Product ID categorized wrong (check `SubscriptionProducts.getSubscriptionType()`)
2. Feature not in `getBasicFeatures()` or `getPremiumFeatures()`
3. Debug/test product (doesn't grant features)

**Fix**: Check product ID categorization logic in `SubscriptionProducts.kt`

### Restore works but features disappear offline

**Cause**: Cached state not updated

**Fix**: Ensure `updateCachedStateFromLegacyPurchase()` is called when legacy purchase detected

## Migration Notes

If migrating users from old app version:

1. ✅ RevenueCat automatically syncs purchases when user restores
2. ✅ App automatically detects legacy purchases without dashboard config
3. ✅ Cached state survives offline/app restart
4. ❌ No automatic migration on first launch (user must restore)

Consider adding auto-restore on first launch:
```kotlin
override suspend fun initialize() {
    // ...existing initialization...
    
    // Auto-restore on first launch to migrate legacy purchases
    if (isFirstLaunch()) {
        restorePurchases()
    }
}
```

## See Also

- [REVENUECAT_COMPLETE_PROGRESS_SUMMARY.md](../docs/billing/REVENUECAT_COMPLETE_PROGRESS_SUMMARY.md) - RevenueCat integration details
- [SubscriptionProducts.kt](../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/BillingClient.kt) - Product ID mappings
- [LegacyPurchaseRestorationTest.kt](../composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/billing/LegacyPurchaseRestorationTest.kt) - Test suite
