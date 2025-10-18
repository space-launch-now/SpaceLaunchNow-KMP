package me.calebjones.spacelaunchnow.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log

/**
 * Android implementation for opening system notification settings
 */
actual object NotificationSettingsHelper {
    
    private lateinit var context: Context
    
    /**
     * Initialize with Android context
     */
    fun initialize(context: Context) {
        this.context = context.applicationContext
    }
    
    /**
     * Opens the Android system notification settings for this app
     * Will attempt to open the most specific settings page available
     */
    actual fun openSystemNotificationSettings() {
        if (!::context.isInitialized) {
            Log.e("NotificationSettings", "Context not initialized. Call initialize() first.")
            return
        }
        
        try {
            val intent = when {
                // Android O+ (API 26+) - Open app notification channels settings
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                // Android 5.0-7.1 (API 21-25) - Open app info page
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP -> {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
                // Fallback for older versions - Open general app settings
                else -> {
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                }
            }
            
            // Verify that the intent can be resolved
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Log.d("NotificationSettings", "Opened system notification settings")
            } else {
                // Fallback to general settings if specific intent fails
                openGeneralSettings()
            }
            
        } catch (e: Exception) {
            Log.e("NotificationSettings", "Failed to open notification settings", e)
            // Try fallback
            openGeneralSettings()
        }
    }
    
    /**
     * Fallback method to open general device settings
     */
    private fun openGeneralSettings() {
        try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d("NotificationSettings", "Opened general settings as fallback")
        } catch (e: Exception) {
            Log.e("NotificationSettings", "Failed to open even general settings", e)
        }
    }
}