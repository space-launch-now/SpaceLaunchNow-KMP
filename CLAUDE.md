# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Space Launch Now is a Kotlin Multiplatform Compose app (Android, iOS, Desktop, Wear OS) backed by The Space Devs Launch Library 2 (LL2) and SNAPI APIs. Indie freemium with RevenueCat billing, AdMob, Firebase, and Datadog RUM.

## Toolchain

- **JDK 21** required (JetBrains JDK recommended — Compose Hot Reload depends on it). Kotlin compiles to `JVM_11`.
- Kotlin 2.1, Compose Multiplatform, Koin, Ktor, SQLDelight, DataStore, kotlinx-datetime/-coroutines/-serialization.
- Two Gradle modules: `:composeApp` (phone + iOS + desktop), `:wearApp` (Wear OS standalone, same `applicationId`).
- `.env` file at repo root must exist before any build (see `.env.example`). API keys, RevenueCat keys, AdMob unit IDs, Datadog tokens, Maps key, and the debug-menu TOTP secret are loaded into `BuildConfig` and manifest placeholders by `composeApp/build.gradle.kts`.

## Commands

```bash
# Generate API clients (LL2 + SNAPI). Generated sources are NOT committed —
# run after a clean checkout or after editing schema/openapi-config-*.yaml.
./gradlew openApiGenerate              # LL2 only
./gradlew generateSnapiClient          # SNAPI v4 only
./gradlew generateAllApiClients        # both

# Run / install
./gradlew installDebug                 # phone APK to device/emulator
./gradlew :wearApp:installDebug        # Wear OS APK (needs API 30+ Wear emulator)
./gradlew desktopRun                   # JVM desktop app
./gradlew compileKotlinDesktop         # fast desktop compile check

# iOS — open iosApp/iosApp.xcodeproj in Xcode, run iosApp scheme.
# After fresh clone or .env change: ./scripts/generate-ios-secrets.sh

# Tests
./gradlew test                         # all platforms
./gradlew :composeApp:jvmTest          # commonMain tests via JVM (fastest)
./gradlew :composeApp:jvmTest --tests "me.calebjones.spacelaunchnow.util.LaunchFormatUtilTest"
./gradlew ktlintCheck                  # formatting (soft-failure in CI)
```

Debug builds use `applicationIdSuffix = ".kmpdebug"` (separate install slot from release).

## Architecture

Target architecture is documented in `docs/architecture/ARCHITECTURE_OVERVIEW.md` and ADRs `0001`–`0006`. **Read the ADRs before any non-trivial repository or domain change.** The codebase is mid-migration to a layered architecture:

```
UI (Compose + ViewModels)
   ↓ domain types only
domain/      model/ + mapper/ + (planned) usecase/
   ↓ domain types only
data/repository/   stale-while-revalidate, returns Result<…>
   ↓
SQLDelight cache  +  Generated OpenAPI client (api.launchlibrary.*, api.snapi.*)
```

**Golden rules from ADR-0001:**
1. `me.calebjones.spacelaunchnow.api.*.models.*` may be imported **only** by `api/extensions/` and `domain/mapper/`. Everything else uses `domain/model/*`.
2. ViewModels depend on repositories (or use cases when they exist), never on mappers or API extensions directly.
3. Cache blobs should be domain JSON with a `schema_version` (in progress — see ADR-0004).

Known debt to avoid making worse: god repositories (e.g. `LaunchRepositoryImpl` ~1,400 lines), monolithic Compose screens (>1,000 lines), and the triple-nested `Result<DataResult<PaginatedResult<T>>>` wrapper. Don't add to these — split when touching them.

## Generated API client pattern

OpenAPI generates from `schema/ll_2.4.0.json` and `schema/snapi_v4.yaml` into `composeApp/src/openApiLL/` and `composeApp/src/openApiSNAPI/`. Generated methods have **70+ positional parameters** — never call them directly.

Always go through extension functions in `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/api/extensions/`:

```kotlin
// ✅ Use this
launchesApi.getLaunchMiniList(limit = 10, upcoming = true, ordering = "net")

// ❌ Never this
launchesApi.launchesMiniList(null, null, null, /* …70 params… */)
```

When you need a new filter, add the parameter to the extension signature and map it through to the generated call. Do not introduce a new direct call site.

Auth: `setApiKey(...)` + `setApiKeyPrefix("Bearer")` on the generated client. Launch IDs are UUIDs (`UUID.fromString(id)` when needed).

## DI (Koin)

- `di/AppModule.kt` — ViewModels (`viewModelOf`) + repositories (`bind<Interface>()` singletons).
- `di/NetworkModule.kt` — Ktor clients with platform engines (Android/Darwin/CIO).
- `di/ApiModule.kt` — generated API client instances.
- `di/AnalyticsModule.kt`, `di/ImageLoaderModule.kt` — supporting graph.
- Platform-specific Koin setup goes through the `nativeConfig()` expect/actual pattern.

When adding a screen: register the VM in `AppModule.kt` (`viewModelOf(::FooViewModel)`) and the repository with `single { FooRepositoryImpl(...) } bind FooRepository::class`.

## UI conventions

- Add screens to the `Screen` sealed class in `navigation/Screen.kt`.
- Responsive layout switches via `isTabletOrDesktop()` between `PhoneLayout` and `TabletDesktopLayout`.
- Prefer existing components in `ui/components/` over building new ones.
- Use `LaunchFormatUtil.formatLaunchTitle(launch)` for any "LSP | Rocket Configuration" title — never roll a one-off.
- Use `DateTimeUtil` for any datetime formatting (it honors the user's UTC toggle).
- **Every new Composable gets dual previews** — light + dark — wrapped in `SpaceLaunchNowPreviewTheme()` and `SpaceLaunchNowPreviewTheme(isDark = true)`.

## Versioning & releases

`version.properties` is the single source of truth. Both phone and Wear read from it; CI bumps `versionBuildNumber` per release.

- Phone `versionCode = 1_100_000_000 + (build * 2)`
- Wear  `versionCode = 1_100_000_000 + (build * 2) + 1`

Both share `applicationId = me.calebjones.spacelaunchnow`, so wear must always outrank phone in the same release. **Never decrease a versionCode** — Play requires monotonic forever, per applicationId.

## Commits & CI

Conventional Commits are mandatory — they drive automated versioning:

- `feat:` → minor bump · `fix:`/`chore:` → patch bump · `feat!:` or `BREAKING CHANGE:` footer → major bump.
- Scopes encouraged: `feat(ui):`, `fix(api):`, `chore(deps):`.
- Don't suggest "Update files" / "Fix bug" style messages.
- Don't bump major without human review.

Workflows: `pr-validation.yml` (PRs → tests + debug APK) and `master-deploy.yml` (merges to `main` → version bump, signed Android release, Firebase Distribution, GitHub Release). iOS builds are triggered manually (cost ~12× Android). Full details: `docs/cicd/CICD_PIPELINE.md`, `docs/cicd/HYBRID_RELEASE_STRATEGY.md`.

## Secrets — do not commit

`.env`, `keystore.properties`, `*.keystore`/`*.jks`, `google-services.json`, `GoogleService-Info.plist`, `iosApp/iosApp/Secrets.plist`. CI provides these via secrets/base64-encoded artifacts.

## Worktree-based subagent work

Subagents that need an isolated workspace use `isolation: "worktree"` in the Agent tool call. The harness creates a linked git worktree from the current branch, starts a fresh session inside it, and the SessionStart hook in `.claude/settings.json` automatically copies gitignored secret files from the main checkout before any build runs.

**What gets copied (only if missing in the worktree):**
- `.env` — required for every Gradle build
- `composeApp/google-services.json` — Firebase Android config
- `iosApp/iosApp/GoogleService-Info.plist` — Firebase iOS config
- `iosApp/iosApp/Secrets.plist` — iOS secrets (if present; generated by `scripts/generate-ios-secrets.sh` from `.env`)

**Requirement:** `.claude/settings.json` and `.claude/scripts/bootstrap-worktree.sh` must be committed to the branch the worktree is created from. Uncommitted, they won't exist in the worktree and the hook won't fire.

**Adding a file to the sync list:** edit the `files=(...)` array in [`.claude/scripts/bootstrap-worktree.sh`](.claude/scripts/bootstrap-worktree.sh).

**Platform:** the hook runs `bash`, which works on macOS, Linux, and Windows via Git Bash.

## Skills & docs lookup

- **`android-cli` skill** — use the `android` command-line tool for SDK management, emulator/device control, deployment, and environment diagnostics rather than hand-rolling `adb`/`sdkmanager` invocations.
- **Compose-related skills** — when working on Compose Multiplatform UI, animations, navigation, or Material 3 / Wear Compose specifics, invoke the relevant Compose skill to pull in current API docs and idioms before guessing.
- Reach for these proactively when a task touches Android tooling or Compose APIs you haven't verified — beats inventing flag names or deprecated symbols from memory.

## Useful entry points

- App startup: `composeApp/src/androidMain/.../MainApplication.kt`, `MainActivity.kt`; desktop `MainKt`; iOS framework exports as `ComposeApp` (static).
- Common ViewModels & state: `ui/viewmodel/`, `ui/state/`.
- Feature screens: `ui/home/`, `ui/schedule/`, `ui/detail/`, `ui/explore/`, `ui/newsevents/`, `ui/onboarding/`, `ui/subscription/`.
- Notifications & widgets: `data/notifications/`, `widgets/` (commonMain) + platform implementations under `androidMain/widgets/`, `iosApp/LaunchWidget/`.
- Wear OS app: `wearApp/src/main/` — uses Wear Compose M3, ProtoLayout tiles, complications, and DataLayer for phone↔watch sync.
- Specs in flight: `specs/<feature>/` — check tasks.md before starting work in that area.


DO NOT ADD Claude as a co-author in commits.