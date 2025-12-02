package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.data.repository.ArticlesRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Manages news and updates feed data for the home screen:
 * - Launch Library updates/news feed
 * - SNAPI news articles
 * 
 * This ViewModel follows the Single Responsibility Principle by focusing only on feed content.
 * It uses the ViewState pattern for consistent state management.
 */
class FeedViewModel(
    private val updatesRepository: UpdatesRepository,
    private val articlesRepository: ArticlesRepository
) : ViewModel() {

    private val log = logger()

    // ========== ViewState Properties ==========

    private val _updatesState = MutableStateFlow(ViewState(data = emptyList<UpdateEndpoint>()))
    val updatesState: StateFlow<ViewState<List<UpdateEndpoint>>> = _updatesState.asStateFlow()

    private val _articlesState = MutableStateFlow(ViewState(data = emptyList<Article>()))
    val articlesState: StateFlow<ViewState<List<Article>>> = _articlesState.asStateFlow()

    // ========== Public API ==========

    /**
     * Loads Launch Library updates/news feed
     * 
     * @param limit Number of updates to load (default: 10)
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadUpdates(limit: Int = 10, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _updatesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = updatesRepository.getLatestUpdates(limit, forceRefresh)

                result.onSuccess { dataResult ->
                    log.i { "Received updates - Total: ${dataResult.data.count}" }

                    _updatesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _updatesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _updatesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Loads SNAPI news articles
     * 
     * @param limit Number of articles to load (default: 5)
     * @param forceRefresh If true, bypass cache (user-initiated refresh)
     */
    fun loadArticles(limit: Int = 5, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _articlesState.update {
                    it.copy(isLoading = true, isUserInitiated = forceRefresh, error = null)
                }

                val result = articlesRepository.getArticles(limit, forceRefresh)

                result.onSuccess { dataResult ->
                    log.i { "Received articles - Total: ${dataResult.data.count}" }

                    _articlesState.update {
                        it.copy(
                            data = dataResult.data.results,
                            isLoading = false,
                            dataSource = dataResult.source,
                            cacheTimestamp = dataResult.timestamp
                        )
                    }
                }.onFailure { exception ->
                    _articlesState.update {
                        it.copy(
                            error = formatErrorMessage(exception),
                            isLoading = false
                        )
                    }
                }
            } catch (exception: Exception) {
                _articlesState.update {
                    it.copy(
                        error = exception.message ?: "Unknown error",
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Refreshes both updates and articles (user-initiated)
     */
    fun refreshAll() {
        viewModelScope.launch {
            launch { loadUpdates(forceRefresh = true) }
            launch { loadArticles(forceRefresh = true) }
        }
    }

    // ========== Private Helper Methods ==========

    /**
     * Formats error messages consistently
     */
    private fun formatErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("throttled") == true ->
                "API rate limit exceeded. Please try again later."

            exception.message?.contains("API Error") == true ->
                exception.message!!.substringAfter("API Error: ")

            else -> exception.message ?: "Unknown error occurred"
        }
    }
}
