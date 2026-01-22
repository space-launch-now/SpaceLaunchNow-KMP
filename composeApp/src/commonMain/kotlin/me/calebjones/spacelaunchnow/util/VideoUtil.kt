package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL

object VideoUtil {

    /**
     * Extracts YouTube video ID from various YouTube URL formats
     */
    fun extractYouTubeVideoId(url: String): String? {
        val patterns = listOf(
            "(?:youtube\\.com/watch\\?v=|youtu\\.be/|youtube\\.com/embed/)([^&\\n?#]+)",
            "youtube\\.com/watch\\?.*v=([^&\\n?#]+)",
            "youtu\\.be/([^&\\n?#]+)",
            "youtube\\.com/embed/([^&\\n?#]+)",
            "youtube\\.com/v/([^&\\n?#]+)"
        )

        for (pattern in patterns) {
            val regex = Regex(pattern, RegexOption.IGNORE_CASE)
            val match = regex.find(url)
            if (match != null) {
                return match.groupValues[1]
            }
        }
        return null
    }

    /**
     * Checks if a URL is a YouTube URL
     */
    fun isYouTubeUrl(url: String): Boolean {
        return url.contains("youtube.com") || url.contains("youtu.be")
    }

    /**
     * Finds the first YouTube video URL from a list of video URLs
     * The first video in the list has the highest priority
     */
    fun findBestYouTubeVideo(vidUrls: List<VidURL>): VidURL? {
        if (vidUrls.isEmpty()) return null

        // Simply return the first YouTube video in the list
        // The first video has the highest priority
        return vidUrls.find { isYouTubeUrl(it.url) }
    }

    /**
     * Gets a human-readable video source name
     */
    fun getVideoSourceName(vidUrl: VidURL): String {
        return when {
            vidUrl.publisher != null -> vidUrl.publisher
            vidUrl.source != null -> vidUrl.source
            isYouTubeUrl(vidUrl.url) -> "YouTube"
            else -> "Video"
        }
    }

    /**
     * Gets the appropriate title for a video
     */
    fun getVideoTitle(vidUrl: VidURL, launchName: String): String {
        val title = if (vidUrl.title.isNullOrBlank()) launchName else vidUrl.title
        val publish =
            if (vidUrl.publisher.isNullOrBlank()) getVideoSourceName(vidUrl) else vidUrl.publisher

        if (vidUrl.live == true) {
            return "$title - $publish (Live)"
        }
        return "$title - $publish"
    }
}