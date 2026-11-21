package me.calebjones.spacelaunchnow.domain.mapper

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * English text from the keys must equal the text the API sent before the labels moved to keys
 * (spec 017, T13). An id or code with no key returns null so the screen shows the API text.
 */
class ApiEnumLabelsTest {
    private val statuses = mapOf(
        1 to ("Go for Launch" to "Go"),
        2 to ("To Be Determined" to "TBD"),
        3 to ("Launch Successful" to "Success"),
        4 to ("Launch Failure" to "Failure"),
        5 to ("On Hold" to "Hold"),
        6 to ("Launch in Flight" to "In Flight"),
        7 to ("Launch was a Partial Failure" to "Partial Failure"),
        8 to ("To Be Confirmed" to "TBC"),
        9 to ("Payload Deployed" to "Deployed")
    )

    private val orbits = mapOf(
        0 to "Elliptical Orbit",
        1 to "Geostationary Orbit",
        2 to "Geostationary Transfer Orbit",
        3 to "Geosynchronous Orbit",
        4 to "Geosynchronous Transfer Orbit",
        5 to "Heliocentric L1",
        6 to "Heliocentric N/A",
        7 to "High Earth Orbit",
        8 to "Low Earth Orbit",
        9 to "Lunar flyby",
        10 to "Lunar Impactor",
        11 to "Lunar Orbit",
        12 to "Medium Earth Orbit",
        13 to "Polar Orbit",
        14 to "Solar Escape Trajectory",
        15 to "Suborbital",
        16 to "Sun-Earth L2",
        17 to "Sun-Synchronous Orbit",
        18 to "Supersynchronous Transfer Orbit",
        19 to "Mars Orbit",
        20 to "Venus Orbit",
        21 to "Asteroid",
        22 to "Venus flyby",
        23 to "Mars flyby",
        24 to "Mercury flyby",
        25 to "Unknown",
        26 to "Enhanced Geostationary Transfer Orbit",
        27 to "Jupiter Orbit",
        28 to "Earth Entry Trajectory"
    )

    private val missionTypes = listOf(
        "Earth Science",
        "Planetary Science",
        "Astrophysics",
        "Heliophysics",
        "Human Exploration",
        "Robotic Exploration",
        "Government/Top Secret",
        "Tourism",
        "Unknown",
        "Communications",
        "Resupply",
        "Suborbital",
        "Test Flight",
        "Dedicated Rideshare",
        "Navigation",
        "Test Target",
        "Lunar Exploration",
        "Materials Science",
        "Biology",
        "Technology",
        "Mission Extension",
        "Space Situational Awareness"
    )

    private val countries = mapOf(
        "US" to "United States of America",
        "CN" to "China",
        "RU" to "Russia",
        "FR" to "France",
        "JP" to "Japan",
        "IN" to "India",
        "NZ" to "New Zealand",
        "GB" to "United Kingdom",
        "IL" to "Israel",
        "IR" to "Iran",
        "KR" to "South Korea",
        "KP" to "North Korea",
        "IT" to "Italy",
        "DE" to "Germany",
        "AU" to "Australia",
        "BR" to "Brazil",
        "CA" to "Canada",
        "KZ" to "Kazakhstan",
        "ES" to "Spain",
        "UA" to "Ukraine"
    )

    @Test
    fun everyKnownStatusIdMapsToItsEnglishName() = runTest {
        statuses.forEach { (id, text) ->
            assertEquals(text.first, getString(assertNotNull(ApiEnumLabels.launchStatusName(id), "status $id")))
            assertEquals(text.second, getString(assertNotNull(ApiEnumLabels.launchStatusAbbrev(id), "abbrev $id")))
        }
    }

    @Test
    fun everyKnownOrbitIdMapsToItsEnglishName() = runTest {
        orbits.forEach { (id, name) ->
            assertEquals(name, getString(assertNotNull(ApiEnumLabels.orbitName(id), "orbit $id")))
        }
    }

    @Test
    fun everyKnownMissionTypeMapsToItsEnglishName() = runTest {
        missionTypes.forEach { name ->
            assertEquals(name, getString(assertNotNull(ApiEnumLabels.missionType(name), name)))
        }
    }

    @Test
    fun everyKnownCountryCodeMapsToItsEnglishName() = runTest {
        countries.forEach { (code, name) ->
            assertEquals(name, getString(assertNotNull(ApiEnumLabels.countryName(code), code)))
        }
        assertEquals("France", getString(assertNotNull(ApiEnumLabels.countryName("fr"))))
    }

    @Test
    fun unknownIdsAndNamesFallBackToTheApiText() {
        assertNull(ApiEnumLabels.launchStatusName(99))
        assertNull(ApiEnumLabels.launchStatusName(null))
        assertNull(ApiEnumLabels.launchStatusAbbrev(99))
        assertNull(ApiEnumLabels.orbitName(999))
        assertNull(ApiEnumLabels.orbitName(null))
        assertNull(ApiEnumLabels.missionType("Space Tourism Deluxe"))
        assertNull(ApiEnumLabels.missionType(null))
        assertNull(ApiEnumLabels.countryName("ZZ"))
        assertNull(ApiEnumLabels.countryName(null))
    }
}
