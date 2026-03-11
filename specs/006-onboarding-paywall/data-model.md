# Data Model: Onboarding Screen

## Existing Entities (No Changes)

### SubscriptionState
**Location**: `data/model/SubscriptionState.kt`

| Field | Type | Description |
|-------|------|-------------|
| isSubscribed | Boolean | User has active subscription |
| subscriptionType | SubscriptionType | FREE, LEGACY, PREMIUM, LIFETIME |
| features | Set\<PremiumFeature\> | Unlocked features |

### ProductInfo  
**Location**: `ui/viewmodel/SubscriptionViewModel.kt`

| Field | Type | Description |
|-------|------|-------------|
| productId | String | Store product identifier |
| formattedPrice | String | Localized price string ("$9.99") |
| productType | ProductType | MONTHLY, ANNUAL, LIFETIME |
| hasFreeTrial | Boolean | Whether product has trial |
| freeTrialPeriodDisplay | String? | Human-readable trial period |

## Modified Entities

### AppPreferences
**Location**: `data/storage/AppPreferences.kt`

**New Fields Added**:

| Field | Type | Default | Description |
|-------|------|---------|-------------|
| ONBOARDING_COMPLETED | Boolean | false | Whether onboarding has been completed (replaces BETA_WARNING_SHOWN for new installs) |

**New Methods**:

| Method | Signature | Description |
|--------|-----------|-------------|
| onboardingCompletedFlow | `Flow<Boolean>` | Reactive stream of onboarding completion state |
| setOnboardingCompleted | `suspend fun(Boolean)` | Persist onboarding completed flag |

**Migration**: Existing users with `BETA_WARNING_SHOWN = true` automatically get `ONBOARDING_COMPLETED = true` via one-time migration in `App.kt`.

### Screen.kt (Navigation Routes)
**Location**: `navigation/Screen.kt`

**New Route**:
```kotlin
@Serializable
data object Onboarding
```

## Composable Contracts

### OnboardingScreen (Stateful — Navigation Entry Point)
**Location**: `ui/onboarding/OnboardingScreen.kt`

```kotlin
@Composable
fun OnboardingScreen(
    viewModel: SubscriptionViewModel = koinInject(),
    onComplete: () -> Unit
)
```

**Behavior**:
- Full-screen composable registered as navigation route
- Loads products via `SubscriptionViewModel`
- On "Continue" / "Maybe Later": calls `appPreferences.setOnboardingCompleted(true)` then `onComplete()`
- On purchase success: calls `appPreferences.setOnboardingCompleted(true)` then `onComplete()`

### OnboardingContent (Stateless — Previewable)
**Location**: `ui/onboarding/OnboardingScreen.kt`

```kotlin
@Composable
fun OnboardingContent(
    annualProduct: ProductInfo? = null,
    monthlyProduct: ProductInfo? = null,
    lifetimeProduct: ProductInfo? = null,
    savingsPercent: String? = null,
    isProcessing: Boolean = false,
    errorMessage: String? = null,
    onSubscribe: (ProductInfo) -> Unit = {},
    onDismiss: () -> Unit = {}
)
```

**Layout (top to bottom)** — designed for visual impact and conversion:
1. **Hero section**: Rich gradient background (space-themed deep blues/purples), AppIconBox with glow effect, "Welcome to Space Launch Now" large heading, warm subtitle — must create an immediate premium impression
2. **Perk cards (4)**: Premium Widgets, Ad-Free, Calendar Sync, Premium Themes — each with gradient icon circle, bold title, descriptive subtitle, slide-in animation
3. **Pricing section header**: "Unlock Premium" with subtle divider
4. **Pricing cards**: Annual (elevated card with "BEST VALUE" badge, primary container color, larger), Monthly (standard), Lifetime (standard) — clear visual hierarchy anchoring Annual
5. **"Continue" / "Maybe Later"** dismiss button — visible but secondary to pricing CTAs
6. Error message (animated visibility)

## App.kt Changes

### Conditional Start Destination
```kotlin
val onboardingCompleted by appPreferences.onboardingCompletedFlow.collectAsState(initial = true)

NavHost(
    navController = navController,
    startDestination = if (onboardingCompleted) Home else Onboarding,
)
```

### Migration from BetaWarningDialog
```kotlin
LaunchedEffect(Unit) {
    if (appPreferences.isBetaWarningShown()) {
        appPreferences.setOnboardingCompleted(true)
    }
}
```

### Onboarding Route Registration
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

### Removal
- Remove `BetaWarningDialog()` call
- Remove `import me.calebjones.spacelaunchnow.ui.compose.BetaWarningDialog`

## State Flow

```
App starts
  → Migration: if BETA_WARNING_SHOWN=true → set ONBOARDING_COMPLETED=true
  → Read ONBOARDING_COMPLETED from DataStore
    → If true (existing user): startDestination = Home → normal app
    → If false (new user): startDestination = Onboarding
      → OnboardingScreen renders (welcome + perks + pricing)
      → User taps "Continue" → setOnboardingCompleted(true) → navigate(Home) with popUpTo
      → User taps pricing card → purchaseProduct() → on success → setOnboardingCompleted(true) → navigate(Home) with popUpTo
      → User taps pricing card → on failure → show inline error, stay on screen
```
