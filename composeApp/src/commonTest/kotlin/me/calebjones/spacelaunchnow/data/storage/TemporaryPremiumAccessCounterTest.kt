package me.calebjones.spacelaunchnow.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TemporaryPremiumAccessCounterTest {

    private fun fakeDataStore(): DataStore<Preferences> {
        val state = MutableStateFlow(emptyPreferences())
        return object : DataStore<Preferences> {
            override val data = state
            override suspend fun updateData(
                transform: suspend (t: Preferences) -> Preferences
            ): Preferences {
                val updated = transform(state.value)
                state.value = updated
                return updated
            }
        }
    }

    @Test
    fun `incrementGrantsTotal increments by 1 each call`() = runTest {
        val ds = fakeDataStore()
        val sut = TemporaryPremiumAccess(ds)

        assertEquals(0L, sut.grantsTotalFlow.first())
        sut.incrementGrantsTotal()
        sut.incrementGrantsTotal()
        sut.incrementGrantsTotal()
        assertEquals(3L, sut.grantsTotalFlow.first())
    }

    @Test
    fun `incrementAdsShownTotal increments by 1 each call`() = runTest {
        val ds = fakeDataStore()
        val sut = TemporaryPremiumAccess(ds)

        assertEquals(0L, sut.adsShownTotalFlow.first())
        sut.incrementAdsShownTotal()
        assertEquals(1L, sut.adsShownTotalFlow.first())
    }
}
