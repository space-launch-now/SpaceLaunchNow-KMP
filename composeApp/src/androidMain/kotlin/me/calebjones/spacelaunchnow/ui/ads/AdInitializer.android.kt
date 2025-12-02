package me.calebjones.spacelaunchnow.ui.ads

import app.lexilabs.basic.ads.BasicAds
import app.lexilabs.basic.ads.DependsOnGoogleMobileAds
import app.lexilabs.basic.ads.RequestConfiguration
import me.calebjones.spacelaunchnow.util.logging.logger

/**
 * Android implementation of AdInitializer using BasicAds library
 */
actual object AdInitializer {
    private val log = logger()
    actual val isSupported: Boolean = true

    private var isInitialized = false

    actual fun initialize(context: Any?): Boolean {
        if (isInitialized) {
            log.w("Already initialized")
            return true
        }

        return try {
            log.d("Initializing BasicAds with context: $context")
            BasicAds.initialize(context)
            isInitialized = true
            log.d("BasicAds initialized successfully")
            true
        } catch (e: Exception) {
            log.e("Failed to initialize BasicAds: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    @OptIn(DependsOnGoogleMobileAds::class)
    actual fun configure(isDebug: Boolean, testDeviceIds: List<String>) {
        try {
            BasicAds.configuration = RequestConfiguration(
                maxAdContentRating = RequestConfiguration.MAX_AD_CONTENT_RATING_PG,
                publisherPrivacyPersonalizationState = RequestConfiguration.PublisherPrivacyPersonalizationState.ENABLED,
                tagForChildDirectedTreatment = RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_UNSPECIFIED,
                tagForUnderAgeOfConsent = RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_UNSPECIFIED,
                testDeviceIds = testDeviceIds
            )
            log.d("Configuration applied (Debug: $isDebug, Test devices: ${testDeviceIds.size})")
        } catch (e: Exception) {
            log.e("❌ Failed to configure: ${e.message}")
            e.printStackTrace()
        }
    }
}
