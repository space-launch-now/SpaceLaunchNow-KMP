package me.calebjones.spacelaunchnow.util

/**
 * Whether this process has a configured default Firebase app.
 *
 * The main apps configure Firebase at startup, but the shared Koin graph is also started by
 * processes that never do, such as the iOS widget extension. Firebase-backed services must
 * check this before asking the SDK for an instance: on iOS the SDK raises an Objective-C
 * exception (`FIRAppNotConfigured`) when no app is configured, which no Kotlin `catch` can
 * stop, so the process is killed.
 *
 * Never raises or throws.
 */
expect fun isFirebaseAppConfigured(): Boolean
