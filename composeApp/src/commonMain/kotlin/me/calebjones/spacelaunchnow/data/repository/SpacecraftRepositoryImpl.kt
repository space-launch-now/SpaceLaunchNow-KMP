package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getSpacecraft
import me.calebjones.spacelaunchnow.api.extensions.getSpacecraftByConfig
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.SpacecraftLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import kotlin.time.Clock

class SpacecraftRepositoryImpl(
    private val spacecraftApi: SpacecraftApi,
    private val localDataSource: SpacecraftLocalDataSource? = null
) : SpacecraftRepository {

    // ============ Domain methods (own the cache) ============

    override suspend fun getSpacecraftByConfigDomain(
        configId: Int,
        limit: Int,
        forceRefresh: Boolean,
        isPlaceholder: Boolean?
    ): Result<DataResult<List<Spacecraft>>> {
        return try {
            println("=== SpacecraftRepository.getSpacecraftByConfigDomain ===")
            println("Parameters: configId=$configId, limit=$limit, forceRefresh=$forceRefresh")

            val now = Clock.System.now().toEpochMilliseconds()

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedSpacecraft = localDataSource?.getSpacecraftByConfigId(configId, limit)
                val cacheTimestamp = localDataSource?.getCacheTimestamp()
                if (!cachedSpacecraft.isNullOrEmpty()) {
                    println("✓ CACHE HIT: Returning ${cachedSpacecraft.size} cached spacecraft")
                    return Result.success(
                        DataResult(
                            data = cachedSpacecraft,
                            source = DataSource.CACHE,
                            timestamp = cacheTimestamp
                        )
                    )
                }
            }

            // Cache miss or force refresh - fetch from API
            println("→ CACHE MISS: Fetching spacecraft from API...")
            val response = spacecraftApi.getSpacecraftByConfig(
                configId = configId,
                limit = limit,
                ordering = "-id",
                isPlaceholder = isPlaceholder
            )

            val spacecraftList = response.body().results
            println("✓ API SUCCESS: Fetched ${spacecraftList.size} spacecraft for config $configId")

            // Cache the raw API payload
            localDataSource?.cacheSpacecraftList(spacecraftList)

            Result.success(
                DataResult(
                    data = spacecraftList.map { it.toDomain() },
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("SpacecraftRepository: API error for config $configId: ${e.message}")
            handleConfigError(e, configId, limit)
        } catch (e: IOException) {
            println("SpacecraftRepository: Network error for config $configId: ${e.message}")
            handleConfigError(e, configId, limit)
        } catch (e: Exception) {
            println("SpacecraftRepository: Unexpected error for config $configId: ${e.message}")
            handleConfigError(e, configId, limit)
        }
    }

    private suspend fun handleConfigError(
        e: Exception,
        configId: Int,
        limit: Int
    ): Result<DataResult<List<Spacecraft>>> {
        val staleCached = localDataSource?.getSpacecraftByConfigIdStale(configId, limit)
        val staleTimestamp = localDataSource?.getCacheTimestamp()

        return if (!staleCached.isNullOrEmpty()) {
            println("⚠️ STALE FALLBACK: Returning ${staleCached.size} stale cached spacecraft")
            Result.success(
                DataResult(
                    data = staleCached,
                    source = DataSource.STALE_CACHE,
                    timestamp = staleTimestamp
                )
            )
        } else {
            println("✗ NO CACHE: Cannot recover from error, no stale data available")
            Result.failure(e)
        }
    }

    override suspend fun getSpacecraftDetailsDomain(spacecraftId: Int): Result<Spacecraft> {
        return try {
            val response = spacecraftApi.spacecraftRetrieve(spacecraftId)
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSpacecraftDomain(
        limit: Int,
        offset: Int,
        inSpace: Boolean?,
        search: String?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<Spacecraft>> {
        return try {
            val response = spacecraftApi.getSpacecraft(
                limit = limit,
                offset = offset,
                inSpace = inSpace,
                search = search,
                ordering = "-flights_count",
                isPlaceholder = isPlaceholder
            )
            Result.success(response.body().toDomain())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}

