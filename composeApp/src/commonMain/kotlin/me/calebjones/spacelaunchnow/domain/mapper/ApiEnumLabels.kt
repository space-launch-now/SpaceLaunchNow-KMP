package me.calebjones.spacelaunchnow.domain.mapper

import androidx.compose.runtime.Composable
import me.calebjones.spacelaunchnow.domain.model.Country
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.Orbit
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_au
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_br
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_ca
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_cn
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_de
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_es
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_fr
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_gb
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_il
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_in
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_ir
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_it
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_jp
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_kp
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_kr
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_kz
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_nz
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_ru
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_ua
import spacelaunchnow_kmp.composeapp.generated.resources.enum_country_us
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_astrophysics
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_biology
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_communications
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_dedicated_rideshare
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_earth_science
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_government_top_secret
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_heliophysics
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_human_exploration
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_lunar_exploration
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_materials_science
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_mission_extension
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_navigation
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_planetary_science
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_resupply
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_robotic_exploration
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_space_situational_awareness
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_suborbital
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_technology
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_test_flight
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_test_target
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_tourism
import spacelaunchnow_kmp.composeapp.generated.resources.enum_mission_type_unknown
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_asteroid
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_earth_entry_trajectory
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_elliptical_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_enhanced_geostationary_transfer_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_geostationary_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_geostationary_transfer_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_geosynchronous_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_geosynchronous_transfer_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_heliocentric_l1
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_heliocentric_n_a
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_high_earth_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_jupiter_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_low_earth_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_lunar_flyby
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_lunar_impactor
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_lunar_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_mars_flyby
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_mars_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_medium_earth_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_mercury_flyby
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_polar_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_solar_escape_trajectory
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_suborbital
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_sun_earth_l2
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_sun_synchronous_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_supersynchronous_transfer_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_unknown
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_venus_flyby
import spacelaunchnow_kmp.composeapp.generated.resources.enum_orbit_venus_orbit
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_deployed
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_deployed_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_failure
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_failure_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_go
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_go_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_hold
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_hold_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_in_flight
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_in_flight_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_partial_failure
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_partial_failure_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_success
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_success_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_tbc
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_tbc_abbrev
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_tbd
import spacelaunchnow_kmp.composeapp.generated.resources.enum_status_tbd_abbrev

/**
 * Maps the stable ids and codes the API sends to string keys, so a launch status, orbit, mission
 * type or country shows in the device language (spec 017, Q8). An id with no key returns null and
 * the caller falls back to the text the API sent. Keys live in `strings_enums.xml`.
 */
object ApiEnumLabels {
    /** Launch status name by status id (1 to 9). */
    fun launchStatusName(id: Int?): StringResource? = when (id) {
        1 -> Res.string.enum_status_go
        2 -> Res.string.enum_status_tbd
        3 -> Res.string.enum_status_success
        4 -> Res.string.enum_status_failure
        5 -> Res.string.enum_status_hold
        6 -> Res.string.enum_status_in_flight
        7 -> Res.string.enum_status_partial_failure
        8 -> Res.string.enum_status_tbc
        9 -> Res.string.enum_status_deployed
        else -> null
    }

    /** Launch status short form by status id (1 to 9). */
    fun launchStatusAbbrev(id: Int?): StringResource? = when (id) {
        1 -> Res.string.enum_status_go_abbrev
        2 -> Res.string.enum_status_tbd_abbrev
        3 -> Res.string.enum_status_success_abbrev
        4 -> Res.string.enum_status_failure_abbrev
        5 -> Res.string.enum_status_hold_abbrev
        6 -> Res.string.enum_status_in_flight_abbrev
        7 -> Res.string.enum_status_partial_failure_abbrev
        8 -> Res.string.enum_status_tbc_abbrev
        9 -> Res.string.enum_status_deployed_abbrev
        else -> null
    }

    /** Orbit name by orbit id. */
    fun orbitName(id: Int?): StringResource? = when (id) {
        0 -> Res.string.enum_orbit_elliptical_orbit
        1 -> Res.string.enum_orbit_geostationary_orbit
        2 -> Res.string.enum_orbit_geostationary_transfer_orbit
        3 -> Res.string.enum_orbit_geosynchronous_orbit
        4 -> Res.string.enum_orbit_geosynchronous_transfer_orbit
        5 -> Res.string.enum_orbit_heliocentric_l1
        6 -> Res.string.enum_orbit_heliocentric_n_a
        7 -> Res.string.enum_orbit_high_earth_orbit
        8 -> Res.string.enum_orbit_low_earth_orbit
        9 -> Res.string.enum_orbit_lunar_flyby
        10 -> Res.string.enum_orbit_lunar_impactor
        11 -> Res.string.enum_orbit_lunar_orbit
        12 -> Res.string.enum_orbit_medium_earth_orbit
        13 -> Res.string.enum_orbit_polar_orbit
        14 -> Res.string.enum_orbit_solar_escape_trajectory
        15 -> Res.string.enum_orbit_suborbital
        16 -> Res.string.enum_orbit_sun_earth_l2
        17 -> Res.string.enum_orbit_sun_synchronous_orbit
        18 -> Res.string.enum_orbit_supersynchronous_transfer_orbit
        19 -> Res.string.enum_orbit_mars_orbit
        20 -> Res.string.enum_orbit_venus_orbit
        21 -> Res.string.enum_orbit_asteroid
        22 -> Res.string.enum_orbit_venus_flyby
        23 -> Res.string.enum_orbit_mars_flyby
        24 -> Res.string.enum_orbit_mercury_flyby
        25 -> Res.string.enum_orbit_unknown
        26 -> Res.string.enum_orbit_enhanced_geostationary_transfer_orbit
        27 -> Res.string.enum_orbit_jupiter_orbit
        28 -> Res.string.enum_orbit_earth_entry_trajectory
        else -> null
    }

    /** Mission type by the type name the API sends (the mission carries no type id). */
    fun missionType(apiName: String?): StringResource? = when (apiName?.trim()) {
        "Earth Science" -> Res.string.enum_mission_type_earth_science
        "Planetary Science" -> Res.string.enum_mission_type_planetary_science
        "Astrophysics" -> Res.string.enum_mission_type_astrophysics
        "Heliophysics" -> Res.string.enum_mission_type_heliophysics
        "Human Exploration" -> Res.string.enum_mission_type_human_exploration
        "Robotic Exploration" -> Res.string.enum_mission_type_robotic_exploration
        "Government/Top Secret" -> Res.string.enum_mission_type_government_top_secret
        "Tourism" -> Res.string.enum_mission_type_tourism
        "Unknown" -> Res.string.enum_mission_type_unknown
        "Communications" -> Res.string.enum_mission_type_communications
        "Resupply" -> Res.string.enum_mission_type_resupply
        "Suborbital" -> Res.string.enum_mission_type_suborbital
        "Test Flight" -> Res.string.enum_mission_type_test_flight
        "Dedicated Rideshare" -> Res.string.enum_mission_type_dedicated_rideshare
        "Navigation" -> Res.string.enum_mission_type_navigation
        "Test Target" -> Res.string.enum_mission_type_test_target
        "Lunar Exploration" -> Res.string.enum_mission_type_lunar_exploration
        "Materials Science" -> Res.string.enum_mission_type_materials_science
        "Biology" -> Res.string.enum_mission_type_biology
        "Technology" -> Res.string.enum_mission_type_technology
        "Mission Extension" -> Res.string.enum_mission_type_mission_extension
        "Space Situational Awareness" -> Res.string.enum_mission_type_space_situational_awareness
        else -> null
    }

    /** Country name by ISO 3166 alpha-2 code. */
    fun countryName(alpha2Code: String?): StringResource? = when (alpha2Code?.uppercase()) {
        "US" -> Res.string.enum_country_us
        "CN" -> Res.string.enum_country_cn
        "RU" -> Res.string.enum_country_ru
        "FR" -> Res.string.enum_country_fr
        "JP" -> Res.string.enum_country_jp
        "IN" -> Res.string.enum_country_in
        "NZ" -> Res.string.enum_country_nz
        "GB" -> Res.string.enum_country_gb
        "IL" -> Res.string.enum_country_il
        "IR" -> Res.string.enum_country_ir
        "KR" -> Res.string.enum_country_kr
        "KP" -> Res.string.enum_country_kp
        "IT" -> Res.string.enum_country_it
        "DE" -> Res.string.enum_country_de
        "AU" -> Res.string.enum_country_au
        "BR" -> Res.string.enum_country_br
        "CA" -> Res.string.enum_country_ca
        "KZ" -> Res.string.enum_country_kz
        "ES" -> Res.string.enum_country_es
        "UA" -> Res.string.enum_country_ua
        else -> null
    }
}

@Composable
private fun StringResource?.orApi(apiText: String): String = this?.let { stringResource(it) } ?: apiText

/** Launch status name in the device language; [apiText] is the fallback for an unknown id. */
@Composable
fun launchStatusLabel(id: Int?, apiText: String): String = ApiEnumLabels.launchStatusName(id).orApi(apiText)

/** Launch status short form in the device language; [apiText] is the fallback. */
@Composable
fun launchStatusAbbrevLabel(id: Int?, apiText: String): String = ApiEnumLabels.launchStatusAbbrev(id).orApi(apiText)

/** Orbit name in the device language; [apiText] is the fallback for an unknown id. */
@Composable
fun orbitLabel(id: Int?, apiText: String): String = ApiEnumLabels.orbitName(id).orApi(apiText)

/** Mission type in the device language; [apiText] is the fallback for an unknown type. */
@Composable
fun missionTypeLabel(apiText: String): String = ApiEnumLabels.missionType(apiText).orApi(apiText)

/** Country name in the device language; [apiText] is the fallback for an unknown code. */
@Composable
fun countryLabel(alpha2Code: String?, apiText: String): String = ApiEnumLabels.countryName(alpha2Code).orApi(apiText)

/** Launch status name in the device language. */
@Composable
fun launchStatusLabel(status: LaunchStatus): String = launchStatusLabel(status.id, status.name)

/** Launch status short form in the device language, or null when the API sent none. */
@Composable
fun launchStatusAbbrevLabel(status: LaunchStatus): String? = status.abbrev?.let { launchStatusAbbrevLabel(status.id, it) }

/** Orbit name in the device language. */
@Composable
fun orbitLabel(orbit: Orbit): String = orbitLabel(orbit.id, orbit.name)

/** Country name in the device language, or null when the API sent none. */
@Composable
fun countryLabel(country: Country): String? = country.name?.let { countryLabel(country.alpha2Code, it) }
