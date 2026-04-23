package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.DockingEventDetailedSerializerForSpacestation
import me.calebjones.spacelaunchnow.api.launchlibrary.models.DockingLocationSerializerForSpacestation
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionMini
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpaceStationDetailedEndpointList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.DockingEvent
import me.calebjones.spacelaunchnow.domain.model.DockingLocation
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.ExpeditionMiniItem
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail

fun SpaceStationDetailedEndpoint.toDomain(): SpaceStationDetail = SpaceStationDetail(
    id = id,
    name = name,
    imageUrl = image?.imageUrl,
    statusName = status?.name,
    statusId = status?.id,
    founded = founded,
    deorbited = null,
    description = description,
    orbit = orbit,
    typeName = type?.name,
    owners = owners?.map { it.toDomainAgency() } ?: emptyList(),
    activeExpeditions = activeExpeditions.map { it.toDomainMini() },
    dockingLocations = dockingLocation.map { it.toDomain() },
    height = height,
    width = width,
    mass = mass,
    volume = volume?.toDouble(),
    onboardCrew = onboardCrew,
    dockedVehicles = dockedVehicles
)

fun ExpeditionDetailed.toDomainDetail(): ExpeditionDetailItem = ExpeditionDetailItem(
    id = id,
    name = name,
    start = start,
    end = end,
    crew = crew.map { it.toDomainCrewMember() },
    missionPatches = missionPatches.map { it.toDomain() },
    spacewalks = spacewalks.map { it.toDomainSummary() }
)

fun ExpeditionMini.toDomainMini(): ExpeditionMiniItem = ExpeditionMiniItem(
    id = id,
    name = name,
    start = start,
    end = end
)

fun DockingLocationSerializerForSpacestation.toDomain(): DockingLocation = DockingLocation(
    id = id,
    name = name,
    currentlyDocked = currentlyDocked?.toDomain()
)

fun DockingEventDetailedSerializerForSpacestation.toDomain(): DockingEvent {
    val vehicleName = flightVehicleChaser?.spacecraft?.name
        ?: payloadFlightChaser?.payload?.name
    val vehicleConfigName = flightVehicleChaser?.spacecraft?.spacecraftConfig?.name
        ?: payloadFlightChaser?.payload?.type?.name
    val vehicleImageUrl = flightVehicleChaser?.spacecraft?.image?.thumbnailUrl
        ?: flightVehicleChaser?.spacecraft?.spacecraftConfig?.image?.thumbnailUrl
        ?: flightVehicleChaser?.spacecraft?.image?.imageUrl
        ?: flightVehicleChaser?.spacecraft?.spacecraftConfig?.image?.imageUrl

    return DockingEvent(
        id = id,
        docking = docking,
        departure = departure,
        vehicleName = vehicleName,
        vehicleConfigName = vehicleConfigName,
        vehicleImageUrl = vehicleImageUrl
    )
}

fun AgencyNormal.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = type?.name,
    countries = country.map { it.toDomain() },
    imageUrl = image?.imageUrl,
    logoUrl = logo?.imageUrl,
    socialLogoUrl = socialLogo?.imageUrl,
    description = description,
    administrator = administrator,
    foundingYear = foundingYear,
    featured = featured,
    launchersDescription = launchers,
    spacecraftDescription = spacecraft
)

fun PaginatedSpaceStationDetailedEndpointList.toDomain(): PaginatedResult<SpaceStationDetail> =
    PaginatedResult(
        count = count,
        next = next,
        previous = previous,
        results = results.map { it.toDomain() }
    )
