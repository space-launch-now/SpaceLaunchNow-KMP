package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.encodeToString
import me.calebjones.spacelaunchnow.api.launchlibrary.models.AgencyEndpointDetailed as LLAgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherConfigDetailed as LLLauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LauncherDetailed as LLLauncherDetailed
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedAgencyNormalList as LLPaginatedAgencyNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigDetailedList as LLPaginatedLauncherConfigDetailedList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherConfigNormalList as LLPaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.api.launchlibrary.models.PaginatedLauncherDetailedList as LLPaginatedLauncherDetailedList
import me.calebjones.spacelaunchnow.api.terminus.models.AgencyEndpointDetailed as TerminusAgencyEndpointDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.LauncherConfigDetailed as TerminusLauncherConfigDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.LauncherDetailed as TerminusLauncherDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedAgencyNormalList as TerminusPaginatedAgencyNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLauncherConfigDetailedList as TerminusPaginatedLauncherConfigDetailedList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLauncherConfigNormalList as TerminusPaginatedLauncherConfigNormalList
import me.calebjones.spacelaunchnow.api.terminus.models.PaginatedLauncherDetailedList as TerminusPaginatedLauncherDetailedList
import me.calebjones.spacelaunchnow.domain.mapper.toDomain
import me.calebjones.spacelaunchnow.domain.mapper.toDomainAgency
import me.calebjones.spacelaunchnow.domain.mapper.toVehicleDomain
import me.calebjones.spacelaunchnow.domain.model.Agency
import me.calebjones.spacelaunchnow.domain.model.LauncherDetail
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.VehicleConfig

// Terminus serves the LL2 shapes for agencies, launcher configurations and launchers. Each
// Terminus model is re-read as the LL model by the JSON bridge, then the one tested LL -> domain
// mapping runs, as the event and launch mappers do.

fun TerminusPaginatedAgencyNormalList.toLL(): LLPaginatedAgencyNormalList =
    bridgeJson.decodeFromString<LLPaginatedAgencyNormalList>(bridgeJson.encodeToString(this))

fun TerminusPaginatedAgencyNormalList.toDomain(): PaginatedResult<Agency> = toLL().toDomain()

fun TerminusAgencyEndpointDetailed.toLL(): LLAgencyEndpointDetailed =
    bridgeJson.decodeFromString<LLAgencyEndpointDetailed>(bridgeJson.encodeToString(this))

fun TerminusAgencyEndpointDetailed.toDomainAgency(): Agency = toLL().toDomainAgency()

fun TerminusPaginatedLauncherConfigNormalList.toLL(): LLPaginatedLauncherConfigNormalList =
    bridgeJson.decodeFromString<LLPaginatedLauncherConfigNormalList>(bridgeJson.encodeToString(this))

fun TerminusPaginatedLauncherConfigNormalList.toDomain(): PaginatedResult<VehicleConfig> = toLL().toDomain()

fun TerminusPaginatedLauncherConfigDetailedList.toLL(): LLPaginatedLauncherConfigDetailedList =
    bridgeJson.decodeFromString<LLPaginatedLauncherConfigDetailedList>(bridgeJson.encodeToString(this))

fun TerminusPaginatedLauncherConfigDetailedList.toDomain(): PaginatedResult<VehicleConfig> = toLL().toDomain()

fun TerminusLauncherConfigDetailed.toLL(): LLLauncherConfigDetailed =
    bridgeJson.decodeFromString<LLLauncherConfigDetailed>(bridgeJson.encodeToString(this))

fun TerminusLauncherConfigDetailed.toVehicleDomain(): VehicleConfig = toLL().toVehicleDomain()

fun TerminusPaginatedLauncherDetailedList.toLL(): LLPaginatedLauncherDetailedList =
    bridgeJson.decodeFromString<LLPaginatedLauncherDetailedList>(bridgeJson.encodeToString(this))

fun TerminusPaginatedLauncherDetailedList.toDomain(): PaginatedResult<LauncherDetail> = toLL().toDomain()

fun TerminusLauncherDetailed.toLL(): LLLauncherDetailed =
    bridgeJson.decodeFromString<LLLauncherDetailed>(bridgeJson.encodeToString(this))

fun TerminusLauncherDetailed.toDomain(): LauncherDetail = toLL().toDomain()
