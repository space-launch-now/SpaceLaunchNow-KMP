package me.calebjones.spacelaunchnow.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.api.trantor.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.AstronautsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.EventsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.FamiliesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.HealthApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.LookupsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.PadsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.api.trantor.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.SpaceStationsApi
import me.calebjones.spacelaunchnow.api.trantor.apis.UpdatesApi
import me.calebjones.spacelaunchnow.util.UserAgentUtil
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Routes Ktor's HTTP logging through SpaceLogger so requests/responses appear under
// a stable tag (KtorHttp) instead of going to System.out / the platform default sink.
private val trantorHttpLogger = object : Logger {
    private val log = SpaceLogger.getLogger("KtorHttp")
    override fun log(message: String) {
        log.d { message }
    }
}

// Trantor (SpaceLaunchNow-API) is unauthenticated in v1 — reads take no Authorization
// header, so unlike apiModule's LL/SNAPI singletons, none of these call setApiKey(...).
//
// Trantor clients resolve against their own named("TrantorBaseUrl") qualifier (provided in
// NetworkModule.kt) — kept separate from the LL/SNAPI named("BaseUrl") so the debug-menu's
// Prod/Dev/Local buttons (which target LL hosts with no /api/v1 prefix) never redirect the
// Trantor client to a host it can't serve. See amendment 2026-09-02 in the Phase 5 plan.
val trantorApiModule = module {
    val httpClientConfig: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
        install(UserAgent) {
            agent = UserAgentUtil.getUserAgent()
        }

        install(Logging) {
            logger = trantorHttpLogger
            level = LogLevel.ALL
        }
    }

    single<LaunchesApi> {
        LaunchesApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<AgenciesApi> {
        AgenciesApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<AstronautsApi> {
        AstronautsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<EventsApi> {
        EventsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<FamiliesApi> {
        FamiliesApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<HealthApi> {
        HealthApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LauncherConfigurationsApi> {
        LauncherConfigurationsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LaunchersApi> {
        LaunchersApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LocationsApi> {
        LocationsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<LookupsApi> {
        LookupsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<PadsApi> {
        PadsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<ProgramsApi> {
        ProgramsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacecraftApi> {
        SpacecraftApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpacecraftConfigurationsApi> {
        SpacecraftConfigurationsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<SpaceStationsApi> {
        SpaceStationsApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }

    single<UpdatesApi> {
        UpdatesApi(
            baseUrl = get<String>(named("TrantorBaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
    }
}
