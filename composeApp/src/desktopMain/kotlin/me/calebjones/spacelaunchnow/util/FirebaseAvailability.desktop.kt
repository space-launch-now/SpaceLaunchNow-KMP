package me.calebjones.spacelaunchnow.util

/**
 * Desktop never configures Firebase, so Remote Config readers use their defaults there. This
 * matches the previous behaviour, where the GitLive JVM SDK threw on first access and the
 * exception was caught.
 */
actual fun isFirebaseAppConfigured(): Boolean = false
