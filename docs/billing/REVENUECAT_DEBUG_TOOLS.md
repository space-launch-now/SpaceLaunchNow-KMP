# RevenueCat Debug Tools

## Overview

Debug tools have been added to the **Debug Settings Screen** to test RevenueCat integration before deploying to production. These tools help verify that the SDK is properly configured and working correctly.

## Location

**Debug Settings Screen**: `Settings` → `Debug Settings` (only visible in debug builds)

## Available Debug Functions

### 1. ✅ Check Initialization Status

**Button**: "✅ Check Initialization Status"

**Purpose**: Verifies that RevenueCat SDK is properly initialized and loaded

**What it checks**:
- SDK initialization status
- Customer info loaded successfully
- Current offering loaded
- Active entitlements (if any)

**Example Output**:
```
✅ RevenueCat Status:
• Initialized: true
• CustomerInfo: ✅ Loaded
• Current Offering: ofrng74226a750e
• Active Entitlements: premium
```

### 2. 📦 Query Products/Offerings

**Button**: "📦 Query Products/Offerings"

**Purpose**: Fetches and displays available products configured in RevenueCat

**What it shows**:
- Offering identifier
- Available packages ($rc_lifetime, $rc_monthly, $rc_annual)
- Product IDs for each platform
- Formatted prices
- Billing periods

**Example Output**:
```
📦 Products/Offerings:
✅ Offering: ofrng74226a750e

Available Packages:
• $rc_lifetime
  Product: spacelaunchnow_pro
  Price: $19.99
  Period: Lifetime

• $rc_monthly
  Product: base_plan (iOS) / sln_production_yearly:base-plan (Android)
  Price: $2.99
  Period: MONTH

• $rc_annual
  Product: yearly (iOS) / sln_production_yearly:yearly (Android)
  Price: $19.99
  Period: YEAR
```

### 3. 🔐 Check Customer Entitlements

**Button**: "🔐 Check Customer Entitlements"

**Purpose**: Shows what features/content the current user has access to

**What it shows**:
- Active entitlements (what user currently has access to)
- All entitlements (including expired ones)
- Product identifiers
- Expiration dates
- Original purchase info

**Example Output**:
```
🔐 Entitlements:
✅ Active Entitlements (1):
  • premium
    Product: spacelaunchnow_pro
    Expires: Never

📋 All Entitlements (1):
  • premium - ✅ Active

💳 Original Purchase Info:
  • Original App User ID: $RCAnonymousID:abc123...
  • First Seen: 2024-01-15T10:30:00Z
```

### 4. 🔄 Test Restore Purchases

**Button**: "🔄 Test Restore Purchases"

**Purpose**: Simulates the "Restore Purchases" flow that users will use

**What it does**:
- Calls RevenueCat's `restorePurchases()` method
- Syncs purchases from App Store / Play Store
- Updates customer info with any found purchases
- Shows active entitlements after restore

**Example Output**:
```
🔄 Restore Purchases:
✅ Restore successful
Active Entitlements: premium
```

### 5. 🎁 View Offering Details

**Button**: "🎁 View Offering Details"

**Purpose**: Shows comprehensive details about the current offering and all packages

**What it shows**:
- Offering ID and description
- Detailed package information:
  - Package identifier
  - Package type (LIFETIME, ANNUAL, MONTHLY, etc.)
  - Product ID
  - Product title
  - Formatted price
  - Billing period
  - Product type
- Quick access to Lifetime, Annual, and Monthly packages

**Example Output**:
```
🎁 Current Offering Details:
✅ Offering ID: ofrng74226a750e
Description: Default offering

📦 Packages (3):

━━━━━━━━━━━━━━━━━━━━
Package: $rc_lifetime
  Type: LIFETIME

  Product Info:
    • ID: spacelaunchnow_pro
    • Title: Space Launch Now Pro (Lifetime)
    • Price: $19.99
    • Period: N/A (N/A)
    • Type: INAPP

━━━━━━━━━━━━━━━━━━━━
Package: $rc_monthly
  Type: MONTHLY

  Product Info:
    • ID: base_plan
    • Title: Space Launch Now Premium
    • Price: $2.99
    • Period: MONTH (1)
    • Type: SUBS

━━━━━━━━━━━━━━━━━━━━
Package: $rc_annual
  Type: ANNUAL

  Product Info:
    • ID: yearly
    • Title: Space Launch Now Premium (Yearly)
    • Price: $19.99
    • Period: YEAR (1)
    • Type: SUBS

⭐ Lifetime Package:
  • spacelaunchnow_pro - $19.99

📅 Annual Package:
  • yearly - $19.99

📆 Monthly Package:
  • base_plan - $2.99
```

## How to Use

### Testing Flow

1. **First Time Setup**:
   ```
   1. Open app in debug build
   2. Navigate to Settings → Debug Settings
   3. Scroll to "RevenueCat Integration Testing" section
   4. Click "✅ Check Initialization Status"
   5. Verify SDK is initialized
   ```

2. **Verify Product Configuration**:
   ```
   1. Click "📦 Query Products/Offerings"
   2. Verify all 3 packages are showing:
      - $rc_lifetime
      - $rc_monthly
      - $rc_annual
   3. Verify prices match expected values
   4. Verify product IDs are correct for platform
   ```

3. **Check Entitlements** (if you've made a test purchase):
   ```
   1. Click "🔐 Check Customer Entitlements"
   2. Verify "premium" entitlement is active
   3. Check expiration date
   ```

4. **Test Restore Flow**:
   ```
   1. Make a test purchase (or use sandbox account with existing purchase)
   2. Uninstall and reinstall app
   3. Go to Debug Settings
   4. Click "🔄 Test Restore Purchases"
   5. Verify purchase is restored
   6. Click "🔐 Check Customer Entitlements" to confirm
   ```

5. **View Full Details**:
   ```
   1. Click "🎁 View Offering Details"
   2. Review all package information
   3. Verify package types and pricing
   ```

## Result Display

### Snackbar
- Short summary message appears at bottom of screen
- Example: "RevenueCat Status Check", "Products Query Result"

### Dialog
- Detailed results open in a scrollable dialog
- Monospaced font for easy reading
- Click "OK" to dismiss

### Dialog Features
- **Scrollable**: Long results can be scrolled
- **Monospaced font**: Easy to read structured data
- **Title**: Shows operation name
- **Dismissible**: Click outside or "OK" button to close

## Prerequisites

⚠️ **IMPORTANT**: RevenueCat must be initialized for these tests to work.

**Required Setup**:
1. RevenueCat SDK installed (already done - v2.2.2+17.10.0)
2. RevenueCatManager in Koin DI (already done)
3. RevenueCat initialized in MainApplication (TODO - see REVENUECAT_QUICK_START.md)

**If tests fail with "RevenueCatManager not available"**:
- RevenueCat not initialized yet
- Follow Step 2 in REVENUECAT_QUICK_START.md to initialize SDK

## Tips

### 💡 Best Practices
- **Run "Check Initialization" first** - Verifies SDK is ready
- **Query products early** - Ensures offerings are loaded
- **Test on real devices** - Sandbox testing requires real devices
- **Use sandbox accounts** - Don't use real money for testing

### ⚠️ Common Issues

**"RevenueCatManager not available"**
- **Cause**: SDK not initialized
- **Fix**: Initialize RevenueCat in MainApplication (see REVENUECAT_QUICK_START.md Step 2)

**"No offering available"**
- **Cause**: Network issue or offerings not configured
- **Fix**: 
  1. Check internet connection
  2. Verify offering `ofrng74226a750e` exists in RevenueCat dashboard
  3. Verify products are attached to offering

**"No customer info available"**
- **Cause**: SDK not initialized or network issue
- **Fix**: Run "Check Initialization" first, then retry

**Prices showing as $0.00**
- **Cause**: Products not configured in App Store Connect / Play Console
- **Fix**: Configure IAP products in store consoles (see SUBSCRIPTION_GOOGLE_PLAY_SETUP.md)

## Testing Checklist

Use this checklist during development:

**Pre-Migration (Current Phase)**:
- [ ] Verify all 5 debug buttons work
- [ ] Check initialization shows SDK is ready
- [ ] Query products returns 3 packages
- [ ] Products show correct prices
- [ ] Products show correct IDs for platform
- [ ] Entitlements check works (shows free user initially)
- [ ] Restore purchases works without errors
- [ ] Offering details shows complete information
- [ ] Dialog displays results properly
- [ ] Can scroll long results in dialog

**After Phase 2 (DI Updates)**:
- [ ] Debug tools still work after DI changes
- [ ] RevenueCatManager properly injected
- [ ] All debug functions return correct data

**After Phase 3 (UI Updates)**:
- [ ] Can still access debug screen
- [ ] Debug tools work with new SupportUsScreen
- [ ] Test purchase flow, then verify with debug tools

**After Phase 4 (Feature Gating)**:
- [ ] Entitlements check shows "premium" after purchase
- [ ] Verify feature gating with debug tools

**Before Production Deployment**:
- [ ] Test all debug functions one final time
- [ ] Verify offerings match production configuration
- [ ] Test restore flow on fresh install
- [ ] Verify entitlements after sandbox purchase

## Files Modified

### DebugSettingsViewModel.kt
**Location**: `composeApp/src/commonMain/kotlin/.../ui/viewmodel/DebugSettingsViewModel.kt`

**Changes**:
- Added `revenueCatManager: RevenueCatManager?` parameter
- Added `detailedMessage` StateFlow for dialog content
- Added 5 RevenueCat debug functions:
  - `checkRevenueCatInitialization()`
  - `queryRevenueCatProducts()`
  - `checkRevenueCatEntitlements()`
  - `testRevenueCatRestore()`
  - `viewRevenueCatOfferingDetails()`
- Added `@OptIn(kotlin.time.ExperimentalTime::class)` for date handling

### DebugSettingsScreen.kt
**Location**: `composeApp/src/commonMain/kotlin/.../ui/settings/DebugSettingsScreen.kt`

**Changes**:
- Added `detailedMessage` state collection
- Added `showDetailedDialog` state and AlertDialog
- Added "RevenueCat Integration Testing" section with 5 buttons
- Added dialog to display detailed results

### AppModule.kt
**Location**: `composeApp/src/commonMain/kotlin/.../di/AppModule.kt`

**Changes**:
- Updated `DebugSettingsViewModel` to inject `RevenueCatManager`

## Next Steps

1. **Initialize RevenueCat**: Follow REVENUECAT_QUICK_START.md Step 2
2. **Test Debug Tools**: Use this document to verify integration
3. **Continue Migration**: Proceed with Phase 2 (DI updates) after testing

## Related Documentation

- [REVENUECAT_QUICK_START.md](./REVENUECAT_QUICK_START.md) - Setup instructions
- [REVENUECAT_TRANSITION_PLAN.md](./REVENUECAT_TRANSITION_PLAN.md) - Complete migration plan
- [REVENUECAT_FEATURE_GATING.md](./REVENUECAT_FEATURE_GATING.md) - Feature gating patterns
- [REVENUECAT_INDEX.md](./REVENUECAT_INDEX.md) - Documentation overview
