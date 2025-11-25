package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences

data class SettingsUiState(
    val notificationSettings: NotificationState = NotificationState.DEFAULT,
    val availableAgencies: List<NotificationAgency> = emptyList(),
    val availableLocations: List<NotificationLocation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val theme: ThemeOption = ThemeOption.System,
    val useUtc: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val hideTbdLaunches: Boolean = false,
    val hasNotificationCustomization: Boolean = false,
    val hasCalendarSync: Boolean = false,
    val snackbarMessage: String? = null
)

enum class ThemeOption(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

class AppSettingsViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val themeFlow = appPreferences.themeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ThemeOption.System
    )

    val useUtcFlow = appPreferences.useUtcFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val hideTbdLaunchesFlow = appPreferences.hideTbdLaunchesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    suspend fun updateHideTbdLaunches(hide: Boolean) {
        appPreferences.updateHideTbdLaunches(hide)
    }

    suspend fun updateTheme(theme: ThemeOption) {
        appPreferences.updateTheme(theme)
    }

    suspend fun updateUseUtc(useUtc: Boolean) {
        appPreferences.updateUseUtc(useUtc)
    }
}

class SettingsViewModel(
    private val notificationRepository: NotificationRepository,
    private val appSettingsViewModel: AppSettingsViewModel,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    // Separate state flows for available options
    private val _availableAgencies = MutableStateFlow<List<NotificationAgency>>(emptyList())
    private val _availableLocations = MutableStateFlow<List<NotificationLocation>>(emptyList())

    // Premium feature state
    private val _hasNotificationCustomization = MutableStateFlow(false)
    val hasNotificationCustomization: StateFlow<Boolean> =
        _hasNotificationCustomization.asStateFlow()
    
    private val _hasCalendarSync = MutableStateFlow(false)
    val hasCalendarSync: StateFlow<Boolean> = _hasCalendarSync.asStateFlow()
    
    // Snackbar message state
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Reactive state flow that updates when agencies/locations are loaded
    val uiState: StateFlow<SettingsUiState> = combine(
        notificationRepository.state,
        _availableAgencies,
        _availableLocations,
        appSettingsViewModel.themeFlow,
        appSettingsViewModel.useUtcFlow,
        appSettingsViewModel.hideTbdLaunchesFlow,
        _hasNotificationCustomization,
        _hasCalendarSync,
        _snackbarMessage
    ) { flows ->
        val notificationState = flows[0] as NotificationState
        val agencies = flows[1] as List<NotificationAgency>
        val locations = flows[2] as List<NotificationLocation>
        val theme = flows[3] as ThemeOption
        val useUtc = flows[4] as Boolean
        val hideTbdLaunches = flows[5] as Boolean
        val hasNotificationCustomization = flows[6] as Boolean
        val hasCalendarSync = flows[7] as Boolean
        val snackbarMessage = flows[8] as String?

        SettingsUiState(
            notificationSettings = notificationState,
            availableAgencies = agencies,
            availableLocations = locations,
            notificationsEnabled = notificationState.enableNotifications,
            isLoading = notificationState.isLoading,
            errorMessage = notificationState.lastError,
            theme = theme,
            useUtc = useUtc,
            hideTbdLaunches = hideTbdLaunches,
            hasNotificationCustomization = hasNotificationCustomization,
            hasCalendarSync = hasCalendarSync,
            snackbarMessage = snackbarMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly, // Load immediately to avoid UI flicker
        initialValue = SettingsUiState()
    )

    init {
        loadAvailableOptions()

        // Collect subscription state and update premium features following ThemeCustomizationViewModel pattern
        viewModelScope.launch {
            subscriptionRepository.state.collect { subscriptionState ->
                _hasNotificationCustomization.value =
                    subscriptionRepository.hasFeature(PremiumFeature.NOTIFICATION_CUSTOMIZATION)
                _hasCalendarSync.value =
                    subscriptionRepository.hasFeature(PremiumFeature.CAL_SYNC)
            }
        }
    }

    private fun loadAvailableOptions() {
        viewModelScope.launch {
            try {
                val agencies = notificationRepository.getAvailableAgencies()
                val locations = notificationRepository.getAvailableLocations()

                _availableAgencies.value = agencies
                _availableLocations.value = locations

                println("Loaded ${agencies.size} agencies and ${locations.size} locations")
            } catch (e: Exception) {
                println("Failed to load available agencies/locations: ${e.message}")
            }
        }
    }

    // Simple delegation methods - no complex logic, just state updates

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                if (enabled) {
                    val hasPermission = notificationRepository.requestNotificationPermission()
                    if (!hasPermission) {
                        println("Notification permission denied")
                        return@launch
                    }
                }
                notificationRepository.setNotificationsEnabled(enabled)
            } catch (e: Exception) {
                println("Failed to update notifications enabled: ${e.message}")
            }
        }
    }

    fun updateFollowAllLaunches(enabled: Boolean) {
        viewModelScope.launch {
            notificationRepository.setFollowAllLaunches(enabled)
        }
    }

    fun updateStrictMatching(enabled: Boolean) {
        viewModelScope.launch {
            notificationRepository.setUseStrictMatching(enabled)
        }
    }

    fun toggleAgencySubscription(agency: NotificationAgency) {
        val currentState = uiState.value.notificationSettings
        val isCurrentlyEnabled = currentState.isAgencyEnabled(agency)
        
        // Check if trying to disable the last agency
        if (isCurrentlyEnabled && currentState.subscribedAgencies.size == 1) {
            _snackbarMessage.value = "At least one agency must be selected"
            return
        }

        viewModelScope.launch {
            notificationRepository.setAgencyEnabled(agency, !isCurrentlyEnabled)
        }
    }

    fun toggleLocationSubscription(location: NotificationLocation) {
        val currentState = uiState.value.notificationSettings
        val isCurrentlyEnabled = currentState.isLocationEnabled(location)
        
        // Check if trying to disable the last location
        if (isCurrentlyEnabled && currentState.subscribedLocations.size == 1) {
            _snackbarMessage.value = "At least one location must be selected"
            return
        }

        viewModelScope.launch {
            notificationRepository.setLocationEnabled(location, !isCurrentlyEnabled)
        }
    }

    // Topic update methods
    fun updateTopic(topic: NotificationTopic, enabled: Boolean) {
        viewModelScope.launch {
            notificationRepository.setTopicEnabled(topic, enabled)
        }
    }
    
    // Snackbar message management
    fun clearSnackbarMessage() {
        _snackbarMessage.value = null
    }

    // Convenience methods for backward compatibility with UI
    fun updateEventNotifications(enabled: Boolean) = updateTopic(NotificationTopic.EVENTS, enabled)
    fun updateNetstampChanged(enabled: Boolean) =
        updateTopic(NotificationTopic.NETSTAMP_CHANGED, enabled)

    fun updateWebcastOnly(enabled: Boolean) = updateTopic(NotificationTopic.WEBCAST_ONLY, enabled)
    fun updateTwentyFourHour(enabled: Boolean) =
        updateTopic(NotificationTopic.TWENTY_FOUR_HOUR, enabled)

    fun updateOneHour(enabled: Boolean) = updateTopic(NotificationTopic.ONE_HOUR, enabled)
    fun updateTenMinutes(enabled: Boolean) = updateTopic(NotificationTopic.TEN_MINUTES, enabled)
    fun updateOneMinute(enabled: Boolean) = updateTopic(NotificationTopic.ONE_MINUTE, enabled)
    fun updateInFlight(enabled: Boolean) = updateTopic(NotificationTopic.IN_FLIGHT, enabled)
    fun updateSuccess(enabled: Boolean) = updateTopic(NotificationTopic.SUCCESS, enabled)

    // App settings delegation
    fun updateTheme(theme: ThemeOption) {
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateTheme(theme)
            } catch (e: Exception) {
                println("Failed to update theme: ${e.message}")
            }
        }
    }

    fun updateUseUtc(useUtc: Boolean) {
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateUseUtc(useUtc)
            } catch (e: Exception) {
                println("Failed to update UTC setting: ${e.message}")
            }
        }
    }

    fun updateHideTbdLaunches(hide: Boolean) {
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateHideTbdLaunches(hide)
            } catch (e: Exception) {
                println("Failed to update hide TBD launches: ${e.message}")
            }
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            try {
                val hasPermission = notificationRepository.requestNotificationPermission()
                if (!hasPermission) {
                    println("Notification permission denied or not available")
                }
            } catch (e: Exception) {
                println("Failed to request notification permission: ${e.message}")
            }
        }
    }

    fun clearError() {
        // Errors are now handled in the repository state
        // UI will automatically reflect when error is cleared
    }

    // For backward compatibility with existing UI components
    suspend fun getAvailableAgencies(): List<NotificationAgency> {
        return notificationRepository.getAvailableAgencies()
    }

    suspend fun getAvailableLocations(): List<NotificationLocation> {
        return notificationRepository.getAvailableLocations()
    }
}