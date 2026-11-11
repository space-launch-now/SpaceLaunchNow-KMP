package me.calebjones.spacelaunchnow.di.terminus

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import me.calebjones.spacelaunchnow.api.terminus.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.AstronautsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.CelestialBodiesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.terminus.apis.DockingEventsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.EventsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ExpeditionsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LandingsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationFamiliesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.MissionPatchesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.PadsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.PayloadFlightsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.PayloadsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.RocketsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SearchApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftConfigurationFamiliesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacecraftFlightsApi
import me.calebjones.spacelaunchnow.api.terminus.apis.SpacewalksApi
import me.calebjones.spacelaunchnow.api.terminus.apis.StagesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.terminus.apis.ViewingApi
import me.calebjones.spacelaunchnow.util.UserAgentUtil
import me.calebjones.spacelaunchnow.util.logging.SHARED_HTTP_LOG_LEVEL
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val terminusHttpLogger = object : Logger {
    private val log = SpaceLogger.getLogger("KtorHttp")
    override fun log(message: String) {
        log.d { message }
    }
}

// Terminus (SLN's own LL-compatible API) is anonymous: no Authorization header, so none of
// these call setApiKey(...). Clients resolve against named("TerminusBaseUrl") (see
// NetworkModule.kt), separate from the LL/SNAPI named("BaseUrl") because Terminus paths
// carry the /api/v1 prefix. Loaded by startKoin in AppModule.kt; the Terminus repository is the only consumer.
// Clients are lazy singletons, so none is built unless the Terminus backend is active.
val terminusApiModule = module {
    val httpClientConfig: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
        install(UserAgent) {
            agent = UserAgentUtil.getUserAgent()
        }

        install(Logging) {
            logger = terminusHttpLogger
            level = SHARED_HTTP_LOG_LEVEL
        }
    }

    single<AgenciesApi> {
        AgenciesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<AstronautsApi> {
        AstronautsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<CelestialBodiesApi> {
        CelestialBodiesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<ConfigApi> {
        ConfigApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<DockingEventsApi> {
        DockingEventsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<EventsApi> {
        EventsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<ExpeditionsApi> {
        ExpeditionsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LandingsApi> {
        LandingsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LauncherConfigurationFamiliesApi> {
        LauncherConfigurationFamiliesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LauncherConfigurationsApi> {
        LauncherConfigurationsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LaunchersApi> {
        LaunchersApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LaunchesApi> {
        LaunchesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LocationsApi> {
        LocationsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<MissionPatchesApi> {
        MissionPatchesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<PadsApi> {
        PadsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<PayloadFlightsApi> {
        PayloadFlightsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<PayloadsApi> {
        PayloadsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<ProgramsApi> {
        ProgramsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<RocketsApi> {
        RocketsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SearchApi> {
        SearchApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpaceStationsApi> {
        SpaceStationsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacecraftApi> {
        SpacecraftApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacecraftConfigurationFamiliesApi> {
        SpacecraftConfigurationFamiliesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacecraftConfigurationsApi> {
        SpacecraftConfigurationsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacecraftFlightsApi> {
        SpacecraftFlightsApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacewalksApi> {
        SpacewalksApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<StagesApi> {
        StagesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<UpdatesApi> {
        UpdatesApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<ViewingApi> {
        ViewingApi(
            baseUrl = get<String>(named("TerminusBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }
}
