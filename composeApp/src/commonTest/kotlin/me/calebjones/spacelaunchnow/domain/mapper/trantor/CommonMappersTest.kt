package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.UpdateList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * E8(P5): the standalone updates row's `program` joins `programs.name` at read time from the
 * row's own `program_id` - not derived from the `program_ids` filter's junction reach.
 */
class CommonMappersTest {

    @Test
    fun programIsNullWhenTheRowHasNoProgramId() {
        val api = UpdateList(id = 1, programId = null, programName = null)
        val domain = api.toDomain()
        assertNull(domain.program)
    }

    @Test
    fun programIsFilledFromTheRowsOwnProgramIdAndName() {
        val api = UpdateList(id = 1, programId = 3, programName = "Starlink")
        val domain = api.toDomain()
        assertEquals(3, domain.program?.id)
        assertEquals("Starlink", domain.program?.name)
    }
}
