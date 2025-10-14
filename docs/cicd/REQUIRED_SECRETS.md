# Required CI/CD Secrets

This document lists all required secrets for the CI/CD pipeline to function correctly.

## Setup Instructions

Configure these secrets in GitHub repository **Settings → Secrets and variables → Actions**.

---

## 🔑 API Keys

### Space Devs API
| Secret Name | Type | Description |
|------------|------|-------------|
| `API_KEY` | string | Launch Library API key from The Space Devs |

**Where to get:**
- Sign up at https://thespacedevs.com/llapi
- Create an API key in your dashboard
- Copy the key and add to GitHub secrets

### RevenueCat API Keys (⚠️ NEW - Required for Premium Features)

| Secret Name | Type | Description |
|------------|------|-------------|
| `REVENUECAT_ANDROID_KEY` | string | RevenueCat Android SDK public key |
| `REVENUECAT_IOS_KEY` | string | RevenueCat iOS SDK public key |

**Where to get:**
1. Go to https://app.revenuecat.com/
2. Select your project
3. Navigate to **Project Settings → API Keys**
4. Copy the **Public App-Specific API Keys**:
   - **Android:** Starts with `goog_` (e.g., `goog_aBcDeFgHiJkLmNoPqRsTuVwXyZ`)
   - **iOS:** Starts with `appl_` (e.g., `appl_aBcDeFgHiJkLmNoPqRsTuVwXyZ`)

**Important Notes:**
- ✅ Use **Public SDK Keys** (not Secret Keys)
- ✅ These keys are platform-specific
- ✅ Keys are safe to embed in apps (they're public by design)
- ❌ Don't use Secret API Keys (those are for server-side only)

---

## 📱 Android Secrets

### Firebase

| Secret Name | Type | Description |
|------------|------|-------------|
| `FIREBASE_APP_ID` | string | Firebase Android app ID |
| `FIREBASE_GOOGLE_SERVICES_JSON` | base64 | `google-services.json` file encoded as base64 |

**FIREBASE_APP_ID - Where to get:**
1. Go to https://console.firebase.google.com/
2. Select your project
3. Go to **Project Settings → General**
4. Scroll to **Your apps** section
5. Copy the **App ID** (format: `1:123456789:android:abc123def456`)

**FIREBASE_GOOGLE_SERVICES_JSON - How to encode:**

Linux/Mac:
```bash
base64 -i composeApp/google-services.json | tr -d '\n'
```

Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("composeApp\google-services.json"))
```

### Android Signing

| Secret Name | Type | Description |
|------------|------|-------------|
| `KEYSTORE_BASE64` | base64 | Release keystore file encoded as base64 |
| `KEYSTORE_PASSWORD` | string | Password for the keystore |
| `KEY_ALIAS` | string | Alias of the signing key |
| `KEY_PASSWORD` | string | Password for the signing key |

**Creating a Release Keystore:**
```bash
keytool -genkey -v -keystore release.keystore \
  -alias spacelaunchnow \
  -keyalg RSA -keysize 2048 -validity 10000
```

**Encoding the Keystore:**

Linux/Mac:
```bash
base64 -i release.keystore | tr -d '\n'
```

Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore"))
```

### Google Play Distribution

| Secret Name | Type | Description |
|------------|------|-------------|
| `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` | base64 | Service account JSON encoded as base64 |

**Where to get:**
1. Go to https://play.google.com/console
2. Navigate to **Setup → API access**
3. Create or select a service account
4. Download the JSON key file
5. Encode it with base64 (same commands as above)

---

## 🍎 iOS Secrets

### Apple Developer Certificates

| Secret Name | Type | Description |
|------------|------|-------------|
| `APPLE_CERTIFICATE_BASE64` | base64 | Distribution certificate (.p12) encoded as base64 |
| `APPLE_CERTIFICATE_PASSWORD` | string | Password for the .p12 certificate |
| `APPLE_PROVISIONING_PROFILE` | base64 | App Store provisioning profile encoded as base64 |
| `APPLE_TEAM_ID` | string | Apple Developer Team ID |
| `IOS_BUNDLE_ID` | string | iOS app bundle identifier |

**Where to get:**
- See [IOS_CICD_SETUP.md](./IOS_CICD_SETUP.md) for detailed iOS setup instructions

### App Store Connect API

| Secret Name | Type | Description |
|------------|------|-------------|
| `APPLE_API_KEY_ID` | string | App Store Connect API Key ID |
| `APPLE_API_ISSUER_ID` | string | App Store Connect Issuer ID |
| `APPLE_API_KEY_CONTENT` | base64 | API Key .p8 file encoded as base64 |
| `IOS_EXPORT_OPTIONS_PLIST` | base64 | Export options plist encoded as base64 |

**Where to get:**
1. Go to https://appstoreconnect.apple.com/
2. Navigate to **Users and Access → Keys**
3. Create an API key with **App Manager** role
4. Download the .p8 file
5. Copy the **Key ID** and **Issuer ID**

**Encoding .p8 file:**

Linux/Mac:
```bash
base64 -i AuthKey_XXXXXXXXXX.p8 | tr -d '\n'
```

Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("AuthKey_XXXXXXXXXX.p8"))
```

### Firebase (iOS)

| Secret Name | Type | Description |
|------------|------|-------------|
| `FIREBASE_GOOGLE_SERVICE_INFO_PLIST` | base64 | `GoogleService-Info.plist` encoded as base64 |

**How to encode:**

Linux/Mac:
```bash
base64 -i iosApp/iosApp/GoogleService-Info.plist | tr -d '\n'
```

Windows PowerShell:
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("iosApp\iosApp\GoogleService-Info.plist"))
```

---

## ✅ Secrets Checklist

Use this checklist to verify all secrets are configured:

### Essential (Required for All Builds)
- [ ] `API_KEY` - Space Devs Launch Library
- [ ] `REVENUECAT_ANDROID_KEY` - Android premium features ⚠️ NEW
- [ ] `REVENUECAT_IOS_KEY` - iOS premium features ⚠️ NEW

### Android Build Secrets
- [ ] `FIREBASE_APP_ID`
- [ ] `FIREBASE_GOOGLE_SERVICES_JSON`
- [ ] `KEYSTORE_BASE64`
- [ ] `KEYSTORE_PASSWORD`
- [ ] `KEY_ALIAS`
- [ ] `KEY_PASSWORD`
- [ ] `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON`

### iOS Build Secrets
- [ ] `APPLE_CERTIFICATE_BASE64`
- [ ] `APPLE_CERTIFICATE_PASSWORD`
- [ ] `APPLE_PROVISIONING_PROFILE`
- [ ] `APPLE_TEAM_ID`
- [ ] `IOS_BUNDLE_ID`
- [ ] `APPLE_API_KEY_ID`
- [ ] `APPLE_API_ISSUER_ID`
- [ ] `APPLE_API_KEY_CONTENT`
- [ ] `IOS_EXPORT_OPTIONS_PLIST`
- [ ] `FIREBASE_GOOGLE_SERVICE_INFO_PLIST`

---

## 🔒 Security Best Practices

1. **Never commit secrets to git** - Always use GitHub Secrets
2. **Use base64 encoding** - For binary files (keystores, certificates, plists)
3. **Rotate keys regularly** - Update API keys and certificates periodically
4. **Limit permissions** - Service accounts should have minimal required permissions
5. **Monitor usage** - Check GitHub Actions logs for secret access issues

---

## 🚨 Troubleshooting

### Build Fails: "Could not find google-services.json"
**Solution:** Verify `FIREBASE_GOOGLE_SERVICES_JSON` secret is set and properly base64 encoded

### Build Fails: Keystore errors
**Solution:** Verify all keystore secrets:
- `KEYSTORE_BASE64` - Must be valid base64 encoded keystore
- `KEYSTORE_PASSWORD` - Must match keystore password
- `KEY_ALIAS` - Must match key alias in keystore
- `KEY_PASSWORD` - Must match key password

### Build Fails: "RevenueCat API key not found"
**Solution:** Add the new RevenueCat secrets:
- `REVENUECAT_ANDROID_KEY` - Android SDK key (starts with `goog_`)
- `REVENUECAT_IOS_KEY` - iOS SDK key (starts with `appl_`)

### iOS Build Fails: Certificate issues
**Solution:** Check certificate expiration dates and provisioning profiles

---

## 📚 Related Documentation

- [CI/CD Pipeline Overview](./CICD_PIPELINE.md)
- [iOS CI/CD Setup Guide](./IOS_CICD_SETUP.md)
- [Conventional Commits Guide](./CONVENTIONAL_COMMITS.md)
- [Hybrid Release Strategy](./HYBRID_RELEASE_STRATEGY.md)
- [RevenueCat Setup Guide](../billing/REVENUECAT_QUICK_START.md)

---

## 🔄 Version History

- **2025-10-14**: Added RevenueCat Android and iOS API keys (required for premium features)
- **2024-12-XX**: Initial documentation
