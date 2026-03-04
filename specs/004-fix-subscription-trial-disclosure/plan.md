# Implementation Plan: Fix Subscription Free Trial Disclosure

**Branch**: `004-fix-subscription-trial-disclosure` | **Date**: 2026-03-03 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/004-fix-subscription-trial-disclosure/spec.md`

**Status**: CRITICAL — Google Play policy violation requiring immediate resolution

## Summary

Google Play flagged SpaceLaunchNow for violating the Subscriptions policy: the yearly plan ($9.99/year) has a 3-day free trial configured in Google Play/RevenueCat, but the app's subscription UI does not disclose trial terms (duration, post-trial cost, cancellation). Fix involves extracting trial data from RevenueCat's `StoreProduct.subscriptionOptions` (Android) / `StoreProduct.introductoryDiscount` (iOS), adding trial fields to `ProductInfo`, and updating `PricingCard` + `FinePrint` composables with required disclosures.

## Technical Context

**Language/Version**: Kotlin 2.x (KMP), Java 21 (JetBrains JDK)  
**Primary Dependencies**: RevenueCat purchases-kmp v1.9.0+14.3.0, Jetpack Compose Multiplatform, Koin DI  
**Storage**: N/A (no persistence changes — trial data is fetched live from RevenueCat)  
**Testing**: JUnit (commonTest/jvmTest), Compose Preview testing  
**Target Platform**: Android (primary, CRITICAL), iOS, Desktop (secondary)  
**Project Type**: Mobile (KMP multiplatform)  
**Performance Goals**: N/A (no perf-sensitive changes)  
**Constraints**: Must comply with Google Play Subscriptions policy (https://support.google.com/googleplay/android-developer/answer/9900533/)  
**Scale/Scope**: 5 files modified, 1 file created. Scoped to subscription screen only.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First (Android & iOS Equal) | ✅ PASS | Android extraction via `subscriptionOptions`, iOS via `introductoryDiscount`. Both platforms handled. |
| II. Pattern-Based Consistency | ✅ PASS | Uses existing `ProductInfo` model + `BillingManager` pattern. No new patterns introduced. |
| III. Accessibility & UX | ✅ PASS | Dual previews required for modified `PricingCard`/`FinePrint`. Trial info provides better UX. |
| IV. CI/CD & Conventional Commits | ✅ PASS | Commit: `fix(billing): add free trial disclosure to subscription UI` |
| V. Code Generation & API Management | ✅ PASS | No generated code changes. |
| VI. Multiplatform Architecture | ✅ PASS | Common model (`ProductInfo`), platform-specific extraction (`AndroidBillingManager`/`IosBillingManager`). |
| VII. Testing Standards | ✅ PASS | Unit tests for `PeriodFormatUtil`, integration tests for trial data extraction. |

**Post-Phase 1 Re-check**: All gates still pass. No violations or complexity escalation.

## Project Structure

### Documentation (this feature)

```text
specs/004-fix-subscription-trial-disclosure/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0: RevenueCat SDK API research
├── data-model.md        # Phase 1: ProductInfo model changes
├── quickstart.md        # Phase 1: Implementation quickstart
├── contracts/           # Phase 1: Internal component contracts
│   └── internal-contracts.md
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Source Code (modified files)

```text
composeApp/src/
├── commonMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── data/
│   │   └── model/
│   │       ├── ProductInfo.kt              # MODIFY: Add trial fields
│   │       └── SubscriptionState.kt        # MODIFY: Add isInTrialPeriod
│   ├── ui/
│   │   ├── subscription/
│   │   │   └── SupportUsScreen.kt          # MODIFY: PricingCard + FinePrint trial disclosure
│   │   └── viewmodel/
│   │       └── SubscriptionViewModel.kt    # MODIFY: Pass trial info to UI
│   └── util/
│       └── PeriodFormatUtil.kt             # CREATE: Period display formatting
├── androidMain/kotlin/me/calebjones/spacelaunchnow/
│   └── data/billing/
│       └── AndroidBillingManager.kt        # MODIFY: Extract trial from subscriptionOptions
└── iosMain/kotlin/me/calebjones/spacelaunchnow/
    └── data/billing/
        └── IosBillingManager.kt            # MODIFY: Extract trial from introductoryDiscount
```

**Structure Decision**: Kotlin Multiplatform mobile project. Common business logic + UI in `commonMain`, platform-specific billing extraction in `androidMain`/`iosMain`. No new modules or structural changes needed.

## Complexity Tracking

> No constitution violations. No complexity justification needed.
