package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import me.calebjones.spacelaunchnow.api.trantor.models.FamilyList
import me.calebjones.spacelaunchnow.api.trantor.models.LauncherConfigSummary
import me.calebjones.spacelaunchnow.api.trantor.models.LocationList
import me.calebjones.spacelaunchnow.api.trantor.models.LookupItem
import me.calebjones.spacelaunchnow.api.trantor.models.ProgramList
import me.calebjones.spacelaunchnow.data.model.FilterOption

/**
 * Mappers from Trantor filter-picker/lookup rows to the domain-facing [FilterOption]
 * used by the schedule/rocket/astronaut filter repositories.
 */

fun LookupItem.toFilterOption(): FilterOption =
    FilterOption(id = id, name = name, abbreviation = abbrev)

fun AgencyList.toFilterOption(): FilterOption =
    FilterOption(id = id, name = name, abbreviation = abbrev)

fun ProgramList.toFilterOption(): FilterOption =
    FilterOption(id = id, name = name, abbreviation = null)

fun FamilyList.toFilterOption(): FilterOption =
    FilterOption(id = id, name = name, abbreviation = null)

fun LocationList.toFilterOption(): FilterOption =
    FilterOption(id = id, name = name, abbreviation = null)

// LauncherConfigSummary is a flat picker row (contract principle 2) — it carries
// manufacturerId but not a manufacturer abbreviation string, so unlike the LL-era
// picker there is no abbreviation to surface here without an extra agency lookup.
fun LauncherConfigSummary.toFilterOption(): FilterOption =
    FilterOption(id = id, name = fullName ?: name, abbreviation = null)
