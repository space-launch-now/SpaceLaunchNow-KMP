# Direct Billing Integration for Debug Purchases

## Overview

This implementation adds the ability to purchase **any product ID** from Google Play Console directly through the native Android Billing Library, bypassing RevenueCat. This is extremely useful for:

- Testing legacy IAP products (2020_super_fan, 2021_bronze_supporter, etc.)
- Testing beta/experimental SKUs (beta_supporter, etc.)
- Testing any product without adding it to RevenueCat dashboard
- Debugging billing flows with complete control

## Architecture

### Multiplatform Structure

```
commonMain/
  ├── platform/billing/DirectBillingClient.kt (expect)
  
androidMain/
  ├── data/billing/DirectAndroidBillingClient.kt (core implementation)
  ├── platform/billing/DirectBillingClient.android.kt (actual wrapper)
  
iosMain/
  └── platform/billing/DirectBillingClient.ios.kt (not supported)
  
desktopMain/
  └── platform/billing/DirectBillingClient.desktop.kt (not supported)
```

### Components

#### 1. **DirectAndroidBillingClient** (Core Android Implementation)
**Location**: `composeApp/src/androidMain/kotlin/.../data/billing/DirectAndroidBillingClient.kt`

Direct wrapper around Google Play Billing Library:
- ✅ Connects to Google Play Billing Service
- ✅ Queries product details from Google Play Console
- ✅ Launches purchase flows for any product ID
- ✅ Supports both one-time products ("inapp") and subscriptions ("subs")
- ✅ Handles base plan selection for subscription products
- ✅ Comprehensive error handling and logging

**Key Methods**:
```kotlin
suspend fun initialize(): Result<Unit>
suspend fun launchPurchaseFlow(
    productId: String,
    productType: String = "inapp", // "inapp" or "subs"
    basePlanId: String? = null
): Result<String>
fun disconnect()
```

#### 2. **DirectBillingClient** (Platform-Agnostic API)
**Location**: `composeApp/src/commonMain/kotlin/.../platform/billing/DirectBillingClient.kt`

Expect/actual pattern for multiplatform support:
- ✅ Same API across all platforms
- ✅ Android: Uses Google Play Billing Library
- ✅ iOS/Desktop: Returns "not supported" errors
- ✅ Factory function for platform-specific creation

**API**:
```kotlin
expect class DirectBillingClient() {
    suspend fun initialize(): Result<Unit>
    suspend fun launchPurchaseFlow(...)
    fun disconnect()
}

expect fun createDirectBillingClient(context: Any?): DirectBillingClient
expect fun isDirectBillingSupported(): Boolean
```

#### 3. **Debug Settings UI**
**Location**: `composeApp/src/commonMain/kotlin/.../ui/settings/DebugSettingsScreen.kt`

Enhanced debug screen with:
- ✅ Product ID text input field
- ✅ Product type dropdown (In-App / Subscription)
- ✅ Base plan ID input (for subscriptions)
- ✅ Platform-aware UI (disabled on iOS/Desktop)
- ✅ Real-time purchase flow with error handling
- ✅ Visual feedback and error messages

## Usage

### In Debug Settings Screen

1. **Navigate to Debug Settings** (Settings → Debug Settings)
2. **Scroll to "Custom SKU Purchase" section**
3. **Enter Product Details**:
   - Product ID: e.g., `spacelaunchnow_pro`, `2020_super_fan`, `beta_supporter`
   - Product Type: Select "In-App Product" or "Subscription"
   - Base Plan ID (subscriptions only): e.g., `yearly`, `monthly`
4. **Click "💳 Buy Product (Direct)"**
5. **Complete Google Play purchase flow**

### Supported Products

**Any product in Google Play Console**, including:

| Product ID | Type | Description |
|------------|------|-------------|
| `spacelaunchnow_pro` | inapp | Current one-time premium product |
| `sln_production_yearly` | subs | Current yearly subscription |
| `2020_super_fan` | inapp | Legacy 2020 IAP |
| `2021_bronze_supporter` | inapp | Legacy 2021 IAP |
| `2022_silver_supporter` | inapp | Legacy 2022 IAP |
| `beta_supporter` | inapp | Beta test product |
| Any delisted product | varies | Can test even if not actively listed |

### Platform Support

| Platform | Supported | Notes |
|----------|-----------|-------|
| **Android** | ✅ Yes | Full support via Google Play Billing Library |
| **iOS** | ❌ No | Use RevenueCat offerings instead |
| **Desktop** | ❌ No | No billing functionality |

## Technical Details

### Why Bypass RevenueCat?

**Problem**: RevenueCat can only purchase products configured in:
1. RevenueCat Dashboard Offerings
2. Active/available in App Store / Play Store

**Example**: Testing `beta_supporter` fails with:
```
⚠️ No package found for beta_supporter:null
```

**Solution**: DirectBillingClient queries Google Play Console directly, allowing purchase of **any** product ID that exists there (active, delisted, or beta).

### Product Types

**"inapp"** (One-Time Purchases):
- Legacy IAPs: `2020_super_fan`, `2021_bronze_supporter`, etc.
- Current product: `spacelaunchnow_pro`
- No base plan needed

**"subs"** (Subscriptions):
- Current subscription: `sln_production_yearly`
- Requires base plan selection (e.g., `yearly`, `monthly`)
- Base plan must match configuration in Google Play Console

### Error Handling

The implementation provides detailed error messages:

| Error Message | Cause | Solution |
|---------------|-------|----------|
| "Direct billing only available on Android" | Running on iOS/Desktop | Use Android device/emulator |
| "Activity context not available" | Context factory not initialized | Restart app |
| "Billing init failed" | Google Play connection issue | Check device has Play Store |
| "Product not found" | Invalid product ID | Verify product exists in Play Console |
| "User canceled" | User closed purchase dialog | Expected behavior |

### Integration with RevenueCat

**DirectBillingClient purchases are NOT tracked by RevenueCat** unless:
1. User clicks "Restore Purchases" in settings
2. RevenueCat syncs with Google Play
3. Purchase appears in `nonSubscriptionTransactions`

This is intentional - DirectBillingClient is for **testing only**, not production purchases.

## Implementation Notes

### expect/actual Pattern

Using Kotlin Multiplatform's expect/actual mechanism:

**Common (expect)**:
```kotlin
expect class DirectBillingClient() {
    suspend fun initialize(): Result<Unit>
    // ...
}

expect fun createDirectBillingClient(context: Any?): DirectBillingClient
```

**Android (actual)**:
```kotlin
actual class DirectBillingClient internal constructor(
    private val activity: Activity
) {
    private val client = DirectAndroidBillingClient(activity)
    // Implementation delegates to DirectAndroidBillingClient
}

actual fun createDirectBillingClient(context: Any?): DirectBillingClient {
    require(context is Activity)
    return DirectBillingClient(context)
}
```

**iOS/Desktop (actual)**:
```kotlin
actual class DirectBillingClient {
    actual suspend fun initialize(): Result<Unit> {
        return Result.failure(UnsupportedOperationException("..."))
    }
}

actual fun createDirectBillingClient(context: Any?): DirectBillingClient {
    return DirectBillingClient() // Returns error on all operations
}
```

### Factory Function Pattern

Using `createDirectBillingClient(context)` instead of direct constructor:
- ✅ Type-safe context handling (Activity on Android)
- ✅ Works in commonMain code
- ✅ No platform-specific casts in common code
- ✅ Compile-time safety

### UI Adaptiveness

The debug screen automatically adapts:
- **Android**: Full feature set enabled
- **iOS/Desktop**: UI disabled with explanation message

```kotlin
enabled = customSku.isNotBlank() && !isLoading && !isPurchasing && isDirectBillingSupported()
```

## Testing Guide

### Test Case 1: Legacy IAP Purchase

1. Open Debug Settings
2. Enter Product ID: `2020_super_fan`
3. Select Type: "In-App Product (one-time)"
4. Click "Buy Product (Direct)"
5. **Expected**: Google Play purchase dialog appears
6. Complete purchase
7. Click "Restore Purchases" in Settings
8. **Expected**: Product appears in "Real Owned Products" list

### Test Case 2: Current Subscription

1. Open Debug Settings
2. Enter Product ID: `sln_production_yearly`
3. Select Type: "Subscription"
4. Enter Base Plan: `yearly`
5. Click "Buy Product (Direct)"
6. **Expected**: Google Play subscription dialog with pricing
7. Complete purchase
8. **Expected**: Premium features unlocked immediately

### Test Case 3: Invalid Product

1. Open Debug Settings
2. Enter Product ID: `does_not_exist_123`
3. Select Type: "In-App Product"
4. Click "Buy Product (Direct)"
5. **Expected**: Error message "Product not found" or similar

### Test Case 4: iOS/Desktop Platform

1. Run app on iOS or Desktop
2. Navigate to Debug Settings
3. **Expected**: Custom SKU section shows warning message
4. **Expected**: "Buy Product (Direct)" button is disabled
5. **Expected**: Help text explains platform limitation

## Benefits

✅ **Testing Flexibility**: Purchase any product without RevenueCat configuration
✅ **Legacy Support**: Test restoration of old IAP products
✅ **Beta Testing**: Try experimental SKUs before release
✅ **Debugging**: Full control and visibility into billing flow
✅ **Platform-Safe**: Graceful degradation on non-Android platforms
✅ **Type-Safe**: Compile-time platform abstraction

## Limitations

⚠️ **Android Only**: iOS and Desktop not supported
⚠️ **Not RevenueCat-Tracked**: Purchases don't appear in RevenueCat until restore
⚠️ **Requires Google Play**: Only works with Google Play Billing products
⚠️ **Debug Tool**: Not intended for production purchase flows

## Next Steps

1. ✅ **Wired DirectBillingClient to Debug UI** - COMPLETE
2. 🔄 **Clean up RevenueCat Dashboard** - Remove delisted legacy products
3. 🔄 **Test Legacy Purchase Flow** - Verify restoration works end-to-end
4. 🔄 **Document Testing Procedures** - Add to testing docs

## Related Documentation

- [LEGACY_PURCHASE_RESTORATION.md](../docs/billing/LEGACY_PURCHASE_RESTORATION.md) - How legacy purchases work
- [REVENUECAT_TROUBLESHOOTING.md](../docs/billing/REVENUECAT_TROUBLESHOOTING.md) - Common issues
- [DEBUG_MENU_SECURITY.md](../docs/DEBUG_MENU_SECURITY.md) - Debug features security
