package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createDataStore(name: String): DataStore<Preferences>

// Debug-specific DataStore for development settings
expect fun createDebugDataStore(): DataStore<Preferences>

// Notification history DataStore for debugging received notifications
expect fun createNotificationHistoryDataStore(): DataStore<Preferences>