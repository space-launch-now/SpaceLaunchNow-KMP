package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.NotificationSettings
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.repository.NotificationRepository

data class SettingsUiState(
    val notificationSettings: NotificationSettings = NotificationSettings(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val theme: ThemeOption = ThemeOption.System,
    val useUtc: Boolean = false
)

enum class ThemeOption(val label: String) {
    System("System"),
    Light("Light"),
    Dark("Dark")
}

class SettingsViewModel(
    private val notificationRepository: NotificationRepository
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
                _uiState.value = _uiState.value.copy(
                    notificationSettings = settings,
                    isLoading = false,
                    errorMessage = null
                )
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
                    NotificationTopic.values().forEach { topic ->
                        notificationRepository.unsubscribeFromTopic(topic)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateDailySummary(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _uiState.value.notificationSettings
            val updatedSettings = currentSettings.copy(notifyDailySummary = enabled)

            try {
                notificationRepository.updateNotificationSettings(updatedSettings)
                _uiState.value = _uiState.value.copy(
                    notificationSettings = updatedSettings,
                    errorMessage = null
                )

                if (enabled) {
                    notificationRepository.subscribeToTopic(NotificationTopic.DAILY_SUMMARY)
                } else {
                    notificationRepository.unsubscribeFromTopic(NotificationTopic.DAILY_SUMMARY)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.message)
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _uiState.value = _uiState.value.copy(theme = theme)
        // TODO: Persist theme setting
    }

    fun updateUseUtc(useUtc: Boolean) {
        _uiState.value = _uiState.value.copy(useUtc = useUtc)
        // TODO: Persist UTC setting
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
}