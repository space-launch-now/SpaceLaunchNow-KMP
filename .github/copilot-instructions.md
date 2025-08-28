# Copilot Instructions for SpaceLaunchNow KMP

## Project Goals - MVP Application

**Priority Platform**: Android (primary focus)

**Target Features**:
1. **Home Page**: Prominent featured launch, upcoming launches list, news events, updates feed
2. **List Page**: Tabbed interface for upcoming/previous launches
3. **Launch Detail Page**: Comprehensive launch information view
4. **Event Detail Page**: Detailed event information display
5. **Firebase Notifications**: Push notification system for launch updates
6. **Android Widget**: Simple list widget showing upcoming launches

## 🚧 Current Status: API 2.4.0 Upgrade IN PROGRESS

**✅ Completed**:
- Upgraded from Launch Library 2.3.1 to 2.4.0
- OpenAPI generation working with `ll_2.4.0.json` spec
- Dynamic user agent detection implemented
- Repository pattern updated for new API structure

**🔄 In Progress**:
- API Extension Functions pattern to handle complex parameter lists - need to complete for all Open API methods
- Import resolution issues may require IDE refresh/rebuild
- Some legacy code still references old API structure

**🔄 TODO**: 
- Home Page: Implement featured launch and news feed
- List Page: Implement tabbed interface for upcoming/previous launches
- Launch Detail Page: Implement detailed launch information view
- Event Detail Page: Implement detailed event information display
- Firebase Notifications: Implement push notification system for launch updates

**⚠️ Known Issues**:
- Generated API methods have 70+ positional parameters (see Extension Functions section)
- Some model imports may be unresolved until full rebuild
- Legacy test files need updating for new API structure

## Project Architecture

This is a **Kotlin Multiplatform Compose** application targeting Android, Desktop (JVM), and iOS, consuming The Space Devs Launch Library API (**now v2.4.0**) to display space launch information.

### Generated API Client Pattern
- **OpenAPI Generator**: Uses `openApiGenerate` task with `ll_2.4.0.json` spec to generate API clients into `me.calebjones.spacelaunchnow.api.*`
- **Generated Package**: `me.calebjones.spacelaunchnow.api` contains `LaunchesApi`, `AgenciesApi`, etc.
- **Regeneration**: Run `./gradlew openApiGenerate` when API spec changes
- **Config File**: `composeApp/openapi-config.yaml` with jvm-ktor library configuration

### ⭐ API Extension Functions Pattern (IMPORTANT)
**Problem**: Generated API methods have 70+ parameters, making them very hard to use:
```kotlin
// Generated method (DON'T USE DIRECTLY)
launchesApi.launchesMiniList(null, null, null, null, null, ...) // 70+ parameters!
```

**Solution**: Use extension functions in `api/extensions/LaunchesApiExtensions.kt`:
```kotlin
// Clean extension function (USE THIS)
launchesApi.getLaunchMiniList(
    limit = 10,
    upcoming = true,
    ordering = "net"
)
```

**Extension Functions Available**:
- `LaunchesApi.getLaunchMiniList()` - Clean interface for basic launch lists
- `LaunchesApi.getLaunchList()` - Clean interface for detailed launch lists
- More can be added as needed

**TODO**: Create similar extensions for `AgenciesApi`, `LaunchersApi`, etc. when needed

### Platform-Specific Configuration
- **Common Main**: Business logic in `composeApp/src/commonMain/kotlin`
- **Android**: Uses `MainActivity.kt` with `MainApplication.kt` for Koin setup
- **Desktop**: Entry point at `me.calebjones.spacelaunchnow.MainKt`
- **iOS**: Framework exported as `ComposeApp` with static binaries

### Dependency Injection (Koin)
- **Module Structure**: `AppModule.kt` (ViewModels + Repositories), `NetworkModule.kt` (HTTP clients), `ApiModule.kt` (generated API instances)
- **Platform Config**: `nativeConfig()` expect/actual pattern for platform-specific Koin configuration
- **API Authentication**: Uses `setApiKey()` and `setApiKeyPrefix("Bearer")` on generated API clients
- **Dynamic User Agent**: `UserAgentUtil.getUserAgent()` provides platform-specific user agent strings

### UI Architecture
- **Responsive Layout**: `isTabletOrDesktop()` switches between `PhoneLayout` and `TabletDesktopLayout`
- **Navigation**: Simple sealed class `Screen` with Home/Schedule/Settings
- **ViewModels**: Standard MVVM with StateFlow (e.g., `NextUpViewModel.fetchNextLaunch()`)
- **Android Priority**: Focus on Android-specific features like widgets and Firebase integration

## Key Patterns

### API Extension Functions (NEW PATTERN)
**Location**: `api/extensions/LaunchesApiExtensions.kt`
**Purpose**: Provide clean, named-parameter interfaces for generated API methods

```kotlin
// Instead of this mess:
launchesApi.launchesMiniList(null, null, null, /*...70+ params...*/)

// Use this:
launchesApi.getLaunchMiniList(
    limit = 10,
    upcoming = true,
    ordering = "net"
)
```

**Key Benefits**:
- Only specify parameters you need
- Named parameters make code readable
- Easy to extend with more commonly-used parameters
- Hides complexity of generated API

**Available Extensions**:
- `getLaunchMiniList()` - For basic launch data (`PaginatedLaunchBasicList`)
- `getLaunchList()` - For detailed launch data (`PaginatedLaunchNormalList`)

**Adding New Parameters**: When you need new filtering options, add them to extension function signature:
```kotlin
suspend fun LaunchesApi.getLaunchList(
    // Existing params...
    program: List<Int>? = null,     // Add new ones easily
    windowStartGt: Instant? = null,
    spacecraftConfigIds: List<Int>? = null
) // Implementation handles mapping to all 70+ generated parameters
```

### API Response Handling
- **Type Changes**: API 2.4.0 uses `PaginatedLaunchBasicList`, `PaginatedLaunchNormalList`, `LaunchDetailed`
- **UUID Conversion**: Launch IDs now require `UUID.fromString(id)` conversion
- **Result Pattern**: Repository methods return `Result<T>` wrapping API responses

Example Repository Pattern (UPDATED):
```kotlin
override suspend fun getUpcomingLaunches(limit: Int): Result<PaginatedLaunchBasicList> {
    return try {
        val response = launchesApi.getLaunchMiniList(
            limit = limit,
            upcoming = true,
            ordering = "net"
        )
        Result.success(response.body())
    } catch (e: ResponseException) {
        Result.failure(e)
    }
}
        Result.failure(e)
    }
}
```

### Environment Configuration
- **API Keys**: Uses `.env` file with `API_KEY=your_key_here` (gitignored)
- **Environment Access**: `EnvironmentManager.getEnv("API_KEY", "default_key")` pattern

### Build Tasks
- **Code Generation**: `openApiGenerate` generates API clients from OpenAPI spec
- **Desktop Packaging**: Uses `compose.desktop.application` with `mainClass = "me.calebjones.spacelaunchnow.MainKt"`
- **Resource Handling**: Custom `ProcessResources` task copies commonMain resources

## Development Workflows

### Java 21 Requirement
**CRITICAL**: This application requires **Java 21** to run. The Compose Hot Reload plugin specifically needs JetBrains JDK 21.

**Windows Setup:**
- Set JAVA_HOME or use Gradle property: `./gradlew :composeApp:desktopRun "-Dorg.gradle.java.home=D:\tools\Android Studio Q4-2024\jbr"`
- VS Code tasks are pre-configured with correct Java path

### API Client Updates
1. **Current Status**: ✅ Successfully upgraded from Launch Library 2.3.1 to 2.4.0
2. **API Spec**: Using `ll_2.4.0.json` spec file in project root
3. **Generation**: Run `./gradlew openApiGenerate` to regenerate API clients
4. **Package**: Generated clients in `me.calebjones.spacelaunchnow.api.*`
5. **Extensions**: Use extension functions in `api/extensions/` for clean API calls
6. **Testing**: Run integration tests to verify compatibility

### Working with Generated API
**❌ Don't Use Generated Methods Directly**:
```kotlin
// This is hard to work with:
launchesApi.launchesMiniList(null, null, null, /*...70 params...*/)
```

**✅ Use Extension Functions**:
```kotlin
// This is clean and maintainable:
launchesApi.getLaunchMiniList(limit = 10, upcoming = true)
```

**Adding New Extensions**:
1. Add parameters to existing extension functions in `LaunchesApiExtensions.kt`
2. Create new extension functions for other API classes as needed
3. Map new parameters to the corresponding generated method parameters
4. Update repository methods to use the new extension parameters

### Testing API Integration
- **Test Files**: `SimpleApiTest.kt`, `ApiTest.kt` for manual API testing (⚠️ May need updates for 2.4.0)
- **Integration Tests**: `GeneratedApiClientIntegrationTest.kt` verifies generated client functionality
- **Example Usage**: `GeneratedApiClientExample.kt` shows proper API client usage patterns (⚠️ May need updates)

### Adding New Screens/Features
1. Add screen to `Screen.kt` sealed class
2. Create ViewModel extending `ViewModel` with StateFlow properties
3. Register ViewModel in `AppModule.kt` using `viewModelOf(::YourViewModel)`
4. Add repository if external data needed, register with `bind<Interface>()`

### Platform-Specific Code
- **Expect/Actual**: Use for platform-specific implementations (see `nativeConfig()` pattern)
- **Platform Detection**: `getPlatform().name` for conditional logic
- **Resources**: Place in `commonMain/resources`, accessed via Compose Resources

## Common Gotchas

- **API Generation**: Generated files are not committed; must run `openApiGenerate` after clean checkout
- **Extension Functions**: Always use extension functions instead of calling generated API methods directly
- **70+ Parameters**: Generated methods have too many parameters - this is why we use extensions
- **UUID Conversion**: Launch IDs are now UUIDs, use `UUID.fromString(id)` when needed
- **Import Resolution**: Some imports may be unresolved until IDE refresh/rebuild after generation
- **Environment Variables**: `.env` file must exist in project root for API key resolution
- **Desktop Main Class**: Ensure `mainClass` in build.gradle.kts points to correct `MainKt` file
- **Koin Scoping**: ViewModels are automatically scoped; repositories are singletons across app lifecycle

## 📝 TODO: Extension Functions
- [ ] Add more commonly-used parameters to existing extensions (program, lspId, rocketConfigurationId)
- [ ] Create utility extensions for common filtering patterns (getCrewedLaunches, getLaunchesByCompany)
- [ ] Update all existing test files to use extension functions instead of generated methods
