# Tasks: Platform-Specific Welcome Dialog

**Input**: Design documents from `/specs/004-fix-subscription-trial-disclosure/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to
- Exact file paths are included in descriptions

---

## Phase 1: Setup (No prerequisites)

**Purpose**: Confirm the single target file and imports are understood before modifying

- [X] T001 Verify `BetaWarningDialog.kt` is located at `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/BetaWarningDialog.kt`
- [X] T002 Confirm `PlatformType` enum and `getPlatform()` are available in `me.calebjones.spacelaunchnow.Platform`
- [X] T003 Confirm `SpaceLaunchNowPreviewTheme` is importable from `me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme`

---

## Phase 2: User Story 1 — Platform-Aware Description Text (Priority: P1) 🎯 MVP

**Goal**: Replace hardcoded "restrictions from Google" text with platform-appropriate copy using `when (platformType)`

**Independent Test**: Build and run on Android — dialog should say "Google". Build and run on iOS — dialog should say "Apple".

### Implementation for User Story 1

- [X] T004 [US1] Add `getPlatform`, `PlatformType` imports and replace hardcoded `description` list with `when (platformType)` expression in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/BetaWarningDialog.kt`
- [X] T005 [US1] Improve heading and subheading copy (warmer tone, forward-looking) in `BetaWarningDialog.kt`

**Checkpoint**: Dialog shows Google text on Android, Apple text on iOS, generic text on Desktop

---

## Phase 3: User Story 2 — Preview Compliance (Priority: P1)

**Goal**: Bring `BetaWarningDialog` up to Constitution Principle III — dual light + dark previews using `SpaceLaunchNowPreviewTheme`

**Independent Test**: Open `BetaWarningDialog.kt` in Android Studio / IntelliJ — both previews appear, one light and one dark.

### Implementation for User Story 2

- [X] T006 [US2] Replace single `MaterialTheme` preview with dual `SpaceLaunchNowPreviewTheme` light + dark previews in `BetaWarningDialog.kt`, following naming pattern `BetaWarningDialogPreview` / `BetaWarningDialogDarkPreview`

**Checkpoint**: Two previews visible — light and dark theme variants

---

## Phase 4: Polish

**Purpose**: Final validation

- [X] T007 Build Desktop target and verify dialog copy does not mention Google or Apple
- [X] T008 Verify `"beta_warning_shown"` DataStore preference key is unchanged (backward compat for existing users)

---

## Dependencies & Execution Order

- **Phase 1 (Setup)**: No dependencies — already verified
- **Phase 2 (US1)**: Depends on Phase 1. T004 before T005.
- **Phase 3 (US2)**: Can run in parallel with Phase 2 (different lines in same file — but to avoid conflicts, run after Phase 2)
- **Phase 4 (Polish)**: Depends on Phase 2 + 3 completion

---

## Notes

- All changes are in a single file: `BetaWarningDialog.kt`
- No new files needed
- Preference key `"beta_warning_shown"` MUST NOT change
- Commit message: `fix(ui): make welcome dialog text platform-specific`
