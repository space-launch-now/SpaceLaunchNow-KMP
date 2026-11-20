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
        waitUntil(timeoutMillis = 15_000) {
            onAllNodesWithText("Open Source Libraries").fetchSemanticsNodes().isNotEmpty()
        }
        waitForIdle()
        TextTreeSnapshot.assertMatches("about_screen", TextTreeSnapshot.dump(this))
    }
}
