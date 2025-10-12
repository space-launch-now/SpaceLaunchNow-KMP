# Billing Client Usage Examples

This document provides code examples for common billing operations in Space Launch Now.

## Table of Contents
- [Initialization](#initialization)
- [Querying Active Purchases](#querying-active-purchases)
- [Launching Purchase Flow](#launching-purchase-flow)
- [Getting Product Pricing](#getting-product-pricing)
- [Listening for Purchase Updates](#listening-for-purchase-updates)
- [Platform-Specific Considerations](#platform-specific-considerations)

## Initialization

The billing client must be initialized before any operations:

```kotlin
// In your repository or ViewModel
class SubscriptionRepository(
    private val billingClient: BillingClient
) {
    suspend fun initialize(): Result<Unit> {
        return billingClient.initialize()
    }
}

// Usage
viewModelScope.launch {
    subscriptionRepository.initialize()
        .onSuccess { 
            println("Billing initialized successfully") 
            // Query current purchases
            checkSubscriptionStatus()
        }
        .onFailure { error ->
            println("Billing initialization failed: ${error.message}")
        }
}
```

## Querying Active Purchases

Check what the user currently owns:

```kotlin
suspend fun checkSubscriptionStatus(): SubscriptionType {
    return billingClient.queryPurchases()
        .map { purchases ->
            if (purchases.isEmpty()) {
                SubscriptionType.FREE
            } else {
                // Get highest subscription tier from active purchases
                purchases.maxOfOrNull { purchase ->
                    SubscriptionProducts.getSubscriptionType(purchase.productId)
                } ?: SubscriptionType.FREE
            }
        }
        .getOrElse { error ->
            println("Failed to query purchases: ${error.message}")
            SubscriptionType.FREE
        }
}

// Usage in ViewModel
viewModelScope.launch {
    val currentTier = checkSubscriptionStatus()
    _subscriptionState.value = currentTier
    
    when (currentTier) {
        SubscriptionType.FREE -> println("User has no subscription")
        SubscriptionType.BASIC -> println("User has legacy subscription (ad-free only)")
        SubscriptionType.PREMIUM -> println("User has full premium access")
    }
}
```

## Launching Purchase Flow

### Monthly Subscription

```kotlin
// Android/iOS agnostic - works on both platforms
suspend fun purchaseMonthly() {
    billingClient.launchPurchaseFlow(
        productId = SubscriptionProducts.PRODUCT_ID,
        basePlanId = SubscriptionProducts.BASE_PLAN_MONTHLY
    )
        .onSuccess { purchaseToken ->
            println("Purchase successful! Token: $purchaseToken")
            // Refresh subscription status
            checkSubscriptionStatus()
        }
        .onFailure { error ->
            when {
                error.message?.contains("User cancelled") == true -> {
                    println("User cancelled purchase")
                }
                else -> {
                    println("Purchase failed: ${error.message}")
                    showError("Unable to complete purchase. Please try again.")
                }
            }
        }
}
```

### Yearly Subscription

```kotlin
suspend fun purchaseYearly() {
    billingClient.launchPurchaseFlow(
        productId = SubscriptionProducts.PRODUCT_ID,
        basePlanId = SubscriptionProducts.BASE_PLAN_YEARLY
    )
        .onSuccess { purchaseToken ->
            println("Yearly subscription purchased!")
            checkSubscriptionStatus()
        }
        .onFailure { error ->
            println("Purchase failed: ${error.message}")
        }
}
```

### Lifetime Purchase

```kotlin
suspend fun purchaseLifetime() {
    billingClient.launchPurchaseFlow(
        productId = SubscriptionProducts.PRO_LIFETIME,
        basePlanId = null  // No base plan for one-time purchases
    )
        .onSuccess { purchaseToken ->
            println("Lifetime purchase successful!")
            checkSubscriptionStatus()
        }
        .onFailure { error ->
            println("Purchase failed: ${error.message}")
        }
}
```

## Getting Product Pricing

Display prices in the UI:

```kotlin
suspend fun loadPricingInfo(): List<ProductPricing> {
    return billingClient.getProductPricing(SubscriptionProducts.PRODUCT_ID)
        .getOrElse { error ->
            println("Failed to load pricing: ${error.message}")
            emptyList()
        }
}

// Usage in ViewModel
data class PricingUIState(
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
    val yearlySavings: Int = 0,
    val lifetimePrice: String = ""
)

fun loadPricing() {
    viewModelScope.launch {
        // Load subscription pricing
        val subscriptionPricing = billingClient.getProductPricing(SubscriptionProducts.PRODUCT_ID)
            .getOrElse { emptyList() }
        
        // Load lifetime pricing
        val lifetimePricing = billingClient.getProductPricing(SubscriptionProducts.PRO_LIFETIME)
            .getOrElse { emptyList() }
        
        // Find monthly and yearly plans
        val monthly = subscriptionPricing.find { 
            it.basePlanId == SubscriptionProducts.BASE_PLAN_MONTHLY 
        }
        val yearly = subscriptionPricing.find { 
            it.basePlanId == SubscriptionProducts.BASE_PLAN_YEARLY 
        }
        val lifetime = lifetimePricing.firstOrNull()
        
        // Calculate savings
        val savings = if (monthly != null && yearly != null) {
            yearly.calculateSavingsPercent(monthly)
        } else 0
        
        _pricingState.value = PricingUIState(
            monthlyPrice = monthly?.formattedPrice ?: "",
            yearlyPrice = yearly?.formattedPrice ?: "",
            yearlySavings = savings,
            lifetimePrice = lifetime?.formattedPrice ?: ""
        )
    }
}

// UI Display
@Composable
fun SubscriptionOptions(state: PricingUIState) {
    Column {
        // Monthly
        SubscriptionCard(
            title = "Monthly",
            price = state.monthlyPrice,
            billingPeriod = "/month"
        )
        
        // Yearly (with savings badge)
        SubscriptionCard(
            title = "Yearly",
            price = state.yearlyPrice,
            billingPeriod = "/year",
            badge = if (state.yearlySavings > 0) {
                "Save ${state.yearlySavings}%"
            } else null
        )
        
        // Lifetime
        SubscriptionCard(
            title = "Lifetime",
            price = state.lifetimePrice,
            billingPeriod = "one-time"
        )
    }
}
```

## Listening for Purchase Updates

Monitor purchase updates in real-time:

```kotlin
class SubscriptionRepository(
    private val billingClient: BillingClient
) {
    private val _subscriptionState = MutableStateFlow(SubscriptionType.FREE)
    val subscriptionState: StateFlow<SubscriptionType> = _subscriptionState
    
    init {
        // Start listening for purchase updates
        viewModelScope.launch {
            billingClient.purchaseUpdates.collect { purchase ->
                println("Purchase update received: ${purchase.productId}")
                
                // Acknowledge purchase if needed (Android)
                if (!purchase.isAcknowledged) {
                    billingClient.acknowledgePurchase(purchase.purchaseToken)
                        .onSuccess { 
                            println("Purchase acknowledged") 
                        }
                }
                
                // Update subscription state
                val newType = SubscriptionProducts.getSubscriptionType(purchase.productId)
                _subscriptionState.value = newType
            }
        }
    }
}
```

## Platform-Specific Considerations

### Android-Specific

#### Setting Activity for Purchase Flow

On Android, you need to set the current activity:

```kotlin
// In your Activity
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get billing client from Koin
        val billingClient = get<BillingClient>() as? AndroidBillingClient
        billingClient?.setActivity(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        val billingClient = get<BillingClient>() as? AndroidBillingClient
        billingClient?.setActivity(null)
    }
}
```

#### Handling Pending Purchases

```kotlin
// Android may have pending purchases that need acknowledgement
suspend fun handlePendingPurchases() {
    billingClient.queryPurchases()
        .onSuccess { purchases ->
            purchases.filter { !it.isAcknowledged }.forEach { purchase ->
                billingClient.acknowledgePurchase(purchase.purchaseToken)
            }
        }
}
```

### iOS-Specific

#### Product ID Mapping

iOS uses different product IDs than Android:

```kotlin
// This is handled automatically by IosBillingClient
// Android: "sln_production_yearly" + "base-plan" → iOS: "sln_production_monthly"
// Android: "sln_production_yearly" + "yearly" → iOS: "sln_production_yearly"
```

#### Restore Purchases

iOS requires a "Restore Purchases" button:

```kotlin
// iOS automatically restores during queryPurchases()
suspend fun restorePurchases() {
    billingClient.queryPurchases()
        .onSuccess { purchases ->
            if (purchases.isEmpty()) {
                showMessage("No purchases to restore")
            } else {
                showMessage("${purchases.size} purchase(s) restored")
                checkSubscriptionStatus()
            }
        }
        .onFailure { error ->
            showError("Failed to restore purchases: ${error.message}")
        }
}

// UI Button (required for iOS App Review)
@Composable
fun SubscriptionScreen() {
    Column {
        // ... subscription options ...
        
        // iOS requires visible Restore Purchases button
        TextButton(onClick = { 
            viewModel.restorePurchases() 
        }) {
            Text("Restore Purchases")
        }
    }
}
```

## Complete ViewModel Example

```kotlin
class SubscriptionViewModel(
    private val billingClient: BillingClient
) : ViewModel() {
    
    private val _state = MutableStateFlow(SubscriptionUIState())
    val state: StateFlow<SubscriptionUIState> = _state
    
    init {
        initialize()
        observePurchaseUpdates()
    }
    
    private fun initialize() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // Initialize billing
            billingClient.initialize()
                .onSuccess {
                    // Load current status
                    checkSubscriptionStatus()
                    // Load pricing
                    loadPricing()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    private fun observePurchaseUpdates() {
        viewModelScope.launch {
            billingClient.purchaseUpdates.collect { purchase ->
                // Acknowledge if needed
                if (!purchase.isAcknowledged) {
                    billingClient.acknowledgePurchase(purchase.purchaseToken)
                }
                // Refresh status
                checkSubscriptionStatus()
            }
        }
    }
    
    private suspend fun checkSubscriptionStatus() {
        billingClient.queryPurchases()
            .onSuccess { purchases ->
                val tier = purchases.maxOfOrNull { purchase ->
                    SubscriptionProducts.getSubscriptionType(purchase.productId)
                } ?: SubscriptionType.FREE
                
                _state.update { 
                    it.copy(
                        currentTier = tier,
                        isLoading = false
                    ) 
                }
            }
    }
    
    private suspend fun loadPricing() {
        val pricing = billingClient.getProductPricing(SubscriptionProducts.PRODUCT_ID)
            .getOrElse { emptyList() }
        
        val monthly = pricing.find { it.basePlanId == SubscriptionProducts.BASE_PLAN_MONTHLY }
        val yearly = pricing.find { it.basePlanId == SubscriptionProducts.BASE_PLAN_YEARLY }
        
        _state.update { 
            it.copy(
                monthlyPrice = monthly?.formattedPrice ?: "",
                yearlyPrice = yearly?.formattedPrice ?: ""
            ) 
        }
    }
    
    fun purchaseMonthly() {
        viewModelScope.launch {
            _state.update { it.copy(isPurchasing = true) }
            
            billingClient.launchPurchaseFlow(
                productId = SubscriptionProducts.PRODUCT_ID,
                basePlanId = SubscriptionProducts.BASE_PLAN_MONTHLY
            )
                .onSuccess { 
                    checkSubscriptionStatus()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isPurchasing = false,
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    fun purchaseYearly() {
        viewModelScope.launch {
            _state.update { it.copy(isPurchasing = true) }
            
            billingClient.launchPurchaseFlow(
                productId = SubscriptionProducts.PRODUCT_ID,
                basePlanId = SubscriptionProducts.BASE_PLAN_YEARLY
            )
                .onSuccess { 
                    checkSubscriptionStatus()
                }
                .onFailure { error ->
                    _state.update { 
                        it.copy(
                            isPurchasing = false,
                            error = error.message
                        ) 
                    }
                }
        }
    }
    
    fun restorePurchases() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            checkSubscriptionStatus()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        billingClient.disconnect()
    }
}

data class SubscriptionUIState(
    val currentTier: SubscriptionType = SubscriptionType.FREE,
    val monthlyPrice: String = "",
    val yearlyPrice: String = "",
    val isLoading: Boolean = false,
    val isPurchasing: Boolean = false,
    val error: String? = null
)
```

## Error Handling Best Practices

```kotlin
fun handleBillingError(error: Throwable) {
    when {
        error.message?.contains("User cancelled") == true -> {
            // Don't show error, user intentionally cancelled
            println("Purchase cancelled by user")
        }
        
        error.message?.contains("already owns") == true -> {
            // User already subscribed
            showMessage("You already have an active subscription")
            checkSubscriptionStatus()
        }
        
        error.message?.contains("network") == true -> {
            // Network error
            showError("Network error. Please check your connection and try again.")
        }
        
        error.message?.contains("not ready") == true -> {
            // Billing not initialized
            showError("Billing service not ready. Please try again.")
            viewModelScope.launch { billingClient.initialize() }
        }
        
        else -> {
            // Generic error
            showError("Unable to complete purchase. Please try again later.")
            println("Billing error: ${error.message}")
        }
    }
}
```

## Testing Examples

```kotlin
class BillingClientTest {
    @Test
    fun `test purchase flow returns success`() = runTest {
        val billingClient = createBillingClient()
        billingClient.initialize()
        
        val result = billingClient.launchPurchaseFlow(
            productId = SubscriptionProducts.PRODUCT_ID,
            basePlanId = SubscriptionProducts.BASE_PLAN_MONTHLY
        )
        
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `test query purchases returns active subscriptions`() = runTest {
        val billingClient = createBillingClient()
        billingClient.initialize()
        
        val purchases = billingClient.queryPurchases().getOrThrow()
        
        assertTrue(purchases.isNotEmpty())
        assertEquals(SubscriptionProducts.PRODUCT_ID, purchases.first().productId)
    }
}
```

## See Also

- [IOS_BILLING_SETUP.md](./IOS_BILLING_SETUP.md) - Complete iOS setup guide
- [SUBSCRIPTION_SYSTEM.md](./SUBSCRIPTION_SYSTEM.md) - Overall subscription architecture
- [BillingClient.kt](../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/billing/BillingClient.kt) - Common interface
