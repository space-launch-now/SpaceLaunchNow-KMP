package me.calebjones.spacelaunchnow.util

import android.app.ActivityManager
import android.content.Context
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android implementation of low-RAM device detection.
 * 
 * Uses two methods:
 * 1. ActivityManager.isLowRamDevice() - system flag for devices with ≤1GB
 * 2. Manual totalMem check for 3-4GB devices that aren't flagged but still constrained
 * 
 * This targets the specific device segment showing high slow warm start rates.
 */
private object MemoryUtilHelper : KoinComponent {
    private val context: Context by inject()
    
    /**
     * Cached result to avoid repeated system calls.
     */
    private var cachedIsLowRam: Boolean? = null
    
    fun isLowRam(): Boolean {
        cachedIsLowRam?.let { return it }
        
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (activityManager == null) {
            cachedIsLowRam = false
            return false
        }
        
        // Check system's own low-RAM flag (usually ≤1GB devices)
        if (activityManager.isLowRamDevice) {
            cachedIsLowRam = true
            return true
        }
        
        // Also consider 3-4GB devices as "constrained" for our purposes
        // These show 12.90% slow warm start rate vs 2.22% overall
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)
        
        val totalMemGB = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        val isConstrained = totalMemGB <= 4.0
        
        cachedIsLowRam = isConstrained
        return isConstrained
    }
}

actual fun isLowRamDevice(): Boolean {
    return try {
        MemoryUtilHelper.isLowRam()
    } catch (e: Exception) {
        // If Koin not initialized yet or any other error, assume not low-RAM
        // This is a safe default that preserves existing behavior
        false
    }
}
