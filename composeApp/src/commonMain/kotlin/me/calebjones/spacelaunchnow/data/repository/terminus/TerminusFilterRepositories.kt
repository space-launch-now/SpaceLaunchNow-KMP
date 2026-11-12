package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationFamiliesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ProgramsApi
import me.calebjones.spacelaunchnow.data.model.FilterOption
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository

/*
 * Filter-option repositories for the Terminus backend (Tasks#1010 S4). They match the LL
 * repositories: same ordering, same filters (featured agencies, active locations, rockets that
 * are not placeholders), same labels, same FilterOption output. Each reads through
 * TerminusFilterFetcher.
 */

private const val UNKNOWN_LOCATION = "Unknown Location"
private const val UNKNOWN_MISSION_TYPE = "Unknown Mission Type"

class TerminusScheduleFilterRepositoryImpl internal constructor(
    private val agenciesApi: AgenciesApi,
    private val programsApi: ProgramsApi,
    private val launcherConfigurationsApi: LauncherConfigurationsApi,
    private val launcherConfigurationFamiliesApi: LauncherConfigurationFamiliesApi,
    private val locationsApi: LocationsApi,
    private val configApi: ConfigApi,
    private val fetcher: TerminusFilterFetcher
) : ScheduleFilterRepository {

    constructor(
        agenciesApi: AgenciesApi,
        programsApi: ProgramsApi,
        launcherConfigurationsApi: LauncherConfigurationsApi,
        launcherConfigurationFamiliesApi: LauncherConfigurationFamiliesApi,
        locationsApi: LocationsApi,
        configApi: ConfigApi
    ) : this(
        agenciesApi, programsApi, launcherConfigurationsApi, launcherConfigurationFamiliesApi,
        locationsApi, configApi, TerminusFilterFetcher()
    )

    override suspend fun getAgencies(forceRefresh: Boolean) = fetcher.options(
        "agencies", forceRefresh,
        { limit, offset -> agenciesApi.agenciesMiniList(limit = limit, offset = offset, ordering = "name", featured = true) }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name, it.abbrev) }, page.count) }

    override suspend fun getPrograms(forceRefresh: Boolean) = programs(programsApi, fetcher, forceRefresh)

    override suspend fun getRockets(forceRefresh: Boolean) = fetcher.options(
        "rockets", forceRefresh,
        { limit, offset ->
            launcherConfigurationsApi.launcherConfigurationsList(
                limit = limit, offset = offset, ordering = "name", isPlaceholder = false
            )
        }
    ) { page ->
        FilterPage(
            page.results.map { FilterOption(it.id, it.fullName ?: it.name, it.manufacturer?.abbrev) },
            page.count
        )
    }

    override suspend fun getLocations(forceRefresh: Boolean) = fetcher.options(
        "locations", forceRefresh,
        { limit, offset -> locationsApi.locationsList(limit = limit, offset = offset, ordering = "name", active = true) }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name ?: UNKNOWN_LOCATION) }, page.count) }

    override suspend fun getStatuses(forceRefresh: Boolean) = fetcher.options(
        "launch-statuses", forceRefresh,
        { limit, offset -> configApi.configLaunchStatusesList(limit = limit, offset = offset, ordering = "name") }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name, it.abbrev) }, page.count) }

    override suspend fun getOrbits(forceRefresh: Boolean) = fetcher.options(
        "orbits", forceRefresh,
        { limit, offset -> configApi.configOrbitsList(limit = limit, offset = offset, ordering = "name") }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name, it.abbrev) }, page.count) }

    override suspend fun getMissionTypes(forceRefresh: Boolean) = fetcher.options(
        "mission-types", forceRefresh,
        { limit, offset -> configApi.configMissionTypesList(limit = limit, offset = offset, ordering = "name") }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name ?: UNKNOWN_MISSION_TYPE) }, page.count) }

    override suspend fun getLauncherConfigFamilies(forceRefresh: Boolean) =
        launcherFamilies(launcherConfigurationFamiliesApi, fetcher, forceRefresh)
}

class TerminusRocketFilterRepositoryImpl internal constructor(
    private val programsApi: ProgramsApi,
    private val launcherConfigurationFamiliesApi: LauncherConfigurationFamiliesApi,
    private val fetcher: TerminusFilterFetcher
) : RocketFilterRepository {

    constructor(
        programsApi: ProgramsApi,
        launcherConfigurationFamiliesApi: LauncherConfigurationFamiliesApi
    ) : this(programsApi, launcherConfigurationFamiliesApi, TerminusFilterFetcher())

    override suspend fun getPrograms(forceRefresh: Boolean) = programs(programsApi, fetcher, forceRefresh)

    override suspend fun getFamilies(forceRefresh: Boolean) =
        launcherFamilies(launcherConfigurationFamiliesApi, fetcher, forceRefresh)
}

class TerminusAstronautFilterRepositoryImpl internal constructor(
    private val configApi: ConfigApi,
    private val fetcher: TerminusFilterFetcher
) : AstronautFilterRepository {

    constructor(configApi: ConfigApi) : this(configApi, TerminusFilterFetcher())

    override suspend fun getStatuses(forceRefresh: Boolean) = fetcher.options(
        "astronaut-statuses", forceRefresh,
        { limit, offset -> configApi.configAstronautStatusesList(limit = limit, offset = offset) }
    ) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name) }, page.count) }
}

/** Programs feed the schedule and the rocket list; both read them the same way. */
private suspend fun programs(
    api: ProgramsApi,
    fetcher: TerminusFilterFetcher,
    forceRefresh: Boolean
) = fetcher.options(
    "programs", forceRefresh,
    { limit, offset -> api.programsMiniList(limit = limit, offset = offset, ordering = "name") }
) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name) }, page.count) }

/** Launcher families feed the schedule and the rocket list; both read them the same way. */
private suspend fun launcherFamilies(
    api: LauncherConfigurationFamiliesApi,
    fetcher: TerminusFilterFetcher,
    forceRefresh: Boolean
) = fetcher.options(
    "launcher-families", forceRefresh,
    { limit, offset -> api.launcherConfigurationFamiliesList(limit = limit, offset = offset, ordering = "name") }
) { page -> FilterPage(page.results.map { FilterOption(it.id, it.name) }, page.count) }
