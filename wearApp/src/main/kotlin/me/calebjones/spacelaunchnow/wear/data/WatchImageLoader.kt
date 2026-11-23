package me.calebjones.spacelaunchnow.wear.data

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.UserAgent

/**
 * Creates the HTTP client behind Coil's image fetches. It sends the shared
 * `SpaceLaunchNow-v<VERSION_NAME> (Android <SDK_INT>)` User-Agent from [buildWatchUserAgent];
 * Coil's default client sends none. No ContentNegotiation: it would add `Accept: application/json`.
 */
internal fun createWatchImageHttpClient(
    engine: HttpClientEngine,
    userAgent: String = buildWatchUserAgent(),
): HttpClient = HttpClient(engine) {
    install(UserAgent) {
        agent = userAgent
    }
}

/** Builds the watch Coil [ImageLoader] on top of [httpClient]. */
internal fun createWatchImageLoader(
    context: PlatformContext,
    httpClient: HttpClient,
): ImageLoader = ImageLoader.Builder(context)
    .components { add(KtorNetworkFetcherFactory(httpClient)) }
    .build()
