package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.extensions.getLatestUpdates
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.data.model.ApiError
import me.calebjones.spacelaunchnow.database.UpdateLocalDataSource

class UpdatesRepositoryImpl(
    private val updatesApi: UpdatesApi,
    private val localDataSource: UpdateLocalDataSource? = null
) : UpdatesRepository {

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

    override suspend fun getLatestUpdates(limit: Int, forceRefresh: Boolean): Result<PaginatedUpdateEndpointList> {
        return try {
            // Try cache first if available and not forcing refresh
            if (!forceRefresh) {
                val cachedUpdates = localDataSource?.getRecentUpdates(limit)
                if (cachedUpdates != null && cachedUpdates.isNotEmpty()) {
                    println("UpdatesRepository: Returning ${cachedUpdates.size} cached updates")
                    return Result.success(PaginatedUpdateEndpointList(
                        count = cachedUpdates.size,
                        next = null,
                        previous = null,
                        results = cachedUpdates
                    ))
                }
            }
            
            println("UpdatesRepository: Fetching updates from API (forceRefresh: $forceRefresh)")
            val response = updatesApi.getLatestUpdates(limit = limit)
            val updates = response.body()
            
            // Cache the results for future use
            localDataSource?.cacheUpdates(updates.results)
            println("UpdatesRepository: Cached ${updates.results.size} updates from API")
            
            Result.success(updates)
        } catch (e: ResponseException) {
            // On error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentUpdates(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("UpdatesRepository: Returning ${staleCached.size} stale cached updates due to API error")
                return Result.success(PaginatedUpdateEndpointList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: IOException) {
            // On network error, try to return stale cache if available
            val staleCached = localDataSource?.getRecentUpdates(limit)
            if (staleCached != null && staleCached.isNotEmpty()) {
                println("UpdatesRepository: Returning ${staleCached.size} stale cached updates due to network error")
                return Result.success(PaginatedUpdateEndpointList(
                    count = staleCached.size,
                    next = null,
                    previous = null,
                    results = staleCached
                ))
            }
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
