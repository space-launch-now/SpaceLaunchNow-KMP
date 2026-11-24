package me.calebjones.spacelaunchnow.offer

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HolidayOfferTest {

    private val start = 1_800_000_000_000L
    private val end = start + 14L * 24 * 60 * 60 * 1000
    private val now = start + 1000L

    private fun config(
        enabled: Boolean = true,
        campaignId: String = "holiday-2026",
        startMs: Long? = start,
        endMs: Long? = end,
    ) = HolidayOfferConfig(enabled, campaignId, startMs, endMs, closeDelaySeconds = 5)

    private fun input(
        config: HolidayOfferConfig = config(),
        type: SubscriptionType = SubscriptionType.FREE,
        eligible: Boolean = true,
        shown: String? = null,
        now: Long = this.now,
    ) = HolidayOfferRule.Input(config, type, eligible, shown, now)

    private fun decide(input: HolidayOfferRule.Input) = HolidayOfferRule.evaluate(input)

    // --- rule ---

    @Test
    fun `shows when every condition holds`() {
        assertEquals(HolidayOfferRule.Decision.SHOW, decide(input()))
    }

    @Test
    fun `blocked for every non free subscription type`() {
        listOf(SubscriptionType.LEGACY, SubscriptionType.PREMIUM, SubscriptionType.LIFETIME).forEach {
            assertEquals(HolidayOfferRule.Decision.BLOCKED_NOT_FREE, decide(input(type = it)), it.name)
        }
    }

    @Test
    fun `blocked when the store offers no intro price`() {
        assertEquals(HolidayOfferRule.Decision.BLOCKED_NOT_ELIGIBLE, decide(input(eligible = false)))
    }

    @Test
    fun `blocked when the flag is off`() {
        assertEquals(HolidayOfferRule.Decision.BLOCKED_FLAG_OFF, decide(input(config = config(enabled = false))))
    }

    @Test
    fun `blocked on a blank campaign id`() {
        assertEquals(HolidayOfferRule.Decision.BLOCKED_NO_CAMPAIGN_ID, decide(input(config = config(campaignId = ""))))
        assertEquals(HolidayOfferRule.Decision.BLOCKED_NO_CAMPAIGN_ID, decide(input(config = config(campaignId = "  "))))
    }

    @Test
    fun `blocked without a start or an end`() {
        assertEquals(HolidayOfferRule.Decision.BLOCKED_NO_START, decide(input(config = config(startMs = null))))
        assertEquals(HolidayOfferRule.Decision.BLOCKED_NO_END, decide(input(config = config(endMs = null))))
    }

    @Test
    fun `window is start inclusive and end exclusive`() {
        assertEquals(HolidayOfferRule.Decision.BLOCKED_BEFORE_START, decide(input(now = start - 1)))
        assertEquals(HolidayOfferRule.Decision.SHOW, decide(input(now = start)))
        assertEquals(HolidayOfferRule.Decision.SHOW, decide(input(now = end - 1)))
        assertEquals(HolidayOfferRule.Decision.BLOCKED_AFTER_END, decide(input(now = end)))
        assertEquals(HolidayOfferRule.Decision.BLOCKED_AFTER_END, decide(input(now = end + 1)))
    }

    @Test
    fun `blocked when this campaign was already shown`() {
        assertEquals(HolidayOfferRule.Decision.BLOCKED_ALREADY_SHOWN, decide(input(shown = "holiday-2026")))
    }

    @Test
    fun `a different campaign shown before does not block`() {
        assertEquals(HolidayOfferRule.Decision.SHOW, decide(input(shown = "holiday-2025")))
    }

    @Test
    fun `disabled default config never shows`() {
        assertEquals(
            HolidayOfferRule.Decision.BLOCKED_FLAG_OFF,
            decide(input(config = HolidayOfferConfig.DISABLED))
        )
    }

    // --- config parsing ---

    @Test
    fun `blank and garbage raw values give the safe defaults`() {
        assertEquals(HolidayOfferConfig.DISABLED, HolidayOfferConfig.fromRaw("", "", "", "", ""))
        assertEquals(
            HolidayOfferConfig.DISABLED,
            HolidayOfferConfig.fromRaw("maybe", "  ", "not-a-date", "2026-13-45", "abc")
        )
        assertEquals(HolidayOfferConfig.DISABLED, HolidayOfferConfig.fromRaw(null, null, null, null, null))
    }

    @Test
    fun `valid raw values parse`() {
        val parsed = HolidayOfferConfig.fromRaw(
            " TRUE ", " holiday-2026 ", "2026-12-01T00:00:00Z", "2026-12-31T23:59:59Z", "8"
        )
        assertTrue(parsed.enabled)
        assertEquals("holiday-2026", parsed.campaignId)
        assertEquals(1_796_083_200_000L, parsed.startMs)
        assertEquals(1_798_761_599_000L, parsed.endMs)
        assertEquals(8, parsed.closeDelaySeconds)
    }

    @Test
    fun `unparseable instants are null`() {
        assertNull(HolidayOfferConfig.parseInstantMs(""))
        assertNull(HolidayOfferConfig.parseInstantMs("tomorrow"))
        assertEquals(0L, HolidayOfferConfig.parseInstantMs("1970-01-01T00:00:00Z"))
    }

    @Test
    fun `close delay is clamped to 0 to 30 and bad values give 5`() {
        assertEquals(0, HolidayOfferConfig.parseCloseDelay("0"))
        assertEquals(0, HolidayOfferConfig.parseCloseDelay("-4"))
        assertEquals(30, HolidayOfferConfig.parseCloseDelay("30"))
        assertEquals(30, HolidayOfferConfig.parseCloseDelay("999"))
        assertEquals(12, HolidayOfferConfig.parseCloseDelay("12"))
        assertEquals(5, HolidayOfferConfig.parseCloseDelay("x"))
        assertEquals(5, HolidayOfferConfig.parseCloseDelay("2.5"))
        assertEquals(5, HolidayOfferConfig.parseCloseDelay(null))
    }

    // --- ProductInfo.isFirstMonthIntroOffer ---

    private fun product(
        hasIntro: Boolean = true,
        price: String? = "0.99 USD",
        value: Int? = 1,
        unit: String? = "MONTH",
        cycles: Int = 1,
    ) = ProductInfo(
        productId = "premium_monthly",
        basePlanId = "monthly",
        title = "Monthly",
        description = "",
        formattedPrice = "2.99 USD",
        priceAmountMicros = 2_990_000L,
        currencyCode = "USD",
        hasIntroOffer = hasIntro,
        introOfferPrice = price,
        introOfferPeriodValue = value,
        introOfferPeriodUnit = unit,
        introOfferCycles = cycles,
    )

    @Test
    fun `first month intro offer holds for one month one cycle`() {
        assertTrue(product().isFirstMonthIntroOffer)
    }

    @Test
    fun `first month intro offer fails when any part differs`() {
        assertFalse(product(hasIntro = false).isFirstMonthIntroOffer)
        assertFalse(product(price = null).isFirstMonthIntroOffer)
        assertFalse(product(value = 3).isFirstMonthIntroOffer)
        assertFalse(product(value = null).isFirstMonthIntroOffer)
        assertFalse(product(unit = "WEEK").isFirstMonthIntroOffer)
        assertFalse(product(unit = null).isFirstMonthIntroOffer)
        assertFalse(product(cycles = 3).isFirstMonthIntroOffer)
    }

    @Test
    fun `a default product has no first month intro offer`() {
        val bare = ProductInfo("p", null, "t", "d", "1 USD", 1_000_000L, "USD")
        assertFalse(bare.isFirstMonthIntroOffer)
        assertEquals(1, bare.introOfferCycles)
    }

    // --- controller ---

    private class Fixture(var now: Long) {
        val remote = MockRemoteConfigRepository()
        var shown: String? = null
        val controller = HolidayOfferController(
            readConfig = { remote.getHolidayOfferConfig() },
            shownCampaignId = { shown },
            saveShown = { shown = it },
            nowMs = { now },
        )
    }

    private val monthly = product()

    @Test
    fun `controller is off by default`() = runTest {
        val f = Fixture(now)
        assertFalse(f.controller.isEligible(SubscriptionType.FREE, monthly))
    }

    @Test
    fun `controller needs an eligible monthly product`() = runTest {
        val f = Fixture(now).apply { remote.holidayOfferConfig = config() }
        assertTrue(f.controller.isEligible(SubscriptionType.FREE, monthly))
        assertFalse(f.controller.isEligible(SubscriptionType.FREE, product(hasIntro = false)))
        assertFalse(f.controller.isEligible(SubscriptionType.FREE, null))
        assertFalse(f.controller.isEligible(SubscriptionType.PREMIUM, monthly))
    }

    @Test
    fun `markShown persists per campaign`() = runTest {
        val f = Fixture(now).apply { remote.holidayOfferConfig = config(campaignId = "a") }
        assertTrue(f.controller.isEligible(SubscriptionType.FREE, monthly))
        f.controller.markShown("a")
        assertEquals("a", f.shown)
        assertFalse(f.controller.isEligible(SubscriptionType.FREE, monthly))
        // A new campaign id shows again.
        f.remote.holidayOfferConfig = config(campaignId = "b")
        assertTrue(f.controller.isEligible(SubscriptionType.FREE, monthly))
        f.controller.markShown("b")
        assertEquals("b", f.shown)
        assertFalse(f.controller.isEligible(SubscriptionType.FREE, monthly))
    }

    @Test
    fun `controller exposes the close delay`() = runTest {
        val f = Fixture(now).apply { remote.holidayOfferConfig = config().copy(closeDelaySeconds = 9) }
        assertEquals(9, f.controller.closeDelaySeconds())
        assertEquals("holiday-2026", f.controller.config().campaignId)
    }
}
