package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals

class DataBackendTest {

    @Test fun `no key gives Terminus`() {
        assertEquals(DataBackend.TERMINUS, DataBackend.fromLegacyFlag(null))
        assertEquals(DataBackend.TERMINUS, DataBackend.fromLegacyFlag(""))
    }

    @Test fun `use_legacy_backend true gives LL`() {
        assertEquals(DataBackend.LL, DataBackend.fromLegacyFlag("true"))
        assertEquals(DataBackend.LL, DataBackend.fromLegacyFlag(" TRUE "))
    }

    @Test fun `use_legacy_backend false or junk gives Terminus`() {
        assertEquals(DataBackend.TERMINUS, DataBackend.fromLegacyFlag("false"))
        assertEquals(DataBackend.TERMINUS, DataBackend.fromLegacyFlag("something_else"))
    }

    @Test fun `a data_backend style value changes nothing`() {
        assertEquals(DataBackend.TERMINUS, DataBackend.fromLegacyFlag("terminus"))
        assertEquals(DataBackend.TERMINUS, DataBackend.fromLegacyFlag("ll"))
    }

    @Test fun `DEFAULT is Terminus so a missing or failed flag never routes users to LL`() {
        assertEquals(DataBackend.TERMINUS, DataBackend.DEFAULT)
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

    @Test fun `default is Terminus when both override and remote are unresolved`() {
        val remoteDefault = DataBackend.fromLegacyFlag(null)
        assertEquals(DataBackend.TERMINUS, resolveDataBackend(override = null, remote = remoteDefault))
    }
}
