package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Test fake for [RocketRepository]. Domain methods are configurable and tracked.
 */
class FakeRocketRepository : RocketRepository {

    // -- Configurable results ---------------------------------------------

    var rocketsDomainResult: Result<PaginatedResult<VehicleConfig>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var rocketDetailsDomainResult: Result<VehicleConfig>? = null

    var shouldFail = false
    private val failureException = Exception("FakeRocketRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getRocketsDomainCalled = false
    var getRocketDetailsDomainCalled = false

    var lastLimit: Int? = null
    var lastOffset: Int? = null
    var lastOrdering: String? = null
    var lastSearch: String? = null
    var lastProgramIds: List<Int>? = null
    var lastFamilyIds: List<Int>? = null
    var lastActive: Boolean? = null
    var lastReusable: Boolean? = null
    var lastId: Int? = null

    var getRocketsDomainCallCount = 0

    // -- Domain methods ---------------------------------------------------

    override suspend fun getRocketsDomain(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        programIds: List<Int>?,
        familyIds: List<Int>?,
        active: Boolean?,
        reusable: Boolean?
    ): Result<PaginatedResult<VehicleConfig>> {
        getRocketsDomainCalled = true
        getRocketsDomainCallCount++
        lastLimit = limit
        lastOffset = offset
        lastOrdering = ordering
        lastSearch = search
        lastProgramIds = programIds
        lastFamilyIds = familyIds
        lastActive = active
        lastReusable = reusable
        if (shouldFail) return Result.failure(failureException)
        return rocketsDomainResult
    }

    override suspend fun getRocketDetailsDomain(id: Int): Result<VehicleConfig> {
        getRocketDetailsDomainCalled = true
        lastId = id
        if (shouldFail) return Result.failure(failureException)
        return rocketDetailsDomainResult
            ?: Result.failure(Exception("No rocketDetailsDomainResult configured"))
    }
}