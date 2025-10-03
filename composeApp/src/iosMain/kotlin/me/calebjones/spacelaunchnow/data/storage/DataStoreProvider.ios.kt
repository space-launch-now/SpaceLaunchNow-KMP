package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
private fun getDocumentsDirectory(): String {
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    return documentDirectory?.path ?: throw IllegalStateException("Could not find documents directory")
}

actual fun createDataStore(name: String): DataStore<Preferences> {
    val documentsPath = getDocumentsDirectory()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$documentsPath/$name.preferences_pb".toPath() }
    )
}

actual fun createDebugDataStore(): DataStore<Preferences> {
    val documentsPath = getDocumentsDirectory()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$documentsPath/debug_settings.preferences_pb".toPath() }
    )
}