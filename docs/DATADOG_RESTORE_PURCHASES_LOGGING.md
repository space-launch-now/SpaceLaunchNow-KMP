# Datadog Restore Purchases Logging

## Overview

Comprehensive Datadog logging has been implemented for the restore purchases flow to help debug user issues with subscription restoration.

## Implementation Summary

### 1. Global Datadog Logger (`DatadogConfig.kt`)

Created a global `DatadogLogger` object for consistent logging throughout the application:

```kotlin
object DatadogLogger {
    fun debug(message: String, attributes: Map<String, Any?> = emptyMap())
    fun info(message: String, attributes: Map<String, Any?> = emptyMap())
    fun warn(message: String, attributes: Map<String, Any?> = emptyMap())
    fun error(message: String, throwable: Throwable? = null, attributes: Map<String, Any?> = emptyMap())
    fun critical(message: String, throwable: Throwable? = null, attributes: Map<String, Any?> = emptyMap())
}
```

**Features:**
- Logs to both console (for development) and Datadog (for production)
- Supports structured attributes for better filtering/analysis
- Handles throwables for error tracking
- Automatically bundles logs with RUM sessions

### 2. User Identification in Datadog

User information is now set in Datadog during customer info refresh:

**Location:** `RevenueCatManager.refreshCustomerInfo()`

**User attributes tracked:**
- `id`: RevenueCat's `originalAppUserId`
- `platform`: Android/iOS
- `active_entitlements`: Comma-separated list of active entitlements
- `active_subscriptions`: Comma-separated list of active subscription product IDs
- `has_premium`: Boolean indicating premium entitlement status
- `first_seen`: First time user was seen in RevenueCat
- `has_legacy_purchase`: Boolean indicating non-subscription purchases (after restore)

This allows you to:
- Filter Datadog logs by specific users
- See user context in error reports
- Track user journey through subscription flows

### 3. Restore Purchases Logging Flow

#### RevenueCatManager.restorePurchases()

**Logs captured:**

1. **Start of restore:**
   ```
   INFO: Restore purchases started
   - user_id: <RevenueCat user ID>
   ```

2. **On success:**
   ```
   INFO: Restore purchases successful
   - user_id: <RevenueCat user ID>
   - total_products_found: <count>
   - all_products: <comma-separated product IDs>
   - active_entitlements: <comma-separated entitlement IDs>
   - active_subscriptions: <comma-separated subscription product IDs>
   - non_subscription_count: <count of lifetime purchases>
   - entitlement_details: <detailed JSON of each entitlement>
   - non_subscription_details: <detailed JSON of each legacy purchase>
   - management_url: <URL to manage subscription>
   - original_purchase_date: <first purchase date>
   - first_seen: <first seen date>
   ```

3. **On error:**
   ```
   ERROR: Restore purchases failed
   - error_message: <error message>
   - error_code: <RevenueCat error code>
   - underlying_error: <platform error if any>
   - user_id: <RevenueCat user ID>
   ```

4. **On exception:**
   ```
   ERROR: Exception during restore purchases
   - user_id: <RevenueCat user ID>
   - throwable: <stack trace>
   ```

#### SubscriptionRepositoryImpl.restorePurchases()

**Logs captured:**

1. **Flow start:**
   ```
   INFO: Restore purchases flow started in SubscriptionRepository
   - current_state: <FREE/BASIC/PREMIUM/LEGACY>
   - is_subscribed: <true/false>
   ```

2. **RevenueCat sync:**
   ```
   INFO: Calling RevenueCat.restorePurchases() to sync with platform store
   ```

3. **Verification start:**
   ```
   INFO: Verifying subscription after restore
   ```

4. **Verification success:**
   ```
   INFO: Restore purchases completed successfully
   - new_state: <FREE/BASIC/PREMIUM/LEGACY>
   - is_subscribed: <true/false>
   - has_entitlements: <true/false>
   - active_entitlements: <comma-separated>
   - verification_source: <error if any, otherwise "none">
   ```

5. **Verification failure:**
   ```
   ERROR: Restore purchases failed during verification
   - error_message: <error message>
   ```

6. **Exception:**
   ```
   ERROR: Exception during restore purchases flow
   - error_message: <error message>
   - error_type: <exception class name>
   ```

#### super_verifySubscription() (called by restore flow)

**Logs captured:**

1. **Using cached state:**
   ```
   DEBUG: Using cached subscription state
   - subscription_type: <type>
   - is_subscribed: <true/false>
   ```

2. **Starting verification:**
   ```
   INFO: Starting subscription verification
   - force_refresh: <true/false>
   - current_subscription_type: <type>
   - needs_verification: <true/false>
   ```

3. **Billing query success:**
   ```
   INFO: Platform billing query successful
   - purchases_count: <count>
   - purchase_tokens: <comma-separated tokens>
   ```

4. **No purchases, checking legacy:**
   ```
   INFO: No active purchases from billing client, checking RevenueCat for legacy purchases
   ```

5. **Legacy purchase found:**
   ```
   INFO: Legacy purchase found in RevenueCat
   - subscription_type: <type>
   - active_entitlements: <comma-separated>
   ```

6. **No legacy purchases:**
   ```
   INFO: No legacy purchases found in RevenueCat
   ```

7. **Verification complete:**
   ```
   INFO: Subscription verification complete
   - is_subscribed: <true/false>
   - subscription_type: <type>
   - active_entitlements: <comma-separated>
   - has_premium: <true/false>
   ```

8. **Billing query failed:**
   ```
   ERROR: Platform billing query failed
   - error_message: <error message>
   ```

9. **Fallback attempt:**
   ```
   INFO: Attempting RevenueCat fallback after billing error
   ```

10. **Fallback success:**
    ```
    INFO: Legacy purchase found despite billing error
    - subscription_type: <type>
    ```

11. **No fallback:**
    ```
    WARN: No fallback purchases found, using cached state with error
    ```

## Debugging User Issues

### Scenario 1: User says "Restore didn't work"

**Datadog query:**
```
@usr.id:<user_id> @message:"Restore purchases*"
```

**What to look for:**
1. Was restore even triggered?
2. Did RevenueCat return successfully?
3. What products were found?
4. Did verification complete?
5. What was the final subscription state?

### Scenario 2: Legacy purchase not recognized

**Datadog query:**
```
@usr.id:<user_id> "legacy purchase"
```

**What to look for:**
1. `non_subscription_count` > 0 in restore success log?
2. `non_subscription_details` showing the product ID?
3. Was `checkForLegacyPurchasesInRevenueCat()` called?
4. Did it find the legacy purchase?

### Scenario 3: RevenueCat error

**Datadog query:**
```
@usr.id:<user_id> @error_code:*
```

**What to look for:**
1. `error_code` from RevenueCat
2. `underlying_error` from platform (Google Play/App Store)
3. Was fallback attempted?
4. Did fallback succeed?

### Scenario 4: Subscription shows but features don't unlock

**Datadog query:**
```
@usr.id:<user_id> "verification complete"
```

**What to look for:**
1. `is_subscribed: true` but `has_premium: false`?
2. `active_entitlements` list - is it empty or missing "premium"?
3. Check `subscription_type` - is it BASIC instead of PREMIUM?
4. Check `entitlement_details` for expiration dates

## User Attributes Available in All Logs

Once a user's customer info is loaded, all subsequent logs will include:

- `usr.id`: RevenueCat user ID
- `usr.platform`: Android/iOS
- `usr.active_entitlements`: Current entitlements
- `usr.active_subscriptions`: Current subscriptions
- `usr.has_premium`: Premium status
- `usr.first_seen`: First seen date
- `usr.has_legacy_purchase`: Has legacy purchases (after restore)

## Testing

To test logging is working:

1. Enable Datadog in `.env`:
   ```
   DATADOG_ENABLED=true
   DATADOG_CLIENT_TOKEN=<your_token>
   DATADOG_APPLICATION_ID=<your_app_id>
   DATADOG_ENVIRONMENT=development
   ```

2. Run the app and trigger restore purchases

3. Check Datadog Logs Explorer for your logs:
   - Filter by `service:space-launch-now`
   - Filter by `env:development`
   - Search for "Restore purchases"

4. Check RUM Sessions to see user context

## Related Files

- `/composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/analytics/DatadogConfig.kt` - Logger implementation
- `/composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatManager.kt` - RevenueCat restore logging
- `/composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/SubscriptionRepositoryImpl.kt` - Repository restore logging

## Best Practices

1. **Always check user context first** - Filter logs by `usr.id` to isolate user-specific issues
2. **Look for the full flow** - Restore involves multiple steps, check all log messages
3. **Check attributes** - Structured attributes make filtering much easier than parsing log messages
4. **Compare before/after** - Check `current_state` vs `new_state` to see what changed
5. **Check timestamps** - Make sure events happened in the right order and timing
