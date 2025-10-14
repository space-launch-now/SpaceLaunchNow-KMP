package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionState
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * DataStore storage for subscription state
 * 
 * ⚠️ CRITICAL SECURITY WARNING ⚠️
 * This storage is for UX CACHING ONLY - NEVER for access control!
 * 
 * Purpose:
 * - Show premium UI immediately without waiting for network
 * - Provide offline UX hints
 * - Remember last verified state
 * 
 * ALWAYS verify with BillingClient before granting actual access!
 */
class SubscriptionStorage(private val dataStore: DataStore<Preferences>) {

    companion object {
        // Subscription status
        private val IS_SUBSCRIBED = booleanPreferencesKey("is_subscribed")
        private val SUBSCRIPTION_TYPE = stringPreferencesKey("subscription_type")
        
        // Subscription details
        private val SUBSCRIPTION_ID = stringPreferencesKey("subscription_id")
        private val PRODUCT_ID = stringPreferencesKey("product_id")
        private val EXPIRES_AT = longPreferencesKey("expires_at")
        private val PURCHASED_AT = longPreferencesKey("purchased_at")
        
        // Verification state
        private val LAST_VERIFIED = longPreferencesKey("last_verified")
        private val NEEDS_VERIFICATION = booleanPreferencesKey("needs_verification")
        private val VERIFICATION_ERROR = stringPreferencesKey("verification_error")
        
        // Features (stored as comma-separated string)
        private val FEATURES = stringSetPreferencesKey("features")
    }

    /**
     * Flow of cached subscription state
     * Subscribe to this for reactive UI updates
     */
    val stateFlow: Flow<SubscriptionState> = dataStore.data.map { preferences ->
        val subscriptionTypeString = preferences[SUBSCRIPTION_TYPE] ?: SubscriptionType.FREE.name
        val subscriptionType = try {
            SubscriptionType.valueOf(subscriptionTypeString)
        } catch (e: IllegalArgumentException) {
            SubscriptionType.FREE
        }
        
        val featuresSet = preferences[FEATURES] ?: emptySet()
        val features = featuresSet.mapNotNull { featureName ->
            try {
                PremiumFeature.valueOf(featureName)
            } catch (e: IllegalArgumentException) {
                null
            }
        }.toSet()

        SubscriptionState(
            isSubscribed = preferences[IS_SUBSCRIBED] ?: false,
            subscriptionType = subscriptionType,
            subscriptionId = preferences[SUBSCRIPTION_ID],
            productId = preferences[PRODUCT_ID],
            expiresAt = preferences[EXPIRES_AT],
            purchasedAt = preferences[PURCHASED_AT],
            lastVerified = preferences[LAST_VERIFIED] ?: 0L,
            needsVerification = preferences[NEEDS_VERIFICATION] ?: false,
            verificationError = preferences[VERIFICATION_ERROR],
            features = features,
            isCached = true // Mark as cached to indicate this is not authoritative
        )
    }

    /**
     * Save subscription state to cache
     * Call this after successful verification with BillingClient
     */
    suspend fun saveState(state: SubscriptionState) {
        dataStore.edit { preferences ->
            preferences[IS_SUBSCRIBED] = state.isSubscribed
            preferences[SUBSCRIPTION_TYPE] = state.subscriptionType.name
            
            state.subscriptionId?.let { preferences[SUBSCRIPTION_ID] = it }
            state.productId?.let { preferences[PRODUCT_ID] = it }
            state.expiresAt?.let { preferences[EXPIRES_AT] = it }
            state.purchasedAt?.let { preferences[PURCHASED_AT] = it }
            
            preferences[LAST_VERIFIED] = state.lastVerified
            preferences[NEEDS_VERIFICATION] = state.needsVerification
            
            state.verificationError?.let { 
                preferences[VERIFICATION_ERROR] = it 
            } ?: preferences.remove(VERIFICATION_ERROR)
            
            preferences[FEATURES] = state.features.map { it.name }.toSet()
        }
    }

    /**
     * Get cached subscription state (synchronous)
     */
    suspend fun getState(): SubscriptionState {
        return stateFlow.first()
    }

    /**
     * Clear all cached subscription data
     * Use when user logs out or subscription is cancelled
     */
    suspend fun clearState() {
        dataStore.edit { preferences ->
            preferences[IS_SUBSCRIBED] = false
            preferences[SUBSCRIPTION_TYPE] = SubscriptionType.FREE.name
            preferences.remove(SUBSCRIPTION_ID)
            preferences.remove(PRODUCT_ID)
            preferences.remove(EXPIRES_AT)
            preferences.remove(PURCHASED_AT)
            preferences[LAST_VERIFIED] = 0L
            preferences[NEEDS_VERIFICATION] = false
            preferences.remove(VERIFICATION_ERROR)
            preferences[FEATURES] = emptySet()
        }
    }
    
    /**
     * Mark state as needing verification
     * Use when subscription might have changed (e.g., app resumed from background)
     */
    suspend fun markNeedsVerification() {
        dataStore.edit { preferences ->
            preferences[NEEDS_VERIFICATION] = true
        }
    }
}
