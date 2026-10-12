package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.ProgramDetail
import me.calebjones.spacelaunchnow.api.trantor.models.ProgramVidUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** E14(P5): program detail gains `vid_urls[]`. */
class ProgramMappersTest {

    @Test
    fun vidUrlsAreMapped() {
        val domain = ProgramDetail(
            id = 1,
            name = "Starship",
            vidUrls = listOf(
                ProgramVidUrl(
                    url = "https://yt/live",
                    title = "Live",
                    publisher = "SpaceX",
                    description = "Flight 12",
                    featureImage = "https://img",
                    live = true,
                    priority = 1
                )
            )
        ).toDomainProgram()
        val v = domain.vidUrls.single()
        assertEquals("https://yt/live", v.url)
        assertEquals("SpaceX", v.publisher)
        assertEquals("Flight 12", v.description)
        assertEquals("https://img", v.featureImage)
        assertEquals(true, v.live)
        assertEquals(1, v.priority)
    }

    @Test
    fun vidUrlsAreEmptyWhenAbsent() {
        assertTrue(ProgramDetail(id = 1, name = "Starship").toDomainProgram().vidUrls.isEmpty())
    }

    @Test
    fun optionalVidUrlFieldsStayNull() {
        val v = ProgramDetail(id = 1, name = "S", vidUrls = listOf(ProgramVidUrl(url = "u")))
            .toDomainProgram().vidUrls.single()
        assertNull(v.description)
        assertNull(v.featureImage)
        assertNull(v.live)
    }
}
