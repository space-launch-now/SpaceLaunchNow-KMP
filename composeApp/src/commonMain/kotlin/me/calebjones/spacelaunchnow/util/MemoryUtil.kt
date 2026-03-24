package me.calebjones.spacelaunchnow.util

/**
 * Platform-specific memory detection utility.
 * 
 * Used to optimize performance for memory-constrained devices,
 * particularly for image loading cache configuration.
 * 
 * On Android: Returns true for devices with ≤4GB RAM or flagged by system
 * On Desktop/iOS: Always returns false (assumed sufficient memory)
 */
expect fun isLowRamDevice(): Boolean

/**
 * Memory cache percentage for image loading based on device capabilities.
 * 
 * Low-RAM devices get reduced cache to prevent memory pressure during warm starts.
 * Normal devices use standard cache sizing for optimal UX.
 */
fun getImageMemoryCachePercent(): Double {
    return if (isLowRamDevice()) 0.10 else 0.20
}

/**
 * Disk cache size in bytes based on device capabilities.
 * 
 * Low-RAM devices: 50MB (reduced to limit I/O during cache eviction)
 * Normal devices: 100MB (standard sizing)
 */
fun getImageDiskCacheSizeBytes(): Long {
    return if (isLowRamDevice()) 50L * 1024 * 1024 else 100L * 1024 * 1024
}
