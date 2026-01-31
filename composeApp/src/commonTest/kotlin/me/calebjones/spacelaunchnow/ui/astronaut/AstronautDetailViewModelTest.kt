package me.calebjones.spacelaunchnow.ui.astronaut

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautBasicList
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
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

    private lateinit var viewModel: AstronautDetailViewModel
    private lateinit var mockRepository: MockAstronautRepository
    private val testDispatcher = StandardTestDispatcher()
    private val testAstronautId = 1

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockAstronautRepository()
        viewModel = AstronautDetailViewModel(mockRepository, testAstronautId)
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
        // When: ViewModel is created
        // Then: Should have correct initial state
        assertNull(viewModel.astronaut.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
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
        mockRepository.setAstronautDetailResponse(mockAstronaut)

        // When: Loading astronaut detail
        viewModel.loadAstronautDetail()
        advanceUntilIdle()

        // Then: Should update state correctly
        assertNotNull(viewModel.astronaut.value)
        assertEquals("Neil Armstrong", viewModel.astronaut.value?.name)
        assertEquals("First person to walk on the Moon", viewModel.astronaut.value?.bio)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `loadAstronautDetail should set loading state during load`() = runTest {
        // Given: Repository configured
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)

        // When: Loading astronaut detail
        viewModel.loadAstronautDetail()
        
        // Then: Should show loading state immediately
        assertTrue(viewModel.isLoading.value)
        
        // When: Load completes
        advanceUntilIdle()
        
        // Then: Loading should be false
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadAstronautDetail should handle error`() = runTest {
        // Given: Repository returns failure
        mockRepository.shouldReturnError = true

        // When: Loading astronaut detail
        viewModel.loadAstronautDetail()
        advanceUntilIdle()

        // Then: Should set error message
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.errorMessage.value?.isNotEmpty() == true)
        assertNull(viewModel.astronaut.value)
    }

    @Test
    fun `loadAstronautDetail should pass correct ID to repository`() = runTest {
        // Given: ViewModel with specific astronaut ID
        val astronautId = 42
        val customViewModel = AstronautDetailViewModel(mockRepository, astronautId)
        val mockAstronaut = createMockAstronautDetailed(id = astronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)

        // When: Loading astronaut detail
        customViewModel.loadAstronautDetail()
        advanceUntilIdle()

        // Then: Should call repository with correct ID
        assertEquals(astronautId, mockRepository.lastAstronautId)
        assertEquals(astronautId, customViewModel.astronaut.value?.id)
    }

    @Test
    fun `loadAstronautDetail should handle astronaut with null fields gracefully`() = runTest {
        // Given: Astronaut with many null fields
        val mockAstronaut = AstronautDetailed(
            id = testAstronautId,
            url = "https://test.example.com",
            name = "John Doe",
            status = null,
            type = null,
            inSpace = false,
            timeInSpace = null,
            evaTime = null,
            age = null,
            dateOfBirth = null,
            dateOfDeath = null,
            nationality = null,
            bio = null,
            twitter = null,
            instagram = null,
            wiki = null,
            agency = null,
            profileImage = null,
            profileImageThumbnail = null,
            lastFlight = null,
            firstFlight = null,
            flights = emptyList()
        )
        mockRepository.setAstronautDetailResponse(mockAstronaut)

        // When: Loading astronaut detail
        viewModel.loadAstronautDetail()
        advanceUntilIdle()

        // Then: Should load successfully without crashes
        assertNotNull(viewModel.astronaut.value)
        assertEquals("John Doe", viewModel.astronaut.value?.name)
        assertNull(viewModel.astronaut.value?.bio)
        assertNull(viewModel.errorMessage.value)
    }

    // ========================================
    // Retry Tests
    // ========================================

    @Test
    fun `retry should reload astronaut detail`() = runTest {
        // Given: Initial error state
        mockRepository.shouldReturnError = true
        viewModel.loadAstronautDetail()
        advanceUntilIdle()
        assertTrue(viewModel.errorMessage.value?.isNotEmpty() == true)
        val initialCallCount = mockRepository.astronautDetailCallCount

        // When: Retrying with successful response
        mockRepository.shouldReturnError = false
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Neil Armstrong")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        viewModel.retry()
        advanceUntilIdle()

        // Then: Should make new API call and clear error
        assertEquals(initialCallCount + 1, mockRepository.astronautDetailCallCount)
        assertNull(viewModel.errorMessage.value)
        assertEquals("Neil Armstrong", viewModel.astronaut.value?.name)
    }

    @Test
    fun `retry should clear previous error message`() = runTest {
        // Given: Error state with error message
        mockRepository.shouldReturnError = true
        viewModel.loadAstronautDetail()
        advanceUntilIdle()
        val errorMessage = viewModel.errorMessage.value
        assertNotNull(errorMessage)

        // When: Retrying with success
        mockRepository.shouldReturnError = false
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        viewModel.retry()
        advanceUntilIdle()

        // Then: Error message should be cleared
        assertNull(viewModel.errorMessage.value)
        assertNotNull(viewModel.astronaut.value)
    }

    // ========================================
    // clearError() Tests
    // ========================================

    @Test
    fun `clearError should clear error message`() = runTest {
        // Given: Error state
        mockRepository.shouldReturnError = true
        viewModel.loadAstronautDetail()
        advanceUntilIdle()
        assertNotNull(viewModel.errorMessage.value)

        // When: Clearing error
        viewModel.clearError()

        // Then: Error message should be null
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `clearError should not affect other state`() = runTest {
        // Given: Successful load then error
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        viewModel.loadAstronautDetail()
        advanceUntilIdle()
        
        mockRepository.shouldReturnError = true
        viewModel.loadAstronautDetail()
        advanceUntilIdle()

        // When: Clearing error
        val astronautBeforeClear = viewModel.astronaut.value
        viewModel.clearError()

        // Then: Astronaut data should remain
        assertEquals(astronautBeforeClear, viewModel.astronaut.value)
        assertNull(viewModel.errorMessage.value)
    }

    // ========================================
    // Edge Cases
    // ========================================

    @Test
    fun `loadAstronautDetail should handle multiple consecutive calls`() = runTest {
        // Given: Repository configured
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Test")
        mockRepository.setAstronautDetailResponse(mockAstronaut)

        // When: Making multiple calls quickly
        viewModel.loadAstronautDetail()
        viewModel.loadAstronautDetail()
        viewModel.loadAstronautDetail()
        advanceUntilIdle()

        // Then: Should handle gracefully (latest call wins)
        assertNotNull(viewModel.astronaut.value)
        assertFalse(viewModel.isLoading.value)
        // Call count may be 3 since we allow multiple calls
        assertTrue(mockRepository.astronautDetailCallCount >= 1)
    }

    @Test
    fun `loadAstronautDetail should preserve previous data on error`() = runTest {
        // Given: Successful initial load
        val mockAstronaut = createMockAstronautDetailed(id = testAstronautId, name = "Neil Armstrong")
        mockRepository.setAstronautDetailResponse(mockAstronaut)
        viewModel.loadAstronautDetail()
        advanceUntilIdle()
        assertEquals("Neil Armstrong", viewModel.astronaut.value?.name)

        // When: Subsequent load fails
        mockRepository.shouldReturnError = true
        viewModel.loadAstronautDetail()
        advanceUntilIdle()

        // Then: Should show error but keep previous data
        assertNotNull(viewModel.errorMessage.value)
        assertNotNull(viewModel.astronaut.value)
        assertEquals("Neil Armstrong", viewModel.astronaut.value?.name)
    }

    // ========================================
    // Helper Methods
    // ========================================

    private fun createMockAstronautDetailed(
        id: Int,
        name: String,
        bio: String = "Test biography"
    ): AstronautDetailed {
        return AstronautDetailed(
            id = id,
            url = "https://test.example.com/astronaut/$id",
            name = name,
            status = null,
            type = null,
            inSpace = false,
            timeInSpace = "P100D",
            evaTime = "P20H",
            age = 50,
            dateOfBirth = null,
            dateOfDeath = null,
            nationality = "American",
            bio = bio,
            twitter = null,
            instagram = null,
            wiki = null,
            agency = null,
            profileImage = "https://test.example.com/image.jpg",
            profileImageThumbnail = "https://test.example.com/thumb.jpg",
            lastFlight = null,
            firstFlight = null,
            flights = emptyList()
        )
    }
}

/**
 * Mock implementation of AstronautRepository for testing ViewModels.
 */
private class MockAstronautRepository : AstronautRepository {
    var astronautDetailResponse: AstronautDetailed? = null
    var shouldReturnError = false
    var astronautDetailCallCount = 0
    var lastAstronautId: Int? = null

    fun setAstronautDetailResponse(response: AstronautDetailed) {
        astronautDetailResponse = response
    }

    override suspend fun getAstronauts(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedAstronautBasicList> {
        throw NotImplementedError("Not needed for detail view model tests")
    }

    override suspend fun getAstronautDetail(astronautId: Int): Result<AstronautDetailed> {
        astronautDetailCallCount++
        lastAstronautId = astronautId

        return if (shouldReturnError) {
            Result.failure(Exception("Test error"))
        } else {
            val response = astronautDetailResponse 
                ?: throw IllegalStateException("Mock response not configured")
            Result.success(response)
        }
    }
}
