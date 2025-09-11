package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LaunchDetailScreen(
    launchId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<LaunchViewModel>()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Launch Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            LaunchDetailView(viewModel = viewModel, launchId = launchId)
        }
    }
}
