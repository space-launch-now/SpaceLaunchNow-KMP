package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.domain.model.Launch
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.schedule.components.ScheduleLaunchView
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Onboarding page 2 Ã¢â‚¬â€ shows a schedule screen with live launch data,
 * functional Upcoming/Previous tabs inside a device frame.
 */
@Composable
fun SchedulePage(
    modifier: Modifier = Modifier,
    upcomingLaunches: List<Launch> = emptyList(),
    previousLaunches: List<Launch> = emptyList()
) {
    OnboardingPage(
        title = "Your Launch Schedule",
        subtitle = "Browse upcoming and previous launches in a clean, organized timeline.",
        icon = Icons.Default.CalendarMonth,
        modifier = modifier,
        allowInteraction = true
    ) {
        SchedulePreviewContent(
            upcomingLaunches = upcomingLaunches,
            previousLaunches = previousLaunches
        )
    }
}

@Composable
private fun SchedulePreviewContent(
    upcomingLaunches: List<Launch>,
    previousLaunches: List<Launch>
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val launches = if (selectedTabIndex == 0) upcomingLaunches else previousLaunches

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Upcoming") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Previous") }
                )
            }

            if (launches.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(launches) { launch ->
                        ScheduleLaunchView(
                            launch = launch,
                            onClick = { /* non-interactive in onboarding */ }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun SchedulePagePreview() {
    SpaceLaunchNowPreviewTheme {
        SchedulePage()
    }
}

@Preview
@Composable
private fun SchedulePageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        SchedulePage()
    }
}
