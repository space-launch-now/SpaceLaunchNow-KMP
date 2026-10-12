package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.database.ProgramLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.ll.toDomainProgram
import me.calebjones.spacelaunchnow.domain.model.Program
import kotlin.time.Clock

/**
 * Program repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-
 * migration implementation, resurrected as a standalone class so the app can fall back to LL in
 * production via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT
 * [ProgramRepository] interface (domain-typed throughout).
 *
 * The network result is mapped LL `ProgramNormal` -> domain [Program] via the existing
 * `ProgramNormal.toDomainProgram()` extension in `domain/mapper/ProgramMappers.kt`, untouched by
 * the Trantor migration, before being cached through [ProgramLocalDataSource], which - per the
 * phase5 migration - only ever stores the domain [Program] model directly.
 */
class LLProgramRepositoryImpl(
    private val programsApi: ProgramsApi,
    private val localDataSource: ProgramLocalDataSource? = null
) : ProgramRepository {

    override suspend fun getProgramDomain(
        id: Int,
        forceRefresh: Boolean
    ): Result<DataResult<Program>> {
        return try {
            println("=== LLProgramRepository.getProgramDomain ===")
            println("Parameters: id=$id, forceRefresh=$forceRefresh")

            val now = Clock.System.now().toEpochMilliseconds()

            val staleTimestamp = localDataSource?.getCacheTimestamp(id)

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

            println("→ CACHE MISS: Fetching program from API...")
            val response = programsApi.programsRetrieve(id)
            val program = response.body()
            val domainProgram = program.toDomainProgram()

            localDataSource?.cacheProgram(domainProgram)
            println("✓ API SUCCESS: Fetched and cached program '${domainProgram.name}'")

            Result.success(
                DataResult(
                    data = domainProgram,
                    source = DataSource.NETWORK,
                    timestamp = now
                )
            )
        } catch (e: ResponseException) {
            println("LLProgramRepository: API error for program $id: ${e.message}")
            handleError(e, id)
        } catch (e: IOException) {
            println("LLProgramRepository: Network error for program $id: ${e.message}")
            handleError(e, id)
        } catch (e: Exception) {
            println("LLProgramRepository: Unexpected error for program $id: ${e.message}")
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
