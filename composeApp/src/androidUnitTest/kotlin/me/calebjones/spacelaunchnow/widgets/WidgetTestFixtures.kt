package me.calebjones.spacelaunchnow.widgets

import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchStatus
import me.calebjones.spacelaunchnow.domain.model.Provider
import kotlin.time.Instant

internal fun widgetLaunch(id: String, net: Instant?, statusId: Int = 1) = Launch(
    id = id,
    name = "Launch $id",
    slug = id,
    net = net,
    windowStart = null,
    windowEnd = null,
    lastUpdated = null,
    status = LaunchStatus(id = statusId, name = "Status $statusId", abbrev = null, description = null),
    provider = Provider(
        id = 1, name = "SpaceX", abbrev = null, type = null, countryCode = null,
        logoUrl = null, socialLogo = null, imageUrl = null
    ),
    imageUrl = null,
    thumbnailUrl = null,
    infographic = null,
    netPrecision = null
)
