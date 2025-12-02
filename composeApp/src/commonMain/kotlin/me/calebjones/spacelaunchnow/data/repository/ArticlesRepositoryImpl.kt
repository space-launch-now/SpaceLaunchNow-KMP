package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlin.time.Clock
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.api.snapi.extensions.getArticles
import me.calebjones.spacelaunchnow.api.snapi.extensions.getArticlesByLaunch
import me.calebjones.spacelaunchnow.api.snapi.extensions.getFeaturedArticles
import me.calebjones.spacelaunchnow.api.snapi.extensions.searchArticles
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import me.calebjones.spacelaunchnow.data.model.ApiError
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.ArticleLocalDataSource
import me.calebjones.spacelaunchnow.util.logging.logger

class ArticlesRepositoryImpl(
    private val articlesApi: ArticlesApi,
    private val localDataSource: ArticleLocalDataSource? = null
) : ArticlesRepository {

    private val log = logger()
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun parseApiError(rawResponse: String): String {
        return try {
            val apiError = json.decodeFromString<ApiError>(rawResponse)
            apiError.getErrorMessage()
        } catch (e: Exception) {
            // If we can't parse as ApiError, return the raw response
            rawResponse
        }
    }

    override suspend fun getArticles(limit: Int, forceRefresh: Boolean): Result<DataResult<PaginatedArticleList>> {
        return try {
            log.d { "getArticles called - limit: $limit, forceRefresh: $forceRefresh, cacheAvailable: ${localDataSource != null}" }

            val now = Clock.System.now().toEpochMilliseconds()
            val staleTimestamp = localDataSource?.getCacheTimestamp("articles")
            
            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedArticles = localDataSource?.getRecentArticles(limit)
                log.v { "Cache query result: ${cachedArticles?.size ?: 0} articles found" }
                if (cachedArticles != null && cachedArticles.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cachedArticles.size} cached articles" }
                    return Result.success(DataResult(
                        data = PaginatedArticleList(
                            count = cachedArticles.size,
                            next = null,
                            previous = null,
                            results = cachedArticles
                        ),
                        source = DataSource.CACHE,
                        timestamp = staleTimestamp ?: now
                    ))
                } else {
                    log.d { "Cache miss - No cached data available, fetching from API" }
                }
            } else {
                log.d { "Force refresh - Bypassing cache, fetching fresh data from API" }
            }
            
            log.d { "Fetching articles from API - limit: $limit" }

            val response = articlesApi.getArticles(
                limit = limit
            )
            
            val body = response.body()
            log.i { "Successfully fetched ${body.results.size} articles from API" }

            // Cache the results for future use
            localDataSource?.cacheArticles(body.results)
            log.d { "Cached ${body.results.size} articles for future use" }

            Result.success(DataResult(
                data = body,
                source = DataSource.NETWORK,
                timestamp = now
            ))
        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching articles" }
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentArticles(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("articles")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached articles due to API error" }
                return Result.success(DataResult(
                    data = PaginatedArticleList(
                        count = staleCached.size,
                        next = null,
                        previous = null,
                        results = staleCached
                    ),
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error while fetching articles" }
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentArticles(limit)
            val staleTimestamp = localDataSource?.getCacheTimestamp("articles")
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached articles due to network error" }
                return Result.success(DataResult(
                    data = PaginatedArticleList(
                        count = staleCached.size,
                        next = null,
                        previous = null,
                        results = staleCached
                    ),
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching articles" }
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedArticles(limit: Int): Result<PaginatedArticleList> {
        return try {
            val response = articlesApi.getFeaturedArticles(limit = limit)
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error while fetching featured articles" }
            // Try to return stale cache if available
            val staleCached = localDataSource?.getRecentArticles(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached articles for featured" }
                return Result.success(PaginatedArticleList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error while fetching featured articles" }
            // Try to return stale cache if available
            val staleCached = localDataSource?.getRecentArticles(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached articles for featured (network error)" }
                return Result.success(PaginatedArticleList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while fetching featured articles" }
            Result.failure(e)
        }
    }

    override suspend fun getArticlesByLaunch(launchIds: List<String>, limit: Int): Result<PaginatedArticleList> {
        return try {
            val response = articlesApi.getArticlesByLaunch(
                launchIds = launchIds,
                limit = limit
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticleById(id: Int): Result<Article> {
        return try {
            val response = articlesApi.articlesRetrieve(id = id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchArticles(query: String, limit: Int): Result<PaginatedArticleList> {
        return try {
            val response = articlesApi.searchArticles(
                query = query,
                limit = limit
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error while searching articles for query: '$query'" }
            // Try to return stale cache if available (less ideal for search, but better than nothing)
            val staleCached = localDataSource?.getRecentArticles(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached articles for search (warning: not filtered by query)" }
                return Result.success(PaginatedArticleList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error while searching articles for query: '$query'" }
            // Try to return stale cache if available
            val staleCached = localDataSource?.getRecentArticles(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                log.w { "Returning ${staleCached.size} stale cached articles for search (network error)" }
                return Result.success(PaginatedArticleList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error while searching articles for query: '$query'" }
            Result.failure(e)
        }
    }
}
