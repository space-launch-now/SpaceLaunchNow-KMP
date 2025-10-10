# AI Agent Prompts for Unit Testing

This document provides ready-to-use prompts for delegating unit testing tasks to AI agents (GitHub Copilot, Cursor, Windsurf, Claude, ChatGPT, etc.).

## 📋 Quick Start

1. Copy a prompt from this file
2. Replace `[PLACEHOLDERS]` with actual values
3. Paste into your AI agent
4. Review and commit the generated tests

---

## 🎯 General Task Assignment Prompt

Use this prompt to assign any testing task to an AI agent:

```
I need you to write comprehensive unit tests for the SpaceLaunchNow-KMP project.

**Task**: Write tests for [COMPONENT_NAME] ([COMPONENT_TYPE])

**Context**:
- Project: Kotlin Multiplatform (Android, iOS, Desktop)
- Test framework: kotlin.test
- Location: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/[PACKAGE]/
- File to test: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/[PACKAGE]/[COMPONENT_NAME].kt

**Requirements**:
1. Follow the patterns in docs/TESTING_GUIDE.md
2. Use the template from docs/templates/[TEMPLATE_TYPE]Template.kt
3. Test all public methods
4. Include happy path, error cases, and edge cases
5. Test null handling for all nullable parameters
6. Use descriptive test names: test[MethodName]_[Scenario]_[ExpectedBehavior]
7. Place test file at: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/[PACKAGE]/[COMPONENT_NAME]Test.kt

**Reference Documentation**:
- Testing guide: docs/TESTING_GUIDE.md
- Quick reference: docs/TESTING_QUICK_REF.md
- Template: docs/templates/[TEMPLATE_TYPE]Template.kt

**Success Criteria**:
- All tests pass: `./gradlew :composeApp:jvmTest --tests "*[COMPONENT_NAME]Test"`
- Coverage includes happy path, errors, nulls, and edge cases
- Test names are descriptive and follow convention
- Code follows existing test patterns

Please generate the complete test file.
```

**Example Usage**:
```
I need you to write comprehensive unit tests for the SpaceLaunchNow-KMP project.

**Task**: Write tests for LaunchFormatUtil (Utility class)

**Context**:
- Project: Kotlin Multiplatform (Android, iOS, Desktop)
- Test framework: kotlin.test
- Location: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/
- File to test: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/LaunchFormatUtil.kt

[... rest of prompt ...]
```

---

## 🔧 Component-Specific Prompts

### For Utility Classes (Pure Functions)

```
Write unit tests for [UTILITY_NAME] utility class in SpaceLaunchNow-KMP.

**Component**: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/[UTILITY_NAME].kt

**Test Location**: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/[UTILITY_NAME]Test.kt

**Template**: Use docs/templates/BasicTestTemplate.kt

**Test Coverage Required**:
1. Happy path with valid inputs
2. Null handling for all nullable parameters
3. Empty string/collection handling
4. Boundary conditions (min/max values)
5. Edge cases specific to the utility's logic
6. Invalid input handling

**Pattern Reference**: See docs/TESTING_GUIDE.md section "Utility Classes"

**Example Test Names**:
- testFormatLaunchTitle_WithRocketConfiguration_ReturnsFormattedString
- testFormatLaunchTitle_WithNullRocket_FallsBackToLaunchName
- testFormatLaunchTitle_WithLongProviderName_UsesAbbreviation
- testFormatLaunchTitle_WithAllNullValues_ReturnsUnknown

Run tests with: `./gradlew :composeApp:jvmTest --tests "*[UTILITY_NAME]Test"`
```

### For Repository Classes

```
Write unit tests for [REPOSITORY_NAME] repository in SpaceLaunchNow-KMP.

**Component**: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/[REPOSITORY_NAME].kt

**Test Location**: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/[REPOSITORY_NAME]Test.kt

**Template**: Use docs/templates/RepositoryTestTemplate.kt

**Requirements**:
1. Create mock API implementation for testing
2. Test successful API calls returning Result.success
3. Test API errors returning Result.failure  
4. Test network timeouts and exceptions
5. Test throttling/rate limit errors
6. Test data transformation from API to domain models
7. Test null/empty response handling
8. Test pagination if applicable

**Mocking Pattern**:
```kotlin
private fun createMockApi(shouldFail: Boolean = false) = object : [API_INTERFACE] {
    override suspend fun [method](...): [ReturnType] {
        if (shouldFail) throw Exception("Test API Error")
        return [MockData]
    }
}
```

**Pattern Reference**: See docs/TESTING_GUIDE.md section "Repository Layer"

Run tests with: `./gradlew :composeApp:jvmTest --tests "*[REPOSITORY_NAME]Test"`
```

### For ViewModel Classes

```
Write unit tests for [VIEWMODEL_NAME] in SpaceLaunchNow-KMP.

**Component**: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/[VIEWMODEL_NAME].kt

**Test Location**: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/[VIEWMODEL_NAME]Test.kt

**Template**: Use docs/templates/ViewModelTestTemplate.kt

**Requirements**:
1. Create mock repository for testing
2. Set up test dispatcher: `Dispatchers.setMain(StandardTestDispatcher())`
3. Test initial state is correct
4. Test loading state transitions
5. Test success state with data emission
6. Test error state with error messages
7. Test StateFlow emissions using testScheduler.advanceUntilIdle()
8. Test coroutine cancellation
9. Test multiple rapid calls if applicable
10. Clean up with `Dispatchers.resetMain()` in @AfterTest

**Critical Imports**:
```kotlin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*
```

**Pattern Reference**: See docs/TESTING_GUIDE.md section "ViewModel Layer"

Run tests with: `./gradlew :composeApp:jvmTest --tests "*[VIEWMODEL_NAME]Test"`
```

---

## 📝 Task-Specific Prompts (Ready to Use)

### Phase 1: Utilities

#### LaunchFormatUtil
```
Write comprehensive unit tests for LaunchFormatUtil in SpaceLaunchNow-KMP.

File to test: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/LaunchFormatUtil.kt
Test location: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/LaunchFormatUtilTest.kt

This utility formats launch titles using the pattern: "<LSP> | <Rocket Configuration>"

Test all four overloaded formatLaunchTitle methods:
1. formatLaunchTitle(name, abbrev, rocket, launchName) - manual parameters
2. formatLaunchTitle(LaunchDetailed) 
3. formatLaunchTitle(LaunchNormal)
4. formatLaunchTitle(LaunchBasic)

Key test scenarios:
- Normal formatting with rocket configuration
- LSP name > 15 chars uses abbreviation
- LSP name <= 15 chars uses full name
- Null rocket configuration falls back to launch name
- All null values returns "Unknown Name"
- Empty strings are handled correctly
- All edge cases for the abbreviation logic

Reference: docs/TESTING_GUIDE.md has a complete example for this class.

Run: ./gradlew :composeApp:jvmTest --tests "*LaunchFormatUtilTest"
```

#### StatusColorUtil
```
Write unit tests for StatusColorUtil in SpaceLaunchNow-KMP.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/StatusColorUtil.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/StatusColorUtilTest.kt

This utility maps launch status IDs to colors.

Test scenarios:
- Each valid status ID maps to correct color
- Invalid status IDs return default color
- Null status ID handling
- Boundary status IDs
- All defined status constants

Use template: docs/templates/BasicTestTemplate.kt

Run: ./gradlew :composeApp:jvmTest --tests "*StatusColorUtilTest"
```

#### VideoUtil
```
Write unit tests for VideoUtil in SpaceLaunchNow-KMP.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/VideoUtil.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/VideoUtilTest.kt

This utility parses and validates video URLs.

Test scenarios:
- YouTube URL parsing and validation
- Platform detection (YouTube, Vimeo, etc.)
- Invalid URL handling
- Null URL handling
- Empty string handling
- Malformed URLs

Use template: docs/templates/BasicTestTemplate.kt

Run: ./gradlew :composeApp:jvmTest --tests "*VideoUtilTest"
```

#### BuildConfig
```
Write unit tests for BuildConfig in SpaceLaunchNow-KMP.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/BuildConfig.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/BuildConfigTest.kt

Test scenarios:
- Version name is accessible and non-null
- Version code is accessible and > 0
- Environment-specific config values
- Debug vs release flags

Use template: docs/templates/BasicTestTemplate.kt

Run: ./gradlew :composeApp:jvmTest --tests "*BuildConfigTest"
```

### Phase 3: Repositories

#### LaunchRepositoryImpl
```
Write comprehensive unit tests for LaunchRepositoryImpl in SpaceLaunchNow-KMP.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImplTest.kt

This repository interacts with the Launch Library API.

Required test coverage:
1. getUpcomingLaunchesList - success returns Result.success with data
2. getUpcomingLaunchesList - API error returns Result.failure
3. getUpcomingLaunchesNormal - success scenario
4. getUpcomingLaunchesNormal - error scenario
5. getLaunchDetails - success with valid ID
6. getLaunchDetails - API error handling
7. getNextLaunch - success scenario
8. Network timeout handling
9. Throttling/rate limit error messages
10. Null/empty response handling

Mock the LaunchesApi interface for testing. Do not make real API calls.

Use template: docs/templates/RepositoryTestTemplate.kt
Reference: docs/TESTING_GUIDE.md section "Repository Layer"

Run: ./gradlew :composeApp:jvmTest --tests "*LaunchRepositoryImplTest"
```

### Phase 4: ViewModels

#### NextUpViewModel
```
Write comprehensive unit tests for NextUpViewModel in SpaceLaunchNow-KMP.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NextUpViewModel.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NextUpViewModelTest.kt

This ViewModel manages the next upcoming launch state.

Required test coverage:
1. Initial state - isLoading=false, nextLaunch=null, error=null
2. fetchNextLaunch - sets loading state correctly
3. fetchNextLaunch success - emits launch data, clears loading
4. fetchNextLaunch error - sets error message, clears loading
5. fetchNextLaunch - handles empty results
6. fetchNextLaunch - handles repository failures
7. Multiple rapid calls handling

Use StandardTestDispatcher and testScheduler.advanceUntilIdle() for coroutine testing.

Mock the LaunchRepository interface.

Use template: docs/templates/ViewModelTestTemplate.kt
Reference: docs/TESTING_GUIDE.md section "ViewModel Layer"

Critical setup:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class NextUpViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() { Dispatchers.setMain(testDispatcher) }
    
    @AfterTest
    fun teardown() { Dispatchers.resetMain() }
}
```

Run: ./gradlew :composeApp:jvmTest --tests "*NextUpViewModelTest"
```

---

## 🚀 Batch Task Prompts

### Complete Phase 1 (All Utilities)
```
I need you to write unit tests for all Phase 1 utility classes in SpaceLaunchNow-KMP.

**Files to test** (in priority order):
1. util/LaunchFormatUtil.kt (15+ tests)
2. util/StatusColorUtil.kt (8+ tests)
3. util/VideoUtil.kt (6+ tests)
4. util/BuildConfig.kt (3+ tests)
5. utils/DateTimeLocal.kt (10+ tests)

For each file:
- Place tests in commonTest with same package structure
- Use docs/templates/BasicTestTemplate.kt
- Follow patterns in docs/TESTING_GUIDE.md
- Test happy path, errors, nulls, and edge cases
- Use descriptive test names

**Process**:
1. Start with LaunchFormatUtil (highest priority)
2. Create comprehensive test file
3. Verify tests pass
4. Move to next utility
5. Repeat until all Phase 1 complete

Reference: docs/TESTING_TASKS.md Phase 1 section

Run all: ./gradlew :composeApp:jvmTest
```

### High-Impact Components
```
Write tests for the highest-impact components in SpaceLaunchNow-KMP:

**Priority components**:
1. LaunchRepositoryImpl (data/repository/) - 15+ tests
2. NextUpViewModel (ui/viewmodel/) - 12+ tests
3. LaunchFormatUtil (util/) - 15+ tests

These three components are critical for:
- API interaction (repository)
- UI state management (viewmodel)
- Data formatting (utility)

For each:
- Use appropriate template (Repository, ViewModel, or Basic)
- Follow docs/TESTING_GUIDE.md patterns
- Mock all external dependencies
- Test all error paths
- Verify with: ./gradlew :composeApp:jvmTest

Reference: docs/TESTING_TASKS.md "High Impact" section
```

---

## 🎓 Learning Progression Prompts

### For Beginners
```
I'm new to testing in this project. Write tests for VideoUtil to help me learn.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/VideoUtil.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/VideoUtilTest.kt

This is a simple utility with ~6 tests needed - perfect for learning.

Please:
1. Use docs/templates/BasicTestTemplate.kt as starting point
2. Add detailed comments explaining each test
3. Show examples of all assertion types
4. Include happy path, null checks, and error cases
5. Make it educational - I'll use this as a reference

Run: ./gradlew :composeApp:jvmTest --tests "*VideoUtilTest"
```

### For Intermediate
```
I've done basic utility tests. Now I need to test LaunchRepositoryImpl.

File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImplTest.kt

This requires:
- Mocking the API interface
- Testing Result<T> patterns
- Async/suspend function testing
- Error handling scenarios

Use docs/templates/RepositoryTestTemplate.kt and show me proper mocking patterns.

Reference: docs/TESTING_GUIDE.md Repository section

Run: ./gradlew :composeApp:jvmTest --tests "*LaunchRepositoryImplTest"
```

### For Advanced
```
I'm ready for complex ViewModel testing with StateFlows and coroutines.

Write tests for NextUpViewModel:
File: composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NextUpViewModel.kt
Test: composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/NextUpViewModelTest.kt

Show me:
- Proper test dispatcher setup
- StateFlow testing patterns  
- testScheduler.advanceUntilIdle() usage
- Repository mocking
- State transition verification

Use docs/templates/ViewModelTestTemplate.kt

Reference: docs/TESTING_GUIDE.md ViewModel section

Run: ./gradlew :composeApp:jvmTest --tests "*NextUpViewModelTest"
```

---

## 🔄 Iterative Improvement Prompts

### Add Missing Test Cases
```
Review the existing tests in [TEST_FILE] and add missing test cases.

Current tests are incomplete. Please add:
1. Edge cases not currently covered
2. Null handling tests if missing
3. Error scenarios
4. Boundary conditions

Analyze the source file to identify all public methods and ensure each has:
- Happy path test
- Error case test
- Null handling test (if applicable)
- Edge case tests

Run: ./gradlew :composeApp:jvmTest --tests "*[TEST_CLASS_NAME]"
```

### Improve Test Coverage
```
Expand test coverage for [COMPONENT_NAME].

Current test file: [TEST_FILE_PATH]
Source file: [SOURCE_FILE_PATH]

Add tests for:
1. Any public methods not yet tested
2. Additional error scenarios
3. Edge cases and boundary conditions
4. Complex interactions between methods

Ensure all tests follow conventions in docs/TESTING_GUIDE.md

Run: ./gradlew :composeApp:jvmTest --tests "*[TEST_CLASS_NAME]"
```

---

## 📊 Progress Tracking Prompts

### Update Task Status
```
After completing tests, update the task status in docs/TESTING_TASKS.md:

Change the status for [COMPONENT_NAME]:
- From: ⬜ (Not Started)
- To: ✅ (Complete)

Find the component in the appropriate phase table and update the Status column.
```

### Generate Coverage Report
```
Analyze the test coverage for Phase [PHASE_NUMBER] and report:

1. Components completed (✅)
2. Components in progress (🚧)
3. Components not started (⬜)
4. Estimated coverage percentage
5. Next recommended tasks

Reference: docs/TESTING_TASKS.md
```

---

## 🛠️ Troubleshooting Prompts

### Fix Failing Tests
```
The tests in [TEST_FILE] are failing. Please debug and fix.

Error messages:
[PASTE_ERROR_MESSAGES]

Review the test file and:
1. Identify the root cause of failures
2. Fix any incorrect assertions
3. Update test data if needed
4. Ensure mocks are properly configured
5. Verify all tests pass

Run: ./gradlew :composeApp:jvmTest --tests "*[TEST_CLASS_NAME]"
```

### Resolve Import Issues
```
Fix import errors in [TEST_FILE].

Common imports needed:
- kotlin.test.* for assertions
- kotlinx.coroutines.test.* for coroutine testing
- Platform-specific test utilities

Ensure all imports match the patterns in docs/TESTING_GUIDE.md
```

---

## 📚 Reference

- **Full Documentation**: docs/TESTING_INDEX.md
- **Detailed Guide**: docs/TESTING_GUIDE.md
- **Task List**: docs/TESTING_TASKS.md
- **Quick Reference**: docs/TESTING_QUICK_REF.md
- **Templates**: docs/templates/

---

## 💡 Tips for Using These Prompts

1. **Customize placeholders**: Replace [COMPONENT_NAME], [PACKAGE], etc. with actual values
2. **Provide context**: Include relevant error messages or specific requirements
3. **Reference examples**: Point to existing tests as examples when helpful
4. **Iterate**: Start with basic prompt, refine based on results
5. **Verify**: Always run tests after generation
6. **Update tracking**: Mark tasks as complete in TESTING_TASKS.md

---

## 🎯 Success Checklist

After using an AI agent prompt:
- [ ] Tests are generated in correct location
- [ ] All tests pass: `./gradlew :composeApp:jvmTest`
- [ ] Tests follow project conventions
- [ ] Happy path, errors, and edge cases covered
- [ ] Test names are descriptive
- [ ] Updated TESTING_TASKS.md status
- [ ] Committed with conventional format: `test(component): description`
