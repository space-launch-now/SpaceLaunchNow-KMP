package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LiveOnboarding
import me.calebjones.spacelaunchnow.navigation.Onboarding
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.util.isLowRamDevice
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

enum class PreloadTier {
    CRITICAL,
    WARM_CACHE
}

data class PreloadTask(
    val name: String,
    val tier: PreloadTier,
    val execute: suspend () -> Unit
)

data class PreloadState(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val isComplete: Boolean = false,
    val nextDestination: Any? = null
)

class PreloadViewModel(
    private val launchRepository: LaunchRepository,
    private val articlesRepository: ArticlesRepository,
    private val eventsRepository: EventsRepository,
    private val updatesRepository: UpdatesRepository,
    private val astronautRepository: AstronautRepository,
    private val rocketRepository: RocketRepository,
    private val agencyRepository: AgencyRepository,
    private val scheduleFilterRepository: ScheduleFilterRepository,
    private val spaceStationRepository: SpaceStationRepository,
    private val notificationStateStorage: NotificationStateStorage,
    private val launchFilterService: LaunchFilterService,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val log = logger()

    private val _preloadState = MutableStateFlow(PreloadState())
    val preloadState: StateFlow<PreloadState> = _preloadState.asStateFlow()

    // Scope that survives ViewModel clearing — Tier 2 tasks run here
    private val backgroundScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    fun startPreload(isNewUser: Boolean) {
        viewModelScope.launch {
            val liveOnboardingCompleted = appPreferences.liveOnboardingCompletedFlow.first()
            val onboardingPaywallShown = appPreferences.onboardingPaywallV1ShownFlow.first()

            val nextDestination: Any = when {
                liveOnboardingCompleted == false -> LiveOnboarding
                onboardingPaywallShown == true -> Home
                else -> Onboarding
            }

            // Detect low-RAM devices for memory-aware throttling
            val isLowRam = isLowRamDevice()
            // On low-RAM: limit to 2 concurrent network requests; normal: 4
            val tier1Concurrency = if (isLowRam) 2 else 4
            // On low-RAM: limit to 2 concurrent; normal: 6
            val tier2Concurrency = if (isLowRam) 2 else 6

            val tier1Tasks = if (isNewUser) buildTier1Tasks() else emptyList()
            // On low-RAM devices, skip tier 2 entirely to preserve memory for UI rendering
            val tier2Tasks = if (isLowRam) emptyList() else buildTier2Tasks()

            _preloadState.update {
                it.copy(
                    totalTasks = tier1Tasks.size,
                    completedTasks = 0,
                    isComplete = false,
                    nextDestination = nextDestination
                )
            }

            log.i { "Starting preload: ${tier1Tasks.size} tier1, ${tier2Tasks.size} tier2 (isNewUser=$isNewUser, lowRam=$isLowRam, next=$nextDestination)" }

            // Tier 1: must complete before navigation (throttled on low-RAM)
            if (tier1Tasks.isNotEmpty()) {
                val tier1Timeout = 15_000L
                val semaphore = Semaphore(tier1Concurrency)
                val deferredTier1 = tier1Tasks.map { task ->
                    async {
                        semaphore.withPermit {
                            try {
                                task.execute()
                                log.d { "✅ Tier 1 complete: ${task.name}" }
                            } catch (e: Exception) {
                                log.w(e) { "⚠️ Tier 1 failed: ${task.name}" }
                            } finally {
                                _preloadState.update { state ->
                                    val newCompleted = state.completedTasks + 1
                                    state.copy(
                                        completedTasks = newCompleted,
                                        isComplete = newCompleted >= state.totalTasks
                                    )
                                }
                            }
                        }
                    }
                }

                withTimeoutOrNull(tier1Timeout) {
                    deferredTier1.awaitAll()
                }

                val tier1State = _preloadState.value
                log.i { "Tier 1 finished: ${tier1State.completedTasks}/${tier1State.totalTasks}" }
            }

            // Ensure navigation happens even if tier1 timed out
            if (!_preloadState.value.isComplete) {
                _preloadState.update { it.copy(isComplete = true) }
            }

            // Tier 2: fire-and-forget in background scope (skipped on low-RAM, throttled otherwise)
            if (tier2Tasks.isNotEmpty()) {
                log.i { "Launching ${tier2Tasks.size} tier 2 tasks in background (concurrency=$tier2Concurrency)" }
                val tier2Semaphore = Semaphore(tier2Concurrency)
                backgroundScope.launch {
                    tier2Tasks.map { task ->
                        async {
                            tier2Semaphore.withPermit {
                                try {
                                    task.execute()
                                    log.d { "✅ Tier 2 complete: ${task.name}" }
                                } catch (e: Exception) {
                                    log.w(e) { "⚠️ Tier 2 failed: ${task.name}" }
                                }
                            }
                        }
                    }.awaitAll()
                    log.i { "All tier 2 tasks finished" }
                }
            } else if (isLowRam) {
                log.i { "Tier 2 skipped on low-RAM device to preserve memory" }
            }
        }
    }

    private fun buildTier1Tasks(): List<PreloadTask> = listOf(
        PreloadTask("Next launch", PreloadTier.CRITICAL) {
            launchRepository.getNextNormalLaunch(1)
        },
        PreloadTask("Upcoming basic launches", PreloadTier.CRITICAL) {
            launchRepository.getUpcomingLaunchesList(5)
        },
        PreloadTask("Previous basic launches", PreloadTier.CRITICAL) {
            launchRepository.getPreviousLaunchesList(5)
        },
        PreloadTask("Articles for onboarding", PreloadTier.CRITICAL) {
            articlesRepository.getArticles(3)
        },
        PreloadTask("Astronauts in space", PreloadTier.CRITICAL) {
            astronautRepository.getAstronauts(limit = 3, inSpace = true)
        },
        PreloadTask("Active rockets", PreloadTier.CRITICAL) {
            rocketRepository.getRockets(limit = 3, active = true)
        },
        PreloadTask("Featured agencies", PreloadTier.CRITICAL) {
            agencyRepository.getAgencies(limit = 3, featured = true)
        }
    )

    private suspend fun buildTier2Tasks(): List<PreloadTask> {
        val filters = try {
            notificationStateStorage.stateFlow.first()
        } catch (e: Exception) {
            log.w(e) { "Failed to load notification state for preload filters" }
            null
        }

        val filterParams = filters?.let { launchFilterService.getFilterParams(it) }
        val agencyIds = filterParams?.agencyIds
        val locationIds = filterParams?.locationIds

        val now = Clock.System.now()
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault())

        return listOf(
            PreloadTask("Featured launch", PreloadTier.WARM_CACHE) {
                launchRepository.getFeaturedLaunch(agencyIds = agencyIds, locationIds = locationIds)
            },
            PreloadTask("Upcoming normal launches", PreloadTier.WARM_CACHE) {
                launchRepository.getUpcomingLaunchesNormal(
                    limit = 8,
                    agencyIds = agencyIds,
                    locationIds = locationIds
                )
            },
            PreloadTask("Previous normal launches", PreloadTier.WARM_CACHE) {
                launchRepository.getPreviousLaunchesNormal(
                    limit = 8,
                    agencyIds = agencyIds,
                    locationIds = locationIds
                )
            },
            PreloadTask("Latest updates", PreloadTier.WARM_CACHE) {
                updatesRepository.getLatestUpdates()
            },
            PreloadTask("Articles", PreloadTier.WARM_CACHE) {
                articlesRepository.getArticles()
            },
            PreloadTask("Upcoming events", PreloadTier.WARM_CACHE) {
                eventsRepository.getUpcomingEvents()
            },
            PreloadTask("History launches", PreloadTier.WARM_CACHE) {
                launchRepository.getLaunchesByDayAndMonth(today.day, today.month.ordinal)
            },
            PreloadTask("Stats: next 24h", PreloadTier.WARM_CACHE) {
                launchRepository.getUpcomingLaunchesList(
                    limit = 1,
                    netGt = now,
                    netLt = now.plus(24.hours)
                )
            },
            PreloadTask("Stats: next week", PreloadTier.WARM_CACHE) {
                launchRepository.getUpcomingLaunchesList(
                    limit = 1,
                    netGt = now,
                    netLt = now.plus(7.days)
                )
            },
            PreloadTask("Stats: next month", PreloadTier.WARM_CACHE) {
                launchRepository.getUpcomingLaunchesList(
                    limit = 1,
                    netGt = now,
                    netLt = now.plus(30.days)
                )
            },
            PreloadTask("Filter: agencies", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getAgencies()
            },
            PreloadTask("Filter: programs", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getPrograms()
            },
            PreloadTask("Filter: rockets", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getRockets()
            },
            PreloadTask("Filter: locations", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getLocations()
            },
            PreloadTask("Filter: statuses", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getStatuses()
            },
            PreloadTask("Filter: orbits", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getOrbits()
            },
            PreloadTask("Filter: mission types", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getMissionTypes()
            },
            PreloadTask("Filter: launcher families", PreloadTier.WARM_CACHE) {
                scheduleFilterRepository.getLauncherConfigFamilies()
            },
            PreloadTask("ISS station details", PreloadTier.WARM_CACHE) {
                spaceStationRepository.prewarmIssCache()
            }
        )
    }
}
