package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.offer.HolidayOfferSheetContent
import kotlin.test.Test

/** Holiday offer sheet text in the dense-screen languages, for a 1-month and a 3-month intro price (Tasks#1477). */
@OptIn(ExperimentalTestApi::class)
class HolidayOfferSheetLayoutTest {
    private val monthly = ProductInfo(
        productId = "sln_production_yearly",
        basePlanId = "\$rc_monthly",
        title = "Space Launch Now Pro",
        description = "Monthly subscription",
        formattedPrice = "$2.99",
        priceAmountMicros = 2_990_000L,
        currencyCode = "USD",
        hasIntroOffer = true,
        introOfferPrice = "$0.99",
        introOfferPeriodValue = 1,
        introOfferPeriodUnit = "MONTH",
        introOfferCycles = 1,
    )

    @Test
    fun oneMonthOfferLayout() = ScreenLayoutHarness.assertLayout("holiday_offer_1_month") {
        HolidayOfferSheetContent(product = monthly)
    }

    @Test
    fun threeMonthOfferLayout() = ScreenLayoutHarness.assertLayout("holiday_offer_3_months") {
        HolidayOfferSheetContent(product = monthly.copy(introOfferCycles = 3))
    }
}
