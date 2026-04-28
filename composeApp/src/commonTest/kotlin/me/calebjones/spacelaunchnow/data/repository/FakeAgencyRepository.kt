package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

class FakeAgencyRepository : AgencyRepository {

    // -- Configurable results ---------------------------------------------

    var agenciesDomainResult: Result<PaginatedResult<Agency>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var searchAgenciesDomainResult: Result<PaginatedResult<Agency>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var agencyDetailDomainResult: Result<Agency>? = null

    var shouldFail = false
    private val failureException = Exception("FakeAgencyRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getAgenciesDomainCalled = false
    var searchAgenciesDomainCalled = false
    var getAgencyDetailDomainCalled = false

    var lastLimit: Int? = null
    var lastOffset: Int? = null
    var lastOrdering: String? = null
    var lastSearch: String? = null
    var lastFeatured: Boolean? = null
    var lastTypeId: Int? = null
    var lastCountryCode: List<String>? = null
    var lastSearchQuery: String? = null
    var lastDetailId: Int? = null

    // -- Domain methods ---------------------------------------------------

    override suspend fun getAgenciesDomain(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        featured: Boolean?,
        typeId: Int?,
        countryCode: List<String>?
    ): Result<PaginatedResult<Agency>> {
        getAgenciesDomainCalled = true
        lastLimit = limit
        lastOffset = offset
        lastOrdering = ordering
        lastSearch = search
        lastFeatured = featured
        lastTypeId = typeId
        lastCountryCode = countryCode
        if (shouldFail) return Result.failure(failureException)
        return agenciesDomainResult
    }

    override suspend fun searchAgenciesDomain(
        searchQuery: String,
        limit: Int
    ): Result<PaginatedResult<Agency>> {
        searchAgenciesDomainCalled = true
        lastSearchQuery = searchQuery
        lastLimit = limit
        if (shouldFail) return Result.failure(failureException)
        return searchAgenciesDomainResult
    }

    override suspend fun getAgencyDetailDomain(id: Int): Result<Agency> {
        getAgencyDetailDomainCalled = true
        lastDetailId = id
        if (shouldFail) return Result.failure(failureException)
        return agencyDetailDomainResult
            ?: Result.failure(Exception("No agencyDetailDomainResult configured"))
    }
}