package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.events.AnalyticsEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigNormal
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.util.logging.logger

class OnboardingViewModel(
    private val launchRepository: LaunchRepository,
    private val articlesRepository: ArticlesRepository,
    private val astronautRepository: AstronautRepository,
    private val rocketRepository: RocketRepository,
    private val agencyRepository: AgencyRepository,
    private val analyticsManager: AnalyticsManager
) : ViewModel() {

    private val log = logger()

    // ========== Analytics ==========

    fun trackOnboardingStep(step: Int, completed: Boolean) {
        analyticsManager.track(AnalyticsEvent.OnboardingStep(step = step, completed = completed))
    }

    private val _upcomingLaunches = MutableStateFlow<List<LaunchBasic>>(emptyList())
    val upcomingLaunches: StateFlow<List<LaunchBasic>> = _upcomingLaunches

    private val _previousLaunches = MutableStateFlow<List<LaunchBasic>>(emptyList())
    val previousLaunches: StateFlow<List<LaunchBasic>> = _previousLaunches

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles

    private val _astronauts = MutableStateFlow<List<AstronautEndpointNormal>>(emptyList())
    val astronauts: StateFlow<List<AstronautEndpointNormal>> = _astronauts

    private val _rockets = MutableStateFlow<List<LauncherConfigNormal>>(emptyList())
    val rockets: StateFlow<List<LauncherConfigNormal>> = _rockets

    private val _agencies = MutableStateFlow<List<AgencyNormal>>(emptyList())
    val agencies: StateFlow<List<AgencyNormal>> = _agencies

    fun fetchScheduleData(limit: Int = 5) {
        viewModelScope.launch {
            launchRepository.getUpcomingLaunchesList(limit).onSuccess { paginated ->
                _upcomingLaunches.value = paginated.results
                log.d { "Loaded ${paginated.results.size} upcoming launches for onboarding" }
            }.onFailure { e ->
                log.e(e) { "Failed to load upcoming launches for onboarding" }
            }

            launchRepository.getPreviousLaunchesList(limit).onSuccess { paginated ->
                _previousLaunches.value = paginated.results
                log.d { "Loaded ${paginated.results.size} previous launches for onboarding" }
            }.onFailure { e ->
                log.e(e) { "Failed to load previous launches for onboarding" }
            }
        }
    }

    fun fetchArticles(limit: Int = 3) {
        viewModelScope.launch {
            articlesRepository.getArticles(limit).onSuccess { dataResult ->
                _articles.value = dataResult.data.results
                log.d { "Loaded ${dataResult.data.results.size} articles for onboarding" }
            }.onFailure { e ->
                log.e(e) { "Failed to load articles for onboarding" }
            }
        }
    }

    fun fetchExploreData(limit: Int = 3) {
        viewModelScope.launch {
            astronautRepository.getAstronauts(limit = limit, inSpace = true).onSuccess { paginated ->
                _astronauts.value = paginated.results
                log.d { "Loaded ${paginated.results.size} astronauts for onboarding" }
            }.onFailure { e ->
                log.e(e) { "Failed to load astronauts for onboarding" }
            }

            rocketRepository.getRockets(limit = limit, active = true).onSuccess { paginated ->
                _rockets.value = paginated.results
                log.d { "Loaded ${paginated.results.size} rockets for onboarding" }
            }.onFailure { e ->
                log.e(e) { "Failed to load rockets for onboarding" }
            }

            agencyRepository.getAgencies(limit = limit, featured = true).onSuccess { paginated ->
                _agencies.value = paginated.results
                log.d { "Loaded ${paginated.results.size} agencies for onboarding" }
            }.onFailure { e ->
                log.e(e) { "Failed to load agencies for onboarding" }
            }
        }
    }
}
