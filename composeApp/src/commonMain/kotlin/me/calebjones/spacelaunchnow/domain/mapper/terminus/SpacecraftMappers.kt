package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftConfigDetailedList as LLPaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedSpacecraftEndpointDetailedList as LLPaginatedSpacecraftEndpointDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftConfigDetailed as LLSpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.SpacecraftEndpointDetailed as LLSpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedSpacecraftConfigDetailedList as TerminusPaginatedSpacecraftConfigDetailedList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedSpacecraftEndpointDetailedList as TerminusPaginatedSpacecraftEndpointDetailedList
import me.calebjones.spacelaunchnow.api.terminus.models.SpacecraftConfigDetailed as TerminusSpacecraftConfigDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.SpacecraftEndpointDetailed as TerminusSpacecraftEndpointDetailed
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Spacecraft
import me.calebjones.spacelaunchnow.domain.model.SpacecraftConfig

// Terminus serves the LL2 shapes for spacecraft and spacecraft configurations. Each mapper
// re-reads the Terminus model as the LL model (see bridgeJson) and reuses the one tested
// LL -> domain mapping.

/** Terminus `GET /spacecraft/{id}/` to the domain spacecraft. */
fun TerminusSpacecraftEndpointDetailed.toDomain(): Spacecraft =
    bridgeJson
        .decodeFromString<LLSpacecraftEndpointDetailed>(bridgeJson.encodeToString(this))
        .toDomain()

/** Terminus `GET /spacecraft/detailed/` page to the domain page. */
fun TerminusPaginatedSpacecraftEndpointDetailedList.toDomain(): PaginatedResult<Spacecraft> =
    bridgeJson
        .decodeFromString<LLPaginatedSpacecraftEndpointDetailedList>(bridgeJson.encodeToString(this))
        .toDomain()

/** Terminus `GET /spacecraft_configurations/{id}/` to the domain spacecraft configuration. */
fun TerminusSpacecraftConfigDetailed.toDomain(): SpacecraftConfig =
    bridgeJson
        .decodeFromString<LLSpacecraftConfigDetailed>(bridgeJson.encodeToString(this))
        .toDomain()

/** Terminus `GET /spacecraft_configurations/detailed/` page to the domain page. */
fun TerminusPaginatedSpacecraftConfigDetailedList.toDomain(): PaginatedResult<SpacecraftConfig> =
    bridgeJson
        .decodeFromString<LLPaginatedSpacecraftConfigDetailedList>(bridgeJson.encodeToString(this))
        .toDomain()
