package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.Expedition as TrantorExpedition
import me.calebjones.spacelaunchnow.api.trantor.models.ExpeditionCrewMember as TrantorExpeditionCrewMember
import me.calebjones.spacelaunchnow.api.trantor.models.ExpeditionMissionPatch as TrantorExpeditionMissionPatch
import me.calebjones.spacelaunchnow.api.trantor.models.StationDetail as TrantorStationDetail
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.CrewMember
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.ExpeditionMiniItem
import me.calebjones.spacelaunchnow.domain.model.MissionPatchSummary
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail

// ==================== Trantor overloads (phase5-browse-space migration) ====================
//
// Escalations (fields the Trantor `/api/v1/space_stations` contract does not expose, left
// absent rather than fabricated per the fail-closed rule for this unit):
// - `deorbited`: Trantor exposes this as a Boolean flag; the domain model expects a
//   `LocalDate?` (a deorbit date). These are different types — a bool can't be turned into
//   a date without inventing one, so this maps to null (a type-shape mismatch, not just a
//   missing value; needs a domain-model or contract decision).
// - `dockingLocations`: no docking-location entity exists in the Trantor contract at all.
// - `activeExpeditions`: Trantor's `expeditions[]` is the full history, not filtered to
//   "active"; approximated here as expeditions with no `end` date.
// - `ExpeditionDetailItem.spacewalks`: not present on Trantor's embedded expedition payload.
//   (`mission_patches[]` is mapped since E14(P5).)
// - `CrewMember.astronaut`: Trantor's crew entries carry `astronaut_id`/`astronaut_name`/
//   `role` plus, since E14(P5), `image_url` and the agency id/name/abbrev (no full astronaut
//   record), so the embedded `AstronautListItem` fills those and leaves everything else
//   null/empty — not a fabrication, just a thinner reference than the old LL payload.

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
    owners = owners?.takeIf { it.isNotEmpty() }?.map { it.toDomainAgency() }
        // TODO(E12(P5) follow-up phase5-owners-cleanup): drop the owner_ids/owner_names
        // fallback once `owners[]` has shipped for a release and the parallel lists are
        // removed from the contract.
        ?: (ownerIds ?: emptyList()).zip(ownerNames ?: emptyList()) { ownerId, ownerName ->
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
    missionPatches = missionPatches?.map { it.toDomain() } ?: emptyList(),
    spacewalks = emptyList()
)

fun TrantorExpeditionMissionPatch.toDomain(): MissionPatchSummary = MissionPatchSummary(
    id = id,
    name = name,
    imageUrl = imageUrl,
    priority = priority
)

fun TrantorExpeditionCrewMember.toDomainCrewMember(): CrewMember = CrewMember(
    id = astronautId ?: 0,
    role = role,
    astronaut = AstronautListItem(
        id = astronautId ?: 0,
        name = astronautName,
        statusName = null,
        statusId = null,
        agencyName = agencyName,
        agencyAbbrev = agencyAbbrev,
        agencyId = agencyId,
        imageUrl = imageUrl,
        thumbnailUrl = imageUrl,
        age = null,
        bio = null,
        typeName = null,
        nationality = emptyList()
    )
)
