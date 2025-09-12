package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.ui.compose.CollapsingToolbar
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowTheme
import me.calebjones.spacelaunchnow.util.LaunchFormatUtil
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share


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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
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
            )
        }, content = {
            Box(modifier = Modifier.fillMaxSize().padding()) {
                LaunchDetailView(
                    viewModel = viewModel,
                    launchId = launchId,
                    preloadedLaunchDetailed = cachedLaunchDetailed
                )
            }
        }
    )
}
