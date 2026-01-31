package me.calebjones.spacelaunchnow.ui.astronaut

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautBasicList
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for AstronautListViewModel.
 * Validates astronaut list loading, pagination, search, and error handling.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AstronautListViewModelTest {

    private lateinit var viewModel: AstronautListViewModel
    private lateinit var mockRepository: MockAstronautRepository
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockAstronautRepository()
        viewModel = AstronautListViewModel(mockRepository)
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
        assertTrue(viewModel.astronauts.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoadingMore.value)
        assertFalse(viewModel.hasMorePages.value)
    }

    // ========================================
    // loadAstronauts() Tests
    // ========================================

    @Test
    fun `loadAstronauts should load astronauts successfully`() = runTest {
        // Given: Repository returns success
        val mockAstronauts = listOf(
            createMockAstronaut(id = 1, name = "Neil Armstrong"),
            createMockAstronaut(id = 2, name = "Buzz Aldrin")
        )
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 2,
                next = null,
                previous = null,
                results = mockAstronauts
            )
        )

        // When: Loading astronauts
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // Then: Should update state correctly
        assertEquals(2, viewModel.astronauts.value.size)
        assertEquals("Neil Armstrong", viewModel.astronauts.value[0].name)
        assertEquals("Buzz Aldrin", viewModel.astronauts.value[1].name)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `loadAstronauts should set loading state during load`() = runTest {
        // Given: Repository configured
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(count = 0, next = null, previous = null, results = emptyList())
        )

        // When: Loading astronauts
        viewModel.loadAstronauts()
        
        // Then: Should show loading state immediately
        assertTrue(viewModel.isLoading.value)
        
        // When: Load completes
        advanceUntilIdle()
        
        // Then: Loading should be false
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun `loadAstronauts should handle empty results`() = runTest {
        // Given: Repository returns empty list
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(count = 0, next = null, previous = null, results = emptyList())
        )

        // When: Loading astronauts
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // Then: Should have empty list
        assertTrue(viewModel.astronauts.value.isEmpty())
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)
    }

    @Test
    fun `loadAstronauts should handle error`() = runTest {
        // Given: Repository returns failure
        mockRepository.shouldReturnError = true

        // When: Loading astronauts
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // Then: Should set error message
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.errorMessage.value?.isNotEmpty() == true)
        assertTrue(viewModel.astronauts.value.isEmpty())
    }

    @Test
    fun `loadAstronauts should clear error on retry`() = runTest {
        // Given: Initial error state
        mockRepository.shouldReturnError = true
        viewModel.loadAstronauts()
        advanceUntilIdle()
        assertTrue(viewModel.errorMessage.value?.isNotEmpty() == true)

        // When: Retrying with successful response
        mockRepository.shouldReturnError = false
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 1,
                next = null,
                previous = null,
                results = listOf(createMockAstronaut(id = 1, name = "Test"))
            )
        )
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // Then: Should clear error
        assertNull(viewModel.errorMessage.value)
        assertEquals(1, viewModel.astronauts.value.size)
    }

    // ========================================
    // loadMoreAstronauts() Tests
    // ========================================

    @Test
    fun `loadMoreAstronauts should append astronauts to existing list`() = runTest {
        // Given: Initial list loaded
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 4,
                next = "next_page_url",
                previous = null,
                results = listOf(
                    createMockAstronaut(id = 1, name = "Astronaut 1"),
                    createMockAstronaut(id = 2, name = "Astronaut 2")
                )
            )
        )
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // When: Loading more
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 4,
                next = null,
                previous = "prev_page_url",
                results = listOf(
                    createMockAstronaut(id = 3, name = "Astronaut 3"),
                    createMockAstronaut(id = 4, name = "Astronaut 4")
                )
            )
        )
        viewModel.loadMoreAstronauts()
        advanceUntilIdle()

        // Then: Should have all 4 astronauts
        assertEquals(4, viewModel.astronauts.value.size)
        assertEquals("Astronaut 1", viewModel.astronauts.value[0].name)
        assertEquals("Astronaut 4", viewModel.astronauts.value[3].name)
    }

    @Test
    fun `loadMoreAstronauts should set isLoadingMore state`() = runTest {
        // Given: Initial list with next page
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 2,
                next = "next_page",
                previous = null,
                results = listOf(createMockAstronaut(id = 1, name = "Test"))
            )
        )
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // When: Loading more
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 2,
                next = null,
                previous = null,
                results = listOf(createMockAstronaut(id = 2, name = "Test 2"))
            )
        )
        viewModel.loadMoreAstronauts()
        
        // Then: Should show loadingMore state
        assertTrue(viewModel.isLoadingMore.value)
        
        // When: Load completes
        advanceUntilIdle()
        
        // Then: LoadingMore should be false
        assertFalse(viewModel.isLoadingMore.value)
    }

    @Test
    fun `loadMoreAstronauts should not load when already loading`() = runTest {
        // Given: Initial list
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 1,
                next = "next_page",
                previous = null,
                results = listOf(createMockAstronaut(id = 1, name = "Test"))
            )
        )
        viewModel.loadAstronauts()
        advanceUntilIdle()

        // When: Trying to load more while already loading
        viewModel.loadMoreAstronauts()
        assertTrue(viewModel.isLoadingMore.value)
        val callCountBefore = mockRepository.astronautsCallCount
        
        viewModel.loadMoreAstronauts() // Second call while loading
        
        // Then: Should not make another API call
        assertEquals(callCountBefore, mockRepository.astronautsCallCount)
    }

    @Test
    fun `loadMoreAstronauts should update hasMorePages based on next field`() = runTest {
        // Given: Initial list with next page
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 2,
                next = "next_page_url",
                previous = null,
                results = listOf(createMockAstronaut(id = 1, name = "Test"))
            )
        )
        viewModel.loadAstronauts()
        advanceUntilIdle()
        assertTrue(viewModel.hasMorePages.value)

        // When: Loading last page (no next)
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 2,
                next = null,
                previous = "prev_page",
                results = listOf(createMockAstronaut(id = 2, name = "Test 2"))
            )
        )
        viewModel.loadMoreAstronauts()
        advanceUntilIdle()

        // Then: hasMorePages should be false
        assertFalse(viewModel.hasMorePages.value)
    }

    // ========================================
    // searchAstronauts() Tests
    // ========================================

    @Test
    fun `searchAstronauts should filter astronauts by name`() = runTest {
        // Given: Repository returns filtered results
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 1,
                next = null,
                previous = null,
                results = listOf(createMockAstronaut(id = 1, name = "Neil Armstrong"))
            )
        )

        // When: Searching for "Neil"
        viewModel.searchAstronauts("Neil")
        advanceUntilIdle()

        // Then: Should show filtered results
        assertEquals(1, viewModel.astronauts.value.size)
        assertEquals("Neil Armstrong", viewModel.astronauts.value[0].name)
        assertEquals("Neil", mockRepository.lastSearchQuery)
    }

    @Test
    fun `searchAstronauts should clear search when query is empty`() = runTest {
        // Given: Search active
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 1,
                next = null,
                previous = null,
                results = listOf(createMockAstronaut(id = 1, name = "Neil Armstrong"))
            )
        )
        viewModel.searchAstronauts("Neil")
        advanceUntilIdle()

        // When: Clearing search
        mockRepository.setAstronautsResponse(
            PaginatedAstronautBasicList(
                count = 3,
                next = null,
                previous = null,
                results = listOf(
                    createMockAstronaut(id = 1, name = "Neil Armstrong"),
                    createMockAstronaut(id = 2, name = "Buzz Aldrin"),
                    createMockAstronaut(id = 3, name = "Michael Collins")
                )
            )
        )
        viewModel.searchAstronauts("")
        advanceUntilIdle()

        // Then: Should show all results
        assertEquals(3, viewModel.astronauts.value.size)
        assertNull(mockRepository.lastSearchQuery)
    }

    // ========================================
    // Helper Methods
    // ========================================

    private fun createMockAstronaut(id: Int, name: String): AstronautBasic {
        return AstronautBasic(
            id = id,
            url = "https://test.example.com/astronaut/$id",
            name = name,
            status = null,
            type = null,
            inSpace = false,
            timeInSpace = null,
            evaTime = null,
            age = 50,
            dateOfBirth = null,
            dateOfDeath = null,
            nationality = "American",
            bio = "Test biography",
            twitter = null,
            instagram = null,
            wiki = null,
            agency = null,
            profileImage = "https://test.example.com/image.jpg",
            profileImageThumbnail = "https://test.example.com/thumb.jpg",
            flightsCount = 3,
            landingsCount = 2,
            spacewalksCount = 5,
            lastFlight = null,
            firstFlight = null
        )
    }
}

/**
 * Mock implementation of AstronautRepository for testing ViewModels.
 */
private class MockAstronautRepository : AstronautRepository {
    var astronautsResponse: PaginatedAstronautBasicList? = null
    var shouldReturnError = false
    var astronautsCallCount = 0
    var lastSearchQuery: String? = null

    fun setAstronautsResponse(response: PaginatedAstronautBasicList) {
        astronautsResponse = response
    }

    override suspend fun getAstronauts(
        limit: Int,
        offset: Int,
        search: String?
    ): Result<PaginatedAstronautBasicList> {
        astronautsCallCount++
        lastSearchQuery = search

        return if (shouldReturnError) {
            Result.failure(Exception("Test error"))
        } else {
            val response = astronautsResponse 
                ?: PaginatedAstronautBasicList(count = 0, next = null, previous = null, results = emptyList())
            Result.success(response)
        }
    }

    override suspend fun getAstronautDetail(astronautId: Int): Result<me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautDetailed> {
        throw NotImplementedError("Not needed for list view model tests")
    }
}
