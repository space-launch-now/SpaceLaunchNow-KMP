package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Spacecraft configuration repository for the Terminus backend (Tasks#1012). The lists are
 * `GET /api/v1/spacecraft_configurations/detailed/` (the variant the LL repository uses) and the
 * detail is `GET /api/v1/spacecraft_configurations/{id}/`. Nothing delegates to LL.
 *
 * Reads go through the 30 s [TerminusMemoryCache]; a 5xx or a network failure falls back to the
 * stale entry.
 */
class TerminusSpacecraftConfigRepositoryImpl internal constructor(
    private val spacecraftConfigurationsApi: SpacecraftConfigurationsApi,
    private val cache: TerminusMemoryCache
) : SpacecraftConfigRepository {

    constructor(spacecraftConfigurationsApi: SpacecraftConfigurationsApi) :
        this(spacecraftConfigurationsApi, TerminusMemoryCache())

    private val log = logger()

    override suspend fun getConfigurationsByAgencyDomain(
        agencyId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<SpacecraftConfig>> {
        val key = "spacecraft_configurations?agency=$agencyId&limit=$limit&offset=$offset"
        log.d { "terminus $key" }
        return terminusMemoryCachedFetch(cache, key, "spacecraft configs for agency $agencyId", forceRefresh = false) {
            spacecraftConfigurationsApi.spacecraftConfigurationsDetailedList(
                agency = agencyId,
                limit = limit,
                offset = offset,
                ordering = ""
            )
        }.mapCatching { it.value.toDomain() }
    }

    override suspend fun getConfigurationsDomain(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedResult<SpacecraftConfig>> {
        val key = "spacecraft_configurations?limit=$limit&offset=$offset&search=$search"
        log.d { "terminus $key" }
        return terminusMemoryCachedFetch(cache, key, "spacecraft configs", forceRefresh = false) {
            spacecraftConfigurationsApi.spacecraftConfigurationsDetailedList(
                limit = limit,
                offset = offset,
                search = search
            )
        }.mapCatching { it.value.toDomain() }
    }

    override suspend fun getConfigurationDetailsDomain(configId: Int): Result<SpacecraftConfig> {
        log.d { "terminus spacecraft config detail $configId" }
        return terminusMemoryCachedFetch(cache, "spacecraft_configuration?id=$configId", "spacecraft config $configId", forceRefresh = false) {
            spacecraftConfigurationsApi.spacecraftConfigurationsRetrieve(configId)
        }.mapCatching { it.value.toDomain() }
    }
}
