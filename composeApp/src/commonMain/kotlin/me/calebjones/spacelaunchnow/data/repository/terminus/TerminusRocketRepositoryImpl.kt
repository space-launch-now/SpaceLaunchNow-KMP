package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Rocket repository for the Terminus backend. A "rocket" here is a launcher configuration, as in
 * the LL repository: lists use `GET /api/v1/launcher_configurations/` (normal variant) and detail
 * uses `GET /api/v1/launcher_configurations/{id}/`. The Terminus `/rockets/` endpoint is a
 * per-launch rocket, not a configuration, so it is not used. Every [RocketRepository] method is
 * served by Terminus; nothing delegates to LL.
 */
class TerminusRocketRepositoryImpl internal constructor(
    private val api: LauncherConfigurationsApi,
    private val cache: TerminusMemoryCache
) : RocketRepository {

    constructor(api: LauncherConfigurationsApi) : this(api, TerminusMemoryCache())

    private val log = logger()

    private suspend fun <T : Any> read(
        key: String,
        what: String,
        fetch: suspend () -> HttpResponse<T>
    ): Result<TerminusFetched<T>> {
        log.d { "terminus read $what" }
        return terminusMemoryCachedFetch(cache, key, what, forceRefresh = false, fetch = fetch)
    }

    override suspend fun getRocketsDomain(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        programIds: List<Int>?,
        familyIds: List<Int>?,
        active: Boolean?,
        reusable: Boolean?
    ): Result<PaginatedResult<VehicleConfig>> =
        read(
            "rockets?limit=$limit&offset=$offset&ordering=$ordering&search=$search&program=$programIds&families=$familyIds&active=$active&reusable=$reusable",
            "rockets"
        ) {
            api.launcherConfigurationsList(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                active = active,
                reusable = reusable,
                program = programIds,
                families = familyIds
            )
        }.mapCatching { it.value.toDomain() }

    override suspend fun getRocketDetailsDomain(id: Int): Result<VehicleConfig> =
        read("rocket-detail?id=$id", "rocket $id") { api.launcherConfigurationsRetrieve(id) }
            .mapCatching { it.value.toVehicleDomain() }
}
