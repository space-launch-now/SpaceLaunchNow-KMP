# Quickstart: Fix V5 Notification Filter Bug

**Branch**: `fix_notif_filters`  
**Est. Time**: 2-3 hours  
**Complexity**: Medium (requires careful testing)

## Prerequisites

- ✅ Java 21 installed (JetBrains JDK 21 for Compose Hot Reload)
- ✅ Android Studio or IntelliJ IDEA with Kotlin plugin
- ✅ Git checkout on branch `fix_notif_filters`
- ✅ Firebase Test Lab access (or physical Android device for testing)

## Quick Implementation Guide

### Step 1: Modify V5NotificationPayload (10 min)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationPayload.kt`

**Changes**:
1. Change all ID field types from `Int?` / `List<Int>` → `String?`
2. Update `fromMap()` parsing to keep IDs as Strings (remove `toIntOrNull()` calls)
3. Change `programIds: List<Int>` → `programId: String?` (single value)

**Example**:
```kotlin
// BEFORE
val lspId: Int?,
val locationId: Int?,
val programIds: List<Int>,

// AFTER  
val lspId: String?,
val locationId: String?,
val programId: String?,
```

```kotlin
// BEFORE in fromMap()
lspId = data["lsp_id"]?.toIntOrNull(),
locationId = data["location_id"]?.toIntOrNull(),
programIds = data["program_ids"]?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList(),

// AFTER in fromMap()
lspId = data["lsp_id"],
locationId = data["location_id"],
programId = data["program_id"],
```

---

### Step 2: Simplify V5NotificationFilter (30 min)

**File**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilter.kt`

**Changes**:
1. Change `shouldShow()` signature from `V5FilterPreferences` → `NotificationState`
2. Replace complex category filter logic with simple V4-style String membership checks
3. Remove null semantics handling
4. Remove immediate block checks
5. Copy V4 mature filter pattern

**New Logic**:
```kotlin
fun shouldShow(payload: V5NotificationPayload, state: NotificationState): FilterResult {
    // 1. Master enable
    if (!state.enableNotifications) return FilterResult.blocked("Notifications disabled")
    
    // 2. Follow all bypass
    if (state.followAllLaunches) return FilterResult.Allowed
    
    // 3. Empty filters block
    if (state.subscribedAgencies.isEmpty() && state.subscribedLocations.isEmpty()) {
        return FilterResult.blocked("No subscriptions")
    }
    
    // 4. Check filters
    val hasAgencyFilter = state.subscribedAgencies.isNotEmpty()
    val hasLocationFilter = state.subscribedLocations.isNotEmpty()
    
    val agencyMatches = if (hasAgencyFilter) {
        payload.lspId in state.subscribedAgencies
    } else {
        true
    }
    
    val locationMatches = if (hasLocationFilter) {
        payload.locationId in state.subscribedLocations
    } else {
        true
    }
    
    // 5. Apply matching mode
    return if (state.useStrictMatching) {
        if (!hasAgencyFilter || !hasLocationFilter) {
            FilterResult.blocked("Strict requires both filters active")
        } else if (agencyMatches && locationMatches) {
            FilterResult.Allowed
        } else {
            FilterResult.blocked("Strict: agency=$agencyMatches, location=$locationMatches")
        }
    } else {
        if (agencyMatches || locationMatches) {
            FilterResult.Allowed
        } else {
            FilterResult.blocked("No match: agency=$agencyMatches, location=$locationMatches")
        }
    }
}
```

---

### Step 3: Update NotificationWorker (5 min)

**File**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/NotificationWorker.kt`

**Change**:
```kotlin
// BEFORE
val settings = notificationStateStorage.getState()
val v5Preferences = settings.v5Preferences
val filterResult = V5NotificationFilter.shouldShow(v5Payload, v5Preferences)

// AFTER
val state = notificationStateStorage.getState()
val filterResult = V5NotificationFilter.shouldShow(v5Payload, state)
```

---

### Step 4: Add Test Suite (45 min)

**File**: `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/model/V5NotificationFilterSimplifiedTest.kt` (NEW)

**Test Cases** (minimum required):

```kotlin
class V5NotificationFilterSimplifiedTest {
    
    @Test
    fun `SpaceX from Florida - ALLOW when subscribed`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),  // SpaceX
            subscribedLocations = setOf("12")   // Cape Canaveral
        )
        
        val payload = V5NotificationPayload(
            lspId = "121",
            locationId = "12",
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertTrue(result.shouldShow())
    }
    
    @Test
    fun `China from Jiuquan - BLOCK when not subscribed`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),  // SpaceX only
            subscribedLocations = setOf("12")   // Florida only
        )
        
        val payload = V5NotificationPayload(
            lspId = "96",   // China
            locationId = "17"  // Jiuquan
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertFalse(result.shouldShow())
    }
    
    @Test
    fun `ULA from Florida flexible - ALLOW when location matches`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),  // SpaceX
            subscribedLocations = setOf("12"),  // Florida
            useStrictMatching = false  // Flexible
        )
        
        val payload = V5NotificationPayload(
            lspId = "124",   // ULA (not subscribed)
            locationId = "12"   // Florida (subscribed)
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertTrue(result.shouldShow())  // Flexible allows location match
    }
    
    @Test
    fun `ULA from Florida strict - BLOCK when agency doesn't match`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),  // SpaceX
            subscribedLocations = setOf("12"),  // Florida
            useStrictMatching = true  // Strict
        )
        
        val payload = V5NotificationPayload(
            lspId = "124",   // ULA (not subscribed)
            locationId = "12"   // Florida (subscribed)
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertFalse(result.shouldShow())  // Strict requires both to match
    }
    
    @Test
    fun `Follow all launches - ALLOW everything`() {
        val state = NotificationState(
            followAllLaunches = true,  // Bypass filters
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("12")
        )
        
        val payload = V5NotificationPayload(
            lspId = "96",   // China (not subscribed)
            locationId = "17"  // Jiuquan (not subscribed)
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertTrue(result.shouldShow())  // Follow all bypasses filters
    }
    
    @Test
    fun `Notifications disabled - BLOCK everything`() {
        val state = NotificationState(
            enableNotifications = false,  // Master switch off
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("12")
        )
        
        val payload = V5NotificationPayload(
            lspId = "121",   // SpaceX (subscribed)
            locationId = "12"  // Florida (subscribed)
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertFalse(result.shouldShow())  // Master switch blocks all
    }
    
    @Test
    fun `Multiple agencies - ALLOW any match`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "44", "141"),  // SpaceX, NASA, Blue Origin
            subscribedLocations = setOf("12")
        )
        
        val payload = V5NotificationPayload(
            lspId = "44",   // NASA (subscribed)
            locationId = "11"  // Vandenberg (not subscribed)
            // ... other required fields
        )
        
        val result = V5NotificationFilter.shouldShow(payload, state)
        assertTrue(result.shouldShow())  // Flexible allows agency match
    }
}
```

**Test Helper**:
```kotlin
private fun createTestPayload(
    lspId: String? = "121",
    locationId: String? = "12",
    notificationType: String = "twentyFourHour",
    title: String = "Test Launch",
    launchUuid: String = "550e8400-e29b-41d4-a716-446655440000",
    launchName: String = "Test Launch",
    launchNet: String = "2026-02-18T12:00:00Z"
): V5NotificationPayload {
    return V5NotificationPayload(
        notificationType = notificationType,
        title = title,
        body = "Test body",
        launchUuid = launchUuid,
        launchId = launchUuid,
        launchName = launchName,
        launchImage = null,
        launchNet = launchNet,
        launchLocation = "Test Location",
        webcast = true,
        webcastLive = false,
        lspId = lspId,
        locationId = locationId,
        programId = null,
        statusId = null,
        orbitId = null,
        missionTypeId = null,
        launcherFamilyId = null
    )
}
```

---

### Step 5: Run Tests (15 min)

**Command**:
```bash
./gradlew :composeApp:testDebugUnitTest --tests V5NotificationFilterSimplifiedTest
```

**Expected**: All 7 tests pass ✅

---

### Step 6: Manual Testing (30 min)

**Using Debug Settings Screen**:

1. Open app → Settings → Debug Settings
2. Scroll to "Test V5 Notification Filtering" section
3. Configure test notification:
   - Agency: SpaceX (121)
   - Location: Cape Canaveral (12)
   - Webcast: true
4. Click "Send Test V5 Notification"
5. **Expected**: Notification appears (matches your filters)

6. Change to non-matching:
   - Agency: China (96)
   - Location: Jiuquan (17)
7. Click "Send Test V5 Notification"
8. **Expected**: Notification does NOT appear (blocked by filters)

9. Enable "Follow All Launches"
10. Send China notification again
11. **Expected**: Notification appears (follow all bypasses filters)

---

## Verification Checklist

- [ ] Unit tests pass (`V5NotificationFilterSimplifiedTest`)
- [ ] Integration tests pass (if any exist for notifications)
- [ ] Manual test: SpaceX + Florida → notification shown
- [ ] Manual test: China + Jiuquan → notification blocked
- [ ] Manual test: Follow all → all notifications shown
- [ ] Manual test: Strict matching works correctly
- [ ] Manual test: Flexible matching works correctly
- [ ] Existing V4 notifications still work (no regression)
- [ ] Settings UI still works (no UI changes needed)
- [ ] No crashes or errors in logcat

---

## Rollback Plan

If issues arise:

1. **Revert commits** on `fix_notif_filters` branch:
   ```bash
   git revert HEAD~3..HEAD  # Revert last 3 commits
   ```

2. **Cherry-pick working V4 logic** if needed:
   ```bash
   git cherry-pick <commit-hash-of-v4-filter>
   ```

3. **Restore from V5_SIMPLIFIED_SOLUTION.md** document if logic gets lost

---

## Performance Benchmarks (Optional)

**Before** (Int-based with conversion):
- Filter evaluation: ~2-3ms
- String→Int conversion: ~200ns

**After** (String-based, no conversion):
- Filter evaluation: ~1-2ms (-50%)
- No conversion overhead

**Run benchmark** (if time permits):
```kotlin
@Test
fun benchmarkFilterPerformance() {
    val payload = createTestPayload()
    val state = createTestState()
    
    val startTime = System.nanoTime()
    repeat(10000) {
        V5NotificationFilter.shouldShow(payload, state)
    }
    val endTime = System.nanoTime()
    
    val avgTimeMs = (endTime - startTime) / 10000.0 / 1_000_000
    println("Average filter time: ${avgTimeMs}ms")
    assertTrue(avgTimeMs < 0.01)  // <10μs per filter (target: <10ms but we're way faster)
}
```

---

## Troubleshooting

### Issue: Tests fail with "Unresolved reference: NotificationState"
**Solution**: Run `./gradlew clean` and rebuild. Ensure you're in `commonTest` source set.

### Issue: Notification still showing for non-matching launches
**Solution**: 
1. Check logcat for filter evaluation logs
2. Verify `subscribedAgencies` and `subscribedLocations` in storage
3. Ensure NotificationWorker is calling the updated filter

### Issue: All notifications blocked unexpectedly
**Solution**: Check if `followAllLaunches` is false AND both filter sets are empty. UI should prevent this, but check DataStore state:
```kotlin
// In debug screen or log
log.d { "State: $state" }
```

---

## Next Steps After Completion

1. **Create PR** with conventional commit message:
   ```
   fix(notifications): simplify V5 filter to use String IDs
   
   Fixes bug where users with custom filters receive all notifications.
   Changes V5 to reuse NotificationState (String-based) instead of
   V5FilterPreferences (Int-based), reducing code by ~180 lines.
   
   Test: SpaceX+Florida filter now blocks China/EU launches correctly
   ```

2. **Wait for CI/CD** to run tests and build APK

3. **Deploy to Firebase Distribution** (automatic on merge to master)

4. **Monitor Datadog** for filter block/allow metrics

5. **Update docs** (V5_SIMPLIFIED_SOLUTION.md already exists)

---

## Estimated Timeline

| Task | Time | Progress Checkpoint |
|------|------|---------------------|
| Modify V5NotificationPayload | 10 min | Fields changed, parsing updated |
| Simplify V5NotificationFilter | 30 min | Logic simplified, compiles |
| Update NotificationWorker | 5 min | Calls new signature |
| Add Test Suite | 45 min | 7 tests written and pass |
| Manual Testing | 30 min | Both scenarios verified |
| Code Review & Cleanup | 20 min | Clean code, logs, comments |
| **Total** | **2h 20m** | **Ready for PR** |

---

## Success Indicators

✅ User with "SpaceX + Florida" subscription receives ONLY matching notifications  
✅ All 7 core test cases pass  
✅ No regression in V4 notification behavior  
✅ Code reduced by ~180 lines  
✅ Filter performance <10ms (target, actual ~1-2ms)  
✅ No user migration required  
✅ Settings UI works without changes
