package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAstronautDetail
import me.calebjones.spacelaunchnow.api.extensions.getAstronautList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AstronautsApi
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Implementation of AstronautRepository using the Launch Library API.
 *
 * This repository handles network calls to fetch astronaut data and wraps
 * responses in Result<T> for proper error handling.
 */
class AstronautRepositoryImpl(
    private val astronautsApi: AstronautsApi
) : AstronautRepository {

    private val log = logger()

    override suspend fun getAstronauts(
        limit: Int,
        offset: Int,
        search: String?,
        statusIds: List<Int>?,
        agencyIds: List<Int>?,
        ordering: String?,
        hasFlown: Boolean?,
        inSpace: Boolean?,
        isHuman: Boolean?
    ): Result<PaginatedResult<AstronautListItem>> {
        return try {
            log.d { "getAstronauts - limit: $limit, offset: $offset, search: $search" }
            
            val response = astronautsApi.getAstronautList(
                limit = limit,
                offset = offset,
                search = search,
                statusIds = statusIds?.map { it.toDouble() },
                agencyIds = agencyIds?.map { it.toDouble() },
                ordering = ordering,
                hasFlown = hasFlown,
                inSpace = inSpace,
                isHuman = isHuman
            )
            
            val astronauts = response.body()
            log.i { "✅ API SUCCESS: Fetched ${astronauts.results.size} astronauts (total: ${astronauts.count})" }
            Result.success(astronauts.toDomain())
            
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getAstronauts: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getAstronauts: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getAstronauts: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getAstronautDetail(id: Int): Result<AstronautDetail> {
        return try {
            log.d { "getAstronautDetail - id: $id" }
            
            val response = astronautsApi.getAstronautDetail(id = id)
            val astronaut = response.body()
            
            log.i { "✅ API SUCCESS: Fetched astronaut detail for '${astronaut.name}' (ID: $id)" }
            Result.success(astronaut.toDomainDetail())
            
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getAstronautDetail (ID: $id): ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getAstronautDetail (ID: $id): ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getAstronautDetail (ID: $id): ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }
}
