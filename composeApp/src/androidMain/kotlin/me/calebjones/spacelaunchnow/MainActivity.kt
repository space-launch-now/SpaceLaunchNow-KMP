package me.calebjones.spacelaunchnow

import android.content.Context
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import chaintech.videoplayer.util.PlaybackPreference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.initializeBuildConfig
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val appPreferences: AppPreferences by inject()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize BuildConfig to set DEBUG flag
        initializeBuildConfig()

        // Subscribe all notifications on first app launch (fresh install)
        val notificationRepository by inject<NotificationRepository>()
        CoroutineScope(Dispatchers.IO).launch {
            val settings = notificationRepository.getNotificationSettings()
            if (settings.subscribedAgencies.isEmpty() && settings.subscribedLocations.isEmpty()) {
                val agencies = notificationRepository.getAvailableAgencies()
                val locations = notificationRepository.getAvailableLocations()
                agencies.forEach { notificationRepository.subscribeToAgency(it) }
                locations.forEach { notificationRepository.subscribeToLocation(it) }
            }
        }

        // Ask for notification permission only on first launch (Android 13+)
        if (!hasAskedNotificationPermission(this)) {
            val handler = AndroidNotificationPermissionHandler(this)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !handler.hasNotificationPermission()) {
                handler.requestNotificationPermission(this)
            }
            markNotificationPermissionAsAsked(this)
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
            
            SpaceLaunchNowApp()
        }
    }

    private fun hasAskedNotificationPermission(context: Context): Boolean {
        val prefs = context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
        return prefs.getBoolean("hasAskedForNotificationPermission", false)
    }

    private fun markNotificationPermissionAsAsked(context: Context) {
        context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            .edit { putBoolean("hasAskedForNotificationPermission", true) }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    SpaceLaunchNowApp()
}