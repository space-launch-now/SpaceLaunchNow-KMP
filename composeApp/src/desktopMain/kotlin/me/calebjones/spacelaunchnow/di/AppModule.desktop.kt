package me.calebjones.spacelaunchnow.di

import org.koin.dsl.koinConfiguration
import org.koin.logger.slf4jLogger

actual fun nativeConfig() = koinConfiguration {
    slf4jLogger()
}