package me.calebjones.spacelaunchnow.data.billing

import android.app.Activity
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.android.billingclient.api.BillingClient as GoogleBillingClient

/**
 * Direct Android Billing Library wrapper for debug/testing purposes
 *
 * This bypasses RevenueCat and uses the native Google Play Billing Library directly.
 * Use this ONLY for testing/debugging custom SKUs.
 */
class DirectAndroidBillingClient(private val activity: Activity) {

    private var billingClient: GoogleBillingClient? = null

    /**
     * Initialize the Google Play Billing client
     */
    suspend fun initialize(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        try {
            billingClient = GoogleBillingClient.newBuilder(activity)
                .setListener { billingResult, purchases ->
                    // Handle purchase updates
                    println("DirectBilling: Purchase update - ${billingResult.debugMessage}")
                }
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == GoogleBillingClient.BillingResponseCode.OK) {
                        println("DirectBilling: ✅ Connected to Google Play Billing")
                        continuation.resume(Result.success(Unit))
                    } else {
                        val error = "Failed to connect: ${billingResult.debugMessage}"
                        println("DirectBilling: ❌ $error")
                        continuation.resume(Result.failure(Exception(error)))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    println("DirectBilling: ⚠️ Disconnected from Google Play Billing")
                }
            })
        } catch (e: Exception) {
            println("DirectBilling: ❌ Exception during initialization: ${e.message}")
            continuation.resume(Result.failure(e))
        }
    }

    /**
     * Launch purchase flow for a specific product ID
     *
     * @param productId The product ID (SKU) to purchase
     * @param productType Either "inapp" for one-time purchases or "subs" for subscriptions
     * @param basePlanId Optional base plan ID for subscriptions (e.g., "base-plan", "yearly")
     */
    suspend fun launchPurchaseFlow(
        productId: String,
        productType: String = "inapp", // "inapp" or "subs"
        basePlanId: String? = null
    ): Result<String> {
        return try {
            val client = billingClient
            if (client == null || !client.isReady) {
                return Result.failure(Exception("Billing client not ready. Call initialize() first."))
            }

            println("DirectBilling: Querying product details for $productId (type: $productType)")

            // Query product details
            val productDetailsResult = queryProductDetails(productId, productType)

            productDetailsResult.fold(
                onSuccess = { productDetails ->
                    println("DirectBilling: Found product: ${productDetails.name}")
                    println("  Title: ${productDetails.title}")
                    println("  Description: ${productDetails.description}")

                    // Build purchase flow params
                    val productDetailsParamsList =
                        if (productType == "subs" && basePlanId != null) {
                            // Subscription with specific base plan
                            val offerToken = productDetails.subscriptionOfferDetails
                                ?.find { it.basePlanId == basePlanId }
                                ?.offerToken
                                ?: return Result.failure(Exception("Base plan '$basePlanId' not found"))

                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .setOfferToken(offerToken)
                                    .build()
                            )
                        } else {
                            // One-time purchase or subscription without specific plan
                            listOf(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                    .setProductDetails(productDetails)
                                    .build()
                            )
                        }

                    val billingFlowParams = BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build()

                    // Launch billing flow
                    println("DirectBilling: Launching purchase flow...")
                    val billingResult = client.launchBillingFlow(activity, billingFlowParams)

                    if (billingResult.responseCode == GoogleBillingClient.BillingResponseCode.OK) {
                        Result.success("Purchase flow launched")
                    } else {
                        Result.failure(Exception("Purchase flow failed: ${billingResult.debugMessage}"))
                    }
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            println("DirectBilling: ❌ Exception: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Query product details from Google Play
     */
    private suspend fun queryProductDetails(
        productId: String,
        productType: String
    ): Result<ProductDetails> = suspendCancellableCoroutine { continuation ->
        val client = billingClient ?: run {
            continuation.resume(Result.failure(Exception("Billing client not initialized")))
            return@suspendCancellableCoroutine
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(productType)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        client.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            when (billingResult.responseCode) {
                GoogleBillingClient.BillingResponseCode.OK -> {
                    val productDetails = productDetailsList.productDetailsList.firstOrNull()
                    if (productDetails != null) {
                        continuation.resume(Result.success(productDetails))
                    } else {
                        val error =
                            "Product '$productId' not found in Google Play Console (type: $productType)"
                        println("DirectBilling: ❌ $error")
                        continuation.resume(Result.failure(Exception(error)))
                    }
                }

                else -> {
                    val error = "Query failed: ${billingResult.debugMessage}"
                    println("DirectBilling: ❌ $error")
                    continuation.resume(Result.failure(Exception(error)))
                }
            }
        }
    }

    /**
     * Disconnect and cleanup
     */
    fun disconnect() {
        billingClient?.endConnection()
        billingClient = null
        println("DirectBilling: Disconnected")
    }
}
