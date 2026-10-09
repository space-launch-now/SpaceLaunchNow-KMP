package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigFull
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherDetail as TrantorLauncherDetail
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherListItem
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseLauncherListItem
import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.LauncherStatus
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

/**
 * Trantor equivalents of the LL mappers in VehicleMappers.kt. Trantor's `/configurations`
 * schema (E10(P5)) joins `manufacturer_name` and `manufacturer_abbrev` beside
 * `manufacturer_id` at read time; [VehicleConfig] has no abbrev field (no UI reads one), so
 * only `manufacturerName` is mapped. Trantor still has no `families` array, so
 * [VehicleConfig.family] comes back null from every source below.
 */

fun LauncherConfigSummary.toVehicleDomain(): VehicleConfig = VehicleConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = null,
    variant = variant,
    imageUrl = imageUrl,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    manufacturerName = manufacturerName,
    active = active,
    reusable = reusable
)

fun LauncherConfigFull.toVehicleDomain(): VehicleConfig = VehicleConfig(
    id = id,
    name = name,
    fullName = fullName,
    family = null,
    variant = variant,
    imageUrl = imageUrl,
    description = description,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    manufacturerName = manufacturerName,
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
    consecutiveSuccessfulLandings = consecutiveSuccessfulLandings,
    attemptedLandings = attemptedLandings,
    successfulLandings = successfulLandings,
    failedLandings = failedLandings,
    maidenFlight = maidenFlight,
    active = active,
    reusable = reusable
)

fun PaginatedResponseLauncherConfigSummary.toDomain(): PaginatedResult<VehicleConfig> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toVehicleDomain() }
)

fun TrantorLauncherDetail.toDomain(): LauncherDetail = LauncherDetail(
    id = id,
    serialNumber = serialNumber,
    flightProven = false,
    imageUrl = imageUrl,
    flights = flights,
    lastLaunchDate = lastLaunchDate,
    firstLaunchDate = firstLaunchDate,
    status = statusId?.let { LauncherStatus(id = it, name = status) },
    details = details
)

fun LauncherListItem.toDomain(): LauncherDetail = LauncherDetail(
    id = id,
    serialNumber = serialNumber,
    flightProven = false,
    imageUrl = imageUrl,
    flights = flights,
    status = statusId?.let { LauncherStatus(id = it, name = status) }
)

fun PaginatedResponseLauncherListItem.toDomain(): PaginatedResult<LauncherDetail> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)
