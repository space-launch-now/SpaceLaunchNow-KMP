package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.navigation.Home
import me.calebjones.spacelaunchnow.navigation.LiveOnboarding
import me.calebjones.spacelaunchnow.navigation.Onboarding
import kotlin.test.Test
import kotlin.test.assertEquals

class PreloadDestinationTest {

    @Test fun `fresh install goes to live onboarding`() {
        assertEquals(LiveOnboarding, preloadDestination(liveOnboardingCompleted = false, onboardingPaywallShown = false))
    }

    @Test fun `pager done but paywall unseen goes to paywall`() {
        assertEquals(Onboarding, preloadDestination(liveOnboardingCompleted = true, onboardingPaywallShown = false))
    }

    @Test fun `fully onboarded goes home`() {
        assertEquals(Home, preloadDestination(liveOnboardingCompleted = true, onboardingPaywallShown = true))
    }
}
