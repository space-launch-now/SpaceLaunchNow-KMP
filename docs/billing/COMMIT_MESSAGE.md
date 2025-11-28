# Suggested Commit Message

```
fix(billing): add error handling and verification to subscription persistence

Critical fix for users losing premium access after app restart.

## Problem
Users with valid RevenueCat entitlements were unable to access premium 
features after app restart. Investigation revealed silent storage failures 
with no error handling, logging, or retry mechanism.

## Root Causes
1. No try-catch blocks around DataStore/KStore write operations
2. No verification that data was actually written to disk
3. No logging when persistence failed
4. No retry mechanism for failed writes

## Changes

### LocalSubscriptionStorage.kt
- Add error handling with try-catch for IOException
- Implement write verification (read-back after save)
- Return Boolean success status instead of Unit
- Add comprehensive Datadog error logging
- Log subscription type, entitlements, and error details

### SubscriptionSyncer.kt
- Check Boolean return from localStorage.update()
- Log CRITICAL error when persistence fails
- Call markNeedsSync() on failure to trigger retry
- Add Datadog logging with subscription state context

### SubscriptionStorage.kt
- Add error handling to all DataStore write methods
- Return Boolean success status from all write operations
- Add Datadog error logging for failures

## Impact
- Storage failures now logged to Datadog for monitoring
- Failed writes trigger automatic retry on next app start
- Write verification ensures data actually persisted
- Provides visibility into storage issues (disk full, corruption, etc.)

## Monitoring
After deployment, monitor Datadog for:
- "Failed to save subscription state to KStore" (storage failures)
- "Subscription state verification failed" (write verification)
- "CRITICAL: Failed to persist subscription state" (sync failures)

## Testing
Tested with:
- Full disk simulation (95%+ capacity)
- File corruption scenarios
- App kill during write operation

## Documentation
- docs/billing/ENTITLEMENT_PERSISTENCE_ISSUE_ANALYSIS.md (full analysis)
- docs/billing/PERSISTENCE_FIXES_SUMMARY.md (implementation summary)

Fixes: Users losing premium access on app restart
Related: RevenueCat entitlement synchronization
```

## Alternative (Conventional Commits Format)

```
fix(billing): add persistence error handling and verification

Users with valid RevenueCat entitlements unable to access premium features 
after app restart due to silent storage failures.

- Add try-catch blocks to all DataStore/KStore operations
- Implement write verification with read-back
- Return Boolean success status from write methods
- Add Datadog error logging with subscription context
- Trigger retry via markNeedsSync() on failure

This enables monitoring of storage issues (disk full, corruption, 
permissions) and automatic recovery.

BREAKING CHANGE: None (backward compatible - unused Boolean returns)
```

## Alternative (Short Format)

```
fix(billing): handle subscription storage failures

Add error handling and verification to subscription persistence layer.
Silent storage failures were causing users to lose premium access on restart.

- Wrap DataStore/KStore writes in try-catch
- Verify writes by reading back data
- Log failures to Datadog with context
- Trigger retry on failure
```

