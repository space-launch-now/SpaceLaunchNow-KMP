package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.ui.schedule.ScheduleFilterState
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption

class AppPreferences(private val dataStore: DataStore<Preferences>) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val THEME_OPTION = stringPreferencesKey("theme_option")
        private val USE_UTC = booleanPreferencesKey("use_utc")
        private val HIDE_TBD_LAUNCHES = booleanPreferencesKey("hide_tbd_launches")

        // Premium theme customization
        private val CUSTOM_PRIMARY_COLOR = longPreferencesKey("custom_primary_color")
        private val CUSTOM_SECONDARY_COLOR = longPreferencesKey("custom_secondary_color")
        private val PALETTE_STYLE = stringPreferencesKey("palette_style")

        // Debug menu unlock (for release builds)
        private val DEBUG_MENU_UNLOCKED = booleanPreferencesKey("debug_menu_unlocked")

        // Debug: Short cache TTL for testing cache expiration
        private val DEBUG_SHORT_CACHE_TTL = booleanPreferencesKey("debug_short_cache_ttl")

        // Beta warning dialog shown flag
        private val BETA_WARNING_SHOWN = booleanPreferencesKey("beta_warning_shown")

        // Schedule filter state
        private val SCHEDULE_FILTER_STATE = stringPreferencesKey("schedule_filter_state")

        // App rating preferences
        private val APP_LAUNCH_COUNT = longPreferencesKey("app_launch_count")
        private val LAST_RATING_PROMPT_DATE = longPreferencesKey("last_rating_prompt_date")
        private val RATING_DIALOG_SHOWN_COUNT = longPreferencesKey("rating_dialog_shown_count")
        private val USER_HAS_RATED = booleanPreferencesKey("user_has_rated")
        private val USER_GAVE_FEEDBACK = booleanPreferencesKey("user_gave_feedback")
    }

    val themeFlow: Flow<ThemeOption> = dataStore.data.map { preferences ->
        val themeString = preferences[THEME_OPTION] ?: ThemeOption.System.name
        ThemeOption.entries.firstOrNull { it.name == themeString } ?: ThemeOption.System
    }

    val useUtcFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USE_UTC] ?: false
    }

    val hideTbdLaunchesFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[HIDE_TBD_LAUNCHES] ?: false
    }

    val customPrimaryColorFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[CUSTOM_PRIMARY_COLOR]
    }

    val customSecondaryColorFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[CUSTOM_SECONDARY_COLOR]
    }

    val paletteStyleFlow: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PALETTE_STYLE]
    }

    suspend fun updateTheme(theme: ThemeOption) {
        dataStore.edit { preferences ->
            preferences[THEME_OPTION] = theme.name
        }
    }

    suspend fun updateUseUtc(useUtc: Boolean) {
        dataStore.edit { preferences ->
            preferences[USE_UTC] = useUtc
        }
    }

    suspend fun updateHideTbdLaunches(hideTbd: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIDE_TBD_LAUNCHES] = hideTbd
        }
    }

    suspend fun getTheme(): ThemeOption {
        val themeString = dataStore.data.map { it[THEME_OPTION] }.first() ?: ThemeOption.System.name
        return ThemeOption.entries.firstOrNull { it.name == themeString } ?: ThemeOption.System
    }

    suspend fun getUseUtc(): Boolean {
        return dataStore.data.map { it[USE_UTC] }.first() ?: false
    }

    suspend fun getHideTbdLaunches(): Boolean {
        return dataStore.data.map { it[HIDE_TBD_LAUNCHES] }.first() ?: false
    }

    // Debug menu unlock methods
    val debugMenuUnlockedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DEBUG_MENU_UNLOCKED] ?: false
    }

    suspend fun setDebugMenuUnlocked(unlocked: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEBUG_MENU_UNLOCKED] = unlocked
        }
    }

    suspend fun isDebugMenuUnlocked(): Boolean {
        return dataStore.data.map { it[DEBUG_MENU_UNLOCKED] }.first() ?: false
    }

    // Debug short cache TTL methods
    val debugShortCacheTtlFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[DEBUG_SHORT_CACHE_TTL] ?: false
    }

    suspend fun setDebugShortCacheTtl(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[DEBUG_SHORT_CACHE_TTL] = enabled
        }
    }

    suspend fun isDebugShortCacheTtlEnabled(): Boolean {
        return dataStore.data.map { it[DEBUG_SHORT_CACHE_TTL] }.first() ?: false
    }

    // Beta warning dialog methods
    val betaWarningShownFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[BETA_WARNING_SHOWN] ?: false
    }

    suspend fun setBetaWarningShown(shown: Boolean) {
        dataStore.edit { preferences ->
            preferences[BETA_WARNING_SHOWN] = shown
        }
    }

    suspend fun isBetaWarningShown(): Boolean {
        return dataStore.data.map { it[BETA_WARNING_SHOWN] }.first() ?: false
    }

    // Schedule filter state methods
    val scheduleFilterStateFlow: Flow<ScheduleFilterState> = dataStore.data.map { preferences ->
        val jsonString = preferences[SCHEDULE_FILTER_STATE]
        if (jsonString != null) {
            try {
                json.decodeFromString<ScheduleFilterState>(jsonString)
            } catch (e: Exception) {
                ScheduleFilterState() // Return default if deserialization fails
            }
        } else {
            ScheduleFilterState() // Return default if no saved state
        }
    }

    suspend fun updateScheduleFilterState(state: ScheduleFilterState) {
        dataStore.edit { preferences ->
            val jsonString = json.encodeToString(state)
            preferences[SCHEDULE_FILTER_STATE] = jsonString
        }
    }

    suspend fun getScheduleFilterState(): ScheduleFilterState {
        val jsonString = dataStore.data.map { it[SCHEDULE_FILTER_STATE] }.first()
        return if (jsonString != null) {
            try {
                json.decodeFromString<ScheduleFilterState>(jsonString)
            } catch (e: Exception) {
                ScheduleFilterState()
            }
        } else {
            ScheduleFilterState()
        }
    }

    // App rating methods
    val appLaunchCountFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[APP_LAUNCH_COUNT] ?: 0L
    }

    suspend fun incrementLaunchCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[APP_LAUNCH_COUNT] ?: 0L
            preferences[APP_LAUNCH_COUNT] = currentCount + 1
        }
    }

    suspend fun getAppLaunchCount(): Long {
        return dataStore.data.map { it[APP_LAUNCH_COUNT] }.first() ?: 0L
    }

    val lastRatingPromptDateFlow: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[LAST_RATING_PROMPT_DATE]
    }

    suspend fun updateLastRatingPromptDate(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_RATING_PROMPT_DATE] = timestamp
        }
    }

    suspend fun getLastRatingPromptDate(): Long? {
        return dataStore.data.map { it[LAST_RATING_PROMPT_DATE] }.first()
    }

    val ratingDialogShownCountFlow: Flow<Long> = dataStore.data.map { preferences ->
        preferences[RATING_DIALOG_SHOWN_COUNT] ?: 0L
    }

    suspend fun incrementRatingDialogShownCount() {
        dataStore.edit { preferences ->
            val currentCount = preferences[RATING_DIALOG_SHOWN_COUNT] ?: 0L
            preferences[RATING_DIALOG_SHOWN_COUNT] = currentCount + 1
        }
    }

    suspend fun getRatingDialogShownCount(): Long {
        return dataStore.data.map { it[RATING_DIALOG_SHOWN_COUNT] }.first() ?: 0L
    }

    val userHasRatedFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USER_HAS_RATED] ?: false
    }

    suspend fun setUserHasRated(hasRated: Boolean) {
        dataStore.edit { preferences ->
            preferences[USER_HAS_RATED] = hasRated
        }
    }

    suspend fun hasUserRated(): Boolean {
        return dataStore.data.map { it[USER_HAS_RATED] }.first() ?: false
    }

    val userGaveFeedbackFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[USER_GAVE_FEEDBACK] ?: false
    }

    suspend fun setUserGaveFeedback(gaveFeedback: Boolean) {
        dataStore.edit { preferences ->
            preferences[USER_GAVE_FEEDBACK] = gaveFeedback
        }
    }

    suspend fun hasUserGivenFeedback(): Boolean {
        return dataStore.data.map { it[USER_GAVE_FEEDBACK] }.first() ?: false
    }
}