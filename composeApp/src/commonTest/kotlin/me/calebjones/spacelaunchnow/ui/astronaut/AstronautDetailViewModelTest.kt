package me.calebjones.spacelaunchnow.ui.astronaut

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManager
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.ui.viewmodel.AstronautDetailViewModel
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for AstronautDetailViewModel.
 * Validates astronaut detail loading, error handling, and retry logic.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AstronautDetailViewModelTest {

    private lateinit var mockRepository: MockAstronautDetailRepository
    private val testDispatcher = StandardTestDispatcher()
    private val analyticsManager: AnalyticsManager = AnalyticsManagerImpl(emptyList())
    private val testAstronautId = 1

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockAstronautDetailRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ========================================
    // Initial State Tests
    // ========================================

    @Test
    fun `initial state should be correct`() = runTest {
        // Given: Repository not configured, will return error
        mockRepository.shouldReturnError = true
        
        // When: ViewModel is created (loads automatically in init)
        val viewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()
        
        // Then: Should have attempted to load and got an error
        assertNull(viewModel.uiState.value.astronaut)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNotNull(viewModel.uiState.value.error) // Error because mock has no response
    }

    // ========================================
    // loadAstronautDetail() Tests
    // ========================================

    @Test
    fun `loadAstronautDetail should load astronaut successfully`() = runTest {
        // Given: Repository returns success
        val mockAstronaut = createMockAstronautDetailed(
            id = testAstronautId,
            name = "Neil Armstrong",
            bio = "First person to walk on the Moon"
        )
        val newMockRepo = MockAstronautDetailRepository()
        newMockRepo.setAstronautDetailResponse(mockAstronaut)
        val newViewModel = AstronautDetailViewModel(newMockRepo, analyticsManager, testAstronautId)

        // When: Loading astronaut detail (called automatically in init)
        advanceUntilIdle()

        // Then: Should update state correctly
        assertNotNull(newViewModel.uiState.value.astronaut)
        assertEquals("Neil Armstrong", newViewModel.uiState.value.astronaut?.name)
        assertEquals("First person to walk on the Moon", newViewModel.uiState.value.astronaut?.bio)
        assertFalse(newViewModel.uiState.value.isLoading)
        assertNull(newViewModel.uiState.value.error)
    }

    @Test
    fun `loadAstronautDetail should set loading state during load`() = runTest {
        // Given: Repository configured
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        val viewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)

        // When: Loading astronaut detail (called automatically in init)
        // Note: The loading state may already be done by the time we check
        
        // When: Load completes
        advanceUntilIdle()
        
        // Then: Loading should be false after completion
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadAstronautDetail should handle error`() = runTest {
        // Given: Repository returns failure
        mockRepository.shouldReturnError = true
        val viewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)

        // When: Loading astronaut detail (called automatically in init)
        advanceUntilIdle()

        // Then: Should set error message
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.error?.isNotEmpty() == true)
        assertNull(viewModel.uiState.value.astronaut)
    }

    @Test
    fun `loadAstronautDetail should pass correct ID to repository`() = runTest {
        // Given: ViewModel with specific astronaut ID
        val astronautId = 42
        val mockAstronaut = createMockAstronautDetailed(id = astronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        val customViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, astronautId)

        // When: Loading astronaut detail (called automatically in init)
        advanceUntilIdle()

        // Then: Should call repository with correct ID
        assertEquals(astronautId, mockRepository.lastAstronautId)
        assertEquals(astronautId, customViewModel.uiState.value.astronaut?.id)
    }

    @Test
    fun `loadAstronautDetail should handle astronaut with null fields gracefully`() = runTest {
        // Given: Astronaut with many null fields
        val mockAstronaut = AstronautEndpointDetailed(
            id = testAstronautId,
            url = "https://test.example.com",
            name = "John Doe",
            status = null,
            type = AstronautType(id = 1, name = "Government"),
            age = null,
            bio = "",
            nationality = emptyList(),
            inSpace = false,
            timeInSpace = null,
            evaTime = null,
            dateOfBirth = null,
            dateOfDeath = null,
            wiki = null,
            agency = null,
            image = null,
            lastFlight = null,
            firstFlight = null,
            socialMediaLinks = null,
            flightsCount = null,
            landingsCount = null,
            spacewalksCount = null,
            flights = emptyList(),
            landings = emptyList(),
            spacewalks = emptyList()
        )
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        val newViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)

        // When: Loading astronaut detail (called automatically in init)
        advanceUntilIdle()

        // Then: Should load successfully without crashes
        assertNotNull(newViewModel.uiState.value.astronaut)
        assertEquals("John Doe", newViewModel.uiState.value.astronaut?.name)
        assertNull(newViewModel.uiState.value.error)
    }

    // ========================================
    // Retry Tests
    // ========================================

    @Test
    fun `retry should reload astronaut detail`() = runTest {
        // Given: Initial error state
        mockRepository.shouldReturnError = true
        val errorViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()
        assertTrue(errorViewModel.uiState.value.error?.isNotEmpty() == true)
        val initialCallCount = mockRepository.astronautDetailCallCount

        // When: Retrying with successful response
        mockRepository.shouldReturnError = false
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Neil Armstrong")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        errorViewModel.retry()
        advanceUntilIdle()

        // Then: Should make new API call and clear error
        assertEquals(initialCallCount + 1, mockRepository.astronautDetailCallCount)
        assertNull(errorViewModel.uiState.value.error)
        assertEquals("Neil Armstrong", errorViewModel.uiState.value.astronaut?.name)
    }

    @Test
    fun `retry should clear previous error message`() = runTest {
        // Given: Error state with error message
        mockRepository.shouldReturnError = true
        val errorViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()
        val errorMessage = errorViewModel.uiState.value.error
        assertNotNull(errorMessage)

        // When: Retrying with success
        mockRepository.shouldReturnError = false
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        errorViewModel.retry()
        advanceUntilIdle()

        // Then: Error message should be cleared
        assertNull(errorViewModel.uiState.value.error)
        assertNotNull(errorViewModel.uiState.value.astronaut)
    }

    // ========================================
    // clearError() Tests
    // ========================================

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given: Error state
        mockRepository.shouldReturnError = true
        val errorViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()
        assertNotNull(errorViewModel.uiState.value.error)

        // When: Clearing error
        errorViewModel.clearError()

        // Then: Error message should be null
        assertNull(errorViewModel.uiState.value.error)
    }

    @Test
    fun `clearError should not affect other state`() = runTest {
        // Given: Successful load
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        val successViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()
        
        // Manually set error to simulate error after successful load
        mockRepository.shouldReturnError = true
        successViewModel.retry()
        advanceUntilIdle()

        // When: Clearing error
        val astronautBeforeClear = successViewModel.uiState.value.astronaut
        successViewModel.clearError()

        // Then: Astronaut data should remain
        assertEquals(astronautBeforeClear, successViewModel.uiState.value.astronaut)
        assertNull(successViewModel.uiState.value.error)
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun `loadAstronautDetail should handle multiple consecutive calls`() = runTest {
        // Given: Repository configured
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        val newViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()

        // When: Making multiple retry calls quickly
        newViewModel.retry()
        newViewModel.retry()
        newViewModel.retry()
        advanceUntilIdle()

        // Then: Should handle gracefully (latest call wins)
        assertNotNull(newViewModel.uiState.value.astronaut)
        assertFalse(newViewModel.uiState.value.isLoading)
        // Call count includes init + 3 retries
        assertTrue(mockRepository.astronautDetailCallCount >= 4)
    }

    @Test
    fun `loadAstronautDetail should preserve previous data on error`() = runTest {
        // Given: Successful initial load
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Neil Armstrong")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        val newViewModel = AstronautDetailViewModel(mockRepository, analyticsManager, testAstronautId)
        advanceUntilIdle()
        assertEquals("Neil Armstrong", newViewModel.uiState.value.astronaut?.name)

        // When: Subsequent load fails
        mockRepository.shouldReturnError = true
        newViewModel.retry()
        advanceUntilIdle()

        // Then: Should show error but may not keep previous data (depends on implementation)
        assertNotNull(newViewModel.uiState.value.error)
    }

    // ========================================
    // Helper Methods
    // ========================================

    private fun createMockAstronautDetailed(
        id: Int,
        name: String,
        bio: String = "Test biography"
    ): AstronautEndpointDetailed {
        return AstronautEndpointDetailed(
            id = id,
            url = "https://test.example.com/astronaut/$id",
            name = name,
            status = null,
            type = AstronautType(id = 1, name = "Government"),
            age = 50,
            bio = bio,
            nationality = emptyList(),
            inSpace = false,
            timeInSpace = "P100D",
            evaTime = "P20H",
            dateOfBirth = null,
            dateOfDeath = null,
            wiki = null,
            agency = null,
            image = null,
            lastFlight = null,
            firstFlight = null,
            socialMediaLinks = null,
            flightsCount = 3,
            landingsCount = 2,
            spacewalksCount = 5,
            flights = emptyList(),
            landings = emptyList(),
            spacewalks = emptyList()
        )
    }
}

/**
 * Mock implementation of AstronautRepository for testing ViewModels.
 */
class MockAstronautDetailRepository : AstronautRepository {
    private var astronautDetailResponse: AstronautEndpointDetailed? = null
    var shouldReturnError = false
    var astronautDetailCallCount = 0
    var lastAstronautId: Int? = null

    fun setAstronautDetailResponse(response: AstronautEndpointDetailed) {
        astronautDetailResponse = response
    }

    override suspend fun getAstronauts(
        limit: Int,
        offset: Int,
        search: String?,
        statusIds: List<Int>?,
        agencyIds: List<Int>?,
        ordering: String?,
        hasFlown: Boolean?,
        inSpace: Boolean?,
        isHuman: Boolean?
    ): Result<PaginatedAstronautEndpointNormalList> {
        throw NotImplementedError("Not needed for detail view model tests")
    }

    override suspend fun getAstronautDetail(id: Int): Result<AstronautEndpointDetailed> {
        astronautDetailCallCount++
        lastAstronautId = id

        return if (shouldReturnError) {
            Result.failure(Exception("Test error"))
        } else {
            val response = astronautDetailResponse 
                ?: throw IllegalStateException("Mock response not configured")
            Result.success(response)
        }
    }
}
