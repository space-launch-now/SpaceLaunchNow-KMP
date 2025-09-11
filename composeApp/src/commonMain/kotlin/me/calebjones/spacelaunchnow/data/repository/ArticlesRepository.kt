package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList

interface ArticlesRepository {
    suspend fun getArticles(limit: Int = 10): Result<PaginatedArticleList>
    suspend fun getFeaturedArticles(limit: Int = 10): Result<PaginatedArticleList>
    suspend fun getArticlesByLaunch(launchIds: List<String>, limit: Int = 10): Result<PaginatedArticleList>
    suspend fun getArticleById(id: Int): Result<Article>
    suspend fun searchArticles(query: String, limit: Int = 10): Result<PaginatedArticleList>
}
