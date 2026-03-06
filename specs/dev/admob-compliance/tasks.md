# Tasks: AdMob Policy Compliance Remediation

**Input**: Design documents from `/specs/dev/admob-compliance/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/ad-lifecycle-contract.md, quickstart.md

**Tests**: Not explicitly requested in the spec. Test tasks are omitted but verification steps are embedded in checkpoints.

**Organization**: Tasks are grouped by compliance finding (mapped to spec requirements CR-1 through CR-8). Each finding is treated as an independent user story that can be implemented and verified in isolation.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which compliance requirement this task addresses (e.g., CR1, CR2, CR3)
- Include exact file paths in descriptions

## Path Conventions

- **Common code**: `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/`
- **Android**: `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/`
- **iOS**: `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/`
- **Build config**: `composeApp/build.gradle.kts`
- **Manifest**: `composeApp/src/androidMain/AndroidManifest.xml`

---

## Phase 1: Setup

**Purpose**: Verify environment and document baseline state

- [X] T001 Verify `.env` file has all required ad unit ID keys present in `composeApp/build.gradle.kts` (lines 333–339)
- [X] T002 Verify `basic-ads` library version in `gradle/libs.versions.toml` and document current version for reference

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: No foundational blocking work needed — all fixes are isolated in-place modifications to existing files. User stories can proceed immediately after setup.

**Checkpoint**: Setup verified — compliance fix implementation can begin

---

## Phase 3: CR-1 — Production AdMob App ID in AndroidManifest (Priority: P1 — CRITICAL) 🎯

**Goal**: Replace hardcoded test AdMob App ID with environment-driven production App ID so release builds serve real ads

**Independent Test**: Build release APK, extract merged manifest, verify `com.google.android.gms.ads.APPLICATION_ID` value is NOT the test ID `ca-app-pub-3940256099942544~3347511713`

### Implementation

- [X] T003 [CR1] Add `ADMOB_APP_ID` property read from `.env` in `composeApp/build.gradle.kts` after the existing ad unit ID reads (~line 339)
- [X] T004 [CR1] Add `manifestPlaceholders["ADMOB_APP_ID"]` in defaultConfig block of `composeApp/build.gradle.kts` (~line 385, alongside existing `MAPS_API_KEY` placeholder)
- [X] T005 [CR1] Override `manifestPlaceholders["ADMOB_APP_ID"]` with test App ID in debug buildType of `composeApp/build.gradle.kts` (~line 422)
- [X] T006 [CR1] Replace hardcoded test App ID with `${ADMOB_APP_ID}` placeholder in `composeApp/src/androidMain/AndroidManifest.xml` (line 57)
- [X] T007 [CR1] Add `ADMOB_APP_ID` to CI/CD secrets documentation in `docs/cicd/REQUIRED_SECRETS.md`

**Checkpoint**: `./gradlew assembleDebug` succeeds with test App ID; release manifest would use `.env` value

---

## Phase 4: CR-3 — SDK Initialization on Main Thread (Priority: P1 — CRITICAL)

**Goal**: Move Ad SDK initialization from background thread to main thread per Google's threading contract

**Independent Test**: Run app, verify no crash and ad SDK initializes before any ad load requests in Logcat

### Implementation

- [X] T008 [CR3] Wrap the `AdInitializer.initialize()` and `AdInitializer.configure()` calls in `withContext(Dispatchers.Main)` inside the existing `LaunchedEffect` in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt` (~line 188–196)

**Checkpoint**: App launches without crash; Logcat shows ad SDK init on main thread

---

## Phase 5: CR-2 — Gate Ad Loading on Consent Resolution (Priority: P1 — CRITICAL)

**Goal**: Ensure no ad requests are made until UMP consent has been resolved (granted, denied, or not required)

**Independent Test**: Fresh install on GDPR-region device → consent dialog appears → no AdMob network requests logged before user responds

### Implementation

- [X] T009 [CR2] Add `onConsentResolved: (() -> Unit)?` parameter to the `AdConsentPopup` expect function in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdComposables.kt` (line 98)
- [X] T010 [P] [CR2] Update Android `AdConsentPopup` actual to call `onConsentResolved` when consent status resolves in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdSupport.android.kt` (~line 38–57)
- [X] T011 [P] [CR2] Update iOS `AdConsentPopup` actual to call `onConsentResolved` when consent status resolves in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdSupport.ios.kt` (~line 38–68)
- [X] T012 [P] [CR2] Update Desktop `AdConsentPopup` actual stub to immediately call `onConsentResolved` in `composeApp/src/desktopMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdSupport.desktop.kt`
- [X] T013 [CR2] Add `isConsentResolved` state and gate `WithPreloadedAds` on it in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/App.kt` (~lines 262 and 332): consent must resolve before `WithPreloadedAds` preloads ads, while NavHost content renders without ads in the interim

**Checkpoint**: Fresh install shows consent dialog; no ad network requests before consent resolution; after consent, ads load normally

---

## Phase 6: CR-4 — Unify Content Rating Configuration (Priority: P2 — MODERATE)

**Goal**: Make content rating, child-directed, and under-age settings identical on Android and iOS

**Independent Test**: Compare `RequestConfiguration` parameters in both platform implementations — must be identical

### Implementation

- [X] T014 [P] [CR4] Update `tagForChildDirectedTreatment` from `UNSPECIFIED` to `FALSE` and `tagForUnderAgeOfConsent` from `UNSPECIFIED` to `FALSE` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdInitializer.android.kt` (lines 40–41)
- [X] T015 [P] [CR4] Update `maxAdContentRating` from `MAX_AD_CONTENT_RATING_T` to `MAX_AD_CONTENT_RATING_PG` in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/AdInitializer.ios.kt` (line 37)

**Checkpoint**: Both platform `configure()` methods use identical `RequestConfiguration` values: PG, FALSE, FALSE

---

## Phase 7: CR-7 — Fix Duplicate Reward Granting (Priority: P2 — MODERATE)

**Goal**: Ensure rewarded ad grants exactly one reward per ad view by eliminating the unguarded duplicate callback path

**Independent Test**: Set breakpoint on `onRewardEarned`, watch a rewarded ad to completion, verify callback fires exactly once

### Implementation

- [X] T016 [P] [CR7] Add `rewardGranted` guard to `RewardedAd(onRewardEarned = {...})` callback and remove duplicate `onRewardEarned` call from `LaunchedEffect(AdState.SHOWN)` in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/RewardedAdHandler.android.kt` (lines 97–103 and 109–114)
- [X] T017 [P] [CR7] Apply identical duplicate-reward fix in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/RewardedAdHandler.ios.kt` (lines 97–103 and 109–114)

**Checkpoint**: Rewarded ad completes → `onRewardEarned` invoked exactly once on both platforms

---

## Phase 8: CR-5/M-2 — Fix Interstitial Comment Mismatch (Priority: P3 — MODERATE)

**Goal**: Correct misleading code comments that say "every 4th visit" when actual frequency is every 10th visit

**Independent Test**: Code review — comment matches `visitsBeforeInterstitial = 10` in GlobalAdManager

### Implementation

- [X] T018 [P] [CR5] Update KDoc comment from "every 4th detail view visit" to "every 10th detail view visit" in `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/InterstitialAdHandler.android.kt` (line 30)
- [X] T019 [P] [CR5] Update KDoc comment from "every 4th detail view visit" to "every 10th detail view visit" in `composeApp/src/iosMain/kotlin/me/calebjones/spacelaunchnow/ui/ads/InterstitialAdHandler.ios.kt` (line 27)

**Checkpoint**: Comments consistent with actual frequency capping value

---

## Phase 9: Polish & Cross-Cutting Concerns

**Purpose**: Verification, documentation, and final validation

- [X] T020 Build Android debug APK to verify all changes compile: `./gradlew assembleDebug`
- [ ] T021 Run existing tests to verify no regressions: `./gradlew test`
- [ ] T022 Run quickstart.md testing checklist (manual verification on device)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately
- **Foundational (Phase 2)**: No blocking work needed
- **CR-1 Manifest Fix (Phase 3)**: Depends on Setup only — purely build config changes
- **CR-3 Main Thread Init (Phase 4)**: Depends on Setup only — single file change in App.kt
- **CR-2 Consent Gate (Phase 5)**: Depends on Setup only — touches commonMain + all 3 platform actuals + App.kt
- **CR-4 Content Rating (Phase 6)**: Independent — only touches platform AdInitializer files
- **CR-7 Duplicate Reward (Phase 7)**: Independent — only touches platform RewardedAdHandler files
- **CR-5 Comment Fix (Phase 8)**: Independent — only touches platform InterstitialAdHandler files
- **Polish (Phase 9)**: Depends on ALL prior phases being complete

### User Story Dependencies

- **CR-1 (Phase 3)**: Independent — build config + manifest only
- **CR-3 (Phase 4)**: Independent — App.kt LaunchedEffect only
- **CR-2 (Phase 5)**: Independent — but touches App.kt (coordinate with CR-3 if both modify same LaunchedEffect)
- **CR-4 (Phase 6)**: Independent — AdInitializer platform files only
- **CR-7 (Phase 7)**: Independent — RewardedAdHandler platform files only
- **CR-5 (Phase 8)**: Independent — InterstitialAdHandler platform files only

### Within Each User Story

- Build config changes before manifest changes (CR-1)
- Expect declaration changes before actual implementations (CR-2: T009 → T010/T011/T012 → T013)
- Platform files marked [P] can be edited in parallel within a phase

### Parallel Opportunities

**Maximum Parallelism** (after Setup):
- CR-1 (T003–T007) can run in parallel with CR-3 (T008) and CR-4 (T014–T015) and CR-7 (T016–T017) and CR-5 (T018–T019)
- CR-2 (T009–T013) has internal sequencing (T009 first) but can run in parallel with CR-1, CR-4, CR-7, CR-5
- Within CR-2: T010, T011, T012 are parallelizable after T009 completes

---

## Parallel Example: After Setup

```
# All these can start simultaneously:
Task T003-T007: CR-1 Manifest fix (build config + manifest)
Task T008:      CR-3 Main thread init (App.kt)
Task T014-T015: CR-4 Content rating unification (AdInitializer files)
Task T016-T017: CR-7 Duplicate reward fix (RewardedAdHandler files)
Task T018-T019: CR-5 Comment fix (InterstitialAdHandler files)

# CR-2 has internal ordering:
Task T009:      First - update expect declaration
Task T010-T012: Then in parallel - update all 3 platform actuals
Task T013:      Last - gate WithPreloadedAds in App.kt
```

---

## Implementation Strategy

### MVP First (Critical Fixes Only)

1. Complete Phase 1: Setup
2. Complete Phase 3: CR-1 — Manifest App ID (prevents zero ad revenue)
3. Complete Phase 4: CR-3 — Main thread init (prevents SDK crashes)
4. Complete Phase 5: CR-2 — Consent gate (prevents GDPR violation)
5. **STOP and VALIDATE**: Build, run on device, verify all 3 critical fixes
6. Deploy fix release

### Incremental Delivery

1. Phases 3–5 (Critical) → Test → Release (compliance urgency)
2. Phase 6 (CR-4 Content Rating) → Test → Release
3. Phase 7 (CR-7 Duplicate Reward) → Test → Release
4. Phase 8 (CR-5 Comment Fix) → Commit alongside any phase

### Single Developer Sequential Strategy

1. T001–T002 (Setup)
2. T003–T007 (CR-1: Manifest)
3. T008 (CR-3: Main thread)
4. T009–T013 (CR-2: Consent gate)
5. T014–T015 (CR-4: Content rating)
6. T016–T017 (CR-7: Reward fix)
7. T018–T019 (CR-5: Comment fix)
8. T020–T022 (Polish: Build, test, verify)

---

## Notes

- [P] tasks = different files, no dependencies
- [CR#] label maps task to specific compliance requirement from spec.md
- Each CR phase is independently completable and verifiable
- CR-1 is highest priority — without it, release builds serve zero real ads
- CR-2 and CR-3 are next — GDPR liability and SDK stability
- Commit after each phase using conventional commits: `fix(ads): <description>`
- T013 modifies App.kt which is also touched by T008 — apply T008 first if doing sequentially
