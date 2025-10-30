package me.calebjones.spacelaunchnow.data.billing

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Tests for legacy purchase restoration functionality
 */
class LegacyPurchaseRestorationTest {

    @Test
    fun `legacy product IDs should be recognized as LEGACY subscription type`() {
        // Test that legacy product IDs are correctly categorized
        val legacyProducts = listOf(
            "2018_founder",
            "2019_supporter",
            "2020_premium",
            "old_product_123",
            "founder_edition"
        )

        legacyProducts.forEach { productId ->
            val subscriptionType = SubscriptionProducts.getSubscriptionType(productId)
            assertEquals(
                SubscriptionType.LEGACY,
                subscriptionType,
                "Product '$productId' should be recognized as LEGACY"
            )
        }
    }

    @Test
    fun `legacy products should grant basic features`() {
        // Test that legacy products grant the correct feature set
        val legacyProductId = "2018_founder"
        val features = SubscriptionProducts.getFeaturesForProduct(legacyProductId)

        // Legacy products should grant basic features (ad-free + widgets)
        assertTrue(
            features.contains(PremiumFeature.ADVANCED_WIDGETS),
            "Legacy product should grant ADVANCED_WIDGETS"
        )
        assertTrue(
            features.contains(PremiumFeature.WIDGETS_CUSTOMIZATION),
            "Legacy product should grant WIDGETS_CUSTOMIZATION"
        )
        assertTrue(
            features.contains(PremiumFeature.CAL_SYNC),
            "Legacy product should grant CAL_SYNC"
        )
        assertTrue(
            features.contains(PremiumFeature.NOTIFICATION_CUSTOMIZATION),
            "Legacy product should grant NOTIFICATION_CUSTOMIZATION"
        )
    }

    @Test
    fun `current products should grant all premium features`() {
        // Test that current products grant all features
        val currentProducts = listOf(
            SubscriptionProducts.PRO_LIFETIME,
            SubscriptionProducts.PRODUCT_ID
        )

        currentProducts.forEach { productId ->
            val features = SubscriptionProducts.getFeaturesForProduct(productId)
            val allFeatures = PremiumFeature.getPremiumFeatures()

            assertEquals(
                allFeatures,
                features,
                "Product '$productId' should grant all premium features"
            )
        }
    }

    @Test
    fun `legacy products should be identified correctly`() {
        // Test legacy product detection
        assertTrue(
            SubscriptionProducts.isLegacyProduct("2018_founder"),
            "2018_founder should be identified as legacy"
        )
        assertTrue(
            SubscriptionProducts.isLegacyProduct("old_sku_123"),
            "old_sku_123 should be identified as legacy"
        )
        assertTrue(
            !SubscriptionProducts.isLegacyProduct(SubscriptionProducts.PRO_LIFETIME),
            "PRO_LIFETIME should NOT be identified as legacy"
        )
        assertTrue(
            !SubscriptionProducts.isLegacyProduct(SubscriptionProducts.PRODUCT_ID),
            "PRODUCT_ID should NOT be identified as legacy"
        )
    }

    @Test
    fun `legacy products should grant ad-free access`() {
        // Test that legacy products grant ad-free access
        val legacyProductId = "2018_founder"
        
        assertTrue(
            SubscriptionProducts.grantsAdFreeAccess(legacyProductId),
            "Legacy product should grant ad-free access"
        )
        assertTrue(
            SubscriptionProducts.grantsPremiuAccess(legacyProductId),
            "Legacy product should be considered premium access"
        )
    }

    @Test
    fun `debug products should not grant features`() {
        // Test that debug/test products don't grant features
        val debugProductId = SubscriptionProducts.DEBUG_EXPIRED
        val features = SubscriptionProducts.getFeaturesForProduct(debugProductId)

        assertTrue(
            features.isEmpty(),
            "Debug product should not grant any features"
        )
        assertTrue(
            !SubscriptionProducts.grantsAdFreeAccess(debugProductId),
            "Debug product should not grant ad-free access"
        )
    }

    @Test
    fun `product display names should be appropriate`() {
        // Test display names for different product types
        assertEquals(
            "Legacy Premium",
            SubscriptionProducts.getProductDisplayName("2018_founder"),
            "Legacy products should have 'Legacy Premium' display name"
        )
        assertEquals(
            "Space Launch Now Pro (Lifetime)",
            SubscriptionProducts.getProductDisplayName(SubscriptionProducts.PRO_LIFETIME),
            "PRO_LIFETIME should have correct display name"
        )
        assertEquals(
            "Premium (Expired)",
            SubscriptionProducts.getProductDisplayName(SubscriptionProducts.DEBUG_EXPIRED),
            "DEBUG_EXPIRED should have correct display name"
        )
    }
}
