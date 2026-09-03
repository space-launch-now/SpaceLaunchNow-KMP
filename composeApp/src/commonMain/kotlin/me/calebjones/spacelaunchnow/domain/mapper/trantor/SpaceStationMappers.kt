package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.Expedition as TrantorExpedition
import me.calebjones.spacelaunchnow.api.trantor.models.ExpeditionCrewMember as TrantorExpeditionCrewMember
import me.calebjones.spacelaunchnow.api.trantor.models.StationDetail as TrantorStationDetail
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.CrewMember
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.ExpeditionMiniItem
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail

// ==================== Trantor overloads (phase5-browse-space migration) ====================
//
// Escalations (fields the Trantor `/api/v1/space_stations` contract does not expose, left
// absent rather than fabricated per the fail-closed rule for this unit):
// - `deorbited`: Trantor exposes this as a Boolean flag; the domain model expects a
//   `LocalDate?` (a deorbit date). These are different types — a bool can't be turned into
//   a date without inventing one, so this maps to null (a type-shape mismatch, not just a
//   missing value; needs a domain-model or contract decision).
// - `owners`: Trantor gives parallel `owner_ids`/`owner_names` lists (real data, zipped
//   into minimal `Agency(id, name)` values below) but no abbreviation/country/logo/etc., so
//   those Agency fields are null rather than invented.
// - `dockingLocations`: no docking-location entity exists in the Trantor contract at all.
// - `activeExpeditions`: Trantor's `expeditions[]` is the full history, not filtered to
//   "active"; approximated here as expeditions with no `end` date.
// - `ExpeditionDetailItem.missionPatches` / `.spacewalks`: not present on Trantor's embedded
//   expedition/crew payload.
// - `CrewMember.astronaut`: Trantor's crew entries carry only `astronaut_id`/`astronaut_name`
//   /`role` (no full astronaut record), so the embedded `AstronautListItem` below only has
//   those three fields populated and everything else null/empty — not a fabrication, just a
//   much thinner astronaut reference than the old per-expedition LL payload provided.

fun TrantorStationDetail.toDomain(): SpaceStationDetail = SpaceStationDetail(
    id = id,
    name = name,
    imageUrl = imageUrl,
    statusName = status,
    statusId = statusId,
    founded = founded,
    deorbited = null,
    description = description,
    orbit = orbit,
    typeName = type,
    owners = (ownerIds ?: emptyList()).zip(ownerNames ?: emptyList()) { ownerId, ownerName ->
        Agency(
            id = ownerId,
            name = ownerName,
            abbrev = null,
            typeName = null,
            countries = emptyList(),
            imageUrl = null,
            logoUrl = null,
            socialLogoUrl = null,
            description = null,
            administrator = null,
            foundingYear = null
        )
    },
    activeExpeditions = (expeditions ?: emptyList()).filter { it.end == null }.map { it.toDomainMini() },
    dockingLocations = emptyList(),
    height = height,
    width = width,
    mass = mass,
    volume = volume?.toDouble(),
    onboardCrew = onboardCrew,
    dockedVehicles = dockedVehicles
)

fun TrantorExpedition.toDomainMini(): ExpeditionMiniItem = ExpeditionMiniItem(
    id = id,
    name = name,
    start = start,
    end = end
)

fun TrantorExpedition.toDomainDetail(): ExpeditionDetailItem = ExpeditionDetailItem(
    id = id,
    name = name,
    start = start,
    end = end,
    crew = (crew ?: emptyList()).map { it.toDomainCrewMember() },
    missionPatches = emptyList(),
    spacewalks = emptyList()
)

fun TrantorExpeditionCrewMember.toDomainCrewMember(): CrewMember = CrewMember(
    id = astronautId ?: 0,
    role = role,
    astronaut = AstronautListItem(
        id = astronautId ?: 0,
        name = astronautName,
        statusName = null,
        statusId = null,
        agencyName = null,
        agencyAbbrev = null,
        agencyId = null,
        imageUrl = null,
        thumbnailUrl = null,
        age = null,
        bio = null,
        typeName = null,
        nationality = emptyList()
    )
)
