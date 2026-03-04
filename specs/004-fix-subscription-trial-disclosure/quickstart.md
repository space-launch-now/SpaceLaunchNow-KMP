# Quickstart: Platform-Specific Welcome Dialog

**Date**: 2026-03-03 | **Plan**: [plan.md](plan.md)

## What This Changes

The first-run welcome dialog (`BetaWarningDialog`) currently shows Android/Google-specific text on all platforms. This change:

1. **Makes text platform-aware** — Android mentions Google, iOS mentions Apple, Desktop uses generic language
2. **Improves the copy** — Warmer, more forward-looking tone instead of technical/apologetic
3. **Adds dark preview** — Brings the component up to project standards per Constitution Principle III

## File to Modify

**Single file**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/compose/BetaWarningDialog.kt`

## Implementation Steps

### Step 1: Add Platform Import

Add `import me.calebjones.spacelaunchnow.getPlatform` to the imports section.

### Step 2: Get Platform Type in Composable

Inside the `BetaWarningDialog` composable, add:
```kotlin
val platformType = getPlatform().type
```

### Step 3: Make Description Text Platform-Specific

Replace the hardcoded `description` list with a `when (platformType)` expression:

```kotlin
val description = buildList {
    add(when (platformType) {
        PlatformType.ANDROID -> "The original app has been completely rebuilt to meet modern guidelines from Google and deliver a better experience."
        PlatformType.IOS -> "This app has been built from the ground up for iOS, bringing you the best space launch tracking experience on Apple devices."
        PlatformType.DESKTOP -> "This desktop version brings space launch tracking to your computer with a native experience."
    })
    add("")
    add("Features are actively being developed and released frequently. Check the Roadmap in Settings to see what's coming next.")
    add("")
    add("Thank you to everyone who has supported this project!")
}
```

### Step 4: Fix Preview (Dual Light + Dark)

Replace the single `MaterialTheme` preview with dual `SpaceLaunchNowPreviewTheme` previews:

```kotlin
@Preview
@Composable
private fun BetaWarningDialogPreview() {
    val mockPreferences = AppPreferences(/* mock DataStore */)
    SpaceLaunchNowPreviewTheme {
        BetaWarningDialog(appPreferences = mockPreferences)
    }
}

@Preview
@Composable
private fun BetaWarningDialogDarkPreview() {
    val mockPreferences = AppPreferences(/* mock DataStore */)
    SpaceLaunchNowPreviewTheme(isDark = true) {
        BetaWarningDialog(appPreferences = mockPreferences)
    }
}
```

## Verification

1. Run Android emulator — dialog should mention "Google"
2. Run iOS simulator — dialog should mention "Apple" / "iOS"
3. Run Desktop — dialog should use generic language
4. Verify both light and dark previews render correctly
5. Verify existing users who already dismissed the dialog are NOT shown it again (preference key unchanged)

## Commit Message

```
fix(ui): make welcome dialog text platform-specific

The BetaWarningDialog showed Android/Google-specific text on all
platforms. Updated to use PlatformType for platform-appropriate
messaging and improved copy tone. Added dark preview per
project conventions.
```
