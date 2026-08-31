package me.calebjones.spacelaunchnow.data.model

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class OnboardingVariantTest {

    @Test fun `fromString maps known values`() {
        assertEquals(OnboardingVariant.SHORT, OnboardingVariant.fromString("short"))
        assertEquals(OnboardingVariant.CONTROL, OnboardingVariant.fromString("control"))
    }

    @Test fun `fromString falls back to CONTROL on unknown or null`() {
        assertEquals(OnboardingVariant.CONTROL, OnboardingVariant.fromString("experimental_v9"))
        assertEquals(OnboardingVariant.CONTROL, OnboardingVariant.fromString(null))
    }

    @Test fun `resolve returns persisted variant without fetching`() = runTest {
        var fetched = false
        val result = resolveOnboardingVariant(
            persisted = "short",
            fetchRemote = { fetched = true; OnboardingVariant.CONTROL },
            persist = { }
        )
        assertEquals(OnboardingVariant.SHORT, result)
        assertFalse(fetched)
    }

    @Test fun `resolve fetches and persists when nothing stored`() = runTest {
        var persisted: String? = null
        val result = resolveOnboardingVariant(
            persisted = null,
            fetchRemote = { OnboardingVariant.SHORT },
            persist = { persisted = it }
        )
        assertEquals(OnboardingVariant.SHORT, result)
        assertEquals("short", persisted)
    }
}
