package me.calebjones.spacelaunchnow.util

/**
 * iOS implementation - always returns false.
 * 
 * iOS devices generally have well-optimized memory management
 * and don't show the same warm start issues observed on Android.
 * Could be refined later based on iOS analytics if needed.
 */
actual fun isLowRamDevice(): Boolean = false
