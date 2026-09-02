package me.calebjones.spacelaunchnow.domain.mapper

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyFull
import me.calebjones.spacelaunchnow.domain.model.Agency

/**
 * Trantor -> domain mapper for agency detail. Separate file from the LL AgencyMappers.kt to
 * avoid touching a file another migration unit may be editing concurrently.
 *
 * Known gap: [Agency.countries] requires full Country objects (id, name, alpha-2/3 codes).
 * Trantor's AgencyFull only serves `country_codes` (a list of alpha-2 strings, no id) - since
 * domain Country.id is non-null, building one would mean fabricating an id that doesn't
 * exist upstream, so this maps to an empty list instead. See the phase5-launch unit report.
 */
fun AgencyFull.toDomainAgency(): Agency = Agency(
    id = id,
    name = name,
    abbrev = abbrev,
    typeName = agencyType,
    countries = emptyList(),
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
    failedLandings = failedLandings,
    consecutiveSuccessfulLandings = null,
    attemptedLandingsSpacecraft = null,
    successfulLandingsSpacecraft = null,
    failedLandingsSpacecraft = null
)
