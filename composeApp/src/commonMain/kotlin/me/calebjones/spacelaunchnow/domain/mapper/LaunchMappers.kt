package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchBasicList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLaunchNormalList
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchAttemptCounts
import me.calebjones.spacelaunchnow.domain.model.Location
import me.calebjones.spacelaunchnow.domain.model.Pad
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

fun LaunchBasic.toDomain(): Launch = Launch(
    id = id,
    name = name ?: "",
    slug = slug,
    net = net,
    windowStart = windowStart,
    windowEnd = windowEnd,
    lastUpdated = lastUpdated,
    status = status?.toDomain(),
    provider = launchServiceProvider.toDomain(),
    imageUrl = image?.imageUrl,
    thumbnailUrl = image?.thumbnailUrl,
    infographic = infographic,
    netPrecision = netPrecision?.toDomain(),
    pad = locationName?.let {
        Pad(
            id = 0,
            name = null,
            latitude = null,
            longitude = null,
            mapUrl = null,
            mapImage = null,
            totalLaunchCount = null,
            location = Location(id = 0, name = it, countryCode = null)
        )
    }
)

fun LaunchNormal.toDomain(): Launch = Launch(
    id = id,
    name = name ?: "",
    slug = slug,
    net = net,
    windowStart = windowStart,
    windowEnd = windowEnd,
    lastUpdated = lastUpdated,
    status = status?.toDomain(),
    provider = launchServiceProvider.toDomain(),
    imageUrl = image?.imageUrl,
    thumbnailUrl = image?.thumbnailUrl,
    infographic = infographic,
    netPrecision = netPrecision?.toDomain(),
    rocket = rocket?.configuration?.toDomain(),
    mission = mission?.toDomain(),
    pad = pad?.toDomain(),
    programs = program?.map { it.toDomain() } ?: emptyList(),
    probability = probability,
    weatherConcerns = weatherConcerns,
    failreason = failreason,
    hashtag = hashtag,
    webcastLive = webcastLive ?: false,
    launchAttemptCounts = LaunchAttemptCounts(
        orbital = orbitalLaunchAttemptCount,
        location = locationLaunchAttemptCount,
        pad = padLaunchAttemptCount,
        agency = agencyLaunchAttemptCount,
        orbitalYear = orbitalLaunchAttemptCountYear,
        locationYear = locationLaunchAttemptCountYear,
        padYear = padLaunchAttemptCountYear,
        agencyYear = agencyLaunchAttemptCountYear
    )
)

fun LaunchDetailed.toDomain(): Launch = Launch(
    id = id,
    name = name ?: "",
    slug = slug,
    net = net,
    windowStart = windowStart,
    windowEnd = windowEnd,
    lastUpdated = lastUpdated,
    status = status?.toDomain(),
    provider = launchServiceProvider.toDomain(),
    imageUrl = image?.imageUrl,
    thumbnailUrl = image?.thumbnailUrl,
    infographic = infographic,
    netPrecision = netPrecision?.toDomain(),
    rocket = rocket?.configuration?.toDomain(),
    mission = mission?.toDomain(),
    pad = pad?.toDomain(),
    programs = program?.map { it.toDomain() } ?: emptyList(),
    probability = probability,
    weatherConcerns = weatherConcerns,
    failreason = failreason,
    hashtag = hashtag,
    webcastLive = webcastLive ?: false,
    launchAttemptCounts = LaunchAttemptCounts(
        orbital = orbitalLaunchAttemptCount,
        location = locationLaunchAttemptCount,
        pad = padLaunchAttemptCount,
        agency = agencyLaunchAttemptCount,
        orbitalYear = orbitalLaunchAttemptCountYear,
        locationYear = locationLaunchAttemptCountYear,
        padYear = padLaunchAttemptCountYear,
        agencyYear = agencyLaunchAttemptCountYear
    ),
    updates = updates.map { it.toDomain() },
    infoUrls = infoUrls.map { it.toDomain() },
    vidUrls = vidUrls.map { it.toDomain() },
    timeline = timeline.map { it.toDomain() },
    missionPatches = missionPatches.map { it.toDomain() },
    rocketDetail = rocket?.toDomain(),
    flightclubUrl = flightclubUrl,
    padTurnaround = padTurnaround,
    providerDetail = launchServiceProvider.toProviderDetail()
)

fun PaginatedLaunchBasicList.toDomain(): PaginatedResult<Launch> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

fun PaginatedLaunchNormalList.toDomain(): PaginatedResult<Launch> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

fun PaginatedLaunchDetailedList.toDomain(): PaginatedResult<Launch> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)
