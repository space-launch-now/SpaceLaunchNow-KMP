package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailedSerializerNoManufacturer
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherDetailedList
import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.LauncherStatus
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Vehicle mappers produce the richer [VehicleConfig] domain type used on dedicated vehicle
 * screens. The simpler `LauncherConfig*.toDomain()` mappers in CommonMappers produce the
 * slim [me.calebjones.spacelaunchnow.domain.model.RocketConfig] used inline within
 * [me.calebjones.spacelaunchnow.domain.model.Launch].
 */
fun LauncherConfigList.toVehicleDomain(): VehicleConfig = VehicleConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = families?.firstOrNull()?.name,
    variant = variant,
    imageUrl = null
)

fun LauncherConfigNormal.toVehicleDomain(): VehicleConfig = VehicleConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = families?.firstOrNull()?.name,
    variant = variant,
    imageUrl = image?.imageUrl,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    manufacturerName = manufacturer?.name,
    active = active,
    reusable = reusable
)

fun LauncherConfigDetailed.toVehicleDomain(): VehicleConfig = VehicleConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = families?.firstOrNull()?.name,
    variant = variant,
    imageUrl = image?.imageUrl,
    description = description,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    manufacturerName = manufacturer?.name,
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
    active = active,
    reusable = reusable
)

fun LauncherConfigDetailedSerializerNoManufacturer.toVehicleDomain(): VehicleConfig = VehicleConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = families?.firstOrNull()?.name,
    variant = variant,
    imageUrl = image?.imageUrl,
    description = description,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
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
    active = active,
    reusable = reusable
)

fun LauncherDetailed.toDomain(): LauncherDetail = LauncherDetail(
    id = id,
    serialNumber = serialNumber,
    flightProven = flightProven ?: false,
    imageUrl = image?.imageUrl,
    flights = flights,
    lastLaunchDate = lastLaunchDate,
    firstLaunchDate = firstLaunchDate,
    status = status?.let { LauncherStatus(id = it.id, name = it.name) },
    details = details
)

fun PaginatedLauncherDetailedList.toDomain(): PaginatedResult<LauncherDetail> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

fun PaginatedLauncherConfigDetailedList.toDomain(): PaginatedResult<VehicleConfig> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toVehicleDomain() }
)

fun PaginatedLauncherConfigNormalList.toDomain(): PaginatedResult<VehicleConfig> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toVehicleDomain() }
)
