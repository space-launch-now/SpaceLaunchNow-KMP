package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import kotlin.time.Duration.Companion.hours
import kotlin.time.Clock.System

class UpdateLocalDataSource(
    database: SpaceLaunchDatabase
) {
    private val queries = database.updateQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cacheDuration = 6.hours
    
    suspend fun cacheUpdate(update: UpdateEndpoint) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + cacheDuration.inWholeMilliseconds
        
        queries.insertOrReplaceUpdate(
            id = update.id.toLong(),
            profile_image = update.profileImage,
            comment = update.comment ?: "",
            info_url = update.infoUrl,
            created_on = update.createdOn?.toEpochMilliseconds(),
            json_data = json.encodeToString(update),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun cacheUpdates(updates: List<UpdateEndpoint>) {
        updates.forEach { cacheUpdate(it) }
    }
    
    suspend fun getUpdate(id: Int): UpdateEndpoint? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getUpdateById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<UpdateEndpoint>(it.json_data) }
    }
    
    suspend fun getRecentUpdates(limit: Int): List<UpdateEndpoint> {
        val now = System.now().toEpochMilliseconds()
        return queries.getRecentUpdates(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<UpdateEndpoint>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    suspend fun deleteExpiredUpdates() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredUpdates(now)
    }
    
    suspend fun clearAllUpdates() {
        queries.clearAllUpdates()
    }
}
