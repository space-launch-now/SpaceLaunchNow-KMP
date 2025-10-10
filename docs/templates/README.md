# Test Templates

Copy these templates when creating new tests. Replace placeholders in [BRACKETS] with actual values.

## Available Templates

### 1. BasicTestTemplate.kt
**Use for**: Utility classes, formatters, parsers, pure functions

**Placeholders to replace:**
- `[PACKAGE]` - Package name (e.g., `util`, `data.model`)
- `[CLASS_NAME]` - Class being tested (e.g., `LaunchFormatUtil`)
- `[MethodName]` - Method being tested (e.g., `FormatLaunchTitle`)
- `[Scenario]` - Test scenario (e.g., `WithNullInput`, `WithLongName`)
- `[ExpectedBehavior]` - What should happen (e.g., `ReturnsDefault`, `ThrowsException`)

**Example:**
```kotlin
// Replace:
class [CLASS_NAME]Test

// With:
class LaunchFormatUtilTest
```

### 2. RepositoryTestTemplate.kt
**Use for**: Repository implementations that interact with APIs

**Placeholders to replace:**
- `[PACKAGE]` - Package name (e.g., `data.repository`)
- `[REPOSITORY_NAME]` - Repository class name (e.g., `LaunchRepositoryImpl`)
- `[API_INTERFACE]` - API interface name (e.g., `LaunchesApi`)
- `[ReturnType]` - API method return type
- `[MockData]` - Mock response data
- `[methodName]` - Method being tested
- `[DataType]` - Data type returned by repository

**Example:**
```kotlin
// Replace:
class [REPOSITORY_NAME]Test

// With:
class LaunchRepositoryImplTest
```

### 3. ViewModelTestTemplate.kt
**Use for**: ViewModel classes with StateFlows and coroutines

**Placeholders to replace:**
- `[PACKAGE]` - Package name (e.g., `ui.viewmodel`)
- `[VIEWMODEL_NAME]` - ViewModel class name (e.g., `NextUpViewModel`)
- `[RepositoryInterface]` - Repository interface (e.g., `LaunchRepository`)
- `[DataType]` - Type of data managed by ViewModel
- `[loadMethod]` - Method that loads data (e.g., `fetchNextLaunch`)
- `[methodName]` - Specific method being tested

**Example:**
```kotlin
// Replace:
class [VIEWMODEL_NAME]Test

// With:
class NextUpViewModelTest
```

## Quick Start

1. **Copy the appropriate template**
   ```bash
   cp docs/templates/BasicTestTemplate.kt \
      composeApp/src/commonTest/kotlin/me/calebjones/spacelaunchnow/util/LaunchFormatUtilTest.kt
   ```

2. **Replace all placeholders**
   - Search for `[` and replace each placeholder
   - Remove unused test methods
   - Add component-specific tests

3. **Run the tests**
   ```bash
   ./gradlew :composeApp:jvmTest --tests "*LaunchFormatUtilTest"
   ```

## Template Selection Guide

| Component Type | Template to Use | Example |
|----------------|-----------------|---------|
| Utility function | BasicTestTemplate | `LaunchFormatUtil` |
| Data model | BasicTestTemplate | `LaunchStatus` |
| Parser/Formatter | BasicTestTemplate | `DateTimeUtil` |
| Repository | RepositoryTestTemplate | `LaunchRepositoryImpl` |
| ViewModel | ViewModelTestTemplate | `NextUpViewModel` |
| Extension function | BasicTestTemplate | `LaunchesApiExtensions` |

## Common Patterns

### Naming Tests
```kotlin
@Test
fun testMethodName_Scenario_ExpectedBehavior()

// Examples:
@Test
fun testFormatTitle_WithNullRocket_ReturnsLaunchName()

@Test
fun testFetchLaunches_ApiError_ReturnsFailure()

@Test
fun testLoadData_Success_EmitsData()
```

### Assertions to Use

**Basic:**
```kotlin
assertEquals(expected, actual)
assertTrue(condition)
assertNull(value)
```

**Collections:**
```kotlin
assertContentEquals(expectedList, actualList)
assertEquals(5, list.size)
```

**Exceptions:**
```kotlin
assertFailsWith<IllegalArgumentException> { /* code */ }
```

**Results:**
```kotlin
assertTrue(result.isSuccess)
result.onSuccess { data -> /* assertions */ }
```

## Tips

1. **Start simple** - Copy template, replace basics, get it running
2. **Add gradually** - Start with happy path, then add edge cases
3. **One concept per test** - Each test should verify one thing
4. **Descriptive names** - Future developers should understand from name alone
5. **Clean up** - Remove template comments and unused code

## Full Documentation

For detailed examples and patterns, see:
- [TESTING_GUIDE.md](../TESTING_GUIDE.md)
- [TESTING_QUICK_REF.md](../TESTING_QUICK_REF.md)
- [TESTING_TASKS.md](../TESTING_TASKS.md)
