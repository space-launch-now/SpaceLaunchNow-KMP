package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.data.model.DataResult

interface ProgramRepository {
    /**
     * Get program details with caching support.
     * 
     * @param id The program ID
     * @param forceRefresh If true, bypass cache and fetch from API
     * @return DataResult containing the program and source metadata
     */
    suspend fun getProgram(
        id: Int,
        forceRefresh: Boolean = false
    ): Result<DataResult<ProgramNormal>>
}
