package me.calebjones.spacelaunchnow.di

import coil3.ImageLoader

/**
 * Desktop implementation - returns null to use Coil's default ImageLoader.
 * 
 * Desktop environments typically have sufficient RAM and don't need
 * the memory optimizations targeted for Android low-RAM devices.
 */
actual fun createPlatformImageLoader(): ImageLoader? = null
