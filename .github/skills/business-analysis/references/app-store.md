# App Store & Distribution Reference

## Distribution Channels

| Channel | Platform | Type | Trigger |
|---------|----------|------|---------|
| Firebase Distribution | Android | Beta (vip_testers) | Automatic on merge |
| Google Play Store | Android | Production | Manual promotion |
| TestFlight | iOS | Beta | Manual workflow dispatch |
| App Store | iOS | Production | Manual via App Store Connect |

## Release Process

### Android (Automatic)

1. Merge PR to main using conventional commits
2. CI bumps version in `version.properties`
3. Signed APK + AAB built
4. Deployed to Firebase Distribution → `vip_testers` group
5. GitHub Release created with artifacts
6. Manually promote AAB to Play Store when ready

### iOS (Manual)

1. Trigger `release-ios.yml` from Actions tab
2. Reads version from `version.properties` (shared with Android)
3. Builds signed IPA
4. Optionally uploads to TestFlight
5. Adds IPA to existing GitHub Release
6. Manually submit to App Store review when ready

## Version Properties

**File:** `version.properties`

Shared between Android and iOS:
- `MAJOR`, `MINOR`, `PATCH` — Semantic version
- `BUILD_NUMBER` — Monotonically increasing (Play Store requirement)

## App Store Listings

### Release Notes

| Path | Content |
|------|---------|
| `docs/release-notes/v*.md` | Auto-generated per-release notes (70+ versions) |
| `docs/release-notes/whatsnew/whatsnew-en-US/` | App store "What's New" content |
| `docs/release-notes/en-US/default.txt` | Google Play Store description |
| `scripts/prepare-play-store-notes.sh` | Truncates notes to 500 char limit |

### iOS Fastlane

| Path | Purpose |
|------|---------|
| `iosApp/fastlane/` | iOS build and deployment automation |
| `iosApp/Gemfile` | Ruby dependencies for fastlane |
| `iosApp/iosApp.xcodeproj/` | Xcode project configuration |

## Ad Compliance

### SKAdNetwork (iOS)

SKAdNetwork identifiers registered in main app `Info.plist`:
- `cstr6suwn9.skadnetwork` (Google)
- `4fzdc2evr5.skadnetwork`
- `2fnua5tdw4.skadnetwork`
- Additional standard identifiers

**Scope:** Main app only — widget and notification extensions do NOT serve ads.

### AdMob Configuration

| Platform | Config Source | Test Mode |
|----------|-------------|-----------|
| Android | `ADMOB_APP_ID` build config | `ca-app-pub-3940256099942544~3347511713` (debug) |
| iOS | `GADApplicationIdentifier` in Info.plist | Via `generate-ios-secrets.sh` |

## Release Cadence Analysis

Current release history spans 70+ versions (v4.0.0-b15 through v5.19.0-b28+).

To analyze cadence:
- Check `version.properties` for current version
- Review `CHANGELOG.md` for release dates
- Count tags: `git tag --list 'v*' | wc -l`
- Recent velocity: `git log --oneline --since="30 days ago" --grep="^v" --tags`

## Key Documentation

| Doc | Path |
|-----|------|
| Hybrid Release Strategy | `docs/cicd/HYBRID_RELEASE_STRATEGY.md` |
| iOS CI/CD Setup | `docs/cicd/IOS_CICD_SETUP.md` |
| iOS API Key Setup | `docs/cicd/IOS_API_KEY_SETUP.md` |
| Required Secrets | `docs/cicd/REQUIRED_SECRETS.md` |
| SKAdNetwork Spec | `specs/002-enable-skadnetwork/` |

## Business Questions This Enables

- What is the current release velocity? Is it appropriate for user engagement?
- Are beta testers in Firebase Distribution providing enough feedback before Play Store promotion?
- Should the App Store description be refreshed to highlight premium features?
- Is the "What's New" content driving downloads or re-engagement?
- Are SKAdNetwork identifiers up to date with current ad partner requirements?
- Should the app pursue featured placement on either store?
