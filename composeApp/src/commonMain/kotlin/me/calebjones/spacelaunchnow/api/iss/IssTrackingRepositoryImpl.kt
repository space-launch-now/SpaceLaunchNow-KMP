package me.calebjones.spacelaunchnow.api.iss

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Implementation of IssTrackingRepository using wheretheiss.at API
 * 
 * API Documentation: https://wheretheiss.at/w/developer
 * Base URL: https://api.wheretheiss.at/v1
 */
class IssTrackingRepositoryImpl(
    httpClientEngine: HttpClientEngine
) : IssTrackingRepository {

    private val client = HttpClient(httpClientEngine) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d("IssTrackingApi") { message }
                }
            }
            level = LogLevel.INFO
        }
    }

    companion object {
        private const val BASE_URL = "https://api.wheretheiss.at/v1"
        private const val ISS_NORAD_ID = 25544
    }

    override suspend fun getCurrentPosition(): Result<IssPosition> {
        return try {
            val position = client.get("$BASE_URL/satellites/$ISS_NORAD_ID").body<IssPosition>()
            Result.success(position)
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("IssTrackingRepositoryImpl", e) { "Error fetching ISS position" }
            Result.failure(e)
        }
    }

    override suspend fun getTleData(): Result<IssTle> {
        return try {
            val tle = client.get("$BASE_URL/satellites/$ISS_NORAD_ID/tles").body<IssTle>()
            Result.success(tle)
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("IssTrackingRepositoryImpl", e) { "Error fetching ISS TLE data" }
            Result.failure(e)
        }
    }

    override suspend fun getPositionsAtTimestamps(timestamps: List<Long>): Result<List<IssPosition>> {
        return try {
            val timestampsParam = timestamps.joinToString(",")
            val positions = client.get("$BASE_URL/satellites/$ISS_NORAD_ID/positions") {
                parameter("timestamps", timestampsParam)
                parameter("units", "kilometers")
            }.body<List<IssPosition>>()
            Result.success(positions)
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e("IssTrackingRepositoryImpl", e) { "Error fetching ISS positions at timestamps" }
            Result.failure(e)
        }
    }
}
