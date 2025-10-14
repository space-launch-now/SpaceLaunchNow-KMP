# Android RevenueCat API Key Configuration

## Overview
The Android app requires a RevenueCat API key to access subscription services. This guide explains how to configure the API key for local development and CI/CD.

## Quick Setup (Local Development)

### Step 1: Add Key to .env File

1. Open or create `.env` file in the project root
2. Add your RevenueCat Android API key:
   ```
   # The Space Devs API Key
   API_KEY=your_space_devs_key_here
   
   # RevenueCat API Keys
   REVENUECAT_ANDROID_KEY=goog_your_android_key_here
   REVENUECAT_IOS_KEY=appl_your_ios_key_here
   ```

### Step 2: Sync Gradle

The keys are automatically loaded into `BuildConfig` during Gradle sync:

```bash
# Clean and rebuild to generate BuildConfig
./gradlew clean build
```

### Step 3: Verify Configuration

Run the app and check the debug tools:
1. Open app → Settings → Debug Settings
2. Scroll to "RevenueCat Integration Testing"
3. Click "✅ Check Initialization Status"
4. Should show: `Initialized: true`

## How It Works

### Configuration Flow

```
.env file
  ↓
build.gradle.kts reads keys
  ↓
BuildConfig fields generated
  ↓
RevenueCatConfig.android.kt uses BuildConfig
  ↓
RevenueCatManager initialized with API key
```

### Implementation Details

**`build.gradle.kts`**:
```kotlin
android {
    defaultConfig {
        // Load .env file
        val envFile = rootProject.file(".env")
        val envProps = Properties().apply {
            if (envFile.exists()) {
                envFile.inputStream().use { load(it) }
            }
        }
        
        // Read RevenueCat keys
        val revenueCatAndroidKey = envProps.getProperty("REVENUECAT_ANDROID_KEY") ?: ""
        val revenueCatIosKey = envProps.getProperty("REVENUECAT_IOS_KEY") ?: ""
        
        // Add to BuildConfig
        buildConfigField("String", "REVENUECAT_ANDROID_KEY", "\"$revenueCatAndroidKey\"")
        buildConfigField("String", "REVENUECAT_IOS_KEY", "\"$revenueCatIosKey\"")
    }
}
```

**`RevenueCatConfig.android.kt`**:
```kotlin
actual object RevenueCatConfig {
    /**
     * Android RevenueCat API key from .env file
     * Set REVENUECAT_ANDROID_KEY in .env file
     */
    actual val apiKey: String = BuildConfig.REVENUECAT_ANDROID_KEY
    
    actual val platform: String = "Android"
    actual val isDebug: Boolean = BuildConfig.IS_DEBUG
}
```

**Usage in `RevenueCatManager.kt`**:
```kotlin
class RevenueCatManager {
    suspend fun initialize(appUserId: String? = null) {
        // Skip initialization for unsupported platforms
        if (RevenueCatConfig.apiKey == "desktop_not_supported") {
            return
        }
        
        // Configure Purchases with API key
        Purchases.configure(apiKey = RevenueCatConfig.apiKey) {
            this.appUserId = appUserId
        }
    }
}
```

## CI/CD Configuration

For GitHub Actions CI/CD, the RevenueCat keys are injected via GitHub Secrets.

### Required GitHub Secrets

Add these secrets to your GitHub repository:
- `REVENUECAT_ANDROID_KEY` - Android RevenueCat API key (starts with `goog_`)
- `REVENUECAT_IOS_KEY` - iOS RevenueCat API key (starts with `appl_`)

### Workflow Integration

The `master-deploy.yml` workflow includes:

```yaml
- name: Create .env file
  run: |
    echo "API_KEY=${{ secrets.API_KEY }}" > .env
    echo "REVENUECAT_ANDROID_KEY=${{ secrets.REVENUECAT_ANDROID_KEY }}" >> .env
    echo "REVENUECAT_IOS_KEY=${{ secrets.REVENUECAT_IOS_KEY }}" >> .env
```

## Security

### .gitignore
The `.env` file is excluded from git:
```gitignore
# Environment variables
.env
.env.local
```

### What's Committed
- ✅ `build.gradle.kts` - Gradle configuration to read .env
- ✅ `RevenueCatConfig.android.kt` - Config that uses BuildConfig
- ❌ `.env` - Actual file with API keys (gitignored)
- ❌ `BuildConfig.java` - Generated file (in build/ folder)

### Best Practices

1. **Never commit API keys** - Always use .env or GitHub Secrets
2. **Rotate keys regularly** - Update keys in .env and redeploy
3. **Use different keys for dev/prod** - Separate keys for testing
4. **Limit key permissions** - RevenueCat keys should be read-only

## Troubleshooting

### "No offerings available" or "Unresolved reference: REVENUECAT_ANDROID_KEY"

**Cause**: BuildConfig not generated or .env file missing

**Fix**:
1. Verify `.env` file exists in project root
2. Verify `REVENUECAT_ANDROID_KEY` is set in .env
3. Sync Gradle:
   ```bash
   ./gradlew clean
   ./gradlew :composeApp:generateDebugBuildConfig
   ```
4. Rebuild project

### "API key is empty" or SDK initialization fails

**Cause**: .env file not loaded or key value is empty

**Fix**:
1. Open `.env` file
2. Verify format is correct (no spaces around `=`):
   ```
   REVENUECAT_ANDROID_KEY=goog_your_key_here
   ```
3. Verify key starts with `goog_` (Android)
4. Run: `./gradlew clean build`

### BuildConfig field not found

**Cause**: Build configuration issue

**Fix**:
1. Check `build.gradle.kts` has the `buildConfigField` line
2. Verify you're in the correct source set (androidMain)
3. Invalidate caches: Android Studio → File → Invalidate Caches
4. Rebuild: `./gradlew clean build`

### Keys working locally but not in CI/CD

**Cause**: GitHub Secrets not configured

**Fix**:
1. Go to GitHub repository → Settings → Secrets and variables → Actions
2. Add `REVENUECAT_ANDROID_KEY` secret
3. Add `REVENUECAT_IOS_KEY` secret
4. Re-run workflow

## Getting RevenueCat API Keys

### 1. Create RevenueCat Account
1. Visit https://app.revenuecat.com/
2. Sign up for a free account
3. Create a new project

### 2. Configure App in RevenueCat Dashboard

**For Android:**
1. In RevenueCat dashboard → Projects → Your Project
2. Click **"Google Play"** platform
3. Add your Android package name: `me.calebjones.spacelaunchnow`
4. Complete Google Play setup (service account credentials)

**For iOS:**
1. In RevenueCat dashboard → Projects → Your Project
2. Click **"App Store"** platform
3. Add your iOS bundle ID
4. Complete App Store setup (App Store Connect API key)

### 3. Get API Keys

1. In RevenueCat dashboard → **API keys** (left sidebar)
2. Copy **Google Play API key** (starts with `goog_`)
3. Copy **App Store API key** (starts with `appl_`)
4. Add to `.env` file:
   ```
   REVENUECAT_ANDROID_KEY=goog_copied_key_here
   REVENUECAT_IOS_KEY=appl_copied_key_here
   ```

### 4. Configure Products

1. Go to **Products** in RevenueCat dashboard
2. Create your subscription products:
   - `spacelaunchnow_pro` (lifetime - one-time purchase)
   - `sln_production_yearly` (subscription with base-plan and yearly)
3. Create an **Offering** with identifier: `ofrng74226a750e`
4. Add 3 packages to offering:
   - `$rc_lifetime` → `spacelaunchnow_pro`
   - `$rc_monthly` → `sln_production_yearly:base-plan`
   - `$rc_annual` → `sln_production_yearly:yearly`

## Platform Comparison

| Platform | API Key Source | Configuration Method |
|----------|---------------|---------------------|
| **Android** | `.env` → `BuildConfig` | Gradle build configuration |
| **iOS** | `.env` → `Secrets.plist` | Generated plist file |
| **Desktop** | N/A | Not supported (stub) |

## Verification Steps

### 1. Check .env File
```bash
cat .env
# Should show:
# REVENUECAT_ANDROID_KEY=goog_...
# REVENUECAT_IOS_KEY=appl_...
```

### 2. Check BuildConfig Generation
```bash
./gradlew :composeApp:generateDebugBuildConfig
# Check generated file:
cat composeApp/build/generated/source/buildConfig/debug/me/calebjones/spacelaunchnow/BuildConfig.java
# Should contain:
# public static final String REVENUECAT_ANDROID_KEY = "goog_...";
```

### 3. Run App and Test
```bash
# Install debug build
./gradlew :composeApp:installDebug

# Open app → Settings → Debug Settings
# Click "Check Initialization Status"
# Should show: Initialized: true
```

## Next Steps

After configuring API keys:

1. **Test Initialization** - Use debug tools to verify SDK initializes
2. **Configure Products** - Set up products in RevenueCat dashboard
3. **Test Purchases** - Use sandbox accounts to test purchase flow
4. **Deploy** - Configure GitHub Secrets for CI/CD

## Related Documentation

- [REVENUECAT_TROUBLESHOOTING.md](./REVENUECAT_TROUBLESHOOTING.md) - Common issues and solutions
- [REVENUECAT_DEBUG_TOOLS.md](./REVENUECAT_DEBUG_TOOLS.md) - Testing and verification
- [REVENUECAT_QUICK_START.md](./REVENUECAT_QUICK_START.md) - Implementation steps
- [IOS_API_KEY_SETUP.md](./IOS_API_KEY_SETUP.md) - iOS secrets configuration

## References

- [RevenueCat Documentation](https://www.revenuecat.com/docs/)
- [RevenueCat Dashboard](https://app.revenuecat.com/)
- [Android Build Configuration](https://developer.android.com/studio/build/manifest-build-variables)
