package me.calebjones.spacelaunchnow.util

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.SHA256

/**
 * Secure debug menu unlock utility using SHA-256 hashing
 * Password is never stored in plain text, only the hash is checked
 *
 * Uses cryptography-kotlin library for cross-platform SHA-256 hashing
 */
object DebugUnlock {
    // Store the SHA-256 hash of the password "debug2024"
    // To generate a new hash, use: DebugUnlock.hashPassword("yourpassword")
    private const val PASSWORD_HASH =
        "b8acaa36271bcd82946049271b41e977b48c8dbe61fda4e642d58ea4e6991131"

    /**
     * Verify if the provided password matches the stored hash
     */
    suspend fun verifyPassword(password: String): Boolean {
        val inputHash = hashPassword(password)
        return inputHash.equals(PASSWORD_HASH, ignoreCase = true)
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

        return digest.joinToString("") { "%02x".format(it) }
    }
}

