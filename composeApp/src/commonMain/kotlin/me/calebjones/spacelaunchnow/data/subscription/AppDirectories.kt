package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.io.files.Path

/**
 * Platform-specific application directories for KStore file storage
 */
expect object AppDirectories {
    /**
     * Get the app data directory for storing subscription data
     */
    fun getAppDataDir(): Path
}