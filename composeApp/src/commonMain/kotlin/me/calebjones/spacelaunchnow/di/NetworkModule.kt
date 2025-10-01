package me.calebjones.spacelaunchnow.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking

import me.calebjones.spacelaunchnow.util.EnvironmentManager
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    // Provide the API_KEY
    single(named("API_KEY")) {
        EnvironmentManager.getEnv("API_KEY", "")
    }
    
    // Provide the HttpClientEngine (CIO for JVM)
    single<HttpClientEngine> {
        CIO.create()
    }

    // Provide the HttpClient with necessary plugins
    single {
        HttpClient(get<HttpClientEngine>()) {
            // Install Logging plugin for debugging purposes
            install(Logging) {
                level = LogLevel.BODY
            }            // Install DefaultRequest plugin to add headers to every request
            install(DefaultRequest) {
                // We no longer need to set Authorization header here as ApiClient.setApiKey will handle this
                // Each API client will set its own Authorization header
                header(HttpHeaders.ContentType, "application/json")
            }

            // // Install ContentNegotiation plugin with Kotlinx JSON serializer
            // install(ContentNegotiation) {
            //     json(
            //         Json {
            //             ignoreUnknownKeys = true
            //             isLenient = true
            //             classDiscriminator = "response_mode"
            //             serializersModule = launchSerializersModule
            //         }
            //     )
            // }
        }
    }

    // Provide the base URL as a named dependency
    single<String>(named("BaseUrl")) {
        if (BuildConfig.DEBUG) {
            // In debug mode, try to get custom URL from debug preferences
            try {
                val debugPreferences = getOrNull<DebugPreferences>()
                if (debugPreferences != null) {
                    runBlocking { debugPreferences.getEffectiveApiBaseUrl() }
                } else {
                    "https://spacelaunchnow.app"
                }
            } catch (e: Exception) {
                // Fallback to default if debug preferences not available
                "https://spacelaunchnow.app"
            }
        } else {
            "https://spacelaunchnow.app"
        }
    }
}