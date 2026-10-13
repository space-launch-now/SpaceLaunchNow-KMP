package me.calebjones.spacelaunchnow.sync

import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Status
import kotlinx.coroutines.test.runTest
import java.util.concurrent.ExecutionException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WearAvailabilityTest {

    private val apiUnavailable = ApiException(Status(17))

    @Test
    fun apiUnavailableMarksUnavailableAndSecondCallSkipsClient() = runTest {
        val availability = WearAvailability()
        var clientCalls = 0

        val first = availability.runIfAvailable {
            clientCalls++
            throw apiUnavailable
        }
        val second = availability.runIfAvailable { clientCalls++ }

        assertFalse(first)
        assertFalse(second)
        assertTrue(availability.isUnavailable)
        assertEquals(1, clientCalls)
    }

    @Test
    fun apiUnavailableWrappedInExecutionExceptionIsDetected() = runTest {
        val availability = WearAvailability()

        val result = availability.runIfAvailable { throw ExecutionException(apiUnavailable) }

        assertFalse(result)
        assertTrue(availability.isUnavailable)
    }

    @Test
    fun otherFailuresAreRethrownAndDoNotMarkUnavailable() = runTest {
        val availability = WearAvailability()

        assertFailsWith<ApiException> {
            availability.runIfAvailable { throw ApiException(Status(8)) }
        }
        assertFalse(availability.isUnavailable)
    }

    @Test
    fun successfulCallLeavesApiAvailable() = runTest {
        val availability = WearAvailability()
        var clientCalls = 0

        assertTrue(availability.runIfAvailable { clientCalls++ })
        assertTrue(availability.runIfAvailable { clientCalls++ })
        assertEquals(2, clientCalls)
    }
}
