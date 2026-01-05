package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.model.NotificationHistoryItem
import me.calebjones.spacelaunchnow.data.model.NotificationStats
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugSettings
import me.calebjones.spacelaunchnow.data.storage.NotificationHistoryStorage
import me.calebjones.spacelaunchnow.util.BuildConfig
import kotlin.random.Random

expect fun resetNotificationPermissionAskedFlag()

/**
 * ViewModel for debug settings
 * 
 * Phase 7: Updated to use platform-agnostic BillingManager instead of RevenueCatManager
 */
@OptIn(kotlin.time.ExperimentalTime::class)
class DebugSettingsViewModel(
    private val debugPreferences: DebugPreferences? = null,
    private val billingManager: BillingManager? = null,
    private val launchRepository: LaunchRepository? = null,
    private val notificationRepository: NotificationRepository? = null,
    private val pushMessaging: PushMessaging? = null,
    private val notificationHistoryStorage: NotificationHistoryStorage? = null
) : ViewModel() {

    private val _debugSettings = MutableStateFlow(
        DebugSettings(
            useCustomApiUrl = false,
            customApiBaseUrl = DebugPreferences.PROD_API_URL,
            useDebugTopics = false
        )
    )
    val debugSettings: StateFlow<DebugSettings> = _debugSettings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _detailedMessage = MutableStateFlow<String?>(null)
    val detailedMessage: StateFlow<String?> = _detailedMessage.asStateFlow()

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    // Notification History
    private val _notificationHistory = MutableStateFlow<List<NotificationHistoryItem>>(emptyList())
    val notificationHistory: StateFlow<List<NotificationHistoryItem>> = _notificationHistory.asStateFlow()

    private val _notificationStats = MutableStateFlow<NotificationStats?>(null)
    val notificationStats: StateFlow<NotificationStats?> = _notificationStats.asStateFlow()

    val isDebugMode: Boolean = BuildConfig.IS_DEBUG

    init {
        if (debugPreferences != null) {
            viewModelScope.launch {
                debugPreferences.debugSettingsFlow.collect { settings ->
                    _debugSettings.value = settings
                }
            }
        }

        // Collect notification history
        if (notificationHistoryStorage != null) {
            viewModelScope.launch {
                notificationHistoryStorage.historyFlow.collect { history ->
                    _notificationHistory.value = history
                }
            }
        }
    }

    fun setUseCustomApiUrl(use: Boolean) {
        if (debugPreferences == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                debugPreferences.setUseCustomApiUrl(use)
                _statusMessage.value =
                    if (use) "Custom API URL enabled" else "Using default API URL"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update API URL setting: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setCustomApiUrl(url: String) {
        if (debugPreferences == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                debugPreferences.setCustomApiBaseUrl(url)
                _statusMessage.value = "API URL updated to: $url"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update API URL: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setUseDebugTopics(useDebug: Boolean) {
        if (debugPreferences == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                debugPreferences.setUseDebugTopics(useDebug)
                val topicType = if (useDebug) "debug_v3" else "prod_v3"
                _statusMessage.value = "Switched to $topicType topics"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to update topic setting: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun switchToProdUrl() {
        if (debugPreferences == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                debugPreferences.switchToProdUrl()
                _statusMessage.value = "Switched to production API URL"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to switch to prod URL: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun switchToDevUrl() {
        if (debugPreferences == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                debugPreferences.switchToDevUrl()
                _statusMessage.value = "Switched to development API URL"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to switch to dev URL: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun switchToLocalUrl() {
        if (debugPreferences == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                debugPreferences.switchToLocalUrl()
                _statusMessage.value = "Switched to local API URL"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to switch to local URL: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
        _detailedMessage.value = null
    }

    fun resetNotificationPermissionFlag() {
        viewModelScope.launch {
            try {
                resetNotificationPermissionAskedFlag()
                _statusMessage.value =
                    "Notification permission flag reset - will be asked on next app launch"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to reset permission flag: ${e.message}"
            }
        }
    }

    fun fetchFcmToken() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (pushMessaging == null) {
                    _fcmToken.value = "PushMessaging not available"
                    return@launch
                }

                pushMessaging.getToken().fold(
                    onSuccess = { token ->
                        _fcmToken.value = token
                        _statusMessage.value = "FCM token retrieved"
                    },
                    onFailure = { error ->
                        _fcmToken.value = "Error: ${error.message}"
                        _statusMessage.value = "Failed to get FCM token: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _fcmToken.value = "Exception: ${e.message}"
                _statusMessage.value = "Error fetching FCM token: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Billing Debug Functions
    fun checkBillingInitialization() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (billingManager == null) {
                    _statusMessage.value = "❌ BillingManager not available (not injected)"
                    return@launch
                }

                val isInitialized = billingManager.isInitialized.value
                val purchaseState = billingManager.purchaseState.value

                val message = buildString {
                    appendLine("✅ Billing Status:")
                    appendLine("• Initialized: $isInitialized")
                    appendLine("• Is Subscribed: ${purchaseState.isSubscribed}")
                    appendLine("• Subscription Type: ${purchaseState.subscriptionType?.name ?: "FREE"}")
                    appendLine("• Active Entitlements: ${billingManager.getActiveEntitlements().joinToString()}")
                    appendLine("• User ID: ${purchaseState.userId ?: "Anonymous"}")
                }
                _detailedMessage.value = message
                _statusMessage.value = "Billing Status Check"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error checking initialization: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun queryBillingProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (billingManager == null) {
                    _statusMessage.value = "❌ BillingManager not available"
                    return@launch
                }

                billingManager.getAvailableProducts().fold(
                    onSuccess = { products ->
                        val message = buildString {
                            appendLine("📦 Products:")
                            if (products.isEmpty()) {
                                appendLine("❌ No products available")
                            } else {
                                appendLine("✅ Available Products (${products.size}):")
                                products.forEach { product ->
                                    appendLine("\n━━━━━━━━━━━━━━━━━━━━")
                                    appendLine("Product ID: ${product.productId}")
                                    appendLine("  Base Plan: ${product.basePlanId ?: "N/A"}")
                                    appendLine("  Title: ${product.title}")
                                    appendLine("  Description: ${product.description}")
                                    appendLine("  Price: ${product.formattedPrice}")
                                    appendLine("  Currency: ${product.currencyCode}")
                                }
                            }
                        }
                        _detailedMessage.value = message
                        _statusMessage.value = "Products Query Result"
                    },
                    onFailure = { error ->
                        _statusMessage.value = "❌ Error querying products: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error querying products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkBillingEntitlements() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (billingManager == null) {
                    _statusMessage.value = "❌ BillingManager not available"
                    return@launch
                }

                // Refresh purchase state
                billingManager.refreshPurchaseState()
                val purchaseState = billingManager.purchaseState.value
                val activeEntitlements = billingManager.getActiveEntitlements()

                val message = buildString {
                    appendLine("🔐 Entitlements:")
                    appendLine("✅ Active Entitlements (${activeEntitlements.size}):")
                    if (activeEntitlements.isEmpty()) {
                        appendLine("  • None (Free user)")
                    } else {
                        activeEntitlements.forEach { entitlement ->
                            appendLine("  • $entitlement")
                        }
                    }

                    appendLine("\n💳 Purchase Info:")
                    appendLine("  • Is Subscribed: ${purchaseState.isSubscribed}")
                    appendLine("  • Subscription Type: ${purchaseState.subscriptionType.name}")
                    appendLine("  • User ID: ${purchaseState.userId ?: "Anonymous"}")
                    appendLine("  • Last Refreshed: ${purchaseState.lastRefreshed}")
                }
                _detailedMessage.value = message
                _statusMessage.value = "Entitlements Check Result"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error checking entitlements: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun testBillingRestore() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (billingManager == null) {
                    _statusMessage.value = "❌ BillingManager not available"
                    return@launch
                }

                billingManager.restorePurchases().fold(
                    onSuccess = { purchaseState ->
                        val message = buildString {
                            appendLine("🔄 Restore Purchases:")
                            appendLine("✅ Restore successful")
                            appendLine("Active Entitlements: ${billingManager.getActiveEntitlements().joinToString()}")
                            appendLine("Is Subscribed: ${purchaseState.isSubscribed}")
                        }
                        _detailedMessage.value = message
                        _statusMessage.value = "Restore Purchases Result"
                    },
                    onFailure = { error ->
                        _statusMessage.value = "❌ Error restoring purchases: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error restoring purchases: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun viewBillingProductDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (billingManager == null) {
                    _statusMessage.value = "❌ BillingManager not available"
                    return@launch
                }

                billingManager.getAvailableProducts().fold(
                    onSuccess = { products ->
                        val message = buildString {
                            appendLine("🎁 Product Details:")
                            if (products.isEmpty()) {
                                appendLine("❌ No products loaded")
                                appendLine("\n💡 Try refreshing products first")
                            } else {
                                appendLine("✅ Available Products: ${products.size}")
                                
                                // Group by type
                                val lifetime = products.find { 
                                    it.basePlanId?.contains("lifetime", ignoreCase = true) == true ||
                                    it.productId.contains("lifetime", ignoreCase = true) ||
                                    it.productId.contains("pro", ignoreCase = true)
                                }
                                val annual = products.find { 
                                    it.basePlanId?.contains("annual", ignoreCase = true) == true ||
                                    it.basePlanId?.contains("yearly", ignoreCase = true) == true
                                }
                                val monthly = products.find { 
                                    it.basePlanId?.contains("monthly", ignoreCase = true) == true 
                                }

                                lifetime?.let { product ->
                                    appendLine("\n⭐ Lifetime Product:")
                                    appendLine("  • ${product.productId} - ${product.formattedPrice}")
                                    appendLine("  • ${product.title}")
                                }

                                annual?.let { product ->
                                    appendLine("\n📅 Annual Product:")
                                    appendLine("  • ${product.productId} - ${product.formattedPrice}")
                                    appendLine("  • ${product.title}")
                                }

                                monthly?.let { product ->
                                    appendLine("\n📆 Monthly Product:")
                                    appendLine("  • ${product.productId} - ${product.formattedPrice}")
                                    appendLine("  • ${product.title}")
                                }
                            }
                        }
                        _detailedMessage.value = message
                        _statusMessage.value = "Product Details"
                    },
                    onFailure = { error ->
                        _statusMessage.value = "❌ Error viewing product details: ${error.message}"
                    }
                )
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error viewing product details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Test Notification Functions
    fun triggerTestNotification(
        agencyId: String,
        locationId: String,
        webcast: String,
        notificationType: String,
        launchImage: String
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                // Create NotificationData with configurable fields
                val webcastLiveValue = if (Random.nextBoolean()) webcast else ""
                val notificationData = NotificationData(
                    notificationType = notificationType,
                    launchId = "test-launch-123",
                    launchUuid = "550e8400-e29b-41d4-a716-446655440000",
                    launchName = "Test Launch - Falcon 9 Block 5",
                    launchImage = launchImage,
                    launchNet = "2025-10-15T12:00:00Z",
                    launchLocation = "Cape Canaveral, FL, USA",
                    webcast = webcast,
                    webcastLive = webcastLiveValue,
                    agencyId = agencyId,
                    locationId = locationId
                )

                // Get current notification settings
                val notificationState = notificationRepository?.state?.value

                if (notificationState == null) {
                    _statusMessage.value = "❌ NotificationRepository not available"
                    return@launch
                }

                // Run through the notification filter
                val shouldShow = NotificationFilter.shouldShowNotification(
                    data = notificationData,
                    state = notificationState
                )

                if (shouldShow) {
                    // Notification passed filters - show it
                    showTestNotification(notificationData)
                    _statusMessage.value = "✅ Test notification PASSED filters and was sent"
                } else {
                    // Notification was filtered out
                    _statusMessage.value = "🔇 Test notification BLOCKED by filters"
                }

                val detailedInfo = buildString {
                    appendLine("=== Filter Result: ${if (shouldShow) "ALLOWED ✅" else "BLOCKED 🔇"} ===")
                    appendLine()
                    appendLine("Notification Data Sent:")
                    appendLine("• Type: $notificationType")
                    appendLine("• Agency ID: $agencyId")
                    appendLine("• Location ID: $locationId")
                    appendLine("• Webcast: $webcast")
                    appendLine()
                    appendLine("Pre-filled Data:")
                    appendLine("• Launch ID: ${notificationData.launchId}")
                    appendLine("• Launch UUID: ${notificationData.launchUuid}")
                    appendLine("• Launch Name: ${notificationData.launchName}")
                    appendLine("• Launch Image: ${notificationData.launchImage}")
                    appendLine("• Launch NET: ${notificationData.launchNet}")
                    appendLine("• Launch Location: ${notificationData.launchLocation}")
                    appendLine()
                    appendLine("Current Filter Settings:")
                    appendLine("• Notifications Enabled: ${notificationState.enableNotifications}")
                    appendLine("• Follow All Launches: ${notificationState.followAllLaunches}")
                    appendLine("• Strict Matching: ${notificationState.useStrictMatching}")
                    appendLine(
                        "• Subscribed Agencies: ${notificationState.subscribedAgencies.size} (${
                            notificationState.subscribedAgencies.take(
                                3
                            ).joinToString(", ")
                        }...)"
                    )
                    appendLine(
                        "• Subscribed Locations: ${notificationState.subscribedLocations.size} (${
                            notificationState.subscribedLocations.take(
                                3
                            ).joinToString(", ")
                        }...)"
                    )
                }
                _detailedMessage.value = detailedInfo
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error triggering test notification: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Notification History Functions
    fun loadNotificationHistory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (notificationHistoryStorage == null) {
                    _statusMessage.value = "Notification history not available"
                    return@launch
                }

                val history = notificationHistoryStorage.getHistory()
                val stats = notificationHistoryStorage.getStats()
                _notificationHistory.value = history
                _notificationStats.value = stats
                _statusMessage.value = "Loaded ${history.size} notifications"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to load history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearNotificationHistory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (notificationHistoryStorage == null) {
                    _statusMessage.value = "Notification history not available"
                    return@launch
                }

                notificationHistoryStorage.clearHistory()
                _notificationHistory.value = emptyList()
                _notificationStats.value = null
                _statusMessage.value = "Notification history cleared"
            } catch (e: Exception) {
                _statusMessage.value = "Failed to clear history: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
