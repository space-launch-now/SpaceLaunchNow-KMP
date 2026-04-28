package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.CelestialBodyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventEndpointNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.EventType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Image
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ImageLicense
import me.calebjones.spacelaunchnow.api.launchlibrary.models.InfoURL
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Language
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigFamilyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LocationList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.MissionPatch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.NetPrecision
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Orbit
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PadDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.RocketNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEventType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Update
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURLType
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission as ApiMission

internal fun testImage(
    id: Int = 1,
    name: String = "test_image",
    imageUrl: String = "https://example.com/image.jpg",
    thumbnailUrl: String = "https://example.com/thumb.jpg"
) = Image(
    id = id,
    name = name,
    imageUrl = imageUrl,
    thumbnailUrl = thumbnailUrl,
    credit = null,
    license = ImageLicense(id = 1),
    variants = emptyList()
)

internal fun testAgencyType(id: Int = 1, name: String = "Government") =
    AgencyType(id = id, name = name)

internal fun testAgencyMini(
    id: Int = 44,
    name: String = "SpaceX",
    abbrev: String? = "SpX",
    type: AgencyType? = testAgencyType()
) = AgencyMini(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/agencies/$id/",
    name = name,
    type = type,
    abbrev = abbrev
)

internal fun testAgencyNormal(
    id: Int = 44,
    name: String = "SpaceX",
    abbrev: String? = "SpX",
    type: AgencyType? = testAgencyType()
) = AgencyNormal(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/agencies/$id/",
    name = name,
    type = type,
    country = listOf(testCountry()),
    parent = null,
    image = null,
    logo = null,
    socialLogo = null,
    abbrev = abbrev
)

internal fun testLaunchStatus(
    id: Int = 1,
    name: String = "Go for Launch",
    abbrev: String? = "Go"
) = LaunchStatus(id = id, name = name, abbrev = abbrev)

internal fun testNetPrecision(
    id: Int = 1,
    name: String = "Minute",
    abbrev: String? = "MIN"
) = NetPrecision(id = id, name = name, abbrev = abbrev)

internal fun testCountry(
    id: Int = 1,
    name: String = "United States",
    alpha2Code: String? = "US"
) = Country(id = id, name = name, alpha2Code = alpha2Code)

internal fun testCelestialBody(id: Int = 1, name: String = "Earth") =
    CelestialBodyMini(id = id, name = name)

internal fun testLocationList(
    id: Int = 1,
    name: String? = "Kennedy Space Center, FL, USA",
    country: Country? = testCountry()
) = LocationList(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/location/$id/",
    celestialBody = testCelestialBody(),
    country = country,
    image = null,
    name = name
)

internal fun testPadDetailed(
    id: Int = 1,
    name: String? = "Launch Complex 39A",
    latitude: Double? = 28.6,
    longitude: Double? = -80.6
) = PadDetailed(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/pad/$id/",
    agencies = emptyList(),
    image = null,
    description = null,
    infoUrl = null,
    wikiUrl = null,
    mapUrl = "https://maps.google.com",
    latitude = latitude,
    longitude = longitude,
    country = testCountry(),
    mapImage = null,
    totalLaunchCount = 50,
    orbitalLaunchAttemptCount = 40,
    fastestTurnaround = null,
    location = testLocationList(),
    name = name
)

internal fun testOrbit(id: Int = 1, name: String = "Low Earth Orbit", abbrev: String = "LEO") =
    Orbit(id = id, name = name, abbrev = abbrev, celestialBody = testCelestialBody())

internal fun testMission(
    id: Int = 1,
    name: String = "Starlink Group 6-1",
    type: String = "Communications",
    description: String? = "A batch of Starlink satellites"
) = ApiMission(
    id = id,
    name = name,
    type = type,
    image = null,
    orbit = testOrbit(),
    agencies = emptyList(),
    infoUrls = emptyList(),
    vidUrls = emptyList(),
    description = description
)

internal fun testLanguage(id: Int = 1, name: String = "English", code: String = "en") =
    Language(id = id, name = name, code = code)

internal fun testVidURL(
    url: String = "https://youtube.com/watch?v=test",
    title: String? = "Launch Webcast",
    live: Boolean? = true
) = VidURL(
    url = url,
    type = VidURLType(id = 1, name = "YouTube"),
    language = testLanguage(),
    title = title,
    live = live
)

internal fun testInfoURL(
    url: String = "https://example.com/info",
    title: String? = "Launch Info"
) = InfoURL(
    url = url,
    type = null,
    language = null,
    title = title
)

internal fun testUpdate(
    id: Int = 1,
    comment: String? = "Launch is on track"
) = Update(
    id = id,
    profileImage = null,
    comment = comment,
    infoUrl = null,
    createdBy = "Admin"
)

internal fun testTimelineEventType(id: Int = 1, abbrev: String = "LIFTOFF") =
    TimelineEventType(id = id, abbrev = abbrev)

internal fun testTimelineEvent(
    type: TimelineEventType? = testTimelineEventType(),
    relativeTime: String? = "T+0s"
) = TimelineEvent(type = type, relativeTime = relativeTime)

internal fun testMissionPatch(
    id: Int = 1,
    name: String = "Mission Patch",
    imageUrl: String = "https://example.com/patch.png"
) = MissionPatch(
    id = id,
    name = name,
    imageUrl = imageUrl,
    agency = null
)

internal fun testLauncherConfigFamilyMini(id: Int = 1, name: String = "Falcon") =
    LauncherConfigFamilyMini(id = id, name = name)

internal fun testLauncherConfigList(
    id: Int = 1,
    name: String = "Falcon 9 Block 5",
    fullName: String? = "Falcon 9 Block 5"
) = LauncherConfigList(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/config/launcher/$id/",
    name = name,
    families = listOf(testLauncherConfigFamilyMini()),
    fullName = fullName,
    variant = "Block 5"
)

internal fun testRocketNormal(
    id: Int = 1,
    configuration: LauncherConfigList = testLauncherConfigList()
) = RocketNormal(id = id, configuration = configuration)

internal fun testProgramMini(
    id: Int = 1,
    name: String = "ISS Program"
) = ProgramMini(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/program/$id/",
    name = name,
    image = null,
    infoUrl = null,
    wikiUrl = null
)

internal fun testEventType(id: Int = 1, name: String? = "Launch") =
    EventType(id = id, name = name)

internal fun testLaunchBasic(
    id: String = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    name: String? = "SpaceX | Falcon 9 Block 5",
    slug: String = "spacex-falcon-9-block-5",
    locationName: String? = "Kennedy Space Center, FL, USA"
) = LaunchBasic(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/launch/$id/",
    slug = slug,
    launchDesignator = null,
    status = testLaunchStatus(),
    netPrecision = testNetPrecision(),
    image = testImage(),
    launchServiceProvider = testAgencyMini(),
    infographic = null,
    locationName = locationName,
    name = name
)

internal fun testEventEndpointNormal(
    id: Int = 1,
    name: String = "Crew Dragon Docking",
    slug: String = "crew-dragon-docking"
) = EventEndpointNormal(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/event/$id/",
    name = name,
    infoUrls = listOf(testInfoURL()),
    vidUrls = listOf(testVidURL()),
    image = testImage(),
    slug = slug,
    type = testEventType(),
    datePrecision = testNetPrecision(),
    duration = "PT1H30M",
    updates = listOf(testUpdate())
)

internal fun testEventEndpointDetailed(
    id: Int = 1,
    name: String = "Crew Dragon Docking",
    slug: String = "crew-dragon-docking"
) = EventEndpointDetailed(
    id = id,
    url = "https://ll.thespacedevs.com/2.4.0/event/$id/",
    name = name,
    infoUrls = listOf(testInfoURL()),
    vidUrls = listOf(testVidURL()),
    image = testImage(),
    slug = slug,
    type = testEventType(),
    datePrecision = testNetPrecision(),
    duration = "PT1H30M",
    updates = listOf(testUpdate()),
    agencies = listOf(testAgencyMini()),
    launches = listOf(testLaunchBasic()),
    expeditions = emptyList(),
    spacestations = emptyList(),
    program = null,
    astronauts = null
)
