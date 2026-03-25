package me.calebjones.spacelaunchnow.di

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.crossfade
import me.calebjones.spacelaunchnow.util.isLowRamDevice
import okio.Path.Companion.toOkioPath
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of custom ImageLoader with memory-aware configuration.
 * 
 * Provides reduced cache sizes for devices with ≤4GB RAM to improve warm start
 * performance. These devices showed 12.90% slow warm start rate vs 2.22% overall.
 */
private object ImageLoaderFactory : KoinComponent {
    private val context: Context by inject()
    
    fun create(): ImageLoader {
        val isLowRam = isLowRamDevice()
        
        // Low-RAM devices: 10% memory cache, 50MB disk cache, no crossfade
        // Normal devices: 20% memory cache, 100MB disk cache, crossfade enabled
        val memoryCachePercent = if (isLowRam) 0.10 else 0.20
        val diskCacheSize = if (isLowRam) 50L * 1024 * 1024 else 100L * 1024 * 1024
        val enableCrossfade = !isLowRam
        
        return ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, memoryCachePercent)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache").toOkioPath())
                    .maxSizeBytes(diskCacheSize)
                    .build()
            }
            .crossfade(enableCrossfade)
            .build()
    }
}

actual fun createPlatformImageLoader(): ImageLoader? {
    return try {
        ImageLoaderFactory.create()
    } catch (e: Exception) {
        // If Koin not initialized or other error, return null to use default
        null
    }
}
