package me.calebjones.spacelaunchnow.ui.onboarding

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.OnboardingVariant
import me.calebjones.spacelaunchnow.data.model.resolveOnboardingVariant
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.ui.onboarding.pages.ExplorePage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.LaunchCardPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.NewsEventsPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.NotificationPermissionPage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.SchedulePage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.WelcomePage
import me.calebjones.spacelaunchnow.ui.onboarding.pages.WidgetsPage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.NextUpViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.OnboardingViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private val spaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
)

/**
 * The main live-composable onboarding carousel.
 *
 * Pages shown depend on the resolved [OnboardingVariant] (see [pagesFor]):
 * - `control`: Welcome, Launch card preview, News & Events, Widgets showcase,
 *   Notification permission request.
 * - `short`: Welcome, Notification permission request.
 *
 * Includes a "Skip" button, a wavy progress indicator, and a "Next" / "Get Started" button.
 * On completion or skip, persists the completed state via [AppPreferences] and
 * invokes [onComplete].
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LiveOnboardingScreen(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    appPreferences: AppPreferences = koinInject(),
    nextUpViewModel: NextUpViewModel = koinViewModel(),
    onboardingViewModel: OnboardingViewModel = koinViewModel()
) {
    val scope = rememberCoroutineScope()

    val remoteConfigRepository: RemoteConfigRepository = koinInject()
    val variant by produceState<OnboardingVariant?>(initialValue = null) {
        value = resolveOnboardingVariant(
            persisted = appPreferences.onboardingVariantFlow.first(),
            fetchRemote = { remoteConfigRepository.getOnboardingVariant() },
            persist = { appPreferences.setOnboardingVariant(it) }
        )
    }
    val resolvedVariant = variant
    if (resolvedVariant == null) {
        // One frame of bare gradient while DataStore + activated config resolve (both local, no network)
        Box(modifier = modifier.fillMaxSize().background(spaceGradient))
        return
    }
    val pages = remember(resolvedVariant) { pagesFor(resolvedVariant) }
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val nextLaunch by nextUpViewModel.nextLaunch.collectAsState()

    val upcomingLaunches by onboardingViewModel.upcomingLaunches.collectAsState()
    val previousLaunches by onboardingViewModel.previousLaunches.collectAsState()
    val articles by onboardingViewModel.articles.collectAsState()
    val astronauts by onboardingViewModel.astronauts.collectAsState()
    val rockets by onboardingViewModel.rockets.collectAsState()
    val agencies by onboardingViewModel.agencies.collectAsState()

    val isLastPage = pagerState.currentPage == pages.lastIndex
    val isFirstPage = pagerState.currentPage == 0

    // Data is pre-cached by PreloadViewModel; these calls load from cache into ViewModel StateFlows.
    // Only the control variant's content pages need the schedule/articles/explore data.
    LaunchedEffect(resolvedVariant) {
        nextUpViewModel.fetchNextLaunch()
        if (resolvedVariant == OnboardingVariant.CONTROL) {
            onboardingViewModel.fetchScheduleData()
            onboardingViewModel.fetchArticles()
            onboardingViewModel.fetchExploreData()
        }
    }

    // Track onboarding page navigation
    LaunchedEffect(pagerState.currentPage) {
        onboardingViewModel.trackOnboardingStep(
            step = pagerState.currentPage,
            page = pages[pagerState.currentPage].analyticsName,
            variant = resolvedVariant.value,
            completed = pagerState.currentPage == pages.lastIndex
        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Skip button row
            if (!isLastPage && !isFirstPage) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pages.lastIndex)
                        }
                    }) {
                        Text(
                            text = "Skip",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { pageIndex ->
                when (pages[pageIndex]) {
                    OnboardingPage.WELCOME -> WelcomePage(
                        modifier = Modifier.fillMaxSize(),
                        nextLaunch = nextLaunch
                    )

                    OnboardingPage.LAUNCH_CARD -> LaunchCardPage(
                        modifier = Modifier.fillMaxSize(),
                        nextLaunch = nextLaunch
                    )

                    OnboardingPage.NEWS_EVENTS -> NewsEventsPage(
                        modifier = Modifier.fillMaxSize(),
                        articles = articles
                    )

                    OnboardingPage.WIDGETS -> WidgetsPage(modifier = Modifier.fillMaxSize())

                    OnboardingPage.NOTIFICATION_PERMISSION -> NotificationPermissionPage(
                        onPermissionResult = { granted ->
                            onboardingViewModel.trackNotificationPermissionResult(granted, resolvedVariant.value)
                            if (granted) completeOnboarding()
                        },
                        onSkip = {
                            onboardingViewModel.trackNotificationPermissionResult(false, resolvedVariant.value)
                            completeOnboarding()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Progress indicator & navigation — hidden on the notification page
            // so the user must choose "Enable Notifications" or "Maybe Later"
            if (!isLastPage) {
                LinearWavyProgressIndicator(
                    progress = {
                        ((pagerState.currentPage + pagerState.currentPageOffsetFraction) / (pages.size - 1).toFloat()).coerceIn(
                            0f,
                            1f
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    amplitude = { 1f },
                )

                // Next button
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
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
                        text = "Next",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews (stateless content only — no AppPreferences injection in preview)
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LiveOnboardingScreenPreviewContent() {
    val previewPageCount = pagesFor(OnboardingVariant.CONTROL).size
    val pagerState = rememberPagerState(pageCount = { previewPageCount })
    val animatedProgress by animateFloatAsState(
        targetValue = ((pagerState.currentPage + pagerState.currentPageOffsetFraction) /
            (previewPageCount - 1).toFloat()).coerceIn(0f, 1f),
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
    )

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
                    2 -> NewsEventsPage(modifier = Modifier.fillMaxSize())
                    3 -> WidgetsPage(modifier = Modifier.fillMaxSize())
                    4 -> NotificationPermissionPage(
                        onPermissionResult = {},
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            LinearWavyProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                amplitude = { 1f },
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
