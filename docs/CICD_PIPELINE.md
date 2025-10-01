# CI/CD Pipeline Documentation

This document describes the complete CI/CD pipeline for Space Launch Now KMP.

## Overview

The CI/CD pipeline consists of two main workflows:

1. **PR Validation** (`pr-validation.yml`) - Validates pull requests
2. **Master Deploy** (`master-deploy.yml`) - Deploys production releases

## Workflow 1: PR Validation

**Trigger:** All pull requests to `main` or `master` branches

**Purpose:** Ensure code quality before merging

### Steps

1. ✅ **Checkout code** - Fetches PR branch
2. ✅ **Set up JDK 21** - Configures Java environment
3. ✅ **Setup Gradle** - Configures Gradle with caching
4. ✅ **Create .env file** - Sets up API_KEY from secrets
5. ✅ **Decode Google Services** - Decodes Firebase config from base64
6. ✅ **Check code formatting** - Runs ktlintCheck (continues on error)
7. ✅ **Run unit tests** - Executes test suite
8. ✅ **Generate test report** - Creates JUnit test report
9. ✅ **Build Debug APK** - Compiles debug Android build
10. ✅ **Upload Debug APK** - Stores APK as artifact (7 days retention)
11. ✅ **Build Desktop JAR** - Attempts desktop build (optional)
12. ✅ **Comment PR** - Posts validation status to PR

### Required Secrets

- `API_KEY` - Space Devs API key
- `FIREBASE_GOOGLE_SERVICES_JSON` - Firebase config (base64 encoded)

### Artifacts

- `debug-apk` - Debug APK for testing (7 days retention)

### Success Criteria

- All tests pass
- Debug APK builds successfully
- Code formatting passes (soft failure)

## Workflow 2: Master Deploy (Production Release)

**Trigger:** Merges to `master` or `main` branch (also supports manual trigger)

**Purpose:** Automated production release with versioning and deployment

### Steps

#### Phase 1: Setup
1. ✅ **Checkout code** - Fetches master branch with full history
2. ✅ **Set up JDK 21** - Configures Java environment
3. ✅ **Setup Gradle** - Configures Gradle with caching
4. ✅ **Create .env file** - Sets up API_KEY
5. ✅ **Decode Google Services** - Decodes Firebase config
6. ✅ **Decode Keystore** - Decodes release signing keystore from base64

#### Phase 2: Version Bump (Conventional Commits)
7. ✅ **Bump version** - Uses [Conventional Changelog Action](https://github.com/TriPSs/conventional-changelog-action)

**Version Bump Logic:**
The action automatically analyzes commits since the last tag:
- **`BREAKING CHANGE` or `!:`** → MAJOR bump (x.0.0)
- **`feat:`** → MINOR bump (0.x.0)
- **`fix:`** → PATCH bump (0.0.x)
- **Other types** → PATCH bump (default)

**GitHub Action Used:** `TriPSs/conventional-changelog-action@v5`

This action:
- Analyzes all commits since the last tag
- Determines the appropriate version bump
- Generates a formatted changelog
- Updates the CHANGELOG.md file
- Provides outputs for version numbers and changelog content

**Version Code Calculation:**
```
versionCode = (major * 1,000,000) + (minor * 100,000) + (patch * 10,000) + buildNumber
```

**Example:**
- Version: `4.1.2-b15`
- Version Code: `4,112,015`

#### Phase 3: Changelog Generation
8. ✅ **Generate changelog** - Automatically handled by Conventional Changelog Action

**Changelog Format:**
The action generates a well-formatted changelog following the [Keep a Changelog](https://keepachangelog.com/) format with sections:
- 💥 **Breaking Changes** - BREAKING CHANGE commits
- ✨ **Features** - feat: commits  
- 🐛 **Bug Fixes** - fix: commits
- 🔧 **Chores** - chore: commits
- 📝 **Documentation** - docs: commits

**Output Files:**
- `docs/release-notes/v{VERSION}.md` - Version-specific notes
- `CHANGELOG.md` - Cumulative changelog (prepended)
- `release-notes.txt` - Temporary file for GitHub release

#### Phase 4: Commit & Tag
9. ✅ **Commit version and changelog** - Commits version bump with `[skip ci]`
   - Updates `version.properties`
   - Updates `CHANGELOG.md`
   - Adds release notes to `docs/release-notes/`
   - Creates git tag: `v{VERSION_NAME}`

#### Phase 5: Testing
10. ✅ **Run unit tests** - Full test suite
11. ✅ **Generate test report** - JUnit report (fails build on test failure)

#### Phase 6: Build Production Release
12. ✅ **Build Release Bundle (AAB)** - Signed Android App Bundle
13. ✅ **Build Release APK** - Signed Android APK

**Signing Configuration:**
```bash
./gradlew :composeApp:bundleRelease \
  -Pandroid.injected.signing.store.file=$KEYSTORE_FILE \
  -Pandroid.injected.signing.store.password=$KEYSTORE_PASSWORD \
  -Pandroid.injected.signing.key.alias=$KEY_ALIAS \
  -Pandroid.injected.signing.key.password=$KEY_PASSWORD
```

#### Phase 7: Firebase Distribution
14. ✅ **Deploy to Firebase Distribution** - Distributes APK to testers

**Configuration:**
- **Groups:** `testers`
- **File:** Release APK
- **Release Notes:** Auto-generated with version and link

#### Phase 8: GitHub Release
15. ✅ **Push version bump and tags** - Pushes commits and tags to repository
16. ✅ **Create GitHub Release** - Creates public release with artifacts

**Release Includes:**
- 📱 Release APK
- 📦 Release AAB (Android App Bundle)
- 🗺️ ProGuard mapping file
- 📝 Generated release notes

#### Phase 9: Artifacts & Summary
17. ✅ **Upload APK artifact** - Stores APK (90 days retention)
18. ✅ **Upload AAB artifact** - Stores AAB (90 days retention)
19. ✅ **Upload ProGuard mapping** - Stores mapping file (90 days retention)
20. ✅ **Deployment Summary** - Generates workflow summary

### Required Secrets

All secrets must be configured in GitHub repository settings:

| Secret | Type | Description |
|--------|------|-------------|
| `API_KEY` | string | Space Devs API key |
| `FIREBASE_APP_ID` | string | Firebase app ID for distribution |
| `FIREBASE_GOOGLE_SERVICES_JSON` | base64 | Firebase google-services.json (base64 encoded) |
| `FIREBASE_TOKEN` | string | Firebase CI token |
| `KEYSTORE_BASE64` | base64 | Release keystore file (base64 encoded) |
| `KEYSTORE_PASSWORD` | string | Keystore password |
| `KEY_ALIAS` | string | Key alias in keystore |
| `KEY_PASSWORD` | string | Key password |
| `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` | base64 | Google Play service account JSON (reserved for future use) |
| `GITHUB_TOKEN` | auto | Automatically provided by GitHub Actions |

### Artifacts

All artifacts retained for 90 days:

- `release-apk-{VERSION}` - Signed release APK
- `release-aab-{VERSION}` - Signed release AAB
- `proguard-mapping-{VERSION}` - ProGuard mapping for crash analysis

### Outputs

#### Git Repository
- ✅ Updated `version.properties`
- ✅ Updated `CHANGELOG.md`
- ✅ New file `docs/release-notes/v{VERSION}.md`
- ✅ Git tag `v{VERSION_NAME}`
- ✅ Commit: `chore(release): bump version to {VERSION} [skip ci]`

#### Firebase Distribution
- ✅ APK distributed to `testers` group
- ✅ Release notes with version and GitHub link

#### GitHub Release
- ✅ Public release with tag `v{VERSION_NAME}`
- ✅ Title: `🚀 Space Launch Now {VERSION}`
- ✅ Markdown-formatted release notes
- ✅ APK, AAB, and mapping files attached

#### GitHub Actions
- ✅ Workflow artifacts (90 days)
- ✅ Deployment summary in workflow run

## Environment Variables

The pipeline uses these environment variables:

```yaml
env:
  CI: true                      # Indicates CI environment
  VERSION_NAME: "4.1.0-b15"     # Generated version name
  VERSION_CODE: "4110015"       # Generated version code
  BUMP_TYPE: "minor"            # Type of version bump
  KEYSTORE_FILE: "/path/to/keystore"  # Path to decoded keystore
```

## Conventional Commits

The pipeline relies on conventional commit messages for version bumping and changelog generation.

**Format:** `<type>(<scope>): <subject>`

### Types for Version Bumping

| Commit Type | Version Bump | Example |
|-------------|--------------|---------|
| `BREAKING CHANGE` or `!:` | MAJOR (x.0.0) | `feat!: redesign API` |
| `feat:` | MINOR (0.x.0) | `feat: add event detail page` |
| `fix:` | PATCH (0.0.x) | `fix: resolve crash on null` |
| `chore:`, `docs:`, etc. | PATCH (0.0.x) | `chore: update dependencies` |

See [CONVENTIONAL_COMMITS.md](./CONVENTIONAL_COMMITS.md) for detailed guide.

## Versioning Strategy

### Version Format
```
{major}.{minor}.{patch}-b{buildNumber}
```

**Examples:**
- `4.0.0-b1` - Initial version
- `4.0.1-b2` - Patch update
- `4.1.0-b3` - Minor feature
- `5.0.0-b4` - Breaking change

### Version Code Format
```
versionCode = (major * 1,000,000) + (minor * 100,000) + (patch * 10,000) + buildNumber
```

**Examples:**
- `4.0.0-b1` → `4,000,001`
- `4.1.0-b3` → `4,100,003`
- `5.0.0-b4` → `5,000,004`

### Build Number
- Increments on every master deployment
- Never resets
- Ensures unique version codes

## Workflow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                        PR VALIDATION                             │
│                                                                  │
│  PR Created/Updated → Run Tests → Build Debug APK → ✅          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      MASTER DEPLOYMENT                           │
│                                                                  │
│  Merge to Master                                                 │
│       ↓                                                          │
│  Analyze Commits (Conventional)                                  │
│       ↓                                                          │
│  Bump Version (major/minor/patch)                                │
│       ↓                                                          │
│  Generate Changelog                                              │
│       ↓                                                          │
│  Commit & Tag                                                    │
│       ↓                                                          │
│  Run Tests                                                       │
│       ↓                                                          │
│  Build Signed APK + AAB                                          │
│       ↓                                                          │
│  ┌──────────────────┐    ┌──────────────────┐                  │
│  │ Firebase Distrib │    │  GitHub Release  │                   │
│  │  (Testers Group) │    │  (Public + Artifacts)               │
│  └──────────────────┘    └──────────────────┘                  │
│       ↓                            ↓                             │
│  ✅ Deployed            ✅ v{VERSION} Created                   │
└─────────────────────────────────────────────────────────────────┘
```

## Setup Instructions

### 1. Configure Secrets

Go to repository **Settings → Secrets and variables → Actions**

#### Generate Firebase Token
```bash
firebase login:ci
# Copy the token and save as FIREBASE_TOKEN secret
```

#### Encode Google Services JSON
```bash
# Linux/Mac
base64 -i composeApp/google-services.json | tr -d '\n'

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("composeApp\google-services.json"))
```

#### Encode Keystore
```bash
# Linux/Mac
base64 -i release.keystore | tr -d '\n'

# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

### 2. Create Release Keystore

If you don't have a keystore:
```bash
keytool -genkey -v -keystore release.keystore \
  -alias spacelaunchnow \
  -keyalg RSA -keysize 2048 -validity 10000
```

### 3. Get Firebase App ID

From Firebase Console → Project Settings → Your apps → App ID

### 4. Test Locally

#### Test PR Validation
```bash
# Create a feature branch
git checkout -b feature/test

# Make changes
echo "test" > test.txt
git add test.txt
git commit -m "feat: test feature"

# Push and create PR
git push origin feature/test
gh pr create --title "Test PR" --body "Testing PR validation"
```

#### Test Master Deploy
```bash
# Merge PR to master (will trigger deployment)
gh pr merge --merge

# Or manually trigger
gh workflow run master-deploy.yml
```

### 5. Monitor Deployments

- **GitHub Actions:** Repository → Actions tab
- **Firebase Distribution:** Firebase Console → App Distribution
- **GitHub Releases:** Repository → Releases

## Troubleshooting

### Build Fails: "Could not find google-services.json"
**Solution:** Ensure `FIREBASE_GOOGLE_SERVICES_JSON` secret is set correctly

### Build Fails: Keystore errors
**Solution:** Verify all keystore secrets:
- `KEYSTORE_BASE64` - Base64 encoded keystore file
- `KEYSTORE_PASSWORD` - Keystore password
- `KEY_ALIAS` - Key alias
- `KEY_PASSWORD` - Key password

### Firebase Distribution Fails
**Solution:** Check:
- `FIREBASE_APP_ID` is correct (format: `1:123456789:android:abc123`)
- `FIREBASE_TOKEN` is valid (regenerate with `firebase login:ci`)
- APK file exists at expected path

### Version Not Bumping
**Solution:** Check commit messages follow conventional format:
```bash
# ✅ Correct
git commit -m "feat: add new feature"

# ❌ Incorrect
git commit -m "added new feature"
```

### Changelog Empty
**Solution:** Ensure commits exist between current HEAD and last tag:
```bash
git tag  # List tags
git log v4.0.0-b1..HEAD --oneline  # Check commits since tag
```

## Skip CI

To skip the CI pipeline (e.g., for documentation changes):
```bash
git commit -m "docs: update README [skip ci]"
```

The pipeline automatically adds `[skip ci]` to version bump commits to prevent infinite loops.

## Future Enhancements

Potential additions to the pipeline:

- [ ] **Play Store Deployment** - Automatic upload to Google Play Console
- [ ] **iOS Deployment** - TestFlight and App Store deployment
- [ ] **Desktop Releases** - Windows/Mac/Linux builds
- [ ] **E2E Testing** - Automated UI tests
- [ ] **Performance Monitoring** - Automated performance regression checks
- [ ] **Code Coverage** - Track test coverage over time
- [ ] **Security Scanning** - Automated vulnerability scanning

## References

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Firebase Distribution Action](https://github.com/wzieba/Firebase-Distribution-Github-Action)
- [Semantic Versioning](https://semver.org/)
