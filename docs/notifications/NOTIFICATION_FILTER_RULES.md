# Notification Filter Rules v4

## Overview
The NotificationFilter v4 system provides client-side filtering for launch notifications based on user preferences. This document outlines the complete filtering logic.

## Filter Criteria

### 1. Global Enable/Disable
- If `enableNotifications = false`: Block ALL notifications
- Otherwise: Continue to next checks

### 2. Webcast-Only Filter
- If `WEBCAST_ONLY` topic is enabled AND launch has no webcast: Block
- Otherwise: Continue to next checks

### 3. Notification Type (Timing) Filter
- Check if the notification type (e.g., "oneHour", "tenMinutes") is enabled
- Types: netstampChanged, twentyFourHour, oneHour, tenMinutes, oneMinute, inFlight, success, event
- If disabled: Block
- Otherwise: Continue to next checks

### 4. Follow All Launches
- If `followAllLaunches = true`: **Allow ALL notifications** (skip agency/location filtering)
- This bypasses all remaining filters
- Otherwise: Continue to agency/location filtering

## Agency & Location Filtering Logic

### Empty Set Behavior (IMPORTANT!)
- **Empty agency set** = Don't filter by agency (match ALL agencies)
- **Empty location set** = Don't filter by location (match ALL locations)
- **Both empty** = Block (must have at least one filter criterion)

### Location ID "0" (Other) - Wildcard Behavior
- LocationId "0" represents "Other/Unknown" locations
- **If user subscribes to locationId="0"**: Matches **ANY** locationId (wildcard)
- Examples:
  - Subscribed to "0" → Matches launches from KSC (27), Vandenberg (11), Mahia (15), etc.
  - This allows users to get notifications for launches from unknown/unspecified locations

### Matching Modes

#### Flexible Matching (`useStrictMatching = false`)
**Logic**: Agency Match **OR** Location Match

**Examples**:
```
✅ NASA launches from KSC (agency=44, location=27)
   Subscribed: agencies=[44], locations=[11]
   Result: ALLOW (agency matches, location doesn't - OK in flexible mode)

✅ SpaceX launches from Vandenberg (agency=121, location=11)
   Subscribed: agencies=[44], locations=[11]
   Result: ALLOW (location matches, agency doesn't - OK in flexible mode)

❌ Blue Origin launches from Texas (agency=141, location=16)
   Subscribed: agencies=[44], locations=[11]
   Result: BLOCK (neither agency nor location matches)

✅ NASA launches ONLY (agency=44)
   Subscribed: agencies=[44], locations=[]
   Result: ALLOW (empty location set = match all locations, agency matches)

✅ KSC launches ONLY (location=27)
   Subscribed: agencies=[], locations=[27]
   Result: ALLOW (empty agency set = match all agencies, location matches)
```

#### Strict Matching (`useStrictMatching = true`)
**Logic**: Agency Match **AND** Location Match

**Examples**:
```
✅ NASA launches from Vandenberg (agency=44, location=11)
   Subscribed: agencies=[44], locations=[11]
   Result: ALLOW (both agency AND location match)

❌ NASA launches from KSC (agency=44, location=27)
   Subscribed: agencies=[44], locations=[11]
   Result: BLOCK (agency matches but location doesn't - need BOTH)

❌ NASA launches ONLY (agency=44)
   Subscribed: agencies=[44], locations=[]
   Result: BLOCK (strict mode with empty location set = no valid filter)

❌ KSC launches ONLY (location=27)
   Subscribed: agencies=[], locations=[27]
   Result: BLOCK (strict mode with empty agency set = no valid filter)
```

### Special Case: "Other" Location with Multiple Filters

```
✅ Rocket Lab from Mahia (agency=147, location=15)
   Subscribed: agencies=[], locations=[0, 27]  // "Other" + KSC
   Result: ALLOW (location "0" is a wildcard, matches locationId=15)

✅ SpaceX from KSC (agency=121, location=27)
   Subscribed: agencies=[], locations=[0, 27]  // "Other" + KSC
   Result: ALLOW (location 27 matches directly)
```

## Complete Decision Tree

```
1. Is enableNotifications = true?
   NO → BLOCK
   YES ↓

2. Is webcastOnly enabled AND launch has no webcast?
   YES → BLOCK
   NO ↓

3. Is notification type enabled in topicSettings?
   NO → BLOCK
   YES ↓

4. Is followAllLaunches = true?
   YES → ALLOW (skip all agency/location filtering)
   NO ↓

5. Are both subscribedAgencies AND subscribedLocations empty?
   YES → BLOCK (need at least one filter)
   NO ↓

6. Calculate matches:
   - agencyMatch = agencies.isEmpty() OR agencies.contains(agencyId)
   - locationMatch = locations.isEmpty() OR locations.contains(locationId) OR locations.contains("0")

7. Apply matching mode:
   Flexible: agencyMatch OR locationMatch → ALLOW
   Strict: agencyMatch AND locationMatch → ALLOW
   Otherwise → BLOCK
```

## Test Coverage

See `NotificationFilterTest.kt` for 40+ comprehensive tests covering:
- Follow all launches bypass
- Webcast-only filtering
- Notification type filtering
- Flexible matching (OR logic)
- Strict matching (AND logic)
- LocationId="0" edge cases
- **LocationId="0" as wildcard** (NEW)
- **Agency-only subscriptions** (NEW)
- **Location-only subscriptions** (NEW)
- Empty set handling

## Migration Notes

### Changes in v4.0.0-b21+
1. **LocationId="0" now acts as wildcard**: Subscribing to "Other" location matches ANY locationId
2. **Empty sets treated as "don't filter"**: 
   - Empty agency set → Show launches from all agencies
   - Empty location set → Show launches from all locations
3. **Strict mode with empty sets**: Blocks all (need both filters in strict mode)

### Breaking Changes
- Previous behavior: LocationId="0" was treated as a literal match (only matched launches with locationId="0")
- New behavior: LocationId="0" is a wildcard (matches ALL locationIds)
- Migration: Users who specifically wanted only "Other" launches will now get all launches
  - Workaround: Use agency filtering to narrow down results

## UI Recommendations

### Settings Screen
- Show warning when both agency and location lists are empty
- Explain "Other" location as wildcard: "Subscribe to launches from any location"
- Clarify flexible vs strict modes:
  - Flexible: "Show launches matching ANY of my preferences"
  - Strict: "Show launches matching ALL of my preferences"

### Notification Type Settings
- Group by timing: "When should I be notified?"
- Webcast-only as special filter: "Only launches with webcasts"

## Performance Notes
- All filtering happens client-side (no server calls)
- O(1) set lookups for agency/location checks
- Minimal overhead on notification reception
- Location "0" wildcard adds one extra contains() check (negligible)
