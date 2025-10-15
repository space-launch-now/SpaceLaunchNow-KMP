package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.extensions.getLaunchList
import me.calebjones.spacelaunchnow.api.extensions.getLaunchMiniList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.ApiError
import me.calebjones.spacelaunchnow.data.storage.AppPreferences

class LaunchRepositoryImpl(
    private val launchesApi: LaunchesApi,
    private val agenciesApi: AgenciesApi,
    private val appPreferences: AppPreferences
) : LaunchRepository {

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

    override suspend fun getUpcomingLaunchesList(limit: Int): Result<PaginatedLaunchBasicList> {
        return try {
            val response = launchesApi.launchesMiniList(
                limit = limit,
                upcoming = true,
                ordering = "net" // Order by launch time
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

    override suspend fun getUpcomingLaunchesNormal(limit: Int): Result<PaginatedLaunchNormalList> {
        return try {
            val keep24h =
                withContext(Dispatchers.Default) { appPreferences.getKeepLaunchesFor24Hours() }
            val hideTbd = withContext(Dispatchers.Default) { appPreferences.getHideTbdLaunches() }
            val response = if (keep24h) {
                print("Keep24h is true")
                launchesApi.launchesList(
                    limit = limit,
                    upcomingWithRecent = true,
                    ordering = "net"
                )
            } else {
                print("Keep24h is false")
                launchesApi.launchesList(
                    limit = limit,
                    upcoming = true,
                    ordering = "net"
                )
            }
            val launches = response.body()
            val filtered = if (hideTbd) {
                println("HideTBD is true")
                launches.copy(results = launches.results.filterNot { it.status?.id == 8 })
            } else {
                launches
            }
            Result.success(filtered)
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUpcomingLaunchesList(limit: Int, netGt: Instant?, netLt: Instant?): Result<PaginatedLaunchBasicList> {
        return try {
            val response = launchesApi.getLaunchMiniList(
                limit = limit,
                netGt = netGt,
                netLt = netLt,
                ordering = "net"
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

    override suspend fun getPreviousLaunchesNormal(limit: Int): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                ordering = "-net" // Most recent first
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

    override suspend fun getLaunchesByDayAndMonth(day: Int, month: Int, limit: Int): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                previous = true,
                netDay = listOf(day.toDouble()),
                netMonth = listOf(month.toDouble()),
                ordering = "-net" // Most recent first
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

    override suspend fun getLaunchDetails(id: String): Result<LaunchDetailed> {
        return try {
            val response = launchesApi.launchesRetrieve(id)

            // Print raw response for debugging
            val rawResponse = response.response.bodyAsText()
            println("=== LAUNCH DETAILS RAW RESPONSE ===")
            println("Response status: ${response.status}")
            println("Launch ID: $id")
            println("Response length: ${rawResponse.length} characters")
            println("Response:")
            println(rawResponse)
            println("=== END LAUNCH DETAILS RESPONSE ===")

            // Check if it's an error response
            if (response.status >= 400) {
                println("HTTP Error ${response.status}: $rawResponse")
                return Result.failure(Exception("API Error ${response.status}: $rawResponse"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                println("API returned error response: $rawResponse")
                return Result.failure(Exception("API Error: $rawResponse"))
            }

            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException in getLaunchDetails for ID $id: ${e.message}")
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                println("Error response body: $errorBody")
                Result.failure(Exception("API Error: $errorBody"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            println("IOException in getLaunchDetails for ID $id: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Exception in getLaunchDetails for ID $id: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed> {
        return try {
            val response = agenciesApi.agenciesRetrieve(id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getNextLaunch(
        limit: Int
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                limit = limit,
                upcoming = true,
                ordering = "net" // Order by launch time
            )

            // Print raw response for debugging
            val rawResponse = response.response.bodyAsText()
            println("=== FULL API RESPONSE DEBUG ===")
            println("Request URL: ${response.response.request.url}")
            println("Request headers: ${response.response.request.headers}")
            println("Response status: ${response.status}")
            println("Response headers: ${response.response.headers}")
            println("Response length: ${rawResponse.length} characters")
            println("=== FULL RESPONSE BODY ===")
            println(rawResponse)
            println("=== END FULL RESPONSE ===")

            // Check if it's an error response
            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                println("HTTP Error ${response.status}: $errorMessage")
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                println("API returned error response: $errorMessage")
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            // Try to deserialize the response body
            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException in getNextLaunch: ${e.message}")
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                println("Error response body: $errorMessage")
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            println("IOException in getNextLaunch: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Exception in getNextLaunch: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun getNextDetailedLaunch(
        limit: Int
    ): Result<PaginatedLaunchDetailedList> {
        return try {
            val response = launchesApi.launchesDetailedList(
                limit = limit,
                upcomingWithRecent = true,
                ordering = "net"
            )

            // Print raw response for debugging
            val rawResponse = response.response.bodyAsText()
            println("=== FULL API RESPONSE DEBUG ===")
            println("Request URL: ${response.response.request.url}")
            println("Request headers: ${response.response.request.headers}")
            println("Response status: ${response.status}")
            println("Response headers: ${response.response.headers}")
            println("Response length: ${rawResponse.length} characters")
            println("=== FULL RESPONSE BODY ===")
            println(rawResponse)
            println("=== END FULL RESPONSE ===")

            // Check if it's an error response
            if (response.status >= 400) {
                val errorMessage = parseApiError(rawResponse)
                println("HTTP Error ${response.status}: $errorMessage")
                return Result.failure(Exception("API Error ${response.status}: $errorMessage"))
            }

            // Check if response looks like an error (contains "detail" field)
            if (rawResponse.contains("\"detail\"")) {
                val errorMessage = parseApiError(rawResponse)
                println("API returned error response: $errorMessage")
                return Result.failure(Exception("API Error: $errorMessage"))
            }

            // Try to deserialize the response body
            val body = response.body()
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException in getNextLaunch: ${e.message}")
            // Try to get the error response body
            try {
                val errorBody = e.response.bodyAsText()
                val errorMessage = parseApiError(errorBody)
                println("Error response body: $errorMessage")
                Result.failure(Exception("API Error: $errorMessage"))
            } catch (bodyException: Exception) {
                Result.failure(e)
            }
        } catch (e: IOException) {
            println("IOException in getNextLaunch: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Exception in getNextLaunch: ${e::class.simpleName}: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Additional utility methods using the clean extension functions

    suspend fun searchLaunches(
        query: String,
        limit: Int = 20,
        upcoming: Boolean? = null
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                search = query,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getLaunchesByCompany(
        lspIds: List<Int>,
        limit: Int = 20,
        upcoming: Boolean = true
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                lspId = lspIds,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCrewedLaunches(
        limit: Int = 20,
        upcoming: Boolean = true
    ): Result<PaginatedLaunchNormalList> {
        return try {
            val response = launchesApi.getLaunchList(
                isCrewed = true,
                limit = limit,
                upcoming = upcoming,
                ordering = "net"
            )
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
