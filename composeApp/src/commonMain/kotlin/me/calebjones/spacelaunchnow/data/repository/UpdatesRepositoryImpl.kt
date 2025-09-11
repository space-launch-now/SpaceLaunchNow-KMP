package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.extensions.getLatestUpdates
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.data.model.ApiError

class UpdatesRepositoryImpl(private val updatesApi: UpdatesApi) : UpdatesRepository {

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

    override suspend fun getLatestUpdates(limit: Int): Result<PaginatedUpdateEndpointList> {
        return try {
            val response = updatesApi.getLatestUpdates(limit = limit)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
