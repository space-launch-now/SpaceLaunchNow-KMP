# CI/CD Pipeline Setup Guide

This guide walks through setting up the complete CI/CD pipeline for Space Launch Now KMP.

## Prerequisites

- GitHub repository with admin access
- Google Play Console developer account
- Android signing keystore
- Firebase project (for push notifications)
- The Space Devs API key

## Step 1: Generate Android Signing Keystore

If you don't have a keystore yet:

```bash
keytool -genkey -v -keystore release.keystore -alias space-launch-now \
  -keyalg RSA -keysize 2048 -validity 10000
```

Convert keystore to base64 for GitHub secrets:

```bash
# On Linux/Mac
base64 -i release.keystore -o keystore.base64

# On Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File keystore.base64
```

## Step 2: Set Up Google Play Console Service Account

### 2.1 Create Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Create a new project or select existing
3. Enable the **Google Play Android Developer API**
4. Go to **IAM & Admin** → **Service Accounts**
5. Click **Create Service Account**
6. Name: `github-actions-play-console`
7. Grant roles:
    - Service Account User
    - Service Account Token Creator
8. Create and download JSON key

### 2.2 Grant Play Console Access

1. Go to [Google Play Console](https://play.google.com/console)
2. Go to **Settings** → **API access**
3. Link the Google Cloud project
4. Find your service account
5. Click **Grant access**
6. Set permissions:
    - **Release management**: Release to production, exclude devices
    - **Financial data**: View financial data (optional)
    - **Store presence**: Edit store listing, pricing & distribution
7. Select your app
8. Save changes

## Step 3: Configure GitHub Secrets

Go to your GitHub repository → Settings → Secrets and variables → Actions

Add the following secrets:

### Required Secrets

| Secret Name | Description | How to Get |
|------------|-------------|------------|
| `KEYSTORE_BASE64` | Base64 encoded keystore | From Step 1 |
| `KEYSTORE_PASSWORD` | Keystore password | Password you set in Step 1 |
| `KEY_ALIAS` | Key alias in keystore | `space-launch-now` or your chosen alias |
| `KEY_PASSWORD` | Key password | Password for the specific key |
| `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` | Service account JSON | From Step 2.1 |
| `API_KEY` | The Space Devs API key | From your account |
| `FIREBASE_GOOGLE_SERVICES_JSON` | Base64 encoded google-services.json | See below |

### Encoding google-services.json

```bash
# Encode the file
base64 -i composeApp/google-services.json -o google-services.base64

# Copy contents and add as secret
cat google-services.base64
```

## Step 4: Configure Repository Settings

### 4.1 Branch Protection

1. Go to Settings → Branches
2. Add rule for `master` branch:
    - ✅ Require pull request reviews before merging
    - ✅ Dismiss stale pull request approvals
    - ✅ Require status checks to pass
    - ✅ Require branches to be up to date
    - ✅ Include administrators

### 4.2 Actions Permissions

1. Go to Settings → Actions → General
2. Set permissions:
    - ✅ Allow GitHub Actions to create and approve pull requests
    - Workflow permissions: **Read and write permissions**

## Step 5: Test the Pipeline

### 5.1 Test PR Validation

1. Create a feature branch
2. Make a small change
3. Create a PR to `develop` or `master`
4. Verify the PR validation workflow runs

### 5.2 Test Deployment (Dry Run)

1. Temporarily modify `deploy-android.yml`:
   ```yaml
   # Comment out the actual upload step
   # - name: Upload to Play Console
   ```
2. Push to master
3. Verify the build completes
4. Check artifacts are created

### 5.3 Test Full Deployment

1. Restore the upload step
2. Merge a PR to master
3. Monitor the deployment workflow
4. Check Google Play Console Alpha track

## Step 6: Version Management

The pipeline auto-increments build numbers. To change major/minor/patch:

1. Edit `version.properties`:
   ```properties
   versionMajor=4
   versionMinor=1  # Increment for features
   versionPatch=0  # Increment for fixes
   versionBuildNumber=1  # Auto-incremented
   ```
2. Commit directly to master with `[skip ci]` in message

## Step 7: Production Promotion

To promote from Alpha to Production:

1. Go to Actions → **Promote to Production**
2. Click **Run workflow**
3. Select:
    - From: `alpha`
    - To: `production`
    - Rollout: `10` (percentage)
4. Monitor in Play Console

## Troubleshooting

### Build Fails: "Keystore not found"

- Verify `KEYSTORE_BASE64` is correctly encoded
- Check no extra newlines in base64 string

### Play Console Upload: "Version code already exists"

- Check `version.properties` was committed
- Manually increment `versionBuildNumber`

### Play Console Upload: "Unauthorized"

- Verify service account has correct permissions
- Check API is enabled in Google Cloud Console
- Ensure service account is linked in Play Console

### Firebase Error

- Verify `google-services.json` is valid
- Check Firebase project configuration
- Ensure package name matches

## Monitoring

### GitHub Actions

- Check workflow runs: Actions tab
- Enable notifications: Settings → Notifications

### Google Play Console

- Monitor crash reports
- Check user feedback
- Review staged rollout metrics

## Maintenance

### Monthly Tasks

- Review and update dependencies
- Check for security updates in actions
- Monitor build times and optimize

### Quarterly Tasks

- Rotate service account keys
- Review branch protection rules
- Update documentation

## Support

For issues with:

- **Pipeline**: Check `.github/workflows/` files
- **Play Console**: Contact Google Play support
- **Secrets**: Repository admin only

## Quick Commands

```bash
# Check current version
grep version version.properties

# Manual version bump
sed -i 's/versionMinor=0/versionMinor=1/' version.properties

# Test build locally
./gradlew :composeApp:assembleRelease

# Clean build
./gradlew clean build
```

---

*Last Updated: December 2024*