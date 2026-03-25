package me.calebjones.spacelaunchnow.ui.roadmap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.data.model.RoadmapCategory
import me.calebjones.spacelaunchnow.data.model.RoadmapData
import me.calebjones.spacelaunchnow.data.model.RoadmapItem
import me.calebjones.spacelaunchnow.data.model.RoadmapPriority
import me.calebjones.spacelaunchnow.data.model.RoadmapStatus
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for RoadmapViewModel
 * Tests state transitions, remote config integration, and fallback behavior
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RoadmapViewModelTest {
    
    private lateinit var mockRepository: MockRemoteConfigRepository
    private lateinit var viewModel: RoadmapViewModel
    private val testDispatcher = StandardTestDispatcher()
    
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockRemoteConfigRepository()
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ========================================
    // Init / Loading Tests
    // ========================================
    
    @Test
    fun `should call setDefaults on initialization`() = runTest {
        // Given: Fresh repository
        mockRepository.mockRoadmapData = createTestRoadmapData()
        
        // When: ViewModel initializes
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: setDefaults was called
        assertTrue(mockRepository.setDefaultsCalled)
    }
    
    @Test
    fun `should fetch and activate on initialization`() = runTest {
        // Given: Repository with data
        mockRepository.mockRoadmapData = createTestRoadmapData()
        
        // When: ViewModel initializes
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: fetchAndActivate was called
        assertTrue(mockRepository.fetchAndActivateCalled)
        assertEquals(false, mockRepository.lastForceRefresh)
    }
    
    @Test
    fun `should show loading state initially`() = runTest {
        // Given: Repository
        mockRepository.mockRoadmapData = createTestRoadmapData()
        
        // When: ViewModel initializes
        viewModel = RoadmapViewModel(mockRepository)
        
        // Then: Initial state is loading
        val initialState = viewModel.uiState.value
        assertTrue(initialState.isLoading)
    }
    
    // ========================================
    // Success State Tests
    // ========================================
    
    @Test
    fun `should display remote config data on successful load`() = runTest {
        // Given: Repository with roadmap data
        val testData = createTestRoadmapData()
        mockRepository.mockRoadmapData = testData
        
        // When: ViewModel loads
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: UI state has loaded data
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.roadmapData)
        assertEquals(testData.items.size, state.roadmapData!!.items.size)
        assertEquals(testData.message, state.roadmapData!!.message)
    }
    
    @Test
    fun `should fallback to placeholder when remote data is empty`() = runTest {
        // Given: Repository with empty data
        mockRepository.mockRoadmapData = RoadmapData(items = emptyList())
        
        // When: ViewModel loads
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Uses placeholder data (non-empty)
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.roadmapData)
        assertTrue(state.roadmapData!!.items.isNotEmpty())
    }
    
    // ========================================
    // Error State Tests
    // ========================================
    
    @Test
    fun `should show error message on fetch failure`() = runTest {
        // Given: Repository configured to fail
        mockRepository.shouldFail = true
        mockRepository.failureException = Exception("Network error")
        
        // When: ViewModel loads
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: Error state with fallback data
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.errorMessage!!.contains("cached data"))
        // Still has placeholder data as fallback
        assertNotNull(state.roadmapData)
    }
    
    @Test
    fun `should clear error message`() = runTest {
        // Given: ViewModel with error
        mockRepository.shouldFail = true
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When: Clear error
        viewModel.clearError()
        
        // Then: Error is cleared
        assertNull(viewModel.uiState.value.errorMessage)
    }
    
    // ========================================
    // Refresh Tests
    // ========================================
    
    @Test
    fun `should force refresh when refresh is called`() = runTest {
        // Given: ViewModel already loaded
        mockRepository.mockRoadmapData = createTestRoadmapData()
        viewModel = RoadmapViewModel(mockRepository)
        testDispatcher.scheduler.advanceUntilIdle()
        mockRepository.reset()
        mockRepository.mockRoadmapData = createTestRoadmapData()
        
        // When: Refresh is called
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then: forceRefresh was true
        assertTrue(mockRepository.fetchAndActivateCalled)
        assertEquals(true, mockRepository.lastForceRefresh)
    }
    
    // ========================================
    // Helper Methods
    // ========================================
    
    private fun createTestRoadmapData(): RoadmapData {
        return RoadmapData(
            items = listOf(
                RoadmapItem(
                    id = "test-1",
                    title = "Test Feature",
                    description = "Test description",
                    status = RoadmapStatus.IN_PROGRESS,
                    quarter = "Q1 2026",
                    category = RoadmapCategory.FEATURE,
                    priority = RoadmapPriority.HIGH
                ),
                RoadmapItem(
                    id = "test-2",
                    title = "Another Feature",
                    description = "Another description",
                    status = RoadmapStatus.PLANNED,
                    quarter = "Q2 2026",
                    category = RoadmapCategory.ENHANCEMENT,
                    priority = RoadmapPriority.MEDIUM
                )
            ),
            lastUpdated = "March 2026",
            message = "Test roadmap message"
        )
    }
}
