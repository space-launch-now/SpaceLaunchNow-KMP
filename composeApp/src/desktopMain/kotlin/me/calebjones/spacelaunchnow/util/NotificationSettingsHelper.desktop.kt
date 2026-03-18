package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.util.logging.logger
import java.awt.Desktop
import java.net.URI

/**
 * Desktop implementation for opening system notification settings
 */
actual object NotificationSettingsHelper {
    private val log = logger()
    
    /**
     * Opens system notification settings on Desktop platforms
     * Attempts to open platform-specific notification preferences
     */
    actual fun openSystemNotificationSettings() {
        try {
            val os = System.getProperty("os.name").lowercase()
            
            when {
                // macOS - Open System Preferences > Notifications
                os.contains("mac") -> {
                    if (Desktop.isDesktopSupported()) {
                        // Try to open Notifications preferences pane
                        val pb = ProcessBuilder("open", "-b", "com.apple.preference.notifications")
                        pb.start()
                    }
                }
                // Windows - Open Settings > System > Notifications
                os.contains("win") -> {
                    if (Desktop.isDesktopSupported()) {
                        // Open Windows 10/11 notification settings
                        val pb = ProcessBuilder("cmd", "/c", "start", "ms-settings:notifications")
                        pb.start()
                    }
                }
                // Linux - Try to open notification settings (varies by DE)
                os.contains("linux") -> {
                    if (Desktop.isDesktopSupported()) {
                        // Try common notification settings commands
                        val commands = listOf(
                            arrayOf("gnome-control-center", "notifications"), // GNOME
                            arrayOf("systemsettings5", "kcm_notifications"), // KDE
                            arrayOf("unity-control-center", "notifications"), // Unity
                            arrayOf("xfce4-notifyd-config") // XFCE
                        )
                        
                        var opened = false
                        for (command in commands) {
                            try {
                                ProcessBuilder(*command).start()
                                opened = true
                                break
                            } catch (e: Exception) {
                                // Try next command
                            }
                        }
                        
                        if (!opened) {
                            // Fallback: open general system settings
                            try {
                                ProcessBuilder("gnome-control-center").start()
                            } catch (e: Exception) {
                                log.w { "Could not open notification settings on this Linux distribution" }
                            }
                        }
                    }
                }
                else -> {
                    log.w { "Opening system notification settings not supported on this platform: $os" }
                }
            }
        } catch (e: Exception) {
            log.e(e) { "Failed to open system notification settings: ${e.message}" }
        }
    }
}