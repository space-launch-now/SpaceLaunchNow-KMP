# RevenueCat Troubleshooting Guide

## ❌ "No Offerings Available" Error

### Problem
When testing RevenueCat debug tools, you see:
```
📦 Products/Offerings:
❌ No offering available
```

### Common Causes & Solutions

---

## 1. ⚠️ RevenueCat Not Initialized (MOST COMMON)

**Symptoms:**
- "No offerings available"
- "RevenueCatManager not available" 
- "Initialized: false" in debug tools

**Check:**
```kotlin
// Does MainApplication.kt have this?
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // ... Koin initialization ...
        
        // ❌ Missing: RevenueCat initialization
    }
}
```

**Fix:** Initialize RevenueCat in `MainApplication.kt`:

```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import org.koin.android.ext.android.inject

class MainApplication : Application() {
    
    private val revenueCatManager: RevenueCatManager by inject()
    
    override fun onCreate() {
        super.onCreate()
        
        // ... existing Koin initialization ...
        
        // Initialize RevenueCat
        Log.d("MainApplication", "Initializing RevenueCat...")
        GlobalScope.launch {
            try {
                revenueCatManager.initialize()
                Log.d("MainApplication", "✅ RevenueCat initialized successfully")
            } catch (e: Exception) {
                Log.e("MainApplication", "❌ Failed to initialize RevenueCat", e)
            }
        }
    }
}
```

---

## 2. 🚨 **CONFIGURATION ERROR:** Product IDs Don't Match Play Store

**Error Message:**
```
😿‼️ Error fetching offerings - PurchasesError(code=ConfigurationError, 
underlyingErrorMessage=There's a problem with your configuration. 
None of the products registered in the RevenueCat dashboard could be 
fetched from the Play Store.
```

**Symptoms:**
- ✅ SDK initialized
- ✅ Customer info loads
- ❌ **Configuration error when fetching offerings**
- ❌ "None of the products... could be fetched from the Play Store"

**What This Means:**
The product IDs you configured in RevenueCat dashboard **don't exist** or **don't match** the product IDs in Google Play Console!

**Quick Diagnosis:**

1. **List RevenueCat Product IDs:**
   - Go to https://app.revenuecat.com/ → **Products**
   - Write down all product IDs (e.g., `spacelaunchnow_pro`, `base_plan`)

2. **List Play Console Product IDs:**
   - Go to https://play.google.com/console/
   - Check **Monetize → Subscriptions** and **In-app products**
   - Write down all product IDs

3. **Compare:**
   ```
   RevenueCat          Play Console            Match?
   ───────────────────────────────────────────────────
   spacelaunchnow_pro  spacelaunchnow_pro      ✅ YES
   base_plan           sln_monthly:base-plan   ❌ NO!
   yearly              sln_annual:base-plan    ❌ NO!
   ```

**Common Mismatches:**

❌ **Wrong format for subscriptions:**
```
RevenueCat:      base_plan
Play Console:    sln_monthly:base-plan  (subscription:basePlan format)
Should be:       sln_monthly:base-plan  (must include both parts!)
```

❌ **Missing bundle ID prefix:**
```
RevenueCat:      monthly
Play Console:    me.calebjones.spacelaunchnow.monthly
Should be:       me.calebjones.spacelaunchnow.monthly
```

❌ **Typo or case mismatch:**
```
RevenueCat:      spacelauchnow_pro  (missing 'n')
Play Console:    spacelaunchnow_pro
Should be:       spacelaunchnow_pro  (must match exactly!)
```

**Quick Fix:**

**Option A: Update RevenueCat (Recommended)**
1. Go to RevenueCat → **Products**
2. Edit each product
3. Change **Product Identifier** to match Play Console **exactly**
4. For subscriptions, use format: `subscriptionId:basePlanId`
5. Save and verify offerings still link to products

**Option B: Create Products in Play Console**
1. Go to Play Console → Create products matching RevenueCat IDs
2. Activate them
3. Wait 24-48 hours for Play Store cache

**Detailed Guide:** [REVENUECAT_PRODUCT_MISMATCH.md](./REVENUECAT_PRODUCT_MISMATCH.md)

This guide covers:
- ✅ How to find product IDs in both systems
- ✅ Correct format for subscriptions with base plans
- ✅ How to create products in Play Console
- ✅ Using test products for development
- ✅ Play Console vs App Store differences
- ✅ Verbose logging to see which products fail

---

## 3. ⚠️ API Key Not Configured

**Symptoms:**
- SDK initializes but offerings fail to load
- Network errors in logs
- "No offerings available"

**Check Android:**
```kotlin
// composeApp/src/androidMain/kotlin/.../data/config/RevenueCatConfig.android.kt
actual object RevenueCatConfig {
    actual val apiKey: String = BuildConfig.REVENUECAT_ANDROID_KEY // ✅ Should use BuildConfig
}
```

**Check iOS:**
```kotlin
// composeApp/src/iosMain/kotlin/.../data/config/RevenueCatConfig.ios.kt
actual object RevenueCatConfig {
    actual val apiKey: String get() = AppSecrets.revenueCatIosKey // ✅ Should use AppSecrets
}
```

**Fix Android:** 

1. Add keys to `.env` file in project root:
```properties
REVENUECAT_ANDROID_KEY=goog_your_android_key_here
REVENUECAT_IOS_KEY=appl_your_ios_key_here
```

2. Rebuild to generate BuildConfig:
```bash
./gradlew clean
./gradlew :composeApp:generateDebugBuildConfig
./gradlew :composeApp:assembleDebug
```

3. Verify BuildConfig was generated:
```bash
# Check the generated file contains your key
cat composeApp/build/generated/source/buildConfig/debug/me/calebjones/spacelaunchnow/BuildConfig.java
```

**Fix iOS:**

1. Ensure keys are in `.env` file (see Android step 1)

2. Run the generation script:
```bash
./scripts/generate-ios-secrets.sh
```

3. Add `Secrets.plist` to Xcode project:
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - Right-click on `iosApp` folder
   - Select "Add Files to 'iosApp'..."
   - Navigate to `iosApp/iosApp/Secrets.plist`
   - ✅ Check "Copy items if needed"
   - ✅ Check "Add to targets: iosApp"

**Where to Get Keys:**
1. Go to https://app.revenuecat.com/
2. Select your project
3. Go to **API keys** (left sidebar)
4. Copy:
   - **Google Play API key** → `REVENUECAT_ANDROID_KEY` (starts with `goog_`)
   - **App Store API key** → `REVENUECAT_IOS_KEY` (starts with `appl_`)

**Detailed Setup Guides:**
- [ANDROID_REVENUECAT_KEY_SETUP.md](./ANDROID_REVENUECAT_KEY_SETUP.md) - Android key configuration
- [IOS_API_KEY_SETUP.md](./IOS_API_KEY_SETUP.md) - iOS key configuration

---

## 3. ⚠️ Offering Not Created in Dashboard

**Symptoms:**
- SDK initializes successfully
- API key is valid
- Still "No offerings available"

**Check:** Verify offering exists in RevenueCat dashboard:

1. Go to https://app.revenuecat.com/
2. Navigate to **Offerings** (left sidebar)
3. Check if offering `ofrng74226a750e` exists
4. Verify it has 3 packages attached:
   - `$rc_lifetime`
   - `$rc_monthly`
   - `$rc_annual`

**Fix:** Create the offering if missing:

1. In RevenueCat dashboard → **Offerings**
2. Click **"+ New"** to create offering
3. Set identifier: `ofrng74226a750e`
4. Add packages:
   - Package ID: `$rc_lifetime`, Product: `spacelaunchnow_pro`
   - Package ID: `$rc_monthly`, Product: `base_plan` (iOS) / `sln_production_yearly:base-plan` (Android)
   - Package ID: `$rc_annual`, Product: `yearly` (iOS) / `sln_production_yearly:yearly` (Android)
5. Set as **Current Offering**

---

## 4. ⚠️ Products Not Configured in Stores

**Symptoms:**
- Offering loads but products have $0.00 prices
- Products show but can't be purchased
- Store errors in logs

**Check:** Products must exist in App Store Connect / Play Console

**Android - Google Play Console:**
1. Go to Google Play Console
2. Your App → **Monetize** → **In-app products**
3. Verify these products exist:
   - `spacelaunchnow_pro` (one-time purchase)
   - `sln_production_yearly` (subscription with 2 base plans)
4. Products must be **Active** and **Published**

**iOS - App Store Connect:**
1. Go to App Store Connect
2. Your App → **In-App Purchases**
3. Verify these products exist:
   - `spacelaunchnow_pro` (non-consumable)
   - `base_plan` (auto-renewable subscription - monthly)
   - `yearly` (auto-renewable subscription - yearly)
4. Products must be **Ready to Submit** or **Approved**

**Fix:** Create missing products following:
- [SUBSCRIPTION_GOOGLE_PLAY_SETUP.md](./SUBSCRIPTION_GOOGLE_PLAY_SETUP.md) for Android

---

## 5. 🚨 **MOST COMMON:** No "Current" Offering Set

**Symptoms:**
- ✅ SDK initialized
- ✅ Customer info loads (shows customer ID)
- ❌ **"No offerings available" but offerings exist in dashboard**

**This is the #1 cause of "no offerings" issues!**

**Diagnosis:** Check your logcat after clicking "Fetch Offerings":

```
RevenueCat: ✅ Offerings API response received
  - Total offerings available: 1           ← Offerings exist
  - All offering IDs: default
  - Current offering ID: ❌ NONE           ← THE PROBLEM!
  - ⚠️ WARNING: No 'current' offering is set!
```

**What Happened:**
- RevenueCat API returned offerings successfully
- But no offering is marked as "Current"
- SDK only exposes the "current" offering to apps
- Result: `currentOffering.value` is `null`

**Quick Fix:**
1. Go to https://app.revenuecat.com/
2. Navigate to **Offerings** (left sidebar)
3. You should see your offering listed (e.g., `default` or `ofrng74226a750e`)
4. Click the **⋮** (three dots) menu next to the offering
5. Select **"Set as Current"**
6. Look for the **"CURRENT"** badge to appear
7. Force close your app completely
8. Relaunch app and test "Fetch Offerings" again

**Expected log after fix:**
```
RevenueCat: ✅ Offerings API response received
  - Total offerings available: 1
  - All offering IDs: default
  - Current offering ID: default           ← Fixed!
  - Available packages: 3                  ← Now shows packages
    • Package: $rc_monthly
      Product: me.calebjones.spacelaunchnow.monthly
      Price: $4.99
```

**Why This Happens:**
- New offerings are created as "draft" by default
- Must be explicitly set as "Current" to be visible to apps
- Can only have ONE current offering at a time
- Allows A/B testing different offerings without code changes

**Still Having Issues?**

See comprehensive guide: **[REVENUECAT_OFFERINGS_ISSUE.md](./REVENUECAT_OFFERINGS_ISSUE.md)**

This guide covers:
- ✅ How to create products in RevenueCat
- ✅ How to create offerings with packages
- ✅ How to verify product IDs match stores
- ✅ Sandbox testing setup
- ✅ API key permissions check
- ✅ Enhanced verbose logging
- ✅ 5 other common configuration issues

---

## 6. ⚠️ Network Issues

**Symptoms:**
- Works on some devices but not others
- Works on WiFi but not cellular
- Intermittent failures

**Check:**
- Internet connectivity
- Firewall/proxy settings
- Corporate network restrictions

**Debug:**
```kotlin
// In RevenueCatManager.kt - check logs
override suspend fun initialize() {
    try {
        println("RevenueCat: Network status: ${isNetworkAvailable()}")
        // ... initialization code ...
    } catch (e: Exception) {
        println("RevenueCat: Network error - ${e.message}")
    }
}
```

**Fix:**
- Test on different networks
- Add retry logic
- Show user-friendly error messages

---

## 6. ⚠️ Desktop Platform (Expected Behavior)

**Symptoms:**
- "No offerings available" on Desktop/JVM
- "platform not supported" message

**This is NORMAL!** Desktop uses a stub implementation:

```kotlin
// composeApp/src/desktopMain/.../RevenueCatConfig.desktop.kt
actual object RevenueCatConfig {
    actual val apiKey: String = "desktop_not_supported" // ✅ Expected
}
```

RevenueCat only supports Android and iOS. Desktop builds use a no-op implementation.

---

## 🔍 Debug Checklist

Use this checklist to diagnose the issue:

### Step 1: Check Initialization
```
1. Open app in debug build
2. Go to Settings → Debug Settings → RevenueCat Testing
3. Click "✅ Check Initialization Status"
4. Expected: "Initialized: true"
   ❌ If false: RevenueCat not initialized in MainApplication
```

### Step 2: Check API Key
```
1. Open RevenueCatConfig.android.kt (or .ios.kt)
2. Check apiKey value
3. Expected: Starts with "goog_" (Android) or "appl_" (iOS)
   ❌ If placeholder: Update with real API key from dashboard
```

### Step 3: Check Offerings
```
1. In debug screen, click "📦 Query Products/Offerings"
2. Expected: Shows offering "ofrng74226a750e" with 3 packages
   ❌ If empty: Check RevenueCat dashboard for offering
```

### Step 4: Check Products
```
1. Look at product IDs and prices in debug screen
2. Expected: 
   - Lifetime: $19.99 (or your price)
   - Monthly: $2.99
   - Annual: $19.99
   ❌ If $0.00: Products not configured in Play Console/App Store
```

### Step 5: Check Logs
```
1. Filter logs for "RevenueCat"
2. Look for initialization messages
3. Expected: 
   "RevenueCat: Configuration complete"
   "RevenueCat: Initialization successful"
   ❌ If errors: Check error message for specific issue
```

---

## 🛠️ Quick Fixes

### Fix 1: Initialize RevenueCat (5 minutes)

**File:** `composeApp/src/androidMain/kotlin/.../MainApplication.kt`

Add after Koin initialization:
```kotlin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import org.koin.android.ext.android.inject

class MainApplication : Application() {
    private val revenueCatManager: RevenueCatManager by inject()
    
    override fun onCreate() {
        super.onCreate()
        // ... existing code ...
        
        // Initialize RevenueCat
        GlobalScope.launch {
            revenueCatManager.initialize()
        }
    }
}
```

### Fix 2: Add Real API Key (2 minutes)

**File:** `composeApp/src/androidMain/kotlin/.../data/config/RevenueCatConfig.android.kt`

```kotlin
actual object RevenueCatConfig {
    actual val apiKey: String = "goog_YOUR_ACTUAL_KEY_HERE" // Get from dashboard
    actual val platform: String = "Android"
    actual val isDebug: Boolean = BuildConfig.IS_DEBUG
}
```

### Fix 3: Test Again (1 minute)

1. **Rebuild app:** `./gradlew clean build`
2. **Reinstall app:** Uninstall old version, install new
3. **Wait 5 seconds** for initialization
4. **Go to Debug Settings** → RevenueCat Testing
5. **Click "Check Initialization"** - Should show "true"
6. **Click "Query Products"** - Should show offerings

---

## 📱 Testing on Different Platforms

### Android
- ✅ Should work with real API key
- ✅ Requires Google Play Services
- ✅ Test on physical device or emulator with Play Store

### iOS
- ✅ Should work with real API key  
- ✅ Requires StoreKit
- ✅ Test on physical device or simulator

### Desktop/JVM
- ⚠️ **Not supported** - Expected behavior
- Uses stub implementation
- Debug tools will show "platform not supported"

---

## 🆘 Still Not Working?

### Check RevenueCat Dashboard Logs

1. Go to https://app.revenuecat.com/
2. Navigate to **Customer History**
3. Search for your test device (by App User ID)
4. Check for API errors or failed requests

### Enable Verbose Logging

In `RevenueCatManager.kt`:
```kotlin
suspend fun initialize(appUserId: String? = null) {
    // Enable verbose logging
    Purchases.logLevel = LogLevel.VERBOSE // Change from DEBUG
    
    // ... rest of initialization ...
}
```

### Contact Support

If still stuck after trying everything:

1. **Check RevenueCat Status:** https://status.revenuecat.com/
2. **Community Forum:** https://community.revenuecat.com/
3. **Documentation:** https://www.revenuecat.com/docs/
4. **Support:** support@revenuecat.com

---

## 📚 Related Documentation

- [REVENUECAT_DEBUG_TOOLS.md](./REVENUECAT_DEBUG_TOOLS.md) - Debug tools guide
- [REVENUECAT_QUICK_START.md](./REVENUECAT_QUICK_START.md) - Implementation steps
- [REVENUECAT_TRANSITION_PLAN.md](./REVENUECAT_TRANSITION_PLAN.md) - Migration plan
- [SUBSCRIPTION_GOOGLE_PLAY_SETUP.md](./SUBSCRIPTION_GOOGLE_PLAY_SETUP.md) - Product setup

---

## ✅ Success Criteria

You'll know it's working when:

✅ Debug tool shows "Initialized: true"  
✅ Offerings query returns offering ID  
✅ 3 packages show up ($rc_lifetime, $rc_monthly, $rc_annual)  
✅ Prices show correctly (not $0.00)  
✅ Can see package details  
✅ Entitlements check returns data  

**Example Success Output:**
```
✅ RevenueCat Status:
• Initialized: true
• CustomerInfo: ✅ Loaded
• Current Offering: ofrng74226a750e
• Active Entitlements: (none for free user)

📦 Products/Offerings:
✅ Offering: ofrng74226a750e

Available Packages:
• $rc_lifetime
  Product: spacelaunchnow_pro
  Price: $19.99
  Period: Lifetime

• $rc_monthly
  Product: base_plan
  Price: $2.99
  Period: MONTH

• $rc_annual
  Product: yearly
  Price: $19.99
  Period: YEAR
```

Now you're ready to proceed with the migration! 🚀
