package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyFull
import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAgencyList
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Country
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/**
 * Trantor equivalents of the LL mappers in AgencyMappers.kt.
 *
 * Trantor's `GET /agencies` list row (`AgencyList`) carries only `id, name, abbrev` — no type,
 * countries, or logo/image, unlike LL's `AgencyNormal`. [AgencyListView] renders a logo circle,
 * a type badge, and country chips per row from those fields, so every row in the migrated list
 * will show a placeholder icon and no chips until the contract adds them (`AgencySummary`,
 * already served for the embedded launch `provider` field, has exactly what's missing here) —
 * see Phase 5 browse-vehicles unit escalation. `country_code` is also a single value in Trantor
 * vs. LL's list; see AgencyRepositoryImpl for the multi-select filter fallback.
 */

fun AgencyList.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = null,
    countries = emptyList(),
    imageUrl = null,
    logoUrl = null,
    socialLogoUrl = null,
    description = null,
    administrator = null,
    foundingYear = null
)

fun AgencyFull.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = agencyType,
    countries = countryCodes?.map { code ->
        Country(
            id = 0,
            name = null,
            alpha2Code = code,
            alpha3Code = null,
            nationalityName = null,
            nationalityNameComposed = null
        )
    } ?: emptyList(),
    imageUrl = imageUrl,
    logoUrl = logoUrl,
    socialLogoUrl = socialLogoUrl,
    description = description,
    administrator = administrator,
    foundingYear = foundingYear,
    featured = featured,
    infoUrl = infoUrl,
    wikiUrl = wikiUrl,
    launchersDescription = launchers,
    spacecraftDescription = spacecraft,
    totalLaunchCount = totalLaunchCount,
    consecutiveSuccessfulLaunches = consecutiveSuccessfulLaunches,
    successfulLaunches = successfulLaunches,
    failedLaunches = failedLaunches,
    pendingLaunches = pendingLaunches,
    attemptedLandings = attemptedLandings,
    successfulLandings = successfulLandings,
    failedLandings = failedLandings
)

fun PaginatedResponseAgencyList.toDomain(): PaginatedResult<Agency> = PaginatedResult(
    count = count,
    next = next,
    previous = previous,
    results = results.map { it.toDomainAgency() }
)
