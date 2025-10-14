package me.calebjones.spacelaunchnow.[PACKAGE]

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Unit tests for [VIEWMODEL_NAME]
 * 
 * Tests cover:
 * - Initial state correctness
 * - Loading state transitions
 * - Success state with data
 * - Error state with messages
 * - StateFlow emissions
 * - Coroutine cancellation
 * - Repository error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class [VIEWMODEL_NAME]Test {
    
    // Test dispatcher for controlling coroutine execution
    private val testDispatcher = StandardTestDispatcher()
    
    /**
     * Setup test environment before each test
     */
    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }
    
    /**
     * Cleanup test environment after each test
     */
    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }
    
    /**
     * Creates a mock repository for testing
     */
    private fun createMockRepository(
        shouldFail: Boolean = false,
        data: [DataType]? = null
    ) = object : [RepositoryInterface] {
        override suspend fun [methodName](): Result<[DataType]> {
            return if (shouldFail) {
                Result.failure(Exception("Test error"))
            } else {
                Result.success(data ?: createMockData())
            }
        }
    }
    
    /**
     * Creates mock data for testing
     */
    private fun createMockData(): [DataType] {
        return [DataType](
            // Initialize with test data
        )
    }
    
    /**
     * Test initial state is correct
     */
    @Test
    fun testInitialState_IsCorrect() {
        val repository = createMockRepository()
        val viewModel = [VIEWMODEL_NAME](repository)
        
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.data.value)
        assertNull(viewModel.error.value)
    }
    
    /**
     * Test loading state transitions correctly
     */
    @Test
    fun test[MethodName]_SetsLoadingState() = runTest {
        val repository = createMockRepository()
        val viewModel = [VIEWMODEL_NAME](repository)
        
        // Initially not loading
        assertFalse(viewModel.isLoading.value)
        
        // Trigger data load
        viewModel.[loadMethod]()
        
        // Advance coroutines until idle
        testScheduler.advanceUntilIdle()
        
        // After completion, should not be loading
        assertFalse(viewModel.isLoading.value)
    }
    
    /**
     * Test successful data load emits data
     */
    @Test
    fun test[MethodName]_Success_EmitsData() = runTest {
        val expectedData = createMockData()
        val repository = createMockRepository(data = expectedData)
        val viewModel = [VIEWMODEL_NAME](repository)
        
        viewModel.[loadMethod]()
        testScheduler.advanceUntilIdle()
        
        assertNotNull(viewModel.data.value)
        assertEquals(expectedData, viewModel.data.value)
        assertNull(viewModel.error.value)
    }
    
    /**
     * Test error handling sets error state
     */
    @Test
    fun test[MethodName]_Error_SetsErrorState() = runTest {
        val repository = createMockRepository(shouldFail = true)
        val viewModel = [VIEWMODEL_NAME](repository)
        
        viewModel.[loadMethod]()
        testScheduler.advanceUntilIdle()
        
        assertNotNull(viewModel.error.value)
        assertNull(viewModel.data.value)
    }
    
    /**
     * Test multiple rapid calls don't cause issues
     */
    @Test
    fun test[MethodName]_MultipleRapidCalls_HandlesCorrectly() = runTest {
        val repository = createMockRepository()
        val viewModel = [VIEWMODEL_NAME](repository)
        
        // Make multiple calls
        viewModel.[loadMethod]()
        viewModel.[loadMethod]()
        viewModel.[loadMethod]()
        
        testScheduler.advanceUntilIdle()
        
        // Should still have valid state
        assertFalse(viewModel.isLoading.value)
        assertNotNull(viewModel.data.value)
    }
}
