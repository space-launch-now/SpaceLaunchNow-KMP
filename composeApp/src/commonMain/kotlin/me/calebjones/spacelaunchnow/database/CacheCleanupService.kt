package me.calebjones.spacelaunchnow.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.util.logging.logger
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
    private val log = logger()
    
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
                    log.e(e) { "Error during cleanup: ${e.message}" }
                }
                delay(cleanupInterval)
            }
        }
    }
    
    /**
     * Manually trigger a cleanup operation
     */
    suspend fun performCleanup() {
        log.i { "Starting cache cleanup..." }
        
        try {
            launchDataSource.deleteExpiredLaunches()
            log.d { "Cleaned up expired launches" }
        } catch (e: Exception) {
            log.e(e) { "Error cleaning launches: ${e.message}" }
        }
        
        try {
            eventDataSource.deleteExpiredEvents()
            log.d { "Cleaned up expired events" }
        } catch (e: Exception) {
            log.e(e) { "Error cleaning events: ${e.message}" }
        }
        
        try {
            articleDataSource.deleteExpiredArticles()
            log.d { "Cleaned up expired articles" }
        } catch (e: Exception) {
            log.e(e) { "Error cleaning articles: ${e.message}" }
        }
        
        try {
            updateDataSource.deleteExpiredUpdates()
            log.d { "Cleaned up expired updates" }
        } catch (e: Exception) {
            log.e(e) { "Error cleaning updates: ${e.message}" }
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

        log.i { "Cache cleanup completed" 
    }
}
