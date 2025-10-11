package me.calebjones.spacelaunchnow.data.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.data.model.Platform
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.data.model.ProductPricing
import kotlin.coroutines.resume
import com.android.billingclient.api.BillingClient as GoogleBillingClient

/**
 * Android implementation using Google Play Billing Library 8.0.0
 */
actual class BillingClient(private val context: Context) {

    private var billingClient: GoogleBillingClient? = null
    private var currentActivity: Activity? = null

    private val _purchaseUpdates = MutableSharedFlow<PlatformPurchase>()
    actual val purchaseUpdates: Flow<PlatformPurchase> = _purchaseUpdates

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            GoogleBillingClient.BillingResponseCode.OK -> {
                purchases?.forEach { purchase ->
                    CoroutineScope(Dispatchers.Main).launch {
                        _purchaseUpdates.emit(purchase.toPlatformPurchase())
                    }
                }
            }

            GoogleBillingClient.BillingResponseCode.USER_CANCELED -> {
                println("AndroidBillingClient: User canceled purchase")
            }

            else -> {
                println("AndroidBillingClient: Purchase update failed - ${billingResult.debugMessage}")
            }
        }
    }

    actual suspend fun initialize(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        println("AndroidBillingClient: Initializing...")

        try {
            billingClient = GoogleBillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder()
                        .enableOneTimeProducts()
                        .build()
                )
                .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    when (billingResult.responseCode) {
                        GoogleBillingClient.BillingResponseCode.OK -> {
                            println("AndroidBillingClient: Connected successfully")
                            continuation.resume(Result.success(Unit))
                        }

                        else -> {
                            val error =
                                Exception("Billing setup failed: ${billingResult.debugMessage}")
                            println("AndroidBillingClient: Setup failed - ${billingResult.debugMessage}")
                            continuation.resume(Result.failure(error))
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    println("AndroidBillingClient: Service disconnected")
                }
            })
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    actual suspend fun queryPurchases(): Result<List<PlatformPurchase>> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || !client.isReady) {
                continuation.resume(Result.failure(Exception("Billing client not ready")))
                return@suspendCancellableCoroutine
            }

            println("AndroidBillingClient: Querying purchases...")

            val allPurchases = mutableListOf<PlatformPurchase>()
            var completedQueries = 0
            val totalQueries = 2

            fun checkCompletion() {
                completedQueries++
                if (completedQueries == totalQueries) {
                    println("AndroidBillingClient: Total ${allPurchases.size} purchases found")
                    continuation.resume(Result.success(allPurchases))
                }
            }

            // Query subscriptions
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(GoogleBillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, purchases ->
                when (billingResult.responseCode) {
                    GoogleBillingClient.BillingResponseCode.OK -> {
                        val subsPurchases = purchases.map { it.toPlatformPurchase() }
                        allPurchases.addAll(subsPurchases)
                        println("AndroidBillingClient: Found ${subsPurchases.size} subscriptions")
                    }

                    else -> {
                        println("AndroidBillingClient: Subscription query failed - ${billingResult.debugMessage}")
                    }
                }
                checkCompletion()
            }

            // Query in-app purchases
            client.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(GoogleBillingClient.ProductType.INAPP)
                    .build()
            ) { billingResult, purchases ->
                when (billingResult.responseCode) {
                    GoogleBillingClient.BillingResponseCode.OK -> {
                        val inAppPurchases = purchases.map { it.toPlatformPurchase() }
                        allPurchases.addAll(inAppPurchases)
                        println("AndroidBillingClient: Found ${inAppPurchases.size} in-app purchases")
                    }

                    else -> {
                        println("AndroidBillingClient: In-app query failed - ${billingResult.debugMessage}")
                    }
                }
                checkCompletion()
            }
        }

    actual suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<String> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClient
            val activity = currentActivity

            if (client == null || !client.isReady) {
                continuation.resume(Result.failure(Exception("Billing client not ready")))
                return@suspendCancellableCoroutine
            }

            if (activity == null) {
                continuation.resume(Result.failure(Exception("No activity set for purchase flow")))
                return@suspendCancellableCoroutine
            }

            val isOneTimePurchase = SubscriptionProducts.isOneTimePurchase(productId)
            val productType = if (isOneTimePurchase) {
                GoogleBillingClient.ProductType.INAPP
            } else {
                GoogleBillingClient.ProductType.SUBS
            }

            println("AndroidBillingClient: Launching ${if (isOneTimePurchase) "one-time" else "subscription"} purchase flow for $productId")

            // First, query product details
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(productType)
                    .build()
            )

            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
            ) { billingResult, queryProductDetailsResult ->
                when (billingResult.responseCode) {
                    GoogleBillingClient.BillingResponseCode.OK -> {
                        val productDetailsList = queryProductDetailsResult.productDetailsList
                        val unfetchedProducts = queryProductDetailsResult.unfetchedProductList

                        if (productDetailsList.isNotEmpty()) {
                            launchBillingFlowWithProductDetails(
                                client,
                                activity,
                                productDetailsList[0],
                                basePlanId,
                                isOneTimePurchase,
                                continuation
                            )
                        } else if (unfetchedProducts.isNotEmpty()) {
                            println("AndroidBillingClient: Some products could not be fetched: ${unfetchedProducts.map { it.productId }}")
                            continuation.resume(Result.failure(Exception("Product not found: $productId")))
                        } else {
                            continuation.resume(Result.failure(Exception("Product not found: $productId")))
                        }
                    }

                    else -> {
                        continuation.resume(Result.failure(Exception("Failed to query product details: ${billingResult.debugMessage}")))
                    }
                }
            }
        }

    private fun launchBillingFlowWithProductDetails(
        client: GoogleBillingClient,
        activity: Activity,
        productDetails: ProductDetails,
        basePlanId: String?,
        isOneTimePurchase: Boolean,
        continuation: kotlin.coroutines.Continuation<Result<String>>
    ) {
        try {
            val productDetailsParamsList = if (isOneTimePurchase) {
                // One-time purchase
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )
            } else {
                // Subscription - find appropriate offer
                val subscriptionOfferDetails = productDetails.subscriptionOfferDetails
                if (subscriptionOfferDetails.isNullOrEmpty()) {
                    continuation.resume(Result.failure(Exception("No subscription offers available")))
                    return
                }

                val selectedOffer = if (basePlanId != null) {
                    subscriptionOfferDetails.find { it.basePlanId == basePlanId }
                } else {
                    subscriptionOfferDetails.firstOrNull()
                }

                if (selectedOffer == null) {
                    continuation.resume(Result.failure(Exception("No suitable subscription offer found")))
                    return
                }

                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(selectedOffer.offerToken)
                        .build()
                )
            }

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            val launchResult = client.launchBillingFlow(activity, billingFlowParams)

            when (launchResult.responseCode) {
                GoogleBillingClient.BillingResponseCode.OK -> {
                    println("AndroidBillingClient: Purchase flow launched successfully")
                    continuation.resume(Result.success(""))
                }

                else -> {
                    continuation.resume(Result.failure(Exception("Failed to launch billing flow: ${launchResult.debugMessage}")))
                }
            }
        } catch (e: Exception) {
            continuation.resume(Result.failure(e))
        }
    }

    actual suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || !client.isReady) {
                continuation.resume(Result.failure(Exception("Billing client not ready")))
                return@suspendCancellableCoroutine
            }

            println("AndroidBillingClient: Acknowledging purchase...")

            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchaseToken)
                .build()

            client.acknowledgePurchase(params) { billingResult ->
                when (billingResult.responseCode) {
                    GoogleBillingClient.BillingResponseCode.OK -> {
                        println("AndroidBillingClient: Purchase acknowledged successfully")
                        continuation.resume(Result.success(Unit))
                    }

                    else -> {
                        continuation.resume(Result.failure(Exception("Failed to acknowledge purchase: ${billingResult.debugMessage}")))
                    }
                }
            }
        }

    actual suspend fun getAvailableProducts(): Result<List<String>> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || !client.isReady) {
                continuation.resume(Result.failure(Exception("Billing client not ready")))
                return@suspendCancellableCoroutine
            }

            println("AndroidBillingClient: Querying available products...")

            val productList = SubscriptionProducts.ALL_SUBSCRIPTION_PRODUCTS.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(GoogleBillingClient.ProductType.SUBS)
                    .build()
            }

            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
            ) { billingResult, queryProductDetailsResult ->
                when (billingResult.responseCode) {
                    GoogleBillingClient.BillingResponseCode.OK -> {
                        val productDetailsList = queryProductDetailsResult.productDetailsList
                        val unfetchedProducts = queryProductDetailsResult.unfetchedProductList

                        val availableProducts = mutableListOf<String>()
                        productDetailsList.forEach { productDetails ->
                            availableProducts.add(productDetails.productId)
                        }

                        if (unfetchedProducts.isNotEmpty()) {
                            println("AndroidBillingClient: Some products could not be fetched: ${unfetchedProducts.map { it.productId }}")
                        }

                        println("AndroidBillingClient: Found ${availableProducts.size} available products")
                        continuation.resume(Result.success(availableProducts))
                    }

                    else -> {
                        continuation.resume(Result.failure(Exception("Failed to query products: ${billingResult.debugMessage}")))
                    }
                }
            }
        }

    actual suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> =
        suspendCancellableCoroutine { continuation ->
            val client = billingClient
            if (client == null || !client.isReady) {
                continuation.resume(Result.failure(Exception("Billing client not ready")))
                return@suspendCancellableCoroutine
            }

            println("AndroidBillingClient: Querying pricing for $productId...")

            val isOneTimePurchase = SubscriptionProducts.isOneTimePurchase(productId)
            val productType = if (isOneTimePurchase) {
                GoogleBillingClient.ProductType.INAPP
            } else {
                GoogleBillingClient.ProductType.SUBS
            }

            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(productType)
                    .build()
            )

            client.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder()
                    .setProductList(productList)
                    .build()
            ) { billingResult, queryProductDetailsResult ->
                when (billingResult.responseCode) {
                    GoogleBillingClient.BillingResponseCode.OK -> {
                        val productDetailsList = queryProductDetailsResult.productDetailsList
                        val unfetchedProducts = queryProductDetailsResult.unfetchedProductList

                        if (productDetailsList.isNotEmpty()) {
                            val pricingList = extractPricingFromProductDetails(
                                productDetailsList[0],
                                isOneTimePurchase
                            )
                            continuation.resume(Result.success(pricingList))
                        } else if (unfetchedProducts.isNotEmpty()) {
                            println("AndroidBillingClient: Some products could not be fetched: ${unfetchedProducts.map { it.productId }}")
                            continuation.resume(Result.failure(Exception("Product not found: $productId")))
                        } else {
                            continuation.resume(Result.failure(Exception("Product not found: $productId")))
                        }
                    }

                    else -> {
                        continuation.resume(Result.failure(Exception("Failed to query product pricing: ${billingResult.debugMessage}")))
                    }
                }
            }
        }

    private fun extractPricingFromProductDetails(
        productDetails: ProductDetails,
        isOneTimePurchase: Boolean
    ): List<ProductPricing> {
        val pricingList = mutableListOf<ProductPricing>()

        try {
            if (isOneTimePurchase) {
                val oneTimeOffer = productDetails.oneTimePurchaseOfferDetails
                if (oneTimeOffer != null) {
                    val pricing = ProductPricing(
                        productId = productDetails.productId,
                        basePlanId = "lifetime",
                        formattedPrice = oneTimeOffer.formattedPrice,
                        priceCurrencyCode = oneTimeOffer.priceCurrencyCode,
                        priceAmountMicros = oneTimeOffer.priceAmountMicros,
                        billingPeriod = "LIFETIME",
                        title = productDetails.title,
                        description = productDetails.description
                    )
                    pricingList.add(pricing)
                    println("AndroidBillingClient: Found one-time pricing - ${oneTimeOffer.formattedPrice}")
                }
            } else {
                productDetails.subscriptionOfferDetails?.forEach { offerDetails ->
                    val pricingPhase = offerDetails.pricingPhases.pricingPhaseList.firstOrNull()
                    if (pricingPhase != null) {
                        val pricing = ProductPricing(
                            productId = productDetails.productId,
                            basePlanId = offerDetails.basePlanId,
                            formattedPrice = pricingPhase.formattedPrice,
                            priceCurrencyCode = pricingPhase.priceCurrencyCode,
                            priceAmountMicros = pricingPhase.priceAmountMicros,
                            billingPeriod = pricingPhase.billingPeriod,
                            title = productDetails.title,
                            description = productDetails.description
                        )
                        pricingList.add(pricing)
                        println("AndroidBillingClient: Found pricing - ${offerDetails.basePlanId}: ${pricingPhase.formattedPrice}")
                    }
                }
            }
        } catch (e: Exception) {
            println("AndroidBillingClient: Error extracting pricing - ${e.message}")
        }

        return pricingList
    }

    actual fun disconnect() {
        println("AndroidBillingClient: Disconnecting...")
        try {
            billingClient?.endConnection()
        } catch (e: Exception) {
            println("AndroidBillingClient: Error during disconnect - ${e.message}")
        } finally {
            billingClient = null
        }
    }

    /**
     * Set the activity for launching purchase flows
     */
    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }
}

/**
 * Convert Google Play Purchase to platform-agnostic PlatformPurchase
 */
private fun Purchase.toPlatformPurchase(): PlatformPurchase {
    return PlatformPurchase(
        purchaseToken = purchaseToken,
        productId = products.firstOrNull() ?: "",
        purchaseTime = purchaseTime,
        expiryTime = null, // Google Play doesn't provide expiry in Purchase object
        isAcknowledged = isAcknowledged,
        orderId = orderId ?: "",
        platform = Platform.GOOGLE_PLAY
    )
}

/**
 * Factory function to create Android BillingClient with Context
 */
actual fun createBillingClient(): BillingClient {
    return BillingClient(org.koin.mp.KoinPlatform.getKoin().get())
}
