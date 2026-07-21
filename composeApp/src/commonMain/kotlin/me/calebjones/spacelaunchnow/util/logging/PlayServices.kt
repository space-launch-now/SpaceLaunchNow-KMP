package me.calebjones.spacelaunchnow.util.logging

/** Google Play Services availability as seen at startup. NOT_APPLICABLE on iOS/Desktop. */
enum class PlayServicesAvailability { AVAILABLE, MISSING, UPDATE_REQUIRED, NOT_APPLICABLE, UNKNOWN }

/** Synchronous Play Services check. Never throws; NOT_APPLICABLE on iOS/Desktop. */
expect fun checkPlayServicesAvailability(): PlayServicesAvailability
