# Google Play Subscription Setup

## ✅ Configuration Complete

Your Google Play Console subscription product has been successfully integrated into the app.

## Product Configuration

### Google Play Console Setup

**Product ID**: `sln_production_yearly`

**Base Plans**:
- `base-plan` - Monthly subscription ($4.99/month)
- `yearly` - Yearly subscription ($24.99/year, Save 58%!)

**Benefits**:
- Premium Widgets
- Remove Ads  
- Premium Themes

### Code Configuration

The following constants are now configured in `BillingClient.kt`:

```kotlin
object SubscriptionProducts {
    const val PRODUCT_ID = "sln_production_yearly"
    const val BASE_PLAN_MONTHLY = "base-plan"
    const val BASE_PLAN_YEARLY = "yearly"
}
```

## How It Works

### Purchase Flow

1. **User clicks "Subscribe" button** (either Monthly or Yearly)
2. **App calls**: 
   ```kotlin
   viewModel.purchaseSubscription(
       productId = SubscriptionProducts.PRODUCT_ID,
       basePlanId = SubscriptionProducts.BASE_PLAN_YEARLY // or BASE_PLAN_MONTHLY
   )
   ```
3. **Android billing client**:
   - Queries product details from Google Play
   - Finds the matching base plan (`base-plan` or `yearly`)
   - Launches Google Play purchase flow
4. **Google Play** handles payment
5. **Purchase result** returns to app via `purchasesUpdatedListener`
6. **Repository** verifies and updates subscription state

### Base Plan Selection

The `basePlanId` parameter determines which subscription tier the user purchases:

- **Monthly**: `basePlanId = "base-plan"` → $4.99/month
- **Yearly**: `basePlanId = "yearly"` → $24.99/year

### Code Flow

```
UI Screen (SupportUsScreen / SubscriptionScreen)
    ↓
SubscriptionViewModel.purchaseSubscription(productId, basePlanId)
    ↓
SubscriptionRepository.launchPurchaseFlow(productId, basePlanId)
    ↓
AndroidBillingClient.launchPurchaseFlow(productId, basePlanId)
    ↓
Google Play Billing Library
    ↓
Google Play Console
```

## Testing

### Test with License Testers

1. **Add test accounts** in Google Play Console → Setup → License testing
2. **Install app** from internal testing track (not from APK directly)
3. **Test purchase flow**:
   - Tap "Subscribe Yearly" → Should show $24.99/year
   - Tap "Subscribe Monthly" → Should show $4.99/month
4. **Verify purchase** completes successfully
5. **Check subscription state** in app (should show as subscribed)

### Debug Logging

Enable verbose logging to see purchase flow:

```
AndroidBillingClient: Launching purchase flow for sln_production_yearly with basePlan: yearly
AndroidBillingClient: Using base plan: yearly, offer token: <token>
AndroidBillingClient: Purchase flow launched successfully
SubscriptionRepository: Purchase successful - <purchaseToken>
```

## Next Steps

### 1. Configure Testing

✅ **Already done in Play Console**:
- Product created: `sln_production_yearly`
- Base plans configured: `base-plan`, `yearly`
- Benefits listed: Premium Widgets, Remove Ads, Premium Themes

⏳ **TODO**:
- Add license testers to Google Play Console
- Create internal testing track and upload signed APK
- Test purchase flow with real Google account

### 2. Implement Feature Gating

Gate features behind subscription checks:

```kotlin
// In your widget code
PremiumFeatureGate(
    feature = PremiumFeature.ADVANCED_WIDGETS,
    subscriptionViewModel = subscriptionViewModel
) {
    // Show premium widget UI
    PremiumWidgetContent()
}

// In your ad code
val hasAdFree by rememberHasFeature(
    feature = PremiumFeature.AD_FREE,
    subscriptionViewModel = subscriptionViewModel
)

if (!hasAdFree) {
    // Show ads
    AdBanner()
}
```

### 3. Set Activity for Purchase Flows

In your MainActivity:

```kotlin
class MainActivity : ComponentActivity() {
    
    private val billingClient: BillingClient by inject()
    
    override fun onResume() {
        super.onResume()
        // Set activity for purchase flows
        if (billingClient is AndroidBillingClient) {
            (billingClient as AndroidBillingClient).setActivity(this)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Clear activity reference
        if (billingClient is AndroidBillingClient) {
            (billingClient as AndroidBillingClient).setActivity(null)
        }
    }
}
```

### 4. Production Release Checklist

Before releasing to production:

- [ ] Test all purchase flows (monthly, yearly)
- [ ] Test restore purchases functionality
- [ ] Test subscription expiry handling
- [ ] Verify feature gating works correctly
- [ ] Test with different subscription states (active, expired, cancelled)
- [ ] Add server-side receipt validation (recommended for production)
- [ ] Set up Play Console app signing
- [ ] Configure subscription notifications in Play Console

## Troubleshooting

### "Product not found" Error

**Cause**: App can't find the product in Google Play

**Solutions**:
1. Verify product ID matches exactly: `sln_production_yearly`
2. Ensure app is installed from Play Console (not from APK)
3. Check account is added as license tester
4. Wait 2-4 hours after creating product (propagation delay)

### "No offer token found" Error

**Cause**: Base plan doesn't exist or isn't active

**Solutions**:
1. Verify base plan IDs: `base-plan`, `yearly`
2. Check base plans are ACTIVE in Play Console
3. Ensure pricing is configured for all countries

### Purchase Flow Doesn't Launch

**Cause**: Activity not set or billing client not initialized

**Solutions**:
1. Ensure `billingClient.setActivity(activity)` is called
2. Verify `billingClient.initialize()` succeeded
3. Check Google Play Services is installed and updated

## Reference

- [SUBSCRIPTION_SYSTEM.md](./SUBSCRIPTION_SYSTEM.md) - Complete system documentation
- [SUBSCRIPTION_QUICKSTART.md](./SUBSCRIPTION_QUICKSTART.md) - Quick reference guide
- [Google Play Billing Docs](https://developer.android.com/google/play/billing)
