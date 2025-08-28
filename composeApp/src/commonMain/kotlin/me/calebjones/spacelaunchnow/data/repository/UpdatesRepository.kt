package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.models.PaginatedUpdateEndpointList

interface UpdatesRepository {
    suspend fun getLatestUpdates(limit: Int = 10): Result<PaginatedUpdateEndpointList>
}
