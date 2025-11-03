package me.calebjones.spacelaunchnow.data.billing

import kotlinx.datetime.Instant

/**
 * Data class representing an old purchase found via direct billing query
 *
 * This contains the information needed to import the purchase to RevenueCat
 */
data class OldPurchaseRecord(
    val productIds: List<String>,           // Product ID(s) - usually just one for one-time purchases
    val purchaseToken: String,              // Purchase token - needed for RevenueCat import
    val purchaseTime: Long,                 // Epoch milliseconds when purchased
    val signature: String,                  // Google Play signature for verification
    val originalJson: String,               // Original JSON receipt data
    val orderId: String,                    // Google Play order ID
    val purchaseState: Int                  // Purchase state (0=UNSPECIFIED, 1=PURCHASED, 2=PENDING)
) {
    /** Get the primary product ID (first in the list) */
    val productId: String get() = productIds.firstOrNull() ?: "unknown"

    /** Convert purchase time to kotlinx.datetime.Instant */
    val purchaseInstant: Instant get() = Instant.fromEpochMilliseconds(purchaseTime)

    /** Check if this is a legacy purchase (before 2024) */
    val isLegacyPurchase: Boolean
        get() {
            val purchaseYear = purchaseInstant.toString().substring(0, 4).toIntOrNull() ?: 0
            return purchaseYear < 2024
        }

    /** Check if purchase is in a valid state - platform-specific */
    val isValidPurchase: Boolean get() = purchaseState == 1 // 1 = PURCHASED
}
