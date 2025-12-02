package me.calebjones.spacelaunchnow.util

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.HMAC
import me.calebjones.spacelaunchnow.util.logging.logger
import dev.whyoleg.cryptography.algorithms.SHA1
import dev.whyoleg.cryptography.algorithms.SHA256

/**
 * Secure debug menu unlock utility using SHA-256 hashing and TOTP
 * Supports two authentication methods:
 * 1. Static password (hashed with SHA-256)
 * 2. Time-based One-Time Password (TOTP) - Custom implementation using cryptography-kotlin
 *
 * Uses cryptography-kotlin library for cross-platform hashing (SHA-256 for password, SHA1-HMAC for TOTP)
 */
object DebugUnlock {
    private val log = logger()
    
    // Store the SHA-256 hash of the password "debug2024"
    // To generate a new hash, use: DebugUnlock.hashPassword("yourpassword")
    private const val PASSWORD_HASH =
        "b8acaa36271bcd82946049271b41e977b48c8dbe61fda4e642d58ea4e6991131"

    // TOTP Configuration
    // Load from environment variable for security - DO NOT hardcode in production!
    // Add to your .env file: TOTP_SECRET=YOUR_BASE32_SECRET
    // Generate a new secret: https://stefansundin.github.io/2fa-qr/
    // Fallback for development/testing only
    private val TOTP_SECRET_BASE32 = EnvironmentManager.getEnv(
        "TOTP_SECRET_BASE32",
        "JBSWY3DPEHPK3PXP" // Default for development only - CHANGE THIS!
    )

    // TOTP settings (standard RFC 6238 defaults)
    private const val TOTP_DIGITS = 6
    private const val TOTP_PERIOD_SECONDS = 30L
    private const val TOTP_TOLERANCE = 1 // Allow 1 time step before/after for clock drift

    /**
     * Verify if the provided input matches either the password hash or is a valid TOTP code
     * @param input The password or TOTP code to verify
     * @return true if authentication succeeds via either method
     */
    suspend fun verifyPassword(input: String): Boolean {
        // Try password verification first
        if (verifyStaticPassword(input)) {
            return true
        }

        // Try TOTP verification
        return verifyTotpCode(input)
    }

    /**
     * Verify if the provided password matches the stored hash
     */
    private suspend fun verifyStaticPassword(password: String): Boolean {
        val inputHash = hashPassword(password)
        return inputHash.equals(PASSWORD_HASH, ignoreCase = true)
    }

    /**
     * Verify if the provided code is a valid TOTP code
     * @param code The 6-digit TOTP code to verify
     * @return true if the code is valid (within tolerance window)
     */
    private suspend fun verifyTotpCode(code: String): Boolean {
        // TOTP codes should be exactly 6 digits
        if (code.length != TOTP_DIGITS || !code.all { it.isDigit() }) {
            return false
        }

        try {
            val secret = decodeBase32(TOTP_SECRET_BASE32)
            val currentTimeSeconds = kotlin.time.Clock.System.now().epochSeconds
            val currentWindow = currentTimeSeconds / TOTP_PERIOD_SECONDS

            // Check current window and adjacent windows (tolerance)
            for (offset in -TOTP_TOLERANCE..TOTP_TOLERANCE) {
                val windowToCheck = currentWindow + offset
                val generatedCode = generateTotpCode(secret, windowToCheck)
                if (code == generatedCode) {
                    return true
                }
            }

            return false
        } catch (e: Exception) {
            // Log error in debug builds
            log.e(e) { "TOTP verification error" }
            return false
        }
    }

    /**
     * Generate the current TOTP code for testing/debugging
     * Useful for generating codes without an authenticator app
     */
    suspend fun generateCurrentTotpCode(): String {
        val secret = decodeBase32(TOTP_SECRET_BASE32)
        val currentTimeSeconds = kotlin.time.Clock.System.now().epochSeconds
        val currentWindow = currentTimeSeconds / TOTP_PERIOD_SECONDS
        return generateTotpCode(secret, currentWindow)
    }

    /**
     * Generate a TOTP code for a specific time window using HMAC-SHA1
     * Implements RFC 6238 TOTP algorithm
     */
    @OptIn(DelicateCryptographyApi::class)
    private suspend fun generateTotpCode(secret: ByteArray, timeWindow: Long): String {
        // Convert time window to 8-byte array (big-endian)
        val timeBytes = ByteArray(8)
        var time = timeWindow
        for (i in 7 downTo 0) {
            timeBytes[i] = (time and 0xFF).toByte()
            time = time shr 8
        }

        // Compute HMAC-SHA1 using cryptography-kotlin
        val provider = CryptographyProvider.Default
        val hmac = provider.get(HMAC)

        // Decode the secret key for HMAC
        val keyDecoder = hmac.keyDecoder(SHA1)
        val hmacKey = keyDecoder.decodeFromByteArrayBlocking(HMAC.Key.Format.RAW, secret)

        // Generate signature using the key's signature generator
        val signature = hmacKey.signatureGenerator().generateSignatureBlocking(timeBytes)

        // Dynamic truncation (RFC 6238)
        val offset = (signature.last().toInt() and 0x0F)
        val binary = ((signature[offset].toInt() and 0x7F) shl 24) or
                ((signature[offset + 1].toInt() and 0xFF) shl 16) or
                ((signature[offset + 2].toInt() and 0xFF) shl 8) or
                (signature[offset + 3].toInt() and 0xFF)

        // Generate digits
        val otp = binary % 1000000 // 10^6 for 6 digits
        return otp.toString().padStart(TOTP_DIGITS, '0')
    }

    /**
     * Generate SHA-256 hash of a password using cryptography-kotlin library
     * Use this to generate new password hashes for PASSWORD_HASH constant
     */
    suspend fun hashPassword(password: String): String {
        val provider = CryptographyProvider.Default
        val sha256 = provider.get(SHA256)
        val hasher = sha256.hasher()

        val bytes = password.encodeToByteArray()
        val digest = hasher.hash(bytes)

        // KMP-compatible hex string conversion
        return digest.joinToString("") { byte ->
            val hex = (byte.toInt() and 0xFF).toString(16)
            if (hex.length == 1) "0$hex" else hex
        }
    }

    /**
     * Decode a Base32-encoded string to ByteArray for TOTP
     * Base32 alphabet: A-Z, 2-7 (case-insensitive)
     */
    private fun decodeBase32(encoded: String): ByteArray {
        val base32Chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        val clean = encoded.uppercase().replace("=", "")

        val bits = clean.map { char ->
            base32Chars.indexOf(char).toByte()
        }.joinToString("") { byte ->
            byte.toString(2).padStart(5, '0')
        }

        val bytes = mutableListOf<Byte>()
        for (i in bits.indices step 8) {
            if (i + 8 <= bits.length) {
                val byteString = bits.substring(i, i + 8)
                bytes.add(byteString.toInt(2).toByte())
            }
        }

        return bytes.toByteArray()
    }

    /**
     * Get the TOTP secret in a QR-code compatible format
     * Use this to generate a QR code for authenticator apps
     * Format: otpauth://totp/SpaceLaunchNow:Debug?secret=SECRET&issuer=SpaceLaunchNow
     */
    fun getTotpUri(label: String = "Debug"): String {
        return "otpauth://totp/SpaceLaunchNow:$label?secret=$TOTP_SECRET_BASE32&issuer=SpaceLaunchNow&digits=$TOTP_DIGITS&period=$TOTP_PERIOD_SECONDS"
    }
}
