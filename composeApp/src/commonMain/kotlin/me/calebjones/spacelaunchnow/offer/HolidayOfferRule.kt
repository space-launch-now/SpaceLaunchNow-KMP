package me.calebjones.spacelaunchnow.offer

import me.calebjones.spacelaunchnow.data.model.SubscriptionType

/**
 * Pure rule for the holiday offer sheet. No I/O: the caller reads the config, preferences,
 * subscription state, store products and the clock, and passes them in.
 */
object HolidayOfferRule {

    data class Input(
        val config: HolidayOfferConfig,
        /** The user's current subscription type. Only [SubscriptionType.FREE] may see the offer. */
        val subscriptionType: SubscriptionType,
        /** True when the monthly product has an eligible monthly intro price for one or more months ([me.calebjones.spacelaunchnow.data.model.ProductInfo.isMonthlyIntroOffer]). */
        val monthlyIntroEligible: Boolean,
        /** Id of the last campaign whose sheet was shown, or null if none. */
        val shownCampaignId: String?,
        val nowMs: Long,
    )

    enum class Decision(val reason: String) {
        SHOW("all conditions met"),
        BLOCKED_NOT_FREE("user is not on the free tier"),
        BLOCKED_NOT_ELIGIBLE("the store offers no first-month intro price to this user"),
        BLOCKED_FLAG_OFF("remote config flag is off"),
        BLOCKED_NO_CAMPAIGN_ID("campaign id is blank"),
        BLOCKED_NO_START("campaign start is unset or invalid"),
        BLOCKED_NO_END("campaign end is unset or invalid"),
        BLOCKED_BEFORE_START("campaign has not started"),
        BLOCKED_AFTER_END("campaign has ended"),
        BLOCKED_ALREADY_SHOWN("the sheet was already shown for this campaign"),
    }

    fun evaluate(input: Input): Decision {
        val config = input.config
        val startMs = config.startMs
        val endMs = config.endMs
        return when {
            input.subscriptionType != SubscriptionType.FREE -> Decision.BLOCKED_NOT_FREE
            !input.monthlyIntroEligible -> Decision.BLOCKED_NOT_ELIGIBLE
            !config.enabled -> Decision.BLOCKED_FLAG_OFF
            config.campaignId.isBlank() -> Decision.BLOCKED_NO_CAMPAIGN_ID
            startMs == null -> Decision.BLOCKED_NO_START
            endMs == null -> Decision.BLOCKED_NO_END
            input.nowMs < startMs -> Decision.BLOCKED_BEFORE_START
            input.nowMs >= endMs -> Decision.BLOCKED_AFTER_END
            input.shownCampaignId == config.campaignId -> Decision.BLOCKED_ALREADY_SHOWN
            else -> Decision.SHOW
        }
    }
}
