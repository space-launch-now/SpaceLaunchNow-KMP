package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.ProgramDetail as TrantorProgramDetail
import me.calebjones.spacelaunchnow.domain.model.Program

/**
 * Map the Trantor [TrantorProgramDetail] to the full [Program] domain type
 * (phase5-browse-space migration).
 *
 * Escalations (fields the Trantor `/api/v1/programs/{id}` contract does not expose,
 * left absent rather than fabricated — see phase5-browse-space unit report):
 * - `type`: no program-type field in the Trantor contract.
 * - `agencies`: Trantor only returns `agency_ids` (no names), and [me.calebjones.spacelaunchnow.domain.model.Provider.name]
 *   is a required non-null field, so building a `Provider` list without real names would mean
 *   inventing data. Left empty rather than faked.
 * - `missionPatches` / `vidUrls`: not present on the Trantor program payload.
 */
fun TrantorProgramDetail.toDomainProgram(): Program = Program(
    id = id,
    name = name,
    description = description,
    imageUrl = imageUrl,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    type = null,
    startDate = startDate,
    endDate = endDate,
    agencies = emptyList(),
    missionPatches = emptyList(),
    vidUrls = emptyList()
)
