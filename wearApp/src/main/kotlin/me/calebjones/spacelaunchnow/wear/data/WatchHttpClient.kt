package me.calebjones.spacelaunchnow.wear.data

import android.os.Build
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.wear.BuildConfig

private const val APP_NAME = "SpaceLaunchNow"

/** Builds `SpaceLaunchNow-v<VERSION_NAME> (Android <SDK_INT>)`, matching the phone app. */
internal fun buildWatchUserAgent(
    versionName: String = BuildConfig.APP_VERSION_NAME,
    sdkInt: Int = Build.VERSION.SDK_INT,
): String = "$APP_NAME-v$versionName (Android $sdkInt)"

/** Creates the watch HTTP client with JSON parsing and the shared User-Agent header. */
internal fun createWatchHttpClient(
    engine: HttpClientEngine,
    userAgent: String = buildWatchUserAgent(),
): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
    install(UserAgent) {
        agent = userAgent
    }
}
