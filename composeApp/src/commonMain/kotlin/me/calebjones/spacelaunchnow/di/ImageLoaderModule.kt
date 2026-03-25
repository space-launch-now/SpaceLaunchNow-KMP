package me.calebjones.spacelaunchnow.di

import coil3.ImageLoader
import org.koin.dsl.module

/**
 * Expect function to create a platform-specific ImageLoader.
 * 
 * Returns null if the platform doesn't need a custom ImageLoader.
 * Android returns a custom ImageLoader with optimized cache settings for low-RAM devices.
 */
expect fun createPlatformImageLoader(): ImageLoader?

/**
 * Koin module that provides a custom Coil ImageLoader optimized for device memory class.
 * 
 * The ImageLoader is only provided on platforms that return a non-null instance
 * from createPlatformImageLoader(). On Android, this uses reduced cache sizes
 * for devices with ≤4GB RAM to improve warm start performance.
 * 
 * Configuration (Android low-RAM):
 * - Memory cache: 10% of heap (vs 25% default)
 * - Disk cache: 50MB (vs default)
 * - Crossfade disabled (reduces animation overhead)
 * 
 * Configuration (Android normal):
 * - Memory cache: 20% of heap
 * - Disk cache: 100MB
 * - Crossfade enabled
 */
val imageLoaderModule = module {
    single<ImageLoader?> {
        createPlatformImageLoader()
    }
}
