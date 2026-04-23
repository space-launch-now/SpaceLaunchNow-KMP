package me.calebjones.spacelaunchnow.data.repository

import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft

interface SpacecraftRepository {

    suspend fun getSpacecraftByConfigDomain(
        configId: Int,
        limit: Int = 20,
        forceRefresh: Boolean = false,
        isPlaceholder: Boolean? = null
    ): Result<DataResult<List<Spacecraft>>>

    suspend fun getSpacecraftDetailsDomain(spacecraftId: Int): Result<Spacecraft>

    suspend fun getSpacecraftDomain(
        limit: Int = 20,
        offset: Int = 0,
        inSpace: Boolean? = null,
        search: String? = null,
        isPlaceholder: Boolean? = null
    ): Result<PaginatedResult<Spacecraft>>
}