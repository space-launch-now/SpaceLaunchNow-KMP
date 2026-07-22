# Remote Diagnostics Control (Phases 1–2) Implementation Plan

> Implements `docs/logging/REMOTE_LOG_SAMPLING_SPEC.md` Phases 1–2 (Phase 0 attribution landed in `bb051cc`).
> **For agentic workers:** REQUIRED SUB-SKILL: superpowers:executing-plans. Steps use checkbox syntax.

**Goal:** Remotely raise/lower the Datadog sample rate and diagnostic level for a specific device, targeted by RevenueCat App User ID, via Firebase Remote Config — no app update, no restart.

**Architecture:**

```
Firebase Remote Config  (JSON key: diagnostics_config)
        │ fetchAndActivate (startup force-refresh, then 6h re-assert loop)
        ▼
RemoteDiagnosticsController (commonMain; started on Android + iOS, not Desktop)
   waits for UserContext.revenueCatUserId (fed by Phase 0 billing wiring)
   parseDiagnosticsConfig() → resolveDiagnostics(config, rcUserId, now)   ← pure, unit-tested
        │ sampleRate: Float?                    │ diagnosticLevel: DiagnosticLevel?
        ▼                                       ▼
DebugPreferences                         LoggingPreferences
 .remoteDatadogSampleRateOverride         remote-override keys (level name +
 (DataStore, null = no override)          72h-capped expiry; separate from the
        │                                 user's own diagnostic_level key)
        ▼ debugSettingsFlow                     ▼ getDiagnosticSettings()
DatadogLogger observer (android/ios):     effective = remoteOverride ?: userLevel
 effective = remote ?? local slider       → DiagnosticLevelController.apply()
 → rebuild logger with new sample rate      (consent + writer severities)
```

**Key decisions (resolved from spec open questions):**
- v1 matches raw `rc_user_id` (RC anonymous ids are opaque/low-sensitivity per spec §7); salted-hash matching is Phase 3.
- Remote level is stored in **separate DataStore keys**, never touching the user's own `diagnostic_level` — the user's choice restores automatically when the override lapses or is removed.
- Remote-driven level carries a **72h expiry backstop** (re-stamped on each successful re-assert), preserving the spec §7 consent backstop even if the config is never cleaned up.
- Fetch **failure** (offline) → keep last-known override untouched. Fetch **success** with missing/malformed config or no match → clear overrides (fall back to local).
- Re-assert interval: 6h (counters the 72h backstop while the override is active; spec §4.4).

## Global Constraints

- Conventional Commits; DO NOT add Claude as co-author. Tests: `./gradlew :composeApp:desktopTest`. Compile: `:composeApp:compileDebugKotlinAndroid`, `:composeApp:compileKotlinDesktop`. iOS verified by CI/Xcode.
- Safe-by-default: any parse/fetch problem must produce **no behavior change** beyond falling back to local values; sample rates always coerced 0–100.
- `MockRemoteConfigRepository` (commonTest) implements the interface — new interface method needs an override there.

---

### Task 1: Config DTOs + pure resolver (`RemoteDiagnostics.kt`) with tests

**Files:** Create `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/logging/RemoteDiagnostics.kt`; Test `composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/logging/RemoteDiagnosticsTest.kt`.

**Produces:** `DiagnosticsConfig` / `DiagnosticsOverride` / `DiagnosticsMatch` (kotlinx-serialization), `ResolvedDiagnostics(sampleRate: Float?, diagnosticLevel: DiagnosticLevel?)`, `parseDiagnosticsConfig(jsonString: String?): DiagnosticsConfig?`, `resolveDiagnostics(config, rcUserId, nowEpochSeconds): ResolvedDiagnostics`, `resolveRemoteLevelOverride(remoteName: String?, remoteExpiresAtEpochSeconds: Long?, nowEpochSeconds: Long): DiagnosticLevel?`.

- [x] Test cases: valid config parses; blank/malformed/unknown-version → null; match by rc_user_id wins with coerced rates and parsed level; unknown level name → null level; expired/malformed `expires_at` entry ignored; first-match-wins; no match → default_sample_rate + null level; null config/user → (null, null); `resolveRemoteLevelOverride` honors expiry.
- [x] Run test → RED → implement → GREEN.
- [x] Commit: `feat(logging): add remote diagnostics config parsing and resolver`

### Task 2: Persisted overrides + effective-value plumbing

**Files:** Modify `data/storage/DebugPreferences.kt` (key `remote_datadog_sample_rate_override`, `DebugSettings.remoteDatadogSampleRateOverride: Float? = null` appended last, `setRemoteDatadogSampleRateOverride(rate: Float?)`); `util/logging/LoggingPreferences.kt` (keys `remote_diagnostic_level_override`, `remote_diagnostic_level_expires_at`; `setRemoteDiagnosticLevelOverride(level: DiagnosticLevel?)` stamping now+72h; `getDiagnosticSettings()` overlays `resolveRemoteLevelOverride(...) ?: userLevel`); `analytics/DatadogConfig.android.kt` + `.ios.kt` observers (`newRate = (settings.remoteDatadogSampleRateOverride ?: settings.datadogSampleRate).coerceIn(0f,100f)`).

- [x] Implement; compile Android + desktop; full desktopTest green (existing DiagnosticLevel/VerboseExpiry tests must not regress).
- [x] Commit: `feat(logging): apply remote sample-rate and diagnostic-level overrides`

### Task 3: Remote Config read + controller + startup wiring

**Files:** Modify `data/repository/RemoteConfigRepository.kt` + `Impl` (`getDiagnosticsConfigJson(): String?`, key `diagnostics_config`, default `""` in `setDefaults`); `commonTest/.../MockRemoteConfigRepository.kt` (override → null). Create `util/logging/RemoteDiagnosticsController.kt` (observe `UserContext.revenueCatUserId` via `collectLatest`; first refresh force=true; 6h re-assert loop; fetch-failure keeps state; writes both overrides). Modify `di/AppModule.kt` (`single { RemoteDiagnosticsController(get(), get(), get()) }`), `MainApplication.kt` (start after `DiagnosticLevelController.start`), `MainViewController.kt` (same, iOS).

- [x] Implement; compile Android + desktop; desktopTest green.
- [x] Commit: `feat(logging): remotely control per-user diagnostics via Firebase Remote Config`

### Ops (post-merge, from spec §6)
Publish `diagnostics_config` in Firebase console, e.g. `{"version":1,"default_sample_rate":100,"overrides":[{"match":{"rc_user_id":"$RCAnonymousID:..."},"sample_rate":100,"diagnostic_level":"VERBOSE","expires_at":"2026-08-01T00:00:00Z"}]}` — device picks it up on next launch (force-refresh) or ≤6h re-assert; remove the entry (or let it expire) to revert.
