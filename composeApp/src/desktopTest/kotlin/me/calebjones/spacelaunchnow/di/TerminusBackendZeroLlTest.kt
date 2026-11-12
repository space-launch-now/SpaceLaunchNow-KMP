package me.calebjones.spacelaunchnow.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.DataBackend
import me.calebjones.spacelaunchnow.data.model.TerminusHost
import me.calebjones.spacelaunchnow.data.repository.AgencyRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautFilterRepository
import me.calebjones.spacelaunchnow.data.repository.AstronautRepository
import me.calebjones.spacelaunchnow.data.repository.EventsRepository
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherConfigRepository
import me.calebjones.spacelaunchnow.data.repository.LauncherRepository
import me.calebjones.spacelaunchnow.data.repository.ProgramRepository
import me.calebjones.spacelaunchnow.data.repository.RocketFilterRepository
import me.calebjones.spacelaunchnow.data.repository.RocketRepository
import me.calebjones.spacelaunchnow.data.repository.ScheduleFilterRepository
import me.calebjones.spacelaunchnow.data.repository.SpaceStationRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftConfigRepository
import me.calebjones.spacelaunchnow.data.repository.SpacecraftRepository
import me.calebjones.spacelaunchnow.data.repository.UpdatesRepository
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.di.terminus.terminusApiModule
import me.calebjones.spacelaunchnow.ui.viewmodel.InMemoryPreferencesDataStore
import org.koin.core.Koin
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

/**
 * Close-out check for the Terminus port (Tasks#1013, master list in Tasks#1006). It builds the
 * app's real Koin graph on [DataBackend.TERMINUS]: the real network, LL API, Terminus API,
 * repository and app modules, with only the HTTP engine, the database and the preference store
 * replaced by test doubles. It then calls every repository method on the master list and asserts
 * that no request goes to the Launch Library path (`/api/ll/2.4.0/`) and that every request goes
 * to the Terminus host.
 *
 * The mock answers with an empty page. The test checks which host a call reaches, not what it
 * returns, so a detail call may fail to decode and return a failed `Result`.
 *
 * A method added to one of the repository interfaces fails the last assertion until it is added
 * here, so a new LL read cannot slip past this check.
 */
class TerminusBackendZeroLlTest {

    private val llPathPrefix = "/api/ll/2.4.0/"
    private val terminusHost = Url(TerminusHost.DEFAULT.baseUrl).host

    private class Probe {
        val requests = mutableListOf<Pair<String, Url>>()
        var current: String = ""
        val engine = MockEngine { request ->
            requests += current to request.url
            respond(
                content = """{"count":0,"next":null,"previous":null,"results":[]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
    }

    private fun graph(probe: Probe): Koin = koinApplication {
        modules(
            networkModule, apiModule, terminusApiModule,
            launchRepositoryModule(), eventsUpdatesRepositoryModule(), filterRepositoryModule(),
            spaceRepositoryModule(), vehicleRepositoryModule(), appModule,
            // Test doubles for what a desktop test cannot or should not build.
            module {
                single<HttpClientEngine> { probe.engine }
                single<DataBackend> { DataBackend.TERMINUS }
                single<DataStore<Preferences>>(named("AppSettingsDataStore")) { InMemoryPreferencesDataStore() }
                single {
                    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
                    SpaceLaunchDatabase.Schema.create(driver)
                    SpaceLaunchDatabase(driver)
                }
            }
        )
    }.koin

    /** Every master-list method, keyed `Interface.method`. */
    private fun masterList(koin: Koin): Map<String, suspend () -> Any?> {
        val now = Clock.System.now()
        val launches = koin.get<LaunchRepository>()
        val events = koin.get<EventsRepository>()
        val updates = koin.get<UpdatesRepository>()
        val schedule = koin.get<ScheduleFilterRepository>()
        val astronautFilter = koin.get<AstronautFilterRepository>()
        val rocketFilter = koin.get<RocketFilterRepository>()
        val agencies = koin.get<AgencyRepository>()
        val launcherConfigs = koin.get<LauncherConfigRepository>()
        val launchers = koin.get<LauncherRepository>()
        val rockets = koin.get<RocketRepository>()
        val stations = koin.get<SpaceStationRepository>()
        val astronauts = koin.get<AstronautRepository>()
        val programs = koin.get<ProgramRepository>()
        val spacecraft = koin.get<SpacecraftRepository>()
        val spacecraftConfigs = koin.get<SpacecraftConfigRepository>()
        return linkedMapOf(
            "LaunchRepository.getLaunchesByDayAndMonth" to { launches.getLaunchesByDayAndMonth(12, 11, forceRefresh = true) },
            "LaunchRepository.getStaleDetailedLaunch" to { launches.getStaleDetailedLaunch("abc") },
            "LaunchRepository.getStatsCount" to { launches.getStatsCount("k", now - 30.days, now, forceRefresh = true) },
            "LaunchRepository.getUpcomingLaunchesDomain" to { launches.getUpcomingLaunchesDomain(5, forceRefresh = true) },
            "LaunchRepository.getPreviousLaunchesDomain" to { launches.getPreviousLaunchesDomain(5, forceRefresh = true) },
            "LaunchRepository.getFeaturedLaunchDomain" to { launches.getFeaturedLaunchDomain(forceRefresh = true) },
            "LaunchRepository.getInFlightLaunchesDomain" to { launches.getInFlightLaunchesDomain(forceRefresh = true) },
            "LaunchRepository.getUpcomingLaunchesNormalDomain" to { launches.getUpcomingLaunchesNormalDomain(5, forceRefresh = true) },
            "LaunchRepository.getPreviousLaunchesNormalDomain" to { launches.getPreviousLaunchesNormalDomain(5, forceRefresh = true) },
            "LaunchRepository.getLaunchDetailDomain" to { launches.getLaunchDetailDomain("abc", forceRefresh = true) },
            "LaunchRepository.getStarshipLaunchesDomain" to { launches.getStarshipLaunchesDomain(5, forceRefresh = true) },
            "LaunchRepository.getStarshipHistoryDomain" to { launches.getStarshipHistoryDomain(5, forceRefresh = true) },
            "LaunchRepository.getLaunchByIdDomain" to { launches.getLaunchByIdDomain("abc") },
            "LaunchRepository.getFilteredLaunchesDomain" to { launches.getFilteredLaunchesDomain(5, forceRefresh = true) },

            "EventsRepository.getEventsByProgram" to { events.getEventsByProgram(1, forceRefresh = true) },
            "EventsRepository.getEvents" to { events.getEvents() },
            "EventsRepository.getUpcomingEventsDomain" to { events.getUpcomingEventsDomain(forceRefresh = true) },
            "EventsRepository.getEventDetailDomain" to { events.getEventDetailDomain(1) },
            "EventsRepository.getEventsByTypeDomain" to { events.getEventsByTypeDomain(listOf(1)) },
            "EventsRepository.getEventsByLaunchIdDomain" to { events.getEventsByLaunchIdDomain("abc") },
            "EventsRepository.getEventsPaginatedDomain" to { events.getEventsPaginatedDomain(forceRefresh = true) },
            "EventsRepository.getEventTypesDomain" to { events.getEventTypesDomain() },
            "UpdatesRepository.getLatestUpdates" to { updates.getLatestUpdates(forceRefresh = true) },
            "UpdatesRepository.getUpdatesByProgram" to { updates.getUpdatesByProgram(1, forceRefresh = true) },

            "ScheduleFilterRepository.getAgencies" to { schedule.getAgencies(forceRefresh = true) },
            "ScheduleFilterRepository.getPrograms" to { schedule.getPrograms(forceRefresh = true) },
            "ScheduleFilterRepository.getRockets" to { schedule.getRockets(forceRefresh = true) },
            "ScheduleFilterRepository.getLocations" to { schedule.getLocations(forceRefresh = true) },
            "ScheduleFilterRepository.getStatuses" to { schedule.getStatuses(forceRefresh = true) },
            "ScheduleFilterRepository.getOrbits" to { schedule.getOrbits(forceRefresh = true) },
            "ScheduleFilterRepository.getMissionTypes" to { schedule.getMissionTypes(forceRefresh = true) },
            "ScheduleFilterRepository.getLauncherConfigFamilies" to { schedule.getLauncherConfigFamilies(forceRefresh = true) },
            "AstronautFilterRepository.getStatuses" to { astronautFilter.getStatuses(forceRefresh = true) },
            "RocketFilterRepository.getPrograms" to { rocketFilter.getPrograms(forceRefresh = true) },
            "RocketFilterRepository.getFamilies" to { rocketFilter.getFamilies(forceRefresh = true) },

            "AgencyRepository.getAgenciesDomain" to { agencies.getAgenciesDomain(5) },
            "AgencyRepository.searchAgenciesDomain" to { agencies.searchAgenciesDomain("nasa") },
            "AgencyRepository.getAgencyDetailDomain" to { agencies.getAgencyDetailDomain(44) },
            "LauncherConfigRepository.getConfigurationsByProgramDomain" to { launcherConfigs.getConfigurationsByProgramDomain(1) },
            "LauncherConfigRepository.getConfigurationsDomain" to { launcherConfigs.getConfigurationsDomain() },
            "LauncherConfigRepository.getConfigurationDetailsDomain" to { launcherConfigs.getConfigurationDetailsDomain(164) },
            "LauncherRepository.getLaunchersDomain" to { launchers.getLaunchersDomain() },
            "LauncherRepository.getLaunchersByConfigDomain" to { launchers.getLaunchersByConfigDomain(164) },
            "LauncherRepository.getLauncherDetailsDomain" to { launchers.getLauncherDetailsDomain(77) },
            "RocketRepository.getRocketsDomain" to { rockets.getRocketsDomain() },
            "RocketRepository.getRocketDetailsDomain" to { rockets.getRocketDetailsDomain(164) },

            "SpaceStationRepository.getSpaceStationDetails" to { stations.getSpaceStationDetails(4, forceRefresh = true) },
            "SpaceStationRepository.getExpeditionDetails" to { stations.getExpeditionDetails(listOf(1), 4, forceRefresh = true) },
            "AstronautRepository.getAstronauts" to { astronauts.getAstronauts() },
            "AstronautRepository.getAstronautDetail" to { astronauts.getAstronautDetail(1) },
            "ProgramRepository.getProgramDomain" to { programs.getProgramDomain(1, forceRefresh = true) },
            "SpacecraftRepository.getSpacecraftByConfigDomain" to { spacecraft.getSpacecraftByConfigDomain(1, forceRefresh = true) },
            "SpacecraftRepository.getSpacecraftDetailsDomain" to { spacecraft.getSpacecraftDetailsDomain(1) },
            "SpacecraftRepository.getSpacecraftDomain" to { spacecraft.getSpacecraftDomain() },
            "SpacecraftConfigRepository.getConfigurationsByAgencyDomain" to { spacecraftConfigs.getConfigurationsByAgencyDomain(1) },
            "SpacecraftConfigRepository.getConfigurationsDomain" to { spacecraftConfigs.getConfigurationsDomain() },
            "SpacecraftConfigRepository.getConfigurationDetailsDomain" to { spacecraftConfigs.getConfigurationDetailsDomain(1) },
        )
    }

    // ISS TLE reads wheretheiss.at, not Launch Library, and is not on the master list.
    private val notLl = setOf("SpaceStationRepository.getIssTle", "SpaceStationRepository.prewarmIssCache")

    // Reads the local cache only, so it makes no request by design.
    private val localOnly = setOf("LaunchRepository.getStaleDetailedLaunch")

    private val interfaces: List<KClass<*>> = listOf(
        LaunchRepository::class, EventsRepository::class, UpdatesRepository::class,
        ScheduleFilterRepository::class, AstronautFilterRepository::class, RocketFilterRepository::class,
        AgencyRepository::class, LauncherConfigRepository::class, LauncherRepository::class,
        RocketRepository::class, SpaceStationRepository::class, AstronautRepository::class,
        ProgramRepository::class, SpacecraftRepository::class, SpacecraftConfigRepository::class
    )

    @Test
    fun terminusBackendSendsNoRequestToLl() = runTest {
        val probe = Probe()
        val koin = graph(probe)
        try {
            val methods = masterList(koin)
            for ((name, call) in methods) {
                probe.current = name
                runCatching { call() } // a failed Result or a decode error is fine: the host is the point
            }

            val llCalls = probe.requests.filter { (_, url) -> url.encodedPath.startsWith(llPathPrefix) }
            assertTrue(llCalls.isEmpty(), "These methods still call Launch Library: $llCalls")
            val offHost = probe.requests.filter { (_, url) -> url.host != terminusHost }
            assertTrue(offHost.isEmpty(), "Requests left the Terminus host: $offHost")

            val silent = methods.keys - localOnly - probe.requests.map { it.first }.toSet()
            assertTrue(silent.isEmpty(), "These methods made no request, so the check proves nothing for them: $silent")

            val declared = interfaces.flatMap { type ->
                type.java.declaredMethods
                    .filter { !it.isSynthetic && !java.lang.reflect.Modifier.isStatic(it.modifiers) }
                    // Kotlin mangles the name of a function that returns Result (`name-hash`).
                    .map { "${type.simpleName}.${it.name.substringBefore('-')}" }
            }.toSet() - notLl
            assertEquals(declared, methods.keys, "The repository interfaces and the zero-LL method list differ")
        } finally {
            koin.close()
        }
    }
}
