package me.calebjones.spacelaunchnow.ui.preload

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.calebjones.spacelaunchnow.Primary
import me.calebjones.spacelaunchnow.Secondary
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.components.AppIconBox
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.PreloadState
import me.calebjones.spacelaunchnow.ui.viewmodel.PreloadViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val spaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
)

private val loadingMessages = listOf(
    "Reticulating launch trajectories...",
    "Convincing boosters to come back...",
    "Adding more struts just in case...",
    "Calculating optimal snack-to-fuel ratio...",
    "Slowly planning disassembly...",
)

@Composable
fun PreloadScreen(
    onPreloadComplete: (nextDestination: Any) -> Unit,
    modifier: Modifier = Modifier,
    preloadViewModel: PreloadViewModel = koinViewModel(),
    appPreferences: AppPreferences = koinInject()
) {
    val preloadState by preloadViewModel.preloadState.collectAsState()
    val liveOnboardingCompleted by appPreferences.liveOnboardingCompletedFlow.collectAsState(initial = null)

    // Hold the screen for at least 1s so the splash doesn't flash by on fast devices.
    var minDurationElapsed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1500)
        minDurationElapsed = true
    }

    // Start preload once we know the user type
    LaunchedEffect(liveOnboardingCompleted) {
        val completed = liveOnboardingCompleted ?: return@LaunchedEffect
        preloadViewModel.startPreload(isNewUser = !completed)
    }

    // Navigate when preload is complete AND the minimum display time has elapsed
    LaunchedEffect(preloadState.isComplete, preloadState.nextDestination, minDurationElapsed) {
        if (preloadState.isComplete && preloadState.nextDestination != null && minDurationElapsed) {
            onPreloadComplete(preloadState.nextDestination!!)
        }
    }

    PreloadScreenContent(
        state = preloadState,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PreloadScreenContent(
    state: PreloadState,
    modifier: Modifier = Modifier
) {
    var messageIndex by remember { mutableIntStateOf((loadingMessages.indices).random()) }
    val currentMessage = loadingMessages[messageIndex]

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            messageIndex = (loadingMessages.indices - messageIndex).random()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(spaceGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 48.dp)
        ) {
            AppIconBox(
                modifier = Modifier.size(120.dp),
                primaryColor = Primary,
                secondaryColor = Secondary,
                tertiaryColor = Primary,
                containerColor = Primary,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = currentMessage,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .semantics {
                        contentDescription = "Loading launch data"
                    }
            )

            LinearWavyProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp)),
                color = Primary,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}

@Preview
@Composable
private fun PreloadScreenPreview() {
    SpaceLaunchNowPreviewTheme {
        PreloadScreenContent(
            state = PreloadState(totalTasks = 20, completedTasks = 10)
        )
    }
}

@Preview
@Composable
private fun PreloadScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        PreloadScreenContent(
            state = PreloadState(totalTasks = 20, completedTasks = 10)
        )
    }
}
