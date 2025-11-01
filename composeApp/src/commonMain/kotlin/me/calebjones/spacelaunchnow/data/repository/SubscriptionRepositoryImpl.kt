package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Clock.System
import me.calebjones.spacelaunchnow.analytics.DatadogLogger
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.billing.RevenueCatManager
import me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts
import me.calebjones.spacelaunchnow.data.model.PlatformPurchase
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.data.storage.SubscriptionStorage
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.storage.TemporaryAccessStatus
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.widgets.PlatformWidgetUpdater
import kotlin.time.Duration.Companion.days
import kotlinx.coroutines.flow.first

/**
 * Implementation of SubscriptionRepository
 *
 * Architecture:
 * 1. Load cached state from DataStore (instant UI)
 * 2. Check RevenueCat entitlements for feature access
 * 3. Update cache when purchases complete
 * 4. Fall back to cached state for offline scenarios
 *
 * Phase 6: Simplified implementation using RevenueCat entitlements only
 */
class SubscriptionRepositoryImpl(
    private val billingClient: BillingClient,
    private val storage: SubscriptionStorage,
    private val debugPreferences: DebugPreferences,
    private val appPreferences: AppPreferences,
    private val temporaryPremiumAccess: TemporaryPremiumAccess,
    private val revenueCatManager: RevenueCatManager,  // RevenueCat for entitlements
    private val widgetPreferences: WidgetPreferences,  // Cache widget access for widgets
    private val platformWidgetUpdater: PlatformWidgetUpdater? = null  // Optional: for triggering widget updates
) : SubscriptionRepository {

    // Repository scope for background work
    private val repositoryScope = CoroutineScope(SupervisorJob())

    // Single source of truth - all UI observes this
    private val _state = MutableStateFlow(SubscriptionState.DEFAULT)
    override val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    // Debug simulation state
    private var debugSimulationState: SubscriptionState? = null

    /**
     * Check if debug features are enabled (debug build OR debug menu unlocked in production)
     */
    private suspend fun isDebugEnabled(): Boolean {
        return BuildConfig.IS_DEBUG || appPreferences.isDebugMenuUnlocked()
    }

    override suspend fun initialize() {
        println("=== SubscriptionRepository: Initializing ===")

        try {
            // 1. Load cached state for instant UI
            val cachedState = storage.getState()
            _state.value = cachedState.copy(isLoading = false)
            println("SubscriptionRepository: Loaded cached state - isSubscribed: ${cachedState.isSubscribed}")

            // 2. Load debug simulation state if debug features are enabled
            if (isDebugEnabled()) {
                loadDebugSimulationState()
            }

            // 3. Initialize billing client
            billingClient.initialize().onFailure { error ->
                println("SubscriptionRepository: Failed to initialize billing - ${error.message}")
                _state.value =
                    SubscriptionState.error("Failed to initialize billing: ${error.message}")
                return
            }

            // 4. Verify subscription in background (only if not using debug simulation)
            if (debugSimulationState == null) {
                repositoryScope.launch {
                    verifySubscription(forceRefresh = false)
                }
            }

            // 5. Listen for purchase updates
            repositoryScope.launch {
                billingClient.purchaseUpdates.collect { purchase ->
                    println("SubscriptionRepository: Purchase update received - ${purchase.productId}")
                    handlePurchaseUpdate(purchase)
                }
            }

        } catch (e: Exception) {
            println("SubscriptionRepository: Initialization error - ${e.message}")
            _state.value = SubscriptionState.error("Initialization failed: ${e.message}")
        }
    }

    /**
     * Load debug simulation state from persistent storage
     */
    private suspend fun loadDebugSimulationState() {
        if (!isDebugEnabled()) return

        val debugSettings = debugPreferences.getDebugSettings()
        if (debugSettings.debugSubscriptionActive) {
            val subscriptionType = debugSettings.debugSubscriptionType?.let {
                try {
                    SubscriptionType.valueOf(it)
                } catch (e: Exception) {
                    SubscriptionType.FREE
                }
            } ?: SubscriptionType.FREE

            val productId = debugSettings.debugSubscriptionProductId

            println("SubscriptionRepository: Loading persisted debug simulation - type: $subscriptionType, productId: $productId")

            simulateSubscriptionState(
                isSubscribed = subscriptionType != SubscriptionType.FREE,
                subscriptionType = subscriptionType,
                productId = productId,
                persist = false // Don't save again since we're loading
            )
        }
    }

    override suspend fun verifySubscription(forceRefresh: Boolean): Result<SubscriptionState> {
        // If we have a debug simulation active and not forcing refresh, use it
        if (isDebugEnabled() && debugSimulationState != null && !forceRefresh) {
            println("SubscriptionRepository: Using persisted debug simulation state")
            return Result.success(debugSimulationState!!)
        }

        // Otherwise, use normal verification logic
        return super_verifySubscription(forceRefresh)
    }

    private suspend fun super_verifySubscription(forceRefresh: Boolean): Result<SubscriptionState> {
        val currentState = _state.value

        // Skip if recently verified and not forcing refresh
        if (!forceRefresh && currentState.isRecentlyVerified() && !currentState.needsVerification) {
            println("SubscriptionRepository: Using recently verified state")
            DatadogLogger.debug("Using cached subscription state", mapOf(
                "subscription_type" to currentState.subscriptionType.name,
                "is_subscribed" to currentState.isSubscribed
            ))
            return Result.success(currentState)
        }

        println("SubscriptionRepository: Verifying subscription with platform...")
        DatadogLogger.info("Starting subscription verification", mapOf(
            "force_refresh" to forceRefresh,
            "current_subscription_type" to currentState.subscriptionType.name,
            "needs_verification" to currentState.needsVerification
        ))
        
        _state.value = currentState.copy(isLoading = true)

        return try {
            // Query platform billing (Google Play / App Store)
            val purchasesResult = billingClient.queryPurchases()

            purchasesResult.fold(
                onSuccess = { purchases ->
                    DatadogLogger.info("Platform billing query successful", mapOf(
                        "purchases_count" to purchases.size,
                        "purchase_tokens" to purchases.map { it.purchaseToken }.joinToString(",")
                    ))
                    
                    var newState = processVerifiedPurchases(purchases)

                    // If no purchases found via billing client, check RevenueCat for legacy purchases
                    if (!newState.isSubscribed) {
                        println("SubscriptionRepository: No purchases from billing client, checking RevenueCat for legacy purchases...")
                        DatadogLogger.info("No active purchases from billing client, checking RevenueCat for legacy purchases")
                        
                        val legacyState = checkForLegacyPurchasesInRevenueCat()
                        if (legacyState != null) {
                            println("SubscriptionRepository: Found legacy purchase in RevenueCat")
                            DatadogLogger.info("Legacy purchase found in RevenueCat", mapOf(
                                "subscription_type" to legacyState.subscriptionType.name,
                                "features" to legacyState.features.joinToString(",") { it.name }
                            ))
                            newState = legacyState
                        } else {
                            DatadogLogger.info("No legacy purchases found in RevenueCat")
                        }
                    }

                    // Update state and cache
                    _state.value = newState
                    storage.saveState(newState)

                    println("SubscriptionRepository: Verification complete - isSubscribed: ${newState.isSubscribed}")
                    DatadogLogger.info("Subscription verification complete", mapOf(
                        "is_subscribed" to newState.isSubscribed,
                        "subscription_type" to newState.subscriptionType.name,
                        "features" to newState.features.joinToString(",") { it.name },
                        "has_premium" to newState.hasFeature(PremiumFeature.AD_FREE)
                    ))
                    
                    Result.success(newState)
                },
                onFailure = { error ->
                    println("SubscriptionRepository: Verification failed - ${error.message}")
                    DatadogLogger.error("Platform billing query failed", error, mapOf(
                        "error_message" to (error.message ?: "unknown")
                    ))

                    // Check RevenueCat for legacy purchases as fallback
                    DatadogLogger.info("Attempting RevenueCat fallback after billing error")
                    val legacyState = checkForLegacyPurchasesInRevenueCat()
                    if (legacyState != null) {
                        println("SubscriptionRepository: Found legacy purchase in RevenueCat despite billing error")
                        DatadogLogger.info("Legacy purchase found despite billing error", mapOf(
                            "subscription_type" to legacyState.subscriptionType.name
                        ))
                        _state.value = legacyState
                        storage.saveState(legacyState)
                        return Result.success(legacyState)
                    }

                    DatadogLogger.warn("No fallback purchases found, using cached state with error")
                    
                    // Use cached state but mark as needing verification
                    val errorState = currentState.copy(
                        isLoading = false,
                        needsVerification = true,
                        verificationError = error.message
                    )
                    _state.value = errorState

                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            println("SubscriptionRepository: Verification exception - ${e.message}")
            val errorState = SubscriptionState.error(e.message ?: "Unknown error")
            _state.value = errorState
            Result.failure(e)
        }
    }

    override suspend fun launchPurchaseFlow(
        productId: String,
        basePlanId: String?
    ): Result<String> {
        println("SubscriptionRepository: Launching purchase flow for $productId (basePlan: $basePlanId)")

        _state.value = _state.value.copy(isLoading = true)

        return try {
            val result = billingClient.launchPurchaseFlow(productId, basePlanId)

            result.fold(
                onSuccess = { purchaseToken ->
                    println("SubscriptionRepository: Purchase successful - $purchaseToken")

                    // Verify subscription to update state
                    verifySubscription(forceRefresh = true)

                    Result.success(purchaseToken)
                },
                onFailure = { error ->
                    println("SubscriptionRepository: Purchase failed - ${error.message}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        verificationError = "Purchase failed: ${error.message}"
                    )
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            println("SubscriptionRepository: Purchase exception - ${e.message}")
            _state.value = _state.value.copy(isLoading = false)
            Result.failure(e)
        }
    }

    override suspend fun restorePurchases(): Result<SubscriptionState> {
        println("SubscriptionRepository: Restoring purchases...")
        DatadogLogger.info("Restore purchases flow started in SubscriptionRepository", mapOf(
            "current_state" to _state.value.subscriptionType.name,
            "is_subscribed" to _state.value.isSubscribed
        ))
        
        try {
            // First, call RevenueCat's restore which syncs all platform purchases to RevenueCat
            println("SubscriptionRepository: Calling RevenueCat.restorePurchases() to sync platform purchases...")
            DatadogLogger.info("Calling RevenueCat.restorePurchases() to sync with platform store")
            
            revenueCatManager.restorePurchases()
            
            // Wait a moment for RevenueCat to sync
            println("SubscriptionRepository: Waiting for RevenueCat sync to complete...")
            delay(1000)
            
            // Then verify to get the updated state
            println("SubscriptionRepository: Verifying subscription after restore...")
            DatadogLogger.info("Verifying subscription after restore")
            
            val result = verifySubscription(forceRefresh = true)
            
            result.fold(
                onSuccess = { state ->
                    DatadogLogger.info("Restore purchases completed successfully", mapOf(
                        "new_state" to state.subscriptionType.name,
                        "is_subscribed" to state.isSubscribed,
                        "has_features" to state.features.isNotEmpty(),
                        "features" to state.features.joinToString(",") { it.name },
                        "verification_source" to (state.verificationError ?: "none")
                    ))
                },
                onFailure = { error ->
                    DatadogLogger.error("Restore purchases failed during verification", error, mapOf(
                        "error_message" to (error.message ?: "unknown")
                    ))
                }
            )
            
            return result
            
        } catch (e: Exception) {
            println("SubscriptionRepository: Exception during restore - ${e.message}")
            e.printStackTrace()
            
            DatadogLogger.error("Exception during restore purchases flow", e, mapOf(
                "error_message" to (e.message ?: "unknown"),
                "error_type" to e::class.simpleName
            ))
            
            return Result.failure(e)
        }
    }

    override suspend fun getProductPricing(productId: String): Result<List<me.calebjones.spacelaunchnow.data.model.ProductPricing>> {
        println("SubscriptionRepository: Getting pricing for $productId")
        return billingClient.getProductPricing(productId)
    }

    override suspend fun hasFeature(feature: PremiumFeature): Boolean {
        println("=== SubscriptionRepository.hasFeature(${feature.name}) ===")
        println("  Debug build: ${BuildConfig.IS_DEBUG}")
        println("  Debug simulation state: ${debugSimulationState?.subscriptionType}")

        // Priority 1: Temporary premium access from rewarded ads (ALWAYS CHECK FIRST!)
        // This allows users to get temporary access even in debug/free mode
        val hasTemporaryAccess = temporaryPremiumAccess.hasTemporaryAccess(feature)
        if (hasTemporaryAccess) {
            println("  🎁 Temporary access active for ${feature.name}")
            
            // Update widget preferences to match temporary access
            if (feature == PremiumFeature.ADVANCED_WIDGETS) {
                widgetPreferences.updateWidgetAccessGranted(true)
                updateWidgetsAfterAccessChange("temporary access granted")
            }
            
            return true
        }

        // Priority 2: Debug simulation state (debug builds only)
        if (debugSimulationState != null) {
            val debugAccess = debugSimulationState!!.hasFeature(feature)
            println("  ✅ Debug simulation active - ${feature.name}: $debugAccess")

            // Update widget preferences to match debug simulation
            if (feature == PremiumFeature.ADVANCED_WIDGETS) {
                widgetPreferences.updateWidgetAccessGranted(debugAccess)
                updateWidgetsAfterAccessChange(if (debugAccess) "debug access granted" else "debug access revoked")
            }

            return debugAccess
        }

        // Priority 3: RevenueCat entitlements (live verification)
        val hasPremiumEntitlement =
            revenueCatManager.hasEntitlement(SubscriptionProducts.RC_ENTITLEMENT_PREMIUM)
        println("  RevenueCat premium entitlement: $hasPremiumEntitlement")

        if (hasPremiumEntitlement) {
            println("  ✅ User has premium entitlement - granting ${feature.name}")

            // Cache widget access for widgets (they can't call RevenueCat)
            if (feature == PremiumFeature.ADVANCED_WIDGETS) {
                widgetPreferences.updateWidgetAccessGranted(true)
                // Trigger widget update after granting access
                updateWidgetsAfterAccessChange("access granted")
            }

            return true
        }

        // Priority 3.5: Check for legacy purchases without entitlements
        // This handles purchases made before RevenueCat or not configured in dashboard
        val activeProductIds = revenueCatManager.getActiveProductIdentifiers()
        println("  RevenueCat active products: $activeProductIds")
        
        if (activeProductIds.isNotEmpty()) {
            // Check if any of the active products grant the requested feature
            for (productId in activeProductIds) {
                val productFeatures = SubscriptionProducts.getFeaturesForProduct(productId)
                if (productFeatures.contains(feature)) {
                    println("  ✅ Legacy/active purchase '$productId' grants ${feature.name}")
                    
                    // Cache widget access for widgets
                    if (feature == PremiumFeature.ADVANCED_WIDGETS) {
                        widgetPreferences.updateWidgetAccessGranted(true)
                        updateWidgetsAfterAccessChange("legacy access granted")
                    }
                    
                    // Update cached state with legacy purchase
                    updateCachedStateFromLegacyPurchase(productId, productFeatures)
                    
                    return true
                }
            }
            println("  ⚠️ Active products found but none grant ${feature.name}")
        }

        // No entitlement or legacy purchase - revoke widget access
        if (feature == PremiumFeature.ADVANCED_WIDGETS) {
            widgetPreferences.updateWidgetAccessGranted(false)
            // Trigger widget update after revoking access
            updateWidgetsAfterAccessChange("access revoked")
        }

        // Priority 4: Fallback to cached state for offline scenarios
        val cachedAccess = _state.value.hasFeature(feature)
        println("  ⚠️ No premium entitlement - cached access for ${feature.name}: $cachedAccess")
        println("  Current state: ${_state.value.subscriptionType} with features: ${_state.value.features}")
        return cachedAccess
    }

    override suspend fun getAvailableFeatures(): Set<PremiumFeature> {
        return _state.value.features
    }

    override suspend fun cancelSubscription(): Result<Unit> {
        // Platform-specific cancellation handled by platform implementations
        // On mobile, this typically opens the subscription management page
        println("SubscriptionRepository: Cancellation requested - redirecting to platform")
        return Result.success(Unit)
    }

    override suspend fun clearSubscriptionCache() {
        println("SubscriptionRepository: Clearing subscription cache")
        storage.clearState()
        _state.value = SubscriptionState.free()
    }

    /**
     * Process purchases from platform and create verified state
     */
    private fun processVerifiedPurchases(purchases: List<PlatformPurchase>): SubscriptionState {
        if (purchases.isEmpty()) {
            println("SubscriptionRepository: No active purchases found")
            return SubscriptionState.free().copy(
                lastVerified = System.now().toEpochMilliseconds(),
                needsVerification = false,
                isLoading = false
            )
        }

        // Find the most premium active purchase
        val activePurchase = purchases
            .filter { !it.isExpired() }
            .maxByOrNull { it.subscriptionType.ordinal }

        if (activePurchase == null) {
            println("SubscriptionRepository: All purchases are expired")
            return SubscriptionState.free().copy(
                lastVerified = System.now().toEpochMilliseconds(),
                needsVerification = false,
                isLoading = false
            )
        }

        println("SubscriptionRepository: Active purchase found - ${activePurchase.productId}")

        val subscriptionType = SubscriptionProducts.getSubscriptionType(activePurchase.productId)
        val features = SubscriptionProducts.getFeaturesForProduct(activePurchase.productId)

        return SubscriptionState(
            isSubscribed = true,
            subscriptionType = subscriptionType,
            subscriptionId = activePurchase.orderId,
            productId = activePurchase.productId,
            expiresAt = activePurchase.expiryTime,
            purchasedAt = activePurchase.purchaseTime,
            lastVerified = System.now().toEpochMilliseconds(),
            needsVerification = false,
            verificationError = null,
            features = features,
            isLoading = false,
            isCached = false
        )
    }

    /**
     * Handle real-time purchase updates from billing client
     */
    private suspend fun handlePurchaseUpdate(purchase: PlatformPurchase) {
        println("SubscriptionRepository: Handling purchase update - ${purchase.productId}")

        // Acknowledge purchase if not already acknowledged (required for Google Play)
        if (!purchase.isAcknowledged) {
            billingClient.acknowledgePurchase(purchase.purchaseToken)
        }

        // Re-verify to update state
        verifySubscription(forceRefresh = true)
    }

    /**
     * Debug method to simulate different subscription states
     * Only available in debug builds or when debug menu is unlocked
     */
    fun simulateSubscriptionState(
        isSubscribed: Boolean,
        subscriptionType: SubscriptionType,
        productId: String?,
        persist: Boolean = true
    ) {
        repositoryScope.launch {
            if (!isDebugEnabled()) return@launch

            debugSimulationState = if (isSubscribed) {
                SubscriptionState(
                    isSubscribed = true,
                    subscriptionType = subscriptionType,
                    subscriptionId = "debug_order_${System.now().toEpochMilliseconds()}",
                    productId = productId ?: "debug_product",
                    expiresAt = System.now().plus(30.days).toEpochMilliseconds(),
                    purchasedAt = System.now().minus(7.days).toEpochMilliseconds(),
                    lastVerified = System.now().toEpochMilliseconds(),
                    needsVerification = false,
                    verificationError = null,
                    features = if (productId != null) {
                        SubscriptionProducts.getFeaturesForProduct(productId)
                    } else {
                        PremiumFeature.getFeaturesForType(subscriptionType)
                    },
                    isLoading = false,
                    isCached = false
                )
            } else {
                SubscriptionState.free()
            }

            _state.value = debugSimulationState!!

            // Save to persistent storage if requested
            if (persist) {
                debugPreferences.setDebugSubscriptionSimulation(
                    isActive = true,
                    subscriptionType = subscriptionType.name,
                    productId = productId
                )
            }

            println("SubscriptionRepository: Debug simulation set to ${debugSimulationState!!.subscriptionType} (${debugSimulationState!!.productId})")
        }
    }

    /**
     * Debug method to simulate needs verification state
     */
    fun simulateNeedsVerification(needsVerification: Boolean) {
        repositoryScope.launch {
            if (!isDebugEnabled()) return@launch

            val currentState = _state.value
            debugSimulationState = currentState.copy(
                needsVerification = needsVerification,
                verificationError = if (needsVerification) "Debug: Verification required" else null
            )
            _state.value = debugSimulationState!!
            println("SubscriptionRepository: Debug simulation - needsVerification = $needsVerification")
        }
    }

    /**
     * Debug method to simulate expired subscription
     */
    fun simulateExpiredSubscription() {
        repositoryScope.launch {
            if (!isDebugEnabled()) return@launch

            debugSimulationState = SubscriptionState(
                isSubscribed = false,
                subscriptionType = SubscriptionType.FREE,
                subscriptionId = "debug_expired_${System.now().toEpochMilliseconds()}",
                productId = "expired_premium",
                expiresAt = System.now().minus(7.days)
                    .toEpochMilliseconds(), // Expired 7 days ago
                purchasedAt = System.now().minus(365.days).toEpochMilliseconds(),
                lastVerified = System.now().toEpochMilliseconds(),
                needsVerification = false,
                verificationError = "Subscription expired",
                features = emptySet(),
                isLoading = false,
                isCached = false
            )
            _state.value = debugSimulationState!!

            // Save to persistent storage
            debugPreferences.setDebugSubscriptionSimulation(
                isActive = true,
                subscriptionType = SubscriptionType.FREE.name,
                productId = "expired_premium"
            )

            println("SubscriptionRepository: Debug simulation - expired subscription")
        }
    }

    /**
     * Clear debug simulation and return to real billing state
     */
    fun clearDebugSimulation() {
        repositoryScope.launch {
            if (!isDebugEnabled()) return@launch

            debugSimulationState = null

            // Clear persistent storage
            debugPreferences.clearDebugSubscriptionSimulation()

            // Trigger real verification
            verifySubscription(forceRefresh = true)
            
            println("SubscriptionRepository: Debug simulation cleared")
        }
    }

    /**
     * Triggers widget updates after widget access changes (premium granted/revoked).
     * Uses a longer delay to ensure DataStore writes complete before widget refresh.
     */
    private fun updateWidgetsAfterAccessChange(reason: String) {
        if (platformWidgetUpdater == null) {
            println("SubscriptionRepository: PlatformWidgetUpdater not available, skipping widget update for: $reason")
            return
        }

        repositoryScope.launch {
            println("SubscriptionRepository: Widget $reason - scheduling widget update in 750ms")
            try {
                // Longer delay for access changes to ensure DataStore write completes
                delay(750)
                println("SubscriptionRepository: Triggering widget update after $reason")
                platformWidgetUpdater.updateAllWidgets()
                println("SubscriptionRepository: Widget update completed for: $reason")
            } catch (e: Exception) {
                println("SubscriptionRepository: ERROR updating widgets after $reason: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Grant temporary premium access to a feature through rewarded ads
     * This provides 24-hour access to premium features
     */
    suspend fun grantTemporaryPremiumAccess(feature: PremiumFeature) {
        println("SubscriptionRepository: Granting temporary access to ${feature.name}")
        
        // Grant the temporary access
        temporaryPremiumAccess.grantTemporaryAccess(feature)
        
        // Update widget access if needed
        if (feature == PremiumFeature.ADVANCED_WIDGETS) {
            widgetPreferences.updateWidgetAccessGranted(true)
            updateWidgetsAfterAccessChange("temporary access granted via rewarded ad")
        }
        
        println("SubscriptionRepository: Temporary access granted to ${feature.name} for 24 hours")
    }

    /**
     * Check if user has temporary access to a feature and get remaining time
     */
    suspend fun getTemporaryAccessStatus(feature: PremiumFeature): TemporaryAccessStatus {
        val hasAccess = temporaryPremiumAccess.hasTemporaryAccess(feature)
        val timeRemaining = if (hasAccess) {
            temporaryPremiumAccess.getTimeRemaining(feature)
        } else {
            null
        }
        
        return TemporaryAccessStatus(
            hasAccess = hasAccess,
            expiresAt = null, // Could be added if needed
            timeRemaining = timeRemaining
        )
    }

    /**
     * Update cached subscription state from a legacy purchase
     * This is called when we detect an active purchase in RevenueCat that doesn't have
     * an entitlement configured, but we still want to grant access based on product ID
     */
    private suspend fun updateCachedStateFromLegacyPurchase(
        productId: String,
        features: Set<PremiumFeature>
    ) {
        val subscriptionType = SubscriptionProducts.getSubscriptionType(productId)
        
        // Only update if the current state is different to avoid infinite loops
        val currentState = _state.value
        if (currentState.productId == productId && 
            currentState.subscriptionType == subscriptionType &&
            currentState.isSubscribed) {
            // State already matches, no update needed
            return
        }
        
        val newState = SubscriptionState(
            isSubscribed = true,
            subscriptionType = subscriptionType,
            subscriptionId = null, // RevenueCat handles subscription IDs
            productId = productId,
            expiresAt = null, // Legacy purchases are typically lifetime
            purchasedAt = null, // Don't have this info from RevenueCat
            lastVerified = System.now().toEpochMilliseconds(),
            needsVerification = false,
            verificationError = null,
            features = features,
            isLoading = false,
            isCached = false
        )
        
        println("SubscriptionRepository: Updating cached state from legacy purchase: $productId -> $subscriptionType")
        _state.value = newState
        storage.saveState(newState)
    }

    /**
     * Check RevenueCat for any active purchases (including legacy products without entitlements)
     * Returns a SubscriptionState if any active purchase is found, null otherwise
     */
    private fun checkForLegacyPurchasesInRevenueCat(): SubscriptionState? {
        val activeProductIds = revenueCatManager.getActiveProductIdentifiers()
        if (activeProductIds.isEmpty()) {
            println("SubscriptionRepository: No active products in RevenueCat")
            return null
        }

        println("SubscriptionRepository: Found ${activeProductIds.size} active products in RevenueCat: $activeProductIds")

        // Find the most premium product
        var bestProductId: String? = null
        var bestSubscriptionType = SubscriptionType.FREE
        var bestFeatures: Set<PremiumFeature> = emptySet()

        for (productId in activeProductIds) {
            val subscriptionType = SubscriptionProducts.getSubscriptionType(productId)
            val features = SubscriptionProducts.getFeaturesForProduct(productId)

            // Prefer PREMIUM over LEGACY over FREE
            if (subscriptionType.ordinal > bestSubscriptionType.ordinal) {
                bestProductId = productId
                bestSubscriptionType = subscriptionType
                bestFeatures = features
            }
        }

        if (bestProductId == null || bestSubscriptionType == SubscriptionType.FREE) {
            println("SubscriptionRepository: No premium/legacy products found")
            return null
        }

        println("SubscriptionRepository: Best product: $bestProductId with type $bestSubscriptionType")

        return SubscriptionState(
            isSubscribed = true,
            subscriptionType = bestSubscriptionType,
            subscriptionId = null,
            productId = bestProductId,
            expiresAt = null, // Legacy purchases are typically lifetime
            purchasedAt = null,
            lastVerified = System.now().toEpochMilliseconds(),
            needsVerification = false,
            verificationError = null,
            features = bestFeatures,
            isLoading = false,
            isCached = false
        )
    }
}

/**
 * Extension: Check if platform purchase is expired
 */
private fun PlatformPurchase.isExpired(): Boolean {
    val expiryTime = this.expiryTime ?: return false
    return System.now().toEpochMilliseconds() > expiryTime
}

/**
 * Extension: Get subscription type from purchase
 */
private val PlatformPurchase.subscriptionType: SubscriptionType
    get() = SubscriptionProducts.getSubscriptionType(this.productId)
