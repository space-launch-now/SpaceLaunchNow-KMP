# Widget Update Reliability - Testing Guide

## Quick Test

### Prerequisites
1. Have widgets added to home screen (NextUpWidget and/or LaunchListWidget)
2. Have access to logcat to view debug logs
3. Have a test premium account OR debug mode enabled

---

## Test 1: Premium Access Grant (Widget Unlock)

### Setup
- Start with free account
- Widgets should show locked state

### Steps
1. Open the app
2. Navigate to Settings → Subscription
3. Purchase premium (or simulate with debug tools)
4. **Immediately** press home button to view widgets
5. Wait up to 1 second

### Expected Result
✅ Widgets refresh and show **unlocked content** (launches visible)

### Logs to Check
```
SubscriptionRepository: User has premium entitlement - granting ADVANCED_WIDGETS
SubscriptionRepository: Widget access granted - scheduling update in 750ms
SubscriptionRepository: Triggering widget update after access granted
WidgetUpdater: Starting updateAllWidgets()
WidgetUpdater: All widget updates completed successfully
```

### If Widgets Don't Update
1. Check if widgets are actually on home screen: `adb shell dumpsys appwidget | grep SpaceLaunchNow`
2. Check logcat for errors
3. Manually trigger update: Long-press widget → "Update"
4. Report bug with logs

---

## Test 2: Premium Access Revoke (Widget Lock)

### Setup
- Start with premium account
- Widgets should show unlocked state

### Steps
1. Use debug settings to simulate expired subscription
2. Trigger subscription verification (restart app)
3. Press home button to view widgets
4. Wait up to 1 second

### Expected Result
✅ Widgets refresh and show **locked state** with lock icon

### Logs to Check
```
SubscriptionRepository: No premium entitlement
SubscriptionRepository: Widget access revoked - scheduling update in 750ms
SubscriptionRepository: Triggering widget update after access revoked
WidgetUpdater: Starting updateAllWidgets()
```

---

## Test 3: Widget Appearance Changes

### Setup
- Have premium access
- Widgets on home screen

### Steps
1. Open app → Settings → Theme Settings
2. Scroll to "Widget Appearance" section
3. Change "Widget Theme" to "Dynamic Colors"
4. Adjust "Background Transparency" to 50%
5. Adjust "Corner Radius" to 24dp
6. Click **"Apply"** button
7. Press home button immediately
8. Wait up to 1 second

### Expected Result
✅ Widgets refresh with new appearance:
- Colors match wallpaper (Material You)
- Background is semi-transparent
- Corners are more rounded

### Logs to Check
```
ThemeCustomizationViewModel: applyWidgetChanges() called
  Theme Source: DYNAMIC_COLORS
  Alpha: 0.5
  Radius: 24
WidgetUpdateSideEffect: Apply trigger detected (counter: 1)
WidgetUpdateSideEffect: Waiting 750ms for DataStore writes to complete...
WidgetUpdater: Starting updateAllWidgets()
WidgetUpdater: Found X NextUpWidget instances
WidgetUpdater: All widget updates completed successfully
```

---

## Test 4: Multiple Rapid Changes

### Purpose
Verify that only the final state is shown (no flickering)

### Steps
1. Open Settings → Theme Settings → Widget Appearance
2. Rapidly change settings:
   - Theme: Follow App Theme
   - Alpha: 100%
   - Theme: Follow System
   - Alpha: 25%
   - Theme: Dynamic Colors
   - Alpha: 75%
   - Radius: 0dp
   - Radius: 48dp
   - Radius: 16dp
3. Click "Apply" once
4. View widgets

### Expected Result
✅ Widget shows only the **final state**:
- Theme: Dynamic Colors
- Alpha: 75%
- Radius: 16dp

✅ No intermediate states visible (no flickering)

---

## Test 5: No Widgets on Home Screen

### Purpose
Verify graceful handling when no widgets exist

### Steps
1. Remove all SpaceLaunchNow widgets from home screen
2. Make any widget appearance change
3. Click "Apply"
4. Check logs

### Expected Result
✅ No crash or error
✅ Log message: "WARNING - No widgets found on home screen!"

### Logs to Check
```
WidgetUpdater: Starting updateAllWidgets()
WidgetUpdater: Found 0 NextUpWidget instances on home screen
WidgetUpdater: Found 0 LaunchListWidget instances on home screen
WidgetUpdater: WARNING - No widgets found on home screen! User needs to add widgets first.
```

---

## Test 6: Offline/Cached State

### Purpose
Verify widgets work offline with cached data

### Steps
1. Enable premium access while online
2. Let widgets update successfully
3. Enable airplane mode
4. Restart app (to clear in-memory cache)
5. Check widgets on home screen

### Expected Result
✅ Widgets still show unlocked content (using cached `widgetAccessGranted` from DataStore)
✅ Log shows: "cached access for ADVANCED_WIDGETS: true"

---

## Test 7: Device Reboot

### Purpose
Verify widget state persists across reboots

### Steps
1. Configure widgets with custom appearance
2. Ensure premium access granted
3. Reboot device
4. Check widgets immediately after boot

### Expected Result
✅ Widgets show correct appearance (theme, alpha, radius)
✅ Widgets show correct access state (locked/unlocked)
✅ No need to open app first

---

## Debugging Commands

### View Widget IDs
```bash
adb shell dumpsys appwidget | grep -A 20 "me.calebjones.spacelaunchnow"
```

### Force Widget Update
```bash
adb shell am broadcast -a android.appwidget.action.APPWIDGET_UPDATE
```

### View Real-time Logs
```bash
adb logcat | grep -E "WidgetUpdater|SubscriptionRepository|WidgetUpdateSideEffect"
```

### Clear App Data (Reset State)
```bash
adb shell pm clear me.calebjones.spacelaunchnow.debug
```

---

## Performance Metrics

### Expected Timings
- Widget access change → Widget refresh: **< 1 second**
- Appearance change → Widget refresh: **< 1 second**
- DataStore write completion: **< 750ms**

### What's Normal
- ✅ 750ms delay before update (by design)
- ✅ Brief flash during widget redraw
- ✅ Widgets update one at a time (multiple widget instances)

### What's NOT Normal
- ❌ Widgets never update (> 5 seconds)
- ❌ App crashes when applying changes
- ❌ Widgets show wrong state persistently
- ❌ Excessive battery drain

---

## Troubleshooting

### Widgets Don't Update After Premium Purchase
**Possible Causes**:
1. No widgets on home screen → Add widget first
2. RevenueCat sync delay → Wait 5 seconds, restart app
3. DataStore write failed → Check storage permissions
4. PlatformWidgetUpdater not injected → Check Koin logs

**Solution**: Check logs for "Widget access granted" message. If missing, premium entitlement check failed.

### Widgets Update Too Slowly
**Possible Causes**:
1. Slow device (old Android version)
2. DataStore writes taking longer than 750ms
3. Many widgets on home screen (sequential updates)

**Solution**: Increase delay to 1000ms in `AndroidWidgetUpdateSideEffect.kt` if needed.

### Widgets Show Wrong Appearance
**Possible Causes**:
1. DataStore write incomplete before widget read
2. Widget cached old values
3. Multiple rapid "Apply" clicks

**Solution**: 
- Ensure only one "Apply" click
- Check DataStore values: `adb shell run-as <package> cat /data/data/<package>/files/datastore/app_settings.preferences_pb`

---

## Success Criteria

✅ All 7 tests pass  
✅ Widgets update within 1 second  
✅ No crashes or errors in logs  
✅ Widget state persists across app restarts  
✅ Widget state persists across device reboots  
✅ Premium access changes reflected in widgets  
✅ Appearance changes reflected in widgets  

---

## Regression Checklist

Before marking this feature as complete, verify:

- [ ] Free users see locked widgets
- [ ] Premium users see unlocked widgets
- [ ] Widget appearance changes work
- [ ] Multiple widgets update simultaneously
- [ ] No battery drain from widget updates
- [ ] No crashes when no widgets on home screen
- [ ] Widgets work offline with cached state
- [ ] Widget state persists after reboot
- [ ] Debug logs are comprehensive
- [ ] iOS/Desktop builds unaffected (no compilation errors)
