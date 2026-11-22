package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Remembers that the user closed the upgrade row on Home. The close is permanent:
 * the row never comes back for this install.
 */
class HomeUpgradeRowPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        private val DISMISSED = booleanPreferencesKey("home_upgrade_row_dismissed")
    }

    val dismissedFlow: Flow<Boolean> = dataStore.data.map { it[DISMISSED] ?: false }

    suspend fun dismiss() {
        dataStore.edit { it[DISMISSED] = true }
    }
}
