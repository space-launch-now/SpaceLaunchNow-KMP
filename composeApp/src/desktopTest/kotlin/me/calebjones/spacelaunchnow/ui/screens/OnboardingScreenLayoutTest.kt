package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.onboarding.pages.LaunchCardPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.NewsEventsPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.NotificationPermissionPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.WelcomePage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.WidgetsPage
import kotlin.test.Test

/** The onboarding pager shows one page at a time, so each page is its own screen in the report. */
@OptIn(ExperimentalTestApi::class)
class OnboardingScreenLayoutTest {
    private val modules get() = listOf(ScreenKoin.common)

    @Test
    fun welcomePage() = ScreenLayoutHarness.assertLayout("onboarding_welcome", koinModules = { modules }) {
        WelcomePage(modifier = Modifier, nextLaunch = ScreenFixtures.launch)
    }

    @Test
    fun launchCardPage() = ScreenLayoutHarness.assertLayout("onboarding_launch_card", koinModules = { modules }) {
        LaunchCardPage(nextLaunch = ScreenFixtures.launch)
    }

    @Test
    fun newsPage() = ScreenLayoutHarness.assertLayout("onboarding_news", koinModules = { modules }) {
        NewsEventsPage()
    }

    @Test
    fun widgetsPage() = ScreenLayoutHarness.assertLayout("onboarding_widgets", koinModules = { modules }) {
        WidgetsPage()
    }

    @Test
    fun notificationPermissionPage() = ScreenLayoutHarness.assertLayout("onboarding_notifications", koinModules = { modules }) {
        NotificationPermissionPage(onPermissionResult = {})
    }
}
