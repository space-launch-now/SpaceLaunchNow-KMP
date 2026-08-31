package me.calebjones.spacelaunchnow.util

import me.calebjones.spacelaunchnow.domain.model.VideoLink

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
            "youtube\\.com/v/([^&\\n?#]+)",
            "youtube\\.com/live/([^&\\n?#]+)",
            "youtube\\.com/shorts/([^&\\n?#]+)"
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
     * Whether the embedded player can play this URL inline.
     *
     * The media player library only routes URLs it can extract a video ID from
     * to its YouTube WebView player; anything else falls into the native
     * AVPlayer/ExoPlayer path, which renders dead controls at 00:00 for page
     * URLs. Channel-form links (youtube.com/@NASA/live, /channel/..., /c/...)
     * are YouTube URLs but have no extractable ID, so they must open externally.
     */
    fun canPlayInline(url: String): Boolean {
        return extractYouTubeVideoId(url) != null
    }

    /**
     * Finds the first inline-playable YouTube video URL from a list of video URLs
     * The first video in the list has the highest priority
     */
    fun findBestYouTubeVideo(vidUrls: List<VideoLink>): VideoLink? {
        if (vidUrls.isEmpty()) return null

        // Return the first video the embedded player can actually play inline.
        // The first video has the highest priority.
        return vidUrls.find { canPlayInline(it.url) }
    }

    /**
     * Gets a human-readable video source name
     */
    fun getVideoSourceName(vidUrl: VideoLink): String {
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
    fun getVideoTitle(vidUrl: VideoLink, launchName: String): String {
        val title = if (vidUrl.title.isNullOrBlank()) launchName else vidUrl.title
        val publish =
            if (vidUrl.publisher.isNullOrBlank()) getVideoSourceName(vidUrl) else vidUrl.publisher

        if (vidUrl.live == true) {
            return "$title - $publish (Live)"
        }
        return "$title - $publish"
    }

}