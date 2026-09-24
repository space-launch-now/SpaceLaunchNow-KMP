package me.calebjones.spacelaunchnow.data.repository.trantor

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getAgency
import me.calebjones.spacelaunchnow.api.extensions.trantor.listAgencies
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAgencyList
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Implementation of AgencyRepository using the Trantor AgenciesApi (`GET /agencies`).
 *
 * Trantor's `country_codes` filter (E6(P5)) takes a CSV of ISO alpha-2 codes, matching the
 * country multi-select filter in AgencyListViewModel one-to-one.
 */
class AgencyRepositoryImpl(
    private val agenciesApi: AgenciesApi
) : AgencyRepository {

    private suspend fun getAgenciesRaw(
        limit: Int,
        offset: Int,
        ordering: String?,
        search: String?,
        featured: Boolean?,
        typeId: Int?,
        countryCode: List<String>?
    ): Result<PaginatedResponseAgencyList> {
        return try {
            val response = agenciesApi.listAgencies(
                limit = limit,
                offset = offset,
                ordering = ordering,
                search = search,
                featured = featured,
                typeIds = typeId?.let { listOf(it) },
                // E6(P5): the server takes a CSV of codes; the UI multi-selects.
                countryCodes = countryCode
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun searchAgenciesRaw(searchQuery: String, limit: Int): Result<PaginatedResponseAgencyList> {
        return try {
            val response = agenciesApi.listAgencies(
                limit = limit,
                search = searchQuery,
                ordering = "-total_launch_count"
            )
            Result.success(response.body())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
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
            val response = agenciesApi.getAgency(agencyId = id)
            Result.success(response.body().toDomainAgency())
        } catch (e: ResponseException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
