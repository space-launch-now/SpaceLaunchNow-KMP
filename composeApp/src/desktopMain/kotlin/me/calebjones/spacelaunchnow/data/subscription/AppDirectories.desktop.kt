package me.calebjones.spacelaunchnow.data.subscription

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

/**
 * Desktop (JVM) implementation of AppDirectories
 */
actual object AppDirectories {
    
    actual fun getAppDataDir(): Path {
        // Use user home directory for desktop
        val userHome = System.getProperty("user.home")
        val appDir = Path("$userHome/.spacelaunchnow")
        
        // Ensure directory exists
        if (!SystemFileSystem.exists(appDir)) {
            SystemFileSystem.createDirectories(appDir)
        }
        
        return appDir
    }
}