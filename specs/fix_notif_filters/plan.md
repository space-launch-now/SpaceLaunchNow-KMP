# Implementation Plan: Fix V5 Notification Filter Bug

**Branch**: `fix_notif_filters` | **Date**: 2026-02-18 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/fix_notif_filters/spec.md`

**Note**: This template is filled in by the `/speckit.plan` command. See `.specify/templates/commands/plan.md` for the execution workflow.

## Summary

Fix critical bug where V5 notification filters are not working - users with custom filters (e.g., "SpaceX + Florida") receive ALL notifications regardless of settings. Root cause: overcomplicated Int-based filtering with type conversion and dual state systems. Solution: Simplify to String-based filtering matching V4's proven approach, reusing existing NotificationState. Reduces code by ~180 lines while fixing the bug.

## Technical Context

**Language/Version**: Kotlin 2.1.0 (Kotlin Multiplatform)  
**Primary Dependencies**: Kotlinx Serialization, Firebase Cloud Messaging, Koin DI  
**Storage**: DataStore (key-value persistence for NotificationState)  
**Testing**: Kotlin Test (JUnit-based), existing test infrastructure in `commonTest/`  
**Target Platform**: Android (primary), iOS (secondary via KMP)  
**Project Type**: Mobile application (KMP structure with commonMain/androidMain/iosMain)  
**Performance Goals**: Filter evaluation <10ms per notification  
**Constraints**: No breaking changes to existing V4 behavior, no user data migration, maintain backward compatibility  
**Scale/Scope**: Single bug fix affecting 2-3 files, add simplified test suite (~120 lines), remove complex code (~300 lines)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

### ✅ Pattern-Based Consistency (Principle II)
- **Requirement**: Follow established patterns (NotificationFilter, Result<T>, String-based IDs)
- **Status**: PASS - Solution follows V4's proven String-based filter pattern
- **Justification**: Reuses existing NotificationState model, maintains API extension pattern

### ✅ Code Generation & API Management (Principle V)
- **Requirement**: Use extension functions for clean API interfaces
- **Status**: PASS - V5NotificationPayload parsing uses standard fromMap() pattern
- **Justification**: No changes to generated API clients, maintains existing parse patterns

### ✅ Multiplatform Architecture (Principle VI)
- **Requirement**: Business logic in commonMain, avoid magic strings
- **Status**: PASS - All filtering logic in commonMain, uses String IDs (not magic strings, but server-provided identifiers)
- **Justification**: V5NotificationFilter in commonMain, String IDs match server contract

### ✅ Testing Standards (Principle VII)
- **Requirement**: Integration tests for critical paths, repository tests, ViewModel tests
- **Status**: PASS - Will include comprehensive test suite covering all filter scenarios
- **Justification**: V5NotificationFilterSimplifiedTest.kt with 7+ test cases covering exact user scenario

### ⚠️ Accessibility & UX (Principle III)
- **Requirement**: UI components must be accessible
- **Status**: N/A - This is a backend logic fix, no UI changes
- **Justification**: Settings UI already exists and works correctly with String IDs

**Overall Assessment**: ✅ PASS - All applicable constitution principles satisfied. This bug fix simplifies code while following established patterns.

## Project Structure

### Documentation (this feature)

```text
specs/fix_notif_filters/
├── spec.md              # Feature specification (completed)
├── plan.md              # This file (in progress)
├── research.md          # Phase 0 output (to be generated)
├── data-model.md        # Phase 1 output (to be generated)
├── quickstart.md        # Phase 1 output (to be generated)
└── contracts/           # Phase 1 output (empty - no API contracts)
```

### Source Code (repository root)

```text
composeApp/src/
├── commonMain/kotlin/me/calebjones/spacelaunchnow/
│   └── data/model/
│       ├── V5NotificationPayload.kt       # MODIFY: Change Int → String IDs
│       ├── V5NotificationFilter.kt        # MODIFY: Simplified filter logic
│       ├── NotificationState.kt           # READ ONLY: Existing V4 model
│       └── NotificationData.kt            # READ ONLY: V4 filter reference
│
├── commonTest/kotlin/me/calebjones/spacelaunchnow/
│   └── data/model/
│       └── V5NotificationFilterSimplifiedTest.kt  # CREATE: New test suite
│
└── androidMain/kotlin/me/calebjones/spacelaunchnow/
    └── workers/
        └── NotificationWorker.kt          # MODIFY: Pass NotificationState to filter
```

**Structure Decision**: Kotlin Multiplatform Mobile structure with commonMain for shared business logic. This bug fix modifies existing files in `data/model/` package and updates Android-specific WorkManager integration. No iOS changes needed as iOS uses the same commonMain filter logic.

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

**No violations** - Constitution check passed. This fix actually **reduces** complexity:

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| Lines of Code | ~800 | ~620 | -180 lines (-22%) |
| Data Models | 2 (NotificationState + V5FilterPreferences) | 1 (NotificationState only) | -1 model |
| Type Conversions | String→Int on every filter check | None | 0 conversions |
| Null Semantics | Complex (null = follow all) | Simple (boolean flag) | Eliminated |
| Sync Logic | ~300 lines (V4→V5 sync) | 0 lines | -300 lines |

**Simplification Justification**: Original V5 implementation over-engineered the problem by introducing type conversions and dual state systems. This fix returns to V4's proven approach while maintaining all functionality.

---

## Phase 0: Research & Analysis

### Research Questions

**Q1**: Why does V4 filtering work correctly while V5 filtering fails?  
**Answer**: V4 uses String IDs matching server format (`"121"` in  `Set<String>`), while V5 converts to Int (`121` in `Set<Int>?`) with complex null semantics. The conversion and sync Logic introduces bugs.

**Q2**: What is the exact data model sent by the server for V5 notifications?  
**Answer**: Server sends `Map<String, String>` with fields:
- `lsp_id`: String (e.g., "121" for SpaceX)
- `location_id`: String (e.g., "12" for Cape Canaveral)  
- `program_id`: String (e.g., "25" for Artemis)
All IDs are Strings, not Ints.

**Q3**: Can we reuse NotificationState (V4 model) for V5 filtering without breaking changes?  
**Answer**: YES. NotificationState already stores:
- `subscribedAgencies: Set<String>` (e.g., ["121", "44"])
- `subscribedLocations: Set<String>` (e.g., ["12", "27"])  
- `followAllLaunches: Boolean`
- `useStrictMatching: Boolean`

This is exactly what V5 needs. No migration required.

**Q4**: What filter logic patterns exist in the V4 implementation?  
**Answer**: V4 NotificationFilter.shouldShowNotification() follows this pattern:
1. Check master enable → Return false if disabled
2. Check webcast-only filter → Return false if enabled and no webcast
3. Check notification type (timing) → Return false if type disabled
4. Check follow all launches → Return true if enabled (bypass filtering)
5. Check if both filters empty → Return false (block everything)
6. Check agency match → `agencyId in state.subscribedAgencies`
7. Check location match → `locationId in state.subscribedLocations` (with wildcards)
8. Apply strict vs flexible → `(agency AND location)` vs `(agency OR location)`

**Q5**: What are the performance implications of String vs Int filtering?  
**Answer**: String filtering is actually FASTER:
- No String→Int conversion overhead (toIntOrNull() call)
- Set<String>.contains() is O(1) just like Set<Int>.contains()
- Fewer object allocations (no conversion results)
- Simpler code path = better CPU cache utilization

### Research Summary

**Key Finding**: V5 over-engineered the solution. Server sends String IDs, V4 uses String IDs successfully, but V5 introduced unnecessary Int conversion creating:
1. Type conversion bugs (`toIntOrNull()` failures)
2. Null semantic complexity (null = follow all, empty = block all)
3. Dual state systems requiring sync logic
4. More verbose code (~180 extra lines)

**Solution Approach**: Return to V4's proven String-based pattern:
- V5NotificationPayload: Keep IDs as String (match server format)
- V5NotificationFilter: Accept NotificationState (reuse V4 model)
- Filter logic: Simple String membership checks (no conversion, no sync)

**Decision Log**:
| Decision | Rationale | Alternative Rejected |
|----------|-----------|---------------------|
| Use String IDs | Matches server format, no conversion errors | Int IDs: requires parsing, adds complexity |
| Reuse NotificationState | Already works for V4, no migration needed | V5FilterPreferences: duplicate state, sync required |
| Simple membership check | Fast (O(1)), readable, proven in V4 | Complex null semantics: harder to debug, more bugs |

---

## Phase 1: Design & Contracts

**Status**: ✅ Completed

### Artifacts Generated

1. **data-model.md**: Complete data model documentation with:
   - V5NotificationPayload modifications (Int → String IDs)
   - V5NotificationFilter simplification 
   - NotificationWorker updates
   - State transition diagrams
   - Validation rules
   - No-migration-required justification

2. **contracts/README.md**: Documentation confirming:
   - No external API changes (client-side bug fix only)
   - Server payload format reference (all IDs are Strings)
   - No new contracts to define

3. **quickstart.md**: Step-by-step implementation guide with:
   - 6-step implementation process (~2-3 hours)
   - Code examples for each change
   - 7 comprehensive test cases
   - Manual testing procedure
   - Verification checklist
   - Rollback plan
   - Troubleshooting section

### Design Decisions

**Data Model**:
- V5NotificationPayload: String IDs matching server format (no conversion)
- V5NotificationFilter: Accepts NotificationState (reuse V4 model) 
- NotificationWorker: Pass full state instead of v5Preferences

**Filter Algorithm**:
- Copy V4's proven pattern with minimal changes
- Replace `agencyId` → `lspId`, keep all logic identical
- Simple String membership: `lspId in state.subscribedAgencies`

**Testing Strategy**:
- 7 core test cases covering exact user scenario (SpaceX + Florida)
- Test strict vs flexible matching modes
- Test follow all bypass
- Test master enable/disable
- Manual testing via Debug Settings screen

### Constitution Re-Check (Post-Design)

✅ **Pattern-Based Consistency**: Follows V4 NotificationFilter pattern exactly  
✅ **Testing Standards**: Comprehensive test suite covers all scenarios  
✅ **Multiplatform Architecture**: All logic in commonMain, no platform-specific code  
✅ **No Breaking Changes**: V4 behavior unchanged, V5 now works correctly

**Result**: PASS - Design follows all constitution principles

---

## Agent Context Update

**Status**: Ready to execute

Run the following command to update agent context with new knowledge from this feature:

```powershell
.\.specify\scripts\powershell\update-agent-context.ps1 -AgentType copilot
```

This will:
- Detect Copilot agent (GitHub Copilot instructions file)
- Add new technology/context: "V5 Notification Filtering with String-based IDs"
- Preserve existing manual additions
- Update agent-specific context file

---

## Phase 2: Task Breakdown

**Note**: Phase 2 planning (task breakdown) is handled by the `/speckit.tasks` command, NOT by `/speckit.plan`.

This plan document ends after Phase 1 design. To generate the task breakdown:

```bash
# Run after reviewing this plan
/speckit.tasks
```

Expected output: `specs/fix_notif_filters/tasks.md` with implementation tasks broken into:
- Core implementation tasks (V5NotificationPayload, V5NotificationFilter, NotificationWorker)
- Testing tasks (unit tests, integration tests, manual testing)
- Documentation tasks (update V5_SIMPLIFIED_SOLUTION.md)
- Verification tasks (CI/CD checks, performance benchmarks)

---

## Summary

**Planning Complete**: ✓ Spec, ✓ Research, ✓ Data Model, ✓ Quickstart

**Key Decisions**:
1. Use String IDs (match server format)
2. Reuse NotificationState (no V5FilterPreferences)
3. Copy V4 filter pattern (proven approach)
4. No user migration needed

**Next Steps**:
1. Review this plan document
2. Run agent context update: `.specify/scripts/powershell/update-agent-context.ps1 -AgentType copilot`
3. Generate task breakdown: `/speckit.tasks`
4. Begin implementation following `quickstart.md`

**Branch**: `fix_notif_filters`  
**Estimated Implementation Time**: 2-3 hours  
**Code Impact**: -180 lines (simplification)  
**Testing**: 7 comprehensive test cases  
**Risk Level**: Low (simplifies existing code, no breaking changes)
