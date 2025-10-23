package me.calebjones.spacelaunchnow.data.billing

import kotlinx.cinterop.*
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.calebjones.spacelaunchnow.data.model.Platform
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.data.model.ProductPricing
import platform.Foundation.*
import platform.StoreKit.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

/**
 * iOS implementation using StoreKit (wrapper pattern)
 * 
 * This class wraps the StoreKit helper to avoid Koin KClass issues with NSObject subclasses.
 * The actual StoreKit interaction is delegated to StoreKitHelper which extends NSObject.
 * 
 * Product mapping:
 * - Android PRODUCT_ID "sln_production_yearly" with base plans -> iOS uses base plan ID directly
 * - Android PRO_LIFETIME "spacelaunchnow_pro" -> iOS "spacelaunchnow_pro"
 * 
 * iOS Product IDs (configured in App Store Connect):
 * - "base_plan" - Monthly subscription (same as Android BASE_PLAN_MONTHLY)
 * - "yearly" - Yearly subscription (same as Android BASE_PLAN_YEARLY)
 * - "spacelaunchnow_pro" - Lifetime purchase (non-consumable)
 */
actual class BillingClient {
    
    private val helper = StoreKitHelper()
    
    actual val purchaseUpdates: Flow<PlatformPurchase> 
        get() = helper.purchaseUpdates

    actual suspend fun initialize(): Result<Unit> {
        return helper.initialize()
    }

    actual suspend fun queryPurchases(): Result<List<PlatformPurchase>> {
        return helper.queryPurchases()
    }

    actual suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<String> {
        return helper.launchPurchaseFlow(productId, basePlanId)
    }

    actual suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        return helper.acknowledgePurchase(purchaseToken)
    }

    actual suspend fun getAvailableProducts(): Result<List<String>> {
        return helper.getAvailableProducts()
    }

    actual suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> {
        return helper.getProductPricing(productId)
    }

    actual fun disconnect() {
        helper.disconnect()
    }
}

/**
 * Internal StoreKit helper that extends NSObject
 * This is not exposed to Koin, avoiding KClass reflection issues
 */
@OptIn(ExperimentalForeignApi::class)
private class StoreKitHelper : NSObject(), SKProductsRequestDelegateProtocol, SKPaymentTransactionObserverProtocol {

    private val _purchaseUpdates = MutableSharedFlow<PlatformPurchase>(replay = 0)
    val purchaseUpdates: Flow<PlatformPurchase> = _purchaseUpdates

    private var productsRequest: SKProductsRequest? = null
    private var cachedProducts: Map<String, SKProduct> = emptyMap()
    
    // Continuations for async operations
    private var initContinuation: CancellableContinuation<Result<Unit>>? = null
    private var purchaseContinuation: CancellableContinuation<Result<String>>? = null
    private var productsContinuation: CancellableContinuation<Result<List<String>>>? = null
    private var pricingContinuation: CancellableContinuation<Result<List<ProductPricing>>>? = null
    
    // Track what we're requesting for
    private var requestingPricing: String? = null

    /**
     * Initialize StoreKit and add transaction observer
     */
    @OptIn(BetaInteropApi::class)
    suspend fun initialize(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        println("IosBillingClient: Initializing StoreKit...")
        
        initContinuation = continuation
        
        // Add this as a transaction observer
        SKPaymentQueue.defaultQueue().addTransactionObserver(this)
        
        // Fetch all available products to verify setup
        val productIds = setOf(
            mapAndroidToIosProductId(SubscriptionProducts.PRODUCT_ID, SubscriptionProducts.BASE_PLAN_MONTHLY),
            mapAndroidToIosProductId(SubscriptionProducts.PRODUCT_ID, SubscriptionProducts.BASE_PLAN_YEARLY),
            SubscriptionProducts.PRO_LIFETIME,
            SubscriptionProducts.FOUNDER_2018  // Legacy founder SKU for testing
        )
        
        productsRequest = SKProductsRequest(productIds)
        productsRequest?.delegate = this
        productsRequest?.start()
        
        continuation.invokeOnCancellation {
            productsRequest?.cancel()
            productsRequest = null
            initContinuation = null
        }
    }

    /**
     * Query current active purchases/subscriptions
     * This checks the receipt for current entitlements
     */
    @OptIn(BetaInteropApi::class)
    suspend fun queryPurchases(): Result<List<PlatformPurchase>> {
        println("IosBillingClient: Querying purchases...")
        
        // Restore completed transactions to get latest state
        SKPaymentQueue.defaultQueue().restoreCompletedTransactions()
        
        // Get receipt data
        val appStoreReceiptURL = NSBundle.mainBundle.appStoreReceiptURL
        if (appStoreReceiptURL == null) {
            println("IosBillingClient: No receipt found")
            return Result.success(emptyList())
        }
        
        val receiptData = NSData.dataWithContentsOfURL(appStoreReceiptURL)
        if (receiptData == null) {
            println("IosBillingClient: Could not load receipt data")
            return Result.success(emptyList())
        }
        
        // For a proper implementation, you would:
        // 1. Send receipt to your backend server
        // 2. Verify with Apple's receipt validation API
        // 3. Return validated purchases
        
        // For now, we check transactions in the queue
        val purchases = mutableListOf<PlatformPurchase>()
        SKPaymentQueue.defaultQueue().transactions.forEach { transaction ->
            val skTransaction = transaction as? SKPaymentTransaction
            if (skTransaction?.transactionState == SKPaymentTransactionState.SKPaymentTransactionStatePurchased ||
                skTransaction?.transactionState == SKPaymentTransactionState.SKPaymentTransactionStateRestored) {
                
                val platformPurchase = skTransaction.toPlatformPurchase()
                purchases.add(platformPurchase)
                
                // IMPORTANT: Finish the transaction to prevent infinite re-processing
                // This is critical for legacy purchases that aren't in the current product catalog
                println("IosBillingClient: Finishing transaction for ${skTransaction.payment.productIdentifier}")
                SKPaymentQueue.defaultQueue().finishTransaction(skTransaction)
            }
        }
        
        println("IosBillingClient: Found ${purchases.size} purchases")
        return Result.success(purchases)
    }

    /**
     * Launch purchase flow for a product
     * 
     * @param productId Android product ID (will be mapped to iOS product ID)
     * @param basePlanId Android base plan ID (used to determine which iOS product to use)
     */
    @OptIn(BetaInteropApi::class)
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<String> = 
        suspendCancellableCoroutine { continuation ->
            
        val iosProductId = mapAndroidToIosProductId(productId, basePlanId)
        println("IosBillingClient: Launching purchase flow for $iosProductId (Android: $productId, basePlan: $basePlanId)")
        
        purchaseContinuation = continuation
        
        // Find the product in cache
        val product = cachedProducts[iosProductId]
        if (product == null) {
            println("IosBillingClient: Product not found in cache, fetching...")
            // Need to fetch product first
            productsRequest = SKProductsRequest(setOf(iosProductId))
            productsRequest?.delegate = this
            productsRequest?.start()
        } else {
            // Launch purchase with cached product
            launchPurchaseWithProduct(product)
        }
        
        continuation.invokeOnCancellation {
            productsRequest?.cancel()
            productsRequest = null
            purchaseContinuation = null
        }
    }

    /**
     * StoreKit handles acknowledgement automatically
     * We finish the transaction when we receive it
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        println("IosBillingClient: Acknowledging purchase (auto-handled by StoreKit)")
        // StoreKit transactions are finished in the transaction observer
        return Result.success(Unit)
    }

    /**
     * Get all available products configured in App Store Connect
     */
    @OptIn(BetaInteropApi::class)
    suspend fun getAvailableProducts(): Result<List<String>> = suspendCancellableCoroutine { continuation ->
        println("IosBillingClient: Querying available products...")
        
        productsContinuation = continuation
        
        val productIds = setOf(
            mapAndroidToIosProductId(SubscriptionProducts.PRODUCT_ID, SubscriptionProducts.BASE_PLAN_MONTHLY),
            mapAndroidToIosProductId(SubscriptionProducts.PRODUCT_ID, SubscriptionProducts.BASE_PLAN_YEARLY),
            SubscriptionProducts.PRO_LIFETIME,
            SubscriptionProducts.FOUNDER_2018 // Legacy founder SKU for testing
        )
        
        productsRequest = SKProductsRequest(productIds)
        productsRequest?.delegate = this
        productsRequest?.start()
        
        continuation.invokeOnCancellation {
            productsRequest?.cancel()
            productsRequest = null
            productsContinuation = null
        }
    }

    /**
     * Get pricing information for a specific product
     */
    @OptIn(BetaInteropApi::class)
    suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> = 
        suspendCancellableCoroutine { continuation ->
        
        println("IosBillingClient: Querying pricing for $productId...")
        
        pricingContinuation = continuation
        requestingPricing = productId
        
        // For subscriptions, we need to get both monthly and yearly
        val productIds = if (productId == SubscriptionProducts.PRODUCT_ID) {
            setOf(
                mapAndroidToIosProductId(productId, SubscriptionProducts.BASE_PLAN_MONTHLY),
                mapAndroidToIosProductId(productId, SubscriptionProducts.BASE_PLAN_YEARLY)
            )
        } else {
            setOf(productId) // One-time purchase uses same ID
        }
        
        productsRequest = SKProductsRequest(productIds)
        productsRequest?.delegate = this
        productsRequest?.start()
        
        continuation.invokeOnCancellation {
            productsRequest?.cancel()
            productsRequest = null
            pricingContinuation = null
            requestingPricing = null
        }
    }

    fun disconnect() {
        println("IosBillingClient: Disconnecting...")
        SKPaymentQueue.defaultQueue().removeTransactionObserver(this)
        productsRequest?.cancel()
        productsRequest = null
        cachedProducts = emptyMap()
    }

    // MARK: - SKProductsRequestDelegate
    
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    override fun productsRequest(request: SKProductsRequest, didReceiveResponse: SKProductsResponse) {
        println("IosBillingClient: Products request completed")
        
        val products = didReceiveResponse.products
        val invalidProductIds = didReceiveResponse.invalidProductIdentifiers
        
        println("IosBillingClient: Found ${products.size} products")
        if (invalidProductIds.isNotEmpty()) {
            println("IosBillingClient: Invalid product IDs: $invalidProductIds")
        }
        
        // Cache products
        val productMap = mutableMapOf<String, SKProduct>()
        products.forEach { product ->
            val skProduct = product as? SKProduct
            if (skProduct != null) {
                productMap[skProduct.productIdentifier] = skProduct
                println("IosBillingClient: Product - ${skProduct.productIdentifier}: ${skProduct.localizedTitle}")
            }
        }
        cachedProducts = productMap
        
        // Handle different continuation types
        when {
            initContinuation != null -> {
                println("IosBillingClient: Initialization complete")
                initContinuation?.resume(Result.success(Unit))
                initContinuation = null
            }
            
            purchaseContinuation != null -> {
                // Launch purchase with first product found
                val product = products.firstOrNull() as? SKProduct
                if (product != null) {
                    launchPurchaseWithProduct(product)
                } else {
                    purchaseContinuation?.resume(Result.failure(Exception("Product not found")))
                    purchaseContinuation = null
                }
            }
            
            productsContinuation != null -> {
                val productIds = products.mapNotNull { (it as? SKProduct)?.productIdentifier }
                // Map back to Android product IDs for consistency
                val androidProductIds = productIds.map { iosId ->
                    mapIosToAndroidProductId(iosId)
                }.distinct()
                
                productsContinuation?.resume(Result.success(androidProductIds))
                productsContinuation = null
            }
            
            pricingContinuation != null -> {
                val pricingList = products.mapNotNull { product ->
                    (product as? SKProduct)?.toProductPricing()
                }
                pricingContinuation?.resume(Result.success(pricingList))
                pricingContinuation = null
                requestingPricing = null
            }
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    override fun request(request: SKRequest, didFailWithError: NSError) {
        println("IosBillingClient: Request failed - ${didFailWithError.localizedDescription}")
        
        val exception = Exception("StoreKit request failed: ${didFailWithError.localizedDescription}")
        
        initContinuation?.resume(Result.failure(exception))
        initContinuation = null
        
        purchaseContinuation?.resume(Result.failure(exception))
        purchaseContinuation = null
        
        productsContinuation?.resume(Result.failure(exception))
        productsContinuation = null
        
        pricingContinuation?.resume(Result.failure(exception))
        pricingContinuation = null
        requestingPricing = null
    }

    // MARK: - SKPaymentTransactionObserver
    
    @OptIn(BetaInteropApi::class)
    override fun paymentQueue(queue: SKPaymentQueue, updatedTransactions: List<*>) {
        println("IosBillingClient: Payment queue updated with ${updatedTransactions.size} transactions")
        
        updatedTransactions.forEach { transaction ->
            val skTransaction = transaction as? SKPaymentTransaction ?: return@forEach
            
            when (skTransaction.transactionState) {
                SKPaymentTransactionState.SKPaymentTransactionStatePurchased -> {
                    println("IosBillingClient: Transaction purchased - ${skTransaction.payment.productIdentifier}")
                    handlePurchasedTransaction(skTransaction)
                }
                
                SKPaymentTransactionState.SKPaymentTransactionStateFailed -> {
                    println("IosBillingClient: Transaction failed - ${skTransaction.error?.localizedDescription}")
                    handleFailedTransaction(skTransaction)
                }
                
                SKPaymentTransactionState.SKPaymentTransactionStateRestored -> {
                    println("IosBillingClient: Transaction restored - ${skTransaction.payment.productIdentifier}")
                    handleRestoredTransaction(skTransaction)
                }
                
                SKPaymentTransactionState.SKPaymentTransactionStatePurchasing -> {
                    println("IosBillingClient: Transaction purchasing...")
                }
                
                SKPaymentTransactionState.SKPaymentTransactionStateDeferred -> {
                    println("IosBillingClient: Transaction deferred (waiting for approval)")
                }
                
                else -> {
                    println("IosBillingClient: Unknown transaction state: ${skTransaction.transactionState}")
                }
            }
        }
    }
    
    @OptIn(BetaInteropApi::class)
    private fun handlePurchasedTransaction(transaction: SKPaymentTransaction) {
        // Convert to platform purchase and emit
        CoroutineScope(Dispatchers.Main).launch {
            _purchaseUpdates.emit(transaction.toPlatformPurchase())
        }
        
        // Finish the transaction
        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
        
        // Resume purchase flow continuation if waiting
        purchaseContinuation?.resume(Result.success(transaction.transactionIdentifier ?: ""))
        purchaseContinuation = null
    }
    
    @OptIn(BetaInteropApi::class)
    private fun handleFailedTransaction(transaction: SKPaymentTransaction) {
        val error = transaction.error
        val nsError = error as? NSError
        
        // Check if user cancelled (SKErrorPaymentCancelled = 2)
        val userCancelled = nsError?.code == 2L
        
        if (userCancelled) {
            println("IosBillingClient: User cancelled purchase")
            purchaseContinuation?.resume(Result.failure(Exception("User cancelled")))
        } else {
            purchaseContinuation?.resume(Result.failure(Exception(nsError?.localizedDescription ?: "Purchase failed")))
        }
        
        purchaseContinuation = null
        
        // Finish the transaction
        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }
    
    @OptIn(BetaInteropApi::class)
    private fun handleRestoredTransaction(transaction: SKPaymentTransaction) {
        // Convert to platform purchase and emit
        CoroutineScope(Dispatchers.Main).launch {
            _purchaseUpdates.emit(transaction.toPlatformPurchase())
        }
        
        // Finish the transaction
        SKPaymentQueue.defaultQueue().finishTransaction(transaction)
    }

    // MARK: - Helper methods
    
    @OptIn(BetaInteropApi::class)
    private fun launchPurchaseWithProduct(product: SKProduct) {
        println("IosBillingClient: Launching purchase for ${product.productIdentifier}")
        
        if (!SKPaymentQueue.canMakePayments()) {
            purchaseContinuation?.resume(Result.failure(Exception("Payments not allowed on this device")))
            purchaseContinuation = null
            return
        }
        
        val payment = SKPayment.paymentWithProduct(product)
        SKPaymentQueue.defaultQueue().addPayment(payment)
    }
    
    /**
     * Map Android product ID + base plan to iOS product ID
     * 
     * iOS uses the base plan ID directly as the product ID
     * - Android: PRODUCT_ID + BASE_PLAN_MONTHLY -> iOS: "base_plan"
     * - Android: PRODUCT_ID + BASE_PLAN_YEARLY -> iOS: "yearly"
     * - Android: PRO_LIFETIME -> iOS: "spacelaunchnow_pro"
     */
    private fun mapAndroidToIosProductId(productId: String, basePlanId: String?): String {
        return when {
            // Subscription with base plan - use base plan ID directly
            productId == SubscriptionProducts.PRODUCT_ID && basePlanId != null -> basePlanId
            
            // Default to yearly if no base plan specified for subscription
            productId == SubscriptionProducts.PRODUCT_ID -> SubscriptionProducts.BASE_PLAN_YEARLY
            
            // One-time purchases use same ID
            productId == SubscriptionProducts.PRO_LIFETIME -> SubscriptionProducts.PRO_LIFETIME
            productId == SubscriptionProducts.FOUNDER_2018 -> SubscriptionProducts.FOUNDER_2018
            
            // Unknown, return as-is
            else -> productId
        }
    }
    
    /**
     * Map iOS product ID back to Android product ID for consistency
     */
    private fun mapIosToAndroidProductId(iosProductId: String): String {
        return when (iosProductId) {
            SubscriptionProducts.BASE_PLAN_MONTHLY, SubscriptionProducts.BASE_PLAN_YEARLY -> 
                SubscriptionProducts.PRODUCT_ID
            else -> iosProductId
        }
    }
}

/**
 * Convert SKPaymentTransaction to PlatformPurchase
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun SKPaymentTransaction.toPlatformPurchase(): PlatformPurchase {
    val transactionDate = this.transactionDate?.timeIntervalSince1970?.times(1000)?.toLong() ?: 0L
    
    return PlatformPurchase(
        purchaseToken = this.transactionIdentifier ?: "",
        productId = this.payment.productIdentifier,
        purchaseTime = transactionDate,
        expiryTime = null, // iOS doesn't provide expiry in transaction
        isAcknowledged = true, // iOS auto-acknowledges
        orderId = this.transactionIdentifier,
        platform = Platform.APP_STORE
    )
}

/**
 * Convert SKProduct to ProductPricing
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun SKProduct.toProductPricing(): ProductPricing {
    val numberFormatter = NSNumberFormatter()
    numberFormatter.numberStyle = NSNumberFormatterCurrencyStyle
    numberFormatter.locale = this.priceLocale
    val formattedPrice = numberFormatter.stringFromNumber(this.price) ?: "${this.price}"
    
    // Determine billing period from product identifier
    val billingPeriod = when (this.productIdentifier) {
        SubscriptionProducts.BASE_PLAN_MONTHLY -> "P1M"
        SubscriptionProducts.BASE_PLAN_YEARLY -> "P1Y"
        SubscriptionProducts.PRO_LIFETIME -> "LIFETIME"
        else -> "P1M" // Default
    }
    
    // Base plan ID is the product identifier for subscriptions
    val basePlanId = when (this.productIdentifier) {
        SubscriptionProducts.BASE_PLAN_MONTHLY -> SubscriptionProducts.BASE_PLAN_MONTHLY
        SubscriptionProducts.BASE_PLAN_YEARLY -> SubscriptionProducts.BASE_PLAN_YEARLY
        SubscriptionProducts.PRO_LIFETIME -> "lifetime"
        else -> this.productIdentifier
    }
    
    // Convert iOS product ID back to Android product ID
    val androidProductId = when (this.productIdentifier) {
        SubscriptionProducts.BASE_PLAN_MONTHLY, SubscriptionProducts.BASE_PLAN_YEARLY -> 
            SubscriptionProducts.PRODUCT_ID
        else -> this.productIdentifier
    }
    
    // Price in micros (StoreKit uses decimal, we need micros)
    val priceAmountMicros = (this.price.doubleValue * 1_000_000).toLong()
    
    val currencyCode = (this.priceLocale.objectForKey(NSLocaleCurrencyCode) as? String) ?: "USD"
    
    return ProductPricing(
        productId = androidProductId,
        basePlanId = basePlanId,
        formattedPrice = formattedPrice,
        priceCurrencyCode = currencyCode,
        priceAmountMicros = priceAmountMicros,
        billingPeriod = billingPeriod,
        title = this.localizedTitle,
        description = this.localizedDescription
    )
}

/**
 * Factory function to create iOS BillingClient
 * No parameters needed for iOS
 */
actual fun createBillingClient(): BillingClient {
    return BillingClient()
}
