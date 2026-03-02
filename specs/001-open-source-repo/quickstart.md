# Quickstart: Developer Secrets Setup

**Branch**: `001-open-source-repo` | **Date**: 2026-03-02

This guide explains how to configure local secrets after cloning SpaceLaunchNow-KMP as an open-source contributor.

## Prerequisites

- JDK 21 (JetBrains JDK recommended)
- Android Studio or IntelliJ IDEA
- Xcode 15+ (for iOS builds)

## Step 1: Create `.env` File

Create a `.env` file in the project root:

```bash
cp .env.example .env
```

Edit `.env` with your values:

```env
# Required: Space Devs API key (get one at https://thespacedevs.com)
API_KEY=your_api_key_here

# Optional: Debug menu TOTP secret (base32-encoded)
# Generate one with: python -c "import base64, os; print(base64.b32encode(os.urandom(20)).decode())"
TOTP_SECRET=

# Optional: Datadog monitoring (leave empty to disable)
DATADOG_CLIENT_TOKEN=
DATADOG_APPLICATION_ID=
```

## Step 2: Configure Android Signing (Optional)

For release builds only. Create `keystore.properties` in the project root:

```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=your_key_alias
storeFile=/path/to/your/keystore.jks
```

## Step 3: Configure Firebase (Optional)

For push notifications and analytics:

1. Create a Firebase project at https://console.firebase.google.com
2. Download `google-services.json` and place it in `composeApp/`
3. For iOS: Download `GoogleService-Info.plist` and place it in `iosApp/iosApp/`

## Step 4: Configure iOS Secrets (Optional)

For iOS builds with full functionality:

1. Copy the secrets template:
   ```bash
   cp iosApp/iosApp/Secrets.plist.example iosApp/iosApp/Secrets.plist
   ```
2. Fill in your values in `Secrets.plist`

## Step 5: Generate API Client

```bash
./gradlew openApiGenerate
```

## Step 6: Build & Run

```bash
# Android
./gradlew installDebug

# Desktop
./gradlew desktopRun

# iOS (requires Xcode)
# Open iosApp/iosApp.xcodeproj in Xcode
```

## Environment Variables Reference

| Variable | Required | Description |
|----------|----------|-------------|
| `API_KEY` | Yes | Space Devs Launch Library API key |
| `TOTP_SECRET` | No | Base32 TOTP secret for debug menu access |
| `DATADOG_CLIENT_TOKEN` | No | Datadog RUM client token |
| `DATADOG_APPLICATION_ID` | No | Datadog RUM application ID |

## Troubleshooting

- **Build fails with missing API key**: Ensure `.env` file exists with `API_KEY` set
- **Generated code missing**: Run `./gradlew openApiGenerate` after clean checkout
- **Import errors in IDE**: Rebuild project and invalidate caches
- **iOS build fails**: Ensure you've set up signing in Xcode and created `Secrets.plist`

## What's Gitignored

The following sensitive files are excluded from version control:
- `.env` — API keys and secrets
- `keystore.properties` — Android signing config
- `*.jks` / `*.keystore` — Signing keystores
- `google-services.json` — Firebase config
- `GoogleService-Info.plist` — iOS Firebase config
- `Secrets.plist` — iOS app secrets
- `DesktopSecret.kt` — Desktop platform secrets
- `local.properties` — Local SDK paths
