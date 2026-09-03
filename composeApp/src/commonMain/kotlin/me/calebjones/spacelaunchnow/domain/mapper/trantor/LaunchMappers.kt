package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AgencySummary
import me.calebjones.spacelaunchnow.api.trantor.models.InfoUrl
import me.calebjones.spacelaunchnow.api.trantor.models.LandingSummary
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchDetail
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchList
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchUpdate
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.MissionPatchSchema
import me.calebjones.spacelaunchnow.api.trantor.models.PadSummary
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLaunchList
import me.calebjones.spacelaunchnow.api.trantor.models.Rocket
import me.calebjones.spacelaunchnow.api.trantor.models.StageExpanded
import me.calebjones.spacelaunchnow.api.trantor.models.TimelineEvent
import me.calebjones.spacelaunchnow.api.trantor.models.VidUrl
import me.calebjones.spacelaunchnow.domain.model.InfoLink
import me.calebjones.spacelaunchnow.domain.model.LandingAttemptSummary
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.Location
import me.calebjones.spacelaunchnow.domain.model.MissionPatchSummary
import me.calebjones.spacelaunchnow.domain.model.Pad
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Provider
import me.calebjones.spacelaunchnow.domain.model.ProviderDetail
import me.calebjones.spacelaunchnow.domain.model.RocketConfig
import me.calebjones.spacelaunchnow.domain.model.RocketDetail
import me.calebjones.spacelaunchnow.domain.model.RocketStage
import me.calebjones.spacelaunchnow.domain.model.TimelineEntry
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.domain.model.LauncherSummary as DomainLauncherSummary
import me.calebjones.spacelaunchnow.domain.model.Mission as DomainMission
import me.calebjones.spacelaunchnow.domain.model.Update as DomainUpdate

/*
 * Trantor -> domain mappers for the launch surface (LaunchList/LaunchDetail and their
 * embedded sub-objects). Mirrors the shape of the LL mappers in LaunchMappers.kt /
 * CommonMappers.kt.
 *
 * Known, deliberate fidelity gaps versus Trantor's flatter schema (see the phase5-launch
 * unit report for the full rationale - not fabricated, just left null/empty since the
 * source field doesn't exist):
 *  - Mission.orbit: Trantor's embedded Mission carries only orbit/orbit_name strings, no
 *    orbit id, so the domain Orbit(id, name, abbrev) can't be built without inventing an
 *    id. Left null.
 *  - Launch.netPrecision: Trantor's LaunchDetail.net_precision is a plain string, not an
 *    id-bearing object. Left null.
 *  - Launch.programs: Trantor's LaunchDetail has no embedded program list (the junction
 *    table exists server-side, but detail doesn't inline it yet). Left empty.
 *  - Launch.launchAttemptCounts: not served by Trantor's launch detail/list. Left null.
 *  - RocketDetail.spacecraftFlights / .payloads: not served by Trantor's Rocket object.
 *    Left empty.
 *  - LandingAttemptSummary.landingLocation / downrangeDistance: Trantor's LandingSummary
 *    gives only a landing_location_name string, no id. landingLocation left null; the name
 *    is preserved in the `location` field instead.
 */

private fun flatProvider(providerId: Int?, providerName: String?): Provider = Provider(
    id = providerId ?: 0,
    name = providerName ?: "Unknown",
    abbrev = null,
    type = null,
    countryCode = null,
    logoUrl = null,
    socialLogo = null,
    imageUrl = null
)

fun LaunchList.toDomain(): Launch = Launch(
    id = id,
    name = name,
    slug = slug,
    net = net,
    windowStart = null,
    windowEnd = null,
    lastUpdated = null,
    status = LaunchStatus(id = statusId, name = status, abbrev = null, description = null),
    provider = flatProvider(providerId, providerName),
    imageUrl = imageUrl,
    thumbnailUrl = null,
    infographic = null,
    netPrecision = null,
    rocket = rocketId?.let { rid ->
        RocketConfig(
            id = rid,
            name = configurationName ?: "",
            fullName = null,
            family = null,
            variant = null,
            imageUrl = null,
            active = null,
            reusable = null
        )
    },
    mission = missionId?.let { mid ->
        DomainMission(
            id = mid,
            name = missionName ?: "",
            description = null,
            type = null,
            orbit = null,
            imageUrl = null
        )
    },
    pad = padId?.let { pid ->
        Pad(
            id = pid,
            name = padName,
            latitude = null,
            longitude = null,
            mapUrl = null,
            mapImage = null,
            totalLaunchCount = null,
            location = null
        )
    },
    webcastLive = webcastLive ?: false
)

fun PaginatedResponseLaunchList.toDomain(): PaginatedResult<Launch> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

fun AgencySummary.toDomain(): Provider = Provider(
    id = id,
    name = name,
    abbrev = abbrev,
    type = agencyType,
    countryCode = countryCodes?.firstOrNull(),
    logoUrl = logoUrl,
    socialLogo = null,
    imageUrl = imageUrl
)

fun AgencySummary.toProviderDetail(): ProviderDetail = ProviderDetail(
    description = null,
    administrator = null,
    foundingYear = null,
    totalLaunchCount = totalLaunchCount,
    successfulLaunches = successfulLaunches,
    failedLaunches = failedLaunches,
    pendingLaunches = null,
    consecutiveSuccessfulLaunches = null,
    successfulLandings = null,
    failedLandings = null,
    attemptedLandings = null,
    consecutiveSuccessfulLandings = null,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl
)

fun LauncherConfigSummary.toDomain(): RocketConfig = RocketConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = null,
    variant = variant,
    imageUrl = imageUrl,
    active = active,
    reusable = reusable,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl
)

fun Rocket.toDomain(): RocketDetail = RocketDetail(
    stages = stages?.map { it.toDomain() } ?: emptyList(),
    spacecraftFlights = emptyList(),
    payloads = emptyList()
)

fun StageExpanded.toDomain(): RocketStage = RocketStage(
    id = id,
    type = type,
    reused = reused,
    launcherFlightNumber = launcherFlightNumber,
    launcher = launcher?.toDomain(),
    landingAttempt = landing?.toDomain(),
    previousFlightDate = null,
    turnAroundTime = turnAroundTime
)

fun me.calebjones.spacelaunchnow.api.trantor.models.LauncherSummary.toDomain(): DomainLauncherSummary =
    DomainLauncherSummary(
        id = id,
        serialNumber = serialNumber,
        flightProven = flightProven ?: false,
        imageUrl = null
    )

fun LandingSummary.toDomain(): LandingAttemptSummary = LandingAttemptSummary(
    id = id,
    attempt = attempt,
    success = success,
    downrangeDistance = null,
    landingLocation = null,
    outcome = when (success) {
        true -> "Success"
        false -> "Failure"
        null -> null
    },
    description = description,
    location = landingLocationName,
    type = type
)

fun me.calebjones.spacelaunchnow.api.trantor.models.Mission.toDomain(): DomainMission = DomainMission(
    id = id,
    name = name,
    description = description,
    type = type,
    orbit = null,
    imageUrl = null
)

fun PadSummary.toDomain(): Pad = Pad(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    mapUrl = null,
    mapImage = null,
    totalLaunchCount = totalLaunchCount,
    location = locationId?.let { lid -> Location(id = lid, name = location, countryCode = null) },
    imageUrl = imageUrl
)

fun LaunchUpdate.toDomain(): DomainUpdate = DomainUpdate(
    id = id,
    profileImage = null,
    comment = comment,
    infoUrl = infoUrl,
    createdBy = createdBy,
    createdOn = createdOn
)

fun TimelineEvent.toDomain(): TimelineEntry = TimelineEntry(
    type = abbrev,
    relativeTime = relativeTime
)

fun InfoUrl.toDomain(): InfoLink = InfoLink(
    url = url,
    title = title,
    source = source,
    description = null,
    featureImage = null,
    type = type,
    priority = priority
)

fun VidUrl.toDomain(): VideoLink = VideoLink(
    url = url,
    title = title,
    source = source,
    publisher = publisher,
    description = null,
    featureImage = null,
    live = live ?: false,
    priority = priority
)

fun MissionPatchSchema.toDomain(): MissionPatchSummary = MissionPatchSummary(
    id = id,
    name = name,
    imageUrl = imageUrl,
    priority = priority
)

fun LaunchDetail.toDomain(): Launch {
    val resolvedProvider = provider?.toDomain() ?: flatProvider(providerId, providerName)
    val resolvedRocket = rocket?.configuration?.toDomain() ?: rocket?.let { r ->
        RocketConfig(
            id = r.configurationId,
            name = r.configurationName ?: "",
            fullName = null,
            family = null,
            variant = null,
            imageUrl = null,
            active = null,
            reusable = null
        )
    }
    return Launch(
        id = id,
        name = name,
        slug = slug,
        net = net,
        windowStart = windowStart,
        windowEnd = windowEnd,
        lastUpdated = lastUpdated,
        status = LaunchStatus(id = statusId, name = status, abbrev = null, description = null),
        provider = resolvedProvider,
        imageUrl = imageUrl,
        thumbnailUrl = null,
        infographic = null,
        netPrecision = null,
        rocket = resolvedRocket,
        mission = mission?.toDomain(),
        pad = pad?.toDomain(),
        programs = emptyList(),
        probability = probability,
        weatherConcerns = weatherConcerns,
        failreason = failreason,
        hashtag = null,
        webcastLive = webcastLive ?: false,
        launchAttemptCounts = null,
        updates = updates?.map { it.toDomain() } ?: emptyList(),
        infoUrls = infoUrls?.map { it.toDomain() } ?: emptyList(),
        vidUrls = vidUrls?.map { it.toDomain() } ?: emptyList(),
        timeline = timeline?.map { it.toDomain() } ?: emptyList(),
        missionPatches = missionPatches?.map { it.toDomain() } ?: emptyList(),
        rocketDetail = rocket?.toDomain(),
        flightclubUrl = flightclubUrl,
        padTurnaround = padTurnaround,
        providerDetail = provider?.toProviderDetail()
    )
}
