# Trial-to-Paid Conversion Fix

**Date:** November 28, 2025  
**Issue:** Subscribed customers shown as "Free User" after trial conversion  
**Customer Case:** Trial converted to paid (GBP 8.99 yearly) but app still shows "Free User"

---

## Root Cause Analysis

### Timeline of Customer Issue

1. **2025-11-24 01:11 AM UTC**: First seen using the app
2. **2025-11-24 01:20 PM UTC**: Started trial of sln_production_yearly:yearly
3. **2025-11-27 07:18 AM UTC**: Last opened the app, trial expired, started new trial
4. **2025-11-27 01:19 PM UTC**: **Converted from trial to paid subscription (GBP 8.99)**
5. **Customer Report**: Still showing as "Free User" despite active subscription

### The Problem

**Missing RevenueCat Sync on App Resume:**
When a user converts from trial to paid subscription while the app is in the background or closed,
the current implementation does NOT properly sync with RevenueCat/Google Play when the app resumes.

**Current Flow (BROKEN):**

```
1. App resumes → MainActivity.onResume()
2. Calls billingManager.refreshPurchaseState()
3. RevenueCat returns CACHED customer info (still shows trial)
4. App displays stale "Free User" status
```

**What's Missing:**

- `syncPurchases()` is NOT called on resume
- `getCustomerInfo()` uses RevenueCat's cache
- No server-side verification of subscription status

### Why the Diff Wasn't Sufficient

The `billing-persistence-fixes.diff` added:

- ✅ `isDebugMode` field to separate debug state from sync state
- ✅ `MainActivity.onResume()` refresh with 30-second rate limiting
- ✅ Better logging

But it did NOT add:

- ❌ `syncPurchases()` call before `refreshPurchaseState()`
- ❌ Force server verification flag

---

## The Fix

### Change #1: Sync Purchases on Resume

**File:** `MainActivity.kt`

Add `billingManager.syncPurchases()` BEFORE `refreshPurchaseState()`:

```kotlin
override fun onResume() {
    super.onResume()
    // ...existing setActivity code...
    
    val currentTime = System.currentTimeMillis()
    if (currentTime - lastPurchaseRefreshTime > purchaseRefreshCooldownMs) {
        lastPurchaseRefreshTime = currentTime
        lifecycleScope.launch {
            try {
                println("MainActivity: Syncing purchases with store on resume...")
                
                // CRITICAL: Sync with store FIRST to get latest subscription status
                billingManager.syncPurchases()
                
                // THEN refresh purchase state (will now have fresh data)
                println("MainActivity: Refreshing purchase state after sync...")
                val refreshed = billingManager.refreshPurchaseState()
                
                if (refreshed) {
                    println("MainActivity: ✅ Purchase state refreshed successfully")
                } else {
                    println("MainActivity: ⚠️ Failed to refresh purchase state")
                }
            } catch (e: Exception) {
                println("MainActivity: Error refreshing purchase state: ${e.message}")
            }
        }
    }
}
```

### Why This Works

**RevenueCat's Two-Step Process:**

1. `syncPurchases()`: Contacts Google Play to get current subscription status, updates RevenueCat
   servers
2. `getCustomerInfo()`: Retrieves customer info (may use cache if recent)

By calling `syncPurchases()` FIRST, we ensure RevenueCat has the latest subscription data from
Google Play BEFORE we query for customer info.

---

## Testing Plan

### Test Case 1: Trial Conversion (Main Issue)

1. Start trial subscription
2. Convert to paid subscription via Google Play (outside app)
3. Force-stop the app
4. Reopen app
5. **Expected**: User shown as premium subscriber
6. **Actual (before fix)**: User shown as "Free User"
7. **Actual (after fix)**: User shown as premium subscriber

### Test Case 2: Subscription Renewal

1. Have active subscription near expiry
2. Let it auto-renew while app in background
3. Resume app
4. **Expected**: Subscription status remains active

### Test Case 3: Subscription Cancellation

1. Have active subscription
2. Cancel via Google Play
3. Resume app
4. **Expected**: Status updates to show pending cancellation

### Test Case 4: Rate Limiting

1. Resume app
2. Immediately background and resume again
3. **Expected**: Second resume skips sync (30s cooldown)
4. Wait 30 seconds, resume again
5. **Expected**: Third resume triggers sync

---

## Monitoring

### Logs to Watch For

**Success Path:**

```
MainActivity: Syncing purchases with store on resume...
AndroidBillingManager: 🔄 Syncing purchases with store...
AndroidBillingManager: ✅ Purchases synced
MainActivity: Refreshing purchase state after sync...
AndroidBillingManager: 🔄 Refreshing purchase state...
AndroidBillingManager: 📊 Updating purchase state...
  • Subscription Type: PREMIUM
  • Active Entitlements: [Space Launch Now - Pro]
MainActivity: ✅ Purchase state refreshed successfully
SubscriptionSyncer: Purchase state updated, syncing...
SubscriptionSyncer: ✅ Sync complete - isSubscribed=true
```

**Error Path:**

```
MainActivity: Error refreshing purchase state: [error message]
AndroidBillingManager: ⚠️ Sync failed - [error message]
```

### Datadog Metrics

Monitor these events:

- `purchases_synced_on_resume` (success count)
- `purchase_refresh_on_resume` (success count)
- `subscription_type_changed` (track conversions)
- `sync_failures_on_resume` (error count)

---

## Related Issues

- **#1**: Missing error handling in storage layer (fixed in previous diff)
- **#2**: `needsSync` conflated with debug mode (fixed in previous diff)
- **#3**: No automatic refresh on app resume (PARTIALLY fixed - needed sync call)

---

## Rollout Plan

1. **Phase 1**: Apply fix to main branch
2. **Phase 2**: Monitor logs in next release for 48 hours
3. **Phase 3**: Check RevenueCat dashboard for successful conversions
4. **Phase 4**: Reach out to affected users for confirmation

---

## Customer Communication Template

> Hi [Customer],
>
> Thank you for reporting this issue. We've identified and fixed a bug where subscription status
> wasn't properly refreshing when the app resumed.
>
> The fix ensures that when you convert from trial to paid (or make any subscription changes), the
> app will immediately sync with Google Play when reopened.
>
> This will be included in the next update. In the meantime, you can force a refresh by:
> 1. Going to Settings
> 2. Tapping "Restore Purchases"
>
> Your subscription is active in our system, and you should have full access to premium features
> once you restore purchases.
>
> Thanks for your patience!

