package me.calebjones.spacelaunchnow.ui.onboarding

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import org.jetbrains.compose.resources.getString
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_paywall_monthly_per_month
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_paywall_subtitle_subscribed
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_welcome_explore
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_welcome_tagline

/** Strings with spaces, a line break or an apostrophe must read as the old Kotlin literals did. */
class OnboardingStringsTest {
    @Test
    fun quotedWhitespaceIsKept() = runTest {
        assertEquals("  Explore the App  ", getString(Res.string.onboarding_welcome_explore))
        assertEquals(" \u00B7 \$9.99/mo", getString(Res.string.onboarding_paywall_monthly_per_month, "\$9.99"))
    }

    @Test
    fun escapesAreResolved() = runTest {
        assertEquals("Your guide to every rocket launch,\nright from your pocket.", getString(Res.string.onboarding_welcome_tagline))
        assertEquals("You're a Premium member. Thanks for supporting!", getString(Res.string.onboarding_paywall_subtitle_subscribed))
    }
}
