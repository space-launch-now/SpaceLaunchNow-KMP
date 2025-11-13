package me.calebjones.spacelaunchnow.data.billing

/**
 * Factory function for Desktop - creates DesktopBillingManager
 */
actual fun createBillingManager(): BillingManager {
    return DesktopBillingManager()
}
