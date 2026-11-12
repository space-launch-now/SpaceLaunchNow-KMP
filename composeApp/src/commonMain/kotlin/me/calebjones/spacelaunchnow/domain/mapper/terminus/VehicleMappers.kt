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

private inline fun <reified T, reified R> T.bridge(): R = bridgeJson.decodeFromString<R>(bridgeJson.encodeToString(this))

fun TerminusPaginatedAgencyNormalList.toLL(): LLPaginatedAgencyNormalList =
    this.bridge()

fun TerminusPaginatedAgencyNormalList.toDomain(): PaginatedResult<Agency> = toLL().toDomain()

fun TerminusAgencyEndpointDetailed.toLL(): LLAgencyEndpointDetailed =
    this.bridge()

fun TerminusAgencyEndpointDetailed.toDomainAgency(): Agency = toLL().toDomainAgency()

fun TerminusPaginatedLauncherConfigNormalList.toLL(): LLPaginatedLauncherConfigNormalList =
    this.bridge()

fun TerminusPaginatedLauncherConfigNormalList.toDomain(): PaginatedResult<VehicleConfig> = toLL().toDomain()

fun TerminusPaginatedLauncherConfigDetailedList.toLL(): LLPaginatedLauncherConfigDetailedList =
    this.bridge()

fun TerminusPaginatedLauncherConfigDetailedList.toDomain(): PaginatedResult<VehicleConfig> = toLL().toDomain()

fun TerminusLauncherConfigDetailed.toLL(): LLLauncherConfigDetailed =
    this.bridge()

fun TerminusLauncherConfigDetailed.toVehicleDomain(): VehicleConfig = toLL().toVehicleDomain()

fun TerminusPaginatedLauncherDetailedList.toLL(): LLPaginatedLauncherDetailedList =
    this.bridge()

fun TerminusPaginatedLauncherDetailedList.toDomain(): PaginatedResult<LauncherDetail> = toLL().toDomain()

fun TerminusLauncherDetailed.toLL(): LLLauncherDetailed =
    this.bridge()

fun TerminusLauncherDetailed.toDomain(): LauncherDetail = toLL().toDomain()
