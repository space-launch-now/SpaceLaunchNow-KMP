package me.calebjones.spacelaunchnow.di

import org.koin.dsl.KoinAppDeclaration
import org.koin.logger.slf4jLogger

actual fun nativeConfig(): KoinAppDeclaration = {
    slf4jLogger()
}