package me.calebjones.spacelaunchnow.di

import io.ktor.client.engine.cio.CIO
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

import me.calebjones.spacelaunchnow.api.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.apis.LauncherConfigurationsApi
import me.calebjones.spacelaunchnow.api.apis.LaunchersApi
import me.calebjones.spacelaunchnow.api.apis.AgenciesApi
import me.calebjones.spacelaunchnow.api.apis.UpdatesApi
import me.calebjones.spacelaunchnow.api.infrastructure.ApiClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.LogLevel
import me.calebjones.spacelaunchnow.util.UserAgentUtil

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
            httpClientEngine = CIO.create(),
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
            httpClientEngine = CIO.create(),
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }
    
    single<LaunchersApi> {
        LaunchersApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = CIO.create(),
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }
    
    single<AgenciesApi> {
        AgenciesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = CIO.create(),
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }
    
    single<UpdatesApi> {
        UpdatesApi(
            baseUrl = get<String>(named("BaseUrl")),
            httpClientEngine = CIO.create(),
            httpClientConfig = httpClientConfig,
        ).apply {
            setApiKey(get<String>(named("API_KEY")), "Authorization")
            setApiKeyPrefix("Token", "Authorization")
        }
    }
}