package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Onboarding page showcasing iOS lock screen and home screen widgets.
 * Only shown on iOS — displays static widget mockups inside a device frame.
 */
@Composable
fun WidgetsPage(modifier: Modifier = Modifier) {
    OnboardingPage(
        title = "Widgets at a Glance",
        subtitle = "Keep track of launches right from your Lock Screen and Home Screen — exclusive to Premium.",
        icon = Icons.Default.Widgets,
        modifier = modifier
    ) {
        WidgetShowcaseContent(modifier = Modifier.fillMaxSize())
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun WidgetsPagePreview() {
    SpaceLaunchNowPreviewTheme {
        WidgetsPage()
    }
}

@Preview
@Composable
private fun WidgetsPageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        WidgetsPage()
    }
}
