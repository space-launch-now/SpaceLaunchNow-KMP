package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Clock.System

class ArticleLocalDataSource(
    database: SpaceLaunchDatabase,
    private val appPreferences: AppPreferences
) {
    private val queries = database.articleQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cacheDuration = 1.hours
    private val debugCacheDuration = 2.minutes
    
    private suspend fun getEffectiveCacheDuration(): kotlin.time.Duration {
        return if (appPreferences.isDebugShortCacheTtlEnabled()) {
            println("⚠️ DEBUG MODE: Using short cache TTL (2 minutes) instead of ${cacheDuration.inWholeHours} hours")
            debugCacheDuration
        } else {
            cacheDuration
        }
    }
    
    suspend fun cacheArticle(article: Article) {
        val now = System.now().toEpochMilliseconds()
        val duration = getEffectiveCacheDuration()
        val expiresAt = now + duration.inWholeMilliseconds
        
        queries.insertOrReplaceArticle(
            id = article.id.toLong(),
            title = article.title,
            url = article.url,
            image_url = article.imageUrl,
            news_site = article.newsSite,
            summary = article.summary,
            published_at = article.publishedAt.toEpochMilliseconds(),
            updated_at = article.updatedAt.toEpochMilliseconds(),
            json_data = json.encodeToString(article),
            cached_at = now,
            expires_at = expiresAt
        )
    }
    
    suspend fun cacheArticles(articles: List<Article>) {
        articles.forEach { cacheArticle(it) }
    }
    
    suspend fun getArticle(id: Int): Article? {
        val now = System.now().toEpochMilliseconds()
        val cached = queries.getArticleById(id.toLong(), now).executeAsOneOrNull()
        return cached?.let { json.decodeFromString<Article>(it.json_data) }
    }
    
    suspend fun getRecentArticles(limit: Int): List<Article> {
        val now = System.now().toEpochMilliseconds()
        val results = queries.getRecentArticles(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    val ageMinutes = (now - cached.cached_at) / 60000
                    println("  Cache entry age: ${ageMinutes} minutes (cached at ${cached.cached_at}, expires at ${cached.expires_at})")
                    json.decodeFromString<Article>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
        return results
    }
    
    suspend fun deleteExpiredArticles() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredArticles(now)
    }
    
    /**
     * Gets the timestamp of when articles were last cached.
     * Returns the most recent cached_at timestamp.
     */
    suspend fun getCacheTimestamp(key: String): Long? {
        return when (key) {
            "articles" -> queries.getRecentArticles(Long.MAX_VALUE, 1).executeAsOneOrNull()?.cached_at
            else -> null
        }
    }
    
    suspend fun clearAllArticles() {
        queries.clearAllArticles()
    }
}
