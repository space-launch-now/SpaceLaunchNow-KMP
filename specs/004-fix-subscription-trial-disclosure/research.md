# Research: Platform-Specific Welcome Dialog

**Date**: 2026-03-03 | **Plan**: [plan.md](plan.md)

## Research Tasks

### 1. How does the project handle platform-specific text?

**Decision**: Use the existing `PlatformType` enum with `when (platformType)` expressions in common code.

**Rationale**: The project already has a well-established pattern for platform-specific content:
- `PlatformType` enum in `Platform.kt` has `ANDROID`, `IOS`, `DESKTOP` values
- `getPlatform()` is an `expect fun` with platform-specific `actual` implementations
- `SupportUsScreen.kt` already uses `when (platformType)` to show "Google Play" vs "the App Store" vs "Desktop" text (lines 1089-1092, 1126-1143)
- This pattern keeps all logic in common code while producing platform-appropriate output

**Alternatives considered**:
- `expect`/`actual` for entire dialog text: Rejected — excessive code duplication for minor text differences
- String resource localization: Rejected — not yet used in the project, overkill for a single dialog
- Compile-time constants per source set: Rejected — pattern not established in project

### 2. What is the current welcome dialog text and what are its issues?

**Decision**: The dialog text needs improvement in three areas:
1. **Platform specificity**: Line 60 says "restrictions from Google" — irrelevant on iOS/Desktop
2. **Tone**: Text is overly technical and apologetic ("impossible to support", "technical issues")
3. **Preview compliance**: Missing dark preview, not using `SpaceLaunchNowPreviewTheme`

**Current text (line 60)**:
> "The original app's codebase is 10 years old, there were technical issues that made it impossible to support with new guidelines and restrictions from Google."

**Rationale**: Each platform should reference its own ecosystem:
- **Android**: "guidelines and restrictions from Google"
- **iOS**: "guidelines and restrictions from Apple"
- **Desktop**: "evolving platform requirements" (no specific store to reference)

### 3. What copy improvements would make the dialog more welcoming?

**Decision**: Shift the tone from technical/apologetic to warm/forward-looking while keeping the key information:
- Keep: "actively being developed", "frequent updates", "thank you"
- Improve: Remove technical jargon about the old codebase, focus on what's new
- Platform-specific: Reference the correct store/ecosystem per platform
- Keep: Roadmap mention (actionable information for users)

**Rationale**: First-run dialogs set the user's expectations. Technical details about why the app was rewritten are less important than what the user can expect now.

### 4. What preview pattern should the dialog follow?

**Decision**: Add dual previews (light + dark) using `SpaceLaunchNowPreviewTheme` wrapper.

**Rationale**: Constitution Principle III requires:
- Light preview: `@Preview @Composable private fun BetaWarningDialogPreview()` using `SpaceLaunchNowPreviewTheme()`
- Dark preview: `@Preview @Composable private fun BetaWarningDialogDarkPreview()` using `SpaceLaunchNowPreviewTheme(isDark = true)`
- Current dialog only has a single preview with bare `MaterialTheme`

### 5. Should the dialog be renamed?

**Decision**: Rename from `BetaWarningDialog` to `WelcomeDialog` (or keep as-is to minimize churn).

**Rationale**: The dialog is no longer a "beta warning" — it's a welcome/introduction message. However, renaming would require updating:
- `App.kt` import and call site (line 53, 255)
- `AppPreferences.kt` preference key (though the DataStore key string can stay for backward compat)
- Preview function names

A rename is a nice-to-have but not required. The preference key `"beta_warning_shown"` should NOT change to preserve existing user state.

**Alternatives considered**:
- Full rename to `WelcomeDialog`: Cleaner but more files touched
- Keep `BetaWarningDialog`: Minimal diff, preserves git blame — **selected for now**

## Summary of Resolved Items

| Item | Resolution |
|------|-----------|
| Platform text pattern | Use `when (platformType)` in common code |
| Copy improvements | Warmer tone, platform-specific store references |
| Preview compliance | Add dark preview with `SpaceLaunchNowPreviewTheme` |
| Dialog rename | Keep `BetaWarningDialog` name to minimize churn |
| Preference key | Keep `"beta_warning_shown"` for backward compatibility |
