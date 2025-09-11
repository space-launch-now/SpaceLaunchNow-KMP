package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.api.snapi.extensions.getArticles
import me.calebjones.spacelaunchnow.api.snapi.extensions.getArticlesByLaunch
import me.calebjones.spacelaunchnow.api.snapi.extensions.getFeaturedArticles
import me.calebjones.spacelaunchnow.api.snapi.extensions.searchArticles
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import me.calebjones.spacelaunchnow.data.model.ApiError

class ArticlesRepositoryImpl(private val articlesApi: ArticlesApi) : ArticlesRepository {

    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun parseApiError(rawResponse: String): String {
        return try {
            val apiError = json.decodeFromString<ApiError>(rawResponse)
            apiError.getErrorMessage()
        } catch (e: Exception) {
            // If we can't parse as ApiError, return the raw response
            rawResponse
        }
    }

    override suspend fun getArticles(limit: Int): Result<PaginatedArticleList> {
        return try {
            println("=== ArticlesRepository: Fetching articles (limit: $limit) ===")
            
            val response = articlesApi.getArticles(
                limit = limit
            )
            
            val body = response.body()
            println("Successfully fetched ${body.results.size} articles")
            
            Result.success(body)
        } catch (e: ResponseException) {
            println("ResponseException: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            println("IOException: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            println("Failed to get articles: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getFeaturedArticles(limit: Int): Result<PaginatedArticleList> {
        return try {
            val response = articlesApi.getFeaturedArticles(limit = limit)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticlesByLaunch(launchIds: List<String>, limit: Int): Result<PaginatedArticleList> {
        return try {
            val response = articlesApi.getArticlesByLaunch(
                launchIds = launchIds,
                limit = limit
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getArticleById(id: Int): Result<Article> {
        return try {
            val response = articlesApi.articlesRetrieve(id = id)
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchArticles(query: String, limit: Int): Result<PaginatedArticleList> {
        return try {
            val response = articlesApi.searchArticles(
                query = query,
                limit = limit
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
