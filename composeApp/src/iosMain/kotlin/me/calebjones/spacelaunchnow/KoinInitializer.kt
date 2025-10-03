package me.calebjones.spacelaunchnow

import me.calebjones.spacelaunchnow.di.koinConfig
import org.koin.core.context.startKoin

/**
 * Initialize Koin for iOS
 * This should be called before the UI is created to ensure all dependencies are available
 */
fun initKoin() {
    startKoin(koinConfig)
}
