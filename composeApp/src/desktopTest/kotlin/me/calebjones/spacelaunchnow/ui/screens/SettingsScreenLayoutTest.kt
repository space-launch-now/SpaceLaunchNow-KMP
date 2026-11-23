package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.settings.NotificationSettingsScreen
import me.calebjones.spacelaunchnow.ui.settings.SettingsScreen
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettingsScreenLayoutTest {
    private val modules get() = listOf(ScreenKoin.common, ScreenKoin.settings)

    @Test
    fun settingsLayout() = ScreenLayoutHarness.assertLayout("settings", koinModules = { modules }) {
        SettingsScreen(navController = rememberNavController())
    }

    @Test
    fun notificationSettingsLayout() = ScreenLayoutHarness.assertLayout("notification_settings", koinModules = { modules }) {
        NotificationSettingsScreen(navController = rememberNavController())
    }
}
