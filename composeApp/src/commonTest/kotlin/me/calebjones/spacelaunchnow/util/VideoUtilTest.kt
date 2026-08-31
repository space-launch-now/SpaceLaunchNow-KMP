package me.calebjones.spacelaunchnow.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class VideoUtilTest {

    @Test
    fun extractsIdFromWatchUrl() {
        assertEquals(
            "dQw4w9WgXcQ",
            VideoUtil.extractYouTubeVideoId("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        )
    }

    @Test
    fun extractsIdFromShortUrl() {
        assertEquals(
            "dQw4w9WgXcQ",
            VideoUtil.extractYouTubeVideoId("https://youtu.be/dQw4w9WgXcQ?t=42")
        )
    }

    @Test
    fun extractsIdFromLiveUrl() {
        assertEquals(
            "21X5lGlDOfg",
            VideoUtil.extractYouTubeVideoId("https://www.youtube.com/live/21X5lGlDOfg")
        )
    }

    @Test
    fun extractsIdFromShortsUrl() {
        assertEquals(
            "abc123XYZ_-",
            VideoUtil.extractYouTubeVideoId("https://www.youtube.com/shorts/abc123XYZ_-")
        )
    }

    @Test
    fun channelFormUrlsHaveNoExtractableId() {
        assertNull(VideoUtil.extractYouTubeVideoId("https://www.youtube.com/@NASA/live"))
        assertNull(VideoUtil.extractYouTubeVideoId("https://www.youtube.com/channel/UCLA_DiR1FfKNvjuUpBHmylQ"))
        assertNull(VideoUtil.extractYouTubeVideoId("https://www.youtube.com/c/SpaceX"))
    }

    @Test
    fun canPlayInlineOnlyForExtractableUrls() {
        assertTrue(VideoUtil.canPlayInline("https://www.youtube.com/watch?v=dQw4w9WgXcQ"))
        assertTrue(VideoUtil.canPlayInline("https://www.youtube.com/live/21X5lGlDOfg"))
        // YouTube URLs without an extractable ID would hit the dead native player
        assertFalse(VideoUtil.canPlayInline("https://www.youtube.com/@NASA/live"))
        assertFalse(VideoUtil.canPlayInline("https://x.com/i/broadcasts/1YqKDqZjPjdxV"))
        assertFalse(VideoUtil.canPlayInline("https://plus.nasa.gov/video/some-stream"))
    }
}
