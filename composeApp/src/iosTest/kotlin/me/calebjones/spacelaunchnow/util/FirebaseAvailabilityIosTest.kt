package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertFalse

/**
 * The iOS test binary never calls `FirebaseApp.configure()`, exactly like the widget extension.
 * The probe must report that without touching any Firebase API that would raise.
 */
class FirebaseAvailabilityIosTest {

    @Test fun `no configured FirebaseApp is reported as not configured`() {
        assertFalse(isFirebaseAppConfigured())
    }
}
