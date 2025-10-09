package me.calebjones.spacelaunchnow

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.util.PlaybackPreference
import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.notifications.NotificationPermissionManager
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val appPreferences: AppPreferences by inject()
    private lateinit var notificationPermissionHandler: AndroidNotificationPermissionHandler

    // Use mutable state for notification launch ID to trigger recomposition
    private var notificationLaunchIdState by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        setTheme(android.R.style.Theme_Material_NoActionBar)
        super.onCreate(savedInstanceState)

        notificationPermissionHandler = AndroidNotificationPermissionHandler(this)
        NotificationPermissionManager.setCurrentActivity(this)

        // Fresh install logic is now handled in App.kt during app startup

        // Ask for notification permission only on first launch (Android 13+) and if not already granted
        println("=== NOTIFICATION PERMISSION DEBUG ===")
        println("Android SDK version: ${Build.VERSION.SDK_INT}")
        println("Is Android 13+: ${Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU}")
        println("Has notification permission: ${notificationPermissionHandler.hasNotificationPermission()}")
        println("Has asked for permission before: ${hasAskedNotificationPermission(this)}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !notificationPermissionHandler.hasNotificationPermission() &&
            !hasAskedNotificationPermission(this)
        ) {
            println("ALL CONDITIONS MET - Requesting notification permission")
            notificationPermissionHandler.requestNotificationPermission(this)
            // Don't mark as asked yet - wait for the result
        } else {
            println("CONDITIONS NOT MET - Skipping permission request")
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                println("  - Android version is below 13")
            }
            if (notificationPermissionHandler.hasNotificationPermission()) {
                println("  - Already has notification permission")
            }
            if (hasAskedNotificationPermission(this)) {
                println("  - Already asked for permission before")
            }
        }
        println("=== END NOTIFICATION PERMISSION DEBUG ===")

        // Extract launch_id from intent if launched from notification
        val intentLaunchId = intent.getStringExtra("launch_id")
        if (intentLaunchId != null) {
            println("App launched from notification with launch_id: $intentLaunchId")
            notificationLaunchIdState = intentLaunchId
        }

        PlaybackPreference.initialize(this)

        setContent {
            val themeOption by appPreferences.themeFlow.collectAsStateWithLifecycle(
                initialValue = ThemeOption.System,
                lifecycle = this.lifecycle
            )

            // Determine if we should use dark theme for system bars
            val isDarkTheme = when (themeOption) {
                ThemeOption.System -> isSystemInDarkTheme()
                ThemeOption.Light -> false
                ThemeOption.Dark -> true
            }

            // Update edge-to-edge based on theme
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    },
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                    }
                )
            }

            SpaceLaunchNowApp(
                notificationLaunchId = notificationLaunchIdState,
                onNotificationLaunchIdConsumed = { notificationLaunchIdState = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Handle launch_id from new notification intent
        val newLaunchId = intent.getStringExtra("launch_id")
        if (newLaunchId != null) {
            notificationLaunchIdState = newLaunchId
            println("New notification intent received with launch_id: $newLaunchId")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val permissionGranted = notificationPermissionHandler.handlePermissionResult(
            requestCode, permissions, grantResults
        )

        if (requestCode == AndroidNotificationPermissionHandler.NOTIFICATION_PERMISSION_REQUEST_CODE) {
            // Always mark as asked after we get the result, regardless of grant/deny
            markNotificationPermissionAsAsked(this)

            // Notify the permission manager of the result (for settings-triggered requests)
            NotificationPermissionManager.onPermissionResult(permissionGranted)

            if (permissionGranted) {
                println("Notification permission granted")
            } else {
                println("Notification permission denied")
            }
        }
    }

    private fun hasAskedNotificationPermission(context: Context): Boolean {
        val prefs = context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
        val result = prefs.getBoolean("hasAskedForNotificationPermission", false)
        println("hasAskedNotificationPermission() = $result")
        return result
    }

    private fun markNotificationPermissionAsAsked(context: Context) {
        println("markNotificationPermissionAsAsked() called - setting flag to true")
        context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            .edit { putBoolean("hasAskedForNotificationPermission", true) }
    }

    private fun resetNotificationPermissionAsked(context: Context) {
        context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            .edit { putBoolean("hasAskedForNotificationPermission", false) }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationPermissionManager.clearCurrentActivity()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    SpaceLaunchNowApp()
}