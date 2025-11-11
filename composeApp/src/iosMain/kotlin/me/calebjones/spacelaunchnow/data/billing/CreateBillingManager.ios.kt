package me.calebjones.spacelaunchnow.data.billing

/**
 * Factory function for iOS - creates IosBillingManager
 */
actual fun createBillingManager(): BillingManager {
    return IosBillingManager()
}
