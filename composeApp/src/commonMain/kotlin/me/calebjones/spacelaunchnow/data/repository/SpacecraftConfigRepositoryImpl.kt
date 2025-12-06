package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationDetails
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationsByAgency
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed

/**
 * Implementation of SpacecraftConfigRepository using the generated SpacecraftConfigurationsApi
 */
class SpacecraftConfigRepositoryImpl(
    private val spacecraftConfigurationsApi: SpacecraftConfigurationsApi
) : SpacecraftConfigRepository {

    override suspend fun getConfigurationsByAgency(
        agencyId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedSpacecraftConfigDetailedList> {
        return try {
            println("=== SpacecraftConfigRepository.getConfigurationsByAgency ===")
            println("Parameters: agencyId=$agencyId, limit=$limit, offset=$offset")

            val response = spacecraftConfigurationsApi.getConfigurationsByAgency(
                agencyId = agencyId,
                limit = limit,
                offset = offset,
                ordering = ""
            )

            val configs = response.body()
            println("✓ API SUCCESS: Fetched ${configs.results.size} spacecraft configs for agency $agencyId")

            Result.success(configs)
        } catch (e: ResponseException) {
            println("SpacecraftConfigRepository: API error: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("SpacecraftConfigRepository: Network error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("SpacecraftConfigRepository: Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getConfigurations(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedSpacecraftConfigDetailedList> {
        return try {
            println("=== SpacecraftConfigRepository.getConfigurations ===")
            println("Parameters: limit=$limit, offset=$offset, search=$search")

            val response = spacecraftConfigurationsApi.getConfigurationsByAgency(
                agencyId = null,
                limit = limit,
                offset = offset,
                search = search
            )

            val configs = response.body()
            println("✓ API SUCCESS: Fetched ${configs.results.size} spacecraft configs")

            Result.success(configs)
        } catch (e: ResponseException) {
            println("SpacecraftConfigRepository: API error: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("SpacecraftConfigRepository: Network error: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("SpacecraftConfigRepository: Unexpected error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getConfigurationDetails(configId: Int): Result<SpacecraftConfigDetailed> {
        return try {
            println("=== SpacecraftConfigRepository.getConfigurationDetails ===")
            println("Parameters: configId=$configId")

            val response = spacecraftConfigurationsApi.getConfigurationDetails(id = configId)
            val config = response.body()

            println("✓ API SUCCESS: Fetched spacecraft config details for ID $configId")

            Result.success(config)
        } catch (e: ResponseException) {
            println("SpacecraftConfigRepository: API error for config $configId: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("SpacecraftConfigRepository: Network error for config $configId: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("SpacecraftConfigRepository: Unexpected error for config $configId: ${e.message}")
            Result.failure(e)
        }
    }
}
