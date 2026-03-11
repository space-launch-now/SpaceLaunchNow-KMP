# Research: Onboarding Paywall

## Research Tasks

### R1: Navigation Architecture — Conditional Start Destination

**Task**: How to conditionally set `startDestination` based on first-run state

**Decision**: Read `AppPreferences` synchronously before `NavHost` renders, use result to set `startDestination`

**Rationale**: The `NavHost` requires `startDestination` at composition time. Since `AppPreferences` is backed by DataStore, we use `collectAsState(initial = ...)` with a safe default. The key decisions:

1. **Default to showing onboarding**: `startDestination` defaults to `Onboarding` while DataStore loads, then recomposes if actually `Home`. However, this causes a brief flash.
2. **Better: Gate on a loading state**: Show nothing (or splash) until DataStore emits, then render `NavHost` with the correct `startDestination`. This avoids flash.
3. **Best: Use `runBlocking` initial read**: Read the preference synchronously once on first composition. Since this is a single boolean read from local DataStore, it completes in <1ms and avoids both flash and loading states.

**Implementation approach**: In `App.kt`, before `NavHost`, read the onboarding state:
```kotlin
val appPreferences: AppPreferences = koinInject()
val onboardingCompleted by appPreferences.onboardingCompletedFlow.collectAsState(initial = true)
val subscriptionState by subscriptionViewModel.subscriptionState.collectAsState()

// Skip onboarding for existing users or subscribers
val startRoute = if (onboardingCompleted || subscriptionState.isSubscribed) Home else Onboarding
```

Using `initial = true` means the NavHost will default to `Home` while DataStore loads (safe default), and only route to `Onboarding` for genuinely new users once DataStore confirms `false`. This prevents existing users from ever flashing the onboarding screen.

**Alternatives considered**:
- Splash screen gate → Over-engineering for a single boolean check
- `LaunchedEffect` with delayed navigation → Causes visible flicker from Home→Onboarding
- Always start at Onboarding, immediately redirect → Back stack issues, flicker

### R2: BetaWarningDialog Migration

**Task**: How to safely remove BetaWarningDialog without breaking existing users

**Decision**: One-time migration in `App.kt` — if `BETA_WARNING_SHOWN == true`, auto-set `ONBOARDING_COMPLETED = true`

**Rationale**: Existing users who've already seen the welcome dialog should NOT see the onboarding screen. The BetaWarningDialog's DataStore flag (`BETA_WARNING_SHOWN`) indicates they are existing users. A `LaunchedEffect` migration sets `ONBOARDING_COMPLETED` based on this flag.

```kotlin
LaunchedEffect(Unit) {
    if (appPreferences.isBetaWarningShown()) {
        appPreferences.setOnboardingCompleted(true)
    }
}
```

This runs once and ensures existing installs skip the onboarding screen.

**Alternatives considered**:
- Keep both BetaWarningDialog AND onboarding → Confusing UX, two sequential dialogs
- Don't migrate, let existing users see onboarding → Bad UX for returning users
- Use version-based detection → DataStore flag is simpler and more reliable

### R3: Full-Screen Navigation Pattern

**Task**: How does the onboarding screen fit in the existing NavHost?

**Decision**: Add `@Serializable data object Onboarding` route + `composableWithCompositionLocal<Onboarding>` in NavHost

**Rationale**: All screens in the app use the `composableWithCompositionLocal<T>` pattern for navigation. The onboarding screen follows an identical pattern. On dismiss, it navigates to `Home` with `popUpTo(Onboarding) { inclusive = true }` to remove itself from the back stack.

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

### R4: UI Components to Reuse

**Task**: Identify reusable components from SupportUsScreen

**Decision**: Reuse patterns, create a simplified dedicated screen in `ui/onboarding/`

| Component | Source | Reuse Strategy |
|-----------|--------|---------------|
| `PremiumPerkCard` | `SupportUsScreen.kt` (private) | Define locally in OnboardingScreen (simplified) |
| `PricingCard` | `SupportUsScreen.kt` (private) | Define locally (simplified — no trial badge, no savings) |
| `AppIconBox` | `ui/components/AppIconBox.kt` | Direct import (public) |
| `SubscriptionViewModel` | `ui/viewmodel/SubscriptionViewModel.kt` | Inject via `koinInject()` |
| Product loading | `viewModel.getProductByType()` | Use directly |
| Purchase flow | `viewModel.purchaseProduct()` | Use directly |

**Key difference from SupportUsScreen**: The onboarding screen has a welcome/intro section at the top (replacing BetaWarningDialog content), is simpler (no restore purchases, no current plan card, no RevenueCat user ID), and has a prominent "Continue" dismiss instead of a back button.

### R5: DataStore Preference Pattern

**Task**: Best pattern for the onboarding completed flag

**Decision**: Follow exact `BETA_WARNING_SHOWN` pattern in `AppPreferences`

**Implementation**:
```kotlin
// In AppPreferences companion:
private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")

// Flow:
val onboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
    preferences[ONBOARDING_COMPLETED] ?: false
}

// Setter:
suspend fun setOnboardingCompleted(completed: Boolean) {
    dataStore.edit { preferences ->
        preferences[ONBOARDING_COMPLETED] = completed
    }
}
```

### R6: Preview Pattern

**Task**: How to create proper dual previews

**Decision**: Stateless `OnboardingContent` composable with dual previews

```kotlin
@Preview
@Composable
private fun OnboardingScreenPreview() {
    SpaceLaunchNowPreviewTheme {
        OnboardingContent(onDismiss = {}, onSubscribe = {})
    }
}

@Preview
@Composable
private fun OnboardingScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        OnboardingContent(onDismiss = {}, onSubscribe = {})
    }
}
```

## Summary of Decisions

| Topic | Decision |
|-------|----------|
| Screen type | Full-screen navigation route (not dialog overlay) |
| Start destination | Conditional: `Onboarding` for new users, `Home` for returning/subscribed |
| BetaWarningDialog | Removed and replaced; migration flag ensures existing users skip onboarding |
| Component reuse | Simplified PerkCard/PricingCard patterns, import AppIconBox |
| Subscription check | `SubscriptionViewModel.subscriptionState.isSubscribed` |
| Completion flag | `AppPreferences.ONBOARDING_COMPLETED` DataStore boolean |
| Preview approach | Stateless content composable + dual `SpaceLaunchNowPreviewTheme` previews |
| Navigation on dismiss | `navController.navigate(Home) { popUpTo<Onboarding> { inclusive = true } }` |
