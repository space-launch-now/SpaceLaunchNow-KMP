package me.calebjones.spacelaunchnow.domain.model

data class LaunchFilterParams(
    val statusIds: List<Int> = emptyList(),
    val providerIds: List<Int> = emptyList(),
    val locationIds: List<Int> = emptyList(),
    val rocketConfigIds: List<Int> = emptyList(),
    val programIds: List<Int> = emptyList(),
    val orbitIds: List<Int> = emptyList(),
    val includeSuborbital: Boolean? = null,
    val search: String? = null,
    val limit: Int = 20,
    val offset: Int = 0,
    val ordering: String? = null,
    val upcoming: Boolean? = null
)
