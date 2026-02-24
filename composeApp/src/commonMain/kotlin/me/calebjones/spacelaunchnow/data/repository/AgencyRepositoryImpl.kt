package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAgencyList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList

class AgencyRepositoryImpl(
    private val agenciesApi: AgenciesApi
) : AgencyRepository {

    override suspend fun getAgencies(
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

    override suspend fun searchAgencies(searchQuery: String, limit: Int): Result<PaginatedAgencyNormalList> {
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
}
