package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAgencyList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainAgency
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

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
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
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
            val response = agenciesApi.agenciesRetrieve(id)
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