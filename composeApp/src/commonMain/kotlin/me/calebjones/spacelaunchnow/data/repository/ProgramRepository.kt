package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.Program

interface ProgramRepository {
    suspend fun getProgramDomain(
        id: Int,
        forceRefresh: Boolean = false
    ): Result<DataResult<Program>>
}