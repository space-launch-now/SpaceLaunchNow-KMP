# Contributing

Thanks for taking the time to contribute to SpaceLaunchNow-KMP.

## Ways to contribute

- Report bugs and request features via GitHub Issues
- Improve documentation (README, docs/)
- Submit fixes and small refactors via Pull Requests

## Before you start

- **No secrets in commits**: never commit API keys, signing files, `google-services.json`, `GoogleService-Info.plist`, `Secrets.plist`, `.env`, or anything under `*.keystore` / `*.jks`.
- **Generated API clients**: this repo uses OpenAPI generation. If you change the spec/config, regenerate locally with `./gradlew openApiGenerate`.

## Development setup

1. Install **JDK 21** (JetBrains JDK recommended)
2. Clone the repo
3. Copy the environment template and fill values:
   - `cp .env.example .env`
4. Generate API client (if needed):
   - `./gradlew openApiGenerate`

### Common commands

- Desktop run: `./gradlew desktopRun`
- Compile desktop: `./gradlew compileKotlinDesktop`
- Tests: `./gradlew test`
- Android debug install: `./gradlew installDebug`

## Pull requests

- Keep PRs small and focused.
- Include screenshots for UI changes.
- Ensure `./gradlew test` passes.

## Commit messages

This repo uses **Conventional Commits**.

Examples:

- `feat: add event detail page`
- `fix(api): handle null location`
- `chore(deps): update kotlin`


## Reporting security issues

Please do **not** open public issues for security reports. See [SECURITY.md](SECURITY.md).
