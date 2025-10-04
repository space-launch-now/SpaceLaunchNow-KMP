# CI/CD Quick Reference

## 🚀 Quick Start Guide

### For Developers

#### Making a Pull Request
```bash
# 1. Create feature branch
git checkout -b feature/your-feature

# 2. Make changes and commit using conventional format
git add .
git commit -m "feat: your feature description"

# 3. Push and create PR
git push origin feature/your-feature
gh pr create --title "feat: Your Feature" --body "Description"
```

**What happens:**
- ✅ Tests run automatically
- ✅ Debug APK built
- ✅ Results commented on PR
- ❌ NO version bump
- ❌ NO deployment

#### After Merge to Master
```bash
# Merge your PR
gh pr merge --merge
```

**What happens:**
1. ✅ Version bumped based on commit messages
2. ✅ Changelog generated
3. ✅ Tests run
4. ✅ Signed APK + AAB built (Android)
5. ✅ Signed IPA built (iOS)
6. ✅ Deployed to Firebase Distribution (Android)
7. ✅ Deployed to TestFlight (iOS)
8. ✅ GitHub Release created

## 📝 Commit Message Cheat Sheet

### Version Bumps

| Want this bump | Use this format | Example |
|----------------|-----------------|---------|
| **Major** (5.0.0) | `feat!:` or `BREAKING CHANGE` | `feat!: redesign API` |
| **Minor** (4.1.0) | `feat:` | `feat: add event detail page` |
| **Patch** (4.0.1) | `fix:` | `fix: resolve crash` |
| **Patch** (4.0.1) | `chore:`, `docs:`, etc. | `chore: update deps` |

### Common Types

```bash
# New feature (MINOR bump)
git commit -m "feat: add dark mode support"
git commit -m "feat(ui): implement tablet layout"

# Bug fix (PATCH bump)
git commit -m "fix: prevent crash on null"
git commit -m "fix(detail): handle missing data"

# Breaking change (MAJOR bump)
git commit -m "feat!: change API response format"

# Other (PATCH bump)
git commit -m "chore: update dependencies"
git commit -m "docs: update README"
git commit -m "refactor: simplify code"
git commit -m "test: add unit tests"
```

### Skip CI
```bash
git commit -m "docs: fix typo [skip ci]"
```

## 🔑 Secrets Required

Set these in GitHub: **Settings → Secrets → Actions**

### Android Secrets
| Secret | How to Get |
|--------|-----------|
| `API_KEY` | Get from Space Devs |
| `FIREBASE_APP_ID` | Firebase Console → Project Settings |
| `FIREBASE_GOOGLE_SERVICES_JSON` | `base64 google-services.json` |
| `KEYSTORE_BASE64` | `base64 release.keystore` |
| `KEYSTORE_PASSWORD` | Your keystore password |
| `KEY_ALIAS` | Your key alias |
| `KEY_PASSWORD` | Your key password |
| `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` | Google Play service account JSON (base64) |

### iOS Secrets
| Secret | How to Get |
|--------|-----------|
| `APPLE_CERTIFICATE_BASE64` | `base64 distribution.p12` |
| `APPLE_CERTIFICATE_PASSWORD` | P12 export password |
| `APPLE_PROVISIONING_PROFILE` | `base64 profile.mobileprovision` |
| `APPLE_TEAM_ID` | Apple Developer Account |
| `IOS_BUNDLE_ID` | Your iOS bundle identifier |
| `APPLE_API_KEY_ID` | App Store Connect API Key ID |
| `APPLE_API_ISSUER_ID` | App Store Connect Issuer ID |
| `APPLE_API_KEY_CONTENT` | `base64 AuthKey_XXXXX.p8` |
| `IOS_EXPORT_OPTIONS_PLIST` | `base64 ExportOptions.plist` |

**See [IOS_CICD_SETUP.md](./IOS_CICD_SETUP.md) for detailed iOS setup**

## 🎯 Workflows

### PR Validation
- **Triggers:** Any PR to main/master
- **Duration:** ~5 minutes
- **Actions:** Test + Build debug APK
- **Artifacts:** Debug APK (7 days)

### Master Deploy
- **Triggers:** Merge to main/master
- **Duration:** ~15-20 minutes
- **Actions:** Version bump + Test + Build signed (Android + iOS) + Deploy
- **Artifacts:** APK, AAB, IPA, mapping (90 days)
- **Deploys to:** Firebase Distribution (Android) + TestFlight (iOS) + GitHub Release

## 📦 Version Format

```
{major}.{minor}.{patch}-b{buildNumber}

Example: 4.1.2-b15
```

- **major**: Breaking changes
- **minor**: New features
- **patch**: Bug fixes
- **buildNumber**: Auto-increments always

## 🔍 Troubleshooting

### Android Issues

#### "Build failed: google-services.json not found"
→ Check `FIREBASE_GOOGLE_SERVICES_JSON` secret is set

#### "Signing failed"
→ Verify keystore secrets: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`

#### "Firebase Distribution failed"
→ Check `FIREBASE_APP_ID` and service account JSON

### iOS Issues

#### "No matching provisioning profiles found"
→ Check `APPLE_PROVISIONING_PROFILE` secret and `IOS_BUNDLE_ID` match

#### "Code signing error"
→ Verify `APPLE_CERTIFICATE_BASE64` and `APPLE_CERTIFICATE_PASSWORD`

#### "TestFlight upload failed"
→ Check App Store Connect API secrets: `APPLE_API_KEY_ID`, `APPLE_API_ISSUER_ID`, `APPLE_API_KEY_CONTENT`

### General Issues

### "Version didn't bump"
→ Use conventional commit format: `feat:`, `fix:`, etc.

### "Changelog is empty"
→ Ensure commits follow conventional format

## 📚 Full Documentation

- **Conventional Commits Guide:** `docs/CONVENTIONAL_COMMITS.md`
- **Full CI/CD Pipeline:** `docs/CICD_PIPELINE.md`
- **iOS CI/CD Setup:** `docs/IOS_CICD_SETUP.md`
- **Copilot Instructions:** `.github/copilot-instructions.md`

## 🎓 Example Workflow

```bash
# 1. Start feature
git checkout -b feature/event-detail
git add .
git commit -m "feat(detail): implement event detail page"

# 2. Fix bug
git add .
git commit -m "fix(detail): handle null event description"

# 3. Add tests
git add .
git commit -m "test(detail): add viewmodel tests"

# 4. Create PR
git push origin feature/event-detail
gh pr create --title "feat: Event Detail Page" --body "Implements MVP event detail"

# 5. Wait for validation ✅

# 6. Merge to master
gh pr merge --merge

# 7. Automatic deployment! 🚀
# - Version bumped: 4.0.0 → 4.1.0-b6 (feat = minor)
# - Changelog updated with features and fixes
# - Tests passed
# - Signed release built (Android + iOS)
# - Deployed to Firebase Distribution (Android)
# - Deployed to TestFlight (iOS)
# - GitHub Release v4.1.0-b6 created
```

## 📱 Where to Find Builds

| Location | What | Retention |
|----------|------|-----------|
| **Firebase Distribution** | Latest release AAB for Android testers | Until replaced |
| **TestFlight** | Latest release IPA for iOS beta testers | Until replaced |
| **GitHub Releases** | All releases with APK/AAB/IPA/mapping | Forever |
| **GitHub Actions Artifacts** | Workflow artifacts | 90 days |

## 🎉 Tips

1. **Write clear commit messages** - They become your changelog
2. **Use scopes** - Makes changelog organized (`feat(ui):`, `fix(api):`)
3. **One commit per change** - Easier to understand and revert
4. **Test locally first** - Don't rely on CI to catch bugs
5. **Check Actions tab** - Monitor build progress

## ⚡ Fast Commands

```bash
# Check current version
cat version.properties

# View recent tags
git tag -l --sort=-v:refname | head

# View commits since last tag
git log $(git describe --tags --abbrev=0)..HEAD --oneline

# Manually trigger deploy
gh workflow run master-deploy.yml

# View workflow runs
gh run list

# View latest run logs
gh run view --log
```

---

**Need help?** Check the full docs in `docs/` or ask the team!
