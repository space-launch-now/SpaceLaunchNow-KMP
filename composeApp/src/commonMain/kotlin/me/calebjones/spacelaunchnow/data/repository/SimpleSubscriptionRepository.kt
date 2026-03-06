package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import me.calebjones.spacelaunchnow.data.billing.BillingClient
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.data.subscription.LocalSubscriptionStorage
import me.calebjones.spacelaunchnow.data.subscription.SubscriptionSyncer
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.widgets.PlatformWidgetUpdater
import me.calebjones.spacelaunchnow.widgets.WidgetAccessCache
import me.calebjones.spacelaunchnow.widgets.WidgetAccessSharer

/**
 * Simple, reliable subscription repository
 * - Local storage is the source of truth
 * - RevenueCat syncs in background
 * - UI gets immediate, consistent data
 */
class SimpleSubscriptionRepository(
    private val localStorage: LocalSubscriptionStorage,
    private val syncer: SubscriptionSyncer,
    private val billingClient: BillingClient,
    private val widgetPreferences: WidgetPreferences,
    private val platformWidgetUpdater: PlatformWidgetUpdater? = null,
    private val temporaryPremiumAccess: TemporaryPremiumAccess
) : SubscriptionRepository {
    private val log = logger()

    // Scope for StateFlow conversion
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * UI observes this for subscription state
     * Maps local storage data to SubscriptionState for compatibility
     * Also updates widget access cache when subscription state changes
     */
    override val state: StateFlow<SubscriptionState> =
        localStorage.subscriptionData.onEach { local ->
            // Update widget access cache whenever subscription data changes
            repositoryScope.launch {
                try {
                    val hasWidgetAccess =
                        local.availableFeatures.contains(PremiumFeature.ADVANCED_WIDGETS)
                    log.d { "Updating widget access to: $hasWidgetAccess" }

                    // Wait for DataStore write to complete before triggering widget update
                    widgetPreferences.updateWidgetAccessGranted(hasWidgetAccess)

                    // Build enhanced cache and sync to shared UserDefaults for iOS widget extension.
                    // wasEverPremium is sticky — persist true if the stored value is already true.
                    val cache = WidgetAccessCache(
                        hasAccess = hasWidgetAccess,
                        subscriptionExpiryMs = local.subscriptionExpiryMs,
                        lastVerifiedMs = local.lastSynced,
                        wasEverPremium = local.wasEverPremium || hasWidgetAccess,
                        subscriptionType = local.subscriptionType
                    )
                    WidgetAccessSharer.syncWidgetAccessCache(cache)

                    // Trigger widget updates after DataStore write is confirmed complete
                    updateWidgetsAfterAccessChange(if (hasWidgetAccess) "access granted" else "access revoked")
                } catch (e: Exception) {
                    log.e(e) { "❌ Failed to update widget access: ${e.message}" }
                }
            }
        }.map { local ->
            SubscriptionState(
                isSubscribed = local.isSubscribed,
                subscriptionType = local.subscriptionType,
                productId = local.productIds.firstOrNull(), // Extract primary product ID
                features = local.availableFeatures,
                lastVerified = local.lastSynced,
                needsVerification = local.needsSync,
                isLoading = false
            )
        }.stateIn(
            scope = repositoryScope,
            started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
            initialValue = run {
                // Get actual initial value from local storage to avoid flashing ads for premium users
                val initialData = runBlocking { localStorage.get() }
                SubscriptionState(
                    isSubscribed = initialData.isSubscribed,
                    subscriptionType = initialData.subscriptionType,
                    productId = initialData.productIds.firstOrNull(),
                    features = initialData.availableFeatures,
                    lastVerified = initialData.lastSynced,
                    needsVerification = initialData.needsSync,
                    isLoading = false
                )
            }
        )

    /**
     * Initialize the repository
     * - Start background syncing
     * - Trigger initial sync if needed
     */
    override suspend fun initialize() {
        log.d { "SimpleSubscriptionRepository: Initializing..." }

        // Initialize billing client
        billingClient.initialize()

        // Start background syncing with RevenueCat
        syncer.startSyncing()

        log.i { "SimpleSubscriptionRepository: ✅ Initialized" }
    }

    /**
     * Check if user has access to a premium feature
     * Checks both subscription access AND temporary access from rewarded ads
     * This is synchronous and uses local storage for immediate response
     */
    override suspend fun hasFeature(feature: PremiumFeature): Boolean {
        // First check subscription access
        val hasSubscriptionFeature = localStorage.hasFeature(feature)

        // Then check temporary access from rewarded ads
        val hasTemporaryAccess = temporaryPremiumAccess.hasTemporaryAccess(feature)

        val hasFeature = hasSubscriptionFeature || hasTemporaryAccess

        log.d { "SimpleSubscriptionRepository: hasFeature(${feature.name}) = $hasFeature (subscription: $hasSubscriptionFeature, temporary: $hasTemporaryAccess)" }
        return hasFeature
    }

    /**
     * Verify subscription by syncing with RevenueCat
     * This refreshes the local data from RevenueCat
     */
    override suspend fun verifySubscription(forceRefresh: Boolean): Result<SubscriptionState> {
        return try {
            log.d { "SimpleSubscriptionRepository: Verifying subscription (forceRefresh: $forceRefresh)" }

            val syncSuccess = syncer.syncNow()

            if (syncSuccess) {
                val current = localStorage.get()
                val state = SubscriptionState(
                    isSubscribed = current.isSubscribed,
                    subscriptionType = current.subscriptionType,
                    features = current.availableFeatures,
                    lastVerified = current.lastSynced,
                    needsVerification = false,
                    isLoading = false
                )
                log.i { "✅ Verification complete: $state" }
                Result.success(state)
            } else {
                log.w { "❌ Verification failed" }
                Result.failure(Exception("Failed to sync with RevenueCat"))
            }

        } catch (e: Exception) {
            log.e(e) { "SimpleSubscriptionRepository: ❌ Verification error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Launch purchase flow for a product
     */
    override suspend fun launchPurchaseFlow(
        productId: String,
        basePlanId: String?
    ): Result<String> {
        log.d { "SimpleSubscriptionRepository: Launching purchase for $productId" }

        return billingClient.launchPurchaseFlow(productId, basePlanId).also { result ->
            // If purchase was successful, trigger a sync to update local data
            if (result.isSuccess) {
                log.i { "Purchase successful, syncing..." }
                syncer.syncNow()
            }
        }
    }

    /**
     * Restore purchases from the app store
     */
    override suspend fun restorePurchases(): Result<SubscriptionState> {
        return try {
            log.d { "SimpleSubscriptionRepository: Restoring purchases..." }

            // Trigger sync which will restore purchases from RevenueCat
            val syncSuccess = syncer.syncNow()

            if (syncSuccess) {
                val current = localStorage.get()
                val state = SubscriptionState(
                    isSubscribed = current.isSubscribed,
                    subscriptionType = current.subscriptionType,
                    features = current.availableFeatures,
                    lastVerified = current.lastSynced,
                    needsVerification = false,
                    isLoading = false
                )
                log.i { "✅ Restore complete: $state" }
                Result.success(state)
            } else {
                log.w { "❌ Restore failed" }
                Result.failure(Exception("Failed to restore purchases"))
            }

        } catch (e: Exception) {
            log.e(e) { "SimpleSubscriptionRepository: ❌ Restore error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Get product pricing (delegates to billing client)
     */
    override suspend fun getProductPricing(productId: String): Result<List<me.calebjones.spacelaunchnow.data.model.ProductPricing>> {
        return billingClient.getProductPricing(productId)
    }

    /**
     * Get all features available to current subscription
     */
    override suspend fun getAvailableFeatures(): Set<PremiumFeature> {
        val localData = localStorage.get()
        log.d { "SimpleSubscriptionRepository: getAvailableFeatures() = ${localData.availableFeatures}" }
        return localData.availableFeatures
    }

    /**
     * Cancel subscription (platform-specific)
     * Note: On mobile, this typically opens the platform's subscription management
     */
    override suspend fun cancelSubscription(): Result<Unit> {
        return try {
            log.d { "SimpleSubscriptionRepository: Canceling subscription..." }
            billingClient.cancelSubscription()
        } catch (e: Exception) {
            log.e(e) { "SimpleSubscriptionRepository: ❌ Cancel error: ${e.message}" }
            Result.failure(e)
        }
    }

    /**
     * Clear cached subscription data
     * Use when user logs out
     */
    override suspend fun clearSubscriptionCache() {
        log.d { "SimpleSubscriptionRepository: Clearing subscription cache" }
        localStorage.clear()
    }

    /**
     * Force refresh widget access for debugging
     * Call this method to manually check and update widget access
     */
    override suspend fun forceRefreshWidgetAccess(): Boolean {
        log.d { "SimpleSubscriptionRepository: Force refreshing widget access" }
        return try {
            val syncSuccess = syncer.syncNow()
            if (syncSuccess) {
                val localData = localStorage.get()
                val hasWidgetAccess =
                    localData.availableFeatures.contains(PremiumFeature.ADVANCED_WIDGETS)
                log.d { "Widget access: $hasWidgetAccess" }

                // Wait for DataStore write to complete before triggering widget update
                widgetPreferences.updateWidgetAccessGranted(hasWidgetAccess)

                // Sync enhanced cache to shared UserDefaults for iOS widget extension
                val localData2 = localStorage.get()
                val cache = WidgetAccessCache(
                    hasAccess = hasWidgetAccess,
                    subscriptionExpiryMs = localData2.subscriptionExpiryMs,
                    lastVerifiedMs = localData2.lastSynced,
                    wasEverPremium = localData2.wasEverPremium || hasWidgetAccess,
                    subscriptionType = localData2.subscriptionType
                )
                WidgetAccessSharer.syncWidgetAccessCache(cache)
                log.i { "✅ Updated widget preferences cache" }

                // Trigger widget update after DataStore write is confirmed complete
                updateWidgetsAfterAccessChange("force refresh - ${if (hasWidgetAccess) "access granted" else "access revoked"}")

                hasWidgetAccess
            } else {
                log.w { "❌ Failed to refresh widget access" }
                false
            }
        } catch (e: Exception) {
            log.e(e) { "SimpleSubscriptionRepository: ❌ Widget access refresh error: ${e.message}" }
            false
        }
    }

    /**
     * Debug: Clear all subscription data
     */
    suspend fun clearSubscriptionData() {
        log.d { "SimpleSubscriptionRepository: Clearing all subscription data" }
        localStorage.clear()
    }

    /**
     * Debug: Get current local data
     */
    suspend fun getLocalData() = localStorage.get()

    /**
     * Debug: Check if currently in debug/simulation mode
     */
    suspend fun isInDebugMode() = localStorage.isInDebugMode()

    // Debug methods for testing subscription states

    /**
     * Debug: Set subscription state for testing
     */
    suspend fun setDebugSubscription(
        subscriptionType: SubscriptionType,
        productId: String = "",
        entitlements: Set<String> = emptySet()
    ) {
        log.d { "SimpleSubscriptionRepository: Setting debug subscription: $subscriptionType" }
        localStorage.setDebugSubscription(subscriptionType, productId, entitlements)
    }

    /**
     * Debug: Clear debug state and return to real subscription state
     */
    suspend fun clearDebugState() {
        log.d { "SimpleSubscriptionRepository: Clearing debug state" }
        localStorage.clearDebugState()
        // Trigger a sync to get real state
        syncer.syncNow()
    }

    /**
     * Triggers widget updates after widget access changes (premium granted/revoked).
     * Called after DataStore write is confirmed complete, so no delay needed.
     */
    private fun updateWidgetsAfterAccessChange(reason: String) {
        if (platformWidgetUpdater == null) {
            log.d { "SimpleSubscriptionRepository: PlatformWidgetUpdater not available, skipping widget update for: $reason" }
            return
        }

        repositoryScope.launch {
            log.d { "SimpleSubscriptionRepository: Triggering widget update after $reason" }
            try {
                platformWidgetUpdater.updateAllWidgets()
                log.i { "Widget update completed for: $reason" }
            } catch (e: Exception) {
                log.e(e) { "ERROR updating widgets after $reason: ${e.message}" }
            }
        }
    }

    /**
     * Grant temporary access to a premium feature through rewarded ads
     * This provides 24-hour access to the specified feature
     */
    suspend fun grantTemporaryAccess(feature: PremiumFeature) {
        log.i { "SimpleSubscriptionRepository: Granting temporary access to ${feature.name}" }
        temporaryPremiumAccess.grantTemporaryAccess(feature)

        // Update widgets if this affects widget features
        if (feature == PremiumFeature.ADVANCED_WIDGETS || feature == PremiumFeature.WIDGETS_CUSTOMIZATION) {
            // Sync to shared UserDefaults so iOS widget picks up temporary access on next refresh
            val localData = localStorage.get()
            val cache = WidgetAccessCache(
                hasAccess = true,
                subscriptionExpiryMs = localData.subscriptionExpiryMs,
                lastVerifiedMs = kotlin.time.Clock.System.now().toEpochMilliseconds(),
                wasEverPremium = localData.wasEverPremium,
                subscriptionType = localData.subscriptionType
            )
            WidgetAccessSharer.syncWidgetAccessCache(cache)
            updateWidgetsAfterAccessChange("temporary access granted for ${feature.name}")
        }
    }

    /**
     * Check if user has temporary access to a specific feature
     */
    suspend fun hasTemporaryAccess(feature: PremiumFeature): Boolean {
        return temporaryPremiumAccess.hasTemporaryAccess(feature)
    }
}