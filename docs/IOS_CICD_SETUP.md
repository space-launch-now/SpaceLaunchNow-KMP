# iOS CI/CD Setup Guide

This guide explains how to set up iOS app publishing via CI/CD for Space Launch Now KMP.

## Overview

The iOS CI/CD pipeline automatically:
- ✅ Builds the Kotlin Multiplatform framework for iOS
- ✅ Builds and archives the iOS app with Xcode
- ✅ Signs the app with certificates and provisioning profiles
- ✅ Exports the IPA file
- ✅ Uploads to TestFlight for beta testing
- ✅ Uploads IPA to GitHub Releases

## Prerequisites

### 1. Apple Developer Account
- Active Apple Developer Program membership ($99/year)
- Access to App Store Connect
- Team ID from your Apple Developer account

### 2. App Store Connect Setup
1. Create your app in App Store Connect
2. Configure app metadata (name, bundle ID, etc.)
3. Set up TestFlight beta testing

### 3. Required Certificates & Profiles

#### Distribution Certificate (Code Signing)

**Step 1: Generate Certificate Signing Request (CSR) on macOS**

1. Open **Keychain Access** on your Mac
2. Go to **Keychain Access → Certificate Assistant → Request a Certificate from a Certificate Authority**
3. Fill in the form:
   - **User Email Address**: Your email
   - **Common Name**: Your name or company name
   - **CA Email Address**: Leave empty
   - **Request is**: Select "Saved to disk"
4. Click **Continue** and save the `CertificateSigningRequest.certSigningRequest` file

**Step 2: Create Distribution Certificate on Apple Developer**

1. Go to [Apple Developer Certificates](https://developer.apple.com/account/resources/certificates/list)
2. Click the **+** button to create a new certificate
3. Select **iOS Distribution** (under Production section)
4. Click **Continue**
5. Upload the `CertificateSigningRequest.certSigningRequest` file you created in Step 1
6. Click **Continue**
7. Download the certificate (`.cer` file)

**Step 3: Import and Export as P12**

1. Double-click the downloaded `.cer` file to import into **Keychain Access**
2. In Keychain Access, find the certificate under "My Certificates"
   - It will be named "Apple Distribution: [Your Name]" or "Apple Distribution: [Company Name]"
   - Make sure the private key is shown below it (with a key icon)
3. Right-click the certificate (not the private key)
4. Select **Export "Apple Distribution: [Your Name]"**
5. Choose file format: **Personal Information Exchange (.p12)**
6. Save with a strong password (you'll need this for `APPLE_CERTIFICATES_PASSWORD`)
7. The exported `.p12` file is your `APPLE_CERTIFICATES_P12`

**Why P12?** The `.p12` file contains both your certificate AND private key, which is needed to code sign the iOS app in CI/CD.

#### Provisioning Profile
1. Go to [Apple Developer Profiles](https://developer.apple.com/account/resources/profiles/list)
2. Create a new **App Store** provisioning profile
3. Select your Distribution certificate
4. Select your App ID
5. Download the `.mobileprovision` file
6. This is your `APPLE_PROVISIONING_PROFILE`

#### App Store Connect API Key
1. Go to [App Store Connect API Keys](https://appstoreconnect.apple.com/access/api)
2. Create a new API Key with **App Manager** role
3. Download the `.p8` file (only available once!)
4. Note the Key ID and Issuer ID
5. This is your `APPLE_API_KEY_CONTENT`, `APPLE_API_KEY_ID`, and `APPLE_API_ISSUER_ID`

## Required GitHub Secrets

Go to **Repository Settings → Secrets and variables → Actions** and add:

### Apple Signing Secrets

| Secret Name | Description | How to Generate |
|-------------|-------------|-----------------|
| `APPLE_CERTIFICATES_P12` | Distribution certificate | `base64 -i distribution.p12` (macOS) |
| `APPLE_CERTIFICATES_PASSWORD` | Password for P12 file | Password you set when exporting |
| `APPLE_PROVISIONING_PROFILE` | App Store provisioning profile | `base64 -i profile.mobileprovision` |
| `APPLE_TEAM_ID` | Your Apple Developer Team ID | Find in [Apple Developer Account](https://developer.apple.com/account) |
| `IOS_BUNDLE_ID` | iOS app bundle identifier | e.g., `me.calebjones.spacelaunchnow` |

### App Store Connect API Secrets

| Secret Name | Description | How to Find |
|-------------|-------------|-------------|
| `APPLE_API_KEY_ID` | API Key ID | From App Store Connect API page |
| `APPLE_API_ISSUER_ID` | API Issuer ID | From App Store Connect API page |
| `APPLE_API_KEY_CONTENT` | API Key (.p8 file) | `base64 -i AuthKey_XXXXXXXXXX.p8` |

### Export Options Plist

| Secret Name | Description | Template Below |
|-------------|-------------|----------------|
| `IOS_EXPORT_OPTIONS_PLIST` | Export configuration | See template below |

## Export Options Plist Template

Create a file `ExportOptions.plist` with this content:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>app-store</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>uploadBitcode</key>
    <false/>
    <key>uploadSymbols</key>
    <true/>
    <key>signingStyle</key>
    <string>manual</string>
    <key>signingCertificate</key>
    <string>Apple Distribution</string>
    <key>provisioningProfiles</key>
    <dict>
        <key>YOUR_BUNDLE_ID</key>
        <string>YOUR_PROFILE_NAME</string>
    </dict>
</dict>
</plist>
```

Replace:
- `YOUR_TEAM_ID` with your Apple Team ID
- `YOUR_BUNDLE_ID` with your iOS bundle identifier
- `YOUR_PROFILE_NAME` with the name of your provisioning profile

Then encode it:
```bash
base64 -i ExportOptions.plist
```

Save the output as `IOS_EXPORT_OPTIONS_PLIST` secret.

## Step-by-Step Setup

### Step 1: Generate and Encode Certificates

```bash
# On macOS with certificates already in Keychain Access

# 1. Export Distribution Certificate
# - Open Keychain Access
# - Find "Apple Distribution: [Your Name]"
# - Right-click → Export
# - Save as distribution.p12 with a password

# 2. Encode the certificate
base64 -i distribution.p12 | pbcopy
# Paste as APPLE_CERTIFICATES_P12 secret
```

### Step 2: Encode Provisioning Profile

```bash
# Download your App Store provisioning profile from Apple Developer
# Encode it
base64 -i YourProfile.mobileprovision | pbcopy
# Paste as APPLE_PROVISIONING_PROFILE secret
```

### Step 3: Set Up App Store Connect API

```bash
# Encode your API key
base64 -i AuthKey_XXXXXXXXXX.p8 | pbcopy
# Paste as APPLE_API_KEY_CONTENT secret
```

### Step 4: Configure Export Options

```bash
# Create and customize ExportOptions.plist (see template above)
# Encode it
base64 -i ExportOptions.plist | pbcopy
# Paste as IOS_EXPORT_OPTIONS_PLIST secret
```

### Step 5: Add All Secrets to GitHub

1. Go to your repository on GitHub
2. Navigate to **Settings → Secrets and variables → Actions**
3. Click **New repository secret** for each:
   - `APPLE_CERTIFICATES_P12`
   - `APPLE_CERTIFICATES_PASSWORD`
   - `APPLE_PROVISIONING_PROFILE`
   - `APPLE_TEAM_ID`
   - `IOS_BUNDLE_ID`
   - `APPLE_API_KEY_ID`
   - `APPLE_API_ISSUER_ID`
   - `APPLE_API_KEY_CONTENT`
   - `IOS_EXPORT_OPTIONS_PLIST`

### Step 6: Update iOS Project Configuration

Ensure your `iosApp/Configuration/Config.xcconfig` has placeholders:
```
TEAM_ID=
BUNDLE_ID=org.example.project.KotlinProject
APP_NAME=KotlinProject
```

These will be replaced by the CI/CD pipeline with actual values.

## Testing the Pipeline

### Test on Pull Request (Optional)
Create a PR to test the build process without deploying:
```bash
git checkout -b test/ios-ci
git add .
git commit -m "ci: test iOS build pipeline"
git push origin test/ios-ci
gh pr create --title "ci: Test iOS CI/CD" --body "Testing iOS build"
```

### Deploy to TestFlight
Merge to master to trigger full deployment:
```bash
git checkout main
git merge test/ios-ci
git commit -m "feat(ios): enable iOS CI/CD pipeline"
git push origin main
```

The pipeline will:
1. ✅ Bump version
2. ✅ Build Kotlin framework
3. ✅ Build iOS app
4. ✅ Sign with certificates
5. ✅ Upload to TestFlight
6. ✅ Create GitHub Release with IPA

## Troubleshooting

### "No matching provisioning profiles found"
- Verify `APPLE_PROVISIONING_PROFILE` is base64 encoded correctly
- Check that Bundle ID matches between provisioning profile and `IOS_BUNDLE_ID`
- Ensure Team ID in ExportOptions.plist matches `APPLE_TEAM_ID`

### "Code signing error"
- Verify `APPLE_CERTIFICATES_P12` is base64 encoded correctly
- Check `APPLE_CERTIFICATES_PASSWORD` is correct
- Ensure certificate hasn't expired

### "Invalid API Key"
- Verify `APPLE_API_KEY_ID` matches the Key ID from App Store Connect
- Check `APPLE_API_ISSUER_ID` matches Issuer ID from App Store Connect
- Ensure `APPLE_API_KEY_CONTENT` is base64 encoded .p8 file

### "Unable to upload to TestFlight"
- Check App Store Connect API key has **App Manager** role
- Verify app exists in App Store Connect
- Ensure bundle ID matches in all configurations

### "Framework compilation failed"
- Check Gradle task `embedAndSignAppleFrameworkForXcode` exists
- Verify Kotlin Multiplatform is configured correctly
- Check for iOS-specific dependencies issues

## Monitoring

### Check Build Status
- **GitHub Actions:** Repository → Actions tab → master-deploy workflow
- **TestFlight:** App Store Connect → TestFlight tab
- **GitHub Releases:** Repository → Releases

### View Logs
```bash
# View latest workflow run
gh run list --workflow=master-deploy.yml
gh run view --log
```

## Xcode Version

The pipeline uses **Xcode 15.2** on macOS 14 runners. To change:

Update in `.github/workflows/master-deploy.yml`:
```yaml
- name: Select Xcode version
  run: sudo xcode-select -s /Applications/Xcode_15.4.app/Contents/Developer
```

Available versions: [GitHub Actions macOS Runners](https://github.com/actions/runner-images/blob/main/images/macos/macos-14-Readme.md)

## Security Best Practices

1. ✅ **Never commit certificates or keys** - Always use GitHub Secrets
2. ✅ **Use API Keys** - Prefer App Store Connect API over password authentication
3. ✅ **Rotate keys regularly** - Update API keys and certificates annually
4. ✅ **Limit access** - Only grant necessary permissions to API keys
5. ✅ **Use temporary keychains** - CI/CD creates and deletes temporary keychains
6. ✅ **Clean up** - Pipeline automatically removes sensitive files after use

## References

- [Apple Developer Portal](https://developer.apple.com/account)
- [App Store Connect](https://appstoreconnect.apple.com)
- [App Store Connect API](https://appstoreconnect.apple.com/access/api)
- [Xcode Cloud Documentation](https://developer.apple.com/documentation/xcode/distributing-your-app-to-registered-devices)
- [GitHub Actions macOS Runners](https://docs.github.com/en/actions/using-github-hosted-runners/about-github-hosted-runners#supported-runners-and-hardware-resources)

## Next Steps

After iOS CI/CD is set up:

1. ✅ **Test the pipeline** - Create a test commit and verify build
2. ✅ **Configure TestFlight** - Set up beta testing groups in App Store Connect
3. ✅ **Enable notifications** - Get alerts when builds complete
4. ✅ **Monitor feedback** - Check TestFlight for user feedback
5. ✅ **Plan releases** - Schedule production releases to App Store

---

**Need help?** Check the full CI/CD documentation in `docs/CICD_PIPELINE.md`
