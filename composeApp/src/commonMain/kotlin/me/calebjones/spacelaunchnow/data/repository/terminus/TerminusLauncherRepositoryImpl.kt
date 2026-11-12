package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchersApi
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Launcher repository for the Terminus backend. Lists use `GET /api/v1/launchers/detailed/` and
 * detail uses `GET /api/v1/launchers/{id}/`, the same variants the LL repository calls. Like the
 * LL repository, lists always send `is_placeholder=false` (the caller's value is ignored).
 * Every [LauncherRepository] method is served by Terminus; nothing delegates to LL.
 */
class TerminusLauncherRepositoryImpl internal constructor(
    private val api: LaunchersApi,
    private val cache: TerminusMemoryCache
) : LauncherRepository {

    constructor(api: LaunchersApi) : this(api, TerminusMemoryCache())

    override suspend fun getLaunchersDomain(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<LauncherDetail>> =
        cache.terminusRead(
            "launchers?limit=$limit&offset=$offset&search=$search&ordering=$ordering&config=$launcherConfigId",
            "launchers"
        ) {
            api.launchersDetailedList(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                isPlaceholder = false,
                launcherConfigIds = launcherConfigId?.let { listOf(it) }
            )
        }.mapCatching { it.value.toDomain() }

    override suspend fun getLaunchersByConfigDomain(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<LauncherDetail>> =
        getLaunchersDomain(
            limit = limit,
            offset = offset,
            search = null,
            ordering = "-id",
            launcherConfigId = configId,
            isPlaceholder = false
        )

    override suspend fun getLauncherDetailsDomain(launcherId: Int): Result<LauncherDetail> =
        cache.terminusRead("launcher-detail?id=$launcherId", "launcher $launcherId") { api.launchersRetrieve(launcherId) }
            .mapCatching { it.value.toDomain() }
}
