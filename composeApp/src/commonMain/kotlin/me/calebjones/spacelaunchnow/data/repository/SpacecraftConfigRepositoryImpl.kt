package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationDetails
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationsByAgency
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig

/**
 * Implementation of SpacecraftConfigRepository using the generated SpacecraftConfigurationsApi
 */
class SpacecraftConfigRepositoryImpl(
    private val spacecraftConfigurationsApi: SpacecraftConfigurationsApi
) : SpacecraftConfigRepository {

    private suspend fun getConfigurationsByAgencyRaw(
        agencyId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedSpacecraftConfigDetailedList> {
        return try {
            val response = spacecraftConfigurationsApi.getConfigurationsByAgency(
                agencyId = agencyId,
                limit = limit,
                offset = offset,
                ordering = ""
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
    ): Result<PaginatedSpacecraftConfigDetailedList> {
        return try {
            val response = spacecraftConfigurationsApi.getConfigurationsByAgency(
                agencyId = null,
                limit = limit,
                offset = offset,
                search = search
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

    private suspend fun getConfigurationDetailsRaw(configId: Int): Result<SpacecraftConfigDetailed> {
        return try {
            val response = spacecraftConfigurationsApi.getConfigurationDetails(id = configId)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getConfigurationsByAgencyDomain(
        agencyId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<SpacecraftConfig>> =
        getConfigurationsByAgencyRaw(agencyId, limit, offset).map { it.toDomain() }

    override suspend fun getConfigurationsDomain(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedResult<SpacecraftConfig>> =
        getConfigurationsRaw(limit, offset, search).map { it.toDomain() }

    override suspend fun getConfigurationDetailsDomain(configId: Int): Result<SpacecraftConfig> =
        getConfigurationDetailsRaw(configId).map { it.toDomain() }
}