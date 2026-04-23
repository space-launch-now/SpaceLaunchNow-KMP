package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

interface AgencyRepository {
    suspend fun getAgenciesDomain(
        limit: Int,
        offset: Int = 0,
        ordering: String? = null,
        search: String? = null,
        featured: Boolean? = null,
        typeId: Int? = null,
        countryCode: List<String>? = null
    ): Result<PaginatedResult<Agency>>

    suspend fun searchAgenciesDomain(
        searchQuery: String,
        limit: Int = 50
    ): Result<PaginatedResult<Agency>>

    /** Fetch detailed agency information mapped to the [Agency] domain type. */
    suspend fun getAgencyDetailDomain(id: Int): Result<Agency>
}