package me.calebjones.spacelaunchnow.ui.screens

import me.calebjones.spacelaunchnow.domain.model.InfoLink
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.domain.model.LaunchRef
import me.calebjones.spacelaunchnow.domain.model.Orbit
import me.calebjones.spacelaunchnow.domain.model.RocketConfig
import me.calebjones.spacelaunchnow.domain.model.RocketManufacturer
import me.calebjones.spacelaunchnow.domain.model.TimelineEntry
import me.calebjones.spacelaunchnow.domain.model.Update
import me.calebjones.spacelaunchnow.domain.model.VideoLink
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import kotlin.time.Instant

/**
 * Fake data for the dense-screen layout tests. The launch sits far in the future so its countdown
 * keeps the same shape for decades, and it has every optional section filled so each label shows.
 */
object ScreenFixtures {
    private val farFuture = Instant.parse("2099-06-01T12:00:00Z")

    val rocket = RocketConfig(
        id = 164,
        name = "Falcon 9 Block 5",
        fullName = "Falcon 9 Block 5",
        family = "Falcon",
        variant = "Block 5",
        imageUrl = null,
        active = true,
        reusable = true,
        description = "A two-stage, partially reusable medium-lift launch vehicle.",
        manufacturer = RocketManufacturer(121, "SpaceX"),
        minStage = 1, maxStage = 2,
        length = 70.0, diameter = 3.7, launchMass = 549.0,
        leoCapacity = 22800.0, gtoCapacity = 8300.0, toThrust = 7607.0,
        launchCost = 67, totalLaunchCount = 400, successfulLaunches = 398,
        failedLaunches = 1, pendingLaunches = 20,
        attemptedLandings = 350, successfulLandings = 345,
    )

    val launch: Launch = PreviewData.domainLaunchCrewMission.copy(
        net = farFuture,
        windowStart = farFuture,
        windowEnd = farFuture,
        rocket = rocket,
        probability = 80,
        weatherConcerns = "Upper level winds may delay the launch.",
        mission = PreviewData.domainMissionCrew.copy(
            description = "A long-duration mission that carries four crew members to the International Space Station.",
            orbit = Orbit(1, "Low Earth Orbit", "LEO"),
        ),
        updates = listOf(
            Update(
                id = 1, profileImage = null,
                comment = "The launch window opens at the time shown, and the team is watching the weather.",
                infoUrl = null, createdBy = "Launch Team", createdOn = Instant.parse("2099-05-30T12:00:00Z"),
                launch = LaunchRef("ghi-789", "Falcon 9 Block 5 | Crew-8"),
            ),
        ),
        infoUrls = listOf(InfoLink("https://example.invalid/info", "Mission press kit", "Example", null, null, "Press kit", 1)),
        vidUrls = listOf(VideoLink("https://example.invalid/live", "Launch webcast", "Example", null, null, null, true, 1)),
        timeline = listOf(TimelineEntry("Liftoff", "T+00:00:00"), TimelineEntry("Max Q", "T+00:01:12")),
    )

    val upcomingLaunches: List<Launch> = listOf(
        launch,
        PreviewData.domainLaunchSpaceX.copy(net = farFuture),
        PreviewData.domainLaunchULA.copy(net = farFuture),
    )
}
