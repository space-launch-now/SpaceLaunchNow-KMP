package me.calebjones.spacelaunchnow.ui.about

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.i18n.TextTreeSnapshot
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AboutLibrariesScreenSnapshotTest {

    @Test
    fun aboutScreenTextTree() = runComposeUiTest {
        setContent {
            SpaceLaunchNowPreviewTheme {
                // A tall viewport so the first library row is composed on any OS. With the default
                // window, font metrics decide whether the lazy list reaches it (it did not on CI).
                Box(Modifier.requiredSize(1000.dp, 4000.dp)) {
                    AboutLibrariesScreen(onNavigateBack = {})
                }
            }
        }
        // The heading renders before the libraries list, so wait for the first library row.
        waitUntil(timeoutMillis = 15_000) {
            onAllNodesWithText("AboutLibraries Compose Material 3 Library").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
        TextTreeSnapshot.assertMatches("about_screen", firstLibraryOnly(TextTreeSnapshot.dump(this)))
    }

    /**
     * Keeps the screen text, the first library row and the top bar. The 515 later rows add nothing
     * to the i18n check and their count depends on the viewport.
     */
    private fun firstLibraryOnly(tree: String): String {
        val lines = tree.lines()
        val heading = lines.indexOf("text=\"Open Source Libraries\"")
        val firstRowEnd = (heading + 2 until lines.size).first { !lines[it].startsWith("  ") }
        val topBar = lines.indexOf("  description=\"Back\"") - 1
        return (lines.take(firstRowEnd) + lines.drop(topBar)).joinToString("\n")
    }
}
