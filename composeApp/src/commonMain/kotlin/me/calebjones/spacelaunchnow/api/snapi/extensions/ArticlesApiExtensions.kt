package me.calebjones.spacelaunchnow.api.snapi.extensions

import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.api.snapi.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import kotlin.time.Instant

/**
 * Extension functions for ArticlesApi to provide clean, named-parameter interfaces
 * instead of the generated methods with many positional parameters
 */

/**
 * Get articles with clean parameter interface
 */
suspend fun ArticlesApi.getArticles(
    limit: Int? = null,
    offset: Int? = null,
    ordering: List<ArticlesApi.OrderingArticlesList>? = null,
    search: String? = null,
    isFeatured: Boolean? = null,
    hasLaunch: Boolean? = null,
    hasEvent: Boolean? = null,
    newsSite: String? = null,
    newsSiteExclude: String? = null,
    publishedAtGte: Instant? = null,
    publishedAtLte: Instant? = null
): HttpResponse<PaginatedArticleList> {
    return articlesList(
        limit = limit,
        offset = offset,
        ordering = ordering,
        search = search,
        isFeatured = isFeatured,
        hasLaunch = hasLaunch,
        hasEvent = hasEvent,
        newsSite = newsSite,
        newsSiteExclude = newsSiteExclude,
        publishedAtGte = publishedAtGte,
        publishedAtLte = publishedAtLte
    )
}

/**
 * Get featured articles
 */
suspend fun ArticlesApi.getFeaturedArticles(
    limit: Int? = null,
    ordering: List<ArticlesApi.OrderingArticlesList>? = listOf(ArticlesApi.OrderingArticlesList.MinusPublished_at)
): HttpResponse<PaginatedArticleList> {
    return getArticles(
        limit = limit,
        ordering = ordering,
        isFeatured = true
    )
}

/**
 * Get articles related to specific launches
 */
suspend fun ArticlesApi.getArticlesByLaunch(
    launchIds: List<String>,
    limit: Int? = null,
    ordering: List<ArticlesApi.OrderingArticlesList>? = listOf(ArticlesApi.OrderingArticlesList.MinusPublished_at)
): HttpResponse<PaginatedArticleList> {
    return articlesList(
        launch = launchIds,
        limit = limit,
        ordering = ordering
    )
}

/**
 * Search articles by title and summary
 */
suspend fun ArticlesApi.searchArticles(
    query: String,
    limit: Int? = null,
    ordering: List<ArticlesApi.OrderingArticlesList>? = listOf(ArticlesApi.OrderingArticlesList.MinusPublished_at)
): HttpResponse<PaginatedArticleList> {
    return getArticles(
        search = query,
        limit = limit,
        ordering = ordering
    )
}
