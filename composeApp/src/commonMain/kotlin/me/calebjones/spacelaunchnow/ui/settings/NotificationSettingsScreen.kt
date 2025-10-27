package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.subscription.PremiumBadge
import me.calebjones.spacelaunchnow.ui.subscription.PremiumPromptCard
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show status message as snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notification Filters",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {
                Text(
                    text = "Configure your notification preferences to stay updated with your favorite launches!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Follow All Launches Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.updateFollowAllLaunches(!uiState.notificationSettings.followAllLaunches) },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Follow All Launches",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Get notified for every launch across all agencies and locations",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Switch(
                                checked = uiState.notificationSettings.followAllLaunches,
                                onCheckedChange = viewModel::updateFollowAllLaunches
                            )
                        }

                        if (uiState.notificationSettings.followAllLaunches) {
                            Text(
                                text = "You're following all launches! Individual selections below are overridden.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Launch Service Providers Card
            item {
                Column {
                    Text(
                        text = "Launch Service Providers",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select which launch providers to follow",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.availableAgencies.chunked(2).forEach { agencyPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                agencyPair.forEach { agency ->
                                    AgencyCheckboxItem(
                                        agency = agency,
                                        isChecked = uiState.notificationSettings.subscribedAgencies.contains(
                                            agency.id.toString()
                                        ),
                                        onCheckedChange = {
                                            viewModel.toggleAgencySubscription(agency)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (agencyPair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Launch Locations Card
            item {
                Column {
                    Text(
                        text = "Launch Locations",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Select which locations to follow",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.availableLocations.chunked(2).forEach { locationPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                locationPair.forEach { location ->
                                    LocationCheckboxItem(
                                        location = location,
                                        isChecked = uiState.notificationSettings.subscribedLocations.contains(
                                            location.id.toString()
                                        ),
                                        onCheckedChange = {
                                            viewModel.toggleLocationSubscription(location)
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (locationPair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            // Strict Matching Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.notificationSettings.followAllLaunches) {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Notification Matching",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Control how your selected agencies and locations work together",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let {
                                    if (!uiState.notificationSettings.followAllLaunches) {
                                        it.clickable { viewModel.updateStrictMatching(!uiState.notificationSettings.useStrictMatching) }
                                    } else {
                                        it
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Strict Matching",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    color = if (uiState.notificationSettings.followAllLaunches) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                if (uiState.notificationSettings.followAllLaunches) {
                                    Text(
                                        text = "Not applicable when following all launches",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.6f
                                        )
                                    )
                                } else {
                                    // Show explanation based on current setting
                                    if (uiState.notificationSettings.useStrictMatching) {
                                        Column {
                                            Text(
                                                text = "STRICT: Show launches that match BOTH agency AND location",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "Example: If you select SpaceX + Kennedy Space Center, you'll ONLY get SpaceX launches from KSC.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = "• ✅ SpaceX from KSC\n• ❌ SpaceX from other locations\n• ❌ Other agencies from KSC",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Column {
                                            Text(
                                                text = "FLEXIBLE: Show launches that match ANY agency OR location",
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = "Example: If you select SpaceX + Kennedy Space Center, you'll get ALL SpaceX launches AND all KSC launches.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = "• ✅ SpaceX from anywhere\n• ✅ Any agency from KSC\n• ✅ Much more notifications",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                            Switch(
                                checked = if (uiState.notificationSettings.followAllLaunches) false else uiState.notificationSettings.useStrictMatching,
                                onCheckedChange = {
                                    if (!uiState.notificationSettings.followAllLaunches) viewModel.updateStrictMatching(
                                        !uiState.notificationSettings.useStrictMatching
                                    )
                                },
                                enabled = !uiState.notificationSettings.followAllLaunches
                            )
                        }
                    }
                }
            }

            item {
                if (!uiState.hasNotificationCustomization) {
                    PremiumPromptCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Unlock Notification Customization",
                        description = "Upgrade to Premium to customize your notification preferences.",
                        icon = Icons.Default.Notifications,
                        onUpgradeClick = {
                            navController.navigate(SupportUs)
                        }
                    )
                }
            }

            // Notification Topics Card
            item {
                Text(
                    text = "Notification Topics",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "General Notifications",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        NotificationTopicToggle(
                            title = "Event Notifications",
                            description = "Space events and featured news updates",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.EVENTS),
                            onCheckedChange = viewModel::updateEventNotifications
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Countdown Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.weight(1f))
                                Spacer(Modifier.width(4.dp))
                                if (!uiState.hasNotificationCustomization) {
                                    PremiumBadge()
                                }
                            }
                            Text(
                                text = "Get notified at specific times before launch",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        NotificationTopicToggle(
                            title = "Net Timestamp Changes",
                            description = "When launch times are updated",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.NETSTAMP_CHANGED),
                            onCheckedChange = viewModel::updateNetstampChanged,
                            enabled = uiState.hasNotificationCustomization
                        )

                        NotificationTopicToggle(
                            title = "24 Hour Notice",
                            description = "24 hours before launch",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.TWENTY_FOUR_HOUR),
                            onCheckedChange = viewModel::updateTwentyFourHour
                        )

                        NotificationTopicToggle(
                            title = "1 Hour Notice",
                            description = "1 hour before launch",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.ONE_HOUR),
                            onCheckedChange = viewModel::updateOneHour,
                            enabled = uiState.hasNotificationCustomization
                        )

                        NotificationTopicToggle(
                            title = "10 Minutes Notice",
                            description = "10 minutes before launch",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.TEN_MINUTES),
                            onCheckedChange = viewModel::updateTenMinutes
                        )

                        NotificationTopicToggle(
                            title = "1 Minute Notice",
                            description = "1 minute before launch - for the most dedicated!",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.ONE_MINUTE),
                            onCheckedChange = viewModel::updateOneMinute,
                            enabled = uiState.hasNotificationCustomization
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status Notifications",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.weight(1f))
                                Spacer(Modifier.width(4.dp))
                                if (!uiState.hasNotificationCustomization) {
                                    PremiumBadge()
                                }
                            }
                            Text(
                                text = "Stay updated on launch progress and outcomes",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        NotificationTopicToggle(
                            title = "Webcast Only Launches",
                            description = "Launches that have live video streams",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.WEBCAST_ONLY),
                            onCheckedChange = viewModel::updateWebcastOnly,
                            enabled = uiState.hasNotificationCustomization
                        )

                        NotificationTopicToggle(
                            title = "In-Flight Updates",
                            description = "Real-time updates during launch",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.IN_FLIGHT),
                            onCheckedChange = viewModel::updateInFlight,
                            enabled = uiState.hasNotificationCustomization
                        )

                        NotificationTopicToggle(
                            title = "Mission Success",
                            description = "Celebrate successful launches!",
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.SUCCESS),
                            onCheckedChange = viewModel::updateSuccess
                        )
                    }
                }
            }

            // Subscribed Topics Card
            // (Removed - see DebugSettingsScreen for new location)

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun AgencyCheckboxItem(
    agency: NotificationAgency,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = agency.name,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun LocationCheckboxItem(
    location: NotificationLocation,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(vertical = 4.dp)
            .clickable { onCheckedChange(!isChecked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = location.name,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun NotificationTopicToggle(
    title: String,
    description: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable { onCheckedChange(!checked) }
                } else {
                    Modifier
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {
                if (enabled) onCheckedChange(it) else {
                }
            },
            enabled = enabled
        )
    }
}