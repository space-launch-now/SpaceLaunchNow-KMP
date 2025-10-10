# Unit Testing Quick Reference

Quick reference for writing tests in SpaceLaunchNow-KMP.

## 🚀 Getting Started in 30 Seconds

1. **Pick a task**: Open [TESTING_TASKS.md](TESTING_TASKS.md)
2. **Use AI (optional)**: Copy prompt from [AI_AGENT_PROMPTS.md](AI_AGENT_PROMPTS.md)
3. **Follow patterns**: Use [TESTING_GUIDE.md](TESTING_GUIDE.md)
4. **Run tests**: `./gradlew :composeApp:jvmTest`

## 📁 File Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/
│   │   └── me/calebjones/spacelaunchnow/
│   │       ├── util/
│   │       │   └── LaunchFormatUtil.kt        ← Source file
│   │       └── ...
│   └── commonTest/kotlin/
│       └── me/calebjones/spacelaunchnow/
│           ├── util/
│           │   └── LaunchFormatUtilTest.kt    ← Test file (same package)
│           └── ...
```

## ⚡ Test Template

```kotlin
package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals

class MyUtilTest {
    
    @Test
    fun testSomeFunction() {
        // Arrange
        val input = "test"
        
        // Act
        val result = MyUtil.process(input)
        
        // Assert
        assertEquals("expected", result)
    }
}
```

## 🎯 Common Patterns

### Testing Utilities (Pure Functions)
```kotlin
@Test
fun testFormatting() {
    val result = Formatter.format("input")
    assertEquals("expected", result)
}
```

### Testing Coroutines
```kotlin
@Test
fun testAsync() = runTest {
    val result = repository.getData()
    assertTrue(result.isSuccess)
}
```

### Testing ViewModels
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class ViewModelTest {
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
    fun testLoadData() = runTest {
        val vm = MyViewModel(mockRepo)
        vm.loadData()
        testScheduler.advanceUntilIdle()
        assertNotNull(vm.data.value)
    }
}
```

### Mocking Repositories
```kotlin
class MockRepository : LaunchRepository {
    var shouldFail = false
    
    override suspend fun getData(): Result<Data> {
        return if (shouldFail) {
            Result.failure(Exception("Test error"))
        } else {
            Result.success(mockData)
        }
    }
}
```

## ✅ Essential Assertions

```kotlin
// Equality
assertEquals(expected, actual)
assertNotEquals(unexpected, actual)

// Boolean
assertTrue(condition)
assertFalse(condition)

// Null
assertNull(value)
assertNotNull(value)

// Collections
assertContentEquals(expectedList, actualList)
assertEquals(5, list.size)

// Exceptions
assertFailsWith<IllegalArgumentException> {
    riskyOperation()
}

// Results
assertTrue(result.isSuccess)
assertTrue(result.isFailure)
```

## 🏃 Running Tests

```bash
# All tests
./gradlew :composeApp:allTests

# Common/JVM tests (fastest)
./gradlew :composeApp:jvmTest

# Specific test
./gradlew :composeApp:jvmTest --tests "*LaunchFormatUtilTest"

# With info logging
./gradlew :composeApp:jvmTest --info
```

## 📋 Test Checklist

For each component, test:
- ✅ Happy path (normal operation)
- ✅ Null inputs
- ✅ Empty values
- ✅ Boundary conditions
- ✅ Error cases
- ✅ Edge cases

## 🎨 Naming Conventions

### Test Files
- Source: `LaunchFormatUtil.kt`
- Test: `LaunchFormatUtilTest.kt`

### Test Methods
```kotlin
@Test
fun testMethodName_Scenario_ExpectedBehavior() { }

// Examples:
@Test
fun testFormatTitle_WithNullRocket_ReturnsLaunchName() { }

@Test
fun testFetchData_WhenApiError_SetsErrorState() { }
```

## 🚦 Priority Guide

### 🔥 Start Here (Easy, High Value)
1. Utility classes (`util/`)
2. Formatters and parsers
3. Pure functions

### ⭐ Next (Medium Effort)
1. API extensions
2. Repository implementations
3. Data transformations

### 🎯 Advanced (More Complex)
1. ViewModels (require mocking)
2. Storage/Cache
3. State management

## 💡 Tips

1. **Keep tests simple** - One thing per test
2. **Use descriptive names** - Future you will thank you
3. **Test edge cases** - That's where bugs hide
4. **Mock external deps** - No real API calls
5. **Run tests often** - Fast feedback loop

## 📚 Full Documentation

- **Task List**: [TESTING_TASKS.md](TESTING_TASKS.md)
- **Detailed Guide**: [TESTING_GUIDE.md](TESTING_GUIDE.md)
- **Examples**: Look in `composeApp/src/commonTest/kotlin/`

## 🐛 Common Issues

### "Cannot resolve symbol test"
Add to imports:
```kotlin
import kotlin.test.Test
import kotlin.test.assertEquals
```

### "Unresolved reference: runTest"
Add to imports:
```kotlin
import kotlinx.coroutines.test.runTest
```

### "This test is suspended"
Use `runTest` wrapper:
```kotlin
@Test
fun myTest() = runTest {
    // test code
}
```

## 📞 Need Help?

1. Check existing tests for patterns
2. Read [TESTING_GUIDE.md](TESTING_GUIDE.md)
3. Look for similar components
4. Keep it simple!
