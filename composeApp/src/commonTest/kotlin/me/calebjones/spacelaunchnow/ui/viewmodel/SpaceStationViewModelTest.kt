package me.calebjones.spacelaunchnow.ui.viewmodel

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.api.iss.IssPosition
import me.calebjones.spacelaunchnow.api.iss.IssTle
import me.calebjones.spacelaunchnow.api.iss.IssTrackingRepository
import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.FakeSpaceStationRepository
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.ExpeditionMiniItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * T162: Unit tests for [SpaceStationViewModel].
 *
 * Tests exercise the domain-typed [SpaceStationRepository] contract only. The
 * ISS tracking code path is intentionally avoided by using a non-ISS station
 * id (anything other than [ISS_STATION_ID] = 4), which keeps [HttpClient] /
 * [IssTrackingRepository] / YouTube lookup out of the assertion surface.
 *
 * The SNAPI [ArticlesApi] is backed by a [MockEngine] that returns an empty
 * result so the side-effect article fetch completes cleanly without network.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpaceStationViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchStationDetails_success_populatesStationAndExpeditions() = runTest(dispatcher) {
        val expedition = sampleExpeditionMini(id = 71)
        val expeditionDetail = sampleExpeditionDetail(id = 71)
        val repository = FakeSpaceStationRepository().apply {
            spaceStationDetailsResult = Result.success(
                DataResult(
                    data = sampleStationDetail(
                        id = NON_ISS_STATION_ID,
                        activeExpeditions = listOf(expedition)
                    ),
                    source = DataSource.NETWORK
                )
            )
            expeditionDetailsResult = Result.success(
                DataResult(listOf(expeditionDetail), DataSource.NETWORK)
            )
        }

        val viewModel = createViewModel(repository)

        viewModel.fetchStationDetails(NON_ISS_STATION_ID)
        advanceUntilIdle()

        assertTrue(repository.getSpaceStationDetailsCalled)
        assertEquals(NON_ISS_STATION_ID, repository.lastStationId)
        assertNotNull(viewModel.stationDetails.value)
        assertEquals("Tiangong", viewModel.stationDetails.value?.name)
        assertEquals(listOf(expeditionDetail), viewModel.activeExpeditions.value)
        assertNull(viewModel.error.value)
        assertTrue(repository.getExpeditionDetailsCalled)
        assertEquals(listOf(71), repository.lastExpeditionIds)
    }

    @Test
    fun fetchStationDetails_failure_setsError() = runTest(dispatcher) {
        val repository = FakeSpaceStationRepository().apply {
            spaceStationDetailsResult = Result.failure(RuntimeException("boom"))
        }

        val viewModel = createViewModel(repository)

        viewModel.fetchStationDetails(NON_ISS_STATION_ID)
        advanceUntilIdle()

        assertNull(viewModel.stationDetails.value)
        assertEquals("boom", viewModel.error.value)
        assertEquals(false, viewModel.isLoading.value)
        assertTrue(viewModel.activeExpeditions.value.isEmpty())
    }

    @Test
    fun fetchStationDetails_expeditionFailure_doesNotClearStation() = runTest(dispatcher) {
        val expedition = sampleExpeditionMini(id = 42)
        val repository = FakeSpaceStationRepository().apply {
            spaceStationDetailsResult = Result.success(
                DataResult(
                    data = sampleStationDetail(
                        id = NON_ISS_STATION_ID,
                        activeExpeditions = listOf(expedition)
                    ),
                    source = DataSource.CACHE
                )
            )
            expeditionDetailsResult = Result.failure(RuntimeException("exp fail"))
        }

        val viewModel = createViewModel(repository)

        viewModel.fetchStationDetails(NON_ISS_STATION_ID)
        advanceUntilIdle()

        assertNotNull(viewModel.stationDetails.value)
        // Expedition failure is swallowed so the station screen can still render.
        assertTrue(viewModel.activeExpeditions.value.isEmpty())
        assertNull(viewModel.error.value)
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private fun createViewModel(
        repository: FakeSpaceStationRepository
    ): SpaceStationViewModel = SpaceStationViewModel(
        spaceStationRepository = repository,
        articlesApi = createArticlesApiReturningEmpty(),
        issTrackingRepository = NoopIssTrackingRepository,
        httpClient = HttpClient(
            MockEngine { respond("", HttpStatusCode.NotFound) }
        ),
        analyticsManager = AnalyticsManagerImpl(emptyList())
    )

    companion object {
        private const val NON_ISS_STATION_ID = 5
    }
}

private fun sampleStationDetail(
    id: Int,
    activeExpeditions: List<ExpeditionMiniItem>
): SpaceStationDetail = SpaceStationDetail(
    id = id,
    name = "Tiangong",
    imageUrl = null,
    statusName = "Active",
    statusId = 1,
    founded = LocalDate(2021, 4, 29),
    deorbited = null,
    description = "Test station",
    orbit = "LEO",
    typeName = "Modular",
    owners = emptyList(),
    activeExpeditions = activeExpeditions,
    dockingLocations = emptyList(),
    height = null,
    width = null,
    mass = null,
    volume = null,
    onboardCrew = 3,
    dockedVehicles = 1
)

private fun sampleExpeditionMini(id: Int): ExpeditionMiniItem = ExpeditionMiniItem(
    id = id,
    name = "Expedition $id",
    start = Instant.parse("2024-04-06T00:00:00Z"),
    end = null
)

private fun sampleExpeditionDetail(id: Int): ExpeditionDetailItem = ExpeditionDetailItem(
    id = id,
    name = "Expedition $id",
    start = Instant.parse("2024-04-06T00:00:00Z"),
    end = null,
    crew = emptyList(),
    missionPatches = emptyList(),
    spacewalks = emptyList()
)

private fun createArticlesApiReturningEmpty(): ArticlesApi {
    val emptyPage = """{"count":0,"next":null,"previous":null,"results":[]}"""
    val engine = MockEngine {
        respond(
            content = emptyPage,
            status = HttpStatusCode.OK,
            headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
    }
    return ArticlesApi(baseUrl = "https://snapi.test", httpClientEngine = engine)
}

private object NoopIssTrackingRepository : IssTrackingRepository {
    override suspend fun getCurrentPosition(): Result<IssPosition> =
        Result.failure(UnsupportedOperationException("not used in tests"))

    override suspend fun getTleData(): Result<IssTle> =
        Result.failure(UnsupportedOperationException("not used in tests"))

    override suspend fun getPositionsAtTimestamps(timestamps: List<Long>): Result<List<IssPosition>> =
        Result.failure(UnsupportedOperationException("not used in tests"))
}
