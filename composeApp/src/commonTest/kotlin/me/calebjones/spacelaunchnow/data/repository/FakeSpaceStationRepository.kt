package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail

class FakeSpaceStationRepository : SpaceStationRepository {

    // ── Configurable results ─────────────────────────────────────────────

    var spaceStationDetailsResult: Result<DataResult<SpaceStationDetail>>? = null
    var expeditionDetailsResult: Result<DataResult<List<ExpeditionDetailItem>>> =
        Result.success(DataResult(emptyList(), DataSource.NETWORK))
    var issTleResult: Result<DataResult<IssTle>>? = null

    var shouldFail = false
    private val failureException = Exception("FakeSpaceStationRepository configured to fail")

    // ── Call tracking ────────────────────────────────────────────────────

    var getSpaceStationDetailsCalled = false
    var getExpeditionDetailsCalled = false
    var getIssTleCalled = false
    var prewarmIssCacheCalled = false

    var lastStationId: Int? = null
    var lastExpeditionIds: List<Int>? = null
    var lastNoradId: String? = null
    var lastForceRefresh: Boolean? = null

    override suspend fun getSpaceStationDetails(
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<SpaceStationDetail>> {
        getSpaceStationDetailsCalled = true
        lastStationId = stationId
        lastForceRefresh = forceRefresh
        if (shouldFail) return Result.failure(failureException)
        return spaceStationDetailsResult
            ?: Result.failure(Exception("No spaceStationDetailsResult configured"))
    }

    override suspend fun getExpeditionDetails(
        expeditionIds: List<Int>,
        stationId: Int,
        forceRefresh: Boolean
    ): Result<DataResult<List<ExpeditionDetailItem>>> {
        getExpeditionDetailsCalled = true
        lastExpeditionIds = expeditionIds
        lastStationId = stationId
        lastForceRefresh = forceRefresh
        if (shouldFail) return Result.failure(failureException)
        return expeditionDetailsResult
    }

    override suspend fun getIssTle(
        noradId: String,
        forceRefresh: Boolean
    ): Result<DataResult<IssTle>> {
        getIssTleCalled = true
        lastNoradId = noradId
        lastForceRefresh = forceRefresh
        if (shouldFail) return Result.failure(failureException)
        return issTleResult ?: Result.failure(Exception("No issTleResult configured"))
    }

    override suspend fun prewarmIssCache() {
        prewarmIssCacheCalled = true
    }
}
