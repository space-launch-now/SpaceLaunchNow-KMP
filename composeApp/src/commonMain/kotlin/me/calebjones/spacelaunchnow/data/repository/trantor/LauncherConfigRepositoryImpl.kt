package me.calebjones.spacelaunchnow.data.repository.trantor

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getConfiguration
import me.calebjones.spacelaunchnow.api.extensions.trantor.listConfigurations
import me.calebjones.spacelaunchnow.api.trantor.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigFull
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherConfigSummary
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Implementation of LauncherConfigRepository using the Trantor LauncherConfigurationsApi
 * (`GET /configurations`).
 */
class LauncherConfigRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : LauncherConfigRepository {

    private suspend fun getConfigurationsByProgramRaw(
        programId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResponseLauncherConfigSummary> {
        return try {
            val response = launcherConfigurationsApi.listConfigurations(
                programIds = listOf(programId),
                limit = limit,
                offset = offset,
                isPlaceholder = false
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

    private suspend fun getConfigurationsRaw(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedResponseLauncherConfigSummary> {
        return try {
            val response = launcherConfigurationsApi.listConfigurations(
                programIds = null,
                limit = limit,
                offset = offset,
                search = search,
                isPlaceholder = false
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

    private suspend fun getConfigurationDetailsRaw(configId: Int): Result<LauncherConfigFull> {
        return try {
            val response = launcherConfigurationsApi.getConfiguration(configId = configId)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
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
