package me.calebjones.spacelaunchnow.data.model

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Remote config data for pinning/featuring content at the top of the home screen.
 * 
 * This allows featuring launches or events via Firebase Remote Config
 * without requiring an app update.
 * 
 * Example JSON:
 * ```json
 * {
 *   "type": "LAUNCH",
 *   "id": "abc123-uuid-here",
 *   "enabled": true,
 *   "expiresAt": "2026-04-15T00:00:00Z",
 *   "customMessage": "Don't miss this historic launch!"
 * }
 * ```
 */
@Serializable
data class PinnedContent(
    /** Type of content being pinned */
    val type: PinnedContentType,
    
    /** ID of the content to pin (UUID string for launches, Int as string for events) */
    val id: String,
    
    /** Whether this pinned content should be displayed */
    val enabled: Boolean = true,
    
    /** Optional expiration date - content auto-hides after this date */
    @SerialName("expiresAt")
    val expiresAt: Instant? = null,
    
    /** Optional custom message to display on the card (e.g., "Don't miss!") */
    @SerialName("customMessage")
    val customMessage: String? = null
) {
    /**
     * Check if this pinned content is currently active (enabled and not expired)
     */
    fun isActive(currentTime: Instant): Boolean {
        if (!enabled) return false
        if (expiresAt != null && currentTime > expiresAt) return false
        return true
    }
}

/**
 * Type of content that can be pinned/featured
 */
@Serializable
enum class PinnedContentType {
    /** A launch - id should be UUID string */
    @SerialName("LAUNCH")
    LAUNCH,
    
    /** An event - id should be event ID (integer as string) */
    @SerialName("EVENT")
    EVENT
}
