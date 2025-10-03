package me.calebjones.spacelaunchnow.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO

actual fun createHttpClientEngine(): HttpClientEngine {
    return CIO.create()
}
