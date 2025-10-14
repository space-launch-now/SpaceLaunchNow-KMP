# iOS In-App Purchase Setup Guide

This guide explains how to set up and configure In-App Purchases for the iOS version of Space Launch Now using StoreKit.

## Overview

The iOS implementation uses **StoreKit 1 APIs** (via Kotlin/Native interop) to provide the same functionality as the Android Google Play Billing implementation. While StoreKit 2 is newer, it's Swift-only and not directly accessible from Kotlin/Native, so we use the proven StoreKit 1 APIs which are available in Objective-C.

## Product Mapping: Android vs iOS

### Android Product Structure
Android uses a **single subscription product with multiple base plans**:
- Product ID: `sln_production_yearly`
  - Base Plan: `base-plan` (monthly subscription)
  - Base Plan: `yearly` (yearly subscription)
- One-time purchase: `spacelaunchnow_pro`

### iOS Product Structure
iOS uses **the base plan IDs directly as product IDs**:
- `base_plan` → Monthly subscription (same as Android BASE_PLAN_MONTHLY)
- `yearly` → Yearly subscription (same as Android BASE_PLAN_YEARLY)
- `spacelaunchnow_pro` → Lifetime purchase (same as Android)

### Automatic Mapping
The `IosBillingClient` automatically handles mapping between Android and iOS product IDs:

```kotlin
// When calling from shared code:
billingClient.launchPurchaseFlow(
    productId = "sln_production_yearly",  // Android product ID
    basePlanId = "base-plan"              // Android base plan
)

// iOS client maps to:
// iOS product ID: "base_plan" (uses basePlanId directly)
```

## App Store Connect Configuration

### 1. Create In-App Purchase Products

Log in to [App Store Connect](https://appstoreconnect.apple.com/) and navigate to your app.

#### Monthly Subscription
1. Go to **Features** → **In-App Purchases**
2. Click **+** to add a new product
3. Select **Auto-Renewable Subscription**
4. Configure:
   - **Product ID**: `base_plan`
   - **Reference Name**: Space Launch Now Premium (Monthly)
   - **Subscription Group**: Create new group "Premium"
   - **Subscription Duration**: 1 Month
   - **Price**: Set your monthly price (e.g., $4.99/month)

#### Yearly Subscription
1. Add another Auto-Renewable Subscription to the same subscription group
2. Configure:
   - **Product ID**: `yearly`
   - **Reference Name**: Space Launch Now Premium (Yearly)
   - **Subscription Group**: Premium (same group)
   - **Subscription Duration**: 1 Year
   - **Price**: Set your yearly price (e.g., $49.99/year)

#### Lifetime Purchase
1. Add a **Non-Consumable** product
2. Configure:
   - **Product ID**: `spacelaunchnow_pro`
   - **Reference Name**: Space Launch Now Pro (Lifetime)
   - **Price**: Set your one-time price (e.g., $99.99)

### 2. Configure Subscription Features

For each subscription product:

1. **Localizations**: Add descriptions and names for all supported languages
2. **Subscription Prices**: Configure pricing for different regions
3. **Review Information**: Provide screenshots and notes for App Review
4. **Promotional Offers** (optional): Configure intro offers, free trials, etc.

### 3. Set Up Subscription Group

In the subscription group settings:

1. **Group Name**: Premium
2. **Localization**: Add localized group names
3. **Subscription Ranking**: Yearly should be higher rank than Monthly (to show as "better value")

## Testing In-App Purchases

### 1. Create Sandbox Test Accounts

1. Go to **Users and Access** → **Sandbox** in App Store Connect
2. Click **+** to add a tester
3. Create test accounts with different regions for testing

### 2. Configure Xcode for Testing

In Xcode:

1. Go to **Product** → **Scheme** → **Edit Scheme**
2. Select **Run** → **Options**
3. Under **StoreKit Configuration**, select your configuration file (if using)
4. Alternatively, sign in with a Sandbox account on the device

### 3. Testing Workflow

```swift
// On iOS Simulator or Device:
1. Sign out of App Store (Settings → [Your Name] → Sign Out)
2. Run the app from Xcode
3. Attempt a purchase
4. Sign in with sandbox test account when prompted
5. Complete the test purchase (no actual charge)
```

### 4. Verify Purchases

Check that:
- ✅ Purchase flow launches correctly
- ✅ Payment sheet shows correct pricing
- ✅ Sandbox account is charged (test environment)
- ✅ Purchase completes successfully
- ✅ App grants premium features
- ✅ Subscription auto-renews (accelerated in sandbox)

## StoreKit Configuration File (Optional)

For faster local testing without App Store Connect, create a StoreKit Configuration file:

### 1. Create Configuration File

In Xcode:
1. File → New → File
2. Select **StoreKit Configuration File**
3. Name it `Products.storekit`
4. Add to iOS app target

### 2. Configure Products

Add the same products as App Store Connect:

```json
{
  "identifier" : "base_plan",
  "type" : "auto-renewable-subscription",
  "displayName" : "Premium Monthly",
  "description" : "Monthly subscription",
  "price" : "4.99",
  "familyShareable" : false,
  "group" : "Premium",
  "duration" : "P1M"
},
{
  "identifier" : "yearly",
  "type" : "auto-renewable-subscription",
  "displayName" : "Premium Yearly",
  "description" : "Yearly subscription",
  "price" : "49.99",
  "familyShareable" : false,
  "group" : "Premium",
  "duration" : "P1Y"
},
{
  "identifier" : "spacelaunchnow_pro",
  "type" : "non-consumable",
  "displayName" : "Pro Lifetime",
  "description" : "Lifetime access",
  "price" : "99.99",
  "familyShareable" : false
}
```

### 3. Enable in Scheme

Product → Scheme → Edit Scheme → Run → Options → StoreKit Configuration → Select file

## Implementation Details

### Receipt Validation

The current implementation provides basic receipt reading. For production:

**⚠️ CRITICAL SECURITY**: Always validate receipts server-side!

```kotlin
// Current: Basic client-side check (iOS)
actual suspend fun queryPurchases(): Result<List<PlatformPurchase>> {
    // Reads local receipt
    // TODO: Send to server for validation
}
```

**Recommended flow**:
1. iOS app gets receipt from device
2. Send receipt to your backend server
3. Server validates with Apple's API: `https://buy.itunes.apple.com/verifyReceipt`
4. Server returns validated subscription status
5. App grants/denies premium features based on server response

### Subscription Status Lifecycle

StoreKit transaction states:

| State | Description | Action |
|-------|-------------|--------|
| `SKPaymentTransactionStatePurchasing` | Purchase in progress | Show loading UI |
| `SKPaymentTransactionStatePurchased` | Purchase successful | Grant features, finish transaction |
| `SKPaymentTransactionStateFailed` | Purchase failed | Show error, finish transaction |
| `SKPaymentTransactionStateRestored` | Restored from previous purchase | Grant features, finish transaction |
| `SKPaymentTransactionStateDeferred` | Awaiting approval (Ask to Buy) | Notify user, wait |

### Handling Subscription Changes

Users can:
- **Upgrade**: Switch from monthly to yearly (immediate)
- **Downgrade**: Switch from yearly to monthly (takes effect at end of period)
- **Cancel**: Stop renewal (access until end of current period)

Monitor these via:
```kotlin
billingClient.purchaseUpdates.collect { purchase ->
    // Handle subscription changes
}
```

## Common Issues & Solutions

### Issue: "Cannot connect to iTunes Store"
**Solution**: Check Sandbox account is signed in, or use StoreKit configuration file

### Issue: Products not loading
**Solutions**:
- ✅ Verify product IDs match exactly (case-sensitive)
- ✅ Ensure products are "Ready to Submit" in App Store Connect
- ✅ Check Bundle ID matches app registration
- ✅ Wait 2-24 hours after creating products for them to propagate
- ✅ Check internet connection

### Issue: "This In-App Purchase has already been bought"
**Solution**: Restore purchases or use a different Sandbox account

### Issue: Purchase doesn't grant premium features
**Solutions**:
- ✅ Check transaction observer is registered
- ✅ Verify `finishTransaction()` is called
- ✅ Ensure `queryPurchases()` is called on app launch
- ✅ Check product ID mapping is correct

### Issue: Sandbox purchases not auto-renewing
**Note**: Sandbox subscriptions have **accelerated renewal rates**:
- 1 week subscription → renews every 3 minutes
- 1 month subscription → renews every 5 minutes
- 1 year subscription → renews every 1 hour
- After 6 renewals, subscription expires automatically

## Migration from Previous Versions

If you have users with existing purchases:

1. **Legacy Product Support**: Add old product IDs to mapping
2. **Receipt Migration**: Validate old receipts on server
3. **Grace Period**: Allow time for users to restore purchases
4. **Communication**: Email users about changes

## Production Checklist

Before releasing to production:

- [ ] All products configured in App Store Connect
- [ ] Products approved by App Review
- [ ] Sandbox testing complete for all flows
- [ ] Server-side receipt validation implemented
- [ ] Privacy policy updated with subscription terms
- [ ] App Store listing shows subscription details
- [ ] Subscription management link provided
- [ ] Restore purchases functionality tested
- [ ] Subscription cancellation flow tested
- [ ] Error handling for all edge cases
- [ ] Analytics tracking purchase events

## Useful Links

- [App Store Connect](https://appstoreconnect.apple.com/)
- [StoreKit Documentation](https://developer.apple.com/documentation/storekit)
- [Receipt Validation Guide](https://developer.apple.com/documentation/appstorereceipts/verifying_receipts_with_the_app_store)
- [Subscription Best Practices](https://developer.apple.com/app-store/subscriptions/)
- [Testing In-App Purchases](https://developer.apple.com/documentation/storekit/in-app_purchase/testing_in-app_purchases)

## Support & Troubleshooting

For billing-related questions:
- Check Apple Developer Forums
- Review App Store Review Guidelines
- Contact Apple Developer Support
- Check server logs for receipt validation errors

## Architecture Notes

### Why StoreKit 1 instead of StoreKit 2?

**StoreKit 2** is Swift-only and uses modern async/await patterns:
```swift
// StoreKit 2 (Swift only)
let products = try await Product.products(for: productIds)
```

**StoreKit 1** uses Objective-C APIs accessible from Kotlin/Native:
```kotlin
// StoreKit 1 (Kotlin/Native compatible)
val request = SKProductsRequest(productIds)
request.delegate = this
```

We use StoreKit 1 to avoid needing a Swift bridge layer, keeping the codebase simpler and more maintainable.

### Future Enhancements

Potential improvements:
- [ ] Add StoreKit 2 support via Swift bridge
- [ ] Implement promotional offers
- [ ] Add subscription group management
- [ ] Support for offer codes
- [ ] Family Sharing support
- [ ] Refund handling
- [ ] Upgrade/downgrade flows
