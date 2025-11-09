package me.calebjones.spacelaunchnow.data.subscription

import android.content.Context
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Android implementation of AppDirectories
 */
actual object AppDirectories {
    private var filesDir: Path? = null
    
    /**
     * Initialize with Android context
     * Should be called from Application.onCreate()
     */
    fun initialize(context: Context) {
        val dir = Path(context.filesDir.absolutePath)
        // Ensure directory exists
        if (!SystemFileSystem.exists(dir)) {
            SystemFileSystem.createDirectories(dir)
        }
        filesDir = dir
    }
    
    actual fun getAppDataDir(): Path {
        return filesDir ?: throw IllegalStateException(
            "AppDirectories not initialized. Call AppDirectories.initialize(context) from Application.onCreate()"
        )
    }
}