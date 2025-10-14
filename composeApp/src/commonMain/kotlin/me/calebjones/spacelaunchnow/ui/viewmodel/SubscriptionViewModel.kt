package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import com.revenuecat.purchases.kmp.models.Offering
import com.revenuecat.purchases.kmp.models.Package

/**
 * ViewModel for subscription management
 */
class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
    private val revenueCatManager: RevenueCatManager
) : ViewModel() {

    // Subscription state from repository
    val subscriptionState: StateFlow<SubscriptionState> = repository.state

    // UI-specific state
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()
    
    // RevenueCat offerings - expose the manager's state directly
    val currentOffering: StateFlow<Offering?> = revenueCatManager.currentOffering

    init {
        // Initialize repository on creation
        viewModelScope.launch {
            repository.initialize()
            // Load pricing information (legacy)
            loadPricing()
        }
    }

    /**
     * Load product pricing from platform
     */
    private fun loadPricing() {
        viewModelScope.launch {
            // Load subscription pricing
            repository.getProductPricing(me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts.PRODUCT_ID)
                .fold(
                    onSuccess = { pricingList ->
                        val monthlyPricing = pricingList.find {
                            it.basePlanId == me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts.BASE_PLAN_MONTHLY
                        }
                        val yearlyPricing = pricingList.find {
                            it.basePlanId == me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts.BASE_PLAN_YEARLY
                        }

                        _uiState.value = _uiState.value.copy(
                            monthlyPricing = monthlyPricing,
                            yearlyPricing = yearlyPricing
                        )
                    },
                    onFailure = { error ->
                        println("SubscriptionViewModel: Failed to load subscription pricing - ${error.message}")
                    }
                )

            // Load lifetime (Pro) pricing
            repository.getProductPricing(me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts.PRO_LIFETIME)
                .fold(
                    onSuccess = { pricingList ->
                        val lifetimePricing = pricingList.firstOrNull()

                        _uiState.value = _uiState.value.copy(
                            lifetimePricing = lifetimePricing
                        )
                    },
                    onFailure = { error ->
                        println("SubscriptionViewModel: Failed to load lifetime pricing - ${error.message}")
                    }
                )
        }
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
     * Purchase a subscription
     *
     * @param productId The product ID to purchase
     * @param basePlanId The base plan (e.g., "base-plan" for monthly, "yearly" for yearly)
     */
    fun purchaseSubscription(productId: String, basePlanId: String? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)

            repository.launchPurchaseFlow(productId, basePlanId).fold(
                onSuccess = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Purchase initiated successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = error.message ?: "Purchase failed"
                    )
                }
            )
        }
    }
    
    /**
     * Purchase a RevenueCat package
     *
     * @param packageToPurchase The RevenueCat package to purchase
     */
    fun purchasePackage(packageToPurchase: Package) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, errorMessage = null)
            
            println("SubscriptionViewModel: Initiating purchase for package: ${packageToPurchase.identifier}")
            
            // Use the package's product ID and identifier for the purchase
            val productId = packageToPurchase.storeProduct.id
            val basePlanId = packageToPurchase.identifier
            
            repository.launchPurchaseFlow(productId, basePlanId).fold(
                onSuccess = { _ ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        successMessage = "Purchase completed successfully!"
                    )
                    println("SubscriptionViewModel: ✅ Purchase successful for ${packageToPurchase.identifier}")
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        errorMessage = error.message ?: "Purchase failed"
                    )
                    println("SubscriptionViewModel: ❌ Purchase failed: ${error.message}")
                }
            )
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
 * UI state for subscription screen
 */
data class SubscriptionUiState(
    val isProcessing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val monthlyPricing: me.calebjones.spacelaunchnow.data.model.ProductPricing? = null,
    val yearlyPricing: me.calebjones.spacelaunchnow.data.model.ProductPricing? = null,
    val lifetimePricing: me.calebjones.spacelaunchnow.data.model.ProductPricing? = null
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
            return if (savings > 0) "Save $savings%!" else ""
        }

        return "Save 58%!"  // Fallback
    }
}
