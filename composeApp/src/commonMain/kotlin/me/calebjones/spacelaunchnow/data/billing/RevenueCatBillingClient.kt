package me.calebjones.spacelaunchnow.data.billing

import com.revenuecat.purchases.kmp.CustomerInfo
import com.revenuecat.purchases.kmp.Package
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesException
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.Offerings
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.calebjones.spacelaunchnow.data.model.Platform
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.data.model.ProductPricing
import kotlin.time.ExperimentalTime

/**
 * RevenueCat implementation of BillingClient
 *
 * This implementation uses RevenueCat SDK to handle both Android (Google Play)
 * and iOS (App Store) in-app purchases with a unified API.
 *
 */
class RevenueCatBillingClient(
    private val purchases: Purchases
) {

    private val _purchaseUpdates = MutableSharedFlow<PlatformPurchase>(replay = 0)
    val purchaseUpdates: Flow<PlatformPurchase> = _purchaseUpdates.asSharedFlow()

    /**
     * Initialize the billing client
     * RevenueCat is initialized earlier in the app lifecycle,
     * so this just sets up listeners
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            // Just fetch initial customer info to verify connection
            purchases.awaitCustomerInfo()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Query current active purchases
     * This directly queries RevenueCat which syncs with Google Play/App Store
     */
    suspend fun queryPurchases(): Result<List<PlatformPurchase>> {
        return try {
            val customerInfo = purchases.awaitCustomerInfo()
            val purchases =
                customerInfo.entitlements.active.entries.map { (entitlementId, entitlementInfo) ->
                    customerInfoToPlatformPurchase(customerInfo, entitlementId)
                }
            Result.success(purchases)
        } catch (e: PurchasesException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Start purchase flow for a subscription
     * RevenueCat handles the product ID mapping automatically
     */
    suspend fun launchPurchaseFlow(productId: String, basePlanId: String? = null): Result<String> {
        return try {
            // Get offerings from RevenueCat
            val offerings = purchases.awaitOfferings()

            // Find the package that matches our product
            val packageToPurchase = findPackage(offerings, productId, basePlanId)
                ?: return Result.failure(IllegalArgumentException("Product not found: $productId"))

            // Launch purchase flow
            val purchaseResult = purchases.awaitPurchase(packageToPurchase)
            val storeTransaction = purchaseResult.storeTransaction
            val customerInfo = purchaseResult.customerInfo

            // Emit purchase update
            customerInfo.entitlements.active.entries.forEach { (entitlementId, _) ->
                val purchase = customerInfoToPlatformPurchase(customerInfo, entitlementId)
                _purchaseUpdates.tryEmit(purchase)
            }

            // Return transaction ID as token
            Result.success("")
        } catch (e: PurchasesException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Acknowledge a purchase
     * RevenueCat handles this automatically, so this is a no-op
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Unit> {
        // RevenueCat automatically acknowledges purchases
        return Result.success(Unit)
    }

    /**
     * Get available products for purchase
     */
    suspend fun getAvailableProducts(): Result<List<String>> {
        return try {
            val offerings = purchases.awaitOfferings()
            val productIds = offerings.current?.availablePackages?.map { pkg ->
                pkg.storeProduct.id
            } ?: emptyList()
            Result.success(productIds)
        } catch (e: PurchasesException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get product pricing details
     */
    suspend fun getProductPricing(productId: String): Result<List<ProductPricing>> {
        return try {
            val offerings = purchases.awaitOfferings()
            val pricingList = mutableListOf<ProductPricing>()

            offerings.current?.availablePackages?.forEach { pkg ->
                val product = pkg.storeProduct
                if (matchesProduct(product, productId)) {
                    pricingList.add(
                        ProductPricing(
                            productId = product.id,
                            basePlanId = pkg.identifier,
                            formattedPrice = product.price.formatted,
                            priceAmountMicros = product.price.amountMicros.toLong(),
                            priceCurrencyCode = product.price.currencyCode,
                            billingPeriod = product.period?.toString() ?: "",
                            title = product.title,
                            description = ""
                        )
                    )
                }
            }

            Result.success(pricingList)
        } catch (e: PurchasesException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Disconnect and cleanup resources
     */
    fun disconnect() {
        // RevenueCat manages its own lifecycle
        // No cleanup needed
    }

    // Helper functions

    @OptIn(ExperimentalTime::class)
    private fun customerInfoToPlatformPurchase(
        customerInfo: CustomerInfo,
        entitlementId: String
    ): PlatformPurchase {
        val entitlement = customerInfo.entitlements.active[entitlementId]
        return PlatformPurchase(
            purchaseToken = entitlementId,
            productId = entitlement?.productIdentifier ?: entitlementId,
            purchaseTime = entitlement?.originalPurchaseDate?.toEpochMilliseconds() ?: 0L,
            expiryTime = entitlement?.expirationDate?.toEpochMilliseconds(),
            isAcknowledged = true,
            orderId = null,
            platform = Platform.UNKNOWN // RevenueCat abstracts, can't know the real platform here
        )
    }

    /**
     * Find the RevenueCat package matching the requested product
     *
     * Maps legacy product IDs to RevenueCat package identifiers:
     * - spacelaunchnow_pro → $rc_lifetime
     * - sln_production_yearly:base-plan → $rc_monthly
     * - sln_production_yearly:yearly → $rc_annual
     */
    private fun findPackage(
        offerings: Offerings,
        productId: String,
        basePlanId: String?
    ): Package? {
        val currentOffering = offerings.current ?: return null

        // Map legacy product IDs to RevenueCat package identifiers
        val packageIdentifier = when {
            // Lifetime purchase
            productId == SubscriptionProducts.PRO_LIFETIME ->
                SubscriptionProducts.RC_PACKAGE_LIFETIME

            // Monthly subscription (via base plan ID)
            basePlanId == SubscriptionProducts.BASE_PLAN_MONTHLY ->
                SubscriptionProducts.RC_PACKAGE_MONTHLY

            // Annual subscription (via base plan ID)
            basePlanId == SubscriptionProducts.BASE_PLAN_YEARLY ->
                SubscriptionProducts.RC_PACKAGE_ANNUAL

            // If already a RevenueCat package identifier, use as-is
            basePlanId?.startsWith("\$rc_") == true -> basePlanId

            // Default: try to match by product ID
            else -> null
        }

        // If we have a mapped package identifier, find it
        if (packageIdentifier != null) {
            val matchedPackage = currentOffering.availablePackages.find { pkg ->
                pkg.identifier == packageIdentifier
            }
            if (matchedPackage != null) {
                println("RevenueCat: Mapped $productId:$basePlanId → $packageIdentifier")
                return matchedPackage
            }
        }

        // Fallback: Try to find by exact product ID match
        val fallbackPackage = currentOffering.availablePackages.find { pkg ->
            matchesProduct(pkg.storeProduct, productId)
        }

        if (fallbackPackage != null) {
            println("RevenueCat: Found package by product ID: ${fallbackPackage.identifier}")
        } else {
            println("RevenueCat: ⚠️ No package found for $productId:$basePlanId")
            println("  Available packages: ${currentOffering.availablePackages.map { it.identifier }}")
        }

        return fallbackPackage
    }

    private fun matchesProduct(product: StoreProduct, productId: String): Boolean {
        return product.id == productId ||
                product.id.contains(productId, ignoreCase = true) ||
                productId.contains(product.id, ignoreCase = true)
    }
}
