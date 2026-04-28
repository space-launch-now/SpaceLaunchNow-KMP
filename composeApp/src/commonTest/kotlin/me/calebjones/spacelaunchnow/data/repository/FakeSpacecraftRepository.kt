package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft

class FakeSpacecraftRepository : SpacecraftRepository {

    // -- Configurable results ---------------------------------------------

    var spacecraftByConfigDomainResult: Result<DataResult<List<Spacecraft>>> =
        Result.success(DataResult(emptyList(), DataSource.NETWORK))

    var spacecraftDetailsDomainResult: Result<Spacecraft>? = null

    var spacecraftDomainResult: Result<PaginatedResult<Spacecraft>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var shouldFail = false
    private val failureException = Exception("FakeSpacecraftRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getSpacecraftByConfigDomainCalled = false
    var getSpacecraftDetailsDomainCalled = false
    var getSpacecraftDomainCalled = false

    var lastConfigId: Int? = null
    var lastSpacecraftId: Int? = null
    var lastForceRefresh: Boolean? = null
    var lastIsPlaceholder: Boolean? = null
    var lastLimit: Int? = null
    var lastOffset: Int? = null
    var lastInSpace: Boolean? = null
    var lastSearch: String? = null

    // -- Domain methods ---------------------------------------------------

    override suspend fun getSpacecraftByConfigDomain(
        configId: Int,
        limit: Int,
        forceRefresh: Boolean,
        isPlaceholder: Boolean?
    ): Result<DataResult<List<Spacecraft>>> {
        getSpacecraftByConfigDomainCalled = true
        lastConfigId = configId
        lastLimit = limit
        lastForceRefresh = forceRefresh
        lastIsPlaceholder = isPlaceholder
        if (shouldFail) return Result.failure(failureException)
        return spacecraftByConfigDomainResult
    }

    override suspend fun getSpacecraftDetailsDomain(spacecraftId: Int): Result<Spacecraft> {
        getSpacecraftDetailsDomainCalled = true
        lastSpacecraftId = spacecraftId
        if (shouldFail) return Result.failure(failureException)
        return spacecraftDetailsDomainResult
            ?: Result.failure(Exception("No spacecraftDetailsDomainResult configured"))
    }

    override suspend fun getSpacecraftDomain(
        limit: Int,
        offset: Int,
        inSpace: Boolean?,
        search: String?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<Spacecraft>> {
        getSpacecraftDomainCalled = true
        lastLimit = limit
        lastOffset = offset
        lastInSpace = inSpace
        lastSearch = search
        lastIsPlaceholder = isPlaceholder
        if (shouldFail) return Result.failure(failureException)
        return spacecraftDomainResult
    }
}