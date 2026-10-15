package me.calebjones.spacelaunchnow.util.logging

import io.ktor.client.plugins.logging.LogLevel

/**
 * Ktor HTTP logging level for every shared client: URL + status line only. Never headers or
 * bodies (client-log-quality spec D3). Guarded by HttpLoggingLevelTest.
 */
internal val SHARED_HTTP_LOG_LEVEL: LogLevel = LogLevel.INFO
