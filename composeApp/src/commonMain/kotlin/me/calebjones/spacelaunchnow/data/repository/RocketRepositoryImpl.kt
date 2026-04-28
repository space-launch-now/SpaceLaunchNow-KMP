package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getRocketDetails
import me.calebjones.spacelaunchnow.api.extensions.getRocketList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.util.logging.logger

class RocketRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : RocketRepository {

    private val log = logger()

    private suspend fun getRocketsRaw(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        programIds: List<Int>?,
        familyIds: List<Int>?,
        active: Boolean?,
        reusable: Boolean?
    ): Result<PaginatedLauncherConfigNormalList> {
        return try {
            val response = launcherConfigurationsApi.getRocketList(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                active = active,
                reusable = reusable,
                program = programIds,
                families = familyIds
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API ERROR in getRockets: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "NETWORK ERROR in getRockets: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "UNEXPECTED ERROR in getRockets: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun getRocketDetailsRaw(id: Int): Result<LauncherConfigDetailed> {
        return try {
            val response = launcherConfigurationsApi.getRocketDetails(id = id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API ERROR in getRocketDetails: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "NETWORK ERROR in getRocketDetails: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "UNEXPECTED ERROR in getRocketDetails: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getRocketsDomain(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        programIds: List<Int>?,
        familyIds: List<Int>?,
        active: Boolean?,
        reusable: Boolean?
    ): Result<PaginatedResult<VehicleConfig>> =
        getRocketsRaw(limit, offset, ordering, search, programIds, familyIds, active, reusable)
            .map { it.toDomain() }

    override suspend fun getRocketDetailsDomain(id: Int): Result<VehicleConfig> =
        getRocketDetailsRaw(id).map { it.toVehicleDomain() }
}