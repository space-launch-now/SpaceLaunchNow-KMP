package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAstronautStatuses
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Astronaut filter-option repository, backed directly by the Launch Library 2 (LL) API - the
 * pre-Trantor-migration implementation, resurrected as a standalone class so the app can fall
 * back to LL in production via the `DataBackend` revert lever (see Koin wiring). Implements the
 * CURRENT [AstronautFilterRepository] interface, unchanged by the Trantor migration.
 *
 * Mirrors the pre-migration structure exactly: a single `ConfigApi.getAstronautStatuses` LL call
 * with a simple in-memory cache, no persistent local cache involved (this repository never had
 * one, before or after the Trantor migration).
 */
class LLAstronautFilterRepositoryImpl(
    private val configApi: ConfigApi
) : AstronautFilterRepository {

    private val log = logger()

    // In-memory cache for statuses
    private var statusesCache: List<FilterOption>? = null

    override suspend fun getStatuses(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getStatuses - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh && statusesCache != null) {
                log.i { "Cache hit - Returning ${statusesCache!!.size} cached astronaut statuses" }
                return Result.success(statusesCache!!)
            }

            // Fetch from API
            log.d { "Fetching astronaut statuses from API" }
            val response = configApi.getAstronautStatuses(limit = 100)
            val paginatedList = response.body()
            val results = paginatedList.results

            log.d { "Fetched ${results.size} astronaut statuses from API" }

            // Map to FilterOption
            val filterOptions = results.map {
                FilterOption(
                    id = it.id,
                    name = it.name,
                    abbreviation = null
                )
            }

            // Cache the results
            statusesCache = filterOptions

            log.i { "✅ Astronaut statuses loaded and cached: ${filterOptions.size} items" }
            Result.success(filterOptions)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getStatuses: ${e.message}" }

            // Return cache if available
            if (statusesCache != null) {
                log.w { "Using cached data (${statusesCache!!.size} astronaut statuses)" }
                return Result.success(statusesCache!!)
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getStatuses: ${e.message}" }

            // Return cache if available
            if (statusesCache != null) {
                log.w { "Using cached data (${statusesCache!!.size} astronaut statuses)" }
                return Result.success(statusesCache!!)
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getStatuses: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }
}
