package me.calebjones.spacelaunchnow.ui.preview

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.CelestialBodyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Image
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ImageLicense
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LocationList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PadDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramMini
import kotlin.time.Instant

/**
 * Centralized mock data for Compose previews.
 * Provides consistent, reusable mock objects to avoid duplication across preview functions.
 */
object PreviewData {

    // ========================================
    // Common Components
    // ========================================

    val imageLicense = ImageLicense(
        id = 1,
        name = "Public Domain"
    )

    val commercialAgencyType = AgencyType(
        id = 3,
        name = "Commercial"
    )

    val governmentAgencyType = AgencyType(
        id = 1,
        name = "Government"
    )

    val countryUSA = Country(
        id = 1,
        name = "USA",
        alpha2Code = "US",
        alpha3Code = "USA"
    )

    val countryCHN = Country(
        id = 2,
        name = "China",
        alpha2Code = "CN",
        alpha3Code = "CHN"
    )

    // ========================================
    // Celestial Bodies
    // ========================================

    val celestialBodyEarth = CelestialBodyMini(
        responseMode = "list",
        id = 3,
        name = "Earth"
    )

    // ========================================
    // Locations
    // ========================================

    val locationKennedySpaceCenter = LocationList(
        responseMode = "list",
        id = 27,
        url = "https://ll.thespacedevs.com/2.4.0/location/27/",
        celestialBody = celestialBodyEarth,
        country = countryUSA,
        image = null,
        name = "Kennedy Space Center, FL, USA",
        description = "Kennedy Space Center Launch Complex",
        mapImage = null,
        timezoneName = "America/New_York"
    )

    val locationCapeCanaveral = LocationList(
        responseMode = "list",
        id = 12,
        url = "https://ll.thespacedevs.com/2.4.0/location/12/",
        celestialBody = celestialBodyEarth,
        country = countryUSA,
        image = null,
        name = "Cape Canaveral SFS, FL, USA",
        description = "Cape Canaveral Space Force Station",
        mapImage = null,
        timezoneName = "America/New_York"
    )

    // ========================================
    // Images
    // ========================================

    val falcon9Image = Image(
        id = 1,
        name = "Falcon 9 Launch",
        imageUrl = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launcher_images/falcon2520925_image_20230807133459.jpeg",
        thumbnailUrl = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launcher_images/falcon2520925_image_20230807133459.jpeg",
        credit = null,
        license = imageLicense,
        variants = emptyList(),
        singleUse = null
    )

    val spaceXLogo = Image(
        id = 2,
        name = "SpaceX Logo",
        imageUrl = "https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/spacex_image_20190207032501.jpeg",
        thumbnailUrl = "https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/spacex_image_20190207032501.jpeg",
        credit = null,
        license = imageLicense,
        variants = emptyList(),
        singleUse = null
    )

    // ========================================
    // Launch Statuses
    // ========================================

    val statusGo = LaunchStatus(
        id = 1,
        name = "Go for Launch",
        abbrev = "Go",
        description = "Launch is proceeding as scheduled"
    )

    val statusTBD = LaunchStatus(
        id = 2,
        name = "To Be Determined",
        abbrev = "TBD",
        description = "Launch date to be determined"
    )

    val statusSuccess = LaunchStatus(
        id = 3,
        name = "Launch Successful",
        abbrev = "Success",
        description = "Launch was successful"
    )

    val statusFailure = LaunchStatus(
        id = 4,
        name = "Launch Failure",
        abbrev = "Failure",
        description = "Launch failed"
    )

    // ========================================
    // Agencies
    // ========================================

    val agencySpaceXMini = AgencyMini(
        responseMode = "list",
        id = 121,
        url = "https://ll.thespacedevs.com/2.4.0/agencies/121/",
        name = "SpaceX",
        type = commercialAgencyType,
        abbrev = "SpX"
    )

    val agencySpaceXNormal = AgencyNormal(
        responseMode = "normal",
        id = 121,
        url = "https://ll.thespacedevs.com/2.4.0/agencies/121/",
        name = "SpaceX",
        type = commercialAgencyType,
        country = listOf(countryUSA),
        parent = null,
        image = null,
        logo = null,
        socialLogo = spaceXLogo,
        abbrev = "SpX",
        administrator = null,
        foundingYear = null,
        description = null,
        featured = null,
        launchers = null,
        spacecraft = null
    )

    val agencyULAMini = AgencyMini(
        responseMode = "list",
        id = 124,
        url = "https://ll.thespacedevs.com/2.4.0/agencies/124/",
        name = "United Launch Alliance",
        type = commercialAgencyType,
        abbrev = "ULA"
    )

    val agencyULANormal = AgencyNormal(
        responseMode = "normal",
        id = 124,
        url = "https://ll.thespacedevs.com/2.4.0/agencies/124/",
        name = "United Launch Alliance",
        type = commercialAgencyType,
        country = listOf(countryUSA),
        parent = null,
        image = null,
        logo = null,
        socialLogo = null,
        abbrev = "ULA",
        administrator = null,
        foundingYear = null,
        description = null,
        featured = null,
        launchers = null,
        spacecraft = null
    )

    val agencyNASAMini = AgencyMini(
        responseMode = "list",
        id = 44,
        url = "https://ll.thespacedevs.com/2.4.0/agencies/44/",
        name = "National Aeronautics and Space Administration",
        type = governmentAgencyType,
        abbrev = "NASA"
    )

    // ========================================
    // Pads
    // ========================================

    val padLC39A = PadDetailed(
        id = 87,
        url = "https://ll.thespacedevs.com/2.4.0/pad/87/",
        agencies = listOf(agencySpaceXNormal),
        image = null,
        description = "Launch Complex 39A is a launch pad at Kennedy Space Center",
        infoUrl = null,
        wikiUrl = "https://en.wikipedia.org/wiki/Kennedy_Space_Center_Launch_Complex_39A",
        mapUrl = null,
        latitude = 28.60822681,
        longitude = -80.60428186,
        country = countryUSA,
        mapImage = null,
        totalLaunchCount = 200,
        orbitalLaunchAttemptCount = 195,
        fastestTurnaround = "51 days, 0:02:11",
        location = locationKennedySpaceCenter,
        name = "Launch Complex 39A",
        active = true
    )

    val padSLC40 = PadDetailed(
        id = 16,
        url = "https://ll.thespacedevs.com/2.4.0/pad/16/",
        agencies = listOf(agencySpaceXNormal),
        image = null,
        description = "Space Launch Complex 40 at Cape Canaveral Space Force Station",
        infoUrl = null,
        wikiUrl = "https://en.wikipedia.org/wiki/Cape_Canaveral_Space_Launch_Complex_40",
        mapUrl = null,
        latitude = 28.56194122,
        longitude = -80.57735736,
        country = countryUSA,
        mapImage = null,
        totalLaunchCount = 150,
        orbitalLaunchAttemptCount = 145,
        fastestTurnaround = "34 days, 12:45:00",
        location = locationCapeCanaveral,
        name = "Space Launch Complex 40",
        active = true
    )

    val padSLC41 = PadDetailed(
        id = 25,
        url = "https://ll.thespacedevs.com/2.4.0/pad/25/",
        agencies = listOf(agencyULANormal),
        image = null,
        description = "Space Launch Complex 41 at Cape Canaveral Space Force Station",
        infoUrl = null,
        wikiUrl = "https://en.wikipedia.org/wiki/Cape_Canaveral_Space_Launch_Complex_41",
        mapUrl = null,
        latitude = 28.58341025,
        longitude = -80.58303644,
        country = countryUSA,
        mapImage = null,
        totalLaunchCount = 100,
        orbitalLaunchAttemptCount = 98,
        fastestTurnaround = "120 days, 0:00:00",
        location = locationCapeCanaveral,
        name = "Space Launch Complex 41",
        active = true
    )

    // ========================================
    // Missions
    // ========================================

    val missionStarlink = Mission(
        id = 1,
        name = "Starlink Group 6-34",
        type = "Communications",
        image = null,
        orbit = null,
        agencies = emptyList(),
        infoUrls = emptyList(),
        vidUrls = emptyList(),
        description = "Deployment of Starlink satellites"
    )

    val missionGOESU = Mission(
        id = 2,
        name = "GOES-U",
        type = "Earth Science",
        image = null,
        orbit = null,
        agencies = emptyList(),
        infoUrls = emptyList(),
        vidUrls = emptyList(),
        description = "Geostationary weather satellite"
    )

    val missionCrewDragon = Mission(
        id = 3,
        name = "Crew-8",
        type = "Human Exploration",
        image = null,
        orbit = null,
        agencies = emptyList(),
        infoUrls = emptyList(),
        vidUrls = emptyList(),
        description = "SpaceX Crew Dragon mission to ISS"
    )

    // ========================================
    // Programs
    // ========================================

    val programStarlink = ProgramMini(
        id = 25,
        url = "https://ll.thespacedevs.com/2.4.0/program/25/",
        name = "Starlink",
        image = null,
        infoUrl = null,
        wikiUrl = null
    )

    val programISS = ProgramMini(
        id = 17,
        url = "https://ll.thespacedevs.com/2.4.0/program/17/",
        name = "International Space Station",
        image = null,
        infoUrl = null,
        wikiUrl = null
    )

    // ========================================
    // Launch Objects - Basic
    // ========================================

    val launchBasicSpaceX = LaunchBasic(
        id = "abc-123",
        url = "https://ll.thespacedevs.com/2.4.0/launch/abc-123/",
        responseMode = "list",
        slug = "falcon-9-starlink",
        launchDesignator = "F9-001",
        status = statusGo,
        netPrecision = null,
        image = falcon9Image,
        launchServiceProvider = agencySpaceXMini,
        infographic = null,
        locationName = "Kennedy Space Center, FL, USA",
        name = "Falcon 9 Block 5 | Starlink Group 6-34",
        lastUpdated = null,
        net = Instant.parse("2024-02-15T10:30:00Z"),
        windowEnd = null,
        windowStart = null
    )

    val launchBasicULA = LaunchBasic(
        id = "def-456",
        url = "https://ll.thespacedevs.com/2.4.0/launch/def-456/",
        responseMode = "list",
        slug = "atlas-v-mission",
        launchDesignator = "AV-095",
        status = statusTBD,
        netPrecision = null,
        image = null,
        launchServiceProvider = agencyULAMini,
        infographic = null,
        locationName = "Cape Canaveral SFS, FL, USA",
        name = "Atlas V 541 | GOES-U",
        lastUpdated = null,
        net = Instant.parse("2024-06-25T21:26:00Z"),
        windowEnd = null,
        windowStart = null
    )

    // ========================================
    // Launch Objects - Normal
    // ========================================

    val launchNormalSpaceX = LaunchNormal(
        id = "abc-123",
        url = "https://ll.thespacedevs.com/2.4.0/launch/abc-123/",
        responseMode = "normal",
        slug = "falcon-9-starlink",
        launchDesignator = "F9-001",
        status = statusGo,
        netPrecision = null,
        image = falcon9Image,
        launchServiceProvider = agencySpaceXNormal,
        infographic = null,
        probability = null,
        weatherConcerns = null,
        failreason = null,
        hashtag = null,
        rocket = null,
        mission = missionStarlink,
        pad = padSLC40,
        program = listOf(programStarlink),
        orbitalLaunchAttemptCount = null,
        locationLaunchAttemptCount = null,
        padLaunchAttemptCount = null,
        agencyLaunchAttemptCount = null,
        orbitalLaunchAttemptCountYear = null,
        locationLaunchAttemptCountYear = null,
        padLaunchAttemptCountYear = null,
        agencyLaunchAttemptCountYear = null,
        name = "Falcon 9 Block 5 | Starlink Group 6-34",
        lastUpdated = null,
        net = Instant.parse("2024-02-15T10:30:00Z"),
        windowEnd = null,
        windowStart = null,
        webcastLive = false
    )

    val launchNormalULA = LaunchNormal(
        id = "def-456",
        url = "https://ll.thespacedevs.com/2.4.0/launch/def-456/",
        responseMode = "normal",
        slug = "atlas-v-mission",
        launchDesignator = "AV-095",
        status = statusTBD,
        netPrecision = null,
        image = null,
        launchServiceProvider = agencyULANormal,
        infographic = null,
        probability = null,
        weatherConcerns = null,
        failreason = null,
        hashtag = null,
        rocket = null,
        mission = missionGOESU,
        pad = padSLC41,
        program = null,
        orbitalLaunchAttemptCount = null,
        locationLaunchAttemptCount = null,
        padLaunchAttemptCount = null,
        agencyLaunchAttemptCount = null,
        orbitalLaunchAttemptCountYear = null,
        locationLaunchAttemptCountYear = null,
        padLaunchAttemptCountYear = null,
        agencyLaunchAttemptCountYear = null,
        name = "Atlas V 541 | GOES-U",
        lastUpdated = null,
        net = Instant.parse("2024-06-25T21:26:00Z"),
        windowEnd = null,
        windowStart = null,
        webcastLive = false
    )

    val launchNormalCrewMission = LaunchNormal(
        id = "ghi-789",
        url = "https://ll.thespacedevs.com/2.4.0/launch/ghi-789/",
        responseMode = "normal",
        slug = "falcon-9-crew-8",
        launchDesignator = "F9-002",
        status = statusGo,
        netPrecision = null,
        image = falcon9Image,
        launchServiceProvider = agencySpaceXNormal,
        infographic = null,
        probability = 95,
        weatherConcerns = null,
        failreason = null,
        hashtag = "#Crew8",
        rocket = null,
        mission = missionCrewDragon,
        pad = padLC39A,
        program = listOf(programISS),
        orbitalLaunchAttemptCount = null,
        locationLaunchAttemptCount = null,
        padLaunchAttemptCount = null,
        agencyLaunchAttemptCount = null,
        orbitalLaunchAttemptCountYear = null,
        locationLaunchAttemptCountYear = null,
        padLaunchAttemptCountYear = null,
        agencyLaunchAttemptCountYear = null,
        name = "Falcon 9 Block 5 | Crew-8",
        lastUpdated = null,
        net = Instant.parse("2024-03-01T05:00:00Z"),
        windowEnd = null,
        windowStart = null,
        webcastLive = true
    )

    // ========================================
    // Astronauts
    // ========================================

    val astronautStatusActive = AstronautStatus(
        id = 1,
        name = "Active"
    )

    val astronautNormal = AstronautNormal(
        id = 1,
        url = "https://ll.thespacedevs.com/2.4.0/astronaut/1/",
        responseMode = "normal",
        name = "Sunita Williams",
        status = astronautStatusActive,
        agency = agencyNASAMini,
        image = null,
        age = 58,
        bio = "Sunita Williams is a NASA astronaut who holds the record for longest spaceflight by a woman.",
        type = AstronautType(
            id = 1,
            name = "Human"
        ),
        nationality = listOf(countryUSA)
    )
}

