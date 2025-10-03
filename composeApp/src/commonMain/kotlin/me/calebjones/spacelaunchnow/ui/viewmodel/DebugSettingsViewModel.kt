package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugSettings
import me.calebjones.spacelaunchnow.util.BuildConfig

class DebugSettingsViewModel(
    private val debugPreferences: DebugPreferences? = null
) : ViewModel() {

    private val _debugSettings = MutableStateFlow(
        DebugSettings(
            useCustomApiUrl = false,
            customApiBaseUrl = DebugPreferences.PROD_API_URL,
            useDebugTopics = true
        )
    )
    val debugSettings: StateFlow<DebugSettings> = _debugSettings.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

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
    }
}