package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.Country
import me.calebjones.spacelaunchnow.api.launchlibrary.models.DockingEventForChaserNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.FirstStageNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.InfoURL
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LocationList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.MissionPatch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PadDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PayloadFlightSerializerNoLaunch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.RocketDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftFlightDetailedSerializerNoLaunch
import me.calebjones.spacelaunchnow.api.launchlibrary.models.TimelineEvent
import me.calebjones.spacelaunchnow.api.launchlibrary.models.UpdateEndpoint
import me.calebjones.spacelaunchnow.api.launchlibrary.models.VidURL
import me.calebjones.spacelaunchnow.domain.model.CrewMemberSummary
import me.calebjones.spacelaunchnow.domain.model.DockingLocationRef
import me.calebjones.spacelaunchnow.domain.model.InfoLink
import me.calebjones.spacelaunchnow.domain.model.LandingAttemptSummary
import me.calebjones.spacelaunchnow.domain.model.LandingLocationSummary
import me.calebjones.spacelaunchnow.domain.model.LandingTypeSummary
import me.calebjones.spacelaunchnow.domain.model.LauncherSummary
import me.calebjones.spacelaunchnow.domain.model.Location
import me.calebjones.spacelaunchnow.domain.model.MissionPatchSummary
import me.calebjones.spacelaunchnow.domain.model.Pad
import me.calebjones.spacelaunchnow.domain.model.PayloadSummary
import me.calebjones.spacelaunchnow.domain.model.ProgramSummary
import me.calebjones.spacelaunchnow.domain.model.Provider
import me.calebjones.spacelaunchnow.domain.model.ProviderDetail
import me.calebjones.spacelaunchnow.domain.model.RocketConfig
import me.calebjones.spacelaunchnow.domain.model.RocketDetail
import me.calebjones.spacelaunchnow.domain.model.RocketFamily
import me.calebjones.spacelaunchnow.domain.model.RocketManufacturer
import me.calebjones.spacelaunchnow.domain.model.RocketStage
import me.calebjones.spacelaunchnow.domain.model.SpaceStationRef
import me.calebjones.spacelaunchnow.domain.model.SpacecraftDockingEventSummary
import me.calebjones.spacelaunchnow.domain.model.SpacecraftFlightSummary
import me.calebjones.spacelaunchnow.domain.model.SpacecraftFlightVehicle
import me.calebjones.spacelaunchnow.domain.model.SpacecraftLandingSummary
import me.calebjones.spacelaunchnow.domain.model.SpacecraftStatus
import me.calebjones.spacelaunchnow.domain.model.TimelineEntry
import me.calebjones.spacelaunchnow.domain.model.UpdateEventRef
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus as DomainLaunchStatus
import me.calebjones.spacelaunchnow.domain.model.Mission as DomainMission
import me.calebjones.spacelaunchnow.domain.model.NetPrecision as DomainNetPrecision
import me.calebjones.spacelaunchnow.domain.model.Orbit as DomainOrbit
import me.calebjones.spacelaunchnow.domain.model.Update as DomainUpdate

// --- Agency / Provider mappers ---

fun AgencyMini.toDomain(): Provider = Provider(
    id = id,
    name = name,
    abbrev = abbrev,
    type = type?.name,
    countryCode = null,
    logoUrl = null,
    socialLogo = null,
    imageUrl = null
)

fun AgencyNormal.toDomain(): Provider = Provider(
    id = id,
    name = name,
    abbrev = abbrev,
    type = type?.name,
    countryCode = country.firstOrNull()?.toCountryCode(),
    logoUrl = logo?.imageUrl,
    socialLogo = socialLogo?.imageUrl,
    imageUrl = image?.imageUrl
)

fun AgencyDetailed.toDomain(): Provider = Provider(
    id = id,
    name = name,
    abbrev = abbrev,
    type = type?.name,
    countryCode = country.firstOrNull()?.toCountryCode(),
    socialLogo = socialLogo?.imageUrl,
    logoUrl = logo?.imageUrl,
    imageUrl = image?.imageUrl

)

fun AgencyDetailed.toProviderDetail(): ProviderDetail = ProviderDetail(
    description = description,
    administrator = administrator,
    foundingYear = foundingYear,
    totalLaunchCount = totalLaunchCount,
    successfulLaunches = successfulLaunches,
    failedLaunches = failedLaunches,
    pendingLaunches = pendingLaunches,
    consecutiveSuccessfulLaunches = consecutiveSuccessfulLaunches,
    successfulLandings = successfulLandings,
    failedLandings = failedLandings,
    attemptedLandings = attemptedLandings,
    consecutiveSuccessfulLandings = consecutiveSuccessfulLandings,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl
)

fun Country.toCountryCode(): String = alpha2Code ?: alpha3Code ?: name ?: ""

fun Country.toDomain(): me.calebjones.spacelaunchnow.domain.model.Country =
    me.calebjones.spacelaunchnow.domain.model.Country(
        id = id,
        name = name,
        alpha2Code = alpha2Code,
        alpha3Code = alpha3Code,
        nationalityName = nationalityName,
        nationalityNameComposed = nationalityNameComposed
    )

// --- Pad & Location mappers ---

fun PadDetailed.toDomain(): Pad = Pad(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    mapUrl = mapUrl,
    mapImage = mapImage,
    totalLaunchCount = totalLaunchCount,
    location = location?.toDomain(),
    imageUrl = image?.imageUrl,
    description = description,
    fastestTurnaround = fastestTurnaround,
    orbitalLaunchAttemptCount = orbitalLaunchAttemptCount,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl
)

fun LocationList.toDomain(): Location = Location(
    id = id,
    name = name,
    countryCode = country?.toCountryCode(),
    countryName = country?.name,
    countryAlpha2 = country?.alpha2Code,
    celestialBodyName = celestialBody.name,
    imageUrl = image?.imageUrl,
    mapImage = mapImage,
    timezoneName = timezoneName,
    description = description
)

// --- Mission & Orbit ---

fun me.calebjones.spacelaunchnow.api.launchlibrary.models.Mission.toDomain(): DomainMission =
    DomainMission(
        id = id,
        name = name,
        description = description,
        type = type,
        orbit = orbit?.toDomain(),
        imageUrl = image?.imageUrl
    )

fun me.calebjones.spacelaunchnow.api.launchlibrary.models.Orbit.toDomain(): DomainOrbit =
    DomainOrbit(
        id = id,
        name = name,
        abbrev = abbrev
    )

// --- LaunchStatus & NetPrecision ---

fun me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchStatus.toDomain(): DomainLaunchStatus =
    DomainLaunchStatus(
        id = id,
        name = name,
        abbrev = abbrev,
        description = description
    )

fun me.calebjones.spacelaunchnow.api.launchlibrary.models.NetPrecision.toDomain(): DomainNetPrecision =
    DomainNetPrecision(
        id = id,
        name = name,
        abbrev = abbrev,
        description = description
    )

// --- Media types ---

fun VidURL.toDomain(): VideoLink = VideoLink(
    url = url,
    title = title,
    source = source,
    publisher = publisher,
    description = description,
    featureImage = featureImage,
    live = live,
    priority = priority
)

fun InfoURL.toDomain(): InfoLink = InfoLink(
    url = url,
    title = title,
    source = source,
    description = description,
    featureImage = featureImage,
    type = type?.name,
    priority = priority
)

fun MissionPatch.toDomain(): MissionPatchSummary = MissionPatchSummary(
    id = id,
    name = name,
    imageUrl = imageUrl,
    priority = priority
)

fun TimelineEvent.toDomain(): TimelineEntry = TimelineEntry(
    type = type?.abbrev,
    relativeTime = relativeTime
)

fun me.calebjones.spacelaunchnow.api.launchlibrary.models.Update.toDomain(): DomainUpdate =
    DomainUpdate(
        id = id,
        profileImage = profileImage,
        comment = comment,
        infoUrl = infoUrl,
        createdBy = createdBy,
        createdOn = createdOn
    )

// --- Program ---

fun ProgramMini.toDomain(): ProgramSummary = ProgramSummary(
    id = id,
    name = name,
    imageUrl = image?.imageUrl,
    description = null,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    type = null
)

fun ProgramNormal.toDomain(): ProgramSummary = ProgramSummary(
    id = id,
    name = name,
    imageUrl = image?.imageUrl,
    description = description,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    type = type.name
)

// --- Rocket config ---

fun LauncherConfigList.toDomain(): RocketConfig = RocketConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = families?.firstOrNull()?.name,
    variant = variant,
    imageUrl = null,
    active = null,
    reusable = null
)

fun LauncherConfigDetailed.toDomain(): RocketConfig = RocketConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = families?.firstOrNull()?.name,
    variant = variant,
    imageUrl = image?.imageUrl,
    active = active,
    reusable = reusable,
    description = description,
    alias = alias,
    families = families?.map { RocketFamily(it.id, it.name) } ?: emptyList(),
    manufacturer = manufacturer?.let { RocketManufacturer(it.id, it.name) },
    minStage = minStage,
    maxStage = maxStage,
    length = length,
    diameter = diameter,
    launchMass = launchMass,
    leoCapacity = leoCapacity,
    gtoCapacity = gtoCapacity,
    geoCapacity = geoCapacity,
    ssoCapacity = ssoCapacity,
    toThrust = toThrust,
    apogee = apogee,
    launchCost = launchCost,
    totalLaunchCount = totalLaunchCount,
    successfulLaunches = successfulLaunches,
    failedLaunches = failedLaunches,
    pendingLaunches = pendingLaunches,
    consecutiveSuccessfulLaunches = consecutiveSuccessfulLaunches,
    attemptedLandings = attemptedLandings,
    successfulLandings = successfulLandings,
    failedLandings = failedLandings,
    consecutiveSuccessfulLandings = consecutiveSuccessfulLandings,
    maidenFlight = maidenFlight,
    fastestTurnaround = fastestTurnaround,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl
)

// --- Rocket detail ---

fun RocketDetailed.toDomain(): RocketDetail = RocketDetail(
    stages = launcherStage.map { it.toDomain() },
    spacecraftFlights = spacecraftStage.map { it.toDomain() },
    payloads = payloads.map { it.toDomain() }
)

fun FirstStageNormal.toDomain(): RocketStage = RocketStage(
    id = id,
    type = type,
    reused = reused,
    launcherFlightNumber = launcherFlightNumber,
    launcher = LauncherSummary(
        id = launcher.id,
        serialNumber = launcher.serialNumber,
        flightProven = launcher.flightProven ?: false,
        imageUrl = launcher.image?.imageUrl
    ),
    landingAttempt = landing?.let {
        LandingAttemptSummary(
            id = it.id,
            attempt = it.attempt,
            success = it.success,
            downrangeDistance = it.downrangeDistance,
            landingLocation = it.landingLocation?.let { loc ->
                LandingLocationSummary(id = loc.id, name = loc.name)
            },
            outcome = when (it.success) {
                true -> "Success"
                false -> "Failure"
                null -> null
            },
            description = it.description,
            location = it.landingLocation?.name,
            type = it.type?.name
        )
    },
    previousFlightDate = previousFlightDate,
    turnAroundTime = turnAroundTime
)

fun DockingEventForChaserNormal.toDomainSummary(): SpacecraftDockingEventSummary =
    SpacecraftDockingEventSummary(
        id = id,
        docking = docking,
        departure = departure,
        dockingLocation = DockingLocationRef(id = dockingLocation.id, name = dockingLocation.name),
        spaceStationTarget = spaceStationTarget?.let { SpaceStationRef(id = it.id, name = it.name) }
    )

fun SpacecraftFlightDetailedSerializerNoLaunch.toDomain(): SpacecraftFlightSummary =
    SpacecraftFlightSummary(
        id = id,
        serialNumber = spacecraft.serialNumber,
        spacecraftName = spacecraft.name,
        destination = destination,
        missionEnd = missionEnd,
        spacecraft = SpacecraftFlightVehicle(
            id = spacecraft.id,
            name = spacecraft.name,
            status = spacecraft.status.let { SpacecraftStatus(it.id, it.name) },
            serialNumber = spacecraft.serialNumber,
            imageUrl = spacecraft.image?.imageUrl,
            description = spacecraft.description,
            inSpace = spacecraft.inSpace,
            isPlaceholder = spacecraft.isPlaceholder,
            flightsCount = spacecraft.flightsCount,
            missionEndsCount = spacecraft.missionEndsCount,
            timeInSpace = spacecraft.timeInSpace,
            timeDocked = spacecraft.timeDocked,
            fastestTurnaround = spacecraft.fastestTurnaround
        ),
        duration = duration,
        turnAroundTime = turnAroundTime,
        landing = landing?.let { l ->
            SpacecraftLandingSummary(
                type = l.type?.let { LandingTypeSummary(it.id, it.name) },
                landingLocation = l.landingLocation?.let { LandingLocationSummary(it.id, it.name) }
            )
        },
        dockingEvents = dockingEvents.map { it.toDomainSummary() },
        launchCrew = launchCrew.map { af ->
            CrewMemberSummary(
                astronautId = af.astronaut.id,
                astronautName = af.astronaut.name,
                imageUrl = af.astronaut.image?.imageUrl,
                role = af.role?.role
            )
        },
        onboardCrew = onboardCrew.map { af ->
            CrewMemberSummary(
                astronautId = af.astronaut.id,
                astronautName = af.astronaut.name,
                imageUrl = af.astronaut.image?.imageUrl,
                role = af.role?.role
            )
        },
        landingCrew = landingCrew.map { af ->
            CrewMemberSummary(
                astronautId = af.astronaut.id,
                astronautName = af.astronaut.name,
                imageUrl = af.astronaut.image?.imageUrl,
                role = af.role?.role
            )
        }
    )

fun PayloadFlightSerializerNoLaunch.toDomain(): PayloadSummary = PayloadSummary(
    id = payload.id,
    name = payload.name,
    description = payload.description
)

fun UpdateEndpoint.toDomain(): DomainUpdate = DomainUpdate(
    id = id,
    profileImage = profileImage,
    comment = comment,
    infoUrl = infoUrl,
    createdBy = createdBy,
    createdOn = createdOn,
    launch = launch?.toDomain(),
    event = event?.let { UpdateEventRef(id = it.id, name = it.name) },
    program = program?.toDomain()
)
