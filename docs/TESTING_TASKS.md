# Unit Testing Task List

This file breaks down the testing work into discrete, assignable tasks. Each task represents one file to test and can be picked up by an agent independently.

## Legend
- ✅ **Done**: Test file created and verified
- 🚧 **In Progress**: Being worked on
- ⬜ **Not Started**: Available to pick up
- 🔴 **Blocked**: Waiting on dependencies

---

## Phase 1: Utility Classes (No Dependencies)

### Priority: CRITICAL - Start Here
These pure functions are easiest to test and have no external dependencies.

| Status | File | Test File | Estimated Tests | Agent | Notes |
|--------|------|-----------|-----------------|-------|-------|
| ✅ | `util/UserAgentUtil.kt` | `util/UserAgentUtilTest.kt` | 1+ | - | Already exists |
| ✅ | `util/DateTimeUtil.kt` | `util/DateTimeUtilTimelineTest.kt` | 10+ | - | Partially done, needs more |
| ✅ | `util/EnvironmentManager.kt` | `util/EnvironmentManagerTest.kt` | 1+ | - | Already exists |
| ✅ | `util/LaunchFormatUtil.kt` | `util/LaunchFormatUtilTest.kt` | 24 | Copilot | Complete with all overloads and edge cases |
| ⬜ | `util/StatusColorUtil.kt` | `util/StatusColorUtilTest.kt` | 8+ | - | Color mapping logic |
| ⬜ | `util/VideoUtil.kt` | `util/VideoUtilTest.kt` | 6+ | - | URL parsing/validation |
| ⬜ | `util/BuildConfig.kt` | `util/BuildConfigTest.kt` | 3+ | - | Version info access |
| ⬜ | `utils/DateTimeLocal.kt` | `utils/DateTimeLocalTest.kt` | 10+ | - | Platform-specific dates |

### Subtasks for DateTimeUtil.kt (needs expansion)
- ⬜ Test `formatTimelineRelativeTime` (already done)
- ⬜ Test `formatLaunchDate`
- ⬜ Test `formatTimeRemaining`
- ⬜ Test `parseIso8601`
- ⬜ Test timezone conversions
- ⬜ Test relative time calculations

---

## Phase 2: API Extensions (Light Dependencies)

### Priority: HIGH
Test parameter mapping and default values.

| Status | File | Test File | Estimated Tests | Agent | Notes |
|--------|------|-----------|-----------------|-------|-------|
| ⬜ | `api/extensions/LaunchesApiExtensions.kt` | `api/extensions/LaunchesApiExtensionsTest.kt` | 10+ | - | Parameter mapping |
| ⬜ | `api/snapi/extensions/ArticlesApiExtensions.kt` | `api/snapi/extensions/ArticlesApiExtensionsTest.kt` | 6+ | - | SNAPI extensions |
| ⬜ | `api/snapi/extensions/BlogsApiExtensions.kt` | `api/snapi/extensions/BlogsApiExtensionsTest.kt` | 6+ | - | SNAPI extensions |

---

## Phase 3: Repository Layer (Medium Dependencies)

### Priority: CRITICAL
Core business logic - requires mocking API clients.

| Status | File | Test File | Estimated Tests | Agent | Notes |
|--------|------|-----------|-----------------|-------|-------|
| ⬜ | `data/repository/LaunchRepositoryImpl.kt` | `data/repository/LaunchRepositoryImplTest.kt` | 15+ | - | **START HERE** for repos |
| ⬜ | `data/repository/EventsRepositoryImpl.kt` | `data/repository/EventsRepositoryImplTest.kt` | 12+ | - | Event API |
| ⬜ | `data/repository/UpdatesRepositoryImpl.kt` | `data/repository/UpdatesRepositoryImplTest.kt` | 10+ | - | News/updates |
| ⬜ | `data/repository/ArticlesRepositoryImpl.kt` | `data/repository/ArticlesRepositoryImplTest.kt` | 10+ | - | Articles API |
| ⬜ | `data/repository/NotificationRepositoryImpl.kt` | `data/repository/NotificationRepositoryImplTest.kt` | 12+ | - | Notifications |
| ⬜ | `data/repository/SubscriptionProcessor.kt` | `data/repository/SubscriptionProcessorTest.kt` | 8+ | - | Subscription logic |

### Repository Testing Requirements
Each repository test should cover:
- ✅ Successful API call → Result.success
- ✅ API error → Result.failure
- ✅ Network timeout → Result.failure
- ✅ Throttling/rate limit → Specific error message
- ✅ Data transformation
- ✅ Null handling
- ✅ Empty response handling
- ✅ Pagination (if applicable)

---

## Phase 4: ViewModel Layer (High Dependencies)

### Priority: CRITICAL
UI state management - requires repository mocks.

| Status | File | Test File | Estimated Tests | Agent | Notes |
|--------|------|-----------|-----------------|-------|-------|
| ⬜ | `ui/viewmodel/NextUpViewModel.kt` | `ui/viewmodel/NextUpViewModelTest.kt` | 12+ | - | **START HERE** for VMs |
| ⬜ | `ui/viewmodel/HomeViewModel.kt` | `ui/viewmodel/HomeViewModelTest.kt` | 10+ | - | Home screen state |
| ⬜ | `ui/viewmodel/LaunchViewModel.kt` | `ui/viewmodel/LaunchViewModelTest.kt` | 12+ | - | Launch details |
| ⬜ | `ui/viewmodel/EventViewModel.kt` | `ui/viewmodel/EventViewModelTest.kt` | 10+ | - | Event details |
| ⬜ | `ui/viewmodel/UpdatesViewModel.kt` | `ui/viewmodel/UpdatesViewModelTest.kt` | 10+ | - | News/updates |
| ⬜ | `ui/viewmodel/SettingsViewModel.kt` | `ui/viewmodel/SettingsViewModelTest.kt` | 8+ | - | Settings state |
| ⬜ | `ui/viewmodel/DebugSettingsViewModel.kt` | `ui/viewmodel/DebugSettingsViewModelTest.kt` | 6+ | - | Debug features |

### ViewModel Testing Requirements
Each ViewModel test should cover:
- ✅ Initial state is correct
- ✅ Loading state transitions
- ✅ Success state with data
- ✅ Error state with message
- ✅ StateFlow emissions
- ✅ Coroutine cancellation
- ✅ Repository error handling
- ✅ Multiple rapid calls (debouncing if applicable)

---

## Phase 5: Data Storage & Cache (Medium Priority)

### Priority: MEDIUM
Persistence and caching logic.

| Status | File | Test File | Estimated Tests | Agent | Notes |
|--------|------|-----------|-----------------|-------|-------|
| ⬜ | `data/storage/DataStoreProvider.kt` | `data/storage/DataStoreProviderTest.kt` | 8+ | - | Preference storage |
| ⬜ | `cache/*` (if exists) | `cache/*Test.kt` | 10+ | - | Cache mechanisms |

---

## Phase 6: UI Components (Lower Priority)

### Priority: LOW
Compose UI testing - may require additional dependencies.

| Status | File | Test File | Estimated Tests | Agent | Notes |
|--------|------|-----------|-----------------|-------|-------|
| ⬜ | `navigation/Screen.kt` | `navigation/ScreenTest.kt` | 5+ | - | Navigation state |
| ⬜ | Various compose components | TBD | TBD | - | Requires compose test framework |

---

## Phase 7: Integration Tests (Future)

### Priority: FUTURE
End-to-end testing scenarios.

| Status | Description | Test File | Agent | Notes |
|--------|-------------|-----------|-------|-------|
| ⬜ | Launch list to detail flow | `integration/LaunchFlowTest.kt` | - | Future work |
| ⬜ | API integration tests | `integration/ApiIntegrationTest.kt` | - | Future work |

---

## Quick Pick Tasks

### 🔥 BEST STARTER TASKS (5-10 tests, <30 min)
1. `util/VideoUtil.kt` → `util/VideoUtilTest.kt`
2. `util/BuildConfig.kt` → `util/BuildConfigTest.kt`
3. `util/StatusColorUtil.kt` → `util/StatusColorUtilTest.kt`

### ⭐ RECOMMENDED NEXT (10-15 tests, ~1 hour)
1. `util/LaunchFormatUtil.kt` → `util/LaunchFormatUtilTest.kt`
2. `utils/DateTimeLocal.kt` → `utils/DateTimeLocalTest.kt`
3. `api/extensions/LaunchesApiExtensions.kt` → Test file

### 🎯 HIGH IMPACT (15+ tests, 1-2 hours)
1. `data/repository/LaunchRepositoryImpl.kt` → Test file
2. `ui/viewmodel/NextUpViewModel.kt` → Test file
3. `data/repository/EventsRepositoryImpl.kt` → Test file

---

## Test Coverage Goals

### Metrics
- **Phase 1 Complete**: 15% coverage
- **Phase 2 Complete**: 25% coverage
- **Phase 3 Complete**: 50% coverage
- **Phase 4 Complete**: 75% coverage
- **All Phases Complete**: 85%+ coverage

### Critical Path (Minimum Viable Testing)
To have minimally viable test coverage, complete:
1. All Phase 1 (Utils)
2. LaunchRepositoryImpl + LaunchRepositoryImplTest
3. NextUpViewModel + NextUpViewModelTest

---

## How to Pick a Task

1. **Check status**: Pick a task with ⬜ status
2. **Update status**: Change to 🚧 and add your name
3. **Create test file**: Follow patterns in TESTING_GUIDE.md
4. **Run tests**: Verify all tests pass
5. **Update status**: Change to ✅ when complete
6. **Commit**: Use conventional commit format

Example commit:
```
test(util): add comprehensive tests for LaunchFormatUtil

- Test all overload methods
- Test abbreviation logic for long names
- Test null handling and fallbacks
- Test edge cases
```

---

## Dependencies Between Tasks

### Before Testing ViewModels
- ⬜ Must have repository interfaces (already exist)
- ⬜ Create mock repository implementations

### Before Testing Repositories  
- ⬜ Understand API extension functions
- ⬜ Set up mock API clients

### Before Testing API Extensions
- ⬜ No dependencies - can start immediately

---

## Notes for Agents

- Each file should have its own test file
- Follow naming convention: `[ClassName]Test.kt`
- Place tests in same package structure under `commonTest`
- Use examples from TESTING_GUIDE.md
- Run tests before committing
- Update this file when you complete a task

## Questions?

- See TESTING_GUIDE.md for detailed patterns
- Check existing tests for examples
- Keep tests simple and focused
- When in doubt, ask!
