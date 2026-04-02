package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages dismissed pinned content IDs.
 * When a user dismisses a featured/pinned item, its ID is stored here
 * so it won't be shown again until the remote config changes to a different item.
 */
class PinnedContentPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val DISMISSED_PINNED_IDS = stringSetPreferencesKey("dismissed_pinned_ids")
    }

    /**
     * Flow of currently dismissed pinned content IDs.
     */
    val dismissedIdsFlow: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[DISMISSED_PINNED_IDS] ?: emptySet()
    }

    /**
     * Dismiss a pinned content item by its ID.
     * The user won't see this pinned content again until remote config changes.
     */
    suspend fun dismissPinnedContent(id: String) {
        dataStore.edit { preferences ->
            val current = preferences[DISMISSED_PINNED_IDS] ?: emptySet()
            preferences[DISMISSED_PINNED_IDS] = current + id
        }
    }

    /**
     * Check if a pinned content ID has been dismissed.
     */
    suspend fun isDismissed(id: String): Boolean {
        var dismissed = false
        dataStore.data.collect { preferences ->
            dismissed = preferences[DISMISSED_PINNED_IDS]?.contains(id) == true
        }
        return dismissed
    }

    /**
     * Clear a specific dismissed ID (e.g., when remote config changes).
     * This allows the same content to be shown again if re-pinned later.
     */
    suspend fun clearDismissedId(id: String) {
        dataStore.edit { preferences ->
            val current = preferences[DISMISSED_PINNED_IDS] ?: emptySet()
            preferences[DISMISSED_PINNED_IDS] = current - id
        }
    }

    /**
     * Clear all dismissed IDs.
     * Useful for resetting state or debugging.
     */
    suspend fun clearAllDismissed() {
        dataStore.edit { preferences ->
            preferences.remove(DISMISSED_PINNED_IDS)
        }
    }
}
