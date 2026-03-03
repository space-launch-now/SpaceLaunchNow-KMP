<p align="center">
  <img src="https://thespacedevs-prod.nyc3.digitaloceanspaces.com/static/home/img/launcher.png" alt="Space Launch Now" width="200"/>
</p>

<h1 align="center">Space Launch Now</h1>

<p align="center">
  <strong>A Kotlin Multiplatform app for tracking space launches around the world</strong>
</p>

<p align="center">
  <a href="https://github.com/space-launch-now/SpaceLaunchNow-KMP/actions"><img src="https://img.shields.io/github/actions/workflow/status/space-launch-now/SpaceLaunchNow-KMP/pr-validation.yml?branch=main&style=flat-square&logo=github&label=CI" alt="CI Status"></a>
  <a href="https://github.com/space-launch-now/SpaceLaunchNow-KMP/releases"><img src="https://img.shields.io/github/v/release/space-launch-now/SpaceLaunchNow-KMP?style=flat-square&logo=rocket&label=Release" alt="Release"></a>
  <a href="LICENSE"><img src="https://img.shields.io/github/license/space-launch-now/SpaceLaunchNow-KMP?style=flat-square" alt="License"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose_Multiplatform-1.x-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose Multiplatform"></a>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=me.calebjones.spacelaunchnow"><img src="https://img.shields.io/badge/Google_Play-Download-34A853?style=for-the-badge&logo=googleplay&logoColor=white" alt="Get it on Google Play"></a>
  <a href="https://apps.apple.com/app/space-launch-now/id1399715731"><img src="https://img.shields.io/badge/App_Store-Download-0D96F6?style=for-the-badge&logo=appstore&logoColor=white" alt="Download on App Store"></a>
</p>

---

Built with **Compose Multiplatform** and powered by [The Space Devs](https://thespacedevs.com/) Launch Library API, Space Launch Now brings upcoming rocket launches, mission details, and space news to Android, iOS, and Desktop.

## Features

- **Upcoming Launches** &mdash; Live countdown timers, launch windows, and status updates
- **Launch Details** &mdash; Mission info, rocket specs, launch pad location, and agency details
- **Space News & Events** &mdash; Stay up to date with the latest happenings in spaceflight
- **Push Notifications** &mdash; Get alerts before launches you care about
- **iOS & Android Widgets** &mdash; Glanceable launch info right on your home screen
- **Dark Mode** &mdash; Full light and dark theme support
- **Multiplatform** &mdash; Android, iOS, and Desktop from a single codebase

## Architecture

```
composeApp/
├── src/
│   ├── commonMain/     # Shared business logic, UI, and networking
│   ├── androidMain/    # Android-specific integrations
│   ├── iosMain/        # iOS-specific integrations
│   └── desktopMain/    # Desktop (JVM) entry point
iosApp/                 # Xcode project, widgets, notifications
docs/                   # Architecture, CI/CD, and integration docs
```

| Layer | Technology |
|-------|-----------|
| **UI** | Compose Multiplatform |
| **Navigation** | Type-safe sealed class routing |
| **Networking** | Ktor + OpenAPI-generated client |
| **DI** | Koin |
| **State** | MVVM with StateFlow |
| **Persistence** | DataStore, SQLDelight |
| **Billing** | RevenueCat (KMP) |
| **Monitoring** | Datadog |

## Getting Started

### Prerequisites

- **JDK 21** (JetBrains JDK recommended)
- **Android Studio** / IntelliJ IDEA
- **Xcode 15+** (for iOS builds)

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/space-launch-now/SpaceLaunchNow-KMP.git
cd SpaceLaunchNow-KMP

# 2. Create your local environment file
cp .env.example .env
# Edit .env and add your API key from https://ll.thespacedevs.com/

# 3. Generate the API client (optional)
./gradlew openApiGenerate
```

### Run

```bash
# Android
./gradlew installDebug

# Desktop
./gradlew desktopRun

# iOS (requires macOS + Xcode)
open iosApp/iosApp.xcodeproj
# Select iosApp scheme -> Run

# Tests
./gradlew test
```

## Repository Structure

| Path | Description |
|------|-------------|
| `composeApp/` | Shared Kotlin + Compose Multiplatform code |
| `iosApp/` | Native iOS entry point, widgets, and notifications |
| `iosApp/LaunchWidget/` | iOS WidgetKit extension for home screen widgets |
| `docs/` | Architecture, CI/CD, billing, and integration docs |
| `schema/` | OpenAPI specs for Launch Library & SNAPI |
| `specs/` | Feature specifications and task breakdowns |
| `scripts/` | Build and deployment helper scripts |

## Contributing

Contributions are welcome! Please read the [Contributing Guide](CONTRIBUTING.md) before submitting a PR.

1. Fork the repository
2. Create your feature branch (`git checkout -b feat/amazing-feature`)
3. Commit using [conventional commits](https://www.conventionalcommits.org/) (`feat:`, `fix:`, `chore:`, etc.)
4. Push to the branch and open a Pull Request

> **Note:** This project uses automated CI/CD with conventional commits for versioning. See [`docs/cicd/`](docs/cicd/) for details.

## Security

For security vulnerabilities, please see [SECURITY.md](SECURITY.md). Do **not** open public issues for security concerns.

## License

This project is licensed under the **GNU General Public License v3.0** &mdash; see the [LICENSE](LICENSE) file for details.

---

<p align="center">
  Powered by <a href="https://thespacedevs.com/">The Space Devs</a> Launch Library API
</p>
