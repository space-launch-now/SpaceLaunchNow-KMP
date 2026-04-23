package me.calebjones.spacelaunchnow.domain.model

data class EventFilterParams(
    val typeIds: List<Int> = emptyList(),
    val programIds: List<Int> = emptyList(),
    val agencyIds: List<Int> = emptyList(),
    val search: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
    val upcoming: Boolean? = null
)
