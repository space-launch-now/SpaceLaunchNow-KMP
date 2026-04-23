package me.calebjones.spacelaunchnow.ui.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import me.calebjones.spacelaunchnow.domain.model.Event
import me.calebjones.spacelaunchnow.domain.model.EventType
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.Provider

internal class InMemoryPreferencesDataStore(
    initial: Preferences = emptyPreferences()
) : DataStore<Preferences> {
    private val state = MutableStateFlow(initial)

    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

internal fun sampleProvider(
    id: Int = 121,
    name: String = "SpaceX"
) = Provider(
    id = id,
    name = name,
    abbrev = name.take(3),
    type = "Commercial",
    countryCode = "US",
    logoUrl = null,
    imageUrl = null
)

internal fun sampleLaunch(
    id: String = "launch-1",
    name: String = "Falcon 9 Test"
) = Launch(
    id = id,
    name = name,
    slug = id,
    net = null,
    windowStart = null,
    windowEnd = null,
    lastUpdated = null,
    status = null,
    provider = sampleProvider(),
    imageUrl = null,
    thumbnailUrl = null,
    infographic = null,
    netPrecision = null
)

internal fun sampleEvent(
    id: Int = 1,
    name: String = "Docking Event"
) = Event(
    id = id,
    name = name,
    slug = "event-$id",
    type = EventType(id = 1, name = "General"),
    description = null,
    date = null,
    location = "LEO",
    imageUrl = null,
    webcastLive = false,
    lastUpdated = null,
    duration = null,
    datePrecision = null
)
