package me.calebjones.spacelaunchnow.util

/**
 * Constants for the Starship Dashboard feature
 *
 * These constants define program IDs, configuration IDs, and default values
 * for filtering Starship-specific content from the API.
 */
object StarshipConstants {
    /**
     * The program ID for Starship in the Launch Library API
     */
    const val STARSHIP_PROGRAM_ID = 1

    /**
     * The SpaceX agency ID in the Launch Library API
     * Used for filtering spacecraft configs (which don't have program filter)
     */
    const val SPACEX_AGENCY_ID = 121

    /**
     * The spacecraft configuration ID for Starship vehicles
     * Note: This may need verification against the actual API
     */
    const val STARSHIP_CONFIG_ID = 3

    /**
     * Default livestream URL for Starship launches
     */
    const val DEFAULT_LIVESTREAM_URL = "https://www.youtube.com/spacex"

    /**
     * Default limit for status updates
     */
    const val UPDATE_LIMIT = 20

    /**
     * Default limit for news/articles
     */
    const val NEWS_LIMIT = 10

    /**
     * Default limit for vehicles/spacecraft
     */
    const val VEHICLES_LIMIT = 20
}

