# CI/CD Workflow: Hybrid Android/iOS Release Strategy

## Overview

This project uses a **hybrid release strategy** to balance automation with cost control:

- **Android:** Fully automated on merge to master (~$0.50 per release)
- **iOS:** Manual trigger only (~$6.00 per release)

**Rationale:** iOS builds cost 12x more than Android and take 6x longer. By making iOS manual, we save significant GitHub Actions costs while maintaining rapid Android deployment.

## Workflow Files

### 1. `pr-validation.yml` - Pull Request Validation
**Triggers:** Any PR to main/master

**What it does:**
- Runs unit tests
- Builds debug APK
- Reports results as PR comment

**Cost:** ~$0.25 (3-5 minutes on Ubuntu)

**Files changed:** None (validation only)

---

### 2. `release-android.yml` - Android Release (AUTO)
**Triggers:** 
- ✅ Automatic: Merge to main/master
- ✅ Manual: Actions → "Release Android" → Run workflow

**What it does:**
1. **Version Bump** - Analyzes conventional commits since last tag
2. **Changelog** - Generates release notes from commits
3. **Commit & Tag** - Updates `version.properties`, `CHANGELOG.md`, creates git tag
4. **Test** - Runs Android unit tests
5. **Build** - Creates signed APK + AAB
6. **Deploy** - Uploads to Firebase Distribution
7. **Release** - Creates/updates GitHub Release

**Cost:** ~$0.50 (5-7 minutes on Ubuntu)

**Version bump rules:**
- `feat!:` or `BREAKING CHANGE:` → Major (x.0.0)
- `feat:` → Minor (0.x.0)
- `fix:`, `chore:`, etc. → Patch (0.0.x)
- Build number always increments

**Artifacts:**
- APK (signed)
- AAB (signed)
- ProGuard mapping
- Changelog

**Deploys to:**
- Firebase Distribution (`vip_testers` group)
- GitHub Release (creates with Android files)

---

### 3. `release-ios.yml` - iOS Release (MANUAL)
**Triggers:**
- ⏸️ Manual ONLY: Actions → "Release iOS" → Run workflow

**What it does:**
1. **Read Version** - Reads from `version.properties` (same as Android)
2. **Update Info.plist** - Syncs CFBundleShortVersionString and CFBundleVersion
3. **Build** - Creates signed IPA with Xcode
4. **Deploy** - Uploads to TestFlight (if selected)
5. **Update Release** - Adds IPA to existing GitHub Release

**Cost:** ~$6.00 (30 minutes on macOS-14)

**Options:**
- `deploy_to: testflight` - Upload to TestFlight
- `deploy_to: artifacts-only` - Just build IPA, don't upload

**Artifacts:**
- IPA (signed)

**Deploys to:**
- TestFlight (if `testflight` selected)
- GitHub Release (updates existing release with IPA)

## Complete Release Flow

### Normal Release (Android + iOS)

```bash
# 1. Create feature branch
git checkout -b feature/awesome-feature

# 2. Make changes with conventional commits
git commit -m "feat: add awesome feature"
git commit -m "fix: resolve edge case bug"

# 3. Create PR
gh pr create --title "feat: Awesome Feature"

# 4. PR Validation runs automatically (~5 min, $0.25)
#    ✅ Tests pass
#    ✅ Debug APK builds

# 5. Merge PR
gh pr merge --merge

# 6. Android Release runs automatically (~7 min, $0.50)
#    ✅ Version bumped (e.g., 4.0.0 → 4.1.0-b15)
#    ✅ Changelog generated
#    ✅ APK + AAB deployed to Firebase
#    ✅ GitHub Release v4.1.0-b15 created

# 7. Manually trigger iOS when ready (~30 min, $6.00)
gh workflow run release-ios.yml -f deploy_to=testflight

#    ✅ Uses same version: 4.1.0-b15
#    ✅ IPA uploaded to TestFlight
#    ✅ GitHub Release updated with IPA
```

**Total Cost:**
- PR validation: $0.25
- Android release: $0.50
- iOS release: $6.00
- **Total: $6.75**

### Android-Only Release

If you only need Android beta testing:

```bash
# After merging PR, Android release happens automatically
# Cost: $0.75 ($0.25 PR + $0.50 Android)
# Time: ~12 minutes

# Skip iOS trigger entirely
# Savings: $6.00 and 30 minutes
```

### iOS-Only Update

If you need to rebuild iOS without changing Android:

```bash
# Manually trigger iOS workflow
gh workflow run release-ios.yml -f deploy_to=testflight

# Cost: $6.00
# Time: ~30 minutes
# Uses current version from version.properties
```

## Version Management

### Single Source of Truth: `version.properties`

Both Android and iOS read from this file:

```properties
version=4.1.0
versionMajor=4
versionMinor=1
versionPatch=0
versionBuildNumber=15
```

**Android Release workflow:**
- Reads current version
- Analyzes commits for bump type
- Updates `version.properties`
- Commits and tags (e.g., `v4.1.0-b15`)

**iOS Release workflow:**
- Reads from `version.properties`
- Updates `iosApp/iosApp/Info.plist` automatically
- Uses same version tag as Android

**Result:** Both platforms always have synchronized versions.

## Cost Comparison

### Old Workflow (Both Automatic)
```
Merge to master → Android + iOS in parallel
- Android build: 7 min @ $0.50
- iOS build: 30 min @ $6.00
- Total per merge: $6.50
- 10 merges/week: $65/week = $260/month
```

### New Workflow (Android Auto, iOS Manual)
```
Merge to master → Android only
- Android build: 7 min @ $0.50
- Total per merge: $0.50

Manual iOS trigger → iOS only
- iOS build: 30 min @ $6.00
- Trigger 2x/week: $12/week = $48/month

10 merges/week: $5/week = $20/month (Android)
2 iOS builds/week: $12/week = $48/month (iOS)
Total: $68/month (vs $260 old)

SAVINGS: $192/month (74% reduction)
```

## When to Trigger iOS

**Trigger iOS release when:**
- ✅ Major feature complete and needs iOS beta testing
- ✅ Bug fix affects iOS-specific code
- ✅ Ready for App Store submission
- ✅ Weekly/bi-weekly iOS beta schedule

**Don't trigger iOS when:**
- ❌ Minor tweaks to Android-only code
- ❌ Documentation changes
- ❌ Dependency updates that don't affect iOS
- ❌ Quick bug fixes being tested on Android first

## Emergency Releases

### Hotfix Android Only
```bash
git checkout -b hotfix/critical-bug
git commit -m "fix: resolve critical crash"
gh pr create --title "fix: Critical Crash"
gh pr merge --merge

# Auto deploys to Firebase in ~7 minutes
# Cost: $0.75
```

### Hotfix Both Platforms
```bash
# Same as above, then:
gh workflow run release-ios.yml -f deploy_to=testflight

# Total cost: $6.75
# Total time: ~37 minutes
```

## Monitoring Releases

### Check Android Release Status
```bash
# View recent workflow runs
gh run list --workflow=release-android.yml

# View latest run
gh run view --workflow=release-android.yml

# Download APK artifact
gh run download <run-id> -n android-release-v4.1.0-b15
```

### Check iOS Release Status
```bash
# View recent workflow runs
gh run list --workflow=release-ios.yml

# View latest run
gh run view --workflow=release-ios.yml

# Download IPA artifact
gh run download <run-id> -n ios-release-v4.1.0-b15
```

### Check GitHub Release
```bash
# List releases
gh release list

# View specific release
gh release view v4.1.0-b15

# Download all assets
gh release download v4.1.0-b15
```

## Files Modified by Workflows

### Android Release (`release-android.yml`)
**Modified and committed:**
- `version.properties` - Version bumped
- `CHANGELOG.md` - Prepended with new version
- `docs/release-notes/v{VERSION}.md` - New release notes file

**Created:**
- Git tag: `v{VERSION}`
- GitHub Release (draft: false)
- Firebase Distribution release

### iOS Release (`release-ios.yml`)
**Modified (not committed):**
- `iosApp/iosApp/Info.plist` - Version synced from version.properties

**Updated:**
- GitHub Release (adds IPA to existing release)
- TestFlight build

## Secrets Required

### Both Workflows
- `API_KEY` - Space Devs API key

### Android Only
- `FIREBASE_GOOGLE_SERVICES_JSON`
- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`
- `FIREBASE_APP_ID`
- `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON`

### iOS Only
- `APPLE_CERTIFICATE_BASE64`
- `APPLE_CERTIFICATE_PASSWORD`
- `APPLE_PROVISIONING_PROFILE`
- `APPLE_TEAM_ID`
- `IOS_BUNDLE_ID`
- `APPLE_API_KEY_ID`
- `APPLE_API_ISSUER_ID`
- `APPLE_API_KEY_CONTENT`
- `IOS_EXPORT_OPTIONS_PLIST`

## Troubleshooting

### Android release didn't trigger
- Check commit messages use conventional format (`feat:`, `fix:`, etc.)
- Verify push went to `main` or `master` branch
- Check Actions tab for workflow run

### iOS release failed
- Check all iOS secrets are set correctly
- Verify provisioning profile matches bundle ID
- Check TestFlight upload quota (100 builds/day limit)
- Review workflow logs for certificate issues

### Version didn't bump
- Ensure commits follow conventional format
- Check there are new commits since last tag
- Verify `version.properties` is in repository

### GitHub Release missing iOS build
- Ensure Android release ran first (creates the release)
- Check iOS workflow completed successfully
- Verify release tag matches (`v{VERSION}`)

## Migration from Old Workflow

**Old file:** `.github/workflows/master-deploy.yml.OLD`

**Changes:**
1. ✅ Separated Android and iOS into two workflows
2. ✅ Made iOS manual-only
3. ✅ Kept version bumping in Android workflow
4. ✅ Both workflows read from `version.properties`
5. ✅ iOS updates existing GitHub Release instead of creating new one

**Breaking changes:** None - version format and release process remain the same

**Rollback:** Rename `master-deploy.yml.OLD` back to `master-deploy.yml` and delete new workflows

## Best Practices

1. **Commit messages matter** - Use conventional format for correct version bumps
2. **Test locally first** - Don't rely on CI to catch errors
3. **Android first** - Let Android release complete before triggering iOS
4. **iOS on schedule** - Trigger iOS on a regular schedule (e.g., weekly) instead of every merge
5. **Monitor costs** - Check GitHub Actions usage tab monthly
6. **Use artifacts-only** - For testing, use `artifacts-only` to build without deploying

## Future Improvements

- [ ] Add production deployment (Play Store / App Store)
- [ ] Implement release branches for hotfixes
- [ ] Add Slack/Discord notifications
- [ ] Create automated weekly iOS builds
- [ ] Add release approval workflow
