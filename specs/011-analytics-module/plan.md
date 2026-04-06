# Implementation Plan: Analytics Module (Multi-Provider Plugin Architecture)

**Branch**: `011-analytics-module` | **Date**: 2026-04-05 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/011-analytics-module/spec.md`

## Summary

Create a unified, multi-provider analytics module with a plugin architecture. A common
`AnalyticsProvider` interface in `commonMain` is implemented per-platform for Firebase Analytics,
with a `ConsoleAnalyticsProvider` for debug/desktop. An `AnalyticsManager` dispatcher fans out typed
events (sealed class hierarchy) to all registered providers via Koin DI. Includes automatic screen
view tracking via NavController integration and per-provider consent management via DataStore
preferences.

## Technical Context

**Language/Version**: Kotlin 2.1.x (KMP), Java 21 (JetBrains JDK)
**Primary Dependencies**: Koin 4.x, Ktor, Jetpack Compose Multiplatform, GitLive Firebase KMP 2.4.0, Datadog KMP SDK
**Storage**: DataStore Preferences (consent flags only — no event persistence)
**Testing**: kotlin.test (commonTest), JUnit (jvmTest)
**Target Platform**: Android (primary), iOS, Desktop (JVM — no-op analytics)
**Project Type**: Mobile (Kotlin Multiplatform)
**Performance Goals**: Analytics dispatch < 1ms on main thread (fire-and-forget to background scope)
**Constraints**: Zero impact on UI thread; all provider calls on Dispatchers.Default; graceful degradation if provider fails
**Scale/Scope**: ~25 event types, 3 providers initially, ~20 screens tracked

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|---|---|---|
| I. Mobile-First (Android & iOS Equal) | ✅ PASS | Firebase Analytics on both Android + iOS; Desktop gets ConsoleProvider |
| II. Pattern-Based Consistency | ✅ PASS | Uses interface + DI pattern matching BillingManager; no magic strings (sealed class events) |
| III. Accessibility & UX | ✅ PASS | No UI components added (analytics is backend-only); Settings consent toggles follow existing patterns |
| IV. CI/CD & Conventional Commits | ✅ PASS | Feature branch `011-analytics-module`; commits will follow conventional format |
| V. Code Generation & API Management | ✅ PASS | No generated API changes; analytics is a new module alongside existing code |
| VI. Multiplatform Architecture | ✅ PASS | Common interface in `commonMain`; platform providers in `androidMain`/`iosMain`; Desktop no-op via ConsoleProvider; uses Koin DI |
| VII. Testing Standards | ✅ PASS | FakeAnalyticsProvider for unit tests; AnalyticsManager tested with mocked providers |
| VIII. Compose Best Practices | ✅ PASS | AnalyticsScreenTracker uses LaunchedEffect properly; no state in Composables |

**Post-Phase 1 Re-check**: All gates still PASS. Design is additive — no existing patterns violated.

## Project Structure

### Documentation (this feature)

```text
specs/011-analytics-module/
├── plan.md              # This file
├── spec.md              # Feature specification
├── research.md          # Phase 0 research output
├── data-model.md        # Phase 1 data model
├── quickstart.md        # Phase 1 quickstart guide
├── contracts/
│   └── internal-contracts.md  # Kotlin interface contracts
└── tasks.md             # Phase 2 output (created by /speckit.tasks)
```

### Source Code (repository root)

```text
composeApp/src/
├── commonMain/kotlin/me/calebjones/spacelaunchnow/
│   ├── analytics/
│   │   ├── core/
│   │   │   ├── AnalyticsProvider.kt           # Provider interface
│   │   │   ├── AnalyticsManager.kt            # Manager interface
│   │   │   ├── AnalyticsManagerImpl.kt        # Fan-out dispatcher
│   │   │   └── AnalyticsPreferences.kt        # Consent preferences
│   │   ├── events/
│   │   │   └── AnalyticsEvent.kt              # Sealed event hierarchy
│   │   ├── navigation/
│   │   │   ├── AnalyticsScreenTracker.kt      # Auto screen tracking composable
│   │   │   └── RouteScreenMapper.kt           # Route → screen name mapping
│   │   └── providers/
│   │       └── ConsoleAnalyticsProvider.kt    # Debug/Desktop logging provider
│   └── di/
│       └── AnalyticsModule.kt                 # Koin module for analytics
│
├── androidMain/kotlin/me/calebjones/spacelaunchnow/analytics/providers/
│   └── FirebaseAnalyticsProvider.kt           # Firebase Analytics (Android)
│
├── iosMain/kotlin/me/calebjones/spacelaunchnow/analytics/providers/
│   └── FirebaseAnalyticsProvider.kt           # Firebase Analytics (iOS)
│
├── commonTest/kotlin/me/calebjones/spacelaunchnow/analytics/
│   ├── AnalyticsManagerImplTest.kt            # Manager unit tests
│   └── FakeAnalyticsProvider.kt               # Test double
│
└── desktopMain/
    └── (no analytics files — ConsoleProvider is in commonMain)
```

### Modified Existing Files

| File | Change | Scope |
|---|---|---|
| `di/AppModule.kt` | Add `analyticsModule` to `koinConfig` modules list | 1 line |
| `navigation/` (NavHost) | Add `AnalyticsScreenTracker` composable | ~5 lines |
| `gradle/libs.versions.toml` | Add `gitlive-firebase-analytics` library entry (if not present) | 1 line |
| `composeApp/build.gradle.kts` | Add `gitlive-firebase-analytics` dependency to commonMain | 1-2 lines |

**Structure Decision**: KMP mobile architecture with common interface + platform-specific providers.
Follows the exact same pattern as `BillingManager` (interface in commonMain, platform actuals).
Analytics files under `analytics/` package (extending existing `analytics/DatadogConfig.kt`
namespace). New Koin module `analyticsModule` keeps DI clean and modular.

## Complexity Tracking

No constitution violations. All gates pass without justification needed.
