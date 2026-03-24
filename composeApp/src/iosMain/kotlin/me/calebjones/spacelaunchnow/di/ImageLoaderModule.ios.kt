package me.calebjones.spacelaunchnow.di

import coil3.ImageLoader

/**
 * iOS implementation - returns null to use Coil's default ImageLoader.
 * 
 * iOS manages memory differently than Android and doesn't experience
 * the same warm start issues that affect Android low-RAM devices.
 */
actual fun createPlatformImageLoader(): ImageLoader? = null
