package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Clock.System
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.preferences.WidgetThemeSource
import me.calebjones.spacelaunchnow.util.logging.logger
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

/**
 * Manages temporary premium access granted through rewarded ads
 * Provides 24-hour access to premium features after watching ads
 */
class TemporaryPremiumAccess(
    private val dataStore: DataStore<Preferences>,
    private val themePreferences: ThemePreferences? = null,
    private val widgetPreferences: WidgetPreferences? = null
) {
    private val log = logger()

    companion object {
        private val TEMP_CUSTOM_THEMES_EXPIRES_AT = longPreferencesKey("temp_custom_themes_expires_at")
        private val TEMP_ADVANCED_WIDGETS_EXPIRES_AT = longPreferencesKey("temp_advanced_widgets_expires_at")
        private val TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT = longPreferencesKey("temp_widgets_customization_expires_at")
        
        // Duration of temporary access (24 hours)
        private val ACCESS_DURATION = 1.days
    }

    // StateFlow to emit when temporary access changes
    private val _accessChangeTrigger = MutableStateFlow(0L)
    val accessChangeTrigger: StateFlow<Long> = _accessChangeTrigger.asStateFlow()

    private fun notifyAccessChanged() {
        val newValue = System.now().toEpochMilliseconds()
        _accessChangeTrigger.value = newValue
        log.d { "🔔 notifyAccessChanged() -> trigger = $newValue" }
    }

    /**
     * Check if user has temporary access to a specific premium feature
     */
    suspend fun hasTemporaryAccess(feature: PremiumFeature): Boolean {
        val now = System.now()
        val expiresAt = getExpirationTime(feature)
        val hasAccess = expiresAt?.let { now < it } ?: false
        log.d { "🔍 hasTemporaryAccess($feature): now=$now, expiresAt=$expiresAt, hasAccess=$hasAccess" }
        return hasAccess
    }

    /**
     * Grant temporary access to a premium feature for 24 hours
     */
    suspend fun grantTemporaryAccess(feature: PremiumFeature) {
        val expiresAt = System.now().plus(ACCESS_DURATION)
        
        log.i { "🎁 grantTemporaryAccess($feature): granting until $expiresAt" }
        
        dataStore.edit { preferences ->
            when (feature) {
                PremiumFeature.CUSTOM_THEMES -> {
                    preferences[TEMP_CUSTOM_THEMES_EXPIRES_AT] = expiresAt.toEpochMilliseconds()
                    log.d { "✅ Saved TEMP_CUSTOM_THEMES_EXPIRES_AT = ${expiresAt.toEpochMilliseconds()}" }
                }
                PremiumFeature.ADVANCED_WIDGETS -> {
                    preferences[TEMP_ADVANCED_WIDGETS_EXPIRES_AT] = expiresAt.toEpochMilliseconds()
                    log.d { "✅ Saved TEMP_ADVANCED_WIDGETS_EXPIRES_AT = ${expiresAt.toEpochMilliseconds()}" }
                }
                PremiumFeature.WIDGETS_CUSTOMIZATION -> {
                    preferences[TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT] = expiresAt.toEpochMilliseconds()
                    log.d { "✅ Saved TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT = ${expiresAt.toEpochMilliseconds()}" }
                }
                else -> {
                    // Only specific features are supported for temporary access
                    log.w { "❌ Feature $feature not supported for temporary access" }
                }
            }
        }
        
        log.i { "✅ Granted 24h access to $feature until $expiresAt" }
        notifyAccessChanged()  // Notify listeners that access has changed
        
        // Verify it was saved
        val savedExpiresAt = getExpirationTime(feature)
        log.d { "🔍 Verification read for $feature = $savedExpiresAt" }
    }

    /**
     * Get the expiration time for a specific feature's temporary access
     */
    private suspend fun getExpirationTime(feature: PremiumFeature): Instant? {
        val preferences = dataStore.data.first()
        val expiresAtMs = when (feature) {
            PremiumFeature.CUSTOM_THEMES -> preferences[TEMP_CUSTOM_THEMES_EXPIRES_AT]
            PremiumFeature.ADVANCED_WIDGETS -> preferences[TEMP_ADVANCED_WIDGETS_EXPIRES_AT]
            PremiumFeature.WIDGETS_CUSTOMIZATION -> preferences[TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT]
            else -> null
        }
        
        log.v { "📖 getExpirationTime($feature): expiresAtMs=$expiresAtMs" }
        return expiresAtMs?.let { Instant.fromEpochMilliseconds(it) }
    }

    /**
     * Get time remaining for temporary access to a feature
     * Returns null if no temporary access or expired
     */
    suspend fun getTimeRemaining(feature: PremiumFeature): kotlinx.datetime.DateTimePeriod? {
        val expiresAt = getExpirationTime(feature) ?: return null
        val now = System.now()
        
        return if (now < expiresAt) {
            // Calculate remaining time (simplified to just show hours)
            val remainingMs = expiresAt.toEpochMilliseconds() - now.toEpochMilliseconds()
            val remainingHours = (remainingMs / (1000 * 60 * 60)).toInt()
            kotlinx.datetime.DateTimePeriod(hours = remainingHours)
        } else {
            null
        }
    }

    /**
     * Flow that emits whether the user has temporary access to custom themes
     */
    val hasTemporaryCustomThemes: Flow<Boolean> = dataStore.data.map { preferences ->
        val expiresAtMs = preferences[TEMP_CUSTOM_THEMES_EXPIRES_AT] ?: return@map false
        val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
        System.now() < expiresAt
    }

    /**
     * Flow that emits whether the user has temporary access to advanced widgets
     */
    val hasTemporaryAdvancedWidgets: Flow<Boolean> = dataStore.data.map { preferences ->
        val expiresAtMs = preferences[TEMP_ADVANCED_WIDGETS_EXPIRES_AT] ?: return@map false
        val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
        System.now() < expiresAt
    }

    /**
     * Flow that emits whether the user has temporary access to widgets customization
     */
    val hasTemporaryWidgetsCustomization: Flow<Boolean> = dataStore.data.map { preferences ->
        val expiresAtMs = preferences[TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT] ?: return@map false
        val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
        System.now() < expiresAt
    }

    /**
     * Reset premium settings when temporary access expires
     */
    private suspend fun resetPremiumSettings(feature: PremiumFeature) {
        when (feature) {
            PremiumFeature.CUSTOM_THEMES -> {
                // Reset theme customizations to defaults
                themePreferences?.let { prefs ->
                    prefs.resetToDefaults()
                    log.i { "Reset theme settings to defaults" }
                }
            }
            PremiumFeature.ADVANCED_WIDGETS -> {
                // Reset widget settings to defaults (if needed)
                widgetPreferences?.let { prefs ->
                    // Note: ADVANCED_WIDGETS is about access, not customization
                    // So we don't reset settings here, just remove access
                    log.i { "Removed advanced widgets access" }
                }
            }
            PremiumFeature.WIDGETS_CUSTOMIZATION -> {
                // Reset widget customizations to defaults
                widgetPreferences?.let { prefs ->
                    prefs.updateWidgetThemeSource(WidgetThemeSource.FOLLOW_APP_THEME)
                    prefs.updateWidgetBackgroundAlpha(0.95f) // Default alpha
                    prefs.updateWidgetCornerRadius(16) // Default radius
                    log.i { "Reset widget customization settings to defaults" }
                }
            }
            else -> {
                // No settings to reset for other features
            }
        }
    }

    /**
     * Clear expired temporary access entries
     * Should be called periodically to cleanup old entries
     */
    suspend fun cleanupExpiredAccess() {
        val now = System.now()
        val expiredFeatures = mutableListOf<PremiumFeature>()
        
        dataStore.edit { preferences ->
            // Check and remove expired custom themes access
            preferences[TEMP_CUSTOM_THEMES_EXPIRES_AT]?.let { expiresAtMs ->
                val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
                if (now >= expiresAt) {
                    preferences.remove(TEMP_CUSTOM_THEMES_EXPIRES_AT)
                    expiredFeatures.add(PremiumFeature.CUSTOM_THEMES)
                    log.i { "Cleaned up expired custom themes access" }
                }
            }
            
            // Check and remove expired advanced widgets access
            preferences[TEMP_ADVANCED_WIDGETS_EXPIRES_AT]?.let { expiresAtMs ->
                val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
                if (now >= expiresAt) {
                    preferences.remove(TEMP_ADVANCED_WIDGETS_EXPIRES_AT)
                    expiredFeatures.add(PremiumFeature.ADVANCED_WIDGETS)
                    log.i { "Cleaned up expired advanced widgets access" }
                }
            }
            
            // Check and remove expired widgets customization access
            preferences[TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT]?.let { expiresAtMs ->
                val expiresAt = Instant.fromEpochMilliseconds(expiresAtMs)
                if (now >= expiresAt) {
                    preferences.remove(TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT)
                    expiredFeatures.add(PremiumFeature.WIDGETS_CUSTOMIZATION)
                    log.i { "Cleaned up expired widgets customization access" }
                }
            }
        }
        
        // Reset settings for expired features (outside of dataStore.edit to avoid conflicts)
        expiredFeatures.forEach { feature ->
            resetPremiumSettings(feature)
        }
        
        // Notify if any access expired
        if (expiredFeatures.isNotEmpty()) {
            notifyAccessChanged()
        }
    }

    /**
     * Clear all temporary access (for testing or manual reset)
     */
    suspend fun clearAllTemporaryAccess() {
        dataStore.edit { preferences ->
            preferences.remove(TEMP_CUSTOM_THEMES_EXPIRES_AT)
            preferences.remove(TEMP_ADVANCED_WIDGETS_EXPIRES_AT)
            preferences.remove(TEMP_WIDGETS_CUSTOMIZATION_EXPIRES_AT)
        }
        log.i { "Cleared all temporary access" }
        notifyAccessChanged()  // Notify listeners that access has changed
    }

    /**
     * Get comprehensive information about temporary access for a feature
     */
    suspend fun getTemporaryAccessInfo(feature: PremiumFeature): TemporaryAccessStatus? {
        val hasAccess = hasTemporaryAccess(feature)
        if (!hasAccess) return null
        
        val expiresAt = getExpirationTime(feature)
        val timeRemaining = getTimeRemaining(feature)
        
        return TemporaryAccessStatus(
            hasAccess = true,
            expiresAt = expiresAt,
            timeRemaining = timeRemaining
        )
    }
}

/**
 * Data class representing temporary access status
 */
data class TemporaryAccessStatus(
    val hasAccess: Boolean,
    val expiresAt: Instant?,
    val timeRemaining: kotlinx.datetime.DateTimePeriod?
)