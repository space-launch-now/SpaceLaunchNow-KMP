package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.ui.compose.NextUpShimmerBox
import me.calebjones.spacelaunchnow.ui.home.components.NextLaunchItemView
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Onboarding page 1 — shows a live [NextLaunchItemView] with real launch data
 * inside a device frame.
 */
@Composable
fun LaunchCardPage(
    modifier: Modifier = Modifier,
    nextLaunch: LaunchNormal? = null
) {
    val navController = rememberNavController()

    OnboardingPage(
        title = "Track Every Launch",
        subtitle = "Get real-time updates on upcoming rocket launches from agencies worldwide.",
        icon = Icons.Default.RocketLaunch,
        modifier = modifier
    ) {
        if (nextLaunch != null) {
            NextLaunchItemView(
                launch = nextLaunch,
                navController = navController,
                onShare = {}
            )
        } else {
            Box(modifier = Modifier.fillMaxSize()) {
                NextUpShimmerBox()
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun LaunchCardPagePreview() {
    SpaceLaunchNowPreviewTheme {
        LaunchCardPage()
    }
}

@Preview
@Composable
private fun LaunchCardPageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        LaunchCardPage()
    }
}
