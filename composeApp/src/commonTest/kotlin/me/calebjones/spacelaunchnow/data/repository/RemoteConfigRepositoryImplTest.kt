package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.DataBackend
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The Remote Config repository must never ask the Firebase SDK for a Remote Config instance in
 * a process that has no configured Firebase app. On iOS the widget extension is such a process:
 * `FIRRemoteConfig.remoteConfig()` raises an Objective-C exception there, which Kotlin cannot
 * catch, so the extension is killed (seen on the simulator as `FIRAppNotConfigured`).
 */
class RemoteConfigRepositoryImplTest {

    @Test fun `no Firebase app means the SDK is never asked for Remote Config`() = runTest {
        val repository = RemoteConfigRepositoryImpl(
            firebaseConfigured = { false },
            createRemoteConfig = { throw AssertionError("Firebase Remote Config must not be created") },
        )

        // An AssertionError is not an Exception, so it would escape the repository's catch
        // blocks and fail this test if the factory were invoked.
        assertEquals(DataBackend.DEFAULT, repository.getDataBackend())
    }

    @Test fun `a configured Firebase app is asked for Remote Config once and SDK failures still fall back`() = runTest {
        var created = 0
        val repository = RemoteConfigRepositoryImpl(
            firebaseConfigured = { true },
            createRemoteConfig = { created++; throw IllegalStateException("SDK unavailable in tests") },
        )

        assertEquals(DataBackend.DEFAULT, repository.getDataBackend())
        assertEquals(DataBackend.DEFAULT, repository.getDataBackend())
        assertEquals(1, created, "Remote Config is created lazily, once")
    }

    @Test fun `the default factory is only consulted after the configured check`() = runTest {
        var checked = 0
        val repository = RemoteConfigRepositoryImpl(
            firebaseConfigured = { checked++; false },
            createRemoteConfig = { throw AssertionError("Firebase Remote Config must not be created") },
        )

        repository.setDefaults()
        repository.fetchAndActivate(forceRefresh = true)
        assertEquals(1, checked, "the configured check runs once, when the lazy instance is first needed")
    }
}
