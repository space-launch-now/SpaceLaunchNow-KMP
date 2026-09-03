package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.domain.model.LaunchRef
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.UpdateEventRef
import me.calebjones.spacelaunchnow.domain.model.Update as DomainUpdate

// Trantor's standalone updates feed is a flat row: launch_id/launch_name and event_id/event_name
// are denormalized (no nested launch/event objects), and program_id has no accompanying name at
// all. `program` is left null rather than fabricated — see the events-updates unit report.
fun me.calebjones.spacelaunchnow.api.trantor.models.UpdateList.toDomain(): DomainUpdate =
    DomainUpdate(
        id = id,
        profileImage = profileImage,
        comment = comment,
        infoUrl = infoUrl,
        createdBy = createdBy,
        createdOn = createdOn,
        launch = launchId?.let { id -> LaunchRef(id = id, name = launchName ?: "") },
        event = eventId?.let { id -> UpdateEventRef(id = id, name = eventName ?: "") },
        program = null
    )

fun me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseUpdateList.toDomain(): PaginatedResult<DomainUpdate> =
    PaginatedResult(
        count = count,
        next = next,
        previous = previous,
        results = results.map { it.toDomain() }
    )
