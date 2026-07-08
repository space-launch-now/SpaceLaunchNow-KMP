# iOS Notification Re-Alert Policy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make collapsing iOS notifications buzz only for high-value launch phases (oneMinute, liftoff, success, webcast-live) and update silently for the rest, matching the agreed re-alert policy.

**Architecture:** Add one shared, pure `NotificationAlertPolicy` enum (member of both the app and NSE targets, exactly like `V5NotificationData.swift`). Three display sites consult it: the NSE (primary, killed/background path), `willPresent` (foreground), and `displayNotification` (local-reschedule path). The policy decides sound + interruption level from `notification_type`; the notification is always displayed/updated regardless.

**Tech Stack:** Swift, `UserNotifications` (`UNMutableNotificationContent`, `UNNotificationPresentationOptions`, `interruptionLevel`). Built with Xcode (iosApp scheme builds the app + embedded NotificationServiceExtension).

**Spec:** `docs/superpowers/specs/2026-06-20-ios-notification-realert-policy-design.md`
**Companion (backend):** `SpaceLaunchNow-Server/docs/superpowers/specs/2026-06-20-ios-apns-collapse-id-design.md` — independent; collapse only becomes visible once it ships.

## Global Constraints

- **Silent (sound = nil, interruptionLevel = .passive):** `twentyFourHour`, `oneHour`, `tenMinutes`, `netstampChanged`, `failure`, `partialFailure`.
- **Re-alert (sound = .default, interruptionLevel = .active):** everything else — `oneMinute`, `inFlight`, `success`, `webcastLive`, `webcastOnly`, broadcasts (`custom`/`event`/`news`), and any unknown/`nil` type (safe default).
- Matching is **case-insensitive**; default for unknown/`nil` is **re-alert**.
- The shared file MUST be a member of BOTH the `iosApp` and `NotificationServiceExtension` targets (precedent: `iosApp/iosApp/V5NotificationData.swift`).
- Do NOT change filtering, history, deep-linking, channels, topics, or the test/debug send helpers.
- No new third-party dependencies; `UserNotifications` only.
- No Swift unit-test target exists for the app/NSE (only `LaunchWidgetTests`, unrelated). Per the spec, do NOT add a test target solely for this — gate on a clean build + the pure-logic reasoning check + device verification.
- Build verification (primary): in Xcode select the **iosApp** scheme and Build (⌘B) — this compiles the app and the embedded NSE. CLI alternative (needs secrets via `./scripts/generate-ios-secrets.sh` first): `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -sdk iphonesimulator -configuration Debug build`.
- Commit style: Conventional Commits. Do NOT add a Claude co-author (per CLAUDE.md).

---

### Task 1: Create the shared `NotificationAlertPolicy`

**Files:**
- Create: `iosApp/iosApp/NotificationAlertPolicy.swift`
- Modify (Xcode project): add the new file to BOTH targets — `iosApp` and `NotificationServiceExtension`.

**Interfaces:**
- Produces (consumed by Tasks 2-3):
  - `NotificationAlertPolicy.applySound(to content: UNMutableNotificationContent, notificationType: String?)` — sets `content.sound` and (iOS 15+) `content.interruptionLevel`.
  - `NotificationAlertPolicy.foregroundOptions(notificationType: String?) -> UNNotificationPresentationOptions` — `[.banner, .badge]` plus `.sound` when re-alerting.
  - `NotificationAlertPolicy.shouldReAlert(notificationType: String?) -> Bool` — pure predicate.

- [ ] **Step 1: Create the file**

Create `iosApp/iosApp/NotificationAlertPolicy.swift`:

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

- [ ] **Step 2: Add the file to both targets**

In Xcode, select `NotificationAlertPolicy.swift` → File Inspector → **Target Membership** → check both **iosApp** and **NotificationServiceExtension**. (If adding via drag, tick both targets in the dialog.) Verify it matches `V5NotificationData.swift`'s membership.

- [ ] **Step 3: Build to verify it compiles in both targets**

Build the **iosApp** scheme (⌘B). Expected: BUILD SUCCEEDED, with no "cannot find 'NotificationAlertPolicy' in scope" — confirms the NSE target also sees the file.

- [ ] **Step 4: Pure-logic reasoning check**

Confirm against the Global Constraints table (the function has no test home, so verify by reading):
- `shouldReAlert("tenMinutes")`, `("oneHour")`, `("twentyFourHour")`, `("netstampChanged")`, `("failure")`, `("partialFailure")` → `false`.
- `shouldReAlert("oneMinute")`, `("inFlight")`, `("success")`, `("webcastLive")`, `("webcastOnly")`, `("custom")`, `(nil)`, `("anything-else")` → `true`.
- Case-insensitive: `shouldReAlert("TENMINUTES")` → `false`.

- [ ] **Step 5: Commit**

```bash
git add iosApp/iosApp/NotificationAlertPolicy.swift iosApp/iosApp.xcodeproj/project.pbxproj
git commit -m "feat(ios): add NotificationAlertPolicy for phase-aware re-alert"
```

---

### Task 2: Apply the policy in the Notification Service Extension

**Files:**
- Modify: `iosApp/NotificationServiceExtension/NotificationService.swift` (V5 launch branch, the `bestAttemptContent.sound = .default` line ~111)

**Interfaces:**
- Consumes: `NotificationAlertPolicy.applySound(to:notificationType:)` (Task 1). `notificationType` is already in scope (assigned ~line 88).

- [ ] **Step 1: Replace the unconditional sound in the V5 launch branch**

In `iosApp/NotificationServiceExtension/NotificationService.swift`, find (inside `if isV5, let v5Data = ...`):

```swift
            bestAttemptContent.title = v5Data.title
            bestAttemptContent.body = v5Data.body
            bestAttemptContent.sound = .default
```

Replace with:

```swift
            bestAttemptContent.title = v5Data.title
            bestAttemptContent.body = v5Data.body
            NotificationAlertPolicy.applySound(to: bestAttemptContent, notificationType: notificationType)
```

Leave the non-V5 broadcast `else` branch unchanged (broadcasts default to re-alert).

- [ ] **Step 2: Build to verify it compiles**

Build the **iosApp** scheme (⌘B). Expected: BUILD SUCCEEDED.

- [ ] **Step 3: Commit**

```bash
git add iosApp/NotificationServiceExtension/NotificationService.swift
git commit -m "feat(ios): apply re-alert policy to launch notifications in the NSE"
```

---

### Task 3: Apply the policy in AppDelegate (foreground + local reschedule)

**Files:**
- Modify: `iosApp/iosApp/AppDelegate.swift` — `willPresent` parsed-launch branch (~lines 396-400) and `displayNotification` sound (~lines 535-539)

**Interfaces:**
- Consumes: `NotificationAlertPolicy.foregroundOptions(notificationType:)` and `NotificationAlertPolicy.applySound(to:notificationType:)` (Task 1).

- [ ] **Step 1: Replace the foreground launch presentation options**

In `iosApp/iosApp/AppDelegate.swift`, inside `userNotificationCenter(_:willPresent:...)`, in the parsed-launch `if shouldShow {` block (the one right after the comment `// Show notification even when app is in foreground`), find:

```swift
                // Show notification even when app is in foreground
                if #available(iOS 14.0, *) {
                    completionHandler([.banner, .badge, .sound])
                } else {
                    completionHandler([.alert, .badge, .sound])
                }
```

Replace with:

```swift
                // Show notification even when app is in foreground; sound per re-alert policy
                completionHandler(
                    NotificationAlertPolicy.foregroundOptions(notificationType: notificationData.notificationType)
                )
```

> ⚠️ There is a visually identical `if #available... completionHandler([.banner, .badge, .sound])` block earlier in the same method, inside the **broadcast** (`isCustomNotification || isNewsNotification || isEventNotification`) branch. Do NOT change that one — broadcasts re-alert. Only change the block under `// Show notification even when app is in foreground`, which uses `notificationData`.

- [ ] **Step 2: Replace the local-reschedule sound in `displayNotification`**

In the same file, in `displayNotification(data:userInfo:)`, find:

```swift
        // Add badge
        content.badge = 1

        // Add sound
        content.sound = .default
```

Replace with:

```swift
        // Add badge
        content.badge = 1

        // Sound + prominence per re-alert policy
        NotificationAlertPolicy.applySound(to: content, notificationType: data.notificationType)
```

`scheduleNotification(content:identifier:)` is unchanged — it already keys on `data.launchUuid`, which equals the backend `apns-collapse-id` for launches.

- [ ] **Step 3: Build to verify it compiles**

Build the **iosApp** scheme (⌘B). Expected: BUILD SUCCEEDED.

- [ ] **Step 4: Commit**

```bash
git add iosApp/iosApp/AppDelegate.swift
git commit -m "feat(ios): apply re-alert policy in foreground and local notification paths"
```

---

### Task 4: Device verification

No code change — confirm real-device behavior. Requires the backend `apns-collapse-id` to be live for the collapse (steps 1-2) to be visible; the sound policy (steps 3-4) is observable without it.

- [ ] **Step 1: Silent reminder replace (killed app).** Force-quit the app. Trigger `oneHour` then `tenMinutes` for the same launch. Expected: a single notification updated to the `tenMinutes` text, with **no second buzz**.

- [ ] **Step 2: High-value re-alert (killed app).** Continue the same launch with `oneMinute`, then `inFlight`. Expected: the notification updates in place **and buzzes** each time.

- [ ] **Step 3: Failure is silent.** Trigger `failure` (or `partialFailure`) for a launch with a notification already showing. Expected: content updates, **no buzz**.

- [ ] **Step 4: Webcast-live re-alerts.** Trigger `webcastLive`. Expected: **buzzes**.

- [ ] **Step 5: Foreground.** Repeat steps 1-2 with the app foregrounded; sound follows the same policy (silent reminders, audible high-value).

- [ ] **Step 6: Broadcasts unaffected.** Trigger a `custom`/`event`/`news` notification. Expected: **buzzes** (default re-alert).

The in-app iOS debug/test send helpers set `.default` sound themselves, so use real backend sends (or temporarily route a helper through `NotificationAlertPolicy.applySound`) when verifying the silent cases.

## Self-review notes

- Spec coverage: shared policy (Task 1), NSE (Task 2), `willPresent` + `displayNotification` (Task 3), device matrix incl. resolved webcast/failure decisions (Task 4). ✔
- Type consistency: `applySound(to:notificationType:)`, `foregroundOptions(notificationType:)`, `shouldReAlert(notificationType:)` used identically in Tasks 2-3 as defined in Task 1. ✔
- Ambiguity guard: Task 3 explicitly calls out the duplicate `completionHandler([.banner,.badge,.sound])` block to avoid editing the broadcast branch. ✔
- `silentTypes` matches the Global Constraints table exactly (webcast NOT silent; failure/partialFailure silent). ✔
