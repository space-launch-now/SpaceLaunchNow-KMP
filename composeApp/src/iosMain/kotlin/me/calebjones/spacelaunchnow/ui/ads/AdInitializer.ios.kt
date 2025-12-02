package me.calebjones.spacelaunchnow.ui.ads

import app.lexilabs.basic.ads.BasicAds
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.RequestConfiguration
import me.calebjones.spacelaunchnow.logger

/**
 * iOS implementation of AdInitializer using BasicAds library
 */
@OptIn(DependsOnGoogleMobileAds::class)
actual object AdInitializer {
    private val log = logger()
    actual val isSupported: Boolean = true
    
    private var isInitialized = false
    
    actual fun initialize(context: Any?): Boolean {
        if (isInitialized) {
            log.w { "⚠️ AdInitializer (iOS): Already initialized" }
            return true
        }
        
        return try {
            log.d { "🎯 AdInitializer (iOS): Initializing BasicAds (no context required)" }
            BasicAds.initialize(null) // iOS doesn't require context
            isInitialized = true
            log.i { "✅ AdInitializer (iOS): BasicAds initialized successfully" }
            true
        } catch (e: Exception) {
            log.e(e) { "❌ AdInitializer (iOS): Failed to initialize BasicAds: ${e.message}" }
            false
        }
    }
    
    actual fun configure(isDebug: Boolean, testDeviceIds: List<String>) {
        try {
            BasicAds.configuration = RequestConfiguration(
                maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_T,
                publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.ENABLED,
                tagForChildDirectedTreatment = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE,
                tagForUnderAgeOfConsent = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE,
                testDeviceIds = testDeviceIds
            )
            log.i { "✅ AdInitializer (iOS): Configuration applied (Debug: $isDebug, Test devices: ${testDeviceIds.size})" }
        } catch (e: Exception) {
            log.e(e) { "❌ AdInitializer (iOS): Failed to configure: ${e.message}" }
        }
    }
}
