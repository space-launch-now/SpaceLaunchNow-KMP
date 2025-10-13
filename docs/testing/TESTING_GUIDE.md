# Unit Testing Guide for SpaceLaunchNow-KMP

This guide provides detailed instructions for implementing unit tests across the codebase.

## Table of Contents
1. [Overview](#overview)
2. [Test Infrastructure](#test-infrastructure)
3. [Testing Patterns](#testing-patterns)
4. [Component-Specific Guides](#component-specific-guides)
5. [Running Tests](#running-tests)

## Overview

SpaceLaunchNow-KMP is a Kotlin Multiplatform project targeting Android, iOS, and Desktop. Tests are written in `commonTest` to ensure cross-platform compatibility.

### Current Status
- **Source Files**: 89 files in `commonMain`
- **Test Files**: 6 files in `commonTest`
- **Coverage Gap**: ~93% of files need tests

### Testing Goals
- Achieve >80% code coverage for business logic
- 100% coverage for utility classes
- Mock external dependencies (APIs, storage)
- Test all error paths and edge cases

## Test Infrastructure

### Dependencies
Located in `composeApp/build.gradle.kts`:
```kotlin
commonTest {
    dependencies {
        implementation(libs.kotlin.test)
        implementation(libs.kotlinx.coroutines.test)
        implementation(libs.ktor.client.mock)
    }
}
```

### Test Location
All tests go in: `/composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/[package]/`

### File Naming Convention
- Test file: `[ClassName]Test.kt`
- Example: For `LaunchFormatUtil.kt` → `LaunchFormatUtilTest.kt`

## Testing Patterns

### 1. Basic Test Structure

```kotlin
package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertNull

class ExampleUtilTest {
    
    @Test
    fun testBasicFunction() {
        // Arrange (Given)
        val input = "test"
        
        // Act (When)
        val result = ExampleUtil.process(input)
        
        // Assert (Then)
        assertEquals("expected", result)
    }
    
    @Test
    fun testEdgeCase_NullInput() {
        val result = ExampleUtil.process(null)
        assertNull(result)
    }
}
```

### 2. Testing Coroutines

```kotlin
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CoroutineExampleTest {
    
    @Test
    fun testSuspendFunction() = runTest {
        // Use runTest for suspend functions
        val result = repository.fetchData()
        
        assertTrue(result.isSuccess)
    }
}
```

### 3. Testing StateFlows

```kotlin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class ViewModelTest {
    
    @Test
    fun testStateFlowEmission() = runTest {
        val viewModel = TestViewModel()
        
        // Trigger action
        viewModel.loadData()
        
        // Collect first emission
        val state = viewModel.dataState.first()
        
        assertEquals("expected", state)
    }
}
```

### 4. Mocking with Kotlin Test

```kotlin
// For simple mocking, create test implementations
class MockRepository : LaunchRepository {
    var shouldFail = false
    
    override suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList> {
        return if (shouldFail) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(createMockLaunchList())
        }
    }
    
    private fun createMockLaunchList() = PaginatedLaunchBasicList(
        count = 1,
        next = null,
        previous = null,
        results = listOf(/* mock data */)
    )
}
```

### 5. Testing Error Handling

```kotlin
@Test
fun testErrorHandling() {
    val result = runCatching {
        ExampleUtil.riskyOperation(null)
    }
    
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalArgumentException)
}
```

## Component-Specific Guides

### Utility Classes (Pure Functions)

**Example: LaunchFormatUtil**

```kotlin
package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals

class LaunchFormatUtilTest {
    
    @Test
    fun testFormatLaunchTitle_WithRocketConfiguration() {
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = "SpaceX",
            launchServiceProviderAbbrev = "SpX",
            rocketConfigurationName = "Falcon 9 Block 5",
            launchName = "Starlink 1-23"
        )
        
        assertEquals("SpaceX | Falcon 9 Block 5", result)
    }
    
    @Test
    fun testFormatLaunchTitle_UseAbbreviation_WhenNameTooLong() {
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = "Very Long Company Name Here",
            launchServiceProviderAbbrev = "VLCNH",
            rocketConfigurationName = "Rocket X",
            launchName = "Mission 1"
        )
        
        assertEquals("VLCNH | Rocket X", result)
    }
    
    @Test
    fun testFormatLaunchTitle_FallbackToLaunchName_WhenNoRocket() {
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = "NASA",
            launchServiceProviderAbbrev = null,
            rocketConfigurationName = null,
            launchName = "Apollo 11"
        )
        
        assertEquals("Apollo 11", result)
    }
    
    @Test
    fun testFormatLaunchTitle_FallbackToUnknown_WhenAllNull() {
        val result = LaunchFormatUtil.formatLaunchTitle(
            launchServiceProviderName = "NASA",
            launchServiceProviderAbbrev = null,
            rocketConfigurationName = null,
            launchName = null
        )
        
        assertEquals("Unknown Name", result)
    }
}
```

### Repository Layer

**Example: LaunchRepositoryImpl**

```kotlin
package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class LaunchRepositoryImplTest {
    
    private fun createMockApi() = object : LaunchesApi {
        // Mock implementation
    }
    
    @Test
    fun testGetUpcomingLaunches_Success() = runTest {
        val repository = LaunchRepositoryImpl(createMockApi())
        
        val result = repository.getUpcomingLaunchesList(limit = 10)
        
        assertTrue(result.isSuccess)
        val launches = result.getOrNull()
        assertEquals(10, launches?.results?.size)
    }
    
    @Test
    fun testGetUpcomingLaunches_HandlesApiError() = runTest {
        val failingApi = object : LaunchesApi {
            override suspend fun getLaunchMiniList(...) {
                throw Exception("API Error")
            }
        }
        val repository = LaunchRepositoryImpl(failingApi)
        
        val result = repository.getUpcomingLaunchesList(limit = 10)
        
        assertTrue(result.isFailure)
    }
}
```

### ViewModel Layer

**Example: NextUpViewModel**

```kotlin
package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class NextUpViewModelTest {
    
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun testFetchNextLaunch_SetsLoadingState() = runTest {
        val mockRepo = MockLaunchRepository()
        val viewModel = NextUpViewModel(mockRepo)
        
        // Initially not loading
        assertFalse(viewModel.isLoading.value)
        
        // Start fetch
        viewModel.fetchNextLaunch()
        
        // Should be loading (before coroutine completes)
        // Advance time to let coroutine start
        testScheduler.advanceUntilIdle()
        
        // After completion, should not be loading
        assertFalse(viewModel.isLoading.value)
    }
    
    @Test
    fun testFetchNextLaunch_EmitsLaunch() = runTest {
        val mockRepo = MockLaunchRepository()
        val viewModel = NextUpViewModel(mockRepo)
        
        viewModel.fetchNextLaunch()
        testScheduler.advanceUntilIdle()
        
        assertNotNull(viewModel.nextLaunch.value)
    }
    
    @Test
    fun testFetchNextLaunch_HandlesError() = runTest {
        val mockRepo = MockLaunchRepository(shouldFail = true)
        val viewModel = NextUpViewModel(mockRepo)
        
        viewModel.fetchNextLaunch()
        testScheduler.advanceUntilIdle()
        
        assertNotNull(viewModel.error.value)
        assertNull(viewModel.nextLaunch.value)
    }
}
```

## Running Tests

### Run All Tests
```bash
./gradlew :composeApp:allTests
```

### Run Specific Test Suite
```bash
# Desktop tests
./gradlew :composeApp:desktopTest

# Android tests
./gradlew :composeApp:testDebugUnitTest

# Common tests
./gradlew :composeApp:jvmTest
```

### Run Single Test Class
```bash
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.util.LaunchFormatUtilTest"
```

### Run with Coverage (if configured)
```bash
./gradlew :composeApp:jvmTest --coverage
```

## Test Checklist for Each Component

When creating tests for a new component, ensure you cover:

- [ ] **Happy path**: Normal operation with valid inputs
- [ ] **Null handling**: All nullable parameters tested with null
- [ ] **Empty values**: Empty strings, empty lists, etc.
- [ ] **Boundary conditions**: Min/max values, edge cases
- [ ] **Error paths**: Exception handling, failure scenarios
- [ ] **State changes**: For stateful components (ViewModels, etc.)
- [ ] **Concurrent operations**: For coroutines and flows
- [ ] **Platform-specific behavior**: If applicable

## Common Test Assertions

```kotlin
// Equality
assertEquals(expected, actual)
assertNotEquals(unexpected, actual)

// Boolean
assertTrue(condition)
assertFalse(condition)

// Null checks
assertNull(value)
assertNotNull(value)

// Collections
assertContentEquals(expectedList, actualList)
assertTrue(list.contains(element))
assertEquals(5, list.size)

// Exceptions
assertFailsWith<IllegalArgumentException> {
    // code that should throw
}

// Results
assertTrue(result.isSuccess)
assertTrue(result.isFailure)
```

## Tips & Best Practices

1. **Keep tests isolated**: Each test should be independent
2. **Use descriptive names**: `testFormatTitle_WithNullRocket_ReturnsLaunchName`
3. **Test one thing**: One assertion per test (when possible)
4. **Mock external dependencies**: Don't make real API calls
5. **Use test data builders**: Create reusable mock data factories
6. **Clean up resources**: Use @AfterTest for cleanup
7. **Document complex scenarios**: Add comments for non-obvious tests
8. **Test async code properly**: Use runTest and advanceUntilIdle
9. **Avoid test interdependence**: Tests should pass in any order
10. **Keep tests fast**: Unit tests should run in milliseconds

## Examples Repository

Refer to existing tests for patterns:
- `UserAgentUtilTest.kt` - Simple utility testing
- `DateTimeUtilTimelineTest.kt` - String parsing and formatting
- `EnvironmentManagerTest.kt` - Configuration testing

## Questions?

When in doubt:
1. Look at existing test patterns
2. Keep it simple - unit tests should be straightforward
3. Focus on behavior, not implementation details
4. Test what could break, not what can't
