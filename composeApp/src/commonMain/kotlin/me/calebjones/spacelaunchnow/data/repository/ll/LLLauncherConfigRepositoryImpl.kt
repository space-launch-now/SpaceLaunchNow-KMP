package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getConfigurationsByProgram
import me.calebjones.spacelaunchnow.api.extensions.ll.getRocketDetails
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.domain.mapper.ll.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.ll.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Launcher-configuration (rocket type) repository, backed directly by the Launch Library 2 (LL)
 * API - the pre-Trantor-migration implementation, resurrected as a standalone class so the app
 * can fall back to LL in production via the `DataBackend` revert lever (see Koin wiring).
 * Implements the CURRENT [LauncherConfigRepository] interface, which is unchanged from the
 * pre-migration one - this class is close to a straight port, moved to the `ll` package and
 * renamed. LL wire types are mapped to domain via the existing `domain/mapper` extension
 * functions (`toDomain`/`toVehicleDomain` for `LauncherConfigDetailed`/
 * `PaginatedLauncherConfigDetailedList` in `VehicleMappers.kt`), untouched by the Trantor
 * migration. This class has no local cache - matches the pre-migration behavior.
 */
class LLLauncherConfigRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : LauncherConfigRepository {

    private val log = logger()

    private suspend fun getConfigurationsByProgramRaw(
        programId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedLauncherConfigDetailedList> {
        return try {
            val response = launcherConfigurationsApi.getConfigurationsByProgram(
                programIds = listOf(programId),
                limit = limit,
                offset = offset,
                isPlaceholder = false
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getConfigurationsByProgramRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getConfigurationsByProgramRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getConfigurationsByProgramRaw: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun getConfigurationsRaw(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedLauncherConfigDetailedList> {
        return try {
            val response = launcherConfigurationsApi.getConfigurationsByProgram(
                programIds = null,
                limit = limit,
                offset = offset,
                search = search,
                isPlaceholder = false
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getConfigurationsRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getConfigurationsRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getConfigurationsRaw: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun getConfigurationDetailsRaw(configId: Int): Result<LauncherConfigDetailed> {
        return try {
            val response = launcherConfigurationsApi.getRocketDetails(id = configId)
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getConfigurationDetailsRaw for ID $configId: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getConfigurationDetailsRaw for ID $configId: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getConfigurationDetailsRaw for ID $configId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getConfigurationsByProgramDomain(
        programId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<VehicleConfig>> =
        getConfigurationsByProgramRaw(programId = programId, limit = limit, offset = offset)
            .map { it.toDomain() }

    override suspend fun getConfigurationsDomain(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedResult<VehicleConfig>> =
        getConfigurationsRaw(limit = limit, offset = offset, search = search).map { it.toDomain() }

    override suspend fun getConfigurationDetailsDomain(configId: Int): Result<VehicleConfig> =
        getConfigurationDetailsRaw(configId).map { it.toVehicleDomain() }
}
