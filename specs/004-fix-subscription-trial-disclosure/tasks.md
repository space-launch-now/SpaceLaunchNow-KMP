# Tasks: Fix Subscription Free Trial Disclosure

**Input**: Design documents from `/specs/004-fix-subscription-trial-disclosure/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/internal-contracts.md, quickstart.md

**Tests**: Not explicitly requested in spec — test tasks omitted

**Organization**: Tasks grouped by user story. US1 covers FR-1/FR-2/FR-3 (CRITICAL policy fix). US2 covers FR-4 (secondary trial status detection).

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2)
- Exact file paths included in descriptions

## Path Conventions

- **Common code**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`
- **Android**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/`
- **iOS**: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/`

---

## Phase 1: Setup

**Purpose**: No project initialization needed — existing KMP project. This phase verifies the foundation is ready.

- [X] T001 Verify RevenueCat SDK trial APIs are available by checking imports compile against `purchases-kmp` v1.9.0+14.3.0 in `gradle/libs.versions.toml`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Common model and utility changes that MUST be complete before any user story implementation

**⚠️ CRITICAL**: US1 and US2 both depend on these tasks

- [X] T002 [P] Create `PeriodFormatUtil.kt` with `Period.toDisplayString()` and `Period.toReadableString()` extension functions in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/PeriodFormatUtil.kt`
- [X] T003 [P] Add 7 trial/intro offer fields to `ProductInfo` data class (`hasFreeTrial`, `freeTrialPeriodDisplay`, `freeTrialPeriodValue`, `freeTrialPeriodUnit`, `hasIntroOffer`, `introOfferPrice`, `introOfferPeriodDisplay`) with backward-compatible defaults in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/ProductInfo.kt`

**Checkpoint**: Foundation ready — `ProductInfo` carries trial data, `PeriodFormatUtil` can format periods

---

## Phase 3: User Story 1 — Trial Terms Disclosure (Priority: P1) 🎯 MVP

**Goal**: Display free trial terms clearly on the subscription screen so users know: (1) a trial exists, (2) its duration, (3) post-trial cost, and (4) how to cancel. Resolves the Google Play policy violation.

**Independent Test**: Open subscription screen → yearly card shows "3-day free trial" badge, button says "Start Free Trial", fine print includes trial cancellation terms. Monthly card and lifetime card are unchanged.

### Implementation for User Story 1

- [X] T004 [P] [US1] Modify `AndroidBillingManager.getAvailableProducts()` to extract trial data from `StoreProduct.subscriptionOptions.freeTrial` and map `PricingPhase.billingPeriod` into `ProductInfo` trial fields using `PeriodFormatUtil.toDisplayString()` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/billing/AndroidBillingManager.kt`
- [X] T005 [P] [US1] Modify `IosBillingManager.getAvailableProducts()` to extract trial data from `StoreProduct.introductoryDiscount` when `paymentMode == DiscountPaymentMode.FREE_TRIAL` and map into `ProductInfo` trial fields using `PeriodFormatUtil.toDisplayString()` in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/data/billing/IosBillingManager.kt`
- [X] T006 [US1] Add `hasAnyTrialOffer` computed property to `SubscriptionUiState` in `SubscriptionViewModel` — derives from whether any loaded `ProductInfo` has `hasFreeTrial == true`. Pass trial info from yearly `ProductInfo` to UI state in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/SubscriptionViewModel.kt`
- [X] T007 [US1] Update `PricingCard` composable to accept new parameters (`hasFreeTrial: Boolean`, `freeTrialPeriodDisplay: String?`, `subscribeButtonText: String`). When `hasFreeTrial == true`: show trial badge text (e.g., "3-day free trial"), change button text to "Start Free Trial", show "then {price}/{period} after trial" text in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`
- [X] T008 [US1] Update `PricingCard` invocation for yearly plan (~line 381) to pass trial fields from `ProductInfo`. Monthly plan invocation (~line 404) should pass defaults (no trial). Lifetime card unchanged in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`
- [X] T009 [US1] Update `FinePrint` composable to accept `hasTrialOffer: Boolean`, `trialPeriodDisplay: String?`, `postTrialPrice: String?` parameters. When `hasTrialOffer == true`: add text "Free trial will automatically convert to a paid subscription at {postTrialPrice} unless canceled before the trial ends. You won't be charged if you cancel during the trial period." in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`
- [X] T010 [US1] Update `FinePrint` invocation to pass `hasTrialOffer` from `SubscriptionUiState.hasAnyTrialOffer`, `trialPeriodDisplay` from yearly `ProductInfo`, and `postTrialPrice` as formatted yearly price string in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`
- [X] T011 [US1] Add dual Compose Previews (light + dark) for `PricingCard` with trial state and `FinePrint` with trial text using `SpaceLaunchNowPreviewTheme()` and `SpaceLaunchNowPreviewTheme(isDark = true)` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`

**Checkpoint**: Subscription screen shows trial disclosure for yearly plan. Google Play policy violation resolved. This is the MVP — can submit updated build for policy re-review.

---

## Phase 4: User Story 2 — Trial Status Detection (Priority: P2)

**Goal**: Detect if the current user is in a trial period and show trial status in the app (e.g., "Free Trial - 2 days remaining" instead of "Free User" in CurrentPlanCard).

**Independent Test**: Subscribe to yearly plan with trial → subscription screen shows "Free Trial" status with remaining days. After trial converts: shows normal "Premium" status.

### Implementation for User Story 2

- [X] T012 [P] [US2] Add `isInTrialPeriod: Boolean = false` and `trialExpiresAt: Long? = null` fields to `SubscriptionState` data class in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/model/SubscriptionState.kt`
- [X] T013 [US2] Update `RevenueCatManager` (or relevant billing manager) to check `EntitlementInfo.periodType == PeriodType.TRIAL` and `EntitlementInfo.expirationDateMillis` when resolving subscription state, mapping values to the new `SubscriptionState` fields in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/RevenueCatManager.kt`
- [X] T014 [US2] Update `CurrentPlanCard` (or equivalent subscription status UI) to display "Free Trial" with remaining days when `SubscriptionState.isInTrialPeriod == true` instead of the default free/premium label in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`
- [X] T015 [US2] Add dual Compose Previews (light + dark) for `CurrentPlanCard` in trial state using `SpaceLaunchNowPreviewTheme()` and `SpaceLaunchNowPreviewTheme(isDark = true)` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/subscription/SupportUsScreen.kt`

**Checkpoint**: Users in a trial period see their trial status. Full feature is complete.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and build verification

- [X] T016 Build and verify Android debug APK compiles successfully via `./gradlew compileKotlinDesktop` and `./gradlew installDebug`
- [X] T017 Run quickstart.md verification checklist (yearly card shows trial, button says "Start Free Trial", fine print has trial terms, monthly/lifetime unchanged, light+dark previews render)
- [X] T018 [P] Verify no hardcoded trial durations exist — all trial data comes dynamically from RevenueCat SDK

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — verify immediately
- **Foundational (Phase 2)**: Depends on Phase 1 — BLOCKS all user stories
- **US1 (Phase 3)**: Depends on Phase 2 completion — CRITICAL MVP
- **US2 (Phase 4)**: Depends on Phase 2 completion — can proceed in parallel with US1 (different files for T012/T013) or after US1
- **Polish (Phase 5)**: Depends on Phase 3 completion (minimum) or Phase 3+4

### User Story Dependencies

- **US1 (P1)**: Depends only on Foundational (Phase 2). No dependency on US2. This is the MVP.
- **US2 (P2)**: Depends only on Foundational (Phase 2). Independent from US1 except shared `SupportUsScreen.kt` for UI tasks (T014/T015 should coordinate with T007-T011).

### Within Each User Story

- T004 and T005 are parallel (different platform files)
- T006 depends on T004/T005 (needs trial data flowing through)
- T007 must come before T008 (composable signature before invocation)
- T009 must come before T010 (composable signature before invocation)
- T011 depends on T007 and T009 (previews need final composable signatures)
- T012 and T013 are sequential (model then usage)
- T014 depends on T013 (needs trial state to display)
- T015 depends on T014 (preview needs final composable)

### Parallel Opportunities

- **Phase 2**: T002 and T003 can run in parallel (different files)
- **Phase 3**: T004 and T005 can run in parallel (Android vs iOS billing managers)
- **Phase 4**: T012 can run in parallel with Phase 3 tasks (different file)
- **Phase 5**: T018 can run in parallel with T016/T017

---

## Parallel Example: Phase 2 (Foundational)

```text
# Launch both foundational tasks together:
Task T002: "Create PeriodFormatUtil.kt in util/"
Task T003: "Add trial fields to ProductInfo.kt"
```

## Parallel Example: Phase 3 (US1 Platform Extraction)

```text
# Launch both platform billing tasks together:
Task T004: "Modify AndroidBillingManager.kt — extract trial from subscriptionOptions"
Task T005: "Modify IosBillingManager.kt — extract trial from introductoryDiscount"
```

---

## Implementation Strategy

### MVP First (US1 Only — Resolves Policy Violation)

1. Complete Phase 1: Verify SDK APIs (T001)
2. Complete Phase 2: ProductInfo + PeriodFormatUtil (T002-T003)
3. Complete Phase 3: US1 — Trial disclosure on subscription screen (T004-T011)
4. **STOP and VALIDATE**: Build APK, verify trial disclosure appears
5. Submit updated build to Google Play for policy re-review

### Incremental Delivery

1. Phase 1 + 2 → Foundation ready
2. Add US1 (Phase 3) → Test independently → **Submit to Google Play** (resolves violation)
3. Add US2 (Phase 4) → Test independently → Enhanced trial UX
4. Polish (Phase 5) → Final validation

### Commit Strategy

Per project conventions, use conventional commits:
- After Phase 2: `refactor(billing): add trial fields to ProductInfo and PeriodFormatUtil`
- After Phase 3: `fix(billing): add free trial disclosure to subscription UI`
- After Phase 4: `feat(billing): detect and display current trial period status`
