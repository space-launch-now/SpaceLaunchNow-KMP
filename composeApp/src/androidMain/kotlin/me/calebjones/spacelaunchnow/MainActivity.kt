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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import chaintech.videoplayer.util.PlaybackPreference
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.notifications.AndroidNotificationPermissionHandler
import me.calebjones.spacelaunchnow.data.notifications.NotificationPermissionManager
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.util.NotificationSettingsHelper
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val log = logger()
    private val appPreferences: AppPreferences by inject()
    private val billingClient: BillingClient by inject()
    private val billingManager: BillingManager by inject()
    private lateinit var notificationPermissionHandler: AndroidNotificationPermissionHandler

    // Use mutable state for notification launch ID to trigger recomposition
    private var notificationLaunchIdState by mutableStateOf<String?>(null)

    // Use mutable state for notification event ID to trigger recomposition
    private var notificationEventIdState by mutableStateOf<Int?>(null)

    // Use mutable state for navigation destination (e.g., from widget)
    private var navigationDestinationState by mutableStateOf<String?>(null)

    // Rate limiting for purchase state refresh on resume
    private var lastPurchaseRefreshTime = 0L
    private val purchaseRefreshCooldownMs = 30_000L // 30 seconds cooldown

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        setTheme(android.R.style.Theme_Material_NoActionBar)
        super.onCreate(savedInstanceState)

        log.v { "onCreate called - savedInstanceState: ${if (savedInstanceState == null) "null (fresh start)" else "not null (restoring)"}" }

        // Initialize notification settings helper for Android
        NotificationSettingsHelper.initialize(this)

        notificationPermissionHandler = AndroidNotificationPermissionHandler(this)
        NotificationPermissionManager.setCurrentActivity(this)

        // Fresh install logic is now handled in App.kt during app startup

        // Notification permission is now handled by the LiveOnboarding flow (page 4).
        // No auto-request here — the onboarding carousel's "Enable Notifications" button
        // triggers the system permission dialog at the appropriate time.
        log.d { "Notification permission check skipped - handled by LiveOnboarding flow" }

        // Check if launched from notification (with launch_id or event_id)
        val intentLaunchId = intent?.getStringExtra("launch_id")
        val intentEventId = intent?.getIntExtra("event_id", -1)?.takeIf { it != -1 }
        if (intentLaunchId != null) {
            log.i { "App launched from notification - launch_id: $intentLaunchId" }
            notificationLaunchIdState = intentLaunchId
        } else if (intentEventId != null) {
            log.i { "App launched from event notification - event_id: $intentEventId" }
            notificationEventIdState = intentEventId
        }

        // Check if launched with navigation destination (from widget)
        val navigateTo = intent?.getStringExtra("navigate_to")
        if (navigateTo != null) {
            log.i { "App launched with navigation destination: $navigateTo" }
            navigationDestinationState = navigateTo
        }

        // Handle deep links from Wear OS companion (spacelaunchnow://...)
        handleDeepLinkIntent(intent)

        PlaybackPreference.initialize(this)

        setContent {
            val themeOption by appPreferences.themeFlow.collectAsStateWithLifecycle(
                initialValue = ThemeOption.System,
                lifecycle = this.lifecycle
            )
            val useUtc by appPreferences.useUtcFlow.collectAsStateWithLifecycle(
                initialValue = false,
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
                contextFactory = me.calebjones.spacelaunchnow.platform.ContextFactory(this),
                themeOption = themeOption,
                useUtc = useUtc,
                notificationLaunchId = notificationLaunchIdState,
                onNotificationLaunchIdConsumed = { notificationLaunchIdState = null },
                notificationEventId = notificationEventIdState,
                onNotificationEventIdConsumed = { notificationEventIdState = null },
                navigationDestination = navigationDestinationState,
                onNavigationDestinationConsumed = { navigationDestinationState = null }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Set activity for purchase flows using reflection to avoid KMP import issues
        try {
            val setActivityMethod =
                billingClient.javaClass.getMethod("setActivity", android.app.Activity::class.java)
            setActivityMethod.invoke(billingClient, this)
            log.d { "Set billing client activity" }
        } catch (e: Exception) {
            // Not an Android billing client or method doesn't exist - that's okay
            log.w { "Billing client doesn't support setActivity - ${e.message}" }
        }

        // Refresh purchase state to catch any changes made while app was in background
        // (e.g., trial conversions, subscription renewals, or cancellations)
        // Rate limited to avoid excessive API calls when user frequently switches apps
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPurchaseRefreshTime > purchaseRefreshCooldownMs) {
            lastPurchaseRefreshTime = currentTime
            lifecycleScope.launch {
                try {
                    log.d { "Syncing purchases with store on resume..." }

                    // CRITICAL: Sync with store FIRST to get latest subscription status from Google Play
                    // This ensures RevenueCat has the latest data before we query customer info
                    // Fixes issue where trial-to-paid conversions aren't reflected immediately
                    billingManager.syncPurchases()
                    log.d { "Store sync complete" }

                    // THEN refresh purchase state (will now have fresh data from the sync above)
                    log.d { "Refreshing purchase state after sync..." }
                    val refreshed = billingManager.refreshPurchaseState()
                    if (refreshed) {
                        log.i { "Purchase state refreshed successfully" }
                    } else {
                        log.w { "Failed to refresh purchase state" }
                    }
                } catch (e: Exception) {
                    log.e(e) { "Error refreshing purchase state" }
                }
            }
        } else {
            log.d { "Skipping purchase refresh (cooldown active)" }
        }
    }

    override fun onPause() {
        super.onPause()
        // Clear activity reference using reflection
        try {
            val setActivityMethod =
                billingClient.javaClass.getMethod("setActivity", android.app.Activity::class.java)
            setActivityMethod.invoke(billingClient, null)
            log.d { "Cleared billing client activity" }
        } catch (e: Exception) {
            log.w { "Could not clear billing client activity - ${e.message}" }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clear activity reference using reflection
        try {
            val setActivityMethod =
                billingClient.javaClass.getMethod("setActivity", android.app.Activity::class.java)
            setActivityMethod.invoke(billingClient, null)
            log.d { "Cleared billing client activity on destroy" }
        } catch (e: Exception) {
            log.w { "Could not clear billing client activity on destroy - ${e.message}" }
        }
        NotificationPermissionManager.clearCurrentActivity()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        // Check if new intent has launch_id from notification
        val newLaunchId = intent?.getStringExtra("launch_id")
        val newEventId = intent?.getIntExtra("event_id", -1)?.takeIf { it != -1 }
        if (newLaunchId != null) {
            log.i { "New notification intent received - launch_id: $newLaunchId" }
            notificationLaunchIdState = newLaunchId
        } else if (newEventId != null) {
            log.i { "New event notification intent received - event_id: $newEventId" }
            notificationEventIdState = newEventId
        }

        // Check if new intent has navigation destination (from widget)
        val navigateTo = intent?.getStringExtra("navigate_to")
        if (navigateTo != null) {
            log.i { "New intent received with navigation destination: $navigateTo" }
            navigationDestinationState = navigateTo
        }

        // Handle deep links from Wear OS companion (spacelaunchnow://...)
        handleDeepLinkIntent(intent)
    }

    private fun handleDeepLinkIntent(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme != "spacelaunchnow") return
        when (uri.host) {
            "launch" -> {
                val launchId = uri.lastPathSegment
                if (!launchId.isNullOrEmpty()) {
                    log.i { "Deep link from Wear - launch_id: $launchId" }
                    notificationLaunchIdState = launchId
                }
            }
            "premium" -> {
                log.i { "Deep link from Wear - navigating to premium/subscription" }
                navigationDestinationState = "subscription"
            }
            else -> log.w { "Unknown deep link host: ${uri.host}" }
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
                log.i { "Notification permission granted" }
            } else {
                log.i { "Notification permission denied" }
            }
        }
    }

    private fun hasAskedNotificationPermission(context: Context): Boolean {
        val prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
        val result = prefs.getBoolean("asked_notification_permission", false)
        log.v { "hasAskedNotificationPermission() = $result" }
        return result
    }

    private fun markNotificationPermissionAsAsked(context: Context) {
        log.d { "markNotificationPermissionAsAsked() called - setting flag to true" }
        context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            .edit { putBoolean("hasAskedForNotificationPermission", true) }
    }

    private fun resetNotificationPermissionAsked(context: Context) {
        context.getSharedPreferences("onboarding_prefs", MODE_PRIVATE)
            .edit { putBoolean("hasAskedForNotificationPermission", false) }
    }
}