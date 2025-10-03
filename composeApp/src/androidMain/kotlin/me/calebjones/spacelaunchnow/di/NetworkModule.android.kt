package me.calebjones.spacelaunchnow.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun createHttpClientEngine(): HttpClientEngine {
    return Android.create()
}
