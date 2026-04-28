package me.calebjones.spacelaunchnow.data.repository

import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

class FakeLaunchRepository : LaunchRepository {

    // -- Configurable results for domain methods --------------------------

    var upcomingLaunchesDomainResult: Result<PaginatedResult<Launch>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var previousLaunchesDomainResult: Result<PaginatedResult<Launch>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var featuredLaunchDomainResult: Result<DataResult<PaginatedResult<Launch>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var inFlightLaunchesDomainResult: Result<DataResult<PaginatedResult<Launch>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var upcomingLaunchesNormalDomainResult: Result<DataResult<PaginatedResult<Launch>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var previousLaunchesNormalDomainResult: Result<DataResult<PaginatedResult<Launch>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var launchDetailDomainResult: Result<Launch>? = null

    var starshipLaunchesDomainResult: Result<PaginatedResult<Launch>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var starshipHistoryDomainResult: Result<DataResult<PaginatedResult<Launch>>> =
        Result.success(DataResult(PaginatedResult(count = 0, next = null, previous = null), DataSource.NETWORK))

    var launchByIdDomainResult: Result<Launch?> = Result.success(null)

    var shouldFail = false
    private val failureException = Exception("FakeLaunchRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getUpcomingLaunchesDomainCalled = false
    var getPreviousLaunchesDomainCalled = false
    var getFeaturedLaunchDomainCalled = false
    var getInFlightLaunchesDomainCalled = false
    var getUpcomingLaunchesNormalDomainCalled = false
    var getPreviousLaunchesNormalDomainCalled = false
    var getLaunchDetailDomainCalled = false
    var getStarshipLaunchesDomainCalled = false
    var getStarshipHistoryDomainCalled = false
    var getLaunchByIdDomainCalled = false

    var lastLaunchDetailId: String? = null
    var lastLaunchByIdId: String? = null
    var lastUpcomingAgencyIds: List<Int>? = null
    var lastUpcomingLocationIds: List<Int>? = null
    var lastPreviousAgencyIds: List<Int>? = null
    var lastPreviousLocationIds: List<Int>? = null

    // -- Domain-returning methods -----------------------------------------

    override suspend fun getUpcomingLaunchesDomain(
        limit: Int,
        offset: Int,
        netGt: Instant?,
        netLt: Instant?
    ): Result<PaginatedResult<Launch>> {
        getUpcomingLaunchesDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return upcomingLaunchesDomainResult
    }

    override suspend fun getPreviousLaunchesDomain(limit: Int, offset: Int): Result<PaginatedResult<Launch>> {
        getPreviousLaunchesDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return previousLaunchesDomainResult
    }

    override suspend fun getFeaturedLaunchDomain(
        forceRefresh: Boolean, agencyIds: List<Int>?, locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        getFeaturedLaunchDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return featuredLaunchDomainResult
    }

    override suspend fun getInFlightLaunchesDomain(
        forceRefresh: Boolean, agencyIds: List<Int>?, locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        getInFlightLaunchesDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return inFlightLaunchesDomainResult
    }

    override suspend fun getUpcomingLaunchesNormalDomain(
        limit: Int, forceRefresh: Boolean, agencyIds: List<Int>?, locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        getUpcomingLaunchesNormalDomainCalled = true
        lastUpcomingAgencyIds = agencyIds
        lastUpcomingLocationIds = locationIds
        if (shouldFail) return Result.failure(failureException)
        return upcomingLaunchesNormalDomainResult
    }

    override suspend fun getPreviousLaunchesNormalDomain(
        limit: Int, forceRefresh: Boolean, agencyIds: List<Int>?, locationIds: List<Int>?
    ): Result<DataResult<PaginatedResult<Launch>>> {
        getPreviousLaunchesNormalDomainCalled = true
        lastPreviousAgencyIds = agencyIds
        lastPreviousLocationIds = locationIds
        if (shouldFail) return Result.failure(failureException)
        return previousLaunchesNormalDomainResult
    }

    override suspend fun getLaunchDetailDomain(id: String, forceRefresh: Boolean): Result<Launch> {
        getLaunchDetailDomainCalled = true
        lastLaunchDetailId = id
        if (shouldFail) return Result.failure(failureException)
        return launchDetailDomainResult ?: Result.failure(Exception("No launchDetailDomainResult configured"))
    }

    override suspend fun getStarshipLaunchesDomain(
        limit: Int, forceRefresh: Boolean, programId: List<Int>?
    ): Result<PaginatedResult<Launch>> {
        getStarshipLaunchesDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return starshipLaunchesDomainResult
    }

    override suspend fun getStarshipHistoryDomain(
        limit: Int, forceRefresh: Boolean
    ): Result<DataResult<PaginatedResult<Launch>>> {
        getStarshipHistoryDomainCalled = true
        if (shouldFail) return Result.failure(failureException)
        return starshipHistoryDomainResult
    }

    override suspend fun getLaunchByIdDomain(id: String): Result<Launch?> {
        getLaunchByIdDomainCalled = true
        lastLaunchByIdId = id
        if (shouldFail) return Result.failure(failureException)
        return launchByIdDomainResult
    }

    override suspend fun getFilteredLaunchesDomain(
        limit: Int,
        offset: Int,
        upcoming: Boolean?,
        previous: Boolean?,
        ordering: String?,
        search: String?,
        lspIds: List<Int>?,
        locationIds: List<Int>?,
        programIds: List<Int>?,
        rocketConfigurationId: Int?,
        isCrewed: Boolean?,
        includeSuborbital: Boolean?,
        statusIds: List<Int>?,
        orbitIds: List<Int>?,
        missionTypeIds: List<Int>?,
        launcherConfigFamilyIds: List<Int>?
    ): Result<PaginatedResult<Launch>> {
        if (shouldFail) return Result.failure(failureException)
        // Default to an empty page so ViewModels under test reach their success path.
        return Result.success(PaginatedResult(count = 0, next = null, previous = null))
    }

    // -- Non-deprecated legacy passthroughs (API types kept on interface) --

    override suspend fun getLaunchesByDayAndMonth(day: Int, month: Int, limit: Int): Result<PaginatedLaunchNormalList> =
        Result.failure(NotImplementedError("Not wired in fake"))

    override suspend fun getStaleDetailedLaunch(id: String): LaunchDetailed? = null

    override suspend fun getAgencyDetails(id: Int): Result<AgencyEndpointDetailed> =
        Result.failure(NotImplementedError("Not wired in fake"))

    override suspend fun getNextDetailedLaunch(limit: Int): Result<PaginatedLaunchDetailedList> =
        Result.failure(NotImplementedError("Not wired in fake"))

    override suspend fun getNextNormalLaunch(limit: Int): Result<PaginatedLaunchNormalList> =
        Result.failure(NotImplementedError("Not wired in fake"))

    override suspend fun getStatsCount(key: String, netGt: Instant, netLt: Instant, forceRefresh: Boolean): Result<DataResult<Int>> =
        Result.failure(NotImplementedError("Not wired in fake"))
}