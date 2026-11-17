package me.calebjones.spacelaunchnow.util

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.apps

/**
 * Backed by `FIRApp.allApps`, which is nil until `FirebaseApp.configure()` has run and never
 * raises. `Firebase.app` must not be used here: it force-unwraps `FIRApp.defaultApp()`.
 */
actual fun isFirebaseAppConfigured(): Boolean = try {
    Firebase.apps(null).isNotEmpty()
} catch (e: Exception) {
    false
}
