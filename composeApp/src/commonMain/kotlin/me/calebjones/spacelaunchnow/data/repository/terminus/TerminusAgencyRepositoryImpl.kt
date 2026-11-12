package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Agency repository for the Terminus backend. Lists use `GET /api/v1/agencies/` (the normal
 * variant, as the LL repository does) and detail uses `GET /api/v1/agencies/{id}/`. Every
 * [AgencyRepository] method is served by Terminus; nothing delegates to LL. Reads are cached in
 * memory (see [TerminusEventsRepositoryImpl]).
 */
class TerminusAgencyRepositoryImpl internal constructor(
    private val agenciesApi: AgenciesApi,
    private val cache: TerminusMemoryCache
) : AgencyRepository {

    constructor(agenciesApi: AgenciesApi) : this(agenciesApi, TerminusMemoryCache())

    private val log = logger()

    private suspend fun <T : Any> read(
        key: String,
        what: String,
        fetch: suspend () -> HttpResponse<T>
    ): Result<TerminusFetched<T>> {
        log.d { "terminus read $what" }
        return terminusMemoryCachedFetch(cache, key, what, forceRefresh = false, fetch = fetch)
    }

    override suspend fun getAgenciesDomain(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        featured: Boolean?,
        typeId: Int?,
        countryCode: List<String>?
    ): Result<PaginatedResult<Agency>> =
        read(
            "agencies?limit=$limit&offset=$offset&ordering=$ordering&search=$search&featured=$featured&typeId=$typeId&country=$countryCode",
            "agencies"
        ) {
            agenciesApi.agenciesList(
                limit = limit,
                offset = offset,
                ordering = ordering,
                search = search,
                featured = featured,
                typeId = typeId,
                countryCode = countryCode
            )
        }.mapCatching { it.value.toDomain() }

    override suspend fun searchAgenciesDomain(
        searchQuery: String,
        limit: Int
    ): Result<PaginatedResult<Agency>> =
        read("agencies-search?q=$searchQuery&limit=$limit", "agency search") {
            agenciesApi.agenciesList(limit = limit, search = searchQuery, ordering = "-total_launch_count")
        }.mapCatching { it.value.toDomain() }

    override suspend fun getAgencyDetailDomain(id: Int): Result<Agency> =
        read("agency-detail?id=$id", "agency detail $id") { agenciesApi.agenciesRetrieve(id) }
            .mapCatching { it.value.toDomainAgency() }
}
