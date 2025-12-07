package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationsByProgram
import me.calebjones.spacelaunchnow.api.extensions.getRocketDetails
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList

/**
 * Implementation of LauncherConfigRepository using the generated LauncherConfigurationsApi
 */
class LauncherConfigRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : LauncherConfigRepository {

    override suspend fun getConfigurationsByProgram(
        programId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedLauncherConfigDetailedList> {
        return try {
            println("=== LauncherConfigRepository.getConfigurationsByProgram ===")
            println("Parameters: programId=$programId, limit=$limit, offset=$offset")

            val response = launcherConfigurationsApi.getConfigurationsByProgram(
                programIds = listOf(programId),
                limit = limit,
                offset = offset,
                isPlaceholder = false
            )

            val configs = response.body()
            println("✓ API SUCCESS: Fetched ${configs.results.size} launcher configs for program $programId")

            Result.success(configs)
        } catch (e: ResponseException) {
            println("LauncherConfigRepository: API error: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("LauncherConfigRepository: Network error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("LauncherConfigRepository: Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getConfigurations(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedLauncherConfigDetailedList> {
        return try {
            println("=== LauncherConfigRepository.getConfigurations ===")
            println("Parameters: limit=$limit, offset=$offset, search=$search")

            val response = launcherConfigurationsApi.getConfigurationsByProgram(
                programIds = null,
                limit = limit,
                offset = offset,
                search = search,
                isPlaceholder = false
            )

            val configs = response.body()
            println("✓ API SUCCESS: Fetched ${configs.results.size} launcher configs")

            Result.success(configs)
        } catch (e: ResponseException) {
            println("LauncherConfigRepository: API error: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("LauncherConfigRepository: Network error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("LauncherConfigRepository: Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getConfigurationDetails(configId: Int): Result<LauncherConfigDetailed> {
        return try {
            println("=== LauncherConfigRepository.getConfigurationDetails ===")
            println("Parameters: configId=$configId")

            val response = launcherConfigurationsApi.getRocketDetails(id = configId)
            val config = response.body()

            println("✓ API SUCCESS: Fetched launcher config details for ID $configId")

            Result.success(config)
        } catch (e: ResponseException) {
            println("LauncherConfigRepository: API error for config $configId: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("LauncherConfigRepository: Network error for config $configId: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("LauncherConfigRepository: Unexpected error for config $configId: ${e.message}")
            Result.failure(e)
        }
    }
}
