package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.ui.state.VideoPlayerState
import me.calebjones.spacelaunchnow.util.StarshipConstants

/**
 * Vehicle type selector for the Vehicles tab
 */
enum class VehicleType(val label: String) {
    SPACECRAFT("Ships"),      // Starship vehicles (S24, S25, etc.)
    LAUNCHERS("Boosters")     // Super Heavy boosters (B7, B9, etc.)
}

/**
 * Navigation level for the Vehicles tab hierarchy
 */
enum class VehicleNavigationLevel {
    CONFIGS,    // Show configuration grid (categories)
    VEHICLES    // Show individual vehicles for selected config
}

/**
 * Manages the Starship Dashboard screen with three tabs: Overview, Events, and Vehicles.
 *
 * This ViewModel follows industry best practices:
 * - Per-section ViewState<T> for independent loading/error states
 * - Stale-while-revalidate for graceful error recovery
 * - Priority-based parallel loading
 * - Unidirectional data flow (events → state → UI)
 * - Hierarchical vehicle navigation (configs → individual vehicles)
 */
class StarshipViewModel(
    private val launchRepository: LaunchRepository,
    private val updatesRepository: UpdatesRepository,
    private val eventsRepository: EventsRepository,
    private val articlesRepository: ArticlesRepository,
    private val spacecraftRepository: SpacecraftRepository,
    private val launcherRepository: LauncherRepository,
    private val programRepository: ProgramRepository,
    private val launcherConfigRepository: LauncherConfigRepository,
    private val spacecraftConfigRepository: SpacecraftConfigRepository
) : ViewModel() {

    // ========== Per-Section ViewState Flows ==========

    private val _programState = MutableStateFlow(ViewState<ProgramNormal?>(data = null))
    val programState: StateFlow<ViewState<ProgramNormal?>> = _programState.asStateFlow()

    private val _nextLaunchState = MutableStateFlow(ViewState<LaunchNormal?>(data = null))
    val nextLaunchState: StateFlow<ViewState<LaunchNormal?>> = _nextLaunchState.asStateFlow()

    private val _historyLaunchesState = MutableStateFlow(ViewState(data = emptyList<LaunchNormal>()))
    val historyLaunchesState: StateFlow<ViewState<List<LaunchNormal>>> = _historyLaunchesState.asStateFlow()

    private val _updatesState = MutableStateFlow(ViewState(data = emptyList<UpdateEndpoint>()))
    val updatesState: StateFlow<ViewState<List<UpdateEndpoint>>> = _updatesState.asStateFlow()

    private val _eventsState = MutableStateFlow(ViewState(data = emptyList<EventEndpointNormal>()))
    val eventsState: StateFlow<ViewState<List<EventEndpointNormal>>> = _eventsState.asStateFlow()

    private val _newsState = MutableStateFlow(ViewState(data = emptyList<Article>()))
    val newsState: StateFlow<ViewState<List<Article>>> = _newsState.asStateFlow()

    // Spacecraft (Starship ships like S24, S25)
    private val _spacecraftState =
        MutableStateFlow(ViewState(data = emptyList<SpacecraftEndpointDetailed>()))
    val spacecraftState: StateFlow<ViewState<List<SpacecraftEndpointDetailed>>> =
        _spacecraftState.asStateFlow()

    // Launchers (Super Heavy boosters like B7, B9)
    private val _launchersState = MutableStateFlow(ViewState(data = emptyList<LauncherDetailed>()))
    val launchersState: StateFlow<ViewState<List<LauncherDetailed>>> = _launchersState.asStateFlow()

    // Vehicle type selector
    private val _selectedVehicleType = MutableStateFlow(VehicleType.SPACECRAFT)
    val selectedVehicleType: StateFlow<VehicleType> = _selectedVehicleType.asStateFlow()

    // Pagination state for spacecraft
    private val _spacecraftHasMore = MutableStateFlow(true)
    val spacecraftHasMore: StateFlow<Boolean> = _spacecraftHasMore.asStateFlow()
    private val _spacecraftLoadingMore = MutableStateFlow(false)
    val spacecraftLoadingMore: StateFlow<Boolean> = _spacecraftLoadingMore.asStateFlow()
    private var spacecraftOffset = 0

    // Pagination state for launchers
    private val _launchersHasMore = MutableStateFlow(true)
    val launchersHasMore: StateFlow<Boolean> = _launchersHasMore.asStateFlow()
    private val _launchersLoadingMore = MutableStateFlow(false)
    val launchersLoadingMore: StateFlow<Boolean> = _launchersLoadingMore.asStateFlow()
    private var launchersOffset = 0

    // ========== Vehicle Configuration States (Hierarchical Navigation) ==========

    // Launcher configurations (rocket types like "Super Heavy")
    private val _launcherConfigsState =
        MutableStateFlow(ViewState(data = emptyList<LauncherConfigDetailed>()))
    val launcherConfigsState: StateFlow<ViewState<List<LauncherConfigDetailed>>> =
        _launcherConfigsState.asStateFlow()

    // Spacecraft configurations (spacecraft types like "Starship")
    private val _spacecraftConfigsState =
        MutableStateFlow(ViewState(data = emptyList<SpacecraftConfigDetailed>()))
    val spacecraftConfigsState: StateFlow<ViewState<List<SpacecraftConfigDetailed>>> =
        _spacecraftConfigsState.asStateFlow()

    // Navigation level - configs or individual vehicles
    private val _vehicleNavigationLevel = MutableStateFlow(VehicleNavigationLevel.CONFIGS)
    val vehicleNavigationLevel: StateFlow<VehicleNavigationLevel> =
        _vehicleNavigationLevel.asStateFlow()

    // Selected launcher config for drilling into individual vehicles
    private val _selectedLauncherConfig = MutableStateFlow<LauncherConfigDetailed?>(null)
    val selectedLauncherConfig: StateFlow<LauncherConfigDetailed?> =
        _selectedLauncherConfig.asStateFlow()

    // Selected spacecraft config for drilling into individual vehicles
    private val _selectedSpacecraftConfig = MutableStateFlow<SpacecraftConfigDetailed?>(null)
    val selectedSpacecraftConfig: StateFlow<SpacecraftConfigDetailed?> =
        _selectedSpacecraftConfig.asStateFlow()

    // Tab selection (simple state, not ViewState)
    private val _selectedTab = MutableStateFlow(StarshipTab.Overview)
    val selectedTab: StateFlow<StarshipTab> = _selectedTab.asStateFlow()

    // Video player state (separate concern)
    private val _videoPlayerState = MutableStateFlow(VideoPlayerState())
    val videoPlayerState: StateFlow<VideoPlayerState> = _videoPlayerState.asStateFlow()

    // ========== Derived States ==========

    /** Combined loading state for pull-to-refresh indicator */
    val isAnyLoading: StateFlow<Boolean> = combine(
        _programState,
        _nextLaunchState,
        _updatesState,
        _eventsState,
        _spacecraftState
    ) { program, launch, updates, events, spacecraft ->
        program.isLoading || launch.isLoading || updates.isLoading ||
                events.isLoading || spacecraft.isLoading || _launchersState.value.isLoading
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // ========== Initialization ==========

    init {
        loadOverviewData()
    }

    // ========== Public API ==========

    /**
     * Switch to a different tab
     */
    fun switchTab(tab: StarshipTab) {
        _selectedTab.value = tab

        when (tab) {
            StarshipTab.Overview -> loadOverviewData()
            StarshipTab.Events -> loadEventsData()
            StarshipTab.Vehicles -> loadVehiclesData()
        }
    }

    /**
     * Refresh current tab data (user-initiated)
     */
    fun refresh() {
        when (_selectedTab.value) {
            StarshipTab.Overview -> loadOverviewData(forceRefresh = true)
            StarshipTab.Events -> loadEventsData(forceRefresh = true)
            StarshipTab.Vehicles -> loadVehiclesData(forceRefresh = true)
        }
    }

    /**
     * Load all data for the Overview tab with priority-based loading.
     */
    fun loadOverviewData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            // Priority 1: Program data (drives livestream UI)
            val programJob = async { loadProgram(forceRefresh) }

            // Priority 2: Next launch (prominent display)
            val launchJob = async { loadNextLaunch(forceRefresh) }

            // Priority 3: History launches for timeline
            val historyJob = async { loadHistoryLaunches(forceRefresh) }

            // Priority 4: Background data (IO dispatcher)
            launch(Dispatchers.Default) { loadUpdates(forceRefresh) }
        }
    }

    /**
     * Load data for the Events tab.
     */
    fun loadEventsData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val eventsJob = async { loadEvents(forceRefresh) }
            launch(Dispatchers.Default) { loadUpdates(forceRefresh) }
            launch(Dispatchers.Default) { loadNews(forceRefresh) }
        }
    }

    /**
     * Load data for the Vehicles tab.
     * Loads either spacecraft or launchers based on selected vehicle type.
     */
    fun loadVehiclesData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            when (_vehicleNavigationLevel.value) {
                VehicleNavigationLevel.CONFIGS -> {
                    // Load both launcher and spacecraft configurations
                    async { loadLauncherConfigs(forceRefresh) }
                    async { loadSpacecraftConfigs(forceRefresh) }
                }

                VehicleNavigationLevel.VEHICLES -> {
                    // Load individual vehicles for selected config
                    when (_selectedVehicleType.value) {
                        VehicleType.SPACECRAFT -> loadSpacecraft(forceRefresh)
                        VehicleType.LAUNCHERS -> loadLaunchers(forceRefresh)
                    }
                }
            }
        }
    }

    /**
     * Switch between spacecraft and launcher vehicle types
     */
    fun switchVehicleType(type: VehicleType) {
        if (_selectedVehicleType.value != type) {
            _selectedVehicleType.value = type
            // Load data for the new type if not already loaded
            loadVehiclesData(forceRefresh = false)
        }
    }

    /**
     * Navigate to individual vehicles for a launcher configuration
     */
    fun selectLauncherConfig(config: LauncherConfigDetailed) {
        _selectedLauncherConfig.value = config
        _selectedSpacecraftConfig.value = null
        _selectedVehicleType.value = VehicleType.LAUNCHERS
        _vehicleNavigationLevel.value = VehicleNavigationLevel.VEHICLES

        // Reset pagination and load vehicles for this config
        launchersOffset = 0
        _launchersHasMore.value = true
        _launchersState.update { ViewState(data = emptyList()) }
        loadVehiclesData(forceRefresh = true)
    }

    /**
     * Navigate to individual vehicles for a spacecraft configuration
     */
    fun selectSpacecraftConfig(config: SpacecraftConfigDetailed) {
        _selectedSpacecraftConfig.value = config
        _selectedLauncherConfig.value = null
        _selectedVehicleType.value = VehicleType.SPACECRAFT
        _vehicleNavigationLevel.value = VehicleNavigationLevel.VEHICLES

        // Reset pagination and load vehicles for this config
        spacecraftOffset = 0
        _spacecraftHasMore.value = true
        _spacecraftState.update { ViewState(data = emptyList()) }
        loadVehiclesData(forceRefresh = true)
    }

    /**
     * Navigate back to configurations view
     */
    fun navigateBackToConfigs() {
        _vehicleNavigationLevel.value = VehicleNavigationLevel.CONFIGS
        _selectedLauncherConfig.value = null
        _selectedSpacecraftConfig.value = null
    }

    // ========== Private Loading Functions ==========

    private suspend fun loadProgram(forceRefresh: Boolean) {
        _programState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        programRepository.getProgram(StarshipConstants.STARSHIP_PROGRAM_ID, forceRefresh)
            .onSuccess { dataResult ->
                _programState.update {
                    it.copy(
                        data = dataResult.data,
                        isLoading = false,
                        dataSource = dataResult.source,
                        cacheTimestamp = dataResult.timestamp
                    )
                }
                updateVideoPlayerState(dataResult.data.vidUrls)
            }
            .onFailure { exception ->
                _programState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadNextLaunch(forceRefresh: Boolean) {
        _nextLaunchState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        launchRepository.getNextStarshipLaunch(
            limit = 1,
            forceRefresh = forceRefresh,
            programId = listOf(StarshipConstants.STARSHIP_PROGRAM_ID)
        )
            .onSuccess { dataResult ->
                val nextLaunch = dataResult.results.firstOrNull()
                _nextLaunchState.update {
                    it.copy(
                        data = nextLaunch,
                        isLoading = false
                    )
                }
            }
            .onFailure { exception ->
                _nextLaunchState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadHistoryLaunches(forceRefresh: Boolean) {
        _historyLaunchesState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        launchRepository.getStarshipHistoryLaunches(
            limit = 50,
            forceRefresh = forceRefresh
        )
            .onSuccess { dataResult ->
                _historyLaunchesState.update {
                    it.copy(
                        data = dataResult.data.results,
                        isLoading = false,
                        dataSource = dataResult.source,
                        cacheTimestamp = dataResult.timestamp
                    )
                }
            }
            .onFailure { exception ->
                _historyLaunchesState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadUpdates(forceRefresh: Boolean) {
        _updatesState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        updatesRepository.getUpdatesByProgram(
            allProgram = StarshipConstants.STARSHIP_PROGRAM_ID,
            limit = StarshipConstants.UPDATE_LIMIT,
            forceRefresh = forceRefresh
        )
            .onSuccess { dataResult ->
                _updatesState.update {
                    it.copy(
                        data = dataResult.data.results,
                        isLoading = false,
                        dataSource = dataResult.source,
                        cacheTimestamp = dataResult.timestamp
                    )
                }
            }
            .onFailure { exception ->
                _updatesState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadEvents(forceRefresh: Boolean) {
        _eventsState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        eventsRepository.getEventsByProgram(
            programId = StarshipConstants.STARSHIP_PROGRAM_ID,
            limit = 20,
            upcoming = true,
            forceRefresh = forceRefresh
        )
            .onSuccess { dataResult ->
                println("[STARSHIP] Events loaded: ${dataResult.data.results.size} items")
                _eventsState.update {
                    it.copy(
                        data = dataResult.data.results,
                        isLoading = false,
                        dataSource = dataResult.source,
                        cacheTimestamp = dataResult.timestamp
                    )
                }
            }
            .onFailure { exception ->
                println("[STARSHIP] Events load failed: ${exception.message}")
                _eventsState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadNews(forceRefresh: Boolean) {
        _newsState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        articlesRepository.searchArticles(
            query = "Starship",
            limit = StarshipConstants.NEWS_LIMIT
        )
            .onSuccess { articles ->
                println("[STARSHIP] News loaded: ${articles.results.size} articles")
                _newsState.update {
                    it.copy(data = articles.results, isLoading = false)
                }
            }
            .onFailure { exception ->
                println("[STARSHIP] News load failed: ${exception.message}")
                _newsState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    // ========== Configuration Loading Functions ==========

    private suspend fun loadLauncherConfigs(forceRefresh: Boolean) {
        // Skip if already loaded and not forcing refresh
        if (!forceRefresh && _launcherConfigsState.value.data.isNotEmpty()) return

        _launcherConfigsState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        launcherConfigRepository.getConfigurationsByProgram(
            programId = StarshipConstants.STARSHIP_PROGRAM_ID,
            limit = 20
        )
            .onSuccess { configs ->
                println("[STARSHIP] Launcher configs loaded: ${configs.results.size} configurations")
                _launcherConfigsState.update {
                    it.copy(data = configs.results, isLoading = false)
                }
            }
            .onFailure { exception ->
                println("[STARSHIP] Launcher configs load failed: ${exception.message}")
                _launcherConfigsState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadSpacecraftConfigs(forceRefresh: Boolean) {
        // Skip if already loaded and not forcing refresh
        if (!forceRefresh && _spacecraftConfigsState.value.data.isNotEmpty()) return

        _spacecraftConfigsState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        // SpacecraftConfigurations API doesn't have program filter, so we filter by SpaceX agency
        // and then filter client-side by checking the name contains "Starship"
        spacecraftConfigRepository.getConfigurationsByAgency(
            agencyId = StarshipConstants.SPACEX_AGENCY_ID,
            limit = 20
        )
            .onSuccess { configs ->
                // Filter to only Starship-related configs (the API doesn't have program filter)
                val starshipConfigs = configs.results.filter { config ->
                    config.name.contains("Starship", ignoreCase = true)
                }
                println("[STARSHIP] Spacecraft configs loaded: ${starshipConfigs.size} configurations (from ${configs.results.size} SpaceX configs)")
                _spacecraftConfigsState.update {
                    it.copy(data = starshipConfigs, isLoading = false)
                }
            }
            .onFailure { exception ->
                println("[STARSHIP] Spacecraft configs load failed: ${exception.message}")
                _spacecraftConfigsState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    // ========== Individual Vehicle Loading Functions ==========

    private suspend fun loadSpacecraft(forceRefresh: Boolean) {
        val selectedConfig = _selectedSpacecraftConfig.value

        // Reset pagination on refresh
        if (forceRefresh) {
            spacecraftOffset = 0
            _spacecraftHasMore.value = true
        }

        // Skip if already loaded and not forcing refresh
        if (!forceRefresh && _spacecraftState.value.data.isNotEmpty()) return

        _spacecraftState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        // If we have a selected config, filter by it; otherwise fallback to search
        val result = if (selectedConfig != null) {
            spacecraftRepository.getSpacecraftByConfig(
                configId = selectedConfig.id,
                limit = StarshipConstants.VEHICLES_LIMIT,
                forceRefresh = forceRefresh
            ).map { dataResult ->
                // Convert DataResult to simple list and count for pagination
                dataResult.data
            }
        } else {
            spacecraftRepository.getSpacecraft(
                limit = StarshipConstants.VEHICLES_LIMIT,
                search = "Starship",
                isPlaceholder = false
            ).map { it.results }
        }

        result
            .onSuccess { spacecraft ->
                println("[STARSHIP] Spacecraft loaded: ${spacecraft.size} ships (config: ${selectedConfig?.name ?: "search"})")
                spacecraftOffset = spacecraft.size
                // Simple pagination check - if we got fewer than limit, no more data
                _spacecraftHasMore.value = spacecraft.size >= StarshipConstants.VEHICLES_LIMIT
                _spacecraftState.update {
                    it.copy(data = spacecraft, isLoading = false)
                }
            }
            .onFailure { exception ->
                println("[STARSHIP] Spacecraft load failed: ${exception.message}")
                _spacecraftState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    private suspend fun loadLaunchers(forceRefresh: Boolean) {
        val selectedConfig = _selectedLauncherConfig.value

        // Reset pagination on refresh
        if (forceRefresh) {
            launchersOffset = 0
            _launchersHasMore.value = true
        }

        // Skip if already loaded and not forcing refresh
        if (!forceRefresh && _launchersState.value.data.isNotEmpty()) return

        _launchersState.update {
            it.copy(
                isLoading = true,
                isUserInitiated = forceRefresh,
                error = null
            )
        }

        // If we have a selected config, filter by it; otherwise fallback to search
        val result = if (selectedConfig != null) {
            launcherRepository.getLaunchersByConfig(
                configId = selectedConfig.id,
                limit = StarshipConstants.VEHICLES_LIMIT,
                offset = 0
            )
        } else {
            launcherRepository.getLaunchers(
                limit = StarshipConstants.VEHICLES_LIMIT,
                search = "Super Heavy"
            )
        }

        result
            .onSuccess { launchers ->
                // Sort by serial number numerically (e.g., "Booster 4" -> 4, "B12" -> 12)
                val sortedLaunchers = launchers.results.sortedBy { launcher ->
                    launcher.serialNumber?.let { serial ->
                        // Extract numeric part from serial number
                        serial.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
                    } ?: Int.MAX_VALUE
                }
                println("[STARSHIP] Launchers loaded: ${sortedLaunchers.size} boosters (config: ${selectedConfig?.name ?: "search"})")
                launchersOffset = sortedLaunchers.size
                _launchersHasMore.value = launchers.next != null
                _launchersState.update {
                    it.copy(data = sortedLaunchers, isLoading = false)
                }
            }
            .onFailure { exception ->
                println("[STARSHIP] Launchers load failed: ${exception.message}")
                _launchersState.update {
                    it.copy(error = formatErrorMessage(exception), isLoading = false)
                }
            }
    }

    /**
     * Load more spacecraft for pagination
     */
    fun loadMoreSpacecraft() {
        if (_spacecraftLoadingMore.value || !_spacecraftHasMore.value) return

        val selectedConfig = _selectedSpacecraftConfig.value

        viewModelScope.launch {
            _spacecraftLoadingMore.value = true

            // Note: getSpacecraftByConfig doesn't support offset, so for now use search-based pagination
            // In a future update, could add offset support to the repository
            spacecraftRepository.getSpacecraft(
                limit = StarshipConstants.VEHICLES_LIMIT,
                offset = spacecraftOffset,
                search = selectedConfig?.name ?: "Starship",
                isPlaceholder = false
            )
                .onSuccess { spacecraft ->
                    println("[STARSHIP] More spacecraft loaded: ${spacecraft.results.size} ships (offset: $spacecraftOffset)")
                    spacecraftOffset += spacecraft.results.size
                    _spacecraftHasMore.value = spacecraft.next != null
                    _spacecraftState.update { current ->
                        // Deduplicate by ID to prevent duplicate key errors
                        val combined = (current.data + spacecraft.results).distinctBy { it.id }
                        current.copy(data = combined)
                    }
                }
                .onFailure { exception ->
                    println("[STARSHIP] Load more spacecraft failed: ${exception.message}")
                }

            _spacecraftLoadingMore.value = false
        }
    }

    /**
     * Load more launchers for pagination
     */
    fun loadMoreLaunchers() {
        if (_launchersLoadingMore.value || !_launchersHasMore.value) return

        val selectedConfig = _selectedLauncherConfig.value

        viewModelScope.launch {
            _launchersLoadingMore.value = true

            // Use config-based filtering if a config is selected
            val result = if (selectedConfig != null) {
                launcherRepository.getLaunchers(
                    limit = StarshipConstants.VEHICLES_LIMIT,
                    offset = launchersOffset,
                    launcherConfigId = selectedConfig.id
                )
            } else {
                launcherRepository.getLaunchers(
                    limit = StarshipConstants.VEHICLES_LIMIT,
                    offset = launchersOffset,
                    search = "Super Heavy"
                )
            }

            result
                .onSuccess { launchers ->
                    println("[STARSHIP] More launchers loaded: ${launchers.results.size} boosters (offset: $launchersOffset)")
                    launchersOffset += launchers.results.size
                    _launchersHasMore.value = launchers.next != null
                    _launchersState.update { current ->
                        // Combine, deduplicate by ID, and re-sort to maintain order
                        val combined = (current.data + launchers.results)
                            .distinctBy { it.id }
                        val sorted = combined.sortedBy { launcher ->
                            launcher.serialNumber?.let { serial ->
                                serial.filter { it.isDigit() }.toIntOrNull() ?: Int.MAX_VALUE
                            } ?: Int.MAX_VALUE
                        }
                        current.copy(data = sorted)
                    }
                }
                .onFailure { exception ->
                    println("[STARSHIP] Load more launchers failed: ${exception.message}")
                }

            _launchersLoadingMore.value = false
        }
    }

    // ========== Video Player Helpers ==========

    private fun updateVideoPlayerState(videos: List<VidURL>) {
        if (videos.isEmpty()) {
            _videoPlayerState.value = VideoPlayerState()
            return
        }

        val sortedVideos = videos.sortedBy { it.priority ?: Int.MAX_VALUE }
        _videoPlayerState.value = _videoPlayerState.value.copy(
            availableVideos = sortedVideos,
            selectedVideoIndex = 0
        )
    }

    fun setPlayerVisible(visible: Boolean) {
        _videoPlayerState.value = _videoPlayerState.value.copy(isPlayerVisible = visible)
    }

    fun setVideoIndex(index: Int) {
        _videoPlayerState.value = _videoPlayerState.value.copy(selectedVideoIndex = index)
    }

    fun setFullscreen(fullscreen: Boolean) {
        _videoPlayerState.value = _videoPlayerState.value.copy(isFullscreen = fullscreen)
    }

    // ========== Utilities ==========

    private fun formatErrorMessage(exception: Throwable): String {
        return exception.message ?: "An unknown error occurred"
    }
}

/**
 * Tab selection for the Starship Dashboard
 */
enum class StarshipTab {
    Overview,
    Events,
    Vehicles
}

