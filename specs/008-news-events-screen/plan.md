# Implementation Plan: News and Events Screen

**Branch**: `008-news-events-screen` | **Date**: 2025-01-23 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/008-news-events-screen/spec.md`

## Summary

Implement a tabbed screen for browsing space news articles and events with search functionality and filtering capabilities. The screen uses a tabbed interface (News | Events) with shared search bar, tab-specific filters (news source for articles, event type for events), pagination, and navigation to item details. Follows established AgencyListScreen patterns for consistency.

## Technical Context

**Language/Version**: Kotlin 2.0.21 with KMP, Java 21  
**Primary Dependencies**: SNAPI v4 (Articles), Launch Library 2.4.0 (Events), Compose Multiplatform, Ktor, Koin  
**Storage**: Repository-level caching with DataResult pattern (existing)  
**Testing**: JUnit5/KotlinTest for common, platform-specific test runners  
**Target Platform**: Android (primary), iOS, Desktop  
**Project Type**: KMP mobile application  
**Performance Goals**: <300ms initial load, smooth 60fps scrolling, debounced search  
**Constraints**: Must use existing API extension pattern, dual previews required  
**Scale/Scope**: ~5 new files, ~1500 LOC, 1 new screen

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First Development | ✅ PASS | Android primary target, iOS and Desktop supported via KMP |
| II. Pattern-Based Consistency | ✅ PASS | Uses API extension functions, Repository pattern with Result<T>, MVVM with StateFlow |
| III. Accessibility & UX | ✅ PASS | Dual previews specified, component reuse from ui/components/, semantic properties planned |
| IV. CI/CD & Conventional Commits | ✅ PASS | Will use `feat(ui): add news events screen` format |
| V. Code Generation & API Management | ✅ PASS | Uses generated APIs via extension functions, no direct 70+ param calls |
| VI. Multiplatform Architecture | ✅ PASS | All code in commonMain, no platform-specific dependencies, Koin DI |
| VII. Testing Standards | ✅ PASS | Repository tests planned for pagination, ViewModel tests for state transitions |
| VIII. Jetpack Compose Best Practices | ✅ PASS | Modifier first param, state hoisting, collectAsStateWithLifecycle, derivedStateOf for pagination trigger |

## Project Structure

### Documentation (this feature)

```text
specs/008-news-events-screen/
├── plan.md              # This file
├── spec.md              # Feature specification (created)
├── research.md          # API and pattern research (created)
├── data-model.md        # Entity and state definitions (created)
├── quickstart.md        # Setup instructions (created)
├── contracts/           # Interface contracts (created)
│   └── interfaces.md
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/

# New files to create
ui/newsevents/
├── NewsEventsScreen.kt              # Main screen with tabs
├── NewsEventsFilterBottomSheet.kt   # Filter modal
└── components/
    ├── NewsListItem.kt              # Full-size news item card
    └── EventListItem.kt             # Full-size event item card

ui/viewmodel/
└── NewsEventsViewModel.kt           # Combined state management

api/extensions/
└── InfoApiExtensions.kt             # SNAPI Info endpoint wrapper

data/repository/
├── InfoRepository.kt                # Interface for SNAPI info
└── InfoRepositoryImpl.kt            # Implementation

# Files to modify
api/extensions/ArticlesApiExtensions.kt  # Add offset, newsSite params
api/extensions/EventsApiExtensions.kt    # Add offset, search params
data/repository/ArticlesRepository.kt    # Add getArticlesPaginated()
data/repository/ArticlesRepositoryImpl.kt
data/repository/EventsRepository.kt      # Add getEventsPaginated()
data/repository/EventsRepositoryImpl.kt
navigation/Screen.kt                     # Add NewsEvents route
ui/App.kt                                # Add composable entry
di/AppModule.kt                          # Register ViewModel
di/ApiModule.kt                          # Register InfoApi
```

**Structure Decision**: Mobile + shared code structure using KMP composeApp module. All new code goes in commonMain source set following existing package conventions.

## Complexity Tracking

> No constitution violations requiring justification.

| Area | Complexity | Justification |
|------|------------|---------------|
| Combined ViewModel | Medium | Single ViewModel for both tabs simplifies state management and tab switching; alternative of separate ViewModels would duplicate shared search/filter logic |
| InfoRepository | Low | Required for dynamic news site list; minimal interface with single method |

## Artifacts Generated

| Artifact | Description | Status |
|----------|-------------|--------|
| [spec.md](spec.md) | Feature specification with user stories and requirements | ✅ Created |
| [research.md](research.md) | API research and pattern documentation | ✅ Created |
| [data-model.md](data-model.md) | Entity and state model definitions | ✅ Created |
| [contracts/interfaces.md](contracts/interfaces.md) | Repository and ViewModel interfaces | ✅ Created |
| [quickstart.md](quickstart.md) | Setup and verification instructions | ✅ Created |

## Implementation Phases

### Phase 1: API Layer
- [ ] Add InfoApiExtensions.kt
- [ ] Update ArticlesApiExtensions with offset/newsSite params
- [ ] Update EventsApiExtensions with offset/search params

### Phase 2: Repository Layer
- [ ] Create InfoRepository interface and implementation
- [ ] Add getArticlesPaginated() to ArticlesRepository
- [ ] Add getEventsPaginated() to EventsRepository

### Phase 3: ViewModel
- [ ] Create NewsEventsViewModel with combined state
- [ ] Implement pagination logic (PAGE_SIZE=20)
- [ ] Implement search debouncing (300ms)
- [ ] Implement filter management

### Phase 4: UI Components
- [ ] Create NewsListItem component with dual previews
- [ ] Create EventListItem component with dual previews
- [ ] Create NewsEventsFilterBottomSheet with dual previews

### Phase 5: Main Screen
- [ ] Create NewsEventsScreen with tabs
- [ ] Implement search bar integration
- [ ] Implement pagination trigger (5 items from bottom)
- [ ] Add pull-to-refresh support
- [ ] Create dual previews

### Phase 6: Integration
- [ ] Register ViewModel in AppModule
- [ ] Register InfoApi in ApiModule
- [ ] Add Screen.NewsEvents navigation route
- [ ] Add composable entry in App.kt
- [ ] Wire navigation from home/menu

## Next Steps

Run `/speckit.tasks` to generate detailed implementation tasks from this plan.
