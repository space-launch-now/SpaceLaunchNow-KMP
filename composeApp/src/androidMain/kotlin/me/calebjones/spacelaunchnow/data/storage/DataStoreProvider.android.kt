package me.calebjones.spacelaunchnow.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toPath


actual fun createDataStore(name: String): DataStore<Preferences> {
    // Since we can't access context directly here, we need to use a different approach
    // This will be resolved through DI
    throw IllegalStateException("Use createDataStore(context) for Android")
}

actual fun createDebugDataStore(): DataStore<Preferences> {
    // Since we can't access context directly here, we need to use a different approach
    // This will be resolved through DI
    throw IllegalStateException("Use createDebugDataStore(context) for Android")
}

actual fun createNotificationHistoryDataStore(): DataStore<Preferences> {
    // Since we can't access context directly here, we need to use a different approach
    // This will be resolved through DI
    throw IllegalStateException("Use createNotificationHistoryDataStore(context) for Android")
}

fun createDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("sln_notification_settings.preferences_pb").absolutePath.toPath()
    }
}

fun createDebugDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("sln_debug_settings.preferences_pb").absolutePath.toPath()
    }
}

fun createNotificationHistoryDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("sln_notification_history.preferences_pb").absolutePath.toPath()
    }
}

fun createAppSettingsDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("sln_app_settings.preferences_pb").absolutePath.toPath()
    }
}
