package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.BillingManager
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.ProductPricing
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * ViewModel for subscription management
 *
 * Phase 7: Updated to use platform-agnostic BillingManager instead of RevenueCatManager
 */
class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val billingManager: BillingManager
) : ViewModel() {

    private val log = logger()

    // Subscription state from repository
    val subscriptionState: StateFlow<SubscriptionState> = repository.state

    // UI-specific state
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    // Available products - platform-agnostic list
    private val _availableProducts = MutableStateFlow<List<ProductInfo>>(emptyList())
    val availableProducts: StateFlow<List<ProductInfo>> = _availableProducts.asStateFlow()

    init {
        // Initialize repository on creation
        viewModelScope.launch {
            repository.initialize()
            // Load available products from BillingManager
            loadAvailableProducts()
        }
    }

    /**
     * Load available products from BillingManager and derive pricing
     */
    private fun loadAvailableProducts() {
        viewModelScope.launch {
            billingManager.getAvailableProducts().fold(
                onSuccess = { products ->
                    _availableProducts.value = products
                    log.i { "Loaded ${products.size} products from BillingManager" }

                    // Find products by RevenueCat package identifier
                    val monthlyProduct = products.find {
                        it.basePlanId?.contains("monthly", ignoreCase = true) == true
                    }
                    val yearlyProduct = products.find {
                        it.basePlanId?.contains("annual", ignoreCase = true) == true ||
                                it.basePlanId?.contains("yearly", ignoreCase = true) == true
                    }
                    val lifetimeProduct = products.find {
                        it.basePlanId?.contains("lifetime", ignoreCase = true) == true ||
                                it.productId.contains("lifetime", ignoreCase = true) ||
                                it.productId.contains("pro", ignoreCase = true)
                    }

                    // Derive pricing from loaded products
                    val monthlyPricing = monthlyProduct?.toProductPricing(billingPeriod = "P1M")
                    val yearlyPricing = yearlyProduct?.toProductPricing(billingPeriod = "P1Y")
                    val lifetimePricing = lifetimeProduct?.toProductPricing(billingPeriod = "LIFETIME")

                    // Compute trial offer state
                    val hasAnyTrial = products.any { it.hasFreeTrial }

                    _uiState.value = _uiState.value.copy(
                        monthlyPricing = monthlyPricing,
                        yearlyPricing = yearlyPricing,
                        lifetimePricing = lifetimePricing,
                        hasAnyTrialOffer = hasAnyTrial,
                        trialPeriodDisplay = yearlyProduct?.freeTrialPeriodDisplay,
                        trialPostPrice = if (yearlyProduct?.hasFreeTrial == true) {
                            "${yearlyProduct.formattedPrice}/year"
                        } else null
                    )

                    log.i { "Pricing derived - monthly: ${monthlyPricing?.formattedPrice}, yearly: ${yearlyPricing?.formattedPrice}, lifetime: ${lifetimePricing?.formattedPrice}" }
                },
                onFailure = { error ->
                    log.e(error) { "Failed to load products" }
                }
            )
        }
    }

    /**
     * Convert a ProductInfo to ProductPricing
     */
    private fun ProductInfo.toProductPricing(billingPeriod: String): ProductPricing {
        return ProductPricing(
            productId = productId,
            basePlanId = basePlanId ?: "",
            formattedPrice = formattedPrice,
            priceAmountMicros = priceAmountMicros,
            priceCurrencyCode = currencyCode,
            billingPeriod = billingPeriod,
            title = title,
            description = description
        )
    }



    /**
     * Verify subscription status with platform
     */
    fun verifySubscription(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            repository.verifySubscription(forceRefresh).fold(
                onSuccess = { state ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Subscription verified"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = error.message ?: "Verification failed"
                    )
                }
            )
        }
    }

    /**
     * Purchase a product by ID
     *
     * @param productId The product ID to purchase
     * @param basePlanId The base plan (e.g., "monthly", "annual", "lifetime")
     */
    fun purchaseProduct(productId: String, basePlanId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            billingManager.launchPurchaseFlow(productId, basePlanId).fold(
                onSuccess = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Purchase completed successfully!"
                    )
                    log.i { "Purchase successful for $productId" }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = error.message ?: "Purchase failed"
                    )
                    log.e(error) { "Purchase failed for $productId" }
                }
            )
        }
    }

    /**
     * Purchase a product (convenience method using ProductInfo)
     *
     * @param product The ProductInfo to purchase
     */
    fun purchaseProduct(product: ProductInfo) {
        purchaseProduct(product.productId, product.basePlanId)
    }

    /**
     * Legacy method - Purchase a subscription
     *
     * @param productId The product ID to purchase
     * @param basePlanId The base plan (e.g., "base-plan" for monthly, "yearly" for yearly)
     */
    @Deprecated(
        "Use purchaseProduct() instead",
        ReplaceWith("purchaseProduct(productId, basePlanId)")
    )
    fun purchaseSubscription(productId: String, basePlanId: String? = null) {
        purchaseProduct(productId, basePlanId)
    }

    /**
     * Helper method to find products by type
     */
    fun getProductByType(type: ProductType): ProductInfo? {
        return when (type) {
            ProductType.MONTHLY -> _availableProducts.value.find {
                it.basePlanId?.contains("monthly", ignoreCase = true) == true
            }

            ProductType.ANNUAL -> _availableProducts.value.find {
                it.basePlanId?.contains("annual", ignoreCase = true) == true ||
                        it.basePlanId?.contains("yearly", ignoreCase = true) == true
            }

            ProductType.LIFETIME -> _availableProducts.value.find {
                it.basePlanId?.contains("lifetime", ignoreCase = true) == true ||
                        it.productId.contains("lifetime", ignoreCase = true) ||
                        it.productId.contains("pro", ignoreCase = true)
            }
        }
    }

    /**
     * Restore previous purchases
     */
    fun restorePurchases() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            repository.restorePurchases().fold(
                onSuccess = { state ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = if (state.isSubscribed) {
                            "Subscription restored"
                        } else {
                            "No active subscriptions found"
                        }
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = error.message ?: "Restore failed"
                    )
                }
            )
        }
    }

    /**
     * Check if user has access to a feature
     *
     * Uses RevenueCat entitlements as authoritative source with cached state fallback.
     *
     * @param feature The feature to check
     */
    fun hasFeature(feature: PremiumFeature): Boolean {
        return subscriptionState.value.hasFeature(feature)
    }

    /**
     * Clear UI messages
     */
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }
}

/**
 * Product type categories for easier lookup
 */
enum class ProductType {
    MONTHLY,
    ANNUAL,
    LIFETIME
}

/**
 * UI state for subscription screen
 */
data class SubscriptionUiState(
    val isProcessing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val monthlyPricing: me.calebjones.spacelaunchnow.data.model.ProductPricing? = null,
    val yearlyPricing: me.calebjones.spacelaunchnow.data.model.ProductPricing? = null,
    val lifetimePricing: me.calebjones.spacelaunchnow.data.model.ProductPricing? = null,
    val hasAnyTrialOffer: Boolean = false,
    val trialPeriodDisplay: String? = null,
    val trialPostPrice: String? = null
) {
    /**
     * Get formatted monthly price or fallback
     */
    fun getMonthlyPrice(): String = monthlyPricing?.formattedPrice ?: "$2.99"

    /**
     * Get formatted yearly price or fallback
     */
    fun getYearlyPrice(): String = yearlyPricing?.formattedPrice ?: "$12.99"

    /**
     * Get formatted lifetime price or fallback
     */
    fun getLifetimePrice(): String = lifetimePricing?.formattedPrice ?: "$29.99"

    /**
     * Calculate savings percentage
     */
    fun getSavingsPercent(): String {
        val monthly = monthlyPricing
        val yearly = yearlyPricing

        if (monthly != null && yearly != null) {
            val savings = yearly.calculateSavingsPercent(monthly)
            return if (savings > 0) "Save $savings% via yearly plan!" else ""
        }

        return ""
    }
}
