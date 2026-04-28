package me.calebjones.spacelaunchnow.domain.model

data class PaginatedResult<T>(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<T> = emptyList()
)
