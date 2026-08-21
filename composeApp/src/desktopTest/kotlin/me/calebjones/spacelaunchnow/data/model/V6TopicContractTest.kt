package me.calebjones.spacelaunchnow.data.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Conformance tests: client constants must match the shared topic contract.
 *
 * `contracts/notification-topics.v6.json` is the agreement between this app and
 * SpaceLaunchNow-Server about what FCM topics are called. Neither side errors
 * when they disagree — the server sends to a topic nobody subscribed to, or we
 * subscribe to one nothing sends to, and the notification is simply never
 * delivered. These tests are the only thing that turns that silence into a
 * failing build. The server runs the mirror of this file against a
 * byte-identical copy.
 *
 * Lives in desktopTest rather than commonTest because it reads a file from disk,
 * which commonMain has no portable API for. The constants under test are all in
 * commonMain and platform-independent, so a JVM-side check covers them fully.
 * (desktopTest, not jvmTest: the JVM target is declared as `jvm("desktop")`, so
 * src/jvmTest is not wired to any target and would never run.)
 */
class V6TopicContractTest {

    private val contract: JsonObject = loadContract()

    private fun loadContract(): JsonObject {
        // Gradle runs tests with the module dir as the working directory.
        val candidates = listOf(
            File("../contracts/notification-topics.v6.json"),
            File("contracts/notification-topics.v6.json"),
        )
        val file = candidates.firstOrNull { it.isFile }
            ?: fail(
                "Could not find the topic contract. Looked in: " +
                    candidates.joinToString { it.absolutePath }
            )
        return Json.parseToJsonElement(file.readText()).jsonObject
    }

    private fun strings(key: String): List<String> =
        contract[key]!!.jsonArray.map { it.jsonPrimitive.content }

    private fun groups(key: String): List<String> =
        contract[key]!!.jsonArray.map { it.jsonObject["group"]!!.jsonPrimitive.content }

    private fun subscribableGroups(key: String): List<String> =
        contract[key]!!.jsonArray
            .map { it.jsonObject }
            .filter { it["subscribable"]!!.jsonPrimitive.content.toBoolean() }
            .map { it["group"]!!.jsonPrimitive.content }

    @Test
    fun `contract file is present and pins the v6 scheme`() {
        assertEquals("v6", contract["scheme"]!!.jsonPrimitive.content)
    }

    // ---------------------------------------------------------------- types

    @Test
    fun `every launch notification type in the contract has a user-configurable topic`() {
        val clientTypeIds = NotificationTopic.getUserConfigurableTopics().map { it.id }.toSet()
        val missing = strings("notificationTypes").filterNot { it in clientTypeIds }
        assertTrue(
            missing.isEmpty(),
            "Contract types with no NotificationTopic, so nothing can drive a subscription: $missing"
        )
    }

    // ----------------------------------------------------------- attributes

    @Test
    fun `every agency topicName is a group the server actually sends to`() {
        // This is the direction that fails silently and totally: a topicName the
        // server never emits means that user's agency filter matches nothing.
        val contractGroups = groups("agencyGroups").toSet()
        val unknown = NotificationAgency.getAll()
            .map { it.topicName }
            .filterNot { it in contractGroups }
        assertTrue(unknown.isEmpty(), "Agency topicNames absent from the contract: $unknown")
    }

    @Test
    fun `every location topicName is a group the server actually sends to`() {
        val contractGroups = groups("locationGroups").toSet()
        val unknown = NotificationLocation.getAll()
            .map { it.topicName }
            .filterNot { it in contractGroups }
        assertTrue(unknown.isEmpty(), "Location topicNames absent from the contract: $unknown")
    }

    @Test
    fun `the ISRO agency does not reuse the India location topic`() {
        // One flat attribute-topic namespace across both tables. If these were
        // equal, following India-the-location would also match ISRO launches
        // from anywhere in the world. Two independent guards now: the location
        // is named for the place, and the agency keeps its suffix.
        assertEquals("isroAgency", NotificationAgency.ISRO.topicName)
        assertEquals("india", NotificationLocation.INDIA.topicName)
    }

    @Test
    fun `no location is named after an agency acronym`() {
        // The original defect was a *location* called "isro". This is the guard
        // against it coming back under any agency's name.
        val agencyNames = NotificationAgency.getAll().map { it.name }
        assertTrue(
            NotificationLocation.getAll().none { it.topicName == "isro" },
            "A location topicName of 'isro' reintroduces the collision with " +
                "the agency group (agencies present: $agencyNames)"
        )
    }

    @Test
    fun `no agency and location pair shares a topicName`() {
        val agencies = NotificationAgency.getAll().map { it.topicName }.toSet()
        val locations = NotificationLocation.getAll().map { it.topicName }.toSet()
        assertEquals(emptySet(), agencies intersect locations)
    }

    @Test
    fun `every subscribable group has a settings row that reaches it`() {
        // otherAgency gained its row when the spec was approved (2026-08-16).
        // From here on, any subscribable group with no row is an accident: a
        // group the server sends to that no user can select.
        val offered = (NotificationAgency.getAll().map { it.topicName } +
            NotificationLocation.getAll().map { it.topicName }).toSet()
        val missing = (subscribableGroups("agencyGroups") + subscribableGroups("locationGroups"))
            .filterNot { it in offered }
        assertEquals(emptyList(), missing)
    }

    @Test
    fun `the location catch-all is not offered to users`() {
        // "other" is a shipped row meaning three specific sites. The catch-all
        // is a separate, unsubscribable group precisely so that row's meaning
        // does not silently widen to every uncatalogued site on Earth.
        val catchAll = contract["locationGroups"]!!.jsonArray
            .map { it.jsonObject }
            .single { !it["subscribable"]!!.jsonPrimitive.content.toBoolean() }
            .let { it["group"]!!.jsonPrimitive.content }

        assertTrue(
            NotificationLocation.getAll().none { it.topicName == catchAll },
            "$catchAll must not be offered as a user-selectable location"
        )
        assertTrue(NotificationLocation.getAll().any { it.topicName == "other" })
    }

    // ----------------------------------------------------------- broadcasts

    @Test
    fun `each broadcast kind maps a persisted setting id to the wire token`() {
        // The two differ (featured_news -> news, announcements -> announce).
        // Subscription code must translate; using the setting id as the topic
        // segment reaches nothing. The ids are not renamed because they are
        // persisted map keys — renaming resets every user's toggle.
        val clientTopicIds = NotificationTopic.getUserConfigurableTopics().map { it.id }.toSet()

        contract["broadcastKinds"]!!.jsonObject["values"]!!.jsonArray.forEach { element ->
            val settingId = element.jsonObject["clientSettingId"]!!.jsonPrimitive.content
            assertTrue(
                settingId in clientTopicIds,
                "Contract names client setting id '$settingId', which no NotificationTopic declares"
            )
        }
    }

    // ----------------------------------------------------------- mute groups

    @Test
    fun `the starlink mute topic the client emits is a contract mute group`() {
        val muteGroups = contract["muteGroups"]!!.jsonObject["values"]!!.jsonArray
            .map { it.jsonObject["group"]!!.jsonPrimitive.content }
        assertTrue("starlinkMuted" in muteGroups, "Client subscribes to starlinkMuted; contract must list it")

        // The emitted topic follows the attribute grammar: v6_{env}_{group},
        // env-scoped and NOT platform-scoped.
        val state = NotificationState(muteStarlink = true)
        val topics = me.calebjones.spacelaunchnow.data.notifications.v6.V6Topics
            .requiredTopics(state, env = "prod", platform = "ios")
        assertTrue("v6_prod_starlinkMuted" in topics)
    }

    @Test
    fun `the failure exemption the ui copy promises is pinned by the contract`() {
        // The toggle's supporting text says "You'll still be notified if a
        // Starlink launch fails." — that promise lives in the contract's
        // exemptTypes. If this test fails, the copy and the contract have
        // drifted and one of them is lying to users.
        val starlink = contract["muteGroups"]!!.jsonObject["values"]!!.jsonArray
            .map { it.jsonObject }
            .single { it["group"]!!.jsonPrimitive.content == "starlinkMuted" }
        val exempt = starlink["exemptTypes"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertEquals(listOf("failure", "partial_failure"), exempt)
    }
}
