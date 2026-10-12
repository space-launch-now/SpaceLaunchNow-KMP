package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.Expedition
import me.calebjones.spacelaunchnow.api.trantor.models.ExpeditionCrewMember
import me.calebjones.spacelaunchnow.api.trantor.models.ExpeditionMissionPatch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** E14(P5): expedition crew gains `image_url` + agency fields; expeditions gain `mission_patches[]`. */
class SpaceStationParityMappersTest {

    @Test
    fun crewMemberMapsImageAndAgency() {
        val crew = ExpeditionCrewMember(
            astronautId = 9,
            astronautName = "Jane Doe",
            role = "Commander",
            imageUrl = "https://img/jane.png",
            agencyId = 44,
            agencyName = "NASA",
            agencyAbbrev = "NASA"
        ).toDomainCrewMember()
        assertEquals("https://img/jane.png", crew.astronaut.imageUrl)
        assertEquals("https://img/jane.png", crew.astronaut.thumbnailUrl)
        assertEquals(44, crew.astronaut.agencyId)
        assertEquals("NASA", crew.astronaut.agencyName)
        assertEquals("NASA", crew.astronaut.agencyAbbrev)
    }

    @Test
    fun crewMemberFieldsStayNullWhenAbsent() {
        val crew = ExpeditionCrewMember(astronautId = 9, astronautName = "Jane Doe").toDomainCrewMember()
        assertNull(crew.astronaut.imageUrl)
        assertNull(crew.astronaut.agencyId)
        assertNull(crew.astronaut.agencyName)
        assertNull(crew.astronaut.agencyAbbrev)
    }

    @Test
    fun missionPatchesAreMapped() {
        val domain = Expedition(
            id = 1,
            name = "Expedition 70",
            missionPatches = listOf(
                ExpeditionMissionPatch(id = 5, name = "Patch", imageUrl = "https://img/patch.png", priority = 2)
            )
        ).toDomainDetail()
        val patch = domain.missionPatches.single()
        assertEquals(5, patch.id)
        assertEquals("https://img/patch.png", patch.imageUrl)
        assertEquals(2, patch.priority)
    }

    @Test
    fun missionPatchesAreEmptyWhenAbsent() {
        assertTrue(Expedition(id = 1, name = "E").toDomainDetail().missionPatches.isEmpty())
    }
}
