package me.calebjones.spacelaunchnow.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationPermissionRemedyTest {

    @Test
    fun grantedNeedsNoRemedy() {
        assertEquals(
            NotificationPermissionRemedy.NONE,
            NotificationPermissionStatus.GRANTED.remedy()
        )
    }

    @Test
    fun notDeterminedRequestsThePermissionDialog() {
        // iOS: the OS dialog has never been shown, so requesting authorization
        // will present it. Deep-linking to Settings instead is a dead end --
        // the Notifications row doesn't exist there until the app has asked once.
        assertEquals(
            NotificationPermissionRemedy.REQUEST_PERMISSION,
            NotificationPermissionStatus.NOT_DETERMINED.remedy()
        )
    }

    @Test
    fun deniedFallsBackToSystemSettings() {
        // The OS won't show the dialog again after an explicit denial;
        // system settings is the only path back.
        assertEquals(
            NotificationPermissionRemedy.OPEN_SYSTEM_SETTINGS,
            NotificationPermissionStatus.DENIED.remedy()
        )
    }
}
