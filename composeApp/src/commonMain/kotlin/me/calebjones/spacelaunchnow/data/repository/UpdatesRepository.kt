package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.data.model.DataResult

interface UpdatesRepository {
    suspend fun getLatestUpdates(limit: Int = 10, forceRefresh: Boolean = false): Result<DataResult<PaginatedUpdateEndpointList>>
}
