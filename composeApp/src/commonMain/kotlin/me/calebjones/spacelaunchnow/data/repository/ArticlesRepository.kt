package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import me.calebjones.spacelaunchnow.data.model.DataResult

interface ArticlesRepository {
    suspend fun getArticles(limit: Int = 10, forceRefresh: Boolean = false): Result<DataResult<PaginatedArticleList>>
    suspend fun getFeaturedArticles(limit: Int = 10): Result<PaginatedArticleList>
    suspend fun getArticlesByLaunch(launchIds: List<String>, limit: Int = 10): Result<PaginatedArticleList>
    suspend fun getArticleById(id: Int): Result<Article>
    suspend fun searchArticles(query: String, limit: Int = 10): Result<PaginatedArticleList>
    
    /**
     * Get articles with full pagination and filtering support for News & Events screen
     * @param limit Number of articles per page
     * @param offset Pagination offset
     * @param search Optional search query
     * @param newsSites Optional filter by news site names (comma-separated for API)
     * @param forceRefresh Force network fetch bypassing cache
     */
    suspend fun getArticlesPaginated(
        limit: Int = 20,
        offset: Int = 0,
        search: String? = null,
        newsSites: List<String>? = null,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedArticleList>>
}
