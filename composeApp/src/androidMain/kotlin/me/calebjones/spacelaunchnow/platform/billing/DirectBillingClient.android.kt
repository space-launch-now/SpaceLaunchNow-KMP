package me.calebjones.spacelaunchnow.platform.billing

import android.app.Activity
import me.calebjones.spacelaunchnow.data.billing.DirectAndroidBillingClient
import me.calebjones.spacelaunchnow.data.billing.OldPurchaseRecord

/**
 * Android implementation of DirectBillingClient using Google Play Billing Library.
 */
actual class DirectBillingClient {
    private lateinit var client: DirectAndroidBillingClient
    
    internal fun initialize(activity: Activity) {
        client = DirectAndroidBillingClient(activity)
    }
    
    actual suspend fun initialize(): Result<Unit> {
        return client.initialize()
    }
    
    actual suspend fun launchPurchaseFlow(
        productId: String,
        productType: String,
        basePlanId: String?
    ): Result<String> {
        return client.launchPurchaseFlow(productId, productType, basePlanId)
    }
    
    actual suspend fun queryPurchaseHistory(
        productType: String
    ): Result<List<OldPurchaseRecord>> {
        return client.queryPurchaseHistory(productType)
    }
    
    actual fun disconnect() {
        client.disconnect()
    }
}

/**
 * Factory function to create DirectBillingClient with Android Activity context.
 */
actual fun createDirectBillingClient(context: Any?): DirectBillingClient {
    require(context is Activity) { "DirectBillingClient requires an Activity context on Android" }
    val client = DirectBillingClient()
    client.initialize(context)
    return client
}

/**
 * Direct billing is supported on Android.
 */
actual fun isDirectBillingSupported(): Boolean = true
