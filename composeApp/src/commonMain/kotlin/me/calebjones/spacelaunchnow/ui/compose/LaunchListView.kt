package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.client.models.LaunchBasic
import me.calebjones.spacelaunchnow.api.client.models.LaunchDetailed
import me.calebjones.spacelaunchnow.api.client.models.LaunchNormal
import me.calebjones.spacelaunchnow.api.client.models.PolymorphicLaunchEndpoint
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel

@Composable
fun LaunchListView(viewModel: LaunchViewModel) {
    val launches by viewModel.upcomingLaunches.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.fetchUpcomingLaunches(limit = 10)
    }

    if (error != null) {
        Text(text = "Error: $error")
    } else if (launches != null) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(launches!!.results) { launch ->
                LaunchItemView(launch)
            }
        }
    } else {
        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
    }
}

@Composable
fun LaunchItemView(launch: PolymorphicLaunchEndpoint) {
    Card(modifier = Modifier.size(360.dp, 240.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Handle subclass-specific properties
            when (launch) {
                is LaunchNormal -> {
                    // Access properties specific to LaunchNormal
                    launch.name?.let { name ->
                        Text(text = name, style = MaterialTheme.typography.titleLarge)
                    }
                    launch.rocket?.let { rocket ->
                        Text(text = rocket.configuration.name, style = MaterialTheme.typography.bodyMedium)
                    }

                }
                is LaunchBasic -> {
                    // Access properties specific to LaunchNormal
                    launch.name?.let { name ->
                        Text(text = name, style = MaterialTheme.typography.titleLarge)
                    }
                }
                is LaunchDetailed -> {
                    // Access properties specific to LaunchNormal
                    launch.name?.let { name ->
                        Text(text = name, style = MaterialTheme.typography.titleLarge)
                    }
                    launch.rocket?.let { rocket ->
                        Text(text = rocket.configuration.name, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                else -> {
                    // Handle any other subclasses or default case
                    Text(text = "Unknown launch type", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
