# Starlink Mute Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users mute routine Starlink launch notifications (still notified on failure/partial failure) — server-side via a V6 opt-out topic so it works on iOS, with a single toggle in the Notification Filters screen (Option A placement, per the approved mockup).

**Architecture:** Muting = the device subscribes to attribute topic `v6_<env>_starlinkMuted` through the existing V6 reconciler. The server appends `&& !('v6_<env>_starlinkMuted' in topics)` to every audience class's condition when the launch's `program_id` payload field (already computed for V5) contains Starlink's LL2 program id **25** — except for `failure`/`partial_failure` sends, which go to everyone. Condition budget grows from ≤3 to ≤4 topics against FCM's cap of 5.

**Spec anchors:** approved design canvas (Option A) · Discord request (CosmicRay) · 018 item #6 notification-fatigue thread · V6 design docs in `docs/superpowers/specs/`.

**Repos:** client = `SpaceLaunchNow-KMP-Main` (branch `feat/starlink-mute` off merged main) · server = `SpaceLaunchNow-Server` (branch `feat/v6-topic-targeted-notifications`; Caleb's uncommitted WIP in `discord.py` / `run_send_notification.py` must not be touched or committed).

**Verified facts:**
- Starlink LL2 program id = **25** (live LL2 API, launch `program[]`); server already ships `program_id` as comma-separated ids in the V5/V6 payload (`v5.py:216-217,235`).
- Server condition builder: `build_v6_condition` (`bot/utils/util.py:473`), shapes `all` (1 topic) / `flex` (`type && (agency || location)`, ≤3) / `strict` (`type && agency && location`, 3). Budget comment + `ConditionBudgetTests` in `test_v6_topic_conditions.py` pin ≤3 → update to ≤4.
- `failure` and `partial_failure` are distinct entries in `V6_NOTIFICATION_TYPES` — the carve-out covers both.
- Follow-all classes receive every launch on a 1-topic condition ⇒ mute must apply to **all** classes, and the toggle must be visible when Follow All is on, not only when SpaceX is checked.
- Client: `V6Topics.requiredTopics` is the pure desired-set function; `NotificationState` is `@Serializable` with defaults (new `muteStarlink: Boolean = false` is migration-safe); reconciler + AutoReconcileTrigger already sync on every filter change.

**Open verification flagged to Caleb (not blocking):** FCM v1 `condition` negation (`!`) — legacy API supported it; verify with one debug-env send (`v6_debug_*`) before prod rollout.

## Global Constraints

- Conventional Commits, no Claude co-author. **Both repos left uncommitted** at the checkpoint until Caleb tests.
- Client verify gates: `:composeApp:desktopTest` **and** `:composeApp:testDebugUnitTest` (commonTest runs on all targets), plus `compileKotlinDesktop` + `:composeApp:compileDebugKotlinAndroid`. Server: the bot test suite via the repo's `.venv`.
- Analytics carve-out: toggle change tracked via existing `NotificationSettingChanged(type = "starlink_mute", enabled)`; user property `starlink_muted` set on save.

## Tasks

### S1 — server: mute-aware condition builder (`bot/utils/util.py`)
- Constants: `STARLINK_PROGRAM_ID = "25"`, `V6_STARLINK_MUTED_GROUP = "starlinkMuted"`, `V6_MUTE_EXEMPT_TYPES = ("failure", "partial_failure")`.
- `build_v6_condition(..., mute_starlink: bool = False)`: when `mute_starlink` and `notification_type not in V6_MUTE_EXEMPT_TYPES`, append `` && !({term}) `` where term = `_v6_term(get_v6_attribute_topic(env, V6_STARLINK_MUTED_GROUP))`. Applies to **all three shapes** including `all`.
- Update the ≤3 budget comment to ≤4.

### S2 — server: dispatch passes the flag (`bot/app/notifications/v6.py`)
- In `send_v6_launch_notification`: `mute_starlink = STARLINK_PROGRAM_ID in data.get("program_id", "").split(",")` (no new ORM work), passed to `build_v6_condition`; log it in the existing skip/info lines.

### S3 — server: tests
- `test_v6_topic_conditions.py`: muted flex/strict/all conditions carry the negation; failure + partial_failure never do; un-muted conditions unchanged; `ConditionBudgetTests` ceiling → 4.
- `test_topic_contract.py`: add `starlinkMuted` to the attribute-topic vocabulary if the contract enumerates it.
- `test_v6_dispatch.py`: a launch whose `data["program_id"]` contains "25" dispatches muted conditions; one without does not.
- Run the bot suite; leave Caleb's WIP files untouched.

### C1 — client: state + topics
- `NotificationState`: `val muteStarlink: Boolean = false` (near `followAllLaunches`).
- `V6Topics.requiredTopics`: `if (state.muteStarlink) topics += "v6_${env}_starlinkMuted"` (after the kill-switch check; independent of class/agency — needed under follow-all too, harmless otherwise).
- `V6TopicsTest`: literal-string assertions for the new topic (on/off, follow-all and flex).

### C2 — client: Option A UI + ViewModel
- `SettingsViewModel`: `toggleMuteStarlink()` mutating pending state (same idiom as `toggleAgencySubscription`), participating in unsaved-changes tracking; on save, track `NotificationSettingChanged("starlink_mute", enabled)` and set user property `starlink_muted`.
- `NotificationSettingsScreen`: divider + switch row inside the Launch Service Providers card, visible when `followAllLaunches || subscribedAgencies.contains(SpaceX id "121")`; copy: **"Mute Starlink launches"** / **"You'll still be notified if a Starlink launch fails."** (per mockup).
- Tests: unsaved-changes test addition mirroring existing toggle tests.

### C3 — verification + checkpoint
- Client: both test targets + both compiles + `assembleDebug`.
- Server: bot test suite green.
- STOP: hand both halves to Caleb with a test script (debug-env negation send first, then end-to-end: toggle → Save & apply → subscribed topic visible in debug topics screen → debug Starlink send suppressed, debug failure send delivered).

**Staged commit messages (after Caleb confirms):**
- Server: `feat(v6): exclude starlinkMuted subscribers from Starlink launch sends except failures`
- Client: `feat(notifications): add Starlink mute toggle backed by the v6 starlinkMuted topic`
