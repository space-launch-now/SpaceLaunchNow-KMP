package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.repository.MockRemoteConfigRepository
import me.calebjones.spacelaunchnow.ui.home.components.shouldShowHomeUpgradeRow
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeUpgradeRowPreferencesTest {

    private fun newPreferences(): HomeUpgradeRowPreferences {
        val dir = Files.createTempDirectory("upgrade-row")
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { dir.resolve("test.preferences_pb").toString().toPath() }
        )
        return HomeUpgradeRowPreferences(dataStore)
    }

    @Test
    fun `row is not dismissed by default`() = runBlocking {
        assertFalse(newPreferences().dismissedFlow.first())
    }

    @Test
    fun `close persists in the store`() = runBlocking {
        val prefs = newPreferences()

        prefs.dismiss()

        assertTrue(prefs.dismissedFlow.first())
    }

    @Test
    fun `row shows for a free user with the flag on and no close`() {
        assertTrue(shouldShowHomeUpgradeRow(flagEnabled = true, isPremium = false, dismissed = false))
    }

    @Test
    fun `row hides when the flag is off, the user subscribes, or the user closed it`() {
        assertFalse(shouldShowHomeUpgradeRow(flagEnabled = false, isPremium = false, dismissed = false))
        assertFalse(shouldShowHomeUpgradeRow(flagEnabled = true, isPremium = true, dismissed = false))
        assertFalse(shouldShowHomeUpgradeRow(flagEnabled = true, isPremium = false, dismissed = true))
    }

    @Test
    fun `flag defaults to off in the mock config`() = runBlocking {
        assertFalse(MockRemoteConfigRepository().getHomeUpgradeRowEnabled())
    }
}
