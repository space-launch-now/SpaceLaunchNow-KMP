package me.calebjones.spacelaunchnow.util

/**
 * Desktop implementation - always returns false.
 * 
 * Desktop environments typically have sufficient RAM (8GB+)
 * and don't experience the same warm start issues as mobile.
 */
actual fun isLowRamDevice(): Boolean = false
