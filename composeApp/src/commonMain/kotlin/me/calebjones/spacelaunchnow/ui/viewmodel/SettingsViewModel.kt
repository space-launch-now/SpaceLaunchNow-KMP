package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationSettings
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences

data class SettingsUiState(
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val availableAgencies: List<NotificationAgency> = emptyList(),
    val availableLocations: List<NotificationLocation> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val theme: ThemeOption = ThemeOption.System,
    val useUtc: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val hideTbdLaunches: Boolean = false,
    val keepLaunchesFor24Hours: Boolean = true
)

enum class ThemeOption(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

class AppSettingsViewModel(
    private val appPreferences: AppPreferences
) : ViewModel() {

    val themeFlow = appPreferences.themeFlow
    val useUtcFlow = appPreferences.useUtcFlow
    val hideTbdLaunchesFlow = appPreferences.hideTbdLaunchesFlow
    val keepLaunchesFor24HoursFlow = appPreferences.keepLaunchesFor24HoursFlow

    suspend fun updateHideTbdLaunches(hide: Boolean) {
        appPreferences.updateHideTbdLaunches(hide)
    }

    suspend fun updateKeepLaunchesFor24Hours(keep: Boolean) {
        appPreferences.updateKeepLaunchesFor24Hours(keep)
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
    private val appSettingsViewModel: AppSettingsViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val settings = notificationRepository.getNotificationSettings()
                val agencies = notificationRepository.getAvailableAgencies()
                val locations = notificationRepository.getAvailableLocations()

                // Load app settings
                val theme = appSettingsViewModel.themeFlow.first()
                val useUtc = appSettingsViewModel.useUtcFlow.first()
                val hideTbdLaunches = appSettingsViewModel.hideTbdLaunchesFlow.first()
                val keepLaunchesFor24Hours = appSettingsViewModel.keepLaunchesFor24HoursFlow.first()

                // If this is a fresh install (no subscriptions yet), subscribe to all by default
                if (settings.subscribedAgencies.isEmpty() && settings.subscribedLocations.isEmpty()) {
                    println("Fresh install detected - subscribing to all agencies and locations by default")

                    // Subscribe to all agencies
                    for (agency in agencies) {
                        notificationRepository.subscribeToAgency(agency)
                        println("Default subscription to agency: ${agency.name}")
                    }

                    // Subscribe to all locations  
                    for (location in locations) {
                        notificationRepository.subscribeToLocation(location)
                        println("Default subscription to location: ${location.name}")
                    }

                    // Get updated settings after subscriptions
                    val updatedSettings = notificationRepository.getNotificationSettings()
                    _uiState.value = _uiState.value.copy(
                        notificationSettings = updatedSettings,
                        availableAgencies = agencies,
                        availableLocations = locations,
                        theme = theme,
                        useUtc = useUtc,
                        isLoading = false,
                        hideTbdLaunches = hideTbdLaunches,
                        keepLaunchesFor24Hours = keepLaunchesFor24Hours,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        notificationSettings = settings,
                        availableAgencies = agencies,
                        availableLocations = locations,
                        theme = theme,
                        useUtc = useUtc,
                        isLoading = false,
                        hideTbdLaunches = hideTbdLaunches,
                        keepLaunchesFor24Hours = keepLaunchesFor24Hours,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.notificationSettings
            val updatedSettings = currentSettings.copy(enableNotifications = enabled)

            try {
                notificationRepository.updateNotificationSettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    notificationSettings = updatedSettings,
                    errorMessage = null
                )

                // Subscribe to default topics if notifications are enabled
                if (enabled) {
                    notificationRepository.subscribeToTopic(NotificationTopic.LAUNCHES_ALL)
                } else {
                    // Unsubscribe from all topics
                    notificationRepository.unsubscribeFromTopic(NotificationTopic.LAUNCHES_ALL)
                    notificationRepository.unsubscribeFromTopic(NotificationTopic.EVENTS)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateDailySummary(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings =
                _uiState.value.notificationSettings.copy(notifyDailySummary = enabled)
            notificationRepository.updateNotificationSettings(currentSettings)
            _uiState.value =
                _uiState.value.copy(notificationSettings = currentSettings, errorMessage = null)
        }
    }

    fun toggleAgencySubscription(agency: NotificationAgency) {
        println("SettingsViewModel: Toggling agency subscription for ${agency.name} (ID: ${agency.id})")

        val currentSettings = _uiState.value.notificationSettings
        val isSubscribed = currentSettings.subscribedAgencies.contains(agency.id)
        println("Agency ${agency.name} currently subscribed: $isSubscribed")

        // If disabling a provider and followAll is enabled, also disable follow all
        val needsToDisableFollowAll = currentSettings.followAllLaunches && isSubscribed
        val updatedSettings = if (needsToDisableFollowAll) {
            currentSettings.copy(
                followAllLaunches = false,
                subscribedAgencies = currentSettings.subscribedAgencies - agency.id
            )
        } else {
            val updatedAgencies = if (isSubscribed) {
                currentSettings.subscribedAgencies - agency.id
            } else {
                currentSettings.subscribedAgencies + agency.id
            }
            currentSettings.copy(subscribedAgencies = updatedAgencies)
        }
        _uiState.value = _uiState.value.copy(notificationSettings = updatedSettings)

        // Do heavy work in background
        viewModelScope.launch {
            try {
                if (needsToDisableFollowAll) {
                    notificationRepository.updateNotificationSettings(updatedSettings)
                } else if (isSubscribed) {
                    notificationRepository.unsubscribeFromAgency(agency)
                } else {
                    notificationRepository.subscribeToAgency(agency)
                }
                // Do not forcibly reload fetchedSettings unless there is an error
                println("Agency subscription toggle completed for ${agency.name}")
            } catch (e: Exception) {
                println("ERROR: Failed to toggle agency subscription for ${agency.name}: ${e.message}")
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun toggleLocationSubscription(location: NotificationLocation) {
        println("SettingsViewModel: Toggling location subscription for ${location.name} (ID: ${location.id})")
        val currentSettings = _uiState.value.notificationSettings
        val isSubscribed = currentSettings.subscribedLocations.contains(location.id)
        println("Location ${location.name} currently subscribed: $isSubscribed")

        val needsToDisableFollowAll = currentSettings.followAllLaunches && isSubscribed
        val updatedSettings = if (needsToDisableFollowAll) {
            currentSettings.copy(
                followAllLaunches = false,
                subscribedLocations = currentSettings.subscribedLocations - location.id
            )
        } else {
            val updatedLocations = if (isSubscribed) {
                currentSettings.subscribedLocations - location.id
            } else {
                currentSettings.subscribedLocations + location.id
            }
            currentSettings.copy(subscribedLocations = updatedLocations)
        }
        _uiState.value = _uiState.value.copy(notificationSettings = updatedSettings)

        viewModelScope.launch {
            try {
                if (needsToDisableFollowAll) {
                    notificationRepository.updateNotificationSettings(updatedSettings)
                } else if (isSubscribed) {
                    notificationRepository.unsubscribeFromLocation(location)
                } else {
                    notificationRepository.subscribeToLocation(location)
                }
                // Do not forcibly reload fetchedSettings unless there is an error
                println("Location subscription toggle completed for ${location.name}")
            } catch (e: Exception) {
                println("ERROR: Failed to toggle location subscription for ${location.name}: ${e.message}")
                // Only reload state on error
                val fetchedSettings = notificationRepository.getNotificationSettings()
                _uiState.value = _uiState.value.copy(
                    notificationSettings = fetchedSettings,
                    errorMessage = e.message
                )
            }
        }
    }

    fun updateStrictMatching(useStrict: Boolean) {
        val currentSettings = _uiState.value.notificationSettings
        val updatedSettings = currentSettings.copy(useStrictMatching = useStrict)
        _uiState.value =
            _uiState.value.copy(notificationSettings = updatedSettings) // Immediate feedback

        viewModelScope.launch {
            try {
                notificationRepository.updateNotificationSettings(updatedSettings)
                notificationRepository.updateTopicSubscriptions()

                val latestSettings = notificationRepository.getNotificationSettings()
                _uiState.value =
                    _uiState.value.copy(notificationSettings = latestSettings, errorMessage = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateFollowAllLaunches(followAll: Boolean) {
        println("SettingsViewModel: Updating Follow All Launches to: $followAll")
        val currentSettings = _uiState.value.notificationSettings
        val updatedSettings = currentSettings.copy(
            followAllLaunches = followAll,
            // Automatically turn off strict matching when following all launches
            useStrictMatching = if (followAll) false else currentSettings.useStrictMatching
        )
        _uiState.value =
            _uiState.value.copy(notificationSettings = updatedSettings) // Immediate UI update

        viewModelScope.launch {
            try {
                notificationRepository.updateNotificationSettings(updatedSettings)

                // When enabling Follow All, ensure all individual subscriptions are also active
                if (followAll) {
                    println("Follow All enabled - ensuring all individual subscriptions are active")

                    // Subscribe to all agencies if not already subscribed
                    val allAgencies = _uiState.value.availableAgencies
                    for (agency in allAgencies) {
                        if (!currentSettings.subscribedAgencies.contains(agency.id)) {
                            notificationRepository.subscribeToAgency(agency)
                            println("Subscribed to agency: ${agency.name}")
                        }
                    }

                    // Subscribe to all locations if not already subscribed
                    val allLocations = _uiState.value.availableLocations
                    for (location in allLocations) {
                        if (!currentSettings.subscribedLocations.contains(location.id)) {
                            notificationRepository.subscribeToLocation(location)
                            println("Subscribed to location: ${location.name}")
                        }
                    }
                }
                // When disabling Follow All, individual subscriptions remain as they are
                // This ensures users keep their current selections

                notificationRepository.updateTopicSubscriptions()

                // Refresh settings after all updates
                val finalSettings = notificationRepository.getNotificationSettings()
                _uiState.value = _uiState.value.copy(
                    notificationSettings = finalSettings,
                    errorMessage = null
                )
                println("Follow All Launches updated successfully to: $followAll")
                if (followAll) {
                    println("Strict matching automatically disabled, all individual subscriptions ensured")
                }
            } catch (e: Exception) {
                println("ERROR: Failed to update Follow All Launches: ${e.message}")
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateHideTbdLaunches(hide: Boolean) {
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateHideTbdLaunches(hide)
                _uiState.value = _uiState.value.copy(
                    hideTbdLaunches = hide,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateKeepLaunchesFor24Hours(keep: Boolean) {
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateKeepLaunchesFor24Hours(keep)
                _uiState.value = _uiState.value.copy(
                    keepLaunchesFor24Hours = keep,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _uiState.value = _uiState.value.copy(theme = theme)
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateTheme(theme)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateUseUtc(useUtc: Boolean) {
        _uiState.value = _uiState.value.copy(useUtc = useUtc)
        viewModelScope.launch {
            try {
                appSettingsViewModel.updateUseUtc(useUtc)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings =
                    _uiState.value.notificationSettings.copy(enableNotifications = enabled)
                notificationRepository.updateNotificationSettings(currentSettings)
                _uiState.value = _uiState.value.copy(
                    notificationSettings = currentSettings,
                    notificationsEnabled = enabled,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun requestNotificationPermission() {
        viewModelScope.launch {
            try {
                val hasPermission = notificationRepository.requestNotificationPermission()
                if (!hasPermission) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Notification permission denied"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun updateEventNotifications(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(eventNotifications = enabled)
        }
    }

    fun updateNetstampChanged(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(netstampChanged = enabled)
        }
    }

    fun updateWebcastOnly(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(webcastOnly = enabled)
        }
    }

    fun updateTwentyFourHour(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(twentyFourHour = enabled)
        }
    }

    fun updateOneHour(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(oneHour = enabled)
        }
    }

    fun updateTenMinutes(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(tenMinutes = enabled)
        }
    }

    fun updateOneMinute(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(oneMinute = enabled)
        }
    }

    fun updateInFlight(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(inFlight = enabled)
        }
    }

    fun updateSuccess(enabled: Boolean) {
        updateNotificationTopic { settings ->
            settings.copy(success = enabled)
        }
    }

    private fun updateNotificationTopic(updateFunction: (NotificationSettings) -> NotificationSettings) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.notificationSettings
            val updatedSettings = updateFunction(currentSettings)

            try {
                notificationRepository.updateNotificationSettings(updatedSettings)
                // Update topic subscriptions to reflect the changes
                notificationRepository.updateTopicSubscriptions()

                _uiState.value = _uiState.value.copy(
                    notificationSettings = updatedSettings,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }
}