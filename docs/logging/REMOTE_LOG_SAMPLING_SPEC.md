# Remote Log Sampling & Diagnostics Control — Spec

**Status:** Draft / proposal
**Date:** 2026-07-10
**Owner:** Caleb Jones
**Area:** Observability (Datadog logging), RevenueCat identity, Firebase Remote Config

---

## 1. Goal

Let us **remotely raise (or lower) the Datadog log sample rate for a specific device**, targeting
that device by its **RevenueCat App User ID**, without shipping a new build. The motivating use
case: a customer reports a bug we can't reproduce, and we want to pull full-fidelity logs from
*their* install only, then turn it back off.

Non-goal (for v1): cohort/segment targeting (e.g. "all premium users"), remote control of RUM
session sampling, or a general-purpose remote feature-flag framework. Those are noted as future
extensions.

---

## 2. Background — current state

Three subsystems already exist but are wired independently. This feature connects them.

### 2.1 The sample rate is already a live, observable value
- `DebugPreferences.datadogSampleRate` — DataStore-backed `Float`, key `debug_datadog_sample_rate`,
  default `100f`. Setter: `DebugPreferences.setDatadogSampleRate(...)`.
  (`composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/storage/DebugPreferences.kt`)
- `DatadogLogger.initialize(sampleRate, debugPreferences)` builds the Datadog `Logger` with
  `setRemoteSampleRate(sampleRate)` (0–100, coerced) **and observes `debugPreferences.debugSettingsFlow`**,
  rebuilding the logger whenever `datadogSampleRate` changes. This live-reconfig observer is the
  insertion point for a remote value.
  (`composeApp/src/androidMain/.../analytics/DatadogConfig.android.kt`, iOS actual is near-identical)
- Today the **only writer** of `datadogSampleRate` is the local debug slider
  (`ui/settings/LoggingSettingsSection.kt`). No server input.

### 2.2 Sampling is gated by *consent* — orthogonal to sample rate
- `DiagnosticLevel` (`OFF` / `STANDARD` / `VERBOSE`) drives a policy applied by
  `DiagnosticLevelController.apply(level)`: Datadog `TrackingConsent`, console severity, Datadog
  severity, file-writer severity.
  (`composeApp/src/commonMain/.../util/logging/DiagnosticLevel.kt`, `DiagnosticLevelController.kt`)
- Fresh/release installs default to `OFF` → Datadog consent `NOT_GRANTED` → **nothing uploads,
  regardless of sample rate.** `VERBOSE` auto-reverts to a lower level after 72h.
- **Implication:** raising the sample rate alone will *not* produce logs from a device whose
  diagnostic level is `OFF`. To actually collect logs we must also remotely raise that device's
  diagnostic level/consent. This spec therefore covers **both** knobs.

### 2.3 A remote-config client already exists (but isn't used at startup)
- `RemoteConfigRepository` / `RemoteConfigRepositoryImpl` wrap Firebase Remote Config (GitLive KMP
  SDK). Reads two string keys today (`roadmap_data`, `pinned_content`), parsed as JSON. 1-hour min
  fetch interval. Gracefully no-ops where Firebase is unavailable (Desktop).
  (`composeApp/src/commonMain/.../data/repository/RemoteConfigRepositoryImpl.kt`; DI at `di/AppModule.kt`)
- `fetchAndActivate()` is currently only called lazily from `RoadmapViewModel` and
  `FeaturedLaunchViewModel` — **not during app startup.**

### 2.4 RevenueCat identity is available at runtime
- `billingManager.getAppUserId()` returns the current RC App User ID (anonymous installs get
  `$RCAnonymousID:...`). `originalAppUserId` is available from `CustomerInfo`.
  (`composeApp/src/commonMain/.../data/billing/BillingManager.kt` and platform actuals)
- **Timing:** RC init runs *after* Datadog init (async; Android `GlobalScope.launch`, iOS ~2s
  delay). The RC id is known within a second or two of launch, but not at Datadog-init time — so any
  per-user decision must (re)apply once the id resolves.

### 2.5 Known gap — RC id is not reliably attached to Datadog
- `UserContext.setRevenueCatUserId()` exists and would inject `rc_user_id`/`is_premium` into every
  log attribute, **but it is never called** (dead wiring).
- Android's active `AndroidBillingManager.updatePurchaseState` does call
  `DatadogRUM.setUser(id = originalAppUserId, ...)`. The active iOS `IosBillingManager` does **not**
  (only the legacy, un-wired `RevenueCatManager` does).
- **Implication:** even after we target a device, we may be unable to *filter Datadog by
  `rc_user_id`*, especially on iOS. Fixing attribution is a prerequisite (Phase 0).

---

## 3. Requirements

### Functional
1. A server-controlled config can set a **default sample rate** for all installs.
2. The config can set a **per-RC-ID override** of the sample rate for one or more specific devices.
3. The config can optionally set a **per-RC-ID diagnostic level** (`OFF`/`STANDARD`/`VERBOSE`) so a
   targeted device actually uploads logs (consent).
4. An override can carry an **expiry** so a device isn't left verbose/high-sample forever.
5. Changes apply **without an app update** and **without a manual restart** (live reconfig via the
   existing observer), within the remote-config propagation window.
6. When no override matches, behavior falls back to the config default, then to the local value —
   the local debug slider must **not** be clobbered by remote config.

### Non-functional
- **Safe by default:** parse failures, missing config, or Firebase-unavailable → no behavior change
  (fall back to current local/default values). Sample rate always coerced to 0–100.
- **Privacy:** targeted identifiers delivered to clients must not casually expose customer identity
  (see §7).
- **Cost-aware:** default sample rate should let us *lower* global sampling to control Datadog spend
  while still allowing 100% on targeted devices.

---

## 4. Design

### 4.1 Config schema (new Remote Config key)

Add one JSON key, e.g. `diagnostics_config`:

```json
{
  "version": 1,
  "default_sample_rate": 5,
  "overrides": [
    {
      "match": { "rc_user_id_hash": "9f2b..." },
      "sample_rate": 100,
      "diagnostic_level": "VERBOSE",
      "expires_at": "2026-07-20T00:00:00Z"
    }
  ]
}
```

- `default_sample_rate` — applied to every install when no override matches (0–100).
- `overrides[]` — first match wins. `match.rc_user_id_hash` is a salted hash of the RC App User ID
  (see §7). `sample_rate` and `diagnostic_level` are both optional per entry.
- `expires_at` — RFC-3339 UTC; an expired entry is ignored (treated as no match).
- `version` — schema guard; unknown versions are ignored (fall back to local/default).

### 4.2 Resolution logic (new)

A pure, unit-testable resolver:

```
fun resolveDiagnostics(
    config: DiagnosticsConfig?,
    rcUserId: String?,
    now: Instant
): ResolvedDiagnostics   // (sampleRate: Float?, diagnosticLevel: DiagnosticLevel?)
```

- If `config == null` or unparseable → `ResolvedDiagnostics(null, null)` (no remote influence).
- Find first non-expired override whose `rc_user_id_hash` matches `hash(rcUserId)`.
  - Match → use its `sample_rate` / `diagnostic_level` (either may be null).
  - No match → `sampleRate = default_sample_rate`, `diagnosticLevel = null`.

### 4.3 Applying the result (precedence)

- **Sample rate.** Introduce a new persisted field
  `DebugPreferences.remoteDatadogSampleRateOverride: Float?`. The `DatadogLogger` observer computes
  the **effective** rate as:

  ```
  effective = remoteDatadogSampleRateOverride ?? datadogSampleRate   // local slider is the fallback
  ```

  This keeps the developer's local debug slider intact while letting remote take precedence when
  present. Clearing the remote override (config removed / expired) reverts to the local value.

- **Diagnostic level.** If the resolver returns a non-null `diagnosticLevel`, call
  `DiagnosticLevelController.apply(level)`. Because `VERBOSE` auto-reverts after 72h, the controller
  **re-asserts** the remote level on every successful fetch while the override is active. Store the
  remotely-driven level separately from the user's own choice so we can restore the user's setting
  when the override lapses.

### 4.4 Orchestration (new component)

`RemoteDiagnosticsController` (commonMain), started once at app init:

1. Wait for `billingManager.getAppUserId()` to be non-null (observe/await; it resolves shortly after
   startup).
2. `remoteConfigRepository.fetchAndActivate(forceRefresh = true)` on startup (there is no startup
   fetch today — this adds one), then read `diagnostics_config`.
3. `resolveDiagnostics(config, rcUserId, now)` → apply per §4.3.
4. Re-run on: RC id change, subsequent RC refreshes, and a periodic re-assert timer (to counter the
   72h `VERBOSE` auto-revert).

Start sites (mirror existing init):
- Android: `MainApplication.onCreate` (after `initializeDatadog` and alongside
  `DiagnosticLevelController.start`).
- iOS: `MainViewController` / `KoinInitializer`.
- Desktop: no-op (no Datadog / Firebase).

### 4.5 Phase 0 prerequisite — fix RC id attribution

So targeted logs are actually *findable* in Datadog:
- Call `UserContext.setRevenueCatUserId(id)` when the RC id resolves and on `CustomerInfo` updates,
  on **both** platforms.
- Ensure the active `IosBillingManager.updatePurchaseState` sets the Datadog user
  (`DatadogRUM.setUser(originalAppUserId, ...)`), matching Android.
- Result: `rc_user_id` present on log attributes + Datadog user context → filterable in Datadog.

---

## 5. Component / file change list

| Change | Location (new or edited) |
|---|---|
| `DiagnosticsConfig` DTO + `resolveDiagnostics()` resolver + unit tests | `commonMain/.../util/logging/` (new) |
| `remoteDatadogSampleRateOverride` field + setter | `data/storage/DebugPreferences.kt` |
| Observer computes effective = remote ?? local | `analytics/DatadogConfig.{android,ios}.kt` (`DatadogLogger`) |
| Read `diagnostics_config` key + parse | `data/repository/RemoteConfigRepository(Impl).kt` |
| `RemoteDiagnosticsController` (fetch → resolve → apply, re-assert) | `commonMain/.../util/logging/` (new) + DI in `di/AppModule.kt` |
| Startup fetch + controller start | `MainApplication.kt` (Android), `MainViewController.kt` / `KoinInitializer.kt` (iOS) |
| **Phase 0:** wire `UserContext.setRevenueCatUserId`; iOS `DatadogRUM.setUser` | `data/billing/*BillingManager.kt`, `util/logging/UserContext.kt` |

---

## 6. Operations — how we'd use it

1. Get the target device's RC App User ID (visible on the in-app debug / Support screens, and in
   Datadog user context once Phase 0 lands). Note anonymous ids (`$RCAnonymousID:`) rotate on
   reinstall/logout.
2. Compute its salted hash and add an `overrides[]` entry with `sample_rate: 100`,
   `diagnostic_level: "VERBOSE"`, and an `expires_at` a few days out. Publish the Remote Config.
3. Device picks it up on next fetch. **Latency:** Firebase RC min fetch interval is 1h; startup
   force-refresh shortens it for a fresh launch. RC is pull-based (not push), so expect minutes-to-1h.
4. When done, remove the entry (or let `expires_at` lapse) → device reverts to the config default
   and its own diagnostic level.

---

## 7. Privacy & safety

- **Don't ship raw RC ids.** Firebase Remote Config is client-fetchable, so treat it as semi-public.
  Match on a **salted hash** of the RC id (`rc_user_id_hash`); the salt lives in the app
  (build secret), so the published config never contains a raw customer identifier.
- RC anonymous ids are opaque and low-sensitivity, but hashing avoids building a public map of "these
  specific users are being watched."
- **Kill switch / blast radius:** `default_sample_rate` is a global lever — a bad value affects all
  installs. Keep it conservative; the resolver coerces to 0–100 and any parse error falls back to the
  local/default path (no change). Consider a `enabled: true/false` top-level flag for a hard off.
- Do not remotely enable logging in violation of the user's consent choice beyond what the diagnostic
  levels already permit; `VERBOSE`'s 72h auto-revert remains the backstop.

---

## 8. Testing

- **Unit:** `resolveDiagnostics()` — match by hash, no-match → default, expired entry ignored, null
  fields, unknown `version`, malformed JSON → no-op.
- **Precedence:** remote override present vs absent vs cleared; local slider preserved.
- **Integration/manual:** publish an override for a test device's RC id → confirm (a) sample rate
  changes live without restart, (b) diagnostic level rises and logs appear in Datadog filtered by
  `rc_user_id`, (c) removing the entry reverts both, (d) `VERBOSE` re-asserts past its auto-revert.
- **Regression:** Firebase-unavailable (Desktop) and offline startup → no crash, no behavior change.

---

## 9. Phasing

- **Phase 0 — Attribution (prerequisite).** Wire `rc_user_id` onto Datadog logs + user context on
  both platforms. Small; independently valuable.
- **Phase 1 — Remote sample rate.** Config key + resolver + `remoteDatadogSampleRateOverride` +
  observer precedence + startup fetch + controller. Delivers per-device *sampling* control.
- **Phase 2 — Remote diagnostic level (consent).** Adds the level knob + re-assert timer so targeted
  devices actually upload. This is what makes "pull logs from one device" fully work.
- **Phase 3 — Hardening.** Salted-hash matching, `enabled` kill switch, ops runbook, optional
  cohort matching (by entitlement/attribute) as a future extension.

---

## 10. Open questions

1. **Match identifier.** RC anonymous id rotates on reinstall/logout. Do we match only
   `getAppUserId()`, also `originalAppUserId`, or add a stable install id as a fallback?
2. **Force-refresh cadence.** Startup force-refresh vs quota/battery — is up-to-1h latency acceptable,
   or do we want a shorter minimum fetch interval for this key?
3. **Scope creep.** Do we want cohort targeting (all premium, a specific app version) in v1, or keep
   v1 strictly per-device?
4. **Default sample rate rollout.** What global `default_sample_rate` do we want as the steady-state
   Datadog-cost baseline once this exists?
