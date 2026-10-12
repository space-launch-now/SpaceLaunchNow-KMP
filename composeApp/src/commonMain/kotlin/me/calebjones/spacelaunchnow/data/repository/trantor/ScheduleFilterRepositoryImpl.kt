package me.calebjones.spacelaunchnow.data.repository.trantor

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.trantor.getAgencyList
import me.calebjones.spacelaunchnow.api.extensions.trantor.getConfigurationList
import me.calebjones.spacelaunchnow.api.extensions.trantor.getFamilyList
import me.calebjones.spacelaunchnow.api.extensions.trantor.getLocationList
import me.calebjones.spacelaunchnow.api.extensions.trantor.getLookups
import me.calebjones.spacelaunchnow.api.extensions.trantor.getProgramList
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.FamiliesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LookupsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.trantor.models.LookupsResponse
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.database.FilterOptionsLocalDataSource
import me.calebjones.spacelaunchnow.domain.mapper.trantor.toFilterOption
import me.calebjones.spacelaunchnow.util.logging.logger

class ScheduleFilterRepositoryImpl(
    private val agenciesApi: AgenciesApi,
    private val programsApi: ProgramsApi,
    private val launcherConfigurationsApi: LauncherConfigurationsApi,
    private val familiesApi: FamiliesApi,
    private val locationsApi: LocationsApi,
    private val lookupsApi: LookupsApi,
    private val localDataSource: FilterOptionsLocalDataSource? = null
) : ScheduleFilterRepository {

    private val log = logger()

    // The five status/orbit/mission-type picker calls collapse into one GET /lookups
    // payload (contract: "Lookups (one call replaces five)"). Cache it in memory so
    // getStatuses/getOrbits/getMissionTypes share a single network round trip.
    private var cachedLookupsResponse: LookupsResponse? = null

    private suspend fun fetchLookups(forceRefresh: Boolean): LookupsResponse {
        if (!forceRefresh) {
            cachedLookupsResponse?.let { return it }
        }
        log.d { "🚀 API Call: getLookups()" }
        val response = lookupsApi.getLookups()
        val body = response.body()
        cachedLookupsResponse = body
        return body
    }

    override suspend fun getAgencies(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getAgencies - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllAgencies()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached agencies" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = it.abbreviation
                        )
                    })
                }
            }

            // Fetch from API with pagination
            log.d { "Fetching agencies from API (ordering: name, featured: true)" }
            val allAgencies = mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.AgencyList>()
            var offset = 0
            val limit = 100

            do {
                val response = agenciesApi.getAgencyList(
                    limit = limit,
                    offset = offset,
                    ordering = "name",
                    featured = true // Only get featured agencies
                )
                val page = response.body()
                allAgencies.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} agencies (total: ${allAgencies.size}/${page.count})" }
            } while (allAgencies.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allAgencies.size} agencies" }

            // Clear old cache and insert fresh featured agencies only
            localDataSource?.clearAllAgencies()
            localDataSource?.cacheAgencies(
                allAgencies.map { Triple(it.id, it.name, it.abbrev) }
            )

            Result.success(allAgencies.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getAgencies: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllAgenciesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} agencies)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getAgencies: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllAgenciesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} agencies)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getAgencies: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getPrograms(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getPrograms - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllPrograms()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached programs" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = null
                        )
                    })
                }
            }

            // Fetch from API with pagination
            log.d { "Fetching programs from API (ordering: name)" }
            val allPrograms = mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.ProgramList>()
            var offset = 0
            val limit = 100

            do {
                val response = programsApi.getProgramList(
                    limit = limit,
                    offset = offset,
                    ordering = "name"
                )
                val page = response.body()
                allPrograms.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} programs (total: ${allPrograms.size}/${page.count})" }
            } while (allPrograms.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allPrograms.size} programs" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllPrograms()
            localDataSource?.cachePrograms(
                allPrograms.map { Triple(it.id, it.name, null) }
            )

            Result.success(allPrograms.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getPrograms: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllProgramsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} programs)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getPrograms: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllProgramsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} programs)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getPrograms: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getRockets(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getRockets - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllRockets()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached rockets" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = it.abbreviation
                        )
                    })
                }
            }

            // Fetch from API with pagination
            log.d { "Fetching rocket configurations from API (ordering: name)" }
            val allRockets =
                mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigSummary>()
            var offset = 0
            val limit = 100

            do {
                val response = launcherConfigurationsApi.getConfigurationList(
                    limit = limit,
                    offset = offset,
                    ordering = "name",
                    isPlaceholder = false
                )
                val page = response.body()
                allRockets.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} rockets (total: ${allRockets.size}/${page.count})" }
            } while (allRockets.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allRockets.size} rocket configurations" }

            // Clear old cache and insert fresh data. Trantor's flat configuration row
            // carries no manufacturer abbreviation (only manufacturerId) — see
            // FilterOptionMappers.LauncherConfigSummary.toFilterOption.
            localDataSource?.clearAllRockets()
            localDataSource?.cacheRockets(
                allRockets.map { Triple(it.id, it.fullName ?: it.name, null) }
            )

            Result.success(allRockets.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getRockets: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllRocketsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} rockets)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getRockets: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllRocketsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} rockets)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getRockets: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getLocations(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getLocations - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllLocations()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached locations" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = null
                        )
                    })
                }
            }

            // Fetch from API with pagination
            log.d { "Fetching locations from API (ordering: name, active: true)" }
            val allLocations =
                mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.LocationList>()
            var offset = 0
            val limit = 100

            do {
                val response = locationsApi.getLocationList(
                    limit = limit,
                    offset = offset,
                    ordering = "name",
                    active = true // Only get active locations
                )
                val page = response.body()
                allLocations.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} locations (total: ${allLocations.size}/${page.count})" }
            } while (allLocations.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allLocations.size} locations" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllLocations()
            localDataSource?.cacheLocations(
                allLocations.map { Pair(it.id, it.name) }
            )

            Result.success(allLocations.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getLocations: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllLocationsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} locations)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getLocations: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllLocationsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} locations)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getLocations: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getStatuses(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getStatuses - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllStatuses()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached statuses" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = it.abbreviation
                        )
                    })
                }
            }

            log.d { "Fetching statuses from /lookups" }
            val lookups = fetchLookups(forceRefresh)
            val statuses = lookups.launchStatuses

            log.i { "✅ API SUCCESS: Fetched ${statuses.size} statuses" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllStatuses()
            localDataSource?.cacheStatuses(
                statuses.map {
                    me.calebjones.spacelaunchnow.database.Tuple4(
                        it.id,
                        it.name,
                        it.abbrev,
                        null
                    )
                }
            )

            Result.success(statuses.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getStatuses: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllStatusesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} statuses)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getStatuses: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllStatusesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} statuses)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getStatuses: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getOrbits(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getOrbits - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllOrbits()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached orbits" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = it.abbreviation
                        )
                    })
                }
            }

            log.d { "Fetching orbits from /lookups" }
            val lookups = fetchLookups(forceRefresh)
            val orbits = lookups.orbits

            log.i { "✅ API SUCCESS: Fetched ${orbits.size} orbits" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllOrbits()
            localDataSource?.cacheOrbits(
                orbits.map { Triple(it.id, it.name, it.abbrev) }
            )

            Result.success(orbits.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getOrbits: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllOrbitsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} orbits)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getOrbits: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllOrbitsStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} orbits)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = it.abbreviation
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getOrbits: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getMissionTypes(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getMissionTypes - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllMissionTypes()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached mission types" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = null
                        )
                    })
                }
            }

            log.d { "Fetching mission types from /lookups" }
            val lookups = fetchLookups(forceRefresh)
            val missionTypes = lookups.missionTypes

            log.i { "✅ API SUCCESS: Fetched ${missionTypes.size} mission types" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllMissionTypes()
            localDataSource?.cacheMissionTypes(
                missionTypes.map { Pair(it.id, it.name) }
            )

            Result.success(missionTypes.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getMissionTypes: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllMissionTypesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} mission types)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getMissionTypes: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllMissionTypesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} mission types)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getMissionTypes: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getLauncherConfigFamilies(forceRefresh: Boolean): Result<List<FilterOption>> {
        return try {
            log.d { "getLauncherConfigFamilies - forceRefresh: $forceRefresh" }

            // Try cache first if not forcing refresh
            if (!forceRefresh) {
                val cached = localDataSource?.getAllLauncherConfigFamilies()
                if (cached != null && cached.isNotEmpty()) {
                    log.i { "Cache hit - Returning ${cached.size} cached launcher config families" }
                    return Result.success(cached.map {
                        FilterOption(
                            id = it.id.toInt(),
                            name = it.name,
                            abbreviation = null
                        )
                    })
                }
            }

            // Fetch from API with pagination
            log.d { "Fetching launcher config families from API (ordering: name)" }
            val allLauncherConfigFamilies =
                mutableListOf<me.calebjones.spacelaunchnow.api.trantor.models.FamilyList>()
            var offset = 0
            val limit = 100

            do {
                val response = familiesApi.getFamilyList(
                    limit = limit,
                    offset = offset,
                    ordering = "name"
                )
                val page = response.body()
                allLauncherConfigFamilies.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} launcher config families (total: ${allLauncherConfigFamilies.size}/${page.count})" }
            } while (allLauncherConfigFamilies.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allLauncherConfigFamilies.size} launcher config families" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllLauncherConfigFamilies()
            localDataSource?.cacheLauncherConfigFamilies(
                allLauncherConfigFamilies.map { Pair(it.id, it.name) }
            )

            Result.success(allLauncherConfigFamilies.map { it.toFilterOption() })
        } catch (e: ResponseException) {
            log.e(e) { "❌ API ERROR in getLauncherConfigFamilies: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllLauncherConfigFamiliesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} launcher config families)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: IOException) {
            log.e(e) { "❌ NETWORK ERROR in getLauncherConfigFamilies: ${e.message}" }

            // Try stale cache as fallback
            val stale = localDataSource?.getAllLauncherConfigFamiliesStale()
            if (stale != null && stale.isNotEmpty()) {
                log.w { "Using stale cache (${stale.size} launcher config families)" }
                return Result.success(stale.map {
                    FilterOption(
                        id = it.id.toInt(),
                        name = it.name,
                        abbreviation = null
                    )
                })
            }

            Result.failure(e)
        } catch (e: Exception) {
            log.e(e) { "❌ UNEXPECTED ERROR in getLauncherConfigFamilies: ${e::class.simpleName}: ${e.message}" }
            Result.failure(e)
        }
    }
}
