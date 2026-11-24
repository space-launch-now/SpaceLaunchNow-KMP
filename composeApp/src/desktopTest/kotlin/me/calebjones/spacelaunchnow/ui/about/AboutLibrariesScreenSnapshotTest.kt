package me.calebjones.spacelaunchnow.ui.about

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.runComposeUiTest
import me.calebjones.spacelaunchnow.i18n.TextTreeSnapshot
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AboutLibrariesScreenSnapshotTest {

    @Test
    fun aboutScreenTextTree() = runComposeUiTest {
        setContent {
            SpaceLaunchNowPreviewTheme {
                AboutLibrariesScreen(onNavigateBack = {})
            }
        }
        // The heading renders before the libraries JSON loads; wait for a library row so the tree is complete.
        waitUntil(timeoutMillis = 15_000) {
            onAllNodesWithText("AboutLibraries Compose Material 3 Library").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
        TextTreeSnapshot.assertMatches("about_screen", TextTreeSnapshot.dump(this))
    }
}
