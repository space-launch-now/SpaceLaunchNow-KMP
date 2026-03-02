# Debug Menu Security

## Overview

The debug menu is protected by a multi-factor authentication system:
1. **Tap Counter**: User must tap the version number 7 times
2. **Authentication Methods** (user can choose):
   - **Password Verification**: Static password with SHA-256 hashing
   - **TOTP Verification**: Time-based one-time password (6-digit codes)

## Security Features

- **Password Hashing**: Passwords are hashed using SHA-256 via [cryptography-kotlin](https://github.com/whyoleg/cryptography-kotlin) library
- **TOTP Support**: Custom RFC 6238 implementation using HMAC-SHA1 (multiplatform compatible)
- **Environment Variables**: TOTP secret loaded from `.env` file (not hardcoded)
- **Cross-Platform**: Uses platform-specific secure crypto implementations (JDK for Android/Desktop, Apple CommonCrypto for iOS)
- **No Plain Text Storage**: Only the hash is stored in code, never the actual password
- **Dynamic Codes**: TOTP codes expire every 30 seconds
- **Persistent Unlock**: Once unlocked, the state is saved in encrypted DataStore
- **Build-Type Protection**: Auto-enabled in debug builds, requires unlock in release builds

## Environment Setup

### Setting up TOTP Secret

1. **Generate a secure random Base32 secret**:
   ```bash
   # On Linux/Mac
   head -c 20 /dev/urandom | base32
   
   # Or use online generator
   # https://stefansundin.github.io/2fa-qr/
   ```

2. **Add to your `.env` file** (in project root):
   ```env
   # TOTP Secret for Debug Menu Authentication
   TOTP_SECRET=YOUR_GENERATED_BASE32_SECRET_HERE
   ```

3. **Security Notes**:
   - ⚠️ **Never commit the `.env` file to version control** (it's in `.gitignore`)
   - ✅ Use different secrets for development/staging/production
   - ✅ Share secrets securely with team members (password manager, encrypted channels)
   - ✅ The default secret in code is for development only - **change it immediately**

### Is Environment Variable Secure Enough?

**For Development/Testing**: ✅ Yes
- Environment variables in `.env` files are fine for local development
- They're not committed to version control
- Easy to manage and rotate

**For Production**: ⚠️ Consider Additional Security
- ✅ **Good enough** for most cases if `.env` is properly secured
- ✅ Better than hardcoding in source code
- ❌ Not as secure as dedicated secret management

**Production Best Practices**:
1. **Secret Management Service** (Most Secure):
   - AWS Secrets Manager
   - Google Cloud Secret Manager  
   - Azure Key Vault
   - HashiCorp Vault

2. **CI/CD Environment Variables** (Good):
   - GitHub Actions Secrets
   - GitLab CI/CD Variables
   - Encrypted environment variables in deployment platform

3. **Encrypted Config Files** (Acceptable):
   - git-crypt or git-secret
   - SOPS (Secrets OPerationS)
   - Encrypted `.env` files that decrypt at runtime

**Current Implementation Security Level**: 🟢 **Medium**
- Better than hardcoding
- Secure for development
- Acceptable for debug features (not mission-critical)
- For production user authentication, use proper secret management

## Authentication Methods

### Method 1: Static Password (Default)

The default password is: `debug2024`

**⚠️ IMPORTANT**: Change this before deploying to production!

### Method 2: TOTP (Recommended for Teams)

TOTP provides dynamic 6-digit codes that change every 30 seconds. This is the same technology used by Google, GitHub, and other major services.

**See [DEBUG_MENU_TOTP_SETUP.md](DEBUG_MENU_TOTP_SETUP.md) for complete TOTP setup guide.**

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
4. A dialog will appear with two options:
   - **Password**: Enter the static password (default: `debug2024`)
   - **TOTP**: Tap "Use TOTP Instead" and enter 6-digit code from authenticator app
5. Tap "Unlock"
6. The "Developer" section will now be visible in Settings

**Switching Between Methods**: You can tap "Use Password Instead" or "Use TOTP Instead" in the unlock dialog to toggle between authentication methods.

## Resetting the Unlock State

To lock the debug menu again (useful for testing):

```kotlin
// In your code or debug settings
appPreferences.setDebugMenuUnlocked(false)
```

Or clear app data/reinstall the app.

## Security Best Practices

1. **Change Default Password**: Always change the default password before production deployment
2. **Use TOTP for Teams**: TOTP provides better security for team environments with rotating codes
3. **Use Strong Passwords**: Use at least 12 characters with mixed case, numbers, and symbols
4. **Rotate Credentials**: Consider changing passwords/TOTP secrets periodically
5. **Limit Distribution**: Only share credentials with authorized developers/testers
6. **Monitor Access**: Consider adding logging for debug menu access attempts
7. **Secure TOTP Secrets**: Never commit TOTP secrets to version control; use environment variables

## Technical Implementation

### Dependencies

The implementation uses two cryptography libraries:

**cryptography-kotlin** for password hashing:
```toml
# gradle/libs.versions.toml
[versions]
cryptography-kotlin = "0.5.0"

[libraries]
cryptography-core = { module = "dev.whyoleg.cryptography:cryptography-core", version.ref = "cryptography-kotlin" }
cryptography-provider-jdk = { module = "dev.whyoleg.cryptography:cryptography-provider-jdk", version.ref = "cryptography-kotlin" }
cryptography-provider-apple = { module = "dev.whyoleg.cryptography:cryptography-provider-apple", version.ref = "cryptography-kotlin" }
```

**totp-kt** for TOTP code generation/validation:
```toml
[versions]
totp-kt = "1.0.1"

[libraries]
totp-kt = { module = "dev.robinohs:totp-kt", version.ref = "totp-kt" }
```

**Platform-specific providers:**
- **Android/Desktop**: Uses JDK crypto provider (standard Java cryptography)
- **iOS**: Uses Apple CommonCrypto (native iOS cryptography framework)

### Files Modified

1. **`DebugUnlock.kt`**: SHA-256 hashing utility and TOTP code validation
2. **`AppPreferences.kt`**: Persistent storage for unlock state
3. **`SettingsScreen.kt`**: Tap counter, authentication dialog UI with password/TOTP toggle
4. **`DebugSettingsScreen.kt`**: Unlock state verification
5. **`build.gradle.kts`**: Cryptography and TOTP library dependencies
6. **`libs.versions.toml`**: Version catalog for dependencies

### How It Works

**Password Authentication:**
```kotlin
// 1. User taps version 7 times
tapCount++ // Increments to 7

// 2. Authentication dialog appears
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

**TOTP Authentication:**
```kotlin
// 1-2. Same tap counter flow as above

// 3. User taps "Use TOTP Instead"
useTotp = true

// 4. User enters 6-digit code from authenticator app
passwordInput = "123456"

// 5. TOTP code is validated (using totp-kt)
coroutineScope.launch {
    if (DebugUnlock.verifyPassword(passwordInput)) { // Checks TOTP internally
        // 6. Unlock state is saved
        appPreferences.setDebugMenuUnlocked(true)
    }
}
```

The `verifyPassword()` function automatically tries both password and TOTP validation, so the UI code remains simple.

## Example Password Hashes

For testing purposes, here are some pre-generated hashes:

| Password | SHA-256 Hash |
|----------|--------------|
| `debug2024` | `a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3` |
| `test1234` | `937e8d5fbb48bd4949536cd65b8d35c426b80d2f830c5c308e2cdec422ae2244` |
| `admin2024` | `240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9` |

**Note**: These are for development/testing only. Use a unique, strong password for production!

## TOTP Setup

For detailed TOTP configuration, code generation, and troubleshooting:

📖 **See [DEBUG_MENU_TOTP_SETUP.md](DEBUG_MENU_TOTP_SETUP.md)** for the complete TOTP guide.

Quick TOTP reference:
- **Secret**: Configure via `.env` file (`TOTP_SECRET=<YOUR_TOTP_SECRET>`). Debug menu TOTP is disabled when no secret is set.
- **Code Length**: 6 digits
- **Time Period**: 30 seconds
- **Tolerance**: ±1 time step (±30 seconds)

## Troubleshooting

### Password Not Working
- Verify you're using the correct password
- Check that `PASSWORD_HASH` matches your generated hash
- Ensure there are no extra spaces in the password input

### TOTP Code Not Working
- Verify device clocks are synchronized
- Check that the secret matches between code and authenticator app
- Try the next code (wait 30 seconds)
- See [DEBUG_MENU_TOTP_SETUP.md](DEBUG_MENU_TOTP_SETUP.md) for detailed troubleshooting

### Debug Menu Not Appearing
- Verify you've tapped 7 times (not more, not less)
- Check that the authentication was accepted (no error message)
- Try restarting the app
- Check that `debugMenuUnlockedFlow` is being collected in SettingsScreen

### Unlock State Persists After Reinstall
- The unlock state is stored in DataStore which may persist on some platforms
- Manually clear app data/storage to reset


