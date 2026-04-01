package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.isIOS
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Onboarding page showcasing platform-specific home screen widgets.
 * iOS: Shows a medium home screen widget mockup.
 * Android: Shows a launch list widget mockup.
 */
@Composable
fun WidgetsPage(modifier: Modifier = Modifier, isIOS: Boolean = getPlatform().type == PlatformType.IOS) {
    OnboardingPage(
        title = "Widgets at a Glance",
        subtitle = if (isIOS)
            "Keep track of launches right from your Lock Screen and Home Screen."
        else
            "Add a launch widget to your Home Screen.",
        icon = Icons.Default.Widgets,
        modifier = modifier
    ) {
        if (isIOS) {
            IOSWidgetShowcaseContent(modifier = Modifier.fillMaxSize())
        } else {
            AndroidWidgetShowcaseContent(modifier = Modifier.fillMaxSize())
        }
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

@Preview
@Composable
private fun WidgetsPageIOSDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        WidgetsPage(isIOS=true)
    }
}
