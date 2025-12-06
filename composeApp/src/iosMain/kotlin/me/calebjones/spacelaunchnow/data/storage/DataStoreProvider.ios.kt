package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.calebjones.spacelaunchnow.logger
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private val log by lazy { logger() }

@OptIn(ExperimentalForeignApi::class)
private fun getAppGroupDirectory(): String {
    // Use App Group container for shared storage between main app and widget
    val appGroupId = "group.me.spacelaunchnow.spacelaunchnow"
    val fileManager = NSFileManager.defaultManager
    val containerURL = fileManager.containerURLForSecurityApplicationGroupIdentifier(appGroupId)
    
    return if (containerURL != null) {
        log.i { "📱 Using App Group container: ${containerURL.path}" }
        containerURL.path!!
    } else {
        // Fallback to Documents directory if App Group is not available
        log.w { "⚠️ App Group not available, falling back to Documents directory" }
        getDocumentsDirectoryFallback()
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun getDocumentsDirectoryFallback(): String {
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
    val containerPath = getAppGroupDirectory()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$containerPath/$name.preferences_pb".toPath() }
    )
}

actual fun createDebugDataStore(): DataStore<Preferences> {
    val containerPath = getAppGroupDirectory()
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$containerPath/debug_settings.preferences_pb".toPath() }
    )
}