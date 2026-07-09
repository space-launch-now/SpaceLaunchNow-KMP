# Feature Specification: Desktop Launch Notifications (Windows + macOS)

**Feature Branch**: `015-desktop-notifications`  
**Created**: 2026-07-09  
**Status**: Draft  
**Input**: User description: "Desktop launch notifications (Windows + macOS) for the Space Launch Now KMP app — bring launch reminder notifications to the Compose Desktop (JVM) target, which today has neither push (FCM) nor scheduled background delivery (WorkManager)."

## Overview & Context *(informative)*

On Android and iOS, launch notifications are **server-driven push**: the shared `NotificationRepository` manages topic subscriptions (per-agency, per-location, follow-all, strict/flexible matching) as a `NotificationState`, the server decides when to push, and the platform receives the payload and displays it after passing the shared `NotificationFilter`. WorkManager also schedules purely-local reminders on Android.

**Desktop (the `jvm("desktop")` target, entry point [`Main.kt`](../../composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/Main.kt)) has neither FCM nor WorkManager**, so that push path cannot be reused. Today desktop notification support is stubbed: [`TestNotificationHelper.kt`](../../composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/TestNotificationHelper.kt) only logs, and [`NotificationRepositoryPlatform.kt`](../../composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/data/repository/NotificationRepositoryPlatform.kt) reports permission as always-granted and settings as no-op. This feature builds desktop notifications as a **local, client-scheduled** path that reuses the existing shared preference model.

This spec describes WHAT and WHY. Mechanism references (Compose `TrayState`, packaging) are captured only as constraints/risks, not as prescribed implementation.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Receive a launch reminder while the app is open (Priority: P1)

A desktop user who follows certain agencies/locations (or "follow all") has the Space Launch Now app open. As a launch they care about approaches its NET, the OS shows a native toast notification ("🚀 Falcon 9 | Starlink is launching in 10 minutes") so the user knows to switch to a webcast without having to keep the app in focus.

**Why this priority**: This is the minimum viable, fully self-contained capability — it requires no background process, no OS launch agent, and no backend changes. It is the guaranteed baseline of the feature and everything else builds on it.

**Independent Test**: Launch the desktop app with a known upcoming launch inside the user's subscription filter and a lead-time topic (e.g. 10-minute) enabled. Advance/observe until the scheduled offset. Verify exactly one native toast appears with the correct launch title and lead-time wording.

**Acceptance Scenarios**:

1. **Given** the app is running and an upcoming launch matches my notification preferences, **When** the launch reaches an enabled lead-time offset (e.g. 24h / 1h / 10min), **Then** a native desktop toast is shown with the launch name and time-to-launch
2. **Given** a launch does NOT match my preferences (agency/location/strict-matching rules), **When** its lead-time offset is reached, **Then** no notification is shown
3. **Given** notifications are globally disabled (`enableNotifications = false`), **When** any lead-time offset is reached, **Then** no notification is shown
4. **Given** a notification has already been shown for a given (launch, lead-time) pair, **When** the schedule is re-evaluated, **Then** the notification is not shown again (no duplicates)

---

### User Story 2 - Reminders continue when the app is minimized/closed to tray (Priority: P2)

A user does not keep the app window in the foreground all day, but still wants to be alerted before a launch. They minimize or close the window and the app continues running in the system tray, so scheduled reminders still fire while the machine is on.

**Why this priority**: Foreground-only (P1) is of limited value for a reminder feature — users rarely leave a launch-tracking window open for hours. Tray-resident background delivery is the leaning target for v1, but it materially expands scope (tray lifecycle, minimize-to-tray, optional launch-on-startup) and its inclusion is not yet finalized.

> [NEEDS CLARIFICATION: Final v1 delivery model is undecided (leaning tray-resident). Decide whether v1 ships (a) foreground-only, (b) tray-resident background, or (c) a backend push channel. This choice determines whether Story 2 is in v1 scope or deferred. See Open Questions #1.]

**Independent Test**: With tray-resident mode enabled, close the app window (app remains in tray). At a scheduled lead-time offset for a matching launch, verify the toast still appears and clicking it restores the window.

**Acceptance Scenarios**:

1. **Given** tray-resident mode is enabled, **When** I close/minimize the main window, **Then** the app keeps running in the system tray and the scheduler stays alive
2. **Given** the app is tray-resident (window closed) and the machine is on, **When** a matching launch reaches an enabled lead-time offset, **Then** the toast is still shown
3. **Given** the app is tray-resident, **When** I choose "Quit" from the tray menu, **Then** the app fully exits and no further notifications fire
4. **Given** the machine was asleep or the app was not running across a scheduled offset, **When** the app next resumes/starts, **Then** missed-but-still-relevant reminders are handled per the catch-up rule [NEEDS CLARIFICATION: do we fire a late "launching now/soon" catch-up, or silently skip offsets already in the past? See Open Questions #5.]

---

### User Story 3 - Clicking a notification opens the launch (Priority: P2)

When a reminder toast appears, the user clicks it and the app window comes to the foreground showing that launch's detail screen, so they can get more info or find the webcast link.

**Why this priority**: A reminder that can't be acted on is materially less useful; deep-linking mirrors the established Android behavior ([`LaunchNotificationWorker`](../../composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/workers/LaunchNotificationWorker.kt) passes `launch_id` into an activity intent). It is P2 because the reminder itself (P1) delivers value even without click-through.

**Independent Test**: Trigger a reminder toast, click it, and verify the window is focused/restored and navigates to the correct launch detail screen (matched by launch UUID).

**Acceptance Scenarios**:

1. **Given** a reminder toast is visible, **When** I click it, **Then** the app window is brought to the foreground (restored from tray/minimized if needed)
2. **Given** I clicked a reminder toast, **When** the window focuses, **Then** it deep-links to the detail screen for that launch (by UUID)
3. **Given** the toast platform does not support click callbacks [NEEDS CLARIFICATION: Compose `TrayState.sendNotification` click-callback support/consistency across Windows and macOS is unverified], **When** I click it, **Then** at minimum the window is focused (deep-link is best-effort)

---

### User Story 4 - Preferences are shared with mobile, no separate desktop settings (Priority: P3)

A user who already configured which agencies/locations/lead-times they want on their phone opens the desktop app signed in to the same account. Desktop honors those same preferences without the user re-configuring anything.

**Why this priority**: Consistency and zero-setup are valuable, but this is a P3 because it is a reuse/plumbing guarantee rather than net-new user-facing surface; the P1 reminder still works with whatever preferences exist locally.

**Independent Test**: With a `NotificationState` that subscribes only to SpaceX + Texas in flexible mode, confirm desktop shows reminders for a matching launch and suppresses a non-matching one — using the exact same `NotificationFilter` decision as Android/iOS.

**Acceptance Scenarios**:

1. **Given** my shared `NotificationState` (agencies, locations, follow-all, strict/flexible matching, enabled timing topics), **When** desktop evaluates whether to notify, **Then** it uses the shared `NotificationFilter` logic and reaches the same allow/suppress decision the mobile platforms would
2. **Given** I change my notification preferences on another platform and they sync to this device, **When** desktop next evaluates the schedule, **Then** the updated preferences are respected
3. **Given** desktop, **When** I look for notification configuration, **Then** there is no separate/duplicate desktop-only notification settings model [NEEDS CLARIFICATION: is a desktop UI surface needed at all for global enable/disable + tray-resident + launch-on-startup toggles, even though the filter prefs are shared? See Open Questions #3.]

---

### Edge Cases

- **App not running when a launch occurs**: In foreground-only mode, no notification fires — this is an accepted limitation and MUST be communicated to the user (e.g. onboarding copy or settings note), not silently missed.
- **Machine asleep / offline across a scheduled offset**: Reminder may be missed; on resume the catch-up rule (Open Questions #5) governs whether a late notice fires.
- **Launch NET slips (rescheduled) after a reminder was scheduled**: The schedule MUST re-anchor to the new NET so reminders fire relative to the updated time, and a stale reminder for the old time MUST NOT fire.
- **Launch is scrubbed/removed**: Any pending reminders for it MUST be cancelled.
- **Duplicate suppression across restarts**: A (launch, lead-time) pair already notified MUST NOT re-notify after an app restart (persisted dedupe).
- **macOS bare `desktopRun`**: Native toasts on macOS reliably appear only from a signed `.app` bundle; from `./gradlew desktopRun` the notification may be attributed to "java" or not appear. v1 requires best-effort delivery in `desktopRun` (see FR + Risks), so a fallback path and clear dev-mode caveat are required.
- **OS Do-Not-Disturb / Focus mode**: When the OS suppresses toasts, the app cannot force display; the notification is considered "delivered to OS" and the history record reflects the attempt. [NEEDS CLARIFICATION: do we detect/expose OS-level notification permission on Windows/macOS, given desktop currently hard-codes permission = granted?]
- **No upcoming launches / empty schedule**: Scheduler idles without error.
- **Very long launch titles**: Toast text is truncated by the OS; the app supplies a concise title via the existing `LaunchFormatUtil.formatLaunchTitle` convention.
- **System clock change / timezone change**: Scheduled offsets are computed from launch NET and current time; a clock change MUST not cause a burst of stale notifications.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The desktop app MUST display native OS toast notifications for upcoming launches on both Windows and macOS.
- **FR-002**: Desktop notifications MUST be triggered by a **local, client-side scheduler** derived from upcoming launches (from the existing `LaunchRepository`), NOT by FCM/push. [NEEDS CLARIFICATION: unless Open Questions #1 selects a backend push channel instead.]
- **FR-003**: The scheduler MUST reuse the shared `NotificationState` and MUST make its show/suppress decision via the shared `NotificationFilter` (`shouldShowNotification`), honoring: global enable, follow-all, subscribed agencies, subscribed locations, strict vs flexible matching, webcast-only, and enabled timing topics.
- **FR-004**: Desktop MUST reuse the existing lead-time/timing topics (e.g. 24-hour, 1-hour, 10-minute, 1-minute) and only fire offsets whose corresponding timing topic is enabled in `NotificationState`. Desktop MUST NOT define its own separate lead-time set.
- **FR-005**: The app MUST NOT show a duplicate notification for the same (launch, lead-time offset) pair, and this de-duplication MUST persist across app restarts.
- **FR-006**: The app MUST record each notification attempt (shown or filtered) into the existing shared `NotificationHistoryStorage`, including whether it was filtered and why, consistent with the other platforms.
- **FR-007**: When a launch's NET changes, the app MUST re-schedule its reminders against the new NET and cancel reminders anchored to the old NET; when a launch is removed/scrubbed, its pending reminders MUST be cancelled.
- **FR-008**: Clicking a notification MUST bring the app window to the foreground (restoring it from minimized/tray if applicable). Deep-linking to the specific launch detail screen (by UUID) is REQUIRED where the toast platform supports click callbacks and best-effort otherwise. [NEEDS CLARIFICATION: click-callback support across Windows/macOS is unverified.]
- **FR-009**: The desktop notification title/body MUST use the existing formatting conventions (`LaunchFormatUtil.formatLaunchTitle`, `DateTimeUtil` honoring the UTC toggle) rather than a one-off format.
- **FR-010**: The app MUST provide a global desktop notification enable/disable that respects (and does not diverge from) the shared `enableNotifications` flag.
- **FR-011 (delivery model)**: The app MUST fire reminders while the main window is open (foreground). [NEEDS CLARIFICATION: whether background/tray-resident delivery (FR-012/FR-013) is included in v1 is undecided — leaning yes. See Open Questions #1.]
- **FR-012 (conditional on #1)**: If tray-resident mode is in v1 scope, the app MUST continue running in the system tray when the main window is closed/minimized, keep the scheduler alive, and provide a tray affordance to restore the window and to fully quit.
- **FR-013 (conditional on #1)**: If background delivery is in v1 scope, the app SHOULD offer an optional "launch on startup / start minimized" behavior via an OS launch-agent/startup entry so reminders can fire without the user manually opening the app. [NEEDS CLARIFICATION: is launch-on-startup in v1 or deferred?]
- **FR-014 (macOS packaging)**: Native notifications MUST work in the packaged/signed macOS `.app` (`createDistributable`/`packageDmg`) AND MUST make a **best-effort** attempt to display from a bare `./gradlew desktopRun` dev run. Where `desktopRun` cannot show a real toast (attribution as "java", OS limitation), the app MUST degrade gracefully (documented fallback such as AWT `SystemTray.displayMessage`, or a clearly-logged no-op) without crashing. The known packaging gate (`checkRuntime` rejecting Homebrew JDKs) MUST be documented for contributors.
- **FR-015**: On Windows, notifications MUST appear in the Action Center/notification area using the app's identity/icon; Windows-specific requirements for app identity (e.g. AppUserModelID) MUST be captured during planning. [NEEDS CLARIFICATION: Windows app-identity requirements for persistent toasts are unverified.]
- **FR-016**: The app MUST accurately reflect notification capability in `NotificationRepositoryPlatform` rather than hard-coding permission = granted; where an OS exposes a notification permission/authorization state (macOS, Windows Focus settings), the app SHOULD surface it. [NEEDS CLARIFICATION: extent of OS permission introspection available/needed on desktop.]
- **FR-017**: The user MUST be informed of the delivery limitation of the chosen model (e.g. "reminders only fire while the app is running / running in the tray").

### Key Entities

- **NotificationState** (existing, shared): user preferences — global enable, follow-all, strict/flexible matching, subscribed agency IDs, subscribed location IDs, enabled timing topics. Reused as-is; desktop reads it, does not fork it.
- **NotificationFilter** (existing, shared): platform-agnostic allow/suppress decision. Reused as-is as the single source of truth for whether a desktop reminder fires.
- **NotificationData / ParsedNotification** (existing, shared): normalized launch reminder payload (launch UUID, name, image, NET, agency/location IDs, timing type). Desktop constructs this from `LaunchRepository` data for the local path.
- **NotificationHistoryItem / NotificationHistoryStorage** (existing, shared): per-notification audit record (received-at, type, shown/filtered, reason). Reused for desktop dedupe/audit; capped at 100.
- **DesktopReminderSchedule** (new, desktop-only concept): the set of pending (launch UUID, lead-time offset, fire-at) entries the local scheduler tracks; persisted so it survives restarts and can be reconciled when NETs change. [NEEDS CLARIFICATION: exact persistence store — reuse DataStore/`NotificationHistoryStorage` semantics vs a dedicated schedule store.]
- **Tray presence** (new, desktop-only, conditional on #1): the tray icon + menu that keeps the scheduler alive and hosts restore/quit actions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: For a launch matching the user's preferences with an enabled lead-time topic, a native toast is shown on Windows and macOS within **±30 seconds** of the scheduled offset while the app (per the chosen delivery model) is running.
- **SC-002**: **Zero** duplicate notifications for the same (launch, lead-time) pair across a full session including at least one app restart.
- **SC-003**: **100%** agreement between desktop's show/suppress decisions and the shared `NotificationFilter` for a fixed matrix of test preferences × launches (i.e. desktop never diverges from the mobile filtering logic).
- **SC-004**: When a launch NET slips by ≥1 hour, the next reminder fires relative to the **new** NET, and **no** reminder fires for the superseded time.
- **SC-005**: Clicking a notification focuses the app window in **≥95%** of attempts, and deep-links to the correct launch detail screen wherever click callbacks are supported by the platform.
- **SC-006**: Every notification attempt (shown or filtered) produces exactly one `NotificationHistoryStorage` entry with the correct `wasShown`/`wasFiltered`/`filterReason`.
- **SC-007**: A notification is successfully displayed from a packaged/signed macOS `.app`; behavior from `./gradlew desktopRun` is verified and any limitation is documented (no crash, graceful degradation).
- **SC-008 (conditional on #1)**: With tray-resident mode enabled, a reminder for a matching launch fires while the main window is closed (app in tray) on a machine that stays awake.

## Open Questions *(to resolve during planning)*

1. **Delivery model for v1** — foreground-only, tray-resident background, or a new backend push channel? *(Undecided; leaning tray-resident. Drives Story 2, FR-011–FR-013, SC-008.)*
2. **Trigger source** — local scheduling off `LaunchRepository` filtered by `NotificationState` (assumed), vs a new desktop push channel to the backend. *(Coupled to #1.)*
3. **Settings surface** — shared prefs are reused (decided). Still open: does desktop need any UI at all for global enable/disable + tray-resident + launch-on-startup toggles, or is it configuration-free beyond what mobile writes? *(Decided: reuse shared `NotificationState` for filtering; no separate desktop filter model.)*
4. **Lead times** — reuse existing timing topics (decided). No desktop-specific offsets.
5. **Missed-offset / catch-up rule** — when the app was not running (or machine asleep) across a scheduled offset, fire a late "launching soon/now" catch-up on resume, or silently skip past offsets?
6. **Click behavior depth** — window-focus is required; is full deep-link-to-detail a v1 requirement or best-effort, given unverified click-callback support in Compose `TrayState` across Windows/macOS?
7. **Windows vs macOS capability gaps** — app identity (AppUserModelID) for Windows persistent toasts; signed `.app` requirement and `checkRuntime`/Homebrew-JDK gate for macOS; OS notification-permission / Focus-DND introspection on each.
8. **Schedule persistence store** — reuse existing DataStore-backed storage semantics vs a dedicated schedule store for pending (launch, offset) entries.
