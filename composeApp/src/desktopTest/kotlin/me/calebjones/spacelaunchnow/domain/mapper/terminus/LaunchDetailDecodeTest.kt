package me.calebjones.spacelaunchnow.domain.mapper.terminus

import kotlinx.serialization.json.Json
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.RocketDetailed
import me.calebjones.spacelaunchnow.api.terminus.models.LaunchTimeline
import kotlin.test.Test
import kotlin.test.assertTrue

/** Real prod `launches/{id}/`, `/rocket/` and `/timeline/` payloads (2026-11-21) through the bridge. */
class LaunchDetailDecodeTest {
    private val json = Json { ignoreUnknownKeys = true }
    private fun res(name: String) = checkNotNull(javaClass.getResourceAsStream("/terminus/$name")).bufferedReader().use { it.readText() }

    @Test
    fun `prod launch detail with rocket families and manufacturers decodes`() {
        val launch = json.decodeFromString<LaunchDetailed>(res("prod_launch.json"))
        val rocket = json.decodeFromString<RocketDetailed>(res("prod_launch_rocket.json"))
        val timeline = json.decodeFromString<LaunchTimeline>(res("prod_launch_timeline.json"))
        assertTrue(rocket.configuration.families.orEmpty().any { !it.manufacturer.isNullOrEmpty() })
        val domain = TerminusLaunchDetail(launch, rocket, timeline.timeline).toDomain()
        assertTrue(domain.id.isNotEmpty())
    }
}
