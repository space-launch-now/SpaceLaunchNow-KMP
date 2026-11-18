package me.calebjones.spacelaunchnow.util

import com.google.firebase.FirebaseApp

/**
 * `FirebaseInitProvider` configures the default app before `Application.onCreate`, so this is
 * true in the app and its widgets. `getInstance()` throws `IllegalStateException` otherwise
 * (for example in JVM unit tests), which is a catchable Kotlin exception on this platform.
 */
actual fun isFirebaseAppConfigured(): Boolean = try {
    FirebaseApp.getInstance()
    true
} catch (e: IllegalStateException) {
    false
}
