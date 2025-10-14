# iOS API Key Configuration

## Overview
The iOS app requires an API key to access The Space Devs Launch Library API. This guide explains how to configure the API key for local development and CI/CD.

## Quick Setup (Local Development)

### Automatic Setup (Recommended)
1. Ensure you have a `.env` file in the project root with your API key:
   ```
   API_KEY="your_api_key_here"
   ```

2. Run the generation script:
   ```bash
   ./scripts/generate-ios-secrets.sh
   ```

3. Add `Secrets.plist` to your Xcode project:
   - Open `iosApp/iosApp.xcodeproj` in Xcode
   - Right-click on the `iosApp` folder in the navigator
   - Select "Add Files to 'iosApp'..."
   - Navigate to `iosApp/iosApp/Secrets.plist`
   - ✅ Check "Copy items if needed"
   - ✅ Check "Add to targets: iosApp"
   - Click "Add"

### Manual Setup
If you prefer to create the file manually:

1. Copy the template:
   ```bash
   cp iosApp/iosApp/Secrets.plist.template iosApp/iosApp/Secrets.plist
   ```

2. Edit `iosApp/iosApp/Secrets.plist` and replace `YOUR_API_KEY_HERE` with your actual API key

3. Add to Xcode project (see step 3 above)

## File Structure

### Secrets.plist Format
```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
	<key>apiKey</key>
	<string>YOUR_API_KEY_HERE</string>
</dict>
</plist>
```

## How It Works

### Code Flow
1. **EnvironmentManager** (`commonMain`) requests API key
2. **AppSecrets** (`iosMain`) reads from `Secrets.plist`
3. **NSBundle** loads the plist from the app bundle
4. API key is injected into Koin dependency graph
5. All API clients use the key for authentication

### Implementation Details

**`AppSecrets.kt` (iOS)**:
```kotlin
actual object AppSecrets {
    actual val apiKey: String
        get() = getStringResource("Secrets", "plist", "apiKey") ?: ""
}
```

**`EnvironmentManager.kt` (Common)**:
```kotlin
fun getEnv(key: String, defaultValue: String = ""): String {
    return when (key) {
        "API_KEY" -> AppSecrets.apiKey
        else -> defaultValue
    }
}
```

## CI/CD Configuration

For GitHub Actions CI/CD, the Secrets.plist is generated automatically during the build process.

### Required GitHub Secret
- `API_KEY` - Your Space Devs API key

### Workflow Integration
The `master-deploy.yml` workflow includes:
```yaml
- name: Create Secrets.plist for iOS
  run: |
    cat > iosApp/iosApp/Secrets.plist << EOF
    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
    <plist version="1.0">
    <dict>
      <key>apiKey</key>
      <string>${{ secrets.API_KEY }}</string>
    </dict>
    </plist>
    EOF
```

## Security

### .gitignore
The actual `Secrets.plist` file is excluded from git:
```gitignore
# iOS Secrets
iosApp/iosApp/Secrets.plist
```

### What's Committed
- ✅ `Secrets.plist.template` - Template file (no real keys)
- ✅ `generate-ios-secrets.sh` - Script to generate from .env
- ❌ `Secrets.plist` - Actual file with API key (gitignored)

## Troubleshooting

### "API key is empty" or "401 Unauthorized"
1. Verify `Secrets.plist` exists in `iosApp/iosApp/`
2. Verify the file is added to the Xcode project
3. Verify the API key value is correct
4. Clean and rebuild the project

### "Secrets.plist not found"
1. Run `./scripts/generate-ios-secrets.sh`
2. Add the file to Xcode project (see Quick Setup above)
3. Ensure the file is in the correct location: `iosApp/iosApp/Secrets.plist`

### "Could not read Secrets.plist"
1. Verify the file format is valid XML
2. Check that the file is included in the app target
3. In Xcode, verify the file appears in "Build Phases → Copy Bundle Resources"

### After changing .env
1. Re-run the generation script: `./scripts/generate-ios-secrets.sh`
2. Clean build folder in Xcode (Cmd+Shift+K)
3. Rebuild the project

## Platform Comparison

| Platform | API Key Source | Configuration File |
|----------|---------------|-------------------|
| **Android** | `.env` → `BuildConfig` | `build.gradle.kts` |
| **iOS** | `.env` → `Secrets.plist` | Generated at build time |
| **Desktop** | `.env` → `DesktopSecret.kt` | Kotlin source file |

## Getting an API Key

To get a free API key for The Space Devs Launch Library:
1. Visit https://thespacedevs.com/llapi
2. Sign up for a free account
3. Copy your API key
4. Add to `.env` file in project root

## References

- [The Space Devs API Documentation](https://thespacedevs.com/llapi)
- [iOS App Bundle Documentation](https://developer.apple.com/documentation/foundation/bundle)
- [Property List Documentation](https://developer.apple.com/library/archive/documentation/General/Conceptual/DevPedia-CocoaCore/PropertyList.html)
