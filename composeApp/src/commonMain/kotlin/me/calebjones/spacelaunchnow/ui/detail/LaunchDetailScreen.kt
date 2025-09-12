package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.Modifier
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil.formatLaunchTitle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LaunchDetailScreen(
    launchId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<LaunchViewModel>()
    val launchCache = koinInject<LaunchCache>()
    
    // Check if we have pre-loaded detailed data in cache
    val cachedLaunchDetailed = remember(launchId) { launchCache.getCachedLaunchDetailed(launchId) }
    val launchDetails by viewModel.launchDetails.collectAsState()
    
    // Use preloaded data if available, otherwise use fetched data
    val currentLaunch = cachedLaunchDetailed ?: launchDetails

    // Create scroll behavior for collapsing top app bar
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { launchDetails?.let { Text(formatLaunchTitle(it)) } },
                subtitle = { launchDetails?.let { Text(it.mission?.name ?: "") } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }, content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                LaunchDetailView(
                    viewModel = viewModel,
                    launchId = launchId,
                    preloadedLaunchDetailed = cachedLaunchDetailed
                )
            }
        }
    )
}
