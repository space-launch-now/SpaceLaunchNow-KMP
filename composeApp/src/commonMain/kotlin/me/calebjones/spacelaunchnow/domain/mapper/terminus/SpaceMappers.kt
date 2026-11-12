package me.calebjones.spacelaunchnow.domain.mapper.terminus

import me.calebjones.spacelaunchnow.api.launchlibrary.models.AstronautEndpointDetailed as LLAstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ExpeditionDetailed as LLExpeditionDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAstronautEndpointNormalList as LLPaginatedAstronautEndpointNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal as LLProgramNormal
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpaceStationDetailedEndpoint as LLSpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.api.terminus.models.AstronautEndpointDetailed as TerminusAstronautEndpointDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.ExpeditionDetailed as TerminusExpeditionDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedAstronautEndpointNormalList as TerminusPaginatedAstronautEndpointNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.ProgramEndpoint as TerminusProgramEndpoint
import me.calebjones.spacelaunchnow.api.terminus.models.SpaceStationDetailedEndpoint as TerminusSpaceStationDetailedEndpoint
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainDetail
import me.calebjones.spacelaunchnow.domain.mapper.toDomainProgram
import me.calebjones.spacelaunchnow.domain.model.AstronautDetail
import me.calebjones.spacelaunchnow.domain.model.AstronautListItem
import me.calebjones.spacelaunchnow.domain.model.ExpeditionDetailItem
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Program
import me.calebjones.spacelaunchnow.domain.model.SpaceStationDetail

// Terminus serves the LL2 shapes for space stations, expeditions, astronauts and programs. Each
// mapper re-reads the Terminus model as the LL model (see Bridge.kt) and reuses the one tested
// LL -> domain mapping. A field Terminus drops and LL requires fails loudly (SerializationException).
// Extra Terminus fields (`deorbited_date`, `programs`, `space_stations`) are ignored by the bridge.
// The expedition crew (`AstronautFlight`) and the astronaut flights ride the same bridge.

/** Terminus `GET /space_stations/{id}/` to the domain station. */
fun TerminusSpaceStationDetailedEndpoint.toDomain(): SpaceStationDetail =
    bridge<_, LLSpaceStationDetailedEndpoint>()
        .toDomain()

/** Terminus `GET /expeditions/{id}/` to the domain expedition, crew (`AstronautFlight`) included. */
fun TerminusExpeditionDetailed.toDomainDetail(): ExpeditionDetailItem =
    bridge<_, LLExpeditionDetailed>()
        .toDomainDetail()

/** Terminus `GET /astronauts/` page to the domain page. */
fun TerminusPaginatedAstronautEndpointNormalList.toDomain(): PaginatedResult<AstronautListItem> =
    bridge<_, LLPaginatedAstronautEndpointNormalList>()
        .toDomain()

/** Terminus `GET /astronauts/{id}/` to the domain astronaut detail. */
fun TerminusAstronautEndpointDetailed.toDomainDetail(): AstronautDetail =
    bridge<_, LLAstronautEndpointDetailed>()
        .toDomainDetail()

/** Terminus `GET /programs/{id}/` to the domain program. */
fun TerminusProgramEndpoint.toDomainProgram(): Program =
    bridge<_, LLProgramNormal>()
        .toDomainProgram()
