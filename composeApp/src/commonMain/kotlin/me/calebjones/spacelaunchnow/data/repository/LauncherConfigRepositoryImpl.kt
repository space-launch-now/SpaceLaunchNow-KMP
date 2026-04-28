package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationsByProgram
import me.calebjones.spacelaunchnow.api.extensions.getRocketDetails
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Implementation of LauncherConfigRepository using the generated LauncherConfigurationsApi
 */
class LauncherConfigRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : LauncherConfigRepository {

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
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun getConfigurationDetailsRaw(configId: Int): Result<LauncherConfigDetailed> {
        return try {
            val response = launcherConfigurationsApi.getRocketDetails(id = configId)
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