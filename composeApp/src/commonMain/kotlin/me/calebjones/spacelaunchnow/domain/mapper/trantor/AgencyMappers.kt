package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyFull
import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseAgencyList
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.Country
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.Provider
import me.calebjones.spacelaunchnow.util.CountryNames

/**
 * Trantor equivalents of the LL mappers in AgencyMappers.kt.
 *
 * Trantor's `GET /agencies` list row (`AgencyList`, ruling E9(P5)) carries `type`/`type_id`,
 * `featured`, `country_codes`, and all three images (`image_url`/`logo_url`/`social_logo_url`)
 * beside `id, name, abbrev` — [AgencyListView] renders a logo, a type badge, and country chips
 * per row from those fields (client pass E7/E9(P5)).
 */

/** Trantor sends bare ISO alpha-2 codes; the display name comes from [CountryNames]. */
fun String.toDomainCountry(): Country = Country(
    id = 0,
    name = CountryNames.nameFor(this),
    alpha2Code = this,
    alpha3Code = null,
    nationalityName = null,
    nationalityNameComposed = null
)

fun AgencyList.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = type,
    countries = countryCodes?.map { it.toDomainCountry() } ?: emptyList(),
    imageUrl = imageUrl,
    logoUrl = logoUrl,
    socialLogoUrl = socialLogoUrl,
    description = null,
    administrator = null,
    foundingYear = null,
    featured = featured,
    totalLaunchCount = totalLaunchCount
)

/**
 * The agency list row as a launch-card `Provider` embed (event `agencies[]`, and any other
 * one-level embed that reuses the agencies list row per Principle 2).
 */
fun AgencyList.toDomain(): Provider = Provider(
    id = id,
    name = name,
    abbrev = abbrev,
    type = type,
    countryCode = countryCodes?.firstOrNull(),
    logoUrl = logoUrl,
    socialLogo = socialLogoUrl,
    imageUrl = imageUrl
)

fun AgencyFull.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = agencyType,
    countries = countryCodes?.map { it.toDomainCountry() } ?: emptyList(),
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
