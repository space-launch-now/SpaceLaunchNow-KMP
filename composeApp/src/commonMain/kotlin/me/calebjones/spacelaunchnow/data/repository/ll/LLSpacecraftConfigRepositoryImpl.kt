package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getConfigurationDetails
import me.calebjones.spacelaunchnow.api.extensions.ll.getConfigurationsByAgency
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepository
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig

/**
 * Spacecraft configuration repository, backed directly by the Launch Library 2 (LL) API - the
 * pre-Trantor-migration implementation, resurrected as a standalone class so the app can fall
 * back to LL in production via the `DataBackend` revert lever (see Koin wiring). Implements the
 * CURRENT [SpacecraftConfigRepository] interface (domain-typed throughout).
 *
 * Every network result is mapped LL -> domain via the existing `domain/mapper` extension
 * functions declared for LL's `api.launchlibrary.models` types in `SpacecraftMappers.kt`,
 * untouched by the Trantor migration. This repository has no local cache, matching both the
 * current interface (no `DataResult`/stale-cache surface) and the pre-migration implementation.
 */
class LLSpacecraftConfigRepositoryImpl(
    private val spacecraftConfigurationsApi: SpacecraftConfigurationsApi
) : SpacecraftConfigRepository {

    private suspend fun getConfigurationsByAgencyRaw(agencyId: Int, limit: Int, offset: Int) =
        try {
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
