package me.calebjones.spacelaunchnow.data.billing

/**
 * Thin wrapper over the RevenueCat KMP SDK's attribute APIs.
 * Mockable in tests; production implementation calls Purchases.sharedInstance.
 */
interface RevenueCatAttributes {

    /**
     * Push the given attribute map to RevenueCat. Null values clear an attribute.
     * Safe to call repeatedly; SDK coalesces writes.
     */
    fun set(attributes: Map<String, String?>)

    /**
     * Set the FCM (Android) or APNS (iOS) push token via the reserved attribute.
     * No-op on platforms without push.
     */
    fun setPushToken(token: String)
}
