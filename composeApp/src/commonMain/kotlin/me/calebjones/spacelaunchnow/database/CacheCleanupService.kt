package me.calebjones.spacelaunchnow.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.hours

/**
 * Background service for cleaning up expired cache entries
 * Runs periodically to remove stale data and keep database size manageable
 */
class CacheCleanupService(
    private val launchDataSource: LaunchLocalDataSource,
    private val eventDataSource: EventLocalDataSource,
    private val articleDataSource: ArticleLocalDataSource,
    private val updateDataSource: UpdateLocalDataSource,
    private val programDataSource: ProgramLocalDataSource,
    private val spacecraftDataSource: SpacecraftLocalDataSource
) {
    private val cleanupScope = CoroutineScope(Dispatchers.Default)
    private val cleanupInterval = 6.hours
    
    /**
     * Start the periodic cleanup task
     * Should be called once during app initialization
     */
    fun start() {
        cleanupScope.launch {
            while (true) {
                try {
                    performCleanup()
                } catch (e: Exception) {
                    println("CacheCleanupService: Error during cleanup: ${e.message}")
                }
                delay(cleanupInterval)
            }
        }
    }
    
    /**
     * Manually trigger a cleanup operation
     */
    suspend fun performCleanup() {
        println("CacheCleanupService: Starting cache cleanup...")
        
        try {
            launchDataSource.deleteExpiredLaunches()
            println("CacheCleanupService: Cleaned up expired launches")
        } catch (e: Exception) {
            println("CacheCleanupService: Error cleaning launches: ${e.message}")
        }
        
        try {
            eventDataSource.deleteExpiredEvents()
            println("CacheCleanupService: Cleaned up expired events")
        } catch (e: Exception) {
            println("CacheCleanupService: Error cleaning events: ${e.message}")
        }
        
        try {
            articleDataSource.deleteExpiredArticles()
            println("CacheCleanupService: Cleaned up expired articles")
        } catch (e: Exception) {
            println("CacheCleanupService: Error cleaning articles: ${e.message}")
        }
        
        try {
            updateDataSource.deleteExpiredUpdates()
            println("CacheCleanupService: Cleaned up expired updates")
        } catch (e: Exception) {
            println("CacheCleanupService: Error cleaning updates: ${e.message}")
        }
        
        try {
            programDataSource.deleteExpiredPrograms()
            println("CacheCleanupService: Cleaned up expired programs")
        } catch (e: Exception) {
            println("CacheCleanupService: Error cleaning programs: ${e.message}")
        }
        
        try {
            spacecraftDataSource.deleteExpiredSpacecraft()
            println("CacheCleanupService: Cleaned up expired spacecraft")
        } catch (e: Exception) {
            println("CacheCleanupService: Error cleaning spacecraft: ${e.message}")
        }
        
        println("CacheCleanupService: Cache cleanup completed")
    }
}
