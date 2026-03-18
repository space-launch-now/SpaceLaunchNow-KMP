package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchNormal
import me.calebjones.spacelaunchnow.ui.preview.PreviewData
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val LastLaunchCardHeight = 380.dp
private val LoadingPlaceholderHeight = 240.dp

/**
 * Card displaying the most recent previous launch with a header label.
 * Shows a loading placeholder when no data is available.
 */
@Composable
fun LastLaunchCard(
    previousLaunch: LaunchNormal?,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Last Launch",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Last Launch",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            if (previousLaunch != null) {
                LaunchItemView(
                    launch = previousLaunch,
                    navController = navController,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LastLaunchCardHeight)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(LoadingPlaceholderHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Loading...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

// region Previews

@Preview
@Composable
private fun LastLaunchCardPreview() {
    SpaceLaunchNowPreviewTheme {
        LastLaunchCard(
            previousLaunch = PreviewData.launchNormalSpaceX,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun LastLaunchCardDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        LastLaunchCard(
            previousLaunch = PreviewData.launchNormalSpaceX,
            navController = rememberNavController()
        )
    }
}

@Preview
@Composable
private fun LastLaunchCardLoadingPreview() {
    SpaceLaunchNowPreviewTheme {
        LastLaunchCard(
            previousLaunch = null,
            navController = rememberNavController()
        )
    }
}

// endregion
