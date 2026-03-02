# Research: Open Source Repository Preparation

**Branch**: `001-open-source-repo` | **Date**: 2026-03-02

## Research Tasks & Findings

### 1. Exposed Secrets in Tracked Files

#### 1a. Datadog Client Token & Application ID

**Decision**: Replace real Datadog tokens in `docs/cicd/DATADOG_CICD_SECRETS.md` with placeholder values and rotate the tokens in Datadog console.

**Rationale**: The following real values are committed in the documentation file:
- Client token: `<YOUR_DATADOG_CLIENT_TOKEN>` (lines 20, 90)
- Application ID: `<YOUR_DATADOG_APP_ID>` (lines 25, 91)

These are production credentials. Even though Datadog client tokens are lower-risk than server-side API keys, they should not be public. They enable anyone to send telemetry data to your Datadog account.

**Alternatives considered**:
- Leave as-is: Rejected — unnecessary exposure of production credentials
- Remove documentation entirely: Rejected — docs are useful for CI/CD setup reference
- **Replace with placeholders**: Selected — maintains documentation value while removing exposure

#### 1b. CI/CD Workflow Prints google-services.json to Logs

**Decision**: Remove the two `Print google-services.json` steps from `.github/workflows/release-main.yml` (lines 213-214, 354-355). Keep the MD5 hash step (line 357) as a safe debugging alternative.

**Rationale**: The `cat composeApp/google-services.json` commands dump decoded Firebase configuration (API keys, OAuth client IDs, project numbers) to CI logs. On a public repo, these logs are visible to anyone with read access.

**Alternatives considered**:
- Keep the steps: Rejected — leaks sensitive Firebase config to public CI logs
- Redact output: Rejected — complex and error-prone
- **Remove entirely**: Selected — the MD5 hash step provides sufficient debugging

#### 1c. backup-secrets.yml Workflow

**Decision**: Delete `.github/workflows/backup-secrets.yml` entirely.

**Rationale**: This workflow uses `oNaiPs/secrets-to-env-action@v1` to dump ALL GitHub Actions secrets to environment variables, writes them to a file, and uploads it as a downloadable artifact. On a public repository, this is catastrophic — any contributor could potentially trigger it or access artifacts.

**Alternatives considered**:
- Restrict to maintainers only: Rejected — still risky; artifacts could leak
- Add conditions: Rejected — false sense of security
- **Delete entirely**: Selected — no safe way to keep a secret-dumping workflow on a public repo

### 2. Hardcoded TOTP Secret

**Decision**: Remove the hardcoded default TOTP secret from all source files and replace with a mechanism that disables the debug menu when no secret is configured.

**Files affected**:
- `composeApp/src/commonMain/kotlin/.../util/DebugUnlock.kt` (line 33)
- `composeApp/build.gradle.kts` (line 330)
- `composeApp/src/iosMain/kotlin/.../util/AppSecrets.kt` (line 19)
- `scripts/generate-ios-secrets.sh` (line 101)
- `docs/architecture/DEBUG_MENU_SECURITY.md` (line 272)

**Rationale**: Anyone who reads the open-source code could generate valid TOTP codes and unlock the debug menu in production builds. The debug menu could expose internal state, bypass billing, or enable developer-only features.

**Alternatives considered**:
- Keep the default: Rejected — completely defeats the purpose of TOTP protection
- Use a different well-known default: Rejected — same problem
- **Require explicit secret, disable if missing**: Selected — secure by default

### 3. Apple Team ID Hardcoded as Fallback

**Decision**: Accept an Apple Team ID in Xcode project files (`project.pbxproj`, `Config.xcconfig`) since these are required for Xcode builds. Remove fallback defaults from `Fastfile` and `Matchfile` where environment variables should be the sole source.

**Files to modify**:
- `iosApp/fastlane/Fastfile` (line 74): Remove `|| "4T4QRN2U5X"` fallback
- `iosApp/fastlane/Matchfile` (line 12): Remove `|| "4T4QRN2U5X"` fallback

**Files to keep as-is**:
- `iosApp/iosApp.xcodeproj/project.pbxproj`: Required by Xcode, team IDs are semi-public
- `iosApp/Configuration/Config.xcconfig`: Required for build configuration

**Rationale**: Apple Team IDs are visible in published app signatures, making them semi-public. However, keeping unnecessary fallbacks in automation scripts could allow unauthorized use of the signing identity. The Xcode project files legitimately need the team ID for building.

**Alternatives considered**:
- Remove from all files: Rejected — breaks Xcode builds
- Leave all fallbacks: Rejected — unnecessary exposure in CI scripts
- **Keep in Xcode files, remove from CI fallbacks**: Selected — balanced approach

### 4. AdMob App ID in iOS Info.plist

**Decision**: Accept AdMob App IDs in `Info.plist` files as-is.

**Rationale**: AdMob App IDs (`ca-app-pub-9824528399164059~3133099497`) are embedded in published iOS apps and are effectively public. The Android manifest already uses Google's test ID correctly. Moving these to build-time injection would add complexity for minimal security benefit.

**Alternatives considered**:
- Move to build-time injection: Rejected — high effort, low benefit since IDs are in published apps
- **Leave as-is**: Selected — these are inherently public values

### 5. .gitignore Hardening

**Decision**: Add the following patterns to `.gitignore`:

```
# Signing & certificates
*.jks
*.keystore
*.p12
*.pem
*.key
release.keystore

# CI/CD artifacts that should never be local
service-account.json
secrets-backup.txt
secrets-inventory.txt
```

**Rationale**: Current `.gitignore` covers the main secrets files (`.env`, `keystore.properties`, `google-services.json`, IDE secrets) but misses common signing/certificate file extensions. These should be excluded as a defense-in-depth measure.

### 6. License Choice

**Decision**: Use GPL-3.0.

**Rationale**: GPL-3.0 is a strong copyleft license that helps ensure derivative works remain open source.

**Alternatives considered**:
- MIT License: Simple, very permissive, but no patent clause
- GPL-3.0: Strong copyleft, may discourage contributions from corporate developers
- Apache 2.0: Permissive with patent protection, widely adopted in similar projects

### 7. Community Files Best Practices

**Decision**: Create the following community files at the repository root:
- `LICENSE` — GPL-3.0
- `CONTRIBUTING.md` — Guidelines for issues, PRs, code style, conventional commits
- `CODE_OF_CONDUCT.md` — Contributor Covenant v2.1 (industry standard)
- `SECURITY.md` — Vulnerability reporting process via GitHub Security Advisories

**Rationale**: These are standard files expected by the open-source community and recognized by GitHub's community profile checker. They set expectations for contributors and establish governance.

### 8. Documentation Review for Internal Information

**Decision**: Review `docs/` directory for files that contain internal-only operational details. Most documentation is architecture/setup focused and appropriate for public visibility.

**Observations**:
- `docs/cicd/` contains setup guides that reference GitHub Secrets names — this is fine (names, not values)
- `docs/billing/` contains RevenueCat integration docs — appropriate for public (uses placeholder keys)
- `docs/architecture/DEBUG_MENU_SECURITY.md` references the hardcoded TOTP — will be updated
- `specs/` contains feature specifications — appropriate for public (shows project planning)

**Decision**: No docs need removal. Only `DATADOG_CICD_SECRETS.md` and `DEBUG_MENU_SECURITY.md` need token/secret redaction.

### 9. Git History Considerations

**Decision**: Do NOT rewrite git history. Accept that Datadog tokens exist in history and rotate them instead.

**Rationale**: Rewriting git history with `git filter-repo` or BFG is risky for a repository with active branches and CI/CD. The Datadog client token in history is lower-risk (it can only send data TO Datadog, not read from it). Rotating the token in Datadog's console immediately invalidates the historical value.

**Alternatives considered**:
- Use `git filter-repo`: Rejected — high risk of breaking branches, force-push required
- Use BFG Repo-Cleaner: Rejected — same risks as filter-repo
- **Rotate credentials + clean current state**: Selected — pragmatic and safe
