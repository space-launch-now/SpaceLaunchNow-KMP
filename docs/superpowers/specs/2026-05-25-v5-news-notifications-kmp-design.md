# V5 News Notifications — KMP App

**Date:** 2026-05-25
**Status:** Approved
**Repo:** SpaceLaunchNow-KMP-Main
**Companion spec:** `SpaceLaunchNow-Server/docs/superpowers/specs/2026-05-15-v5-news-notifications-design.md`

## Problem

The server (companion spec) will send featured-news on the V5 path with a flat payload marked
by `article_id`. Today the KMP app cannot handle it:

- No V5 news payload parser; `NotificationWorker` detection order is `event_id` → `lsp_id` →
  V4, so a news payload (no `event_id`, no `lsp_id`) falls through to the **V4** parser and is
  dropped/mis-parsed.
- The `FEATURED_NEWS` topic exists in `NotificationState`/`NotificationTopic` (default on) but
  has **no settings UI row and is applied nowhere** — so even once parsed, news could not be
  filtered by user preference.
- No tap routing for news.

This spec also fixes a **pre-existing filtering bug** it sits next to: the `EVENTS`
("Event Notifications") toggle exists in the UI/ViewModel but `processEventNotification` only
checks the global kill switch, so the toggle is silently ignored. News, events, and (companion)
custom all need the same per-type-toggle treatment, so the pattern is established here.

## Filtering model (shared)

Broadcast-type notifications (event, news, custom) are **not** agency/location filtered. Each
is gated by **the global kill switch AND its own per-type topic toggle**:

| Type | Toggle | Today | This spec |
|---|---|---|---|
| Event | `NotificationTopic.EVENTS` | UI exists, **not applied** | apply it (bug fix) |
| News | `NotificationTopic.FEATURED_NEWS` | model only | add UI row + apply |
| Custom | `NotificationTopic.ANNOUNCEMENTS` (new) | — | companion custom-KMP spec |

This is distinct from the launch filter (`V5NotificationFilter` — webcast-only + agency/
location + strict/follow-all). Per-type filtering for *launch* types remains a separate `// TODO`
in `V5NotificationFilter` and is **out of scope** here, as are the hidden `FAILURE` /
`PARTIAL_FAILURE` / `WEBCAST_LIVE` toggles.

## Detection order (shared with custom spec)

`NotificationWorker.doWork` (Android) and the iOS receipt paths adopt this order:

1. `notification_type == "custom"` → custom (companion spec)
2. `event_id` present → event
3. **`article_id` present → news** ← this spec
4. `lsp_id` present → V5 launch
5. else → V4

## Architecture — Android

### New payload parser

`composeApp/src/commonMain/.../data/model/NewsNotificationPayload.kt` (new), mirroring
`EventNotificationPayload`:

```kotlin
data class NewsNotificationPayload(
    val notificationType: String,   // "featured_news"
    val articleId: String,
    val title: String,
    val body: String,
    val articleTitle: String,
    val newsSite: String,
    val articleUrl: String,
    val articleImage: String,
) {
    companion object {
        fun isNewsPayload(data: Map<String, String>) = data.containsKey("article_id")
        fun fromMap(data: Map<String, String>): NewsNotificationPayload? { /* return null on missing required */ }
    }
}
```

### Worker routing

`NotificationWorker.doWork` — insert a news branch per the detection order (after event,
before `lsp_id`/V5). Add `processNewsNotification(dataMap)`:

```kotlin
val state = notificationStateStorage.getState()
if (!state.enableNotifications) return Result.success()
if (!state.isTopicEnabled(NotificationTopic.FEATURED_NEWS)) return Result.success()  // per-type toggle
NotificationDisplayHelper.showNewsNotification(applicationContext, payload, payload.title, payload.body)
```

### Display + tap

Add `NotificationDisplayHelper.showNewsNotification(...)`:

- Channel: existing `SpaceLaunchNotificationChannel.NEWS_UPDATES` (`news_updates_low`).
- Title/body from payload; `BigPictureStyle` if `articleImage` present, else `BigTextStyle`.
- **Tap opens the article URL externally**, matching the news-list behavior
  (`NewsEventsScreen` uses `LocalUriHandler.openUri`). The content `PendingIntent` is a direct
  `Intent(Intent.ACTION_VIEW, Uri.parse(articleUrl))` — no `MainActivity` routing needed.
  Collapse/notify id = `articleId.hashCode()`.

## Architecture — iOS

- **Display (app alive):** `AppDelegate` already shows alert-style V5 messages using
  server-provided `title`/`body`. News carries both, so it displays. Add an `article_id`
  branch so it is recognized as news (not treated as a launch).
- **Tap:** in `userNotificationCenter(_:didReceive:)`, if `notification_type == "featured_news"`,
  open `article_url` via `UIApplication.shared.open(url)` (matches `ExternalLinkHandler.ios`
  external-browser behavior). Do **not** call `setNotificationLaunchId`.
- **NSE (app killed):** news has no `lsp_id`, so it takes the existing non-V5 branch. Extend
  that branch to apply the `FEATURED_NEWS` toggle (see NSE bridge below) and the kill switch.
  Image enrichment on iOS is **phase-2** (MVP shows text); the alert still displays.

## Settings UI + persistence (shared infra)

1. **Surface `FEATURED_NEWS`** as a toggle row in `NotificationSettingsScreen` (General card,
   next to "Event Notifications"), with `SettingsViewModel.updateFeaturedNews(enabled) =
   updateTopic(NotificationTopic.FEATURED_NEWS, enabled)`. Add to
   `NotificationTopic.getUserConfigurableTopics()` so it persists in the `topic_settings` JSON.
2. **Fix the EVENTS toggle (bug):** in `processEventNotification`, add
   `if (!state.isTopicEnabled(NotificationTopic.EVENTS)) return Result.success()` after the
   kill-switch check. Apply the same on the iOS event path.

## NSE preference bridge (shared infra)

`NSEPreferenceBridge` currently writes only kill-switch / follow-all / strict / agencies /
locations to the App Group. To honor per-type toggles when the app is killed, **bridge the
broadcast-type booleans**:

- Write `nse_topic_events`, `nse_topic_featured_news` (and `nse_topic_announcements` for the
  custom spec) into the shared `UserDefaults` whenever settings change.
- In the NSE non-V5 branch, read the relevant bool and suppress via `deliverEmptyNotification`
  if its type is toggled off. Map `notification_type` → key:
  `featured_news → nse_topic_featured_news`, `event_* → nse_topic_events`,
  `custom → nse_topic_announcements`.

## Files touched

- New: `data/model/NewsNotificationPayload.kt`.
- `workers/NotificationWorker.kt` — news branch + EVENTS toggle fix.
- `data/notifications/NotificationDisplayHelper.kt` — `showNewsNotification`.
- `data/model/NotificationTopic.kt` / `NotificationState.kt` — add `FEATURED_NEWS` to
  user-configurable set.
- `ui/settings/NotificationSettingsScreen.kt` + `SettingsViewModel.kt` — news row + event row
  already exists.
- `iosApp/.../AppDelegate.swift` — news recognition + tap-to-URL + EVENTS toggle.
- `iosApp/NotificationServiceExtension/NotificationService.swift` + `NSEPreferenceBridge.kt`
  + `NSEFilterPreferences` — bridge & apply per-type toggles.

## Testing

- Send a debug V5 news push; verify it parses as news (not V4), shows on `NEWS_UPDATES`, and
  tapping opens `article_url` in the browser on both platforms.
- Toggle "Featured News" off → no news shown (app alive AND killed/NSE).
- Toggle "Event Notifications" off → no events shown (regression-fixes the bug).
- Verify launch notifications are unaffected.
