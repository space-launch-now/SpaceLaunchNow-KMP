package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getLaunchers
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherDetailedList
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.domain.mapper.ll.toDomain
import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Launcher (booster/first stage) repository, backed directly by the Launch Library 2 (LL) API -
 * the pre-Trantor-migration implementation, resurrected as a standalone class so the app can fall
 * back to LL in production via the `DataBackend` revert lever (see Koin wiring). Implements the
 * CURRENT [LauncherRepository] interface, which is unchanged from the pre-migration one - this
 * class is close to a straight port, moved to the `ll` package and renamed (`println` debug
 * output swapped for the shared [logger] used elsewhere in the `ll` package). LL wire types are
 * mapped to domain via the existing `domain/mapper` extension functions (`toDomain` for
 * `LauncherDetailed`/`PaginatedLauncherDetailedList` in `VehicleMappers.kt`), untouched by the
 * Trantor migration. This class has no local cache - matches the pre-migration behavior.
 *
 * Adaptation note: the pre-migration `getLaunchersRaw` always hardcoded `isPlaceholder = false`
 * against LL, ignoring its own `isPlaceholder` parameter (and the parameter the current
 * [LauncherRepository] interface declares). This restoration honors the passed-in value instead,
 * since silently overriding a caller's explicit filter would misbehave against the current
 * interface's contract.
 */
class LLLauncherRepositoryImpl(
    private val launchersApi: LaunchersApi
) : LauncherRepository {

    private val log = logger()

    private suspend fun getLaunchersRaw(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?,
        isPlaceholder: Boolean?
    ): Result<PaginatedLauncherDetailedList> {
        return try {
            log.d { "getLaunchersRaw - limit: $limit, offset: $offset, search: $search, ordering: $ordering, configId: $launcherConfigId" }

            val response = launchersApi.getLaunchers(
                limit = limit,
                offset = offset,
                search = search,
                ordering = ordering,
                isPlaceholder = isPlaceholder,
                launcherConfigIds = launcherConfigId?.let { listOf(it) }
            )

            val launchers = response.body()
            log.i { "API SUCCESS: Fetched ${launchers.results.size} launchers (offset: $offset)" }

            Result.success(launchers)
        } catch (e: ResponseException) {
            log.e(e) { "API error in getLaunchersRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getLaunchersRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getLaunchersRaw: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun getLaunchersByConfigRaw(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedLauncherDetailedList> {
        return getLaunchersRaw(
            limit = limit,
            offset = offset,
            search = null,
            ordering = "-id",
            isPlaceholder = false,
            launcherConfigId = configId
        )
    }

    private suspend fun getLauncherDetailsRaw(launcherId: Int): Result<LauncherDetailed> {
        return try {
            log.d { "getLauncherDetailsRaw - launcherId: $launcherId" }

            val response = launchersApi.launchersRetrieve(launcherId)
            val launcher = response.body()

            log.i { "API SUCCESS: Fetched launcher details for ID $launcherId" }

            Result.success(launcher)
        } catch (e: ResponseException) {
            log.e(e) { "API error in getLauncherDetailsRaw for launcher $launcherId: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getLauncherDetailsRaw for launcher $launcherId: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getLauncherDetailsRaw for launcher $launcherId: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getLaunchersDomain(
        limit: Int,
        offset: Int,
        search: String?,
        ordering: String?,
        launcherConfigId: Int?,
        isPlaceholder: Boolean?
    ): Result<PaginatedResult<LauncherDetail>> = getLaunchersRaw(
        limit = limit,
        offset = offset,
        search = search,
        ordering = ordering,
        launcherConfigId = launcherConfigId,
        isPlaceholder = isPlaceholder
    ).map { it.toDomain() }

    override suspend fun getLaunchersByConfigDomain(
        configId: Int,
        limit: Int,
        offset: Int
    ): Result<PaginatedResult<LauncherDetail>> =
        getLaunchersByConfigRaw(configId = configId, limit = limit, offset = offset).map { it.toDomain() }

    override suspend fun getLauncherDetailsDomain(launcherId: Int): Result<LauncherDetail> =
        getLauncherDetailsRaw(launcherId).map { it.toDomain() }
}
