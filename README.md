SpaceLaunchNow-KMP is a Kotlin Multiplatform (Compose Multiplatform) app that displays space launch information using Launch Library data from The Space Devs.

Targets:
- Android
- Desktop (JVM)
- iOS

## Getting started

Prerequisites:
- JDK 21 (JetBrains JDK recommended)
- Android Studio / IntelliJ IDEA
- Xcode 15+ (for iOS builds)

1. Create your local env file:
  - `cp .env.example .env`
2. (Optional) Generate the API client:
  - `./gradlew openApiGenerate`
3. Build / run:
  - Desktop: `./gradlew desktopRun`
  - Desktop compile: `./gradlew compileKotlinDesktop`
  - Tests: `./gradlew test`
  - Android debug install: `./gradlew installDebug`

## Repository structure

- `composeApp/`: shared Kotlin + Compose Multiplatform code
- `iosApp/`: native iOS entry point + Xcode project
- `docs/`: architecture, CI/CD, and integration documentation

## Feedback and contributions

- Use GitHub Issues for bugs and feature requests
- See [CONTRIBUTING.md](CONTRIBUTING.md) for setup and guidelines
- For security issues, see [SECURITY.md](SECURITY.md) (please don’t open public issues)

## License

GPL-3.0. See [LICENSE](LICENSE).
