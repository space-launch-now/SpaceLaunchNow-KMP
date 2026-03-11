# Quickstart: Onboarding Screen

## What This Feature Does

Replaces the `BetaWarningDialog` with a full-screen onboarding experience that serves as the app's start destination for new users. Combines a welcome intro with a premium paywall showing perks and subscription pricing. Shown once per install, skipped for returning/subscribed users.

## Files Changed (3 modified, 1 new, 1 deleted)

### New File
- `composeApp/src/commonMain/kotlin/.../ui/onboarding/OnboardingScreen.kt` — Full-screen onboarding with welcome + paywall sections + dual previews

### Modified Files
- `composeApp/src/commonMain/kotlin/.../data/storage/AppPreferences.kt` — Add `ONBOARDING_COMPLETED` preference key, flow, and setter
- `composeApp/src/commonMain/kotlin/.../navigation/Screen.kt` — Add `@Serializable data object Onboarding`
- `composeApp/src/commonMain/kotlin/.../App.kt` — Conditional `startDestination`, migration from `BETA_WARNING_SHOWN`, `Onboarding` route registration, remove `BetaWarningDialog()` call

### Deleted File
- `composeApp/src/commonMain/kotlin/.../ui/compose/BetaWarningDialog.kt` — Replaced by OnboardingScreen

### Test File  
- `composeApp/src/commonTest/kotlin/.../ui/onboarding/OnboardingScreenTest.kt` — Unit tests for conditional routing and completion logic

## Implementation Steps

### Step 1: Add DataStore Preference
In `AppPreferences.kt`, add to companion object:
```kotlin
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
```

Add flow and setter (same pattern as `BETA_WARNING_SHOWN`):
```kotlin
val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
    preferences[ONBOARDING_COMPLETED] ?: false
}

suspend fun setOnboardingCompleted(completed: Boolean) {
    dataStore.edit { preferences ->
        preferences[ONBOARDING_COMPLETED] = completed
    }
}
```

### Step 2: Add Navigation Route
In `Screen.kt`:
```kotlin
@Serializable
data object Onboarding
```

### Step 3: Create OnboardingScreen.kt
In `ui/onboarding/OnboardingScreen.kt`:
- Stateful `OnboardingScreen(viewModel, onComplete)` — injects SubscriptionViewModel, calls `setOnboardingCompleted(true)` on dismiss/purchase
- Stateless `OnboardingContent(...)` — previewable, renders a visually polished welcome hero (gradient background, glow effects) + animated perk cards + pricing cards with visual hierarchy + "Continue" button

### Step 4: Wire Up in App.kt

**a) Conditional start destination:**
```kotlin
val onboardingCompleted by appPreferences.onboardingCompletedFlow.collectAsState(initial = true)

NavHost(
    navController = navController,
    startDestination = if (onboardingCompleted) Home else Onboarding,
)
```

**b) Migration from BetaWarningDialog:**
```kotlin
LaunchedEffect(Unit) {
    if (appPreferences.isBetaWarningShown()) {
        appPreferences.setOnboardingCompleted(true)
    }
}
```

**c) Register route:**
```kotlin
composableWithCompositionLocal<Onboarding> {
    OnboardingScreen(
        onComplete = {
            navController.navigate(Home) {
                popUpTo<Onboarding> { inclusive = true }
            }
        }
    )
}
```

**d) Remove BetaWarningDialog:**
- Delete the `BetaWarningDialog()` call
- Remove the import

### Step 5: Add Dual Previews
```kotlin
@Preview @Composable private fun OnboardingScreenPreview() {
    SpaceLaunchNowPreviewTheme { OnboardingContent(onDismiss = {}, onSubscribe = {}) }
}
@Preview @Composable private fun OnboardingScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) { OnboardingContent(onDismiss = {}, onSubscribe = {}) }
}
```

## Testing

```bash
# Run all tests
./gradlew test

# Build desktop to verify compilation
./gradlew compileKotlinDesktop
```

## Verification Checklist

- [ ] New user (fresh install) → app starts on onboarding screen
- [ ] Onboarding shows welcome text + premium perks + pricing
- [ ] "Continue" navigates to Home, back button does NOT return to onboarding
- [ ] Returning user (BETA_WARNING_SHOWN=true) → app starts on Home (migration works)
- [ ] Returning user (ONBOARDING_COMPLETED=true) → app starts on Home
- [ ] Subscribed user → app starts on Home
- [ ] Purchase from onboarding works and navigates to Home
- [ ] Light + dark previews render correctly
- [ ] BetaWarningDialog.kt is deleted and no longer referenced
- [ ] No crashes on app restart after onboarding complete
