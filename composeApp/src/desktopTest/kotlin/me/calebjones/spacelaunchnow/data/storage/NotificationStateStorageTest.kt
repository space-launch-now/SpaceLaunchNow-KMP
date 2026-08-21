package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.runBlocking
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * NotificationStateStorage maps each field to its own preferences key BY HAND —
 * a new NotificationState field that is not added to both saveState and the
 * stateFlow mapping silently fails to persist: the in-memory flow carries it,
 * but everything reading storage.getState() (the V6 reconciler above all) sees
 * the default forever. That is exactly how the Starlink mute toggle shipped
 * subscribing to nothing. These tests make the next dropped field a red build.
 */
class NotificationStateStorageTest {

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
    }

    private fun freshStorage(): NotificationStateStorage {
        val dir = Files.createTempDirectory("notif-storage-test")
        val dataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { dir.resolve("test.preferences_pb").toString().toPath() }
        )
        return NotificationStateStorage(dataStore)
    }

    @Test
    fun `muteStarlink survives a save-load round trip`() = runBlocking {
        val storage = freshStorage()

        storage.saveState(NotificationState(muteStarlink = true)).getOrThrow()

        assertEquals(true, storage.getState().muteStarlink)
    }

    @Test
    fun `filter fields survive a save-load round trip`() = runBlocking {
        val storage = freshStorage()
        val state = NotificationState(
            enableNotifications = false,
            followAllLaunches = true,
            useStrictMatching = true,
            hideTbdLaunches = true,
            muteStarlink = true,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("27"),
            hasCompletedV6Changeover = true,
        )

        storage.saveState(state).getOrThrow()
        val loaded = storage.getState()

        assertEquals(false, loaded.enableNotifications)
        assertEquals(true, loaded.followAllLaunches)
        assertEquals(true, loaded.useStrictMatching)
        assertEquals(true, loaded.hideTbdLaunches)
        assertEquals(true, loaded.muteStarlink)
        assertEquals(setOf("121"), loaded.subscribedAgencies)
        assertEquals(setOf("27"), loaded.subscribedLocations)
        assertEquals(true, loaded.hasCompletedV6Changeover)
    }
}
