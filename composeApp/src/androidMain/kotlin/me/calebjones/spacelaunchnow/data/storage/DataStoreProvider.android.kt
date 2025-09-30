package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
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

fun createDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("notification_settings.preferences_pb").absolutePath.toPath()
    }
}

fun createDebugDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("debug_settings.preferences_pb").absolutePath.toPath()
    }
}

fun createAppSettingsDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath {
        context.filesDir.resolve("app_settings.preferences_pb").absolutePath.toPath()
    }
}