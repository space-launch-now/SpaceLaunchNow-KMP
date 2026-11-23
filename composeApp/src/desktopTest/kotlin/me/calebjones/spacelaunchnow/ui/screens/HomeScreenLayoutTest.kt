package me.calebjones.spacelaunchnow.ui.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.navigation.compose.rememberNavController
import me.calebjones.spacelaunchnow.domain.model.Update
import me.calebjones.spacelaunchnow.i18n.ScreenLayoutHarness
import me.calebjones.spacelaunchnow.ui.home.components.ResponsiveHomeContent
import me.calebjones.spacelaunchnow.ui.viewmodel.HistoryViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ViewState
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class HomeScreenLayoutTest {
    @Test
    fun homeLayout() = ScreenLayoutHarness.assertLayout("home", koinModules = { listOf(ScreenKoin.common) }) {
        val launches = ScreenFixtures.upcomingLaunches
        ResponsiveHomeContent(
            navController = rememberNavController(),
            additionalFeaturedLaunchesState = ViewState(data = launches.drop(1)),
            previousLaunchesState = ViewState(data = launches),
            historyState = ViewState(data = HistoryViewModel.HistoryData(count = 5, launches = launches)),
            featuredLaunchState = ViewState(data = launches.first()),
            updatesState = ViewState(data = ScreenFixtures.launch.updates),
            articlesState = ViewState(data = emptyList()),
            eventsState = ViewState(data = emptyList()),
            next24HoursCount = 2,
            nextWeekCount = 8,
            nextMonthCount = 24,
            isAnyViewLoading = false,
            hasAdFree = true,
        )
    }
}
