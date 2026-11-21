package me.calebjones.spacelaunchnow.ui.settings

import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.repository.NotificationPermissionRemedy
import me.calebjones.spacelaunchnow.data.repository.NotificationPermissionStatus
import me.calebjones.spacelaunchnow.data.repository.getPlatformNotificationPermissionStatus
import me.calebjones.spacelaunchnow.data.repository.openPlatformNotificationSettings
import me.calebjones.spacelaunchnow.data.repository.remedy
import me.calebjones.spacelaunchnow.data.repository.requestPlatformNotificationPermission
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.subscription.PremiumBadge
import me.calebjones.spacelaunchnow.ui.subscription.PremiumPromptCard
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_back
import spacelaunchnow_kmp.composeapp.generated.resources.common_enable
import spacelaunchnow_kmp.composeapp.generated.resources.common_notification_filters
import spacelaunchnow_kmp.composeapp.generated.resources.common_notifications_disabled
import spacelaunchnow_kmp.composeapp.generated.resources.common_open_settings
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_announcements_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_announcements_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_countdown_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_countdown_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_disabled_message
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_event_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_event_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_flexible_bullets
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_flexible_example
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_flexible_summary
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_follow_all_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_follow_all_override_note
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_follow_all_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_general_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_in_flight_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_in_flight_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_intro
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_locations_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_locations_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_matching_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_matching_not_applicable
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_matching_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_mission_success_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_mission_success_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_mute_starlink_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_mute_starlink_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_net_change_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_net_change_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_news_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_news_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_10m_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_10m_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_1h_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_1h_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_1m_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_1m_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_24h_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_notice_24h_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_premium_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_premium_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_providers_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_providers_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_status_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_status_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_strict_bullets
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_strict_example
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_strict_matching_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_strict_summary
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_topics_title
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_webcast_description
import spacelaunchnow_kmp.composeapp.generated.resources.notification_settings_webcast_title

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationSettingsScreen(
    navController: NavController,
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    var permissionStatus by remember { mutableStateOf(NotificationPermissionStatus.GRANTED) }
    val hasPermission = permissionStatus == NotificationPermissionStatus.GRANTED

    LaunchedEffect(Unit) {
        permissionStatus = getPlatformNotificationPermissionStatus()
    }

    // Locked controls open the paywall only while the Remote Config flag is on (default off).
    val remoteConfig = koinInject<RemoteConfigRepository>()
    val lockedTapFlag by produceState(initialValue = false, remoteConfig) {
        value = remoteConfig.getLockedNotificationTapPaywallEnabled()
    }
    val onLockedControlClick: (() -> Unit)? =
        if (shouldOpenPaywallOnLockedTap(lockedTapFlag, uiState.hasNotificationCustomization)) {
            { navController.navigate(SupportUs(PaywallSource.LOCKED_NOTIFICATION_CONTROL.value)) }
        } else {
            null
        }

    // Re-check permission when returning from system permission dialog
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    permissionStatus = getPlatformNotificationPermissionStatus()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Show status message as snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Show snackbar for validation messages
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(Res.string.common_notification_filters),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(Res.string.common_back))
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

            if (!hasPermission) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(Res.string.common_notifications_disabled),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    text = stringResource(Res.string.notification_settings_disabled_message),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            val permissionRemedy = permissionStatus.remedy()
                            Button(
                                onClick = {
                                    when (permissionRemedy) {
                                        NotificationPermissionRemedy.REQUEST_PERMISSION ->
                                            scope.launch {
                                                requestPlatformNotificationPermission()
                                                permissionStatus = getPlatformNotificationPermissionStatus()
                                            }
                                        else -> openPlatformNotificationSettings()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                Text(
                                    if (permissionRemedy == NotificationPermissionRemedy.REQUEST_PERMISSION) {
                                        stringResource(Res.string.common_enable)
                                    } else {
                                        stringResource(Res.string.common_open_settings)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(Res.string.notification_settings_intro),
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
                                    text = stringResource(Res.string.notification_settings_follow_all_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = stringResource(Res.string.notification_settings_follow_all_description),
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
                                text = stringResource(Res.string.notification_settings_follow_all_override_note),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
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
                            text = stringResource(Res.string.notification_settings_matching_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(Res.string.notification_settings_matching_description),
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
                                    text = stringResource(Res.string.notification_settings_strict_matching_title),
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
                                        text = stringResource(Res.string.notification_settings_matching_not_applicable),
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
                                                text = stringResource(Res.string.notification_settings_strict_summary),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = stringResource(Res.string.notification_settings_strict_example),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = stringResource(Res.string.notification_settings_strict_bullets),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Column {
                                            Text(
                                                text = stringResource(Res.string.notification_settings_flexible_summary),
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.secondary
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = stringResource(Res.string.notification_settings_flexible_example),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )

                                            Text(
                                                text = stringResource(Res.string.notification_settings_flexible_bullets),
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

            // Launch Service Providers Card
            item {
                Column {
                    Text(
                        text = stringResource(Res.string.notification_settings_providers_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.notification_settings_providers_description),
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

                        // Starlink mute — shown while a Starlink launch can reach
                        // this device: SpaceX followed, or follow-all (which
                        // receives every launch). Server-enforced via the v6
                        // starlinkMuted opt-out topic; failures still notify.
                        val spaceXFollowed = uiState.notificationSettings.followAllLaunches ||
                            uiState.notificationSettings.subscribedAgencies.contains(
                                NotificationAgency.SPACEX.id.toString()
                            )
                        if (spaceXFollowed) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .then(
                                        if (onLockedControlClick != null) {
                                            Modifier.clickable(onClick = onLockedControlClick)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(Res.string.notification_settings_mute_starlink_title),
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        if (!uiState.hasNotificationCustomization) {
                                            PremiumBadge()
                                        }
                                    }
                                    Text(
                                        text = stringResource(Res.string.notification_settings_mute_starlink_description),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = uiState.notificationSettings.muteStarlink,
                                    onCheckedChange = if (uiState.hasNotificationCustomization) {
                                        { viewModel.toggleMuteStarlink() }
                                    } else {
                                        null // locked: the row handles the tap
                                    },
                                    enabled = uiState.hasNotificationCustomization
                                )
                            }
                        }
                    }
                }
            }

            // Launch Locations Card
            item {
                Column {
                    Text(
                        text = stringResource(Res.string.notification_settings_locations_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(Res.string.notification_settings_locations_description),
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

            item {
                if (!uiState.hasNotificationCustomization) {
                    PremiumPromptCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.notification_settings_premium_title),
                        description = stringResource(Res.string.notification_settings_premium_description),
                        icon = Icons.Default.Notifications,
                        onUpgradeClick = {
                            navController.navigate(SupportUs(PaywallSource.NOTIFICATION_SETTINGS.value))
                        }
                    )
                }
            }

            // Notification Topics Card
            item {
                Text(
                    text = stringResource(Res.string.notification_settings_topics_title),
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
                            text = stringResource(Res.string.notification_settings_general_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_event_title),
                            description = stringResource(Res.string.notification_settings_event_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.EVENTS),
                            onCheckedChange = viewModel::updateEventNotifications
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_news_title),
                            description = stringResource(Res.string.notification_settings_news_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.FEATURED_NEWS),
                            onCheckedChange = viewModel::updateFeaturedNews
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_announcements_title),
                            description = stringResource(Res.string.notification_settings_announcements_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.ANNOUNCEMENTS),
                            onCheckedChange = viewModel::updateAnnouncements
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
                                    text = stringResource(Res.string.notification_settings_countdown_title),
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
                                text = stringResource(Res.string.notification_settings_countdown_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_net_change_title),
                            description = stringResource(Res.string.notification_settings_net_change_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.NETSTAMP_CHANGED),
                            onCheckedChange = viewModel::updateNetstampChanged,
                            enabled = uiState.hasNotificationCustomization,
                            onLockedClick = onLockedControlClick
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_notice_24h_title),
                            description = stringResource(Res.string.notification_settings_notice_24h_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.TWENTY_FOUR_HOUR),
                            onCheckedChange = viewModel::updateTwentyFourHour
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_notice_1h_title),
                            description = stringResource(Res.string.notification_settings_notice_1h_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.ONE_HOUR),
                            onCheckedChange = viewModel::updateOneHour,
                            enabled = uiState.hasNotificationCustomization,
                            onLockedClick = onLockedControlClick
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_notice_10m_title),
                            description = stringResource(Res.string.notification_settings_notice_10m_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.TEN_MINUTES),
                            onCheckedChange = viewModel::updateTenMinutes
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_notice_1m_title),
                            description = stringResource(Res.string.notification_settings_notice_1m_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.ONE_MINUTE),
                            onCheckedChange = viewModel::updateOneMinute,
                            enabled = uiState.hasNotificationCustomization,
                            onLockedClick = onLockedControlClick
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
                                    text = stringResource(Res.string.notification_settings_status_title),
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
                                text = stringResource(Res.string.notification_settings_status_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_webcast_title),
                            description = stringResource(Res.string.notification_settings_webcast_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.WEBCAST_ONLY),
                            onCheckedChange = viewModel::updateWebcastOnly,
                            enabled = uiState.hasNotificationCustomization,
                            onLockedClick = onLockedControlClick
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_in_flight_title),
                            description = stringResource(Res.string.notification_settings_in_flight_description),
                            checked = uiState.notificationSettings.isTopicEnabled(NotificationTopic.IN_FLIGHT),
                            onCheckedChange = viewModel::updateInFlight,
                            enabled = uiState.hasNotificationCustomization,
                            onLockedClick = onLockedControlClick
                        )

                        NotificationTopicToggle(
                            title = stringResource(Res.string.notification_settings_mission_success_title),
                            description = stringResource(Res.string.notification_settings_mission_success_description),
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
                        LoadingIndicator()
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
    modifier: Modifier = Modifier,
    /** When set, a tap on this locked (disabled) control calls it, e.g. to open the paywall. */
    onLockedClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (enabled) {
                    Modifier.clickable { onCheckedChange(!checked) }
                } else if (onLockedClick != null) {
                    Modifier.clickable(onClick = onLockedClick)
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
            // A locked switch is not interactive itself; the row handles the tap.
            onCheckedChange = if (enabled) onCheckedChange else null,
            enabled = enabled
        )
    }
}