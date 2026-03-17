package me.calebjones.spacelaunchnow.ui.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.onboarding.pages.ExplorePage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.LaunchCardPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.NewsEventsPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.NotificationPermissionPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.SchedulePage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.WelcomePage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.OnboardingViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val spaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
)

private const val PAGE_COUNT = 6

/**
 * The main live-composable onboarding carousel.
 *
 * Displays swipeable pages:
 * 0. Welcome
 * 1. Launch card preview
 * 2. Schedule screen preview
 * 3. News & Events
 * 4. Explore
 * 5. Notification permission request
 *
 * Includes a "Skip" button, [WavyProgressBar], and a "Next" / "Get Started" button.
 * On completion or skip, persists the completed state via [AppPreferences] and
 * invokes [onComplete].
 */
@Composable
fun LiveOnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences = koinInject(),
    nextUpViewModel: NextUpViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val nextLaunch by nextUpViewModel.nextLaunch.collectAsState()

    val upcomingLaunches by onboardingViewModel.upcomingLaunches.collectAsState()
    val previousLaunches by onboardingViewModel.previousLaunches.collectAsState()
    val articles by onboardingViewModel.articles.collectAsState()
    val astronauts by onboardingViewModel.astronauts.collectAsState()
    val rockets by onboardingViewModel.rockets.collectAsState()
    val agencies by onboardingViewModel.agencies.collectAsState()

    val isLastPage = pagerState.currentPage == PAGE_COUNT - 1

    LaunchedEffect(Unit) {
        nextUpViewModel.fetchNextLaunch()
        onboardingViewModel.fetchScheduleData()
        onboardingViewModel.fetchArticles()
        onboardingViewModel.fetchExploreData()
    }

    fun completeOnboarding() {
        scope.launch {
            appPreferences.setLiveOnboardingCompleted(true)
            onComplete()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(spaceGradient)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, end = 16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = { completeOnboarding() }) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        modifier = Modifier.fillMaxSize(),
                        nextLaunch = nextLaunch
                    )
                    1 -> LaunchCardPage(
                        modifier = Modifier.fillMaxSize(),
                        nextLaunch = nextLaunch
                    )
                    2 -> SchedulePage(
                        modifier = Modifier.fillMaxSize(),
                        upcomingLaunches = upcomingLaunches,
                        previousLaunches = previousLaunches
                    )
                    3 -> NewsEventsPage(
                        modifier = Modifier.fillMaxSize(),
                        articles = articles
                    )
                    4 -> ExplorePage(
                        modifier = Modifier.fillMaxSize(),
                        astronauts = astronauts,
                        rockets = rockets,
                        agencies = agencies
                    )
                    5 -> NotificationPermissionPage(
                        onPermissionResult = { /* Result handled; user taps Get Started below */ },
                        onSkip = { completeOnboarding() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Progress indicator
            LinearProgressIndicator(
                progress = {
                    ((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (PAGE_COUNT - 1).toFloat()).coerceIn(
                        0f,
                        1f
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )

            // Next / Get Started button
            Button(
                onClick = {
                    if (isLastPage) {
                        completeOnboarding()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .animateContentSize(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0A0E2A)
                )
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews (stateless content only — no AppPreferences injection in preview)
// ---------------------------------------------------------------------------

@Composable
private fun LiveOnboardingScreenPreviewContent() {
    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(spaceGradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, end = 12.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                TextButton(onClick = {}) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { page ->
                when (page) {
                    0 -> WelcomePage(modifier = Modifier.fillMaxSize())
                    1 -> LaunchCardPage(modifier = Modifier.fillMaxSize())
                    2 -> SchedulePage(modifier = Modifier.fillMaxSize())
                    3 -> NewsEventsPage(modifier = Modifier.fillMaxSize())
                    4 -> ExplorePage(modifier = Modifier.fillMaxSize())
                    5 -> NotificationPermissionPage(  // preview — uses default empty params
                        onPermissionResult = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            LinearProgressIndicator(
                progress = {
                    ((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (PAGE_COUNT - 1).toFloat()).coerceIn(
                        0f,
                        1f
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
            )

            Button(
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF0A0E2A)
                )
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun LiveOnboardingScreenPreview() {
    SpaceLaunchNowPreviewTheme {
        LiveOnboardingScreenPreviewContent()
    }
}

@Preview
@Composable
private fun LiveOnboardingScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        LiveOnboardingScreenPreviewContent()
    }
}
