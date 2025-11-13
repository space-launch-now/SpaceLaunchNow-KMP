package me.calebjones.spacelaunchnow.database

import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import kotlin.time.Duration.Companion.hours
import kotlin.time.Clock.System

class ArticleLocalDataSource(
    database: SpaceLaunchDatabase
) {
    private val queries = database.articleQueries
    private val json = Json { ignoreUnknownKeys = true }
    
    private val cacheDuration = 12.hours
    
    suspend fun cacheArticle(article: Article) {
        val now = System.now().toEpochMilliseconds()
        val expiresAt = now + cacheDuration.inWholeMilliseconds
        
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
        return queries.getRecentArticles(now, limit.toLong())
            .executeAsList()
            .mapNotNull { cached ->
                try {
                    json.decodeFromString<Article>(cached.json_data)
                } catch (e: Exception) {
                    null
                }
            }
    }
    
    suspend fun deleteExpiredArticles() {
        val now = System.now().toEpochMilliseconds()
        queries.deleteExpiredArticles(now)
    }
    
    suspend fun clearAllArticles() {
        queries.clearAllArticles()
    }
}
