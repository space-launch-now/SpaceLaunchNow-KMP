# Feature Spec: Onboarding Paywall

**Branch**: `006-onboarding-paywall` | **Priority**: P0 (Highest Impact)  
**Origin**: [Subscriber Adoption Plan](../business/plan.md) — Root Cause #1: No Onboarding Conversion Moment

## Problem Statement

Zero premium introduction occurs during first-run. 3,892 new users in 28 days start using the free app with no exposure to premium features or subscription options. The highest-intent moment (app install) is completely wasted. Industry benchmarks show subscription apps convert 2–5% of new users *during onboarding* with a trial offer — SpaceLaunchNow converts 0.14%.

## Goal

Replace the current `BetaWarningDialog` with a **visually stunning** full-screen onboarding experience that serves as the app's start destination for new users. This screen combines the welcome message with a premium paywall, capturing the highest-intent moment (first launch) for conversion.

**This screen is the single most important conversion surface in the app.** Industry data shows 40–60% of subscription conversions happen at onboarding. The current 0.14% conversion rate means nearly every new user leaves without even seeing premium. This screen must be beautiful enough that users *want* to engage with it — not dismiss it reflexively.

The onboarding screen should:

1. **Make a stunning first impression** — rich gradients, polished typography, smooth animations, and space-themed visual identity that establishes the app's quality bar from the very first second
2. Welcome the user with a brief intro to the app (replacing `BetaWarningDialog`)
3. Highlight premium perks with visually compelling perk cards (Ad-Free, Premium Widgets, Calendar Sync, Themes)
4. Present subscription options with clear visual hierarchy (Annual as hero/recommended, Monthly, Lifetime)
5. Include a prominent "Continue" / "Maybe Later" dismiss that navigates to Home
6. Show only once per install (persisted via `AppPreferences` DataStore)
7. Never show to users who are already subscribed or have previously completed onboarding

## User Stories

### US-1: New User Sees Full-Screen Onboarding with Paywall

**As a** new user installing SpaceLaunchNow for the first time,  
**I want to** land on a full-screen onboarding experience with a welcome message and premium overview,  
**So that** I am introduced to the app and aware of premium options at my highest-intent moment.

**Acceptance Criteria:**
- Onboarding screen is the `startDestination` for first-time users (replaces `BetaWarningDialog`)
- Screen includes a welcome/intro section (app icon, welcome text, brief app description)
- Screen shows premium perks with icons and descriptions
- Screen shows subscription pricing cards (Annual as "Best Value", Monthly, Lifetime)
- "Continue" / "Maybe Later" button navigates to Home and marks onboarding complete
- Onboarding never appears again after completion (DataStore flag)
- User cannot navigate back to the onboarding screen once dismissed

### US-2: Existing/Returning User Skips Onboarding

**As a** returning user who has already completed onboarding (or has a subscription),  
**I want to** land directly on the Home screen,  
**So that** my experience is not interrupted.

**Acceptance Criteria:**
- If `onboardingCompleted == true` in DataStore, `startDestination` is Home
- If user has any active subscription (Premium, Lifetime, or Legacy), onboarding is skipped
- BetaWarningDialog is removed — the onboarding screen replaces it entirely

### US-3: User Can Purchase from Onboarding

**As a** new user viewing the onboarding screen,  
**I want to** subscribe directly from the onboarding screen,  
**So that** I can unlock premium features immediately without navigating elsewhere.

**Acceptance Criteria:**
- Tapping a pricing card initiates the purchase flow via `SubscriptionViewModel.purchaseProduct()`
- Success marks onboarding complete and navigates to Home with subscription active
- Failure shows inline error (same pattern as SupportUsScreen)

## Non-Functional Requirements

- **Visual Design Excellence**: This is the app's conversion-critical first impression. The screen must be polished and beautiful:
  - Rich gradient backgrounds (space-themed — deep blues, purples, starfield feel)
  - Smooth entrance animations (fade-in, slide-up for content sections)
  - Polished typography hierarchy — large welcome heading, medium perk titles, body descriptions
  - Perk cards with gradient icon backgrounds and clear visual rhythm
  - Pricing cards with strong visual hierarchy — Annual card elevated/highlighted as recommended
  - Generous whitespace and padding — premium feel, not cramped
  - The overall impression should communicate "this app is worth paying for" before the user even reads the text
- **Performance**: Onboarding screen must render within 300ms of app launch
- **Accessibility**: All perk icons have content descriptions; dual light/dark previews required
- **Platform**: Must work on Android, iOS, and Desktop (common code only)
- **No Hard Gate**: User must always be able to dismiss and proceed to the free app

## Out of Scope

- Free trial configuration on RevenueCat products (separate initiative)
- A/B testing or onboarding variant experimentation
- Analytics event tracking (can be added in a follow-up)
- Multi-page onboarding carousel (single scrollable screen for MVP)

## Technical Approach

1. Add `Onboarding` route to `Screen.kt`
2. Add `ONBOARDING_COMPLETED` preference flag to `AppPreferences`
3. Create `OnboardingScreen` composable — full-screen with welcome section + paywall
4. Conditionally set `startDestination` in `App.kt` NavHost: `Onboarding` for first-time users, `Home` for returning users
5. On dismiss/purchase, navigate to `Home` with `popUpTo(Onboarding) { inclusive = true }` so back button doesn't return to onboarding
6. Remove `BetaWarningDialog()` call from `App.kt` — the onboarding screen replaces it
7. Reuse `SubscriptionViewModel` for product loading and purchase flow
8. Reuse existing perk/pricing card patterns from `SupportUsScreen`
