# iOS Notification Re-Alert Policy — KMP (Client)

**Date:** 2026-06-20
**Status:** Draft
**Repo:** SpaceLaunchNow-KMP-Main (iOS app + Notification Service Extension)
**Companion spec (backend):** `SpaceLaunchNow-Server/docs/superpowers/specs/2026-06-20-ios-apns-collapse-id-design.md`

## Problem

The backend spec adds `apns-collapse-id` so iOS launch notifications **collapse** — a later phase
replaces the earlier one instead of stacking. But with a collapse-id alone, **every** replacement
re-alerts (sound + banner). On Android the equivalent updates are silent (`setOnlyAlertOnce(true)`),
so a `tenMinutes` update doesn't re-buzz over a still-visible `oneHour` reminder.

We do **not** want a literal silent-on-every-replace match. The decision (made during brainstorming)
is a middle ground: **only the high-value phases re-alert**; the routine reminder phases update
silently. This keeps the notification always-current and quiet for reminders, while still buzzing
for the moments that matter (one minute out, liftoff, outcome).

This is a pure client concern. `apns-collapse-id` (backend) controls *which* notification is
replaced; this spec controls *whether the replacement plays sound*. The two are orthogonal and ship
in any order. With only the backend shipped, collapse works but every phase buzzes (acceptable).
With only this shipped, sound changes but notifications don't yet collapse.

## Alert policy

Decision per `notification_type` (case-insensitive). Default for anything unlisted is **re-alert**,
so broadcasts (custom/event/news) and unknown types are never accidentally silenced.

| Phase (`notification_type`)                         | Behavior on display/replace |
|-----------------------------------------------------|-----------------------------|
| `twentyFourHour`, `oneHour`, `tenMinutes`           | **Silent** (reminder)       |
| `netstampChanged`                                   | **Silent** (schedule change)|
| `failure`, `partialFailure`                          | **Silent** (terminal outcome, no buzz) |
| `oneMinute`                                          | **Re-alert**                |
| `inFlight` (liftoff)                                 | **Re-alert**                |
| `success`                                            | **Re-alert**                |
| `webcastLive`, `webcastOnly`                         | **Re-alert** (watch now)    |
| broadcasts (`custom`, `event`, `news`) / unknown    | **Re-alert** (default)      |

"Silent" = `sound = nil` and (iOS 15+) `interruptionLevel = .passive`. "Re-alert" = `sound = .default`
and the system default interruption level (`.active`). The notification is **always displayed and its
content always updated** either way — silent only removes the sound/prominence, never the update.

### Resolved decisions

- **Webcast going live** (`webcastLive`/`webcastOnly`) → **Re-alert**. A webcast going live is
  high-value (open and watch now), so it buzzes.
- **`failure` / `partialFailure`** → **Silent**. Only the positive/imminent phases buzz; failures
  update the notification quietly.

## Architecture

### The two iOS display paths (both must honor the policy)

1. **NSE — `NotificationService.didReceive`** (`NotificationServiceExtension/NotificationService.swift`).
   The **primary, reliable** path: it renders launch notifications when the app is backgrounded or
   killed. Today it sets `bestAttemptContent.sound = .default` unconditionally for V5 launches
   (line 111). This is where the policy matters most.
2. **App foreground — `AppDelegate.userNotificationCenter(_:willPresent:)`**
   (`iosApp/AppDelegate.swift`). When the app is foregrounded, presentation options (including
   `.sound`) are chosen here (the launch branch returns `[.banner, .badge, .sound]`, ~line 396).
3. **App local-reschedule — `AppDelegate.displayNotification` → `scheduleNotification`**
   (`iosApp/AppDelegate.swift`, sound at line 539). This path only runs for `content-available`
   pushes; current V5 iOS launch pushes are alert+`mutable-content` (no `content-available`), so it is
   effectively dormant for production launches but is updated for consistency and any future
   data-only delivery. **`scheduleNotification` already uses `identifier: data.launchUuid`** (line
   578), which equals the backend `apns-collapse-id` for launches — so the client collapse key is
   already consistent; **no identifier change is needed.**

Test helpers (`NotificationTestHelper.swift`, `NotificationDebugHelper.swift`) intentionally always
buzz and are **out of scope**.

### Files changed

- **Create:** `iosApp/iosApp/NotificationAlertPolicy.swift` — the single source of truth for the
  policy. **Add it to BOTH the `iosApp` and `NotificationServiceExtension` targets** (precedent:
  `iosApp/iosApp/V5NotificationData.swift` is already a member of both targets — mirror its target
  membership in the Xcode project).
- **Modify:** `iosApp/NotificationServiceExtension/NotificationService.swift` — replace the
  unconditional `sound = .default` (line 111) with policy-driven sound + interruption level.
- **Modify:** `iosApp/iosApp/AppDelegate.swift` — policy-driven `.sound` option in `willPresent`
  (launch branch, ~line 396) and policy-driven `content.sound`/`interruptionLevel` in
  `displayNotification` (line 539).

### `NotificationAlertPolicy.swift` (complete)

```swift
import UserNotifications

/// Single source of truth for whether a notification phase re-alerts (sound + prominence) or
/// updates silently. Used by both the app target and the Notification Service Extension, so it
/// MUST be a member of both targets (see V5NotificationData.swift for the same dual-target setup).
///
/// Collapse itself is driven server-side by `apns-collapse-id`; this only decides whether the
/// collapsing replacement plays a sound.
enum NotificationAlertPolicy {

    /// Launch phases that update silently when they replace an existing notification.
    /// Everything not in this set re-alerts (high-value launch phases AND all broadcasts/unknown).
    private static let silentTypes: Set<String> = [
        "twentyfourhour",
        "onehour",
        "tenminutes",
        "netstampchanged",
        "failure",
        "partialfailure",
    ]

    /// True if this notification type should play sound and use default prominence.
    static func shouldReAlert(notificationType: String?) -> Bool {
        guard let type = notificationType?.lowercased() else { return true }
        return !silentTypes.contains(type)
    }

    /// Apply the policy to a mutable notification content (sets sound + interruption level).
    static func applySound(to content: UNMutableNotificationContent, notificationType: String?) {
        if shouldReAlert(notificationType: notificationType) {
            content.sound = .default
            if #available(iOS 15.0, *) { content.interruptionLevel = .active }
        } else {
            content.sound = nil
            if #available(iOS 15.0, *) { content.interruptionLevel = .passive }
        }
    }

    /// Presentation options for a foreground launch notification, honoring the policy.
    static func foregroundOptions(notificationType: String?) -> UNNotificationPresentationOptions {
        var options: UNNotificationPresentationOptions = [.banner, .badge]
        if shouldReAlert(notificationType: notificationType) { options.insert(.sound) }
        return options
    }
}
```

### NSE change (`NotificationService.swift`, V5 launch branch ~line 109–111)

Replace:

```swift
bestAttemptContent.title = v5Data.title
bestAttemptContent.body = v5Data.body
bestAttemptContent.sound = .default
```

with:

```swift
bestAttemptContent.title = v5Data.title
bestAttemptContent.body = v5Data.body
NotificationAlertPolicy.applySound(to: bestAttemptContent, notificationType: notificationType)
```

(`notificationType` is already in scope at this point — line 88.) Broadcasts (the `else` branch) are
left unchanged: they are one-shot and default to re-alert.

### App foreground change (`AppDelegate.swift`, `willPresent` launch branch ~line 396)

Replace the hard-coded options in the parsed-launch `shouldShow` branch:

```swift
if #available(iOS 14.0, *) {
    completionHandler([.banner, .badge, .sound])
} else {
    completionHandler([.alert, .badge, .sound])
}
```

with policy-driven options:

```swift
completionHandler(
    NotificationAlertPolicy.foregroundOptions(notificationType: notificationData.notificationType)
)
```

`foregroundOptions` returns `[.banner, .badge]` (+ `.sound` when re-alerting). `.banner` requires iOS
14+, which is the app's floor, so the `.alert` fallback branch is no longer needed here.

### App local-reschedule change (`AppDelegate.swift`, `displayNotification` ~line 535–539)

Replace:

```swift
// Add badge
content.badge = 1

// Add sound
content.sound = .default
```

with:

```swift
// Add badge
content.badge = 1

// Sound + prominence per re-alert policy
NotificationAlertPolicy.applySound(to: content, notificationType: data.notificationType)
```

`scheduleNotification(content:identifier:)` is unchanged — it already keys on `data.launchUuid`.

## Testing & verification

No unit-test harness exists for `UNNotification` delivery in this repo, and the `NotificationAlertPolicy`
logic is pure, so split verification:

1. **Pure-logic checks (cheap, no device).** If/when a Swift test target exists, assert
   `NotificationAlertPolicy.shouldReAlert` for representative inputs: `"tenMinutes"`/`"oneHour"`/
   `"twentyFourHour"`/`"netstampChanged"`/`"failure"`/`"partialFailure"` → `false`; `"oneMinute"`/
   `"inFlight"`/`"success"`/`"webcastLive"`/`"webcastOnly"`/`"custom"`/`nil`/`"garbage"` → `true`. The
   function is case-insensitive, so also check `"TENMINUTES"`. If no test target exists, this is a manual
   reasoning check against the table above (do not add a test target solely for this).
2. **Device — silent replace.** App force-quit. Trigger `oneHour` then `tenMinutes` for the same
   launch (requires the backend collapse-id shipped). Expected: single notification, updated to
   `tenMinutes`, **no second buzz**.
3. **Device — high-value re-alert.** Continue the same launch with `oneMinute`, then `inFlight`.
   Expected: the notification updates in place **and buzzes** each time.
4. **Device — foreground.** Repeat 2–3 with the app foregrounded; confirm sound follows the same
   policy (silent reminders, audible high-value).
5. **Device — broadcasts unaffected.** A `custom`/`event`/`news` notification still buzzes.

The in-app iOS debug/test send helpers are convenient triggers; note they set `.default` sound
themselves, so use real backend sends (or temporarily route them through the policy) when verifying
the silent cases.

## Dependencies & sequencing

- **Independent of the backend spec for compilation/merge.** This can land first; it simply changes
  which phases buzz. Collapse (the visible "replace instead of stack") only appears once the backend
  `apns-collapse-id` ships, so end-to-end device verification of steps 2–3 requires both.
- **No new third-party dependencies.** Uses `UserNotifications` only.

## Risks & mitigations

- **Shared file not added to the NSE target → build failure or `NotificationAlertPolicy` unresolved in
  the extension.** Mitigation: the create-file task explicitly sets membership on both targets and
  cites `V5NotificationData.swift` as the working precedent; verify the NSE target compiles.
- **A reminder type missing from `silentTypes` → it keeps buzzing.** Low impact (reverts to today's
  behavior for that type); the table is the single place to fix, and the pure-logic checks catch it.
- **Over-silencing a high-value phase** (e.g. if `inFlight` were added to the set by mistake) →
  missed alert. Mitigation: default is re-alert and the high-value phases are deliberately **absent**
  from `silentTypes`; the open-question rows are the only intended movement.

## Non-goals

- The `apns-collapse-id` header itself (backend spec).
- Android behavior (already collapses; `setOnlyAlertOnce` unchanged).
- Notification filtering, history, deep-linking, channels, or delivery topology.
- Test/debug send helpers' always-buzz behavior.
- iOS `timeSensitive`/`critical` interruption levels (would require additional entitlements; `.active`
  is sufficient).
