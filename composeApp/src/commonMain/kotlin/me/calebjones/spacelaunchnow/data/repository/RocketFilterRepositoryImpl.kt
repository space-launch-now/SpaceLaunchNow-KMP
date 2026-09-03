package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getFamilyList
import me.calebjones.spacelaunchnow.api.extensions.trantor.getProgramList
import me.calebjones.spacelaunchnow.api.trantor.apis.FamiliesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.ProgramsApi
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toFilterOption
import me.calebjones.spacelaunchnow.util.logging.logger

class RocketFilterRepositoryImpl(
    private val programsApi: ProgramsApi,
    private val familiesApi: FamiliesApi
) : RocketFilterRepository {

    private val log = logger()

    // Simple in-memory cache
    private var cachedPrograms: List<FilterOption>? = null
    private var cachedFamilies: List<FilterOption>? = null

    override suspend fun getPrograms(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getPrograms - forceRefresh: $forceRefresh" }

            // Return cache if available and not forcing refresh
            if (!forceRefresh && cachedPrograms != null) {
                log.i { "Cache hit - Returning ${cachedPrograms!!.size} cached programs" }
                return Result.success(cachedPrograms!!)
            }

            // Fetch from API
            log.d { "Fetching programs from API" }
            val allPrograms = mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.ProgramList>()
            var offset = 0
            val limit = 100

            do {
                log.d { "🚀 API Call: getProgramList(limit=$limit, offset=$offset, ordering=name)" }
                val response = programsApi.getProgramList(
                    limit = limit,
                    offset = offset,
                    ordering = "name"
                )
                val page = response.body()
                allPrograms.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} programs (total: ${allPrograms.size}/${page.count})" }
            } while (page.next != null && allPrograms.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allPrograms.size} programs" }

            val filterOptions = allPrograms.map { it.toFilterOption() }

            // Cache the result
            cachedPrograms = filterOptions

            Result.success(filterOptions)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getPrograms: ${e.message}" }

            // Return stale cache if available
            if (cachedPrograms != null) {
                log.w { "Using stale cache (${cachedPrograms!!.size} programs)" }
                return Result.success(cachedPrograms!!)
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getPrograms: ${e.message}" }

            // Return stale cache if available
            if (cachedPrograms != null) {
                log.w { "Using stale cache (${cachedPrograms!!.size} programs)" }
                return Result.success(cachedPrograms!!)
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getPrograms: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getFamilies(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getFamilies - forceRefresh: $forceRefresh" }

            // Return cache if available and not forcing refresh
            if (!forceRefresh && cachedFamilies != null) {
                log.i { "Cache hit - Returning ${cachedFamilies!!.size} cached families" }
                return Result.success(cachedFamilies!!)
            }

            // Fetch from API
            log.d { "Fetching launcher configuration families from API" }
            val allFamilies = mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.FamilyList>()
            var offset = 0
            val limit = 100

            do {
                log.d { "🚀 API Call: getFamilyList(limit=$limit, offset=$offset)" }
                val response = familiesApi.getFamilyList(
                    limit = limit,
                    offset = offset
                )
                val page = response.body()
                allFamilies.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} families (total: ${allFamilies.size}/${page.count})" }
            } while (page.next != null && allFamilies.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allFamilies.size} launcher configuration families" }

            val filterOptions = allFamilies.map { it.toFilterOption() }

            // Cache the result
            cachedFamilies = filterOptions

            Result.success(filterOptions)
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getFamilies: ${e.message}" }

            // Return stale cache if available
            if (cachedFamilies != null) {
                log.w { "Using stale cache (${cachedFamilies!!.size} families)" }
                return Result.success(cachedFamilies!!)
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getFamilies: ${e.message}" }

            // Return stale cache if available
            if (cachedFamilies != null) {
                log.w { "Using stale cache (${cachedFamilies!!.size} families)" }
                return Result.success(cachedFamilies!!)
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getFamilies: ${e.message}" }
            Result.failure(e)
        }
    }
}
