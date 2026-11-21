package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.onboarding.DeviceFrameStyle
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_10min
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_24h
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_agencies
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_california
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_florida
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_locations
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_texas
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_timing
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_filters_title

/**
 * Onboarding page 3 — shows mock notification filter options with
 * agency checkboxes, location items, and topic toggles inside a device frame.
 */
@Composable
fun NotificationFiltersPage(modifier: Modifier = Modifier) {
    OnboardingPage(
        title = stringResource(Res.string.onboarding_filters_title),
        subtitle = stringResource(Res.string.onboarding_filters_subtitle),
        modifier = modifier
    ) {
        NotificationFiltersPreviewContent()
    }
}

/**
 * A static, non-interactive preview of notification filter options for onboarding.
 */
@Composable
private fun NotificationFiltersPreviewContent() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Agency section
            SectionHeader(stringResource(Res.string.onboarding_filters_agencies))
            // i18n-ignore: agency proper name in static preview
            CheckboxItem(label = "SpaceX", checked = true)
            // i18n-ignore: agency proper name in static preview
            CheckboxItem(label = "NASA", checked = true)
            // i18n-ignore: agency proper name in static preview
            CheckboxItem(label = "Blue Origin", checked = false)
            // i18n-ignore: agency proper name in static preview
            CheckboxItem(label = "ULA", checked = false)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Location section
            SectionHeader(stringResource(Res.string.onboarding_filters_locations))
            CheckboxItem(label = stringResource(Res.string.onboarding_filters_florida), checked = true)
            CheckboxItem(label = stringResource(Res.string.onboarding_filters_texas), checked = false)
            CheckboxItem(label = stringResource(Res.string.onboarding_filters_california), checked = false)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Topic toggles
            SectionHeader(stringResource(Res.string.onboarding_filters_timing))
            ToggleItem(label = stringResource(Res.string.onboarding_filters_24h), checked = true)
            ToggleItem(label = stringResource(Res.string.onboarding_filters_10min), checked = true)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun CheckboxItem(label: String, checked: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
            enabled = false
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ToggleItem(label: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = false
        )
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun NotificationFiltersPagePreview() {
    SpaceLaunchNowPreviewTheme {
        NotificationFiltersPage()
    }
}

@Preview
@Composable
private fun NotificationFiltersPageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        NotificationFiltersPage()
    }
}
