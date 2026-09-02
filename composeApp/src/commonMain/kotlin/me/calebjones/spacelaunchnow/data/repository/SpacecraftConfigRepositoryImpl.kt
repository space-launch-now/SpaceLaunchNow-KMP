package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationDetails
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationsByAgency
import me.calebjones.spacelaunchnow.api.trantor.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig

/**
 * Implementation of SpacecraftConfigRepository using the Trantor SpacecraftConfigurationsApi.
 *
 * Per ADR-0001, the raw Trantor response types are never named explicitly in this file
 * (only inferred); model access goes through `api.extensions` and `domain.mapper`.
 *
 * NOTE (escalation): the `human_rated` / `in_use` filters this class used to pass to Launch
 * Library are deferred on the Trantor side per the phase5 contract, so they are no longer
 * sent as query params (client-side filtering is out of scope for this unit).
 */
class SpacecraftConfigRepositoryImpl(
    private val spacecraftConfigurationsApi: SpacecraftConfigurationsApi
) : SpacecraftConfigRepository {

    private suspend fun getConfigurationsByAgencyRaw(agencyId: Int, limit: Int, offset: Int) =
        try {
            val response = spacecraftConfigurationsApi.getConfigurationsByAgency(
                agencyId = agencyId,
                limit = limit,
                offset = offset
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun getConfigurationsRaw(limit: Int, offset: Int, search: String?) =
        try {
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

    private suspend fun getConfigurationDetailsRaw(configId: Int) =
        try {
            val response = spacecraftConfigurationsApi.getConfigurationDetails(id = configId)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
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
