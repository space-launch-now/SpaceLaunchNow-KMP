package me.calebjones.spacelaunchnow.util

/**
 * Utility for detecting Wear OS watch connectivity.
 *
 * On Android: Queries the Wearable NodeClient for connected nodes.
 * On iOS / Desktop: Always returns false — Wear OS is Android-only.
 */

/**
 * Returns true if at least one paired Wear OS node (watch) is currently connected.
 *
 * This is a suspend function because it performs an async IPC call to Google Play Services
 * on Android. Call it from a coroutine or LaunchedEffect.
 */
expect suspend fun isWearOsConnected(): Boolean

/**
 * Returns true if this platform can ever have a Wear OS companion (i.e. is Android).
 * Use this for compile-time platform guards before hiding UI sections entirely.
 */
expect val isWearOsSupportedPlatform: Boolean


