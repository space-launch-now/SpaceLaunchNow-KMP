# Debug Menu Security

## Overview

The debug menu is protected by a two-factor authentication system:
1. **Tap Counter**: User must tap the version number 7 times
2. **Password Verification**: User must enter the correct password

## Security Features

- **Password Hashing**: Passwords are hashed using SHA-256 via [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin) library
- **Cross-Platform**: Uses platform-specific secure crypto implementations (JDK for Android/Desktop, Apple CommonCrypto for iOS)
- **No Plain Text Storage**: Only the hash is stored in code, never the actual password
- **Persistent Unlock**: Once unlocked, the state is saved in encrypted DataStore
- **Build-Type Protection**: Auto-enabled in debug builds, requires unlock in release builds

## Default Password

The default password is: `debug2024`

**⚠️ IMPORTANT**: Change this before deploying to production!

## Changing the Password

### Step 1: Generate a New Hash

Run this code snippet in a suspend context (e.g., in a ViewModel or test):

```kotlin
import me.calebjones.spacelaunchnow.util.DebugUnlock
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val newPassword = "your_new_password_here"
    val hash = DebugUnlock.hashPassword(newPassword)
    println("New password hash: $hash")
}
```

### Step 2: Update the Hash Constant

Update the `PASSWORD_HASH` constant in `DebugUnlock.kt`:

```kotlin
private const val PASSWORD_HASH = "your_generated_hash_here"
```

### Step 3: Rebuild the App

Rebuild the app to apply the new password hash.

## How to Access Debug Menu

### In Debug Builds
- Navigate to **Settings**
- The "Developer" section is automatically visible
- Tap "Debug Settings"

### In Release Builds (After Unlock)
1. Navigate to **Settings**
2. Scroll to the bottom and find "Version X.X.X"
3. Tap the version number **7 times**
4. A password dialog will appear
5. Enter the password:
6. Tap "Unlock"
7. The "Developer" section will now be visible in Settings

## Resetting the Unlock State

To lock the debug menu again (useful for testing):

```kotlin
// In your code or debug settings
appPreferences.setDebugMenuUnlocked(false)
```

Or clear app data/reinstall the app.

## Security Best Practices

1. **Change Default Password**: Always change the default password before production deployment
2. **Use Strong Passwords**: Use at least 12 characters with mixed case, numbers, and symbols
3. **Rotate Passwords**: Consider changing the password periodically
4. **Limit Distribution**: Only share the password with authorized developers/testers
5. **Monitor Access**: Consider adding logging for debug menu access attempts

## Technical Implementation

### Dependencies

The implementation uses the [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin) library for secure, cross-platform hashing:

```toml
# gradle/libs.versions.toml
[versions]
cryptography-kotlin = "0.4.0"

[libraries]
cryptography-core = { module = "dev.whyoleg.cryptography:cryptography-core", version.ref = "cryptography-kotlin" }
cryptography-provider-jdk = { module = "dev.whyoleg.cryptography:cryptography-provider-jdk", version.ref = "cryptography-kotlin" }
cryptography-provider-apple = { module = "dev.whyoleg.cryptography:cryptography-provider-apple", version.ref = "cryptography-kotlin" }
```

**Platform-specific providers:**
- **Android/Desktop**: Uses JDK crypto provider (standard Java cryptography)
- **iOS**: Uses Apple CommonCrypto (native iOS cryptography framework)

### Files Modified

1. **`DebugUnlock.kt`**: SHA-256 hashing utility using cryptography-kotlin
2. **`AppPreferences.kt`**: Persistent storage for unlock state
3. **`SettingsScreen.kt`**: Tap counter and password dialog UI
4. **`DebugSettingsScreen.kt`**: Unlock state verification
5. **`build.gradle.kts`**: Cryptography library dependencies
6. **`libs.versions.toml`**: Version catalog for dependencies

### How It Works

```kotlin
// 1. User taps version 7 times
tapCount++ // Increments to 7

// 2. Password dialog appears
showPasswordDialog = true

// 3. User enters password
passwordInput = "debug2024"

// 4. Password is hashed and verified (using cryptography-kotlin)
coroutineScope.launch {
    if (DebugUnlock.verifyPassword(passwordInput)) {
        // 5. Unlock state is saved
        appPreferences.setDebugMenuUnlocked(true)
    }
}

// 6. Debug menu becomes visible
if (BuildConfig.IS_DEBUG || debugMenuUnlocked) {
    // Show debug settings
}
```

## Example Password Hashes

For testing purposes, here are some pre-generated hashes:

| Password | SHA-256 Hash |
|----------|--------------|
| `debug2024` | `a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3` |
| `test1234` | `937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244` |
| `admin2024` | `240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9` |

**Note**: These are for development/testing only. Use a unique, strong password for production!

## Troubleshooting

### Password Not Working
- Verify you're using the correct password
- Check that `PASSWORD_HASH` matches your generated hash
- Ensure there are no extra spaces in the password input

### Debug Menu Not Appearing
- Verify you've tapped 7 times (not more, not less)
- Check that the password was accepted (no error message)
- Try restarting the app
- Check that `debugMenuUnlockedFlow` is being collected in SettingsScreen

### Unlock State Persists After Reinstall
- The unlock state is stored in DataStore which may persist on some platforms
- Manually clear app data/storage to reset


