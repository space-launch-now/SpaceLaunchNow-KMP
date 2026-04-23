package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

class FakeLauncherRepository : LauncherRepository {

    // -- Configurable results ---------------------------------------------

    var launchersDomainResult: Result<PaginatedResult<LauncherDetail>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var launchersByConfigDomainResult: Result<PaginatedResult<LauncherDetail>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var launcherDetailsDomainResult: Result<LauncherDetail>? = null

    var shouldFail = false
    private val failureException = Exception("FakeLauncherRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getLaunchersDomainCalled = false
    var getLaunchersByConfigDomainCalled = false
    var getLauncherDetailsDomainCalled = false

    var lastLimit: Int? = null
    var lastOffset: Int? = null
    var lastSearch: String? = null
    var lastOrdering: String? = null
    var lastLauncherConfigId: Int? = null
    var lastIsPlaceholder: Boolean? = null
    var lastConfigId: Int? = null
    var lastLauncherId: Int? = null

    // -- Domain methods ---------------------------------------------------

    override suspend fun getLaunchersDomain(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<LauncherDetail>> {
        getLaunchersDomainCalled = true
        lastLimit = limit
        lastOffset = offset
        lastSearch = search
        lastOrdering = ordering
        lastLauncherConfigId = launcherConfigId
        lastIsPlaceholder = isPlaceholder
        if (shouldFail) return Result.failure(failureException)
        return launchersDomainResult
    }

    override suspend fun getLaunchersByConfigDomain(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<LauncherDetail>> {
        getLaunchersByConfigDomainCalled = true
        lastConfigId = configId
        lastLimit = limit
        lastOffset = offset
        if (shouldFail) return Result.failure(failureException)
        return launchersByConfigDomainResult
    }

    override suspend fun getLauncherDetailsDomain(launcherId: Int): Result<LauncherDetail> {
        getLauncherDetailsDomainCalled = true
        lastLauncherId = launcherId
        if (shouldFail) return Result.failure(failureException)
        return launcherDetailsDomainResult
            ?: Result.failure(Exception("No launcherDetailsDomainResult configured"))
    }
}

class FakeLauncherConfigRepository : LauncherConfigRepository {

    // -- Configurable results ---------------------------------------------

    var configurationsByProgramDomainResult: Result<PaginatedResult<VehicleConfig>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var configurationsDomainResult: Result<PaginatedResult<VehicleConfig>> =
        Result.success(PaginatedResult(count = 0, next = null, previous = null))

    var configurationDetailsDomainResult: Result<VehicleConfig>? = null

    var shouldFail = false
    private val failureException = Exception("FakeLauncherConfigRepository configured to fail")

    // -- Call tracking ----------------------------------------------------

    var getConfigurationsByProgramDomainCalled = false
    var getConfigurationsDomainCalled = false
    var getConfigurationDetailsDomainCalled = false

    var lastProgramId: Int? = null
    var lastLimit: Int? = null
    var lastOffset: Int? = null
    var lastSearch: String? = null
    var lastConfigId: Int? = null

    // -- Domain methods ---------------------------------------------------

    override suspend fun getConfigurationsByProgramDomain(
        programId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<VehicleConfig>> {
        getConfigurationsByProgramDomainCalled = true
        lastProgramId = programId
        lastLimit = limit
        lastOffset = offset
        if (shouldFail) return Result.failure(failureException)
        return configurationsByProgramDomainResult
    }

    override suspend fun getConfigurationsDomain(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedResult<VehicleConfig>> {
        getConfigurationsDomainCalled = true
        lastLimit = limit
        lastOffset = offset
        lastSearch = search
        if (shouldFail) return Result.failure(failureException)
        return configurationsDomainResult
    }

    override suspend fun getConfigurationDetailsDomain(configId: Int): Result<VehicleConfig> {
        getConfigurationDetailsDomainCalled = true
        lastConfigId = configId
        if (shouldFail) return Result.failure(failureException)
        return configurationDetailsDomainResult
            ?: Result.failure(Exception("No configurationDetailsDomainResult configured"))
    }
}