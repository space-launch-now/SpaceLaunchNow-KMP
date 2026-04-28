package me.calebjones.spacelaunchnow.data.billing

import android.content.Context
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PeriodType
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.models.freePhase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.BuildConfig
import me.calebjones.spacelaunchnow.analytics.DatadogRUM
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.PurchaseState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.sync.PhoneDataLayerSync
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.util.toDisplayString
import kotlin.coroutines.resume

/**
 * Android implementation of BillingManager using RevenueCat
 *
 * This class handles all RevenueCat operations for Android platform:
 * - Initialization with Android API key
 * - Purchase flows
 * - Subscription state management
 * - Entitlement checking
 */
class AndroidBillingManager(
    private val context: Context,
    private val phoneDataLayerSync: PhoneDataLayerSync? = null,
) : BillingManager {

    private val log = logger()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _isInitialized = MutableStateFlow(false)
    override val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val _purchaseState = MutableStateFlow(PurchaseState())
    override val purchaseState: StateFlow<PurchaseState> = _purchaseState.asStateFlow()

    private val purchases: Purchases
        get() = Purchases.sharedInstance

    override suspend fun initialize(appUserId: String?): Result<Unit> {
        return try {
            log.d { "Initializing RevenueCat for Android - debug: ${BuildConfig.IS_DEBUG}, appUserId: $appUserId" }

            // Configure RevenueCat
            Purchases.logLevel = if (BuildConfig.IS_DEBUG) LogLevel.DEBUG else LogLevel.WARN

            Purchases.configure(apiKey = BuildConfig.REVENUECAT_ANDROID_KEY) {
                this.appUserId = appUserId
            }

            log.d { "RevenueCat configured" }
            _isInitialized.value = true

            // Register delegate for real-time entitlement updates (including Wear OS sync)
            purchases.delegate = object : PurchasesDelegate {
                override fun onPurchasePromoProduct(
                    product: StoreProduct,
                    startPurchase: (
                        onError: (PurchasesError, Boolean) -> Unit,
                        onSuccess: (StoreTransaction, CustomerInfo) -> Unit
                    ) -> Unit
                ) {
                    // No-op on Android; App Store only
                }

                override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
                    updatePurchaseState(customerInfo)
                    syncEntitlementToWatch(customerInfo)
                }
            }

            // Initial sync with store (silent, no UI)
            syncPurchases()

            // Load initial purchase state
            val success = refreshPurchaseState()
            if (success) {
                log.i { "Initialization successful" }
            } else {
                log.w { "Initialization completed but failed to load customer info" }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "Failed to initialize AndroidBillingManager - ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun refreshPurchaseState(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (!_isInitialized.value) {
                    log.w { "Cannot refresh - not initialized" }
                    continuation.resume(false)
                    return@suspendCancellableCoroutine
                }

                log.d { "Refreshing purchase state..." }

                purchases.getCustomerInfo(
                    onError = { error ->
                        log.e { "Failed to get customer info - message: ${error.message}, code: ${error.code.name}" }
                        continuation.resume(false)
                    },
                    onSuccess = { customerInfo ->
                        updatePurchaseState(customerInfo)
                        log.i { "Purchase state refreshed" }
                        continuation.resume(true)
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "Exception refreshing purchase state" }
                continuation.resume(false)
            }
        }
    }

    override suspend fun getAvailableProducts(): Result<List<ProductInfo>> {
        return try {
            if (!_isInitialized.value) {
                return Result.failure(IllegalStateException("BillingManager not initialized"))
            }

            log.d { "Loading available products..." }
            val offerings = purchases.awaitOfferings()

            val products = offerings.current?.availablePackages?.map { pkg ->
                val product = pkg.storeProduct

                // Strategy 1: explicit freeTrial subscription option (Google Play base plan offer)
                val freeTrialOption = product.subscriptionOptions?.freeTrial
                val freeTrialPhaseFromOption = freeTrialOption?.pricingPhases?.firstOrNull {
                    it.price.amountMicros == 0L
                }

                // Strategy 2: scan defaultOption pricing phases for a $0 phase
                val freeTrialPhaseFromDefault = product.defaultOption?.pricingPhases
                    ?.firstOrNull { it.price.amountMicros == 0L }

                val freeTrialPhase = freeTrialPhaseFromOption ?: freeTrialPhaseFromDefault

                // Extract intro offer info (non-free intro pricing)
                val introOption = product.subscriptionOptions?.introOffer
                val introPhase = introOption?.pricingPhases?.firstOrNull {
                    it.price.amountMicros > 0L
                }

                log.d {
                    "Product ${product.id} (${pkg.identifier}): " +
                            "freeTrial=${freeTrialPhase != null}, " +
                            "trialPeriod=${freeTrialPhase?.billingPeriod?.toDisplayString()}, " +
                            "subscriptionOptions=${product.subscriptionOptions != null}"
                }

                ProductInfo(
                    productId = product.id,
                    basePlanId = pkg.identifier,
                    title = product.title,
                    description = "${pkg.packageType} - ${product.period?.unit?.name ?: "One-time"}",
                    formattedPrice = product.price.formatted,
                    priceAmountMicros = product.price.amountMicros.toLong(),
                    currencyCode = product.price.currencyCode,
                    hasFreeTrial = freeTrialPhase != null,
                    freeTrialPeriodDisplay = freeTrialPhase?.billingPeriod?.toDisplayString(),
                    freeTrialPeriodValue = freeTrialPhase?.billingPeriod?.value,
                    freeTrialPeriodUnit = freeTrialPhase?.billingPeriod?.unit?.name,
                    hasIntroOffer = introPhase != null,
                    introOfferPrice = introPhase?.price?.formatted,
                    introOfferPeriodDisplay = introPhase?.billingPeriod?.toDisplayString()
                )
            } ?: emptyList()

            log.i { "Found ${products.size} available products: ${products.map { "${it.basePlanId}(trial=${it.hasFreeTrial})" }}" }
            Result.success(products)
        } catch (e: Exception) {
            log.e(e) { "Failed to get available products" }
            Result.failure(e)
        }
    }

    override suspend fun launchPurchaseFlow(productId: String, basePlanId: String?): Result<Unit> {
        return try {
            if (!_isInitialized.value) {
                return Result.failure(IllegalStateException("BillingManager not initialized"))
            }

            log.i { "Starting purchase flow - productId: $productId, basePlanId: $basePlanId" }

            val offerings = purchases.awaitOfferings()
            val pkg = offerings.current?.availablePackages?.find {
                it.storeProduct.id == productId &&
                        (basePlanId == null || it.identifier == basePlanId)
            } ?: return Result.failure(IllegalArgumentException("Product not found: $productId"))

            val result = purchases.awaitPurchase(pkg)
            updatePurchaseState(result.customerInfo)

            log.i { "Purchase completed - productId: $productId, basePlanId: ${basePlanId ?: "default"}" }

            Result.success(Unit)
        } catch (e: Exception) {
            log.e(e) { "Purchase failed - productId: $productId" }
            Result.failure(e)
        }
    }

    override suspend fun restorePurchases(): Result<PurchaseState> {
        return suspendCancellableCoroutine { continuation ->
            try {
                if (!_isInitialized.value) {
                    continuation.resume(Result.failure(IllegalStateException("BillingManager not initialized")))
                    return@suspendCancellableCoroutine
                }

                log.i { "Restoring purchases..." }

                purchases.restorePurchases(
                    onError = { error ->
                        log.e { "Restore failed - ${error.message}" }
                        continuation.resume(Result.failure(Exception(error.message)))
                    },
                    onSuccess = { customerInfo ->
                        updatePurchaseState(customerInfo)
                        log.i { "Purchases restored successfully" }
                        continuation.resume(Result.success(_purchaseState.value))
                    }
                )
            } catch (e: Exception) {
                log.e(e) { "Exception restoring purchases" }
                continuation.resume(Result.failure(e))
            }
        }
    }

    override suspend fun syncPurchases() {
        suspendCancellableCoroutine { continuation ->
            try {
                if (!_isInitialized.value) {
                    continuation.resume(Unit)
                    return@suspendCancellableCoroutine
                }

                log.d { "Syncing purchases with store..." }

                purchases.syncPurchases(
                    onError = { error ->
                        log.w { "Sync failed - ${error.message}" }
                        continuation.resume(Unit)
                    },
                    onSuccess = { customerInfo ->
                        updatePurchaseState(customerInfo)
                        log.i { "Purchases synced" }
                        continuation.resume(Unit)
                    }
                )
            } catch (e: Exception) {
                log.w { "Exception syncing purchases - ${e.message}" }
                continuation.resume(Unit)
            }
        }
    }

    override fun hasEntitlement(entitlementId: String): Boolean {
        return _purchaseState.value.activeEntitlements.contains(entitlementId)
    }

    override fun getActiveEntitlements(): Set<String> {
        return _purchaseState.value.activeEntitlements
    }

    /**
     * Update purchase state from RevenueCat CustomerInfo
     */
    private fun updatePurchaseState(customerInfo: CustomerInfo) {
        log.d { "Updating purchase state..." }

        val activeEntitlements = customerInfo.entitlements.active.keys

        // Collect product IDs from entitlements and active subscriptions only.
        // nonSubscriptionTransactions (one-time IAPs) are intentionally excluded —
        // they do not represent an active subscription and must not affect subscription type.
        val productIds = buildSet {
            // From entitlements
            customerInfo.entitlements.active.values.forEach {
                it.productIdentifier?.let { productId -> add(productId) }
            }
            // From active subscriptions only
            addAll(customerInfo.activeSubscriptions)
        }

        val subscriptionType = determineSubscriptionType(activeEntitlements, productIds)
        val features = determineFeatures(subscriptionType)

        // Detect if any active entitlement is in a trial period
        val trialEntitlement = customerInfo.entitlements.active.values.firstOrNull {
            it.periodType == PeriodType.TRIAL
        }
        val isInTrial = trialEntitlement != null
        val trialExpires = trialEntitlement?.expirationDateMillis

        log.i { "Purchase state updated - type: $subscriptionType, entitlements: $activeEntitlements, products: $productIds, features: ${features.size}, inTrial: $isInTrial" }

        _purchaseState.value = PurchaseState(
            isSubscribed = subscriptionType != SubscriptionType.FREE,
            subscriptionType = subscriptionType,
            activeEntitlements = activeEntitlements,
            activeProductIds = productIds,
            features = features,
            lastRefreshed = System.currentTimeMillis(),
            userId = customerInfo.originalAppUserId,
            isInTrialPeriod = isInTrial,
            trialExpiresAt = trialExpires
        )

        // Update Datadog RUM with user subscription info
        DatadogRUM.setUser(
            id = customerInfo.originalAppUserId,
            extraInfo = mapOf(
                "platform" to "Android",
                "active_entitlements" to activeEntitlements.joinToString(","),
                "active_subscriptions" to customerInfo.activeSubscriptions.joinToString(","),
                "subscription_type" to subscriptionType.name,
                "has_premium" to (subscriptionType != SubscriptionType.FREE)
            )
        )
    }

    /**
     * Sync entitlement state to Wear OS watch via DataLayer
     */
    private fun syncEntitlementToWatch(customerInfo: CustomerInfo) {
        val dataLayerSync = phoneDataLayerSync ?: return
        val activeEntitlements = customerInfo.entitlements.active
        val isActive = activeEntitlements.containsKey("premium") ||
            activeEntitlements.containsKey(SubscriptionProducts.RC_ENTITLEMENT_PRO) ||
            activeEntitlements.containsKey("lifetime") ||
            activeEntitlements.containsKey(SubscriptionProducts.RC_ENTITLEMENT_LIFETIME) ||
            activeEntitlements.containsKey("founder")
        val expiresAt = activeEntitlements.values.firstOrNull()?.expirationDateMillis?.let {
            Instant.fromEpochMilliseconds(it)
        }
        scope.launch {
            try {
                dataLayerSync.syncEntitlementToWatch(
                    active = isActive,
                    expiresAt = expiresAt,
                )
            } catch (e: Exception) {
                log.e(e) { "Failed to sync entitlement to watch" }
            }
        }
    }

    /**
     * Determine subscription type from entitlements and product IDs
     */
    private fun determineSubscriptionType(
        entitlements: Set<String>,
        productIds: Set<String>
    ): SubscriptionType {
        log.d {
            "Determining subscription type - entitlements: ${
                entitlements.joinToString(", ") { "\"$it\"" }.ifEmpty { "(none)" }
            }, products: ${productIds.joinToString(", ") { "\"$it\"" }.ifEmpty { "(none)" }}"
        }

        // Check entitlements first (most reliable)
        val result = when {
            // Check for premium entitlement (all variations)
            entitlements.contains("premium") -> {
                log.v { "Match: Found 'premium' entitlement → PREMIUM" }
                SubscriptionType.PREMIUM
            }

            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_PRO) -> {
                log.v { "Match: Found '${SubscriptionProducts.RC_ENTITLEMENT_PRO}' entitlement → PREMIUM" }
                SubscriptionType.PREMIUM
            }

            // Check for lifetime/founder entitlements (all variations)
            entitlements.contains("founder") -> {
                log.v { "Match: Found 'founder' entitlement → LIFETIME" }
                SubscriptionType.LIFETIME
            }

            entitlements.contains("lifetime") -> {
                log.v { "Match: Found 'lifetime' entitlement → LIFETIME" }
                SubscriptionType.LIFETIME
            }

            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LIFETIME) -> {
                log.v { "Match: Found '${SubscriptionProducts.RC_ENTITLEMENT_LIFETIME}' entitlement → LIFETIME" }
                SubscriptionType.LIFETIME
            }

            // Check for legacy entitlement
            entitlements.contains(SubscriptionProducts.RC_ENTITLEMENT_LEGACY) -> {
                log.v { "Match: Found '${SubscriptionProducts.RC_ENTITLEMENT_LEGACY}' entitlement → LEGACY" }
                SubscriptionType.LEGACY
            }

            // Fallback to product ID matching for legacy purchases
            productIds.any { it.contains("lifetime", ignoreCase = true) } -> {
                val matchedProduct = productIds.first { it.contains("lifetime", ignoreCase = true) }
                log.v { "Match: Found lifetime product ID '$matchedProduct' → LIFETIME" }
                SubscriptionType.LIFETIME
            }

            productIds.any {
                it == "spacelaunchnow_pro" ||
                        it.contains("yearly", ignoreCase = true) ||
                        it.contains("monthly", ignoreCase = true)
            } -> {
                val matchedProduct = productIds.first {
                    it == "spacelaunchnow_pro" ||
                            it.contains("yearly", ignoreCase = true) ||
                            it.contains("monthly", ignoreCase = true)
                }
                log.v { "Match: Found premium product ID '$matchedProduct' → PREMIUM" }
                SubscriptionType.PREMIUM
            }

            else -> {
                log.d { "No entitlements or products found → FREE" }
                SubscriptionType.FREE
            }
        }

        log.i { "Subscription type determined: $result - entitlements: ${entitlements.size}, products: ${productIds.size}" }

        return result
    }

    /**
     * Determine available features based on subscription type
     */
    private fun determineFeatures(type: SubscriptionType): Set<PremiumFeature> {
        return PremiumFeature.getFeaturesForType(type)
    }
}

/**
 * Android-specific factory with context
 */
fun createBillingManager(context: Context, phoneDataLayerSync: PhoneDataLayerSync? = null): BillingManager {
    return AndroidBillingManager(context, phoneDataLayerSync)
}
