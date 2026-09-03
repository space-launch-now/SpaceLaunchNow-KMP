package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DataBackendTest {

    @Test fun `fromString maps known values`() {
        assertEquals(DataBackend.TRANTOR, DataBackend.fromString("trantor"))
        assertEquals(DataBackend.LL, DataBackend.fromString("ll"))
    }

    @Test fun `fromString defaults to TRANTOR on unknown or null`() {
        assertEquals(DataBackend.TRANTOR, DataBackend.fromString("something_else"))
        assertEquals(DataBackend.TRANTOR, DataBackend.fromString(null))
    }

    @Test fun `resolve prefers a non-null local override over remote`() {
        assertEquals(
            DataBackend.LL,
            resolveDataBackend(override = DataBackend.LL, remote = DataBackend.TRANTOR)
        )
        assertEquals(
            DataBackend.TRANTOR,
            resolveDataBackend(override = DataBackend.TRANTOR, remote = DataBackend.LL)
        )
    }

    @Test fun `resolve falls back to remote when override is null`() {
        assertEquals(
            DataBackend.LL,
            resolveDataBackend(override = null, remote = DataBackend.LL)
        )
        assertEquals(
            DataBackend.TRANTOR,
            resolveDataBackend(override = null, remote = DataBackend.TRANTOR)
        )
    }

    @Test fun `default is TRANTOR when both override and remote are unresolved`() {
        val remoteDefault = DataBackend.fromString(null)
        assertEquals(DataBackend.TRANTOR, resolveDataBackend(override = null, remote = remoteDefault))
    }
}
