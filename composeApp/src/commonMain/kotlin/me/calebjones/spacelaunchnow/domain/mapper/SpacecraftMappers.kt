package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftEndpointDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseSpacecraftConfigSummary as TrantorPaginatedSpacecraftConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseSpacecraftSummary as TrantorPaginatedSpacecraftSummary
import me.calebjones.spacelaunchnow.api.trantor.models.SpacecraftConfigFull as TrantorSpacecraftConfigFull
import me.calebjones.spacelaunchnow.api.trantor.models.SpacecraftConfigSummary as TrantorSpacecraftConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.SpacecraftFull as TrantorSpacecraftFull
import me.calebjones.spacelaunchnow.api.trantor.models.SpacecraftSummary as TrantorSpacecraftSummary
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Provider
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig
import me.calebjones.spacelaunchnow.domain.model.SpacecraftStatus

fun SpacecraftConfigDetailed.toDomain(): SpacecraftConfig = SpacecraftConfig(
    id = id,
    name = name,
    type = type.name,
    agency = agency.toDomain(),
    imageUrl = image?.imageUrl,
    inUse = inUse,
    capability = capability,
    history = history,
    details = details,
    maidenFlight = maidenFlight,
    humanRated = humanRated,
    crewCapacity = crewCapacity,
    payloadCapacity = payloadCapacity
)

fun SpacecraftEndpointDetailed.toDomain(): Spacecraft = Spacecraft(
    id = id,
    name = name,
    serialNumber = serialNumber,
    status = status?.let { SpacecraftStatus(id = it.id, name = it.name) },
    description = description,
    imageUrl = image?.imageUrl,
    config = spacecraftConfig.toDomain()
)

fun PaginatedSpacecraftEndpointDetailedList.toDomain(): PaginatedResult<Spacecraft> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

fun PaginatedSpacecraftConfigDetailedList.toDomain(): PaginatedResult<SpacecraftConfig> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

// ==================== Trantor overloads (phase5-browse-space migration) ====================
//
// Escalations (fields the Trantor contract does not expose, left absent rather than
// fabricated per the fail-closed rule for this unit):
// - Spacecraft status is a bare label string on Trantor (no id), but the domain
//   `SpacecraftStatus.id: Int` is required non-null — inventing a sentinel id would be
//   fake data, so `status` maps to null when Trantor doesn't give us an id.
// - `SpacecraftConfig`/`Provider` require non-null `name`; Trantor's spacecraft/config
//   summaries only ever pair an id with its name (never one without the other in the
//   same payload), so these are only constructed when both are present, never fabricated.

private fun trantorAgencyProvider(agencyId: Int?, agencyName: String?): Provider? =
    if (agencyId != null && agencyName != null) {
        Provider(
            id = agencyId,
            name = agencyName,
            abbrev = null,
            type = null,
            countryCode = null,
            logoUrl = null,
            socialLogo = null,
            imageUrl = null
        )
    } else null

fun TrantorSpacecraftConfigFull.toDomain(): SpacecraftConfig = SpacecraftConfig(
    id = id,
    name = name,
    type = type,
    agency = trantorAgencyProvider(agencyId, agencyName),
    imageUrl = imageUrl,
    inUse = inUse,
    capability = capability,
    history = history,
    details = details,
    maidenFlight = maidenFlight,
    humanRated = humanRated,
    crewCapacity = crewCapacity,
    payloadCapacity = payloadCapacity
)

fun TrantorSpacecraftConfigSummary.toDomain(): SpacecraftConfig = SpacecraftConfig(
    id = id,
    name = name,
    type = type,
    agency = trantorAgencyProvider(agencyId, agencyName),
    imageUrl = imageUrl,
    inUse = inUse,
    capability = null,
    history = null,
    details = null,
    maidenFlight = null,
    humanRated = humanRated,
    crewCapacity = null,
    payloadCapacity = null
)

fun TrantorPaginatedSpacecraftConfigSummary.toDomain(): PaginatedResult<SpacecraftConfig> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)

private fun trantorSpacecraftConfigRef(configId: Int?, configName: String?): SpacecraftConfig? =
    if (configId != null && configName != null) {
        SpacecraftConfig(id = configId, name = configName)
    } else null

fun TrantorSpacecraftFull.toDomain(): Spacecraft = Spacecraft(
    id = id,
    name = name,
    serialNumber = serialNumber,
    // Trantor gives only a bare status label (no id); SpacecraftStatus.id is required
    // non-null, so we cannot honestly construct one here without a fake id (escalation).
    status = null,
    description = description,
    imageUrl = imageUrl,
    config = trantorSpacecraftConfigRef(spacecraftConfigId, spacecraftConfigName)
)

fun TrantorSpacecraftSummary.toDomain(): Spacecraft = Spacecraft(
    id = id,
    name = name,
    serialNumber = serialNumber,
    // Trantor gives only a bare status label (no id); SpacecraftStatus.id is required
    // non-null, so we cannot honestly construct one here without a fake id (escalation).
    status = null,
    description = null,
    imageUrl = imageUrl,
    config = trantorSpacecraftConfigRef(spacecraftConfigId, spacecraftConfigName)
)

fun TrantorPaginatedSpacecraftSummary.toDomain(): PaginatedResult<Spacecraft> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomain() }
)
