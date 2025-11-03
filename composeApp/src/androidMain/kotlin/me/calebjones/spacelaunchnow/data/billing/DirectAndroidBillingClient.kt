package me.calebjones.spacelaunchnow.data.billing

import android.app.Activity
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
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
            DatadogLogger.info("Direct billing initialization started", emptyMap())

            billingClient = GoogleBillingClient.newBuilder(activity)
                .setListener { billingResult, purchases ->
                    // Handle purchase updates
                    println("DirectBilling: Purchase update - ${billingResult.debugMessage}")
                    DatadogLogger.info(
                        "Direct billing purchase update received", mapOf(
                            "response_code" to billingResult.responseCode,
                            "debug_message" to (billingResult.debugMessage ?: "none"),
                            "purchase_count" to (purchases?.size ?: 0)
                        )
                    )
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
                        DatadogLogger.info(
                            "Direct billing connected successfully", mapOf(
                                "response_code" to billingResult.responseCode
                            )
                        )
                        continuation.resume(Result.success(Unit))
                    } else {
                        val error = "Failed to connect: ${billingResult.debugMessage}"
                        println("DirectBilling: ❌ $error")
                        DatadogLogger.error(
                            "Direct billing connection failed", null, mapOf(
                                "response_code" to billingResult.responseCode,
                                "debug_message" to (billingResult.debugMessage ?: "none")
                            )
                        )
                        continuation.resume(Result.failure(Exception(error)))
                    }
                }

                override fun onBillingServiceDisconnected() {
                    println("DirectBilling: ⚠️ Disconnected from Google Play Billing")
                    DatadogLogger.warn(
                        "Direct billing service disconnected", mapOf(
                            "status" to "disconnected"
                        )
                    )
                }
            })
        } catch (e: Exception) {
            println("DirectBilling: ❌ Exception during initialization: ${e.message}")
            DatadogLogger.error(
                "Direct billing initialization exception", e, mapOf(
                    "error_message" to (e.message ?: "unknown")
                )
            )
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
            DatadogLogger.info(
                "Direct billing purchase flow started", mapOf(
                    "product_id" to productId,
                    "product_type" to productType,
                    "base_plan_id" to (basePlanId ?: "none")
                )
            )

            val client = billingClient
            if (client == null || !client.isReady) {
                DatadogLogger.error(
                    "Direct billing purchase flow failed - client not ready", null, mapOf(
                        "product_id" to productId,
                        "client_null" to (client == null),
                        "client_ready" to (client?.isReady ?: false)
                    )
                )
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

                    DatadogLogger.info(
                        "Direct billing product details retrieved", mapOf(
                            "product_id" to productId,
                            "product_name" to productDetails.name,
                            "product_title" to productDetails.title,
                            "product_type" to productType
                        )
                    )

                    // Build purchase flow params
                    val productDetailsParamsList =
                        if (productType == "subs" && basePlanId != null) {
                            // Subscription with specific base plan
                            val offerToken = productDetails.subscriptionOfferDetails
                                ?.find { it.basePlanId == basePlanId }
                                ?.offerToken

                            if (offerToken == null) {
                                DatadogLogger.error(
                                    "Direct billing base plan not found",
                                    null,
                                    mapOf(
                                        "product_id" to productId,
                                        "base_plan_id" to basePlanId,
                                        "available_plans" to (productDetails.subscriptionOfferDetails?.joinToString(
                                            ","
                                        ) { it.basePlanId } ?: "none")
                                    ))
                                return Result.failure(Exception("Base plan '$basePlanId' not found"))
                            }

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
                        DatadogLogger.info(
                            "Direct billing purchase flow launched successfully", mapOf(
                                "product_id" to productId,
                                "product_type" to productType,
                                "response_code" to billingResult.responseCode
                            )
                        )
                        Result.success("Purchase flow launched")
                    } else {
                        DatadogLogger.error(
                            "Direct billing purchase flow launch failed", null, mapOf(
                                "product_id" to productId,
                                "product_type" to productType,
                                "response_code" to billingResult.responseCode,
                                "debug_message" to (billingResult.debugMessage ?: "none")
                            )
                        )
                        Result.failure(Exception("Purchase flow failed: ${billingResult.debugMessage}"))
                    }
                },
                onFailure = { error ->
                    DatadogLogger.error(
                        "Direct billing purchase flow failed - product query error", error, mapOf(
                            "product_id" to productId,
                            "product_type" to productType,
                            "error_message" to (error.message ?: "unknown")
                        )
                    )
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            println("DirectBilling: ❌ Exception: ${e.message}")
            DatadogLogger.error(
                "Direct billing purchase flow exception", e, mapOf(
                    "product_id" to productId,
                    "product_type" to productType,
                    "error_message" to (e.message ?: "unknown")
                )
            )
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
        DatadogLogger.info(
            "Direct billing product details query started", mapOf(
                "product_id" to productId,
                "product_type" to productType
            )
        )

        val client = billingClient ?: run {
            DatadogLogger.error(
                "Direct billing product query failed - client not initialized", null, mapOf(
                    "product_id" to productId,
                    "product_type" to productType
                )
            )
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
                        DatadogLogger.info(
                            "Direct billing product details found", mapOf(
                                "product_id" to productId,
                                "product_type" to productType,
                                "product_name" to productDetails.name,
                                "product_title" to productDetails.title
                            )
                        )
                        continuation.resume(Result.success(productDetails))
                    } else {
                        val error =
                            "Product '$productId' not found in Google Play Console (type: $productType)"
                        println("DirectBilling: ❌ $error")
                        DatadogLogger.error(
                            "Direct billing product not found", null, mapOf(
                                "product_id" to productId,
                                "product_type" to productType,
                                "products_in_response" to productDetailsList.productDetailsList.size
                            )
                        )
                        continuation.resume(Result.failure(Exception(error)))
                    }
                }

                else -> {
                    val error = "Query failed: ${billingResult.debugMessage}"
                    println("DirectBilling: ❌ $error")
                    DatadogLogger.error(
                        "Direct billing product query failed", null, mapOf(
                            "product_id" to productId,
                            "product_type" to productType,
                            "response_code" to billingResult.responseCode,
                            "debug_message" to (billingResult.debugMessage ?: "none")
                        )
                    )
                    continuation.resume(Result.failure(Exception(error)))
                }
            }
        }
    }

    /**
     * Query ALL purchases from Google Play Billing
     *
     * This retrieves all purchases (active + historical) using queryPurchasesAsync.
     * In Billing Client 6.0+, this replaced queryPurchaseHistoryAsync and returns
     * a more complete list of purchases.
     *
     * THIS IS THE KEY METHOD to find legacy purchases from 2018-2022!
     *
     * @param productType Either "inapp" for one-time purchases or "subs" for subscriptions
     * @return List of purchase records with product IDs and purchase tokens
     */
    suspend fun queryPurchaseHistory(
        productType: String = GoogleBillingClient.ProductType.INAPP
    ): Result<List<OldPurchaseRecord>> = suspendCancellableCoroutine { continuation ->
        val client = billingClient ?: run {
            val error = "Billing client not initialized"
            DatadogLogger.error(
                "Direct billing purchase query failed - not initialized", null, mapOf(
                    "product_type" to productType
                )
            )
            continuation.resume(Result.failure(Exception(error)))
            return@suspendCancellableCoroutine
        }

        if (!client.isReady) {
            val error = "Billing client not ready"
            DatadogLogger.error(
                "Direct billing purchase query failed - not ready", null, mapOf(
                    "product_type" to productType
                )
            )
            continuation.resume(Result.failure(Exception(error)))
            return@suspendCancellableCoroutine
        }

        println("DirectBilling: 🔍 Querying ALL purchases for type: $productType")
        println("DirectBilling: This should find purchases that BC8/RevenueCat can't see!")

        DatadogLogger.info(
            "Direct billing purchase query started", mapOf(
                "product_type" to productType,
                "method" to "queryPurchasesAsync"
            )
        )

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()

        client.queryPurchasesAsync(params) { billingResult, purchasesList ->
            when (billingResult.responseCode) {
                GoogleBillingClient.BillingResponseCode.OK -> {

                    println("DirectBilling: ✅ Purchase query successful!")
                    println("DirectBilling: Found ${purchasesList.size} purchases")

                    if (purchasesList.isEmpty()) {
                        println("DirectBilling: ⚠️ No purchases found")

                        DatadogLogger.info(
                            "Direct billing query - no purchases found", mapOf(
                                "product_type" to productType,
                                "purchase_count" to 0
                            )
                        )
                    } else {
                        val legacyCount = purchasesList.count {
                            val year = Instant.fromEpochMilliseconds(it.purchaseTime)
                                .toString().substring(0, 4).toIntOrNull() ?: 0
                            year < 2024
                        }

                        purchasesList.forEachIndexed { index, purchase ->
                            println("DirectBilling: Purchase #${index + 1}:")
                            println("  Product IDs: ${purchase.products.joinToString(", ")}")
                            println("  Purchase Time: ${Instant.fromEpochMilliseconds(purchase.purchaseTime)}")
                            println("  Purchase Token: ${purchase.purchaseToken.take(50)}...")
                            println("  Order ID: ${purchase.orderId}")
                            println("  State: ${purchase.purchaseState}")
                        }

                        DatadogLogger.info(
                            "Direct billing query - purchases found", mapOf(
                                "product_type" to productType,
                                "purchase_count" to purchasesList.size,
                                "legacy_count" to legacyCount,
                                "product_ids" to purchasesList.flatMap { it.products }.distinct()
                                    .joinToString(","),
                                "purchase_states" to purchasesList.map { it.purchaseState }
                                    .distinct().joinToString(","),
                                "order_ids" to purchasesList.mapNotNull { it.orderId }.take(5)
                                    .joinToString(",")
                            )
                        )
                    }

                    // Convert to our data class
                    val oldPurchases = purchasesList.map { purchase ->
                        OldPurchaseRecord(
                            productIds = purchase.products,
                            purchaseToken = purchase.purchaseToken,
                            purchaseTime = purchase.purchaseTime,
                            signature = purchase.signature,
                            originalJson = purchase.originalJson,
                            orderId = purchase.orderId ?: "unknown",
                            purchaseState = purchase.purchaseState
                        )
                    }

                    continuation.resume(Result.success(oldPurchases))
                }

                GoogleBillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    val error = "Billing unavailable - Google Play Store not installed or outdated"
                    println("DirectBilling: ❌ $error")

                    DatadogLogger.error(
                        "Direct billing query failed - billing unavailable", null, mapOf(
                            "product_type" to productType,
                            "error_code" to billingResult.responseCode.toString(),
                            "debug_message" to (billingResult.debugMessage ?: "none")
                        )
                    )

                    continuation.resume(Result.failure(Exception(error)))
                }

                GoogleBillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    val error = "Service disconnected - need to reconnect"
                    println("DirectBilling: ❌ $error")

                    DatadogLogger.error(
                        "Direct billing query failed - service disconnected", null, mapOf(
                            "product_type" to productType,
                            "error_code" to billingResult.responseCode.toString()
                        )
                    )

                    continuation.resume(Result.failure(Exception(error)))
                }

                else -> {
                    val error =
                        "Query purchase history failed: ${billingResult.debugMessage} (code: ${billingResult.responseCode})"
                    println("DirectBilling: ❌ $error")

                    DatadogLogger.error(
                        "Direct billing query failed", null, mapOf(
                            "product_type" to productType,
                            "error_code" to billingResult.responseCode.toString(),
                            "debug_message" to (billingResult.debugMessage ?: "none"),
                            "response_code_value" to billingResult.responseCode
                        )
                    )

                    continuation.resume(Result.failure(Exception(error)))
                }
            }
        }
    }

    /**
     * Disconnect and cleanup
     */
    fun disconnect() {
        DatadogLogger.info(
            "Direct billing disconnecting", mapOf(
                "client_null" to (billingClient == null)
            )
        )
        billingClient?.endConnection()
        billingClient = null
        println("DirectBilling: Disconnected")
    }
}
