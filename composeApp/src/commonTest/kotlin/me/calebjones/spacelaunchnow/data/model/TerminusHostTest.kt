package me.calebjones.spacelaunchnow.data.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TerminusHostTest {
    @Test
    fun `default is prod`() {
        assertEquals(TerminusHost.PROD, TerminusHost.DEFAULT)
        assertEquals("https://terminus.spacelaunchnow.app", TerminusHost.DEFAULT.baseUrl)
    }

    @Test
    fun `debug with no stored choice resolves to prod`() {
        assertEquals(TerminusHost.PROD, resolveTerminusHost(isDebug = true, stored = null))
    }

    @Test
    fun `debug with staging stored resolves to the staging url`() {
        val host = resolveTerminusHost(isDebug = true, stored = TerminusHost.STAGING)
        assertEquals("https://staging-terminus.spacelaunchnow.app", host.baseUrl)
    }

    @Test
    fun `release ignores the stored choice`() {
        assertEquals(TerminusHost.PROD, resolveTerminusHost(isDebug = false, stored = TerminusHost.STAGING))
    }

    @Test
    fun `parse and url lookup round trip`() {
        TerminusHost.entries.forEach {
            assertEquals(it, TerminusHost.parseOrNull(it.value))
            assertEquals(it, TerminusHost.fromBaseUrl(it.baseUrl))
        }
        assertNull(TerminusHost.parseOrNull("bogus"))
        assertNull(TerminusHost.fromBaseUrl(null))
    }
}
