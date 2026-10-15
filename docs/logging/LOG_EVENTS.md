# Structured log event catalogue

Source of truth for every `log_kind` the app emits. The code side is the `DiagnosticEventKind`
enum in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/DiagnosticEvents.kt`.
`DiagnosticEventCatalogueTest` fails when the enum and the table below disagree.

Spec: `SpaceLaunchNow-Contracts/notifications/plans/2026-08-30-client-log-quality-spec.md` section 4.2.

All events carry `log_kind`, `platform` (`ios` or `android`), `tag`, plus `UserContext.getLogAttributes()`.
The message text is a stable label `[EVENT] <kind>`. Dashboards and monitors query attributes, never the message.

Upload rule: "direct upload" means the event also goes straight to Datadog when remote logging is
active and the Kermit writer would drop that severity (STANDARD drops below Warn). It never uploads
twice at VERBOSE. "Kermit only" means `upload = false`.

| `log_kind` | when | severity | upload rule | fields |
|---|---|---|---|---|
| `nse_breadcrumb` | NSE drain, once per crumb | warn if suppressed, else info | direct upload | `notification_type`, `decision`, `reason`, `nse_ts` |
| `startup_state` | iOS cold start | info | direct upload | 15 `live_*` / `nse_*` attributes, `drained_count` |
| `push_summary` | once per cold start | info | direct upload | existing `push.*`, `diagnostics.level`, `push.token_state` (`present`, `apns_pending`, `unavailable`), `push.token_unavailable_reason` |
| `push_reconcile` | end of every `reconcileSubscriptions()` / `forceResubscribe()` that is not skipped | warn if `failed > 0` and `failure_class != apns_pending`; debug if `failure_class == apns_pending`; info otherwise | direct upload, except Kermit only for the debug case | `trigger` (`app_start`, `post_apns`, `settings`, `kill_switch`, `token_refresh`, `force`, `debug`), `attempted`, `failed`, `skipped`, `failure_class` (`none`, `apns_pending`, `timeout`, `fcm_error`, `mixed`), `changeover_complete`, `confirmed_topic_count`, `token_present` |
| `filter_decision` | iOS in-app notification filter | warn if suppressed, else info | direct upload | `decision`, `reason`, `notification_type`, `agency_id`, `location_id`, `v6_passthrough`, `strict`, `follow_all`, `agency_count`, `location_count` |
| `nse_prefs_sync` | after each App Group preference write | debug | Kermit only | `enabled`, `v6_changeover`, `follow_all`, `strict`, `agency_count_expanded`, `location_count_expanded` |
| `notification_displayed` | Android `NotificationWorker` after `notify()` (emitted by `NotificationDisplayHelper.showV5Notification`) | info | direct upload | `notification_type`, `launch_id`, `reason`, `channel`, `has_image` |

## Rules for changes

- Add the enum entry and the table row in the same commit.
- Never put payloads, launch names, token content, full ID sets or HTTP bodies in a field.
