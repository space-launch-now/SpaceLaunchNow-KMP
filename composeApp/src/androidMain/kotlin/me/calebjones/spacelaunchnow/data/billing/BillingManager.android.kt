package me.calebjones.spacelaunchnow.data.billing

/**
 * Android implementation of createBillingManager
 * 
 * Note: This expect declaration requires Context, so the actual implementation
 * is handled through Koin DI which provides the Android context.
 * Use the createBillingManager(context) function in AndroidBillingManager.kt
 * for manual instantiation.
 */
actual fun createBillingManager(): BillingManager {
    error("Use Koin DI to get BillingManager on Android. Context is required.")
}
