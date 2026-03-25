package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.snapi.apis.InfoApi
import me.calebjones.spacelaunchnow.api.snapi.extensions.getNewsSites
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Implementation of InfoRepository using SNAPI
 */
class InfoRepositoryImpl(
    private val infoApi: InfoApi
) : InfoRepository {

    private val log = logger()
    
    // Simple cache for news sites list (rarely changes)
    private var cachedNewsSites: List<String>? = null

    override suspend fun getNewsSites(): Result<List<String>> {
        return try {
            // Return cached value if available
            cachedNewsSites?.let { 
                log.d { "Returning cached news sites: ${it.size} sites" }
                return Result.success(it) 
            }
            
            log.d { "Fetching news sites from SNAPI" }
            val sites = infoApi.getNewsSites()
            
            // Cache the result
            cachedNewsSites = sites
            log.i { "Successfully fetched ${sites.size} news sites" }
            
            Result.success(sites)
        } catch (e: ResponseException) {
            log.e(e) { "API error fetching news sites" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error fetching news sites" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error fetching news sites" }
            Result.failure(e)
        }
    }
}
