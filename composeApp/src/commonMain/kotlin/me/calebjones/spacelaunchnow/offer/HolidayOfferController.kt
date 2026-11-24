package me.calebjones.spacelaunchnow.offer

import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Decides whether the holiday offer sheet may open and records that it did, per campaign.
 * The host calls [evaluate] once per app open and [markShown] with the campaign id when
 * the sheet appears. A new campaign id makes the sheet eligible again. The evaluation
 * carries the config, for the campaign id and the close delay.
 */
class HolidayOfferController(
    private val readConfig: suspend () -> HolidayOfferConfig,
    private val shownCampaignId: suspend () -> String?,
    private val saveShown: suspend (campaignId: String) -> Unit,
    private val nowMs: () -> Long,
) {
    /** The decision and the config it used, so the host reads Remote Config once per check. */
    data class Evaluation(val decision: HolidayOfferRule.Decision, val config: HolidayOfferConfig) {
        val eligible: Boolean get() = decision == HolidayOfferRule.Decision.SHOW
    }

    /**
     * [subscriptionType] and [monthlyProduct] are read by the caller at the moment of the
     * check. Pass the monthly [ProductInfo] (null when products have not loaded).
     */
    suspend fun evaluate(subscriptionType: SubscriptionType, monthlyProduct: ProductInfo?): Evaluation {
        val config = readConfig()
        val decision = HolidayOfferRule.evaluate(
            HolidayOfferRule.Input(
                config = config,
                subscriptionType = subscriptionType,
                monthlyIntroEligible = monthlyProduct?.isFirstMonthIntroOffer == true,
                shownCampaignId = shownCampaignId(),
                nowMs = nowMs(),
            )
        )
        return Evaluation(decision, config)
    }

    suspend fun markShown(campaignId: String) = saveShown(campaignId)
}
