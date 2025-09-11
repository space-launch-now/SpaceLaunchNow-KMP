package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList

interface UpdatesRepository {
    suspend fun getLatestUpdates(limit: Int = 10): Result<PaginatedUpdateEndpointList>
}
