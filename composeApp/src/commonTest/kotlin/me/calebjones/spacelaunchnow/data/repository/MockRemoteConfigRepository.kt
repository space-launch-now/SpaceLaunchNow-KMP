package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.RoadmapData

/**
 * Mock implementation of RemoteConfigRepository for testing
 * Configurable responses for different test scenarios
 */
class MockRemoteConfigRepository : RemoteConfigRepository {
    
    var shouldFail = false
    var failureException = Exception("Mock failure")
    var mockRoadmapData: RoadmapData? = null
    var fetchAndActivateCalled = false
    var setDefaultsCalled = false
    var lastForceRefresh: Boolean? = null
    
    override suspend fun fetchAndActivate(forceRefresh: Boolean): Result<Unit> {
        fetchAndActivateCalled = true
        lastForceRefresh = forceRefresh
        
        return if (shouldFail) {
            Result.failure(failureException)
        } else {
            Result.success(Unit)
        }
    }
    
    override suspend fun getRoadmapData(): Result<RoadmapData> {
        return if (shouldFail) {
            Result.failure(failureException)
        } else {
            Result.success(mockRoadmapData ?: RoadmapData())
        }
    }
    
    override suspend fun setDefaults() {
        setDefaultsCalled = true
        // setDefaults doesn't throw - it catches exceptions internally
        // like the real implementation
    }
    
    fun reset() {
        shouldFail = false
        failureException = Exception("Mock failure")
        mockRoadmapData = null
        fetchAndActivateCalled = false
        setDefaultsCalled = false
        lastForceRefresh = null
    }
}
