package me.calebjones.spacelaunchnow.ui.onboarding

import me.calebjones.spacelaunchnow.data.model.OnboardingVariant
import kotlin.test.Test
import kotlin.test.assertEquals

class OnboardingPagesTest {

    @Test fun `control shows all five pages in order`() {
        assertEquals(
            listOf(
                OnboardingPage.WELCOME,
                OnboardingPage.LAUNCH_CARD,
                OnboardingPage.NEWS_EVENTS,
                OnboardingPage.WIDGETS,
                OnboardingPage.NOTIFICATION_PERMISSION
            ),
            pagesFor(OnboardingVariant.CONTROL)
        )
    }

    @Test fun `short shows welcome then notification permission only`() {
        assertEquals(
            listOf(OnboardingPage.WELCOME, OnboardingPage.NOTIFICATION_PERMISSION),
            pagesFor(OnboardingVariant.SHORT)
        )
    }

    @Test fun `analytics names are stable across variants`() {
        assertEquals("welcome", OnboardingPage.WELCOME.analyticsName)
        assertEquals("notification_permission", OnboardingPage.NOTIFICATION_PERMISSION.analyticsName)
    }
}
