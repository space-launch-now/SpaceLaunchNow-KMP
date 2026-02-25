# Research: Fix V5 Notification Filter Bug

**Date**: 2026-02-20 | **Spec**: fix_ios.md  
**Scope**: iOS-only (Kotlin commonMain + androidMain changes handled in separate Android branch)

---

## R0: Scope Split — Android vs iOS branches

**Decision**: This plan covers iOS-only changes. The Android V5 filter fix (Kotlin `commonMain` + `androidMain`) is handled in a separate branch.

**Rationale**:
1. The Kotlin changes (`NotificationData.fromMap()` lsp_id fallback, `V5FilterPreferences.kt` deletion, `V5NotificationFilter.kt` deletion, `NotificationWorker.kt` unification) are Android-focused and can be validated independently.
2. The iOS changes (Swift file deletions, NSE simplification) are independent and can proceed in parallel.
3. This branch should be rebased on the Android branch before merge (or merged after it lands).

**Impact on this plan**: All Kotlin file changes (T001-T006 in the original plan) are removed. This plan only covers Swift file deletions (Phase 1), NSE simplification (Phase 2), and iOS verification (Phase 3).

---

## R1: Why are V5 notifications not being filtered on iOS?

**Decision**: Two independent root causes identified.

**Finding 1 — iOS Kotlin bridge never routes V5 payloads to V5 filter:**
- `IosNotificationBridge.shouldShowNotification()` calls `NotificationFilter.shouldShowFromMap()` — the V4 filter.
- V4 filter expects `agency_id` key; V5 payloads send `lsp_id` instead. `NotificationData.fromMap()` returns `null` when `agency_id` is missing.
- When `fromMap()` returns null, `shouldShowFromMap()` returns `false` (suppresses). But `AppDelegate.swift` also implements its own `shouldShowNotification()` wrapper with a fallback that allows the notification through on parse failure.
- **Net effect**: V5 notifications bypass all filtering on iOS.

**Finding 2 — Swift NSE uses V5FilterPreferences but main app never writes them:**
- `NotificationService.swift` (NSE) reads `V5FilterPreferences` from App Groups via `V5PreferencesSyncManager`.
- The main app never calls `V5PreferencesSyncManager.savePreferences()`.
- NSE always loads `V5FilterPreferences.default` → `enableNotifications: true`, all filter sets `nil` (follow all) → allows everything.

**Alternatives Considered**:
- Fix V5FilterPreferences sync: Would work but maintains dual type system (String/Int) and dual filter paths.
- Fix IosNotificationBridge to detect V5 and call V5NotificationFilter: Would work but still has the Int-vs-String type mismatch between V5FilterPreferences (Int) and NotificationState (String).

---

## R2: Should we keep the V5FilterPreferences (Int-based) system or reuse NotificationState (String-based)?

**Decision**: Reuse NotificationState (String-based). Eliminate V5FilterPreferences.

**Rationale**:
1. Server sends String IDs (`"121"`, `"12"`). NotificationState already stores String IDs. Type conversion is unnecessary overhead and a bug source.
2. NotificationState is already persisted to iOS App Groups via DataStore (`DataStoreProvider.ios.kt` uses `group.me.spacelaunchnow.spacelaunchnow`). No new sync mechanism needed.
3. V4 NotificationFilter already handles grouped locations (additionalIds), strict/flexible matching, followAllLaunches bypass, and webcast-only filtering. Re-implementing all this in V5NotificationFilter is duplication.
4. The UI settings screens already work with String IDs through NotificationState. V5FilterPreferences adds a second state system that must be kept in sync manually.

**Alternatives Considered**:
- Keep V5FilterPreferences, fix Int sync: Rejected — maintains two parallel state/filter systems (+1000 lines), same data just different types.
- Keep V5 filter, convert NotificationState to Int: Rejected — breaks V4 compatibility, requires migration.

---

## R3: What happens to the Swift NSE (NotificationServiceExtension)?

**Decision**: Keep the NSE and implement a lightweight Swift-native filter that reads preferences from shared UserDefaults.

**Rationale**:
1. The NSE is the **only** code that reliably runs when the app is killed/terminated. `didReceiveRemoteNotification` is NOT guaranteed to fire when the app is not running.
2. Without NSE filtering, users see notifications they opted out of whenever the app isn't running.
3. The NSE runs in a separate process — it can't call Kotlin code (`IosNotificationBridge`, `NotificationFilter`).
4. DataStore writes `.preferences_pb` (protobuf) which Swift can't easily read.
5. **Solution**: Kotlin writes filter preferences to `UserDefaults(suiteName: "group.me.spacelaunchnow.spacelaunchnow")` whenever `NotificationState` changes. The NSE reads these UserDefaults and applies a simple Swift filter.

**UserDefaults bridge approach chosen over JSON sidecar** because:
- UserDefaults is the standard Apple mechanism for App Group data sharing between app and extensions
- No custom serialization/deserialization needed
- Atomic reads/writes with `synchronize()`
- Well-tested, zero external dependencies

**ID Expansion at write time**: Subscribed IDs are expanded to include `additionalIds` from `NotificationAgency` and `NotificationLocation` when written to UserDefaults. E.g., Russia ("111") expands to ["111", "96", "193", "63"]. This keeps the NSE filter trivial — just `Set.contains()` with no lookup tables needed.

**Fresh install fallback**: If UserDefaults has never been written, defaults are `enableNotifications: true, followAllLaunches: true` → allow everything through. Safe because users haven't configured filters yet.

**Alternatives Considered**:
- NSE allows-all, Kotlin bridge handles filtering in AppDelegate: Rejected — AppDelegate doesn't reliably run when app is killed. Users see unwanted notifications.
- JSON sidecar file in App Group container: Viable but UserDefaults is simpler and more standard.
- Delete NSE entirely: Rejected — NSE provides both content enrichment (image attachment) and now client-side filtering.

---

## R4: Can V4 NotificationFilter handle V5 payloads as-is?

**Decision**: Nearly — needs one small change. V5 payloads use `lsp_id` instead of `agency_id`. The V4 `NotificationData.fromMap()` must be updated to check both keys.

**Finding**:
- V4 payload: `{"agency_id": "121", "location_id": "12", ...}`
- V5 payload: `{"lsp_id": "121", "location_id": "12", ...}`
- Both use String values. The only difference for filtering is the key name.
- `NotificationData.fromMap()` currently does: `agencyId = data["agency_id"] ?: return null`
- Fix: `agencyId = data["agency_id"] ?: data["lsp_id"] ?: return null`
- Location ID key is the same in both: `"location_id"`.

**Alternatives Considered**:
- Create adapter that converts V5 maps to V4 format before parsing: Over-engineering for a single key rename.

---

## R5: What about the extra V5 filter categories (program, orbit, mission type, launcher family)?

**Decision**: These are not currently used in the UI and have no user-facing settings. Drop them for now; the simplified approach only filters on agency/LSP and location (matching the existing settings UI).

**Rationale**:
1. `NotificationState` has `subscribedAgencies` and `subscribedLocations` — no fields for programs, orbits, etc.
2. The settings UI has toggles for agencies and locations — no UI for programs, orbits, etc.
3. The spec explicitly states: "Additional filter categories beyond LSP and Location" are out of scope.
4. V5FilterPreferences has these fields but they're always `null` (default = follow all) since no code sets them.

**Alternatives Considered**:
- Keep V5 filter categories in code but unused: Dead code, violates YAGNI. Can re-add when UI supports them.

---

## R6: What about the Android NotificationWorker V5 path?

**Decision**: Update Android `NotificationWorker` to use unified V4 filter for both V4 and V5 payloads.

**Rationale**:
1. Current Android path: V5 → `V5NotificationPayload.fromMap()` (converts to Int) → `V5NotificationFilter.shouldShow()` with `V5FilterPreferences`.
2. `V5FilterPreferences` on Android is `NotificationState.v5Preferences` which is never explicitly configured by users and stays at defaults (allow all).
3. After fix: V5 → `NotificationData.fromMap()` (String passthrough) → `NotificationFilter.shouldShowNotification()` with `NotificationState` → uses same agency/location filters the user actually configured.

---

## R7: Different App Group identifiers?

**Decision**: This is a known inconsistency to address.

**Finding**:
- Kotlin DataStore uses: `group.me.spacelaunchnow.spacelaunchnow`
- Swift NSE/SyncManager uses: `group.me.calebjones.spacelaunchnow`
- These are DIFFERENT App Groups. The NSE can't read what Kotlin writes.

**Impact**: Even if we fix the filter logic, the NSE would read from a different container. For Phase 1, this is fine because the NSE will allow-all (Kotlin bridge handles filtering). For Phase 2 (NSE filtering), the App Group IDs must be unified.
