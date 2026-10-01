package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.domain.model.ArticleSummary

fun Article.toDomainSummary(): ArticleSummary = ArticleSummary(
    id = id,
    title = title,
    url = url,
    imageUrl = imageUrl,
    newsSite = newsSite,
    summary = summary,
    publishedAt = publishedAt
)
