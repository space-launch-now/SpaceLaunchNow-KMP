package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getProgramList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationFamiliesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.util.logging.logger

class RocketFilterRepositoryImpl(
    private val programsApi: ProgramsApi,
    private val launcherConfigurationFamiliesApi: LauncherConfigurationFamiliesApi
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
            val allPrograms = mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal>()
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
            } while (page.next != null && allPrograms.size < (page.count ?: 0))

            log.i { "✅ API SUCCESS: Fetched ${allPrograms.size} programs" }

            val filterOptions = allPrograms.map {
                FilterOption(
                    id = it.id,
                    name = it.name,
                    abbreviation = null // ProgramNormal doesn't have abbreviation
                )
            }

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
            val allFamilies = mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigFamilyNormal>()
            var offset = 0
            val limit = 100

            do {
                log.d { "🚀 API Call: launcherConfigurationFamiliesList(limit=$limit, offset=$offset)" }
                val response = launcherConfigurationFamiliesApi.launcherConfigurationFamiliesList(
                    limit = limit,
                    offset = offset
                )
                val page = response.body()
                allFamilies.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} families (total: ${allFamilies.size}/${page.count})" }
            } while (page.next != null && allFamilies.size < (page.count ?: 0))

            log.i { "✅ API SUCCESS: Fetched ${allFamilies.size} launcher configuration families" }

            val filterOptions = allFamilies.map { family ->
                FilterOption(
                    id = family.id,
                    name = family.name,
                    abbreviation = null // Families don't typically have abbreviations
                )
            }

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
