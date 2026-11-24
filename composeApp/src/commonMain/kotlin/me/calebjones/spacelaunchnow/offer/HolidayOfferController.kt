package me.calebjones.spacelaunchnow.offer

import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Decides whether the holiday offer sheet may open and records that it did, per campaign.
 * The host calls [isEligible] once per app open and [markShown] with the campaign id when
 * the sheet appears. A new campaign id makes the sheet eligible again. Read the close delay
 * with [closeDelaySeconds].
 */
class HolidayOfferController(
    private val readConfig: suspend () -> HolidayOfferConfig,
    private val shownCampaignId: suspend () -> String?,
    private val saveShown: suspend (campaignId: String) -> Unit,
    private val nowMs: () -> Long,
) {
    /**
     * [subscriptionType] and [monthlyProduct] are read by the caller at the moment of the
     * check. Pass the monthly [ProductInfo] (null when products have not loaded).
     */
    suspend fun isEligible(subscriptionType: SubscriptionType, monthlyProduct: ProductInfo?): Boolean =
        decide(subscriptionType, monthlyProduct) == HolidayOfferRule.Decision.SHOW

    suspend fun decide(subscriptionType: SubscriptionType, monthlyProduct: ProductInfo?): HolidayOfferRule.Decision =
        HolidayOfferRule.evaluate(
            HolidayOfferRule.Input(
                config = readConfig(),
                subscriptionType = subscriptionType,
                monthlyIntroEligible = monthlyProduct?.isFirstMonthIntroOffer == true,
                shownCampaignId = shownCampaignId(),
                nowMs = nowMs(),
            )
        )

    /** The current campaign config (id for [markShown], close delay for the sheet). */
    suspend fun config(): HolidayOfferConfig = readConfig()

    /** Seconds before the sheet's close button appears, 0..30. */
    suspend fun closeDelaySeconds(): Int = readConfig().closeDelaySeconds

    suspend fun markShown(campaignId: String) = saveShown(campaignId)
}
