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
 * schema carries `manufacturer_id` only (no denormalized manufacturer name) and no `families`
 * array, so [VehicleConfig.manufacturerName] and [VehicleConfig.family] come back null from
 * every Trantor source below — see Phase 5 browse-vehicles unit escalation.
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
    manufacturerName = null,
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
    manufacturerName = null,
    minStage = minStage,
    maxStage = maxStage,
    length = length,
    diameter = diameter,
    launchMass = launchMass,
    leoCapacity = leoCapacity,
    gtoCapacity = gtoCapacity,
    toThrust = toThrust,
    apogee = apogee,
    launchCost = launchCost,
    totalLaunchCount = totalLaunchCount,
    successfulLaunches = successfulLaunches,
    failedLaunches = failedLaunches,
    pendingLaunches = pendingLaunches,
    attemptedLandings = null,
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
