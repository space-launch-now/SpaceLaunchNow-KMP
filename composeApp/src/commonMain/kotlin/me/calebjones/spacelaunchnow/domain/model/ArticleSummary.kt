package me.calebjones.spacelaunchnow.domain.model

import androidx.compose.runtime.Immutable
import kotlin.time.Instant

@Immutable
data class ArticleSummary(
    val id: Int,
    val title: String,
    val url: String,
    val imageUrl: String,
    val newsSite: String,
    val summary: String,
    val publishedAt: Instant
)
