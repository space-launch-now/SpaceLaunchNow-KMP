package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.ui.components.AppIconBox
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

/**
 * Beta warning dialog that shows on first app launch to inform users
 * about the beta status and known issues.
 */
@Composable
fun BetaWarningDialog(
    appPreferences: AppPreferences = koinInject()
) {
    val coroutineScope = rememberCoroutineScope()

    val hasShownWarning by appPreferences.betaWarningShownFlow.collectAsState(initial = true)
    var showDialog by remember { mutableStateOf(false) }

    // Show dialog if it hasn't been shown before
    LaunchedEffect(hasShownWarning) {
        if (!hasShownWarning) {
            showDialog = true
        }
    }

    if (showDialog) {
        val platformType = getPlatform().type

        val platformContext = when (platformType) {
            PlatformType.ANDROID -> "Space Launch Now has been rebuilt from the ground up to meet modern guidelines from Google and deliver a faster, more reliable experience."
            PlatformType.IOS -> "Space Launch Now has been rebuilt from the ground up for iOS, bringing you the best space launch tracking experience on Apple devices."
            PlatformType.DESKTOP -> "Space Launch Now is now available on desktop, giving you a native space launch tracking experience right on your computer."
        }

        val description = listOf(
            platformContext,
            "",
            "New features are being added regularly. Check the Roadmap in Settings to see what's coming next.",
            "",
            "Thank you to everyone who has supported this project over the years!"
        )

        AlertDialog(
            onDismissRequest = {
                // Don't allow dismissing without clicking OK
            },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = {
                AppIconBox()
            },
            title = {
                Text(
                    text = "Welcome to Space Launch Now",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "A completely reimagined app for space fans.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "This app is actively being developed with frequent updates. You may occasionally see rough edges as new features roll out.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = description.joinToString("\n"),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            appPreferences.setBetaWarningShown(true)
                            showDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "I Understand",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        )
    }
}

@Preview
@Composable
private fun BetaWarningDialogPreview() {
    val mockDataStore = object : DataStore<Preferences> {
        override val data: Flow<Preferences> = flowOf(emptyPreferences())
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }
    val mockPreferences = AppPreferences(mockDataStore)
    SpaceLaunchNowPreviewTheme {
        BetaWarningDialog(appPreferences = mockPreferences)
    }
}

@Preview
@Composable
private fun BetaWarningDialogDarkPreview() {
    val mockDataStore = object : DataStore<Preferences> {
        override val data: Flow<Preferences> = flowOf(emptyPreferences())
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            return emptyPreferences()
        }
    }
    val mockPreferences = AppPreferences(mockDataStore)
    SpaceLaunchNowPreviewTheme(isDark = true) {
        BetaWarningDialog(appPreferences = mockPreferences)
    }
}

