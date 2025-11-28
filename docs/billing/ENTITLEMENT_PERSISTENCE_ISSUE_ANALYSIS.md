# Entitlement Persistence Issue Analysis

## Problem Summary

Users who have made purchases and have valid entitlements in RevenueCat dashboard are unable to
access premium features in the app.

## Root Cause Analysis

After investigating the billing system, I've identified **4 critical issues** that could prevent
entitlement state from being saved:

---

## Issue #1: Missing Error Handling in DataStore Operations ⚠️ CRITICAL

### Location

- `SubscriptionStorage.kt` (DataStore-based storage)
- `LocalSubscriptionStorage.kt` (KStore-based storage)

### Problem

Neither storage implementation has try-catch blocks around file I/O operations. If DataStore or
KStore fail to write to disk (due to permissions, storage full, corruption), the exceptions are
silently propagated and the app continues without persistence.

### Evidence

```kotlin
// SubscriptionStorage.kt - NO error handling
suspend fun saveState(state: SubscriptionState) {
    dataStore.edit { preferences ->  // ❌ Could throw IOException
        preferences[IS_SUBSCRIBED] = state.isSubscribed
        // ... more writes
    }
}

// LocalSubscriptionStorage.kt - NO error handling  
suspend fun update(data: LocalSubscriptionData) {
    store.set(data)  // ❌ Could throw IOException
}
```

### Impact

- Users with storage issues (full disk, permission problems, corrupted DataStore file) lose their
  entitlements on every app restart
- No logging means we can't diagnose the issue
- Silent failures provide no feedback to user or developer

### Affected Scenarios

1. Device storage nearly full
2. App data directory permissions changed (rare but possible)
3. DataStore/KStore file corruption
4. Android killing app during write operation
5. Multi-process access conflicts (if widget tries to access same file)

---

## Issue #2: No Verification of Successful Persistence

### Location

- `AndroidBillingManager.kt` - `updatePurchaseState()`
- `SubscriptionSyncer.kt` - sync flow

### Problem

After receiving entitlements from RevenueCat, the code updates in-memory state (
`_purchaseState.value`) and **assumes** the persistence will succeed. There's no verification that
the data was actually written to disk.

### Evidence

```kotlin
// AndroidBillingManager.kt
private fun updatePurchaseState(customerInfo: CustomerInfo) {
    _purchaseState.value = PurchaseState(...)  // ✅ In-memory update
    // ❌ No verification that SubscriptionSyncer persisted this
}

// SubscriptionSyncer.kt
syncScope.launch {
    billingManager.purchaseState.collect { purchaseState ->
        localStorage.update(...)  // ❌ Fire and forget - no error checking
    }
}
```

### Impact

- If persistence fails, the app appears to work fine during current session
- On next app restart, user loses premium access
- No way to detect persistence failures

---

## Issue #3: Race Condition in Initialization

### Location

- `MainApplication.kt` - onCreate()
- `SubscriptionSyncer.kt` - startSyncing()

### Problem

The initialization sequence has a potential race condition:

```kotlin
// MainApplication.kt
GlobalScope.launch {
    billingManager.initialize()              // Step 1: RevenueCat init
    syncer.startSyncing()                    // Step 2: Start listening
    repository.initialize()                  // Step 3: Load cached state
    syncer.syncNow()                         // Step 4: Force sync
}
```

Between Step 1 and Step 2, RevenueCat might emit customer info updates that aren't captured by the
syncer. The syncer only starts listening in Step 2, potentially missing the initial entitlement
data.

### Impact

- First-time users might miss initial entitlement sync
- Users who just made a purchase might not see immediate access
- Requires manual restore or app restart

---

## Issue #4: Dual Storage Without Consistency Guarantees

### Location

- `SubscriptionStorage.kt` (DataStore - legacy/unused?)
- `LocalSubscriptionStorage.kt` (KStore - actively used)
- Both registered in DI container

### Problem

The codebase has TWO separate storage mechanisms for subscription state:

1. **SubscriptionStorage** using DataStore (appears to be legacy)
2. **LocalSubscriptionStorage** using KStore (actively used by SubscriptionSyncer)

The `SubscriptionStorage` class is created in DI but **never used** by the active code paths. This
creates confusion and potential for bugs.

### Evidence

```kotlin
// AppModule.kt
single {
    val subscriptionDataStore = get<DataStore<Preferences>>(named("SubscriptionDataStore"))
    SubscriptionStorage(subscriptionDataStore)  // ✅ Created
}
// ❌ Never injected into any active component

single { LocalSubscriptionStorage() }  // ✅ Created AND used
```

### Impact

- Code confusion - unclear which storage is authoritative
- Wasted resources creating unused DataStore
- Risk of future bugs if someone tries to use SubscriptionStorage

---

## Recommended Fixes (Priority Order)

### Fix #1: Add Error Handling to All Storage Operations (CRITICAL)

Add try-catch blocks with logging around all DataStore and KStore operations:

```kotlin
suspend fun saveState(state: SubscriptionState) {
    try {
        dataStore.edit { preferences ->
            preferences[IS_SUBSCRIBED] = state.isSubscribed
            // ... more writes
        }
        DatadogLogger.info("Subscription state saved successfully")
    } catch (e: IOException) {
        DatadogLogger.error(
            "Failed to save subscription state to DataStore", e, mapOf(
                "subscription_type" to state.subscriptionType.name,
                "is_subscribed" to state.isSubscribed
            )
        )
        // Consider showing user notification or fallback strategy
    } catch (e: Exception) {
        DatadogLogger.error("Unexpected error saving subscription state", e)
    }
}
```

### Fix #2: Add Persistence Verification

After saving state, verify it was written correctly:

```kotlin
suspend fun updateWithVerification(data: LocalSubscriptionData): Boolean {
    return try {
        store.set(data)

        // Verify write succeeded by reading back
        val readBack = store.get()
        val success = readBack == data

        if (!success) {
            DatadogLogger.error("Subscription state verification failed - read-back mismatch")
        }

        success
    } catch (e: Exception) {
        DatadogLogger.error("Failed to persist subscription state", e)
        false
    }
}
```

### Fix #3: Ensure Syncer Captures Initial State

Wait for initial customer info before starting syncer:

```kotlin
// MainApplication.kt
GlobalScope.launch {
    val customerInfo = billingManager.initialize()  // Returns initial customer info

    // Manually persist initial state BEFORE starting syncer
    if (customerInfo != null) {
        localStorage.updateWithVerification(/* map customerInfo to LocalSubscriptionData */)
    }

    syncer.startSyncing()  // Now safe to start listening for updates
}
```

### Fix #4: Remove Unused SubscriptionStorage

1. Migrate any remaining references to `LocalSubscriptionStorage`
2. Remove `SubscriptionStorage` class
3. Remove `SubscriptionDataStore` from DI modules

### Fix #5: Add Health Check API

Create a debug/support endpoint that checks storage health:

```kotlin
suspend fun checkStorageHealth(): StorageHealthReport {
    return StorageHealthReport(
        canWriteDataStore = testDataStoreWrite(),
        canWriteKStore = testKStoreWrite(),
        dataStoreFileExists = checkDataStoreFileExists(),
        kstoreFileExists = checkKStoreFileExists(),
        hasStoragePermissions = checkPermissions(),
        availableDiskSpace = getAvailableDiskSpace()
    )
}
```

---

## Immediate Action Items

1. ✅ **COMPLETE** - Add error handling to `LocalSubscriptionStorage.update()`
2. ✅ **COMPLETE** - Add error handling to `SubscriptionStorage.saveState()`
3. ✅ **COMPLETE** - Add persistence verification in `SubscriptionSyncer`
4. ✅ **COMPLETE** - Add Datadog logging for all storage failures
5. ⚠️ **PENDING** - Fix initialization race condition in `MainApplication.kt` (requires testing)
6. ⚠️ **FUTURE** - Consider removing duplicate `SubscriptionStorage` (requires careful migration)
7. ⚠️ **FUTURE** - Add health check debug tool

## Implementation Summary

### Changes Made (2024-11-28)

#### 1. LocalSubscriptionStorage.kt - Error Handling & Verification

- ✅ Added try-catch blocks around all `store.set()` operations
- ✅ Implemented write verification by reading back data after save
- ✅ Added comprehensive Datadog logging for all failure scenarios
- ✅ Changed return types from `Unit` to `Boolean` for success/failure indication
- ✅ Added specific error handling for `IOException` vs other exceptions
- ✅ Log details include subscription type, entitlements, and error type

#### 2. SubscriptionSyncer.kt - Persistence Verification

- ✅ Check return value from `localStorage.update()`
- ✅ Log CRITICAL errors when persistence fails
- ✅ Call `localStorage.markNeedsSync()` on failure to retry on next app start
- ✅ Added Datadog error tracking with subscription state context

#### 3. SubscriptionStorage.kt - DataStore Error Handling

- ✅ Added try-catch blocks around all `dataStore.edit()` operations
- ✅ Changed return types from `Unit` to `Boolean` for all write methods
- ✅ Added comprehensive Datadog logging for all failure scenarios
- ✅ Specific error handling for `IOException` vs other exceptions

### What This Fixes

**Before**:

- Storage failures were silent - users lost entitlements on restart
- No visibility into why purchases weren't persisting
- No way to diagnose storage issues

**After**:

- All storage failures are logged to Datadog with context
- Failed writes trigger retry mechanisms
- Write verification ensures data was actually saved
- Boolean return values allow calling code to handle failures gracefully

### Monitoring

With these changes, you can now monitor in Datadog:

- Total persistence failures (count by error type)
- Verification failures (data mismatch after write)
- User IDs experiencing persistence issues
- Correlation with device storage levels

---

## Testing Recommendations

### Test Case 1: Full Disk Simulation

1. Fill device storage to 99%
2. Make a purchase
3. Verify error is logged to Datadog
4. Check if app shows any user feedback

### Test Case 2: File Corruption

1. Manually corrupt KStore JSON file
2. Launch app
3. Verify app recovers gracefully
4. Check if RevenueCat sync restores state

### Test Case 3: App Kill During Write

1. Start purchase flow
2. Kill app immediately after purchase completes
3. Restart app
4. Verify entitlement is restored

### Test Case 4: Multi-Process Access

1. Enable widget
2. Make purchase while widget is updating
3. Verify no file lock conflicts

---

## Monitoring Recommendations

Add Datadog metrics for:

- `subscription.persistence.success` (counter)
- `subscription.persistence.failure` (counter with error type)
- `subscription.verification.mismatch` (counter)
- `subscription.storage.disk_full` (counter)
- `subscription.recovery.attempted` (counter)
- `subscription.recovery.success` (counter)

This will help identify how many users are affected by persistence issues.

---

## Long-Term Recommendation

Consider migrating entirely to either DataStore OR KStore (not both):

- **DataStore Pros**: Official Android library, better coroutine support, type-safe
- **KStore Pros**: Multiplatform, simpler API, JSON-based (easier debugging)

Current dual approach creates unnecessary complexity and maintenance burden.

