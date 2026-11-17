package me.calebjones.spacelaunchnow.rating

import me.calebjones.spacelaunchnow.rating.NotificationRatingRule.Decision
import me.calebjones.spacelaunchnow.rating.NotificationRatingRule.Input
import kotlin.test.Test
import kotlin.test.assertEquals

class NotificationRatingRuleTest {

    private val now = 1_800_000_000_000L
    private val day = 24L * 60 * 60 * 1000

    private fun allow() = Input(
        launchCount = 5,
        lastPromptAtMs = null,
        nowMs = now,
        hasRated = false,
        statusId = 1,
        flagEnabled = true,
    )

    @Test
    fun `allow case shows`() = assertEquals(Decision.SHOW, NotificationRatingRule.evaluate(allow()))

    @Test
    fun `flag off blocks`() = assertEquals(
        Decision.BLOCKED_FLAG_OFF,
        NotificationRatingRule.evaluate(allow().copy(flagEnabled = false))
    )

    @Test
    fun `already rated blocks`() = assertEquals(
        Decision.BLOCKED_ALREADY_RATED,
        NotificationRatingRule.evaluate(allow().copy(hasRated = true))
    )

    @Test
    fun `launch count 4 blocks and 5 allows`() {
        assertEquals(Decision.BLOCKED_TOO_FEW_LAUNCHES, NotificationRatingRule.evaluate(allow().copy(launchCount = 4)))
        assertEquals(Decision.SHOW, NotificationRatingRule.evaluate(allow().copy(launchCount = 5)))
    }

    @Test
    fun `first session never prompts`() {
        listOf(0L, 1L).forEach {
            assertEquals(Decision.BLOCKED_TOO_FEW_LAUNCHES, NotificationRatingRule.evaluate(allow().copy(launchCount = it)))
        }
    }

    @Test
    fun `day 119 blocks and day 120 allows`() {
        assertEquals(
            Decision.BLOCKED_COOLDOWN,
            NotificationRatingRule.evaluate(allow().copy(lastPromptAtMs = now - 119 * day))
        )
        assertEquals(
            Decision.SHOW,
            NotificationRatingRule.evaluate(allow().copy(lastPromptAtMs = now - 120 * day))
        )
    }

    @Test
    fun `prompt just now blocks`() = assertEquals(
        Decision.BLOCKED_COOLDOWN,
        NotificationRatingRule.evaluate(allow().copy(lastPromptAtMs = now))
    )

    @Test
    fun `go success and in flight allow`() {
        listOf(1, 3, 6).forEach {
            assertEquals(Decision.SHOW, NotificationRatingRule.evaluate(allow().copy(statusId = it)))
        }
    }

    @Test
    fun `other statuses and null block`() {
        listOf(2, 4, 5, 7, 8, 9, 0, 99, null).forEach {
            assertEquals(
                Decision.BLOCKED_STATUS,
                NotificationRatingRule.evaluate(allow().copy(statusId = it)),
                "status $it"
            )
        }
    }
}
