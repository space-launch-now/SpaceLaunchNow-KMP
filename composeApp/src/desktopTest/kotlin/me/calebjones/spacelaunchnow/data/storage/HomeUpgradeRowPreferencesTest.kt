package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
}
