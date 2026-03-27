<!--
SYNC IMPACT REPORT
==================
Version Change: 1.5.0 → 1.6.0

Modified Principles:
  - II. Pattern-Based Consistency (UPDATED)
    - Added Stale-While-Revalidate caching pattern as REQUIRED for all Repository implementations
    - Pattern: Check stale data first → Fresh cache → Return stale immediately → Network fallback
    - Ensures fast UI rendering with offline-first user experience

Principles (Current):
  - I. Mobile-First Development (Android & iOS Equal Priority)
  - II. Pattern-Based Consistency (UPDATED - added caching pattern)
  - III. Accessibility & User Experience
  - IV. CI/CD & Conventional Commits (NON-NEGOTIABLE)
  - V. Code Generation & API Management
  - VI. Multiplatform Architecture
  - VII. Testing Standards
  - VIII. Jetpack Compose Best Practices

Templates Status:
  ✅ plan-template.md - No updates required
  ✅ spec-template.md - No updates required
  ✅ tasks-template.md - No updates required

Follow-up TODOs:
  - None - pattern already implemented in existing repositories

Ratification Date: 2026-01-21 (initial adoption)
Last Amendment: 2026-03-26
Amendment Reason: Added Stale-While-Revalidate caching pattern requirement to Principle II
-->

# SpaceLaunchNow KMP Constitution

## Core Principles

### I. Mobile-First Development (Android & iOS Equal Priority)

Android and iOS are **equal priority target platforms** for SpaceLaunchNow KMP. All feature development, testing, and deployment workflows MUST deliver functionality for both mobile platforms. Desktop implementation serves as a secondary target and MUST NOT block mobile feature releases.

**Rationale**: Both Android and iOS represent critical market segments requiring equal attention. The hybrid release strategy (automatic Android CI/CD at $0.50/build, manual iOS CI/CD at $6.00/build) optimizes cost while maintaining equal feature parity. Desktop serves as a development/testing platform but is not a primary user-facing target.

### II. Pattern-Based Consistency

Code MUST follow established project patterns without deviation unless explicitly justified and documented. Key patterns include:

- **Launch Title Formatting**: MUST use `LaunchFormatUtil.formatLaunchTitle()` for all launch displays
- **API Extension Functions**: MUST use extension functions (e.g., `getLaunchMiniList()`) instead of calling generated API methods directly with 70+ parameters
- **Repository Pattern**: MUST return `Result<T>` wrapping API responses with proper error handling
- **Stale-While-Revalidate Caching**: All Repository implementations with SQLDelight caching MUST follow this pattern:
  1. Check for stale (expired) cache data first (for fallback)
  2. Return fresh cache immediately if available and not forcing refresh
  3. Return stale cache immediately if fresh cache expired (fast UI render)
  4. Fetch from network only when no cache exists or force refresh requested
  5. On network errors, return stale cache if available before failing
  6. Use `DataSource.CACHE`, `DataSource.STALE_CACHE`, or `DataSource.NETWORK` to indicate data freshness
- **DateTime Handling**: MUST use `DateTimeUtil` class for consistency supporting UTC toggle
- **MVVM Architecture**: ViewModels MUST extend `ViewModel` with `StateFlow` properties

**Rationale**: Pattern consistency eliminates cognitive overhead, reduces bugs from incorrect implementations, and enables team members to understand and modify any part of the codebase quickly. Stale-while-revalidate ensures users see content immediately even with poor connectivity, providing offline-first UX.

### III. Accessibility & User Experience

All UI components MUST be accessible and user-friendly:

- **Dual Previews Required**: All new Composables MUST include BOTH light and dark theme previews:
  - Light preview: `@Preview @Composable private fun ComponentNamePreview()` using default `SpaceLaunchNowPreviewTheme()`
  - Dark preview: `@Preview @Composable private fun ComponentNameDarkPreview()` using `SpaceLaunchNowPreviewTheme(isDark = true)`
  - Naming convention: Base preview uses component name + "Preview", dark variant uses component name + "DarkPreview"
- **Component Reuse**: MUST prefer existing components from `ui/components/` before creating new ones
- **File Size**: Keep Composable files as short as possible by extracting reusable components
- **Accessibility**: MUST implement proper content descriptions, semantic properties, and keyboard navigation

**Rationale**: Accessible design expands user reach and ensures compliance with platform accessibility standards. Dual previews ensure UI components render correctly in both light and dark themes, catching theme-specific issues during development rather than in production. Preview consistency enables rapid visual verification across the entire component library.

### IV. CI/CD & Conventional Commits (NON-NEGOTIABLE)

All commits MUST follow conventional commit format. The CI/CD pipeline is automated and **directly triggered by commit message format**:

- **Format**: `<type>(<scope>): <subject>` where type is `feat`, `fix`, `chore`, `docs`, `refactor`, `test`, or `ci`
- **Breaking Changes**: MUST use `!` (e.g., `feat!:`) or include `BREAKING CHANGE:` in footer
- **PR to master**: Runs tests and builds debug APK (no deployment)
- **Merge to master**: Auto-bumps version, generates changelog, builds signed Android release, deploys to Firebase Distribution, creates GitHub Release
- **Version Codes**: MUST NEVER decrease version codes (Google Play requirement)

**Rationale**: Automated versioning and deployment reduce human error and accelerate release velocity. Conventional commits enable semantic versioning automation and automatic changelog generation.

### V. Code Generation & API Management

Generated code MUST NOT be committed to version control. All API interactions MUST use the generated client pattern:

- **OpenAPI Generation**: Run `./gradlew openApiGenerate` to generate API clients from `ll_2.4.0.json` spec
- **Extension Functions**: MUST wrap all generated API methods with clean extension functions providing named parameters
- **No Direct Calls**: MUST NEVER call generated API methods directly (e.g., `launchesMiniList(null, null, ...)` with 70+ params)
- **Regeneration**: MUST regenerate after spec updates and after clean checkout

**Rationale**: Generated code bloat in version control creates merge conflicts and increases repository size. Extension functions provide maintainable, readable interfaces while hiding generated code complexity.

### VI. Multiplatform Architecture

Code organization MUST respect the Kotlin Multiplatform structure and expect/actual pattern:

- **All Targets MUST Build**: Android, iOS, and Desktop builds MUST all succeed. Changes that break any platform build are NOT acceptable, even if that platform is not the primary focus. Mobile-only dependencies (e.g., Datadog KMP, RevenueCat, kermit-crashlytics) MUST be scoped to `androidMain`/`iosMain` source sets, never `commonMain`.
- **Common First**: Business logic MUST reside in `commonMain` unless platform-specific requirements exist
- **Platform-Specific**: Use `expect`/`actual` pattern for platform implementations (e.g., `nativeConfig()` for Koin setup). Desktop actuals MUST provide no-op stubs for mobile-only SDK features.
- **Dependency Injection**: MUST use Koin with proper module structure (`AppModule.kt`, `NetworkModule.kt`, `ApiModule.kt`)
- **No Magic Strings/IDs**: MUST use data classes and constants instead of hardcoded values

**Rationale**: Proper multiplatform architecture maximizes code reuse across Android, Desktop, and iOS while maintaining platform-specific optimization paths. All three platform builds succeeding is a hard requirement — a broken desktop build blocks desktop development and indicates incorrect dependency scoping.

### VII. Testing Standards

All code changes MUST include appropriate test coverage. Testing is a critical quality gate that MUST NOT be bypassed:

- **Integration Tests Required**: All API client interactions MUST have integration tests verifying correct request/response handling
- **Repository Tests**: Repository implementations MUST have tests covering success and failure paths with mocked API responses
- **ViewModel Tests**: ViewModels MUST have unit tests verifying state transitions and business logic
- **UI Tests**: Critical user journeys (launch details, list navigation, settings) MUST have UI/instrumentation tests
- **Test Location**: Tests MUST reside in appropriate source sets (`commonTest`, `androidTest`, `jvmTest`, `iosTest`)
- **CI Gate**: PRs MUST pass all tests before merge; failing tests block deployment

**Rationale**: Testing catches regressions early, documents expected behavior, and provides confidence for refactoring. The multiplatform nature of this project makes automated testing essential for verifying behavior across Android, iOS, and Desktop targets.

### VIII. Jetpack Compose Best Practices

Compose patterns per official Android guidance. Ref: https://developer.android.com/jetpack/compose/documentation

**Composables**: `modifier: Modifier = Modifier` first param; PascalCase names; small files → extract to `ui/components/`

**State**: Hoist to screen; `collectAsStateWithLifecycle()` not `collectAsState()`; explicit `remember(key)` keys; NO MutableState in ViewModel

**Effects**: `LaunchedEffect` = auto/init; `rememberCoroutineScope` = user-triggered; `DisposableEffect` = cleanup

**Performance**: Defer reads to lambda/draw; `items(key = { it.id })`; `derivedStateOf` for computed; `@Stable`/`@Immutable` on UI state classes

**Previews**: `SpaceLaunchNowPreviewTheme()` not `SpaceLaunchNowTheme()`; fake data only; dual light/dark (see Principle III)

**Accessibility**: `contentDescription` on interactive; `null` for decorative; `semantics { role = }` for custom clickables; 48dp touch targets

**Rationale**: Official Android patterns prevent recomposition bugs, optimize performance at scale, and ensure consistent behavior across Android, Desktop, and iOS Compose targets.

## Mobile Development Standards

### Target Features (MVP Priority Order)

1. **Home Page**: Prominent featured launch, upcoming launches list, news events, updates feed
2. **List Page**: Tabbed interface for upcoming/previous launches
3. **Launch Detail Page**: Comprehensive launch information view
4. **Event Detail Page**: Detailed event information display
5. **Firebase Notifications**: Push notification system for launch updates
6. **Widget**: Simple list widget showing upcoming launches

### Technical Requirements

- **Java Version**: MUST use Java 21 (JetBrains JDK 21 for Compose Hot Reload)
- **API Version**: Launch Library API 2.4.0 with UUID-based launch IDs
- **State Management**: StateFlow for reactive UI updates
- **Error Handling**: Result pattern with proper exception wrapping
- **Environment Config**: `.env` file for API keys (gitignored, required for builds)

## Development Workflow Standards

### Feature Development Process

1. **Screen Addition**: Add to `Screen.kt` sealed class
2. **ViewModel Creation**: Extend `ViewModel` with StateFlow, register in `AppModule.kt` using `viewModelOf()`
3. **Repository Setup**: Create repository interface, implement with generated API clients, register with `bind<Interface>()`
4. **UI Implementation**: Create Composables in separate files with previews, reuse components from `ui/components/`
5. **Testing**: Integration tests for API client usage, UI tests for critical user journeys

### Code Quality Gates

- **Import Hygiene**: Proper imports at file top, short class names (e.g., `Purchases.sharedInstance` not fully qualified)
- **Pattern Compliance**: Verify use of required patterns (LaunchFormatUtil, API extensions, DateTimeUtil)
- **Preview Verification**: All Composables have working previews
- **Accessibility Audit**: Content descriptions and semantic properties implemented

## Governance

This constitution supersedes all conflicting practices in the codebase. All pull requests MUST verify compliance with these principles before approval. Any deviation requires explicit justification in PR description and approval from project maintainers.

**Amendment Process**: Constitution changes require documentation of rationale, migration plan for affected code, and version bump following semantic versioning rules (MAJOR for backward-incompatible governance changes, MINOR for new principles/sections, PATCH for clarifications).

**Runtime Guidance**: For day-to-day development guidance, reference `.github/copilot-instructions.md` which provides detailed implementation patterns and common gotchas aligned with these principles.

**Version**: 1.6.0 | **Ratified**: 2026-01-21 | **Last Amended**: 2026-03-26
