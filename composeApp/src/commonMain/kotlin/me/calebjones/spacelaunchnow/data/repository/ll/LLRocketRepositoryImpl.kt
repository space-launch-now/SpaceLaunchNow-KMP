package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getRocketDetails
import me.calebjones.spacelaunchnow.api.extensions.ll.getRocketList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Rocket-configuration repository, backed directly by the Launch Library 2 (LL) API - the
 * pre-Trantor-migration implementation, resurrected as a standalone class so the app can fall
 * back to LL in production via the `DataBackend` revert lever (see Koin wiring). Implements the
 * CURRENT [RocketRepository] interface, which is unchanged from the pre-migration one - this
 * class is close to a straight port, moved to the `ll` package and renamed. LL wire types are
 * mapped to domain via the existing `domain/mapper` extension functions (`toDomain`/
 * `toVehicleDomain` for `LauncherConfigDetailed`/`PaginatedLauncherConfigNormalList` in
 * `VehicleMappers.kt`), untouched by the Trantor migration. This class has no local cache -
 * matches the pre-migration behavior.
 */
class LLRocketRepositoryImpl(
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
            log.e(e) { "API ERROR in getRocketsRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "NETWORK ERROR in getRocketsRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "UNEXPECTED ERROR in getRocketsRaw: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun getRocketDetailsRaw(id: Int): Result<LauncherConfigDetailed> {
        return try {
            val response = launcherConfigurationsApi.getRocketDetails(id = id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API ERROR in getRocketDetailsRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "NETWORK ERROR in getRocketDetailsRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "UNEXPECTED ERROR in getRocketDetailsRaw: ${e.message}" }
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
