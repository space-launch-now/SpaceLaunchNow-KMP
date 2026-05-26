# V5 Custom Admin Notifications — KMP App

**Date:** 2026-05-25
**Status:** Approved
**Repo:** SpaceLaunchNow-KMP-Main
**Companion spec:** `SpaceLaunchNow-Server/docs/superpowers/specs/2026-05-25-v5-custom-notifications-server-design.md`
**Depends on:** `2026-05-25-v5-news-notifications-kmp-design.md` (shared detection order,
per-type-toggle filtering infra, and NSE preference bridge)

## Problem

Custom admin notifications are **fully net-new** on the KMP app: no payload parser, no
notification channel, no settings toggle, no tap routing. The server (companion spec) sends a
flat payload marked by `notification_type == "custom"` with a `target_type` /`target_id`
(/`target_url`) describing an optional launch / event / news / no deep-link target.

## Detection order

Custom is checked **first** (see the news-KMP spec's shared detection order) precisely because
a custom notification may carry an `event_id`-like target and must not be mis-detected:

1. **`notification_type == "custom"` → custom** ← this spec
2. `event_id` present → event
3. `article_id` present → news
4. `lsp_id` present → V5 launch
5. else → V4

## Filtering

Custom is a broadcast type: gated by the global kill switch AND a **new** per-type toggle
`NotificationTopic.ANNOUNCEMENTS` (not agency/location). Implemented via the shared per-type-
toggle infrastructure established in the news-KMP spec (settings UI/ViewModel/persistence +
NSE bridge).

## Architecture — model & channel

### New topic

Add `NotificationTopic.ANNOUNCEMENTS` (id `"announcements"`, `defaultEnabled = true`) to
`NotificationState.kt`, and include it in `getUserConfigurableTopics()` so it persists in the
`topic_settings` JSON.

### New channel

Add `SpaceLaunchNotificationChannel.ANNOUNCEMENTS` (id `"announcements_default"`, default
importance) to `NotificationChannel.kt`, mapped from `notification_type == "custom"` in
`getChannelId()`. (No existing channel fits; events/news/launch channels carry the wrong
semantics.)

### New payload parser

`composeApp/src/commonMain/.../data/model/CustomNotificationPayload.kt` (new):

```kotlin
data class CustomNotificationPayload(
    val notificationType: String,   // "custom"
    val customId: String,
    val title: String,
    val body: String,
    val targetType: String,         // "launch" | "event" | "news" | "none"
    val targetId: String,
    val targetUrl: String,
    val customImage: String,
) {
    companion object {
        fun isCustomPayload(data: Map<String, String>) = data["notification_type"] == "custom"
        fun fromMap(data: Map<String, String>): CustomNotificationPayload? { /* null on missing required */ }
    }
}
```

## Architecture — Android

### Worker routing

`NotificationWorker.doWork` — add the custom branch **first** (before event). Add
`processCustomNotification(dataMap)`:

```kotlin
val state = notificationStateStorage.getState()
if (!state.enableNotifications) return Result.success()
if (!state.isTopicEnabled(NotificationTopic.ANNOUNCEMENTS)) return Result.success()
NotificationDisplayHelper.showCustomNotification(applicationContext, payload, payload.title, payload.body)
```

### Display + tap routing

Add `NotificationDisplayHelper.showCustomNotification(...)`:

- Channel: `ANNOUNCEMENTS`. Title/body from payload; image via `customImage` if present.
- Collapse/notify id = `customId.hashCode()`.
- **Tap routing by `targetType`** (reuse existing mechanisms):

  | `targetType` | Tap action |
  |---|---|
  | `launch` | `MainActivity` intent with `launch_id`/`launch_uuid = targetId`, `is_v5 = true` (existing launch deep-link) |
  | `event` | `MainActivity` intent with `event_id = targetId.toInt()` (existing event deep-link) |
  | `news` | `Intent(ACTION_VIEW, Uri.parse(targetUrl))` — external browser (matches news) |
  | `none` | plain `MainActivity` launch intent (app home) |

## Architecture — iOS

- **Display (app alive):** `AppDelegate` shows the alert (server `title`/`body`). Add a
  `notification_type == "custom"` branch so it is recognized as custom.
- **Tap** (`userNotificationCenter(_:didReceive:)`) routes by `target_type`:
  - `launch` → `setNotificationLaunchId(targetId)` (existing launch routing),
  - `event` → existing event navigation with `targetId`,
  - `news` → `UIApplication.shared.open(targetUrl)`,
  - `none` → bring app to foreground, no navigation.
- **NSE (app killed):** custom has no `lsp_id` → non-V5 branch. Apply the `ANNOUNCEMENTS`
  toggle via the shared NSE bridge key `nse_topic_announcements` (news-KMP spec); kill switch
  otherwise. Image enrichment phase-2.

## Files touched

- New: `data/model/CustomNotificationPayload.kt`.
- `data/model/NotificationTopic.kt` / `NotificationState.kt` — add `ANNOUNCEMENTS`.
- `data/model/NotificationChannel.kt` — add `ANNOUNCEMENTS` channel + `getChannelId` mapping.
- `workers/NotificationWorker.kt` — custom branch (first).
- `data/notifications/NotificationDisplayHelper.kt` — `showCustomNotification` + tap routing.
- `ui/settings/NotificationSettingsScreen.kt` + `SettingsViewModel.kt` — "Announcements" toggle
  row (`updateAnnouncements`).
- `iosApp/.../AppDelegate.swift` — custom recognition + `target_type` tap routing.
- `iosApp/NotificationServiceExtension/NotificationService.swift` + `NSEPreferenceBridge.kt`
  — bridge + apply `nse_topic_announcements`.

## Testing

- Send debug custom pushes with each `target_type` (launch / event / news / none); verify:
  parses as custom (not event/launch), shows on `ANNOUNCEMENTS` channel, taps route correctly
  on both platforms.
- Toggle "Announcements" off → no custom shown (app alive AND killed/NSE).
- A custom referencing an event must NOT be mis-shown as an event notification.
