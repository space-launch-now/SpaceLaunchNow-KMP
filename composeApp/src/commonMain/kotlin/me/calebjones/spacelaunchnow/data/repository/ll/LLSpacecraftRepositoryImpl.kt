package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getSpacecraft
import me.calebjones.spacelaunchnow.api.extensions.ll.getSpacecraftByConfig
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepository
import me.calebjones.spacelaunchnow.database.SpacecraftLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.ll.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import kotlin.time.Clock

/**
 * Spacecraft repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-
 * migration implementation, resurrected as a standalone class so the app can fall back to LL
 * in production via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT
 * [SpacecraftRepository] interface (domain-typed throughout), not the old LL-typed interface
 * that shipped alongside the original `SpacecraftRepositoryImpl` on `main`.
 *
 * Every network result is mapped LL -> domain via the existing `domain/mapper` extension
 * functions (declared for LL's `api.launchlibrary.models` types in `SpacecraftMappers.kt`,
 * untouched by the Trantor migration) before being cached through [SpacecraftLocalDataSource],
 * which - per the phase5 migration - only ever stores the domain [Spacecraft] model. This class
 * never constructs a [SpacecraftLocalDataSource] cache write from a raw LL wire model.
 *
 * LL's spacecraft list endpoints support an `ordering` query param that Trantor's does not; the
 * old orderings ("-id" for by-config, "-flights_count" for the general list) are restored here
 * since LL genuinely supports them.
 */
class LLSpacecraftRepositoryImpl(
    private val spacecraftApi: SpacecraftApi,
    private val localDataSource: SpacecraftLocalDataSource? = null
) : SpacecraftRepository {

    override suspend fun getSpacecraftByConfigDomain(
        configId: Int,
        limit: Int,
        forceRefresh: Boolean,
        isPlaceholder: Boolean?
    ): Result<DataResult<List<Spacecraft>>> {
        return try {
            println("=== LLSpacecraftRepository.getSpacecraftByConfigDomain ===")
            println("Parameters: configId=$configId, limit=$limit, forceRefresh=$forceRefresh")

            val now = Clock.System.now().toEpochMilliseconds()

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

            println("→ CACHE MISS: Fetching spacecraft from API...")
            val response = spacecraftApi.getSpacecraftByConfig(
                configId = configId,
                limit = limit,
                ordering = "-id",
                isPlaceholder = isPlaceholder
            )

            val spacecraftList = response.body().results
            println("✓ API SUCCESS: Fetched ${spacecraftList.size} spacecraft for config $configId")

            val domainSpacecraftList = spacecraftList.map { it.toDomain() }

            localDataSource?.cacheSpacecraftList(domainSpacecraftList)

            Result.success(
                DataResult(
                    data = domainSpacecraftList,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("LLSpacecraftRepository: API error for config $configId: ${e.message}")
            handleConfigError(e, configId, limit)
        } catch (e: IOException) {
            println("LLSpacecraftRepository: Network error for config $configId: ${e.message}")
            handleConfigError(e, configId, limit)
        } catch (e: Exception) {
            println("LLSpacecraftRepository: Unexpected error for config $configId: ${e.message}")
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
