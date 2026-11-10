package me.calebjones.spacelaunchnow.api.extensions.terminus

import me.calebjones.spacelaunchnow.api.terminus.apis.LaunchesApi
import me.calebjones.spacelaunchnow.api.terminus.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed

/**
 * Terminus launch detail: `GET /api/v1/launches/{id}/`. Wraps the generated
 * `launchesRetrieve` so the repository has one call site that does not touch the generated
 * method name. The caller checks [HttpResponse.success]; a 404 is not thrown here.
 */
suspend fun LaunchesApi.getLaunchDetail(id: String): HttpResponse<LaunchDetailed> =
    launchesRetrieve(id)
