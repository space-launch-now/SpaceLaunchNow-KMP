package me.calebjones.spacelaunchnow.wear.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.wear.remote.interactions.RemoteActivityHelper
import co.touchlab.kermit.Logger
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

data class LaunchDetailUiState(
    val launch: CachedLaunch? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val formattedTitle: String = "",
    val countdown: String = "",
)

class LaunchDetailViewModel(
    private val watchLaunchRepository: WatchLaunchRepository,
    private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchDetailUiState(isLoading = true))
    val uiState: StateFlow<LaunchDetailUiState> = _uiState.asStateFlow()

    private val log = Logger.withTag("LaunchDetailVM")

    fun loadLaunch(launchId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val launch = watchLaunchRepository.getLaunchById(launchId)
            if (launch != null) {
                _uiState.value = _uiState.value.copy(
                    launch = launch,
                    isLoading = false,
                    formattedTitle = formatTitle(launch),
                    countdown = formatCountdown(launch),
                )
                startCountdownTicker(launch)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Launch not found",
                )
            }
        }
    }

    fun openOnPhone(launchId: String) {
        viewModelScope.launch {
            try {
                val remoteActivityHelper = RemoteActivityHelper(context)
                val intent = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse("spacelaunchnow://launch/$launchId"))
                remoteActivityHelper.startRemoteActivity(intent).await()
                log.i { "Opened launch $launchId on phone" }
            } catch (e: Exception) {
                log.e(e) { "Failed to open launch on phone" }
            }
        }
    }

    private fun startCountdownTicker(launch: CachedLaunch) {
        viewModelScope.launch {
            while (isActive) {
                _uiState.value = _uiState.value.copy(
                    countdown = formatCountdown(launch),
                )
                delay(30.seconds)
            }
        }
    }

    private fun formatTitle(launch: CachedLaunch): String {
        val lspDisplay = launch.lspName?.let { name ->
            if (name.length > 15 && !launch.lspAbbrev.isNullOrEmpty()) {
                launch.lspAbbrev
            } else {
                name
            }
        }
        return when {
            lspDisplay != null && launch.rocketConfigName != null ->
                "$lspDisplay | ${launch.rocketConfigName}"
            launch.rocketConfigName != null -> launch.rocketConfigName
            else -> launch.name
        }
    }

    private fun formatCountdown(launch: CachedLaunch): String {
        val now = Clock.System.now()
        val duration = launch.net - now
        if (duration.isNegative()) return "Launched"

        val totalMinutes = duration.inWholeMinutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours >= 24 -> "T-${hours / 24}d ${hours % 24}h"
            hours > 0 -> "T-${hours}h ${minutes}m"
            minutes > 0 -> "T-${minutes}m"
            else -> "T-0"
        }
    }
}
