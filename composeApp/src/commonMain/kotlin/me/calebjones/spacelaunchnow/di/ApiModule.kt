package me.calebjones.spacelaunchnow.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ConfigApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.EventsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.LocationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.ProgramsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpacecraftConfigurationsApi
import me.calebjones.spacelaunchnow.api.launchlibrary.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.util.UserAgentUtil
import org.koin.core.qualifier.named
import org.koin.dsl.module

val apiModule = module {
    // Shared HTTP client configuration for all APIs
    val httpClientConfig: io.ktor.client.HttpClientConfig<*>.() -> Unit = {
        install(UserAgent) {
            agent = UserAgentUtil.getUserAgent()
        }

        // Install Logging plugin to show requests and headers
        install(Logging) {
            level = LogLevel.ALL // Show everything including request/response body
        }
    }

    single<LaunchesApi> {
        LaunchesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            // Use tokenAuth specifically (header-based auth, not cookieAuth query param)
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<LauncherConfigurationsApi> {
        LauncherConfigurationsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<LaunchersApi> {
        LaunchersApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<AgenciesApi> {
        AgenciesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<SpacecraftApi> {
        SpacecraftApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<SpacecraftConfigurationsApi> {
        SpacecraftConfigurationsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<UpdatesApi> {
        UpdatesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<EventsApi> {
        EventsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<ProgramsApi> {
        ProgramsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<LocationsApi> {
        LocationsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<ConfigApi> {
        ConfigApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi> {
        me.calebjones.spacelaunchnow.api.launchlibrary.apis.SpaceStationsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    single<me.calebjones.spacelaunchnow.api.launchlibrary.apis.ExpeditionsApi> {
        me.calebjones.spacelaunchnow.api.launchlibrary.apis.ExpeditionsApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }

    // SNAPI (Spaceflight News API) - separate base URL, no auth required
    single<ArticlesApi> {
        ArticlesApi(
            baseUrl = "https://api.spaceflightnewsapi.net",
            httpClientEngine = get<HttpClientEngine>(), // Use platform-specific engine from Koin
            httpClientConfig = httpClientConfig,
        )
        // No API key required for SNAPI
    }

    single<me.calebjones.spacelaunchnow.api.snapi.apis.ReportsApi> {
        me.calebjones.spacelaunchnow.api.snapi.apis.ReportsApi(
            baseUrl = "https://api.spaceflightnewsapi.net",
            httpClientEngine = get<HttpClientEngine>(),
            httpClientConfig = httpClientConfig,
        )
        // No API key required for SNAPI
    }
}