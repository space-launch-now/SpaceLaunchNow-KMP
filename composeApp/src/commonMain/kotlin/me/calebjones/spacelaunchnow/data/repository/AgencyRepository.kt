package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList

interface AgencyRepository {
    suspend fun getAgencies(limit: Int, offset: Int = 0): Result<PaginatedAgencyNormalList>
    suspend fun searchAgencies(searchQuery: String, limit: Int = 50): Result<PaginatedAgencyNormalList>
}
