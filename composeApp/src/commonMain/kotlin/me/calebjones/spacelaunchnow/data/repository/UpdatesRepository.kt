package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Update

interface UpdatesRepository {
    suspend fun getLatestUpdates(
        limit: Int = 10,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedResult<Update>>>

    suspend fun getUpdatesByProgram(
        allProgram: Int,
        limit: Int = 20,
        forceRefresh: Boolean = false
    ): Result<DataResult<PaginatedResult<Update>>>
}
