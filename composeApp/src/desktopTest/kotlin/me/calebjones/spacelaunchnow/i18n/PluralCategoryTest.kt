package me.calebjones.spacelaunchnow.i18n

import kotlinx.coroutines.test.runTest
import org.jetbrains.compose.resources.getPluralString
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.test_plural_launches

/**
 * Proves that Compose resources pick the CLDR plural category per language (spec 017, 5.4).
 * The test strings live in `src/desktopTest/composeResources`; each value shows its category.
 */
class PluralCategoryTest {
    private val defaultLocale = Locale.getDefault()

    @AfterTest
    fun restoreLocale() = Locale.setDefault(defaultLocale)

    private suspend fun category(language: String, quantity: Int): String {
        Locale.setDefault(Locale.forLanguageTag(language))
        return getPluralString(Res.plurals.test_plural_launches, quantity, quantity)
    }

    @Test
    fun ukrainianPicksOneFewManyOther() = runTest {
        val expected = mapOf(
            1 to "one", 21 to "one", 101 to "one",
            2 to "few", 3 to "few", 4 to "few", 22 to "few",
            0 to "many", 5 to "many", 11 to "many", 14 to "many", 20 to "many", 25 to "many",
        )
        expected.forEach { (n, cat) -> assertEquals("$cat:$n", category("uk", n), "uk $n") }
    }

    @Test
    fun polishPicksOneFewManyOther() = runTest {
        val expected = mapOf(
            1 to "one",
            2 to "few", 3 to "few", 4 to "few", 22 to "few", 24 to "few",
            0 to "many", 5 to "many", 12 to "many", 14 to "many", 21 to "many", 25 to "many",
        )
        expected.forEach { (n, cat) -> assertEquals("$cat:$n", category("pl", n), "pl $n") }
    }

    @Test
    fun englishPicksOneAndOther() = runTest {
        assertEquals("1 launch", category("en", 1))
        assertEquals("2 launches", category("en", 2))
    }
}
