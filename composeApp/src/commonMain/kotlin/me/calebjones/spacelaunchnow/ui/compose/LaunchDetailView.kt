package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel

@Composable
fun LaunchDetailView(viewModel: LaunchViewModel, launchId: String) {
    val launchDetails by viewModel.launchDetails.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(launchId) {
        viewModel.fetchLaunchDetails(launchId)
    }

    if (error != null) {
        Text(text = "Error: $error")
    } else if (launchDetails != null) {
        Column(modifier = Modifier.padding(16.dp)) {
            launchDetails!!.name?.let { Text(text = it, style = MaterialTheme.typography.titleLarge) }
            Text(text = launchDetails!!.windowStart.toString(), style = MaterialTheme.typography.bodySmall)
            // Add more details as needed
        }
    } else {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }
}