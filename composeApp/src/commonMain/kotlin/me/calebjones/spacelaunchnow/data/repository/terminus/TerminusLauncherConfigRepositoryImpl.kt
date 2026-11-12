package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Launcher configuration repository for the Terminus backend. Lists use
 * `GET /api/v1/launcher_configurations/detailed/` and detail uses
 * `GET /api/v1/launcher_configurations/{id}/`, the same variants the LL repository calls.
 * Every [LauncherConfigRepository] method is served by Terminus; nothing delegates to LL.
 */
class TerminusLauncherConfigRepositoryImpl internal constructor(
    private val api: LauncherConfigurationsApi,
    private val cache: TerminusMemoryCache
) : LauncherConfigRepository {

    constructor(api: LauncherConfigurationsApi) : this(api, TerminusMemoryCache())

    override suspend fun getConfigurationsByProgramDomain(
        programId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<VehicleConfig>> =
        cache.terminusRead("launcher-configs?program=$programId&limit=$limit&offset=$offset", "launcher configs for program $programId") {
            api.launcherConfigurationsDetailedList(
                program = listOf(programId),
                limit = limit,
                offset = offset,
                isPlaceholder = false
            )
        }.mapCatching { it.value.toDomain() }

    override suspend fun getConfigurationsDomain(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedResult<VehicleConfig>> =
        cache.terminusRead("launcher-configs?limit=$limit&offset=$offset&search=$search", "launcher configs") {
            api.launcherConfigurationsDetailedList(
                limit = limit,
                offset = offset,
                search = search,
                isPlaceholder = false
            )
        }.mapCatching { it.value.toDomain() }

    override suspend fun getConfigurationDetailsDomain(configId: Int): Result<VehicleConfig> =
        cache.terminusRead("launcher-config-detail?id=$configId", "launcher config $configId") {
            api.launcherConfigurationsRetrieve(configId)
        }.mapCatching { it.value.toVehicleDomain() }
}
