package me.calebjones.spacelaunchnow.domain.mapper.trantor

import kotlin.time.Instant
import me.calebjones.spacelaunchnow.api.trantor.models.InfoUrl
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchDetail
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchList
import me.calebjones.spacelaunchnow.api.trantor.models.LaunchUpdate
import me.calebjones.spacelaunchnow.api.trantor.models.VidUrl
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** E14(P5): attempt counters, link art, update avatars, and the new list-row fields. */
class LaunchParityMappersTest {

    private fun detail(
        orbital: Int? = null,
        orbitalYear: Int? = null,
        location: Int? = null,
        locationYear: Int? = null,
        pad: Int? = null,
        padYear: Int? = null,
        agency: Int? = null,
        agencyYear: Int? = null
    ) = LaunchDetail(
        id = "d-1",
        name = "Falcon 9 | Starlink",
        net = Instant.parse("2026-01-01T00:00:00Z"),
        slug = "falcon-9-starlink",
        status = "Go",
        statusId = 1,
        orbitalLaunchAttemptCount = orbital,
        orbitalLaunchAttemptCountYear = orbitalYear,
        locationLaunchAttemptCount = location,
        locationLaunchAttemptCountYear = locationYear,
        padLaunchAttemptCount = pad,
        padLaunchAttemptCountYear = padYear,
        agencyLaunchAttemptCount = agency,
        agencyLaunchAttemptCountYear = agencyYear
    )

    private fun vid(description: String? = null, featureImage: String? = null) = VidUrl(
        url = "https://v",
        description = description,
        featureImage = featureImage,
        priority = 1
    )

    private fun info(description: String? = null, featureImage: String? = null) = InfoUrl(
        url = "https://i",
        description = description,
        featureImage = featureImage,
        priority = 1
    )

    private fun update(profileImage: String? = null) = LaunchUpdate(
        id = 1,
        comment = "Slipped",
        createdOn = Instant.parse("2026-01-01T00:00:00Z"),
        profileImage = profileImage
    )

    @Test
    fun attemptCountsMapAllEightFields() {
        val counts = detail(1, 2, 3, 4, 5, 6, 7, 8).toDomain().launchAttemptCounts
        assertEquals(1, counts?.orbital)
        assertEquals(2, counts?.orbitalYear)
        assertEquals(3, counts?.location)
        assertEquals(4, counts?.locationYear)
        assertEquals(5, counts?.pad)
        assertEquals(6, counts?.padYear)
        assertEquals(7, counts?.agency)
        assertEquals(8, counts?.agencyYear)
    }

    @Test
    fun attemptCountsAreNullWhenNoneServed() {
        assertNull(detail().toDomain().launchAttemptCounts)
    }

    @Test
    fun attemptCountsKeepPartialValues() {
        val counts = detail(pad = 9).toDomain().launchAttemptCounts
        assertEquals(9, counts?.pad)
        assertNull(counts?.orbital)
    }

    @Test
    fun linkArtAndUpdateAvatarAreMapped() {
        val domain = detail().copy(
            vidUrls = listOf(vid(description = "Replay", featureImage = "https://v.png")),
            infoUrls = listOf(info(description = "Read", featureImage = "https://i.png")),
            updates = listOf(update(profileImage = "https://p.png"))
        ).toDomain()
        assertEquals("Replay", domain.vidUrls.single().description)
        assertEquals("https://v.png", domain.vidUrls.single().featureImage)
        assertEquals("Read", domain.infoUrls.single().description)
        assertEquals("https://i.png", domain.infoUrls.single().featureImage)
        assertEquals("https://p.png", domain.updates.single().profileImage)
    }

    @Test
    fun linkArtAndUpdateAvatarStayNullWhenAbsent() {
        val domain = detail().copy(
            vidUrls = listOf(vid()),
            infoUrls = listOf(info()),
            updates = listOf(update())
        ).toDomain()
        assertNull(domain.vidUrls.single().featureImage)
        assertNull(domain.infoUrls.single().description)
        assertNull(domain.updates.single().profileImage)
    }

    private fun row(
        providerAbbrev: String? = null,
        missionDescription: String? = null,
        fullName: String? = null,
        variant: String? = null
    ) = LaunchList(
        id = "row-1",
        name = "Falcon 9 | Starlink",
        net = Instant.parse("2026-01-01T00:00:00Z"),
        slug = "falcon-9-starlink",
        status = "Go",
        statusId = 1,
        providerId = 121,
        providerName = "SpaceX",
        providerAbbrev = providerAbbrev,
        rocketId = 8,
        configurationName = "Falcon 9",
        configurationFullName = fullName,
        configurationVariant = variant,
        missionId = 3,
        missionName = "Starlink",
        missionDescription = missionDescription
    )

    @Test
    fun listRowMapsAbbrevMissionDescriptionAndConfigNames() {
        val domain = row("SpX", "Batch of satellites", "Falcon 9 Block 5", "Block 5").toDomain()
        assertEquals("SpX", domain.provider.abbrev)
        assertEquals("Batch of satellites", domain.mission?.description)
        assertEquals("Falcon 9 Block 5", domain.rocket?.fullName)
        assertEquals("Block 5", domain.rocket?.variant)
    }

    @Test
    fun listRowFieldsStayNullWhenAbsent() {
        val domain = row().toDomain()
        assertNull(domain.provider.abbrev)
        assertNull(domain.mission?.description)
        assertNull(domain.rocket?.fullName)
        assertNull(domain.rocket?.variant)
    }
}
