package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.model.NotificationData
import me.calebjones.spacelaunchnow.data.model.NotificationFilter
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugSettings
import me.calebjones.spacelaunchnow.util.BuildConfig
import kotlin.random.Random

expect fun resetNotificationPermissionAskedFlag()

@OptIn(kotlin.time.ExperimentalTime::class)
class DebugSettingsViewModel(
    private val debugPreferences: DebugPreferences? = null,
    private val revenueCatManager: RevenueCatManager? = null,
    private val launchRepository: LaunchRepository? = null,
    private val notificationRepository: NotificationRepository? = null
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

    val isDebugMode: Boolean = BuildConfig.IS_DEBUG

    init {
        if (debugPreferences != null) {
            viewModelScope.launch {
                debugPreferences.debugSettingsFlow.collect { settings ->
                    _debugSettings.value = settings
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

    // RevenueCat Debug Functions
    fun checkRevenueCatInitialization() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (revenueCatManager == null) {
                    _statusMessage.value = "❌ RevenueCatManager not available (not injected)"
                    return@launch
                }

                val isInitialized = revenueCatManager.isInitialized.value
                val customerInfo = revenueCatManager.customerInfo.value
                val offering = revenueCatManager.currentOffering.value

                val message = buildString {
                    appendLine("✅ RevenueCat Status:")
                    appendLine("• Initialized: $isInitialized")
                    appendLine("• CustomerInfo: ${if (customerInfo != null) "✅ Loaded" else "❌ Not loaded"}")
                    appendLine("• Current Offering: ${offering?.identifier ?: "❌ Not loaded"}")
                    if (customerInfo != null) {
                        appendLine("• Active Entitlements: ${customerInfo.entitlements.active.keys.joinToString()}")
                    }
                }
                _detailedMessage.value = message
                _statusMessage.value = "RevenueCat Status Check"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error checking initialization: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun queryRevenueCatProducts() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (revenueCatManager == null) {
                    _statusMessage.value = "❌ RevenueCatManager not available"
                    return@launch
                }

                revenueCatManager.refreshOfferings()
                val offering = revenueCatManager.currentOffering.value

                val message = buildString {
                    appendLine("📦 Products/Offerings:")
                    if (offering == null) {
                        appendLine("❌ No offering available")
                    } else {
                        appendLine("✅ Offering: ${offering.identifier}")
                        appendLine("\nAvailable Packages:")
                        offering.availablePackages.forEach { pkg ->
                            appendLine("• ${pkg.identifier}")
                            appendLine("  Product: ${pkg.storeProduct.id}")
                            appendLine("  Price: ${pkg.storeProduct.price.formatted}")
                            appendLine("  Period: ${pkg.storeProduct.period?.unit?.name ?: "Lifetime"}")
                        }
                    }
                }
                _detailedMessage.value = message
                _statusMessage.value = "Products Query Result"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error querying products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkRevenueCatEntitlements() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (revenueCatManager == null) {
                    _statusMessage.value = "❌ RevenueCatManager not available"
                    return@launch
                }

                revenueCatManager.refreshCustomerInfo()
                val customerInfo = revenueCatManager.customerInfo.value

                val message = buildString {
                    appendLine("🔐 Entitlements:")
                    if (customerInfo == null) {
                        appendLine("❌ No customer info available")
                    } else {
                        val activeEntitlements = customerInfo.entitlements.active
                        val allEntitlements = customerInfo.entitlements.all

                        appendLine("✅ Active Entitlements (${activeEntitlements.size}):")
                        if (activeEntitlements.isEmpty()) {
                            appendLine("  • None (Free user)")
                        } else {
                            activeEntitlements.forEach { (id, info) ->
                                appendLine("  • $id")
                                appendLine("    Product: ${info.productIdentifier}")
                                appendLine("    Expires: ${info.expirationDateMillis?.toString() ?: "Never"}")
                            }
                        }

                        appendLine("\n📋 All Entitlements (${allEntitlements.size}):")
                        allEntitlements.forEach { (id, info) ->
                            val status = if (info.isActive) "✅ Active" else "❌ Inactive"
                            appendLine("  • $id - $status")
                        }

                        appendLine("\n💳 Original Purchase Info:")
                        appendLine("  • Original App User ID: ${customerInfo.originalAppUserId}")
                        appendLine("  • First Seen: ${customerInfo.firstSeenMillis.toString()}")
                    }
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

    fun testRevenueCatRestore() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (revenueCatManager == null) {
                    _statusMessage.value = "❌ RevenueCatManager not available"
                    return@launch
                }

                revenueCatManager.restorePurchases()

                val message = buildString {
                    appendLine("🔄 Restore Purchases:")
                    val customerInfo = revenueCatManager.customerInfo.value
                    if (customerInfo != null) {
                        appendLine("✅ Restore successful")
                        appendLine("Active Entitlements: ${customerInfo.entitlements.active.keys.joinToString()}")
                    } else {
                        appendLine("⚠️ Restore completed but no customer info")
                    }
                }
                _detailedMessage.value = message
                _statusMessage.value = "Restore Purchases Result"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error restoring purchases: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun viewRevenueCatOfferingDetails() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (revenueCatManager == null) {
                    _statusMessage.value = "❌ RevenueCatManager not available"
                    return@launch
                }

                val offering = revenueCatManager.currentOffering.value

                val message = buildString {
                    appendLine("🎁 Current Offering Details:")
                    if (offering == null) {
                        appendLine("❌ No offering loaded")
                        appendLine("\n💡 Try refreshing offerings first")
                    } else {
                        appendLine("✅ Offering ID: ${offering.identifier}")
                        appendLine("Description: ${offering.serverDescription}")
                        appendLine("\n📦 Packages (${offering.availablePackages.size}):")

                        offering.availablePackages.forEach { pkg ->
                            appendLine("\n━━━━━━━━━━━━━━━━━━━━")
                            appendLine("Package: ${pkg.identifier}")
                            appendLine("  Type: ${pkg.packageType}")
                            appendLine("\n  Product Info:")
                            appendLine("    • ID: ${pkg.storeProduct.id}")
                            appendLine("    • Title: ${pkg.storeProduct.title}")
                            appendLine("    • Price: ${pkg.storeProduct.price.formatted}")
                            appendLine("    • Period: ${pkg.storeProduct.period?.unit?.name ?: "N/A"} (${pkg.storeProduct.period?.value ?: "N/A"})")
                            appendLine("    • Type: ${pkg.storeProduct.type}")
                        }

                        // Lifetime package
                        offering.lifetime?.let { lifetime ->
                            appendLine("\n⭐ Lifetime Package:")
                            appendLine("  • ${lifetime.storeProduct.id} - ${lifetime.storeProduct.price.formatted}")
                        }

                        // Annual package
                        offering.annual?.let { annual ->
                            appendLine("\n📅 Annual Package:")
                            appendLine("  • ${annual.storeProduct.id} - ${annual.storeProduct.price.formatted}")
                        }

                        // Monthly package
                        offering.monthly?.let { monthly ->
                            appendLine("\n📆 Monthly Package:")
                            appendLine("  • ${monthly.storeProduct.id} - ${monthly.storeProduct.price.formatted}")
                        }
                    }
                }
                _detailedMessage.value = message
                _statusMessage.value = "Offering Details"
            } catch (e: Exception) {
                _statusMessage.value = "❌ Error viewing offering details: ${e.message}"
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
}
