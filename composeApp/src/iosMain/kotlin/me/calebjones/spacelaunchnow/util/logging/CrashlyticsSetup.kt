package me.calebjones.spacelaunchnow.util.logging

import co.touchlab.crashkios.crashlytics.enableCrashlytics
import co.touchlab.crashkios.crashlytics.setCrashlyticsUnhandledExceptionHook

/**
 * Initialize CrashKiOS for meaningful Kotlin stack traces in Crashlytics on iOS.
 *
 * By default, unhandled Kotlin exceptions on iOS produce unreadable stack traces.
 * This registers an unhandled exception hook that captures Kotlin stack frames
 * and reports them via FIRExceptionModel with readable line numbers.
 *
 * Must be called early in the iOS initialization process, before any shared Kotlin
 * code that might throw.
 *
 * @see <a href="https://crashkios.touchlab.co/docs/crashlytics/">CrashKiOS Docs</a>
 * @see <a href="https://github.com/firebase/firebase-ios-sdk/issues/15512">Firebase iOS SDK #15512</a>
 */
fun setupCrashlyticsExceptionHook() {
    enableCrashlytics()
    setCrashlyticsUnhandledExceptionHook()
}
