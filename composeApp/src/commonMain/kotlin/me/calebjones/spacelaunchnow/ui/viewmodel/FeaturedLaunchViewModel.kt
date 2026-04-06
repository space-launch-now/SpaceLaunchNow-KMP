package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.model.PinnedContent
import me.calebjones.spacelaunchnow.data.model.PinnedContentType
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.data.storage.PinnedContentPreferences
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Sealed class for pinned content - can be either a launch or an event.
 */
sealed class PinnedContentData {
    abstract val config: PinnedContent
    abstract val customMessage: String?
    abstract val id: String
    abstract val name: String
    abstract val imageUrl: String?
    abstract val location: String?
}

/**
 * Container for pinned content with resolved launch data.
 * This combines the remote config settings with the actual launch object.
 */
data class PinnedLaunchContent(
    override val config: PinnedContent,
    val launch: LaunchNormal,
    override val customMessage: String? = config.customMessage
) : PinnedContentData() {
    override val id: String get() = launch.id
    override val name: String get() = launch.name ?: "Unknown Launch"
    override val imageUrl: String? get() = launch.image?.imageUrl
    override val location: String? get() = launch.pad?.location?.name
}

/**
 * Container for pinned content with resolved event data.
 */
data class PinnedEventContent(
    override val config: PinnedContent,
    val event: EventEndpointDetailed,
    override val customMessage: String? = config.customMessage
) : PinnedContentData() {
    override val id: String get() = event.id.toString()
    override val name: String get() = event.name ?: "Unknown Event"
    override val imageUrl: String? get() = event.image?.imageUrl
    override val location: String? get() = event.location
}

/**
 * Manages the featured launch display (NextUpView) and additional featured launches row.
 * Uses dedicated API call with upcomingWithRecent filter, fetching 4 launches total.
 * The first launch is displayed in the hero card, remaining 3 are shown in a horizontal row.
 * Also handles pinned content from Firebase Remote Config and in-flight LIVE launches.
 */
class FeaturedLaunchViewModel(
    private val launchRepository: LaunchRepository,
    private val eventsRepository: EventsRepository,
    private val launchFilterService: LaunchFilterService,
    private val notificationStateStorage: NotificationStateStorage,
    private val launchCache: LaunchCache,
    private val remoteConfigRepository: RemoteConfigRepository,
    private val pinnedContentPreferences: PinnedContentPreferences
) : ViewModel() {

    private val log = logger()

    // Featured Launch State (hero card - first result)
    // Initialize with isLoading=true to show shimmer instead of empty state before first load
    private val _featuredLaunchState =
        MutableStateFlow(ViewState<LaunchNormal?>(data = null, isLoading = true))
    val featuredLaunchState: StateFlow<ViewState<LaunchNormal?>> =
        _featuredLaunchState.asStateFlow()

    // Additional Featured Launches State (row of 3 - results 2-4)
    // Initialize with isLoading=true to show shimmer instead of empty state before first load
    private val _additionalFeaturedLaunches =
        MutableStateFlow(ViewState<List<LaunchNormal>>(data = emptyList(), isLoading = true))
    val additionalFeaturedLaunches: StateFlow<ViewState<List<LaunchNormal>>> =
        _additionalFeaturedLaunches.asStateFlow()

    // In-Flight Launch State (LIVE launches currently flying - status_id = 6)
    private val _inFlightLaunchState = MutableStateFlow(ViewState<LaunchNormal?>(data = null))
    val inFlightLaunchState: StateFlow<ViewState<LaunchNormal?>> =
        _inFlightLaunchState.asStateFlow()

    // Pinned Content State (featured launch/event from Firebase Remote Config)
    // Contains both the config and the resolved launch or event data
    private val _pinnedContentState = MutableStateFlow(ViewState<PinnedContentData?>(data = null))
    val pinnedContentState: StateFlow<ViewState<PinnedContentData?>> =
        _pinnedContentState.asStateFlow()

    /**
     * Loads featured launches using dedicated API call with upcomingWithRecent filter.
     * Fetches 4 launches: first for hero card, remaining 3 for additional featured row.
     *
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadFeaturedLaunch(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                log.d { "Loading featured launches (4 total) - forceRefresh: $forceRefresh" }

                _featuredLaunchState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                _additionalFeaturedLaunches.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }
                log.d { "Set isLoading=true for featured launch states" }

                // Wait for actual filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                val filterParams = launchFilterService.getFilterParams(currentFilters)
                log.v { "Filter params - agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}" }

                log.d { "Calling repository.getFeaturedLaunch with upcomingWithRecent filter..." }
                val result = launchRepository.getFeaturedLaunch(
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                )
                log.d { "Repository call completed. Success: ${result.isSuccess}" }

                result.onSuccess { dataResult ->
                    val paginatedLaunches = dataResult.data
                    log.i { "Repository success - Data source: ${dataResult.source}, Results: ${paginatedLaunches.results.size}" }
                    log.v { "Cache timestamp: ${dataResult.timestamp}" }

                    val allLaunches = paginatedLaunches.results
                    val firstLaunch = allLaunches.firstOrNull()
                    val additionalLaunches =
                        if (allLaunches.size > 1) allLaunches.drop(1).take(3) else emptyList()

                    if (firstLaunch != null) {
                        log.i { "Featured launch: ${firstLaunch.name} (ID: ${firstLaunch.id})" }
                        log.i { "Additional featured launches: ${additionalLaunches.size}" }
                    } else {
                        log.w { "No launches returned from repository!" }
                    }

                    _featuredLaunchState.update {
                        it.copy(
                            data = firstLaunch,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }

                    _additionalFeaturedLaunches.update {
                        it.copy(
                            data = additionalLaunches,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                    log.d { "Updated featuredLaunchState: hasData=${_featuredLaunchState.value.data != null}, additionalCount=${additionalLaunches.size}" }

                    // If we got stale data and didn't already force refresh, trigger a background refresh
                    if (dataResult.source == DataSource.STALE_CACHE && !forceRefresh) {
                        log.i { "Received stale cache data, triggering background refresh" }
                        viewModelScope.launch {
                            loadFeaturedLaunch(forceRefresh = true)
                        }
                    }

                    // Pre-fetch detailed data if we have a launch
                    firstLaunch?.let { launch ->
                        log.d { "Pre-fetching launch details for ${launch.id}..." }
                        preFetchLaunchDetails(launch.id)
                    }
                }.onFailure { exception ->
                    log.e(exception) { "Repository failure: ${exception.message}" }
                    val errorMsg = formatErrorMessage(exception)

                    _featuredLaunchState.update {
                        it.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                    }
                    _additionalFeaturedLaunches.update {
                        it.copy(
                            error = errorMsg,
                            isLoading = false
                        )
                    }
                    log.d { "Updated featuredLaunchState with error, isLoading=false" }
                }
            } catch (exception: Exception) {
                log.e(exception) { "Exception in loadFeaturedLaunch: ${exception.message}" }

                val errorMsg = exception.message ?: "Unknown error"
                _featuredLaunchState.update {
                    it.copy(
                        error = errorMsg,
                        isLoading = false
                    )
                }
                _additionalFeaturedLaunches.update {
                    it.copy(
                        error = errorMsg,
                        isLoading = false
                    )
                }
                log.d { "Updated featuredLaunchState with exception error, isLoading=false" }
            }
        }
    }

    private suspend fun preFetchLaunchDetails(launchId: String) {
        try {
            // Pre-cache the detailed launch data for faster detail screen loading
            launchCache.getCachedLaunchDetailed(launchId)
        } catch (e: Exception) {
            log.w(e) { "Failed to pre-fetch launch details" }
        }
    }

    private fun formatErrorMessage(exception: Throwable): String {
        return exception.message ?: "An unknown error occurred"
    }

    fun refresh() = loadFeaturedLaunch(forceRefresh = true)

    /**
     * Loads in-flight launches (status_id = 6) for LIVE card display.
     * These are launches that are currently flying and match user's filter preferences.
     *
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadInFlightLaunch(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                log.d { "Loading in-flight launches - forceRefresh: $forceRefresh" }

                _inFlightLaunchState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                // Get filter settings from DataStore
                val currentFilters = notificationStateStorage.stateFlow.first()
                val filterParams = launchFilterService.getFilterParams(currentFilters)
                log.v { "In-flight filter params - agencyIds: ${filterParams.agencyIds}, locationIds: ${filterParams.locationIds}" }

                val result = launchRepository.getInFlightLaunches(
                    forceRefresh = forceRefresh,
                    agencyIds = filterParams.agencyIds,
                    locationIds = filterParams.locationIds
                )

                result.onSuccess { dataResult ->
                    val inFlightLaunches = dataResult.data.results
                    log.i { "In-flight launches found: ${inFlightLaunches.size}" }

                    // Take the first in-flight launch for display
                    val firstInFlight = inFlightLaunches.firstOrNull()
                    if (firstInFlight != null) {
                        log.i { "LIVE launch: ${firstInFlight.name} (status: ${firstInFlight.status?.name})" }
                    }

                    _inFlightLaunchState.update {
                        it.copy(
                            data = firstInFlight,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    log.e(exception) { "Failed to load in-flight launches: ${exception.message}" }
                    _inFlightLaunchState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                log.e(exception) { "Exception in loadInFlightLaunch: ${exception.message}" }
                _inFlightLaunchState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads pinned content from Firebase Remote Config.
     * Fetches and activates the latest Remote Config values before reading pinned content.
     * If valid pinned content exists, fetches the associated launch or event data.
     *
     * @param forceRefresh If true, bypass Remote Config cache (e.g., pull-to-refresh)
     */
    fun loadPinnedContent(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                log.d { "Loading pinned content from Remote Config (forceRefresh=$forceRefresh)..." }

                _pinnedContentState.update {
                    it.copy(isLoading = true, error = null)
                }

                // Fetch latest Remote Config values before reading
                val fetchResult = remoteConfigRepository.fetchAndActivate(forceRefresh)
                fetchResult.onFailure { e ->
                    log.w(e) { "Remote Config fetch failed, will use cached values: ${e.message}" }
                }

                val configResult = remoteConfigRepository.getPinnedContent()

                configResult.onSuccess { pinnedContent ->
                    val currentTime = Instant.fromEpochMilliseconds(
                        Clock.System.now().toEpochMilliseconds()
                    )
                    if (pinnedContent == null || !pinnedContent.isActive(currentTime)) {
                        log.d { "No active pinned content (null or expired)" }
                        _pinnedContentState.update {
                            it.copy(data = null, isLoading = false)
                        }
                        return@onSuccess
                    }

                    log.i { "Active pinned content found: type=${pinnedContent.type}, id=${pinnedContent.id}" }

                    // Check if this content has been dismissed
                    val dismissedIds = pinnedContentPreferences.dismissedIdsFlow.first()
                    if (dismissedIds.contains(pinnedContent.id)) {
                        log.d { "Pinned content ${pinnedContent.id} was dismissed by user" }
                        _pinnedContentState.update {
                            it.copy(data = null, isLoading = false)
                        }
                        return@onSuccess
                    }

                    when (pinnedContent.type) {
                        PinnedContentType.LAUNCH -> {
                            // Fetch the launch data
                            val launchResult = launchRepository.getLaunchById(pinnedContent.id)
                            launchResult.onSuccess { launch ->
                                if (launch != null) {
                                    log.i { "Pinned launch loaded: ${launch.name}" }
                                    _pinnedContentState.update {
                                        it.copy(
                                            data = PinnedLaunchContent(
                                                config = pinnedContent,
                                                launch = launch,
                                                customMessage = pinnedContent.customMessage
                                            ),
                                            isLoading = false
                                        )
                                    }
                                } else {
                                    log.w { "Pinned launch not found: ${pinnedContent.id}" }
                                    _pinnedContentState.update {
                                        it.copy(data = null, isLoading = false)
                                    }
                                }
                            }.onFailure { e ->
                                log.e(e) { "Failed to load pinned launch: ${e.message}" }
                                _pinnedContentState.update {
                                    it.copy(error = e.message, isLoading = false)
                                }
                            }
                        }
                        PinnedContentType.EVENT -> {
                            // Fetch the event data
                            val eventId = pinnedContent.id.toIntOrNull()
                            if (eventId == null) {
                                log.e { "Invalid event ID (not an integer): ${pinnedContent.id}" }
                                _pinnedContentState.update {
                                    it.copy(data = null, isLoading = false, error = "Invalid event ID")
                                }
                                return@onSuccess
                            }
                            
                            val eventResult = eventsRepository.getEventDetails(eventId)
                            eventResult.onSuccess { event ->
                                log.i { "Pinned event loaded: ${event.name}" }
                                _pinnedContentState.update {
                                    it.copy(
                                        data = PinnedEventContent(
                                            config = pinnedContent,
                                            event = event,
                                            customMessage = pinnedContent.customMessage
                                        ),
                                        isLoading = false
                                    )
                                }
                            }.onFailure { e ->
                                log.e(e) { "Failed to load pinned event: ${e.message}" }
                                _pinnedContentState.update {
                                    it.copy(error = e.message, isLoading = false)
                                }
                            }
                        }
                    }
                }.onFailure { e ->
                    log.e(e) { "Failed to get pinned content config: ${e.message}" }
                    _pinnedContentState.update {
                        it.copy(error = e.message, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                log.e(e) { "Exception in loadPinnedContent: ${e.message}" }
                _pinnedContentState.update {
                    it.copy(error = e.message, isLoading = false)
                }
            }
        }
    }

    /**
     * Dismiss the currently displayed pinned content.
     * The user won't see this pinned content again until remote config changes to a different item.
     */
    fun dismissPinnedContent() {
        viewModelScope.launch {
            val currentPinned = _pinnedContentState.value.data
            if (currentPinned != null) {
                log.i { "User dismissed pinned content: ${currentPinned.config.id}" }
                pinnedContentPreferences.dismissPinnedContent(currentPinned.config.id)
                _pinnedContentState.update {
                    it.copy(data = null)
                }
            }
        }
    }
}
