package me.calebjones.spacelaunchnow.data.notifications

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import me.calebjones.spacelaunchnow.shared.R

/**
 * Builds the launch push body for each notification type from the real `strings.xml` files,
 * in English and in German, Spanish and Chinese.
 */
class LaunchBodyTextTest {

    private val resDir = listOf(
        File("src/androidMain/res"),
        File("composeApp/src/androidMain/res"),
    ).first { it.isDirectory }

    private fun strings(folder: String): Map<String, String> {
        val doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(File(resDir, "$folder/strings.xml"))
        val nodes = doc.getElementsByTagName("string")
        return (0 until nodes.length).associate {
            val node = nodes.item(it)
            node.attributes.getNamedItem("name").nodeValue to
                node.textContent.replace("\'", "'")
        }
    }

    /** Builds a body the way the app does, reading text from one language's file. */
    private fun body(
        language: Map<String, String>,
        type: String,
        serverBody: String? = null,
        time: String = "3:04 PM",
        location: String = "Cape Canaveral",
    ): String {
        val nameById = LaunchBodyText.entries.associate { it.resId to it.resName }
        return buildLaunchBody(
            notificationType = type,
            launchLocation = location,
            serverBody = serverBody,
            formattedTime = { time },
            getString = { resId, args ->
                String.format(language.getValue(nameById.getValue(resId)), *args.toTypedArray())
            },
        )
    }

    private val english = strings("values")
    private val languages = listOf("de", "es", "zh-rCN").associateWith { strings("values-$it") }

    private val typesWithTime = listOf(
        "netstampChanged", "oneMinute", "tenMinutes", "oneHour", "twentyFourHour",
    )
    private val typesWithoutTime = listOf("success", "failure", "partialFailure", "inFlight")

    @Test
    fun resourceNamesMatchResourceIds() {
        LaunchBodyText.entries.forEach {
            assertEquals(R.string::class.java.getField(it.resName).getInt(null), it.resId, it.resName)
        }
    }

    @Test
    fun everyTypeInEnglishKeepsTheOriginalWording() {
        val expected = mapOf(
            "netstampChanged" to "SCHEDULE UPDATE: Next attempt no earlier than 3:04 PM",
            "success" to "The launch was successful!",
            "failure" to "A launch failure has occurred.",
            "partialFailure" to "The launch was a partial failure.",
            "inFlight" to "Liftoff! Launch vehicle is now in flight!",
            "oneMinute" to "Launch attempt in less than one minute at 3:04 PM",
            "tenMinutes" to "Launch attempt in less than ten minutes at 3:04 PM",
            "oneHour" to "Launch attempt in less than one hour at 3:04 PM",
            "twentyFourHour" to "Launch attempt in less than 24 hours at 3:04 PM",
        )
        expected.forEach { (type, text) -> assertEquals(text, body(english, type), type) }
    }

    @Test
    fun everyTypeInEachLanguageIsTranslatedAndKeepsTheTime() {
        languages.forEach { (folder, language) ->
            (typesWithTime + typesWithoutTime).forEach { type ->
                val text = body(language, type)
                assertNotEquals(body(english, type), text, "$folder $type is not translated")
            }
            typesWithTime.forEach { type ->
                assertTrue("3:04 PM" in body(language, type), "$folder $type lost the time")
            }
        }
    }

    @Test
    fun typeMatchingIgnoresCase() {
        assertEquals(body(english, "ONEHOUR"), body(english, "oneHour"))
    }

    @Test
    fun unknownTypeShowsTheServerText() {
        languages.values.plus(english).forEach {
            assertEquals("Server says hi", body(it, "webcastLive", serverBody = "Server says hi"))
        }
    }

    @Test
    fun unknownTypeWithoutServerTextFallsBackToLaunchFrom() {
        assertEquals("Launch from Cape Canaveral", body(english, "webcastLive"))
        assertEquals("Launch from Cape Canaveral", body(english, "webcastLive", serverBody = "  "))
        assertNotNull(languages.getValue("de")["notification_fallback_launch_from"])
        assertEquals("Start von Cape Canaveral", body(languages.getValue("de"), "webcastLive"))
    }

    @Test
    fun knownTypeIgnoresTheServerText() {
        assertEquals("The launch was successful!", body(english, "success", serverBody = "Server says hi"))
    }

    @Test
    fun timeIsFormattedOnlyForTypesThatShowIt() {
        var calls = 0
        buildLaunchBody("success", "x", null, { calls++; "t" }, { _, _ -> "s" })
        assertEquals(0, calls)
        buildLaunchBody("oneHour", "x", null, { calls++; "t" }, { _, _ -> "s" })
        assertEquals(1, calls)
    }
}
