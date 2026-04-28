package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.database.ProgramLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.toDomainProgram
import me.calebjones.spacelaunchnow.domain.model.Program
import kotlin.time.Clock

class ProgramRepositoryImpl(
    private val programsApi: ProgramsApi,
    private val localDataSource: ProgramLocalDataSource? = null
) : ProgramRepository {

    // ============ Domain method (owns the cache) ============

    override suspend fun getProgramDomain(
        id: Int,
        forceRefresh: Boolean
    ): Result<DataResult<Program>> {
        return try {
            println("=== ProgramRepository.getProgramDomain ===")
            println("Parameters: id=$id, forceRefresh=$forceRefresh")

            val now = Clock.System.now().toEpochMilliseconds()

            // STALE-WHILE-REVALIDATE: Check for stale data first (for fallback)
            val staleTimestamp = localDataSource?.getCacheTimestamp(id)

            // Try fresh cache if available and not forcing refresh
            if (!forceRefresh) {
                val cachedProgram = localDataSource?.getProgram(id)
                if (cachedProgram != null) {
                    println("✓ CACHE HIT: Returning fresh cached program '${cachedProgram.name}'")
                    return Result.success(
                        DataResult(
                            data = cachedProgram,
                            source = DataSource.CACHE,
                            timestamp = staleTimestamp
                        )
                    )
                }
            }

            // Cache miss or force refresh - fetch from API
            println("→ CACHE MISS: Fetching program from API...")
            val response = programsApi.programsRetrieve(id)
            val program = response.body()

            // Cache the raw API payload
            localDataSource?.cacheProgram(program)
            println("✓ API SUCCESS: Fetched and cached program '${program.name}'")

            Result.success(
                DataResult(
                    data = program.toDomainProgram(),
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("ProgramRepository: API error for program $id: ${e.message}")
            handleError(e, id)
        } catch (e: IOException) {
            println("ProgramRepository: Network error for program $id: ${e.message}")
            handleError(e, id)
        } catch (e: Exception) {
            println("ProgramRepository: Unexpected error for program $id: ${e.message}")
            handleError(e, id)
        }
    }

    /**
     * Handle errors with stale cache fallback.
     */
    private suspend fun handleError(e: Exception, id: Int): Result<DataResult<Program>> {
        val staleCached = localDataSource?.getProgramStale(id)
        val staleTimestamp = localDataSource?.getCacheTimestamp(id)

        return if (staleCached != null) {
            println("⚠️ STALE FALLBACK: Returning stale cached program due to error")
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

}
