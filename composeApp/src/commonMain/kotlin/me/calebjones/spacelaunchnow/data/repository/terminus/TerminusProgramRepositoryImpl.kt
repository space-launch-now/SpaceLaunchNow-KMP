package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.ProgramsApi
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.domain.mapper.terminus.toDomainProgram
import me.calebjones.spacelaunchnow.domain.model.Program
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Program repository for the Terminus backend (Tasks#1012). The one read is
 * `GET /api/v1/programs/{id}/`; Terminus adds `space_stations`, which the LL model does not
 * read and the bridge drops. Reads use the 30 s [TerminusMemoryCache] (stale kept for a 5xx or a
 * network failure); the SQLDelight program table holds LL rows and is not touched.
 */
class TerminusProgramRepositoryImpl internal constructor(
    private val programsApi: ProgramsApi,
    private val cache: TerminusMemoryCache
) : ProgramRepository {

    constructor(programsApi: ProgramsApi) : this(programsApi, TerminusMemoryCache())

    private val log = logger()

    override suspend fun getProgramDomain(id: Int, forceRefresh: Boolean): Result<DataResult<Program>> {
        log.d { "terminus program $id, forceRefresh: $forceRefresh" }
        return terminusMemoryCachedFetch(cache, "program?id=$id", "program $id", forceRefresh) {
            programsApi.programsRetrieve(id)
        }.mapCatching { DataResult(it.value.toDomainProgram(), it.source, it.timestamp) }
    }
}
