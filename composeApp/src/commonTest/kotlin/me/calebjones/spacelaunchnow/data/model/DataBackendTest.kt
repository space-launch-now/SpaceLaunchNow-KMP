package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DataBackendTest {

    @Test fun `fromString maps known values`() {
        assertEquals(DataBackend.TERMINUS, DataBackend.fromString("terminus"))
        assertEquals(DataBackend.LL, DataBackend.fromString("ll"))
    }

    @Test fun `fromString falls back to LL on unknown or null`() {
        assertEquals(DataBackend.LL, DataBackend.fromString("something_else"))
        assertEquals(DataBackend.LL, DataBackend.fromString(null))
    }

    @Test fun `DEFAULT is LL so a missing or failed flag never routes production to Terminus`() {
        assertEquals(DataBackend.LL, DataBackend.DEFAULT)
    }

    @Test fun `resolve prefers a non-null local override over remote`() {
        assertEquals(
            DataBackend.LL,
            resolveDataBackend(override = DataBackend.LL, remote = DataBackend.TERMINUS)
        )
        assertEquals(
            DataBackend.TERMINUS,
            resolveDataBackend(override = DataBackend.TERMINUS, remote = DataBackend.LL)
        )
    }

    @Test fun `resolve falls back to remote when override is null`() {
        assertEquals(
            DataBackend.LL,
            resolveDataBackend(override = null, remote = DataBackend.LL)
        )
        assertEquals(
            DataBackend.TERMINUS,
            resolveDataBackend(override = null, remote = DataBackend.TERMINUS)
        )
    }

    @Test fun `default is LL when both override and remote are unresolved`() {
        val remoteDefault = DataBackend.fromString(null)
        assertEquals(DataBackend.LL, resolveDataBackend(override = null, remote = remoteDefault))
    }
}
