package me.calebjones.spacelaunchnow.data.repository

import io.ktor.client.plugins.ResponseException
import kotlinx.io.IOException
import me.calebjones.spacelaunchnow.api.extensions.getAgencyList
import me.calebjones.spacelaunchnow.api.extensions.getConfigurationsByProgram
import me.calebjones.spacelaunchnow.api.extensions.getLocationList
import me.calebjones.spacelaunchnow.api.extensions.getProgramList
import me.calebjones.spacelaunchnow.api.extensions.getStatusList
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationFamiliesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.database.FilterOptionsLocalDataSource
import me.calebjones.spacelaunchnow.util.logging.logger

class ScheduleFilterRepositoryImpl(
    private val agenciesApi: AgenciesApi,
    private val programsApi: ProgramsApi,
    private val launcherConfigurationsApi: LauncherConfigurationsApi,
    private val launcherConfigurationFamiliesApi: LauncherConfigurationFamiliesApi,
    private val locationsApi: LocationsApi,
    private val configApi: ConfigApi,
    private val localDataSource: FilterOptionsLocalDataSource? = null
) : ScheduleFilterRepository {

    private val log = logger()

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
            val allAgencies = mutableListOf<AgencyNormal>()
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

            Result.success(allAgencies.map {
                FilterOption(
                    id = it.id,
                    name = it.name,
                    abbreviation = it.abbrev
                )
            })
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
            val allPrograms =
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal>()
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

            Result.success(allPrograms.map {
                FilterOption(
                    id = it.id,
                    name = it.name,
                    abbreviation = null
                )
            })
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
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed>()
            var offset = 0
            val limit = 100

            do {
                val response = launcherConfigurationsApi.getConfigurationsByProgram(
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

            // Clear old cache and insert fresh data
            localDataSource?.clearAllRockets()
            localDataSource?.cacheRockets(
                allRockets.map {
                    Triple(it.id, it.fullName ?: it.name, it.manufacturer?.abbrev)
                }
            )

            Result.success(allRockets.map {
                FilterOption(
                    id = it.id,
                    name = it.fullName ?: it.name,
                    abbreviation = it.manufacturer?.abbrev
                )
            })
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
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.LocationSerializerWithPads>()
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
                allLocations.map { Pair(it.id, it.name ?: "Unknown Location") }
            )

            Result.success(allLocations.map {
                FilterOption(
                    id = it.id,
                    name = it.name ?: "Unknown Location",
                    abbreviation = null
                )
            })
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

            // Fetch from API with pagination
            log.d { "Fetching statuses from API (ordering: name)" }
            val allStatuses =
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus>()
            var offset = 0
            val limit = 100

            do {
                val response = configApi.getStatusList(
                    limit = limit,
                    offset = offset,
                    ordering = "name"
                )
                val page = response.body()
                allStatuses.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} statuses (total: ${allStatuses.size}/${page.count})" }
            } while (allStatuses.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allStatuses.size} statuses" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllStatuses()
            localDataSource?.cacheStatuses(
                allStatuses.map {
                    me.calebjones.spacelaunchnow.database.Tuple4(
                        it.id,
                        it.name,
                        it.abbrev,
                        it.description
                    )
                }
            )

            Result.success(allStatuses.map {
                FilterOption(
                    id = it.id,
                    name = it.name,
                    abbreviation = it.abbrev
                )
            })
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

            // Fetch from API with pagination
            log.d { "Fetching orbits from API (ordering: name)" }
            val allOrbits =
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.Orbit>()
            var offset = 0
            val limit = 100

            do {
                val response = configApi.configOrbitsList(
                    limit = limit,
                    offset = offset,
                    ordering = "name"
                )
                val page = response.body()
                allOrbits.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} orbits (total: ${allOrbits.size}/${page.count})" }
            } while (allOrbits.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allOrbits.size} orbits" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllOrbits()
            localDataSource?.cacheOrbits(
                allOrbits.map { Triple(it.id, it.name, it.abbrev) }
            )

            Result.success(allOrbits.map {
                FilterOption(
                    id = it.id,
                    name = it.name,
                    abbreviation = it.abbrev
                )
            })
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

            // Fetch from API with pagination
            log.d { "Fetching mission types from API (ordering: name)" }
            val allMissionTypes =
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.MissionType>()
            var offset = 0
            val limit = 100

            do {
                val response = configApi.configMissionTypesList(
                    limit = limit,
                    offset = offset,
                    ordering = "name"
                )
                val page = response.body()
                allMissionTypes.addAll(page.results)
                offset += limit
                log.v { "Fetched ${page.results.size} mission types (total: ${allMissionTypes.size}/${page.count})" }
            } while (allMissionTypes.size < page.count)

            log.i { "✅ API SUCCESS: Fetched ${allMissionTypes.size} mission types" }

            // Clear old cache and insert fresh data
            localDataSource?.clearAllMissionTypes()
            localDataSource?.cacheMissionTypes(
                allMissionTypes.map { Pair(it.id, it.name ?: "Unknown Mission Type") }
            )

            Result.success(allMissionTypes.map {
                FilterOption(
                    id = it.id,
                    name = it.name ?: "Unknown Mission Type",
                    abbreviation = null
                )
            })
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
                mutableListOf<me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigFamilyNormal>()
            var offset = 0
            val limit = 100

            do {
                val response = launcherConfigurationFamiliesApi.launcherConfigurationFamiliesList(
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
                allLauncherConfigFamilies.map { Pair(it.id, it.name ?: "Unknown Family") }
            )

            Result.success(allLauncherConfigFamilies.map {
                FilterOption(
                    id = it.id,
                    name = it.name ?: "Unknown Family",
                    abbreviation = null
                )
            })
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
