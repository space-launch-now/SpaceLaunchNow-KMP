package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingContent
import me.calebjones.spacelaunchnow.ui.subscription.SupportUsScreen
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class PaywallScreenLayoutTest {
    private val annual = ProductInfo(
        productId = "sln_premium",
        basePlanId = "annual",
        title = "Space Launch Now Premium",
        description = "Annual subscription",
        formattedPrice = "$9.99",
        priceAmountMicros = 9_990_000L,
        currencyCode = "USD",
        hasFreeTrial = true,
        freeTrialPeriodDisplay = "2-week",
        freeTrialPeriodValue = 2,
        freeTrialPeriodUnit = "WEEK",
    )
    private val monthly = ProductInfo(
        productId = "sln_premium",
        basePlanId = "monthly",
        title = "Space Launch Now Premium",
        description = "Monthly subscription",
        formattedPrice = "$1.49",
        priceAmountMicros = 1_490_000L,
        currencyCode = "USD",
    )

    @Test
    fun onboardingPaywallLayout() = ScreenLayoutHarness.assertLayout("paywall_onboarding", koinModules = { listOf(ScreenKoin.common) }) {
        OnboardingContent(
            annualProduct = annual,
            monthlyProduct = monthly,
            savingsPercent = "38%",
            nextLaunch = ScreenFixtures.launch,
        )
    }

    @Test
    fun supportUsLayout() = ScreenLayoutHarness.assertLayout(
        "paywall_support_us",
        koinModules = { listOf(ScreenKoin.common, ScreenKoin.paywall) },
    ) {
        SupportUsScreen()
    }
}
