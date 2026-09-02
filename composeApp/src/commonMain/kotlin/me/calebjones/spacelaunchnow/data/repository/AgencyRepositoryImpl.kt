package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAgency
import me.calebjones.spacelaunchnow.api.extensions.listAgencies
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAgencyList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Implementation of AgencyRepository using the Trantor AgenciesApi (`GET /agencies`).
 *
 * Trantor's `country_code` filter takes a single ISO alpha-2 code, unlike LL's list param —
 * only the first selected code is honored when the caller (the country multi-select filter
 * in AgencyListViewModel) passes more than one; see the Phase 5 browse-vehicles escalation.
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
                countryCode = countryCode?.firstOrNull()
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
