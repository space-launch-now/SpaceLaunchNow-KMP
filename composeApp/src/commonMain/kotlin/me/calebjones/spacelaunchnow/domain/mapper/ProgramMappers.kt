package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.launchlibrary.models.ProgramNormal
import me.calebjones.spacelaunchnow.domain.model.Program

/**
 * Map [ProgramNormal] to the full [Program] domain type. The lightweight
 * `ProgramMini.toDomain()` mapper that produces a `ProgramSummary` lives in CommonMappers.
 *
 * Note: There is no `ProgramDetailed` API type in Launch Library 2.4.0 — `ProgramNormal`
 * is the richest representation available.
 */
fun ProgramNormal.toDomainProgram(): Program = Program(
    id = id,
    name = name,
    description = description,
    imageUrl = image?.imageUrl,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    type = type.name,
    startDate = startDate,
    endDate = endDate,
    agencies = agencies.map { it.toDomain() },
    missionPatches = missionPatches.map { it.toDomain() },
    vidUrls = vidUrls.map { it.toDomain() }
)
