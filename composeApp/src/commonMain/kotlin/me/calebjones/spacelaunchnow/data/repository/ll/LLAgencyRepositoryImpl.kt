package me.calebjones.spacelaunchnow.data.repository.ll

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.ll.getAgencyList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Agency repository, backed directly by the Launch Library 2 (LL) API - the pre-Trantor-migration
 * implementation, resurrected as a standalone class so the app can fall back to LL in production
 * via the `DataBackend` revert lever (see Koin wiring). Implements the CURRENT [AgencyRepository]
 * interface, which is unchanged from the pre-migration one - this class is close to a straight
 * port, moved to the `ll` package and renamed. LL wire types are mapped to domain via the
 * existing `domain/mapper` extension functions (`toDomain` for `PaginatedAgencyNormalList` in
 * `AgencyMappers.kt`, `toDomainAgency` for `AgencyEndpointDetailed` in the same file), untouched
 * by the Trantor migration. This class has no local cache - matches the pre-migration behavior.
 * LL's `/agencies` list rows and detail payload carry type/country/logo natively (LL is the
 * richer, original source), so nothing here is thinned out relative to Trantor's shape.
 */
class LLAgencyRepositoryImpl(
    private val agenciesApi: AgenciesApi
) : AgencyRepository {

    private val log = logger()

    private suspend fun getAgenciesRaw(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        featured: Boolean?,
        typeId: Int?,
        countryCode: List<String>?
    ): Result<PaginatedAgencyNormalList> {
        return try {
            val response = agenciesApi.getAgencyList(
                limit = limit,
                offset = offset,
                ordering = ordering,
                search = search,
                featured = featured,
                typeId = typeId,
                countryCode = countryCode
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getAgenciesRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getAgenciesRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getAgenciesRaw: ${e.message}" }
            Result.failure(e)
        }
    }

    private suspend fun searchAgenciesRaw(searchQuery: String, limit: Int): Result<PaginatedAgencyNormalList> {
        return try {
            val response = agenciesApi.getAgencyList(
                limit = limit,
                search = searchQuery,
                ordering = "-total_launch_count"
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            log.e(e) { "API error in searchAgenciesRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in searchAgenciesRaw: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in searchAgenciesRaw: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getAgenciesDomain(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        featured: Boolean?,
        typeId: Int?,
        countryCode: List<String>?
    ): Result<PaginatedResult<Agency>> = getAgenciesRaw(
        limit = limit,
        offset = offset,
        ordering = ordering,
        search = search,
        featured = featured,
        typeId = typeId,
        countryCode = countryCode
    ).map { it.toDomain() }

    override suspend fun searchAgenciesDomain(
        searchQuery: String,
        limit: Int
    ): Result<PaginatedResult<Agency>> =
        searchAgenciesRaw(searchQuery = searchQuery, limit = limit).map { it.toDomain() }

    override suspend fun getAgencyDetailDomain(id: Int): Result<Agency> {
        return try {
            val response = agenciesApi.agenciesRetrieve(id)
            Result.success(response.body().toDomainAgency())
        } catch (e: ResponseException) {
            log.e(e) { "API error in getAgencyDetailDomain for ID $id: ${e.message}" }
            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "Network error in getAgencyDetailDomain for ID $id: ${e.message}" }
            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "Unexpected error in getAgencyDetailDomain for ID $id: ${e.message}" }
            Result.failure(e)
        }
    }
}
