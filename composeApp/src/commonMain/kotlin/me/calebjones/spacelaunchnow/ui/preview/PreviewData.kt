package me.calebjones.spacelaunchnow.ui.preview

import me.calebjones.spacelaunchnow.data.model.PinnedContent
import me.calebjones.spacelaunchnow.data.model.PinnedContentType
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.Location
import me.calebjones.spacelaunchnow.domain.model.Mission
import me.calebjones.spacelaunchnow.domain.model.Pad
import me.calebjones.spacelaunchnow.domain.model.Provider
import me.calebjones.spacelaunchnow.ui.viewmodel.PinnedLaunchContent
import me.calebjones.spacelaunchnow.ui.viewmodel.PinnedMotdContent
import kotlin.time.Instant

/**
 * Centralized mock data for Compose previews.
 * This object intentionally exposes only domain models.
 */
object PreviewData {

    val domainStatusGo = LaunchStatus(
        id = 1,
        name = "Go for Launch",
        abbrev = "Go",
        description = "Launch is proceeding as scheduled"
    )

    val domainStatusTBD = LaunchStatus(
        id = 2,
        name = "To Be Determined",
        abbrev = "TBD",
        description = "Launch date to be determined"
    )

    val domainStatusSuccess = LaunchStatus(
        id = 3,
        name = "Launch Successful",
        abbrev = "Success",
        description = "Launch was successful"
    )

    val domainStatusInFlight = LaunchStatus(
        id = 6,
        name = "In Flight",
        abbrev = "InFlight",
        description = "Vehicle is currently in flight"
    )

    val domainProviderSpaceX = Provider(
        id = 121,
        name = "SpaceX",
        abbrev = "SpX",
        type = "Commercial",
        countryCode = "US",
        logoUrl = "https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/spacex_image_20190207032501.jpeg",
        imageUrl = null,
        socialLogo = null,
    )

    val domainProviderULA = Provider(
        id = 124,
        name = "United Launch Alliance",
        abbrev = "ULA",
        type = "Commercial",
        countryCode = "US",
        logoUrl = null,
        imageUrl = null,
        socialLogo = null
    )

    val domainMissionStarlink = Mission(
        id = 1,
        name = "Starlink Group 6-34",
        description = "Deployment of Starlink satellites",
        type = "Communications",
        orbit = null,
        imageUrl = null
    )

    val domainMissionGOESU = Mission(
        id = 2,
        name = "GOES-U",
        description = "Geostationary weather satellite",
        type = "Earth Science",
        orbit = null,
        imageUrl = null
    )

    val domainMissionCrew = Mission(
        id = 3,
        name = "Crew-8",
        description = "SpaceX Crew Dragon mission to ISS",
        type = "Human Exploration",
        orbit = null,
        imageUrl = null
    )

    val domainLocationKSC = Location(
        id = 27,
        name = "Kennedy Space Center, FL, USA",
        countryCode = "US"
    )

    val domainLocationCapeCanaveral = Location(
        id = 12,
        name = "Cape Canaveral SFS, FL, USA",
        countryCode = "US"
    )

    val domainPadSLC40 = Pad(
        id = 16,
        name = "Space Launch Complex 40",
        latitude = 28.56194122,
        longitude = -80.57735736,
        mapUrl = null,
        mapImage = null,
        totalLaunchCount = 150,
        location = domainLocationCapeCanaveral
    )

    val domainPadLC39A = Pad(
        id = 87,
        name = "Launch Complex 39A",
        latitude = 28.60822681,
        longitude = -80.60428186,
        mapUrl = null,
        mapImage = null,
        totalLaunchCount = 200,
        location = domainLocationKSC
    )

    val domainPadSLC41 = Pad(
        id = 25,
        name = "Space Launch Complex 41",
        latitude = 28.58341025,
        longitude = -80.58303644,
        mapUrl = null,
        mapImage = null,
        totalLaunchCount = 100,
        location = domainLocationCapeCanaveral
    )

    val domainLaunchSpaceX = Launch(
        id = "abc-123",
        name = "Falcon 9 Block 5 | Starlink Group 6-34",
        slug = "falcon-9-starlink",
        net = Instant.parse("2024-02-15T10:30:00Z"),
        windowStart = null,
        windowEnd = null,
        lastUpdated = null,
        status = domainStatusGo,
        provider = domainProviderSpaceX,
        imageUrl = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launcher_images/falcon2520925_image_20230807133459.jpeg",
        thumbnailUrl = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launcher_images/falcon2520925_image_20230807133459.jpeg",
        infographic = null,
        netPrecision = null,
        mission = domainMissionStarlink,
        pad = domainPadSLC40,
        webcastLive = false
    )

    val domainLaunchULA = Launch(
        id = "def-456",
        name = "Atlas V 541 | GOES-U",
        slug = "atlas-v-mission",
        net = Instant.parse("2024-06-25T21:26:00Z"),
        windowStart = null,
        windowEnd = null,
        lastUpdated = null,
        status = domainStatusTBD,
        provider = domainProviderULA,
        imageUrl = null,
        thumbnailUrl = null,
        infographic = null,
        netPrecision = null,
        mission = domainMissionGOESU,
        pad = domainPadSLC41,
        webcastLive = false
    )

    val domainLaunchCrewMission = Launch(
        id = "ghi-789",
        name = "Falcon 9 Block 5 | Crew-8",
        slug = "falcon-9-crew-8",
        net = Instant.parse("2024-03-01T05:00:00Z"),
        windowStart = null,
        windowEnd = null,
        lastUpdated = null,
        status = domainStatusGo,
        provider = domainProviderSpaceX,
        imageUrl = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launcher_images/falcon2520925_image_20230807133459.jpeg",
        thumbnailUrl = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launcher_images/falcon2520925_image_20230807133459.jpeg",
        infographic = null,
        netPrecision = null,
        probability = 95,
        hashtag = "#Crew8",
        mission = domainMissionCrew,
        pad = domainPadLC39A,
        webcastLive = true
    )

    val pinnedContent = PinnedContent(
        type = PinnedContentType.LAUNCH,
        id = "abc-123",
        enabled = true,
        expiresAt = null,
        customMessage = null
    )

    val pinnedContentWithMessage = PinnedContent(
        type = PinnedContentType.LAUNCH,
        id = "abc-123",
        enabled = true,
        expiresAt = null,
        customMessage = "Don't miss this historic launch!"
    )

    val pinnedLaunchContent = PinnedLaunchContent(
        config = pinnedContent,
        launch = domainLaunchSpaceX
    )

    val pinnedLaunchContentWithMessage = PinnedLaunchContent(
        config = pinnedContentWithMessage,
        launch = domainLaunchSpaceX,
        customMessage = "Don't miss this historic launch!"
    )

    val motdContent = PinnedMotdContent(
        config = PinnedContent(
            type = PinnedContentType.MESSAGE_OF_THE_DAY,
            id = "motd-preview-001",
            enabled = true,
            expiresAt = null,
            customMessage = "Welcome back! SpaceLaunchNow now tracks 50,000+ launches. Explore the new history feature!"
        )
    )
}
