package me.calebjones.spacelaunchnow.wear.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.data.model.CachedLaunch
import me.calebjones.spacelaunchnow.wear.data.model.DataSource
import kotlin.time.Clock
import kotlin.time.Instant

data class LaunchListUiState(
    val launches: List<CachedLaunch> = emptyList(),
    val isLoading: Boolean = false,
    val dataSource: DataSource = DataSource.STALE_CACHE,
    val lastUpdated: Instant? = null,
    val error: String? = null,
    val isPremium: Boolean = false,
)

class LaunchListViewModel(
    private val watchLaunchRepository: WatchLaunchRepository,
    private val entitlementSyncManager: EntitlementSyncManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchListUiState(isLoading = true))
    val uiState: StateFlow<LaunchListUiState> = _uiState.asStateFlow()

    init {
        observeData()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val result = watchLaunchRepository.refreshLaunches()
            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to refresh",
                )
            }
        }
    }

    private fun observeData() {
        combine(
            watchLaunchRepository.launches,
            watchLaunchRepository.dataSource,
            entitlementSyncManager.entitlementState,
        ) { launches, dataSource, entitlement ->
            val now = Clock.System.now()
            val upcoming = launches.filter { it.net > now }.sortedBy { it.net }
            _uiState.value = _uiState.value.copy(
                launches = upcoming,
                dataSource = dataSource,
                lastUpdated = now,
                isLoading = false,
                isPremium = entitlement.hasWearOs,
            )
        }.launchIn(viewModelScope)
    }
}
