package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getRocketDetails
import me.calebjones.spacelaunchnow.api.extensions.getRocketList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.util.logging.logger

class RocketRepositoryImpl(
    private val launcherConfigurationsApi: LauncherConfigurationsApi
) : RocketRepository {

    private val log = logger()

    override suspend fun getRockets(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        programIds: List<Int>?,
        familyIds: List<Int>?,
        active: Boolean?,
        reusable: Boolean?
    ): Result<PaginatedLauncherConfigNormalList> {
        return try {
            log.d { 
                "🚀 API Call: getRockets(limit=$limit, offset=$offset, ordering=$ordering, " +
                "search=$search, programIds=$programIds, familyIds=$familyIds, active=$active, reusable=$reusable)"
            }
            
            val response = launcherConfigurationsApi.getRocketList(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                active = active,
                reusable = reusable,
                program = programIds,
                families = familyIds
            )
            val body = response.body()
            log.i { "✅ API SUCCESS: Received ${body.results.size} rockets (total count: ${body.count}, next: ${body.next != null})" }
            
            Result.success(body)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getRockets: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getRockets: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getRockets: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getRocketDetails(id: Int): Result<LauncherConfigDetailed> {
        return try {
            log.d { "🚀 API Call: getRocketDetails(id=$id)" }
            val response = launcherConfigurationsApi.getRocketDetails(id = id)
            log.i { "✅ API SUCCESS: Received rocket details for '${response.body().name}'" }
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getRocketDetails: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getRocketDetails: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getRocketDetails: ${e.message}" }
            Result.failure(e)
        }
    }
}
