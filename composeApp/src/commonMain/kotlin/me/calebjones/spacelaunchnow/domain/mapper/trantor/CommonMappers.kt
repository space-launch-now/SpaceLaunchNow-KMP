package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.domain.model.LaunchRef
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult
import me.calebjones.spacelaunchnow.domain.model.ProgramSummary
import me.calebjones.spacelaunchnow.domain.model.UpdateEventRef
import me.calebjones.spacelaunchnow.domain.model.Update as DomainUpdate

// Trantor's standalone updates feed is a flat row: launch_id/launch_name, event_id/event_name,
// and program_id/program_name are all denormalized (no nested launch/event/program objects).
// `program` (E8(P5)) joins `programs.name` at read time from the row's own `program_id` —
// deliberately not derived from the `program_ids` filter's junction reach.
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
        program = programId?.let { pid ->
            ProgramSummary(
                id = pid,
                name = programName ?: "",
                imageUrl = null,
                description = null,
                infoUrl = null,
                wikiUrl = null,
                type = null
            )
        }
    )

fun me.calebjones.spacelaunchnow.api.trantor.models.PaginatedResponseUpdateList.toDomain(): PaginatedResult<DomainUpdate> =
    PaginatedResult(
        count = count,
        next = next,
        previous = previous,
        results = results.map { it.toDomain() }
    )
