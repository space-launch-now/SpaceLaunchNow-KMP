package me.calebjones.spacelaunchnow.ui.ads

import app.lexilabs.basic.ads.BasicAds
import app.lexilabs.basic.ads.RequestConfiguration

/**
 * Android implementation of AdInitializer using BasicAds library
 */
actual object AdInitializer {
    actual val isSupported: Boolean = true
    
    private var isInitialized = false
    
    actual fun initialize(context: Any?): Boolean {
        if (isInitialized) {
            println("⚠️ AdInitializer (Android): Already initialized")
            return true
        }
        
        return try {
            println("🎯 AdInitializer (Android): Initializing BasicAds with context: $context")
            BasicAds.initialize(context)
            isInitialized = true
            println("✅ AdInitializer (Android): BasicAds initialized successfully")
            true
        } catch (e: Exception) {
            println("❌ AdInitializer (Android): Failed to initialize BasicAds: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    actual fun configure(isDebug: Boolean, testDeviceIds: List<String>) {
        try {
            BasicAds.configuration = RequestConfiguration(
                maxAdContentRating = null,
                publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.DEFAULT,
                tagForChildDirectedTreatment = 0,
                tagForUnderAgeOfConsent = 0,
                testDeviceIds = testDeviceIds
            )
            println("✅ AdInitializer (Android): Configuration applied (Debug: $isDebug, Test devices: ${testDeviceIds.size})")
        } catch (e: Exception) {
            println("❌ AdInitializer (Android): Failed to configure: ${e.message}")
            e.printStackTrace()
        }
    }
}
