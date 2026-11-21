package me.calebjones.spacelaunchnow.ui.settings

import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringAlertDialog
import me.calebjones.spacelaunchnow.ui.ads.AdCoveringExposedDropdownMenu
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.NotificationPermissionRemedy
import me.calebjones.spacelaunchnow.data.repository.NotificationPermissionStatus
import me.calebjones.spacelaunchnow.data.repository.getPlatformNotificationPermissionStatus
import me.calebjones.spacelaunchnow.data.repository.openPlatformNotificationSettings
import me.calebjones.spacelaunchnow.data.repository.remedy
import me.calebjones.spacelaunchnow.data.repository.requestPlatformNotificationPermission
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.Diagnostics
import me.calebjones.spacelaunchnow.navigation.ThemeCustomization
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.DebugUnlock
import me.calebjones.spacelaunchnow.util.NotificationSettingsHelper
import me.calebjones.spacelaunchnow.util.isWearOsConnected
import me.calebjones.spacelaunchnow.util.ShopLink
import me.calebjones.spacelaunchnow.util.isWearOsSupportedPlatform
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_about_details
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_ad_privacy
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_calendar_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_cancel
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_error_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_error_totp
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_field_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_field_totp
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_hide_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_message_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_message_totp
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_show_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_title_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_title_totp
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_unlock
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_use_password
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_dialog_use_totp
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_debug_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_feedback_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_feedback_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_go_premium_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_go_premium_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_hide_tbd_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_hide_tbd_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notification_filters_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notification_filters_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notifications_disabled_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notifications_disabled_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notifications_enable
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notifications_enabled_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notifications_enabled_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_notifications_open_settings
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_privacy_policy
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_roadmap_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_roadmap_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_about
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_developer
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_diagnostic
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_general
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_notifications
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_premium
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_section_wear_os
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_shop_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_shop_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_system_settings_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_system_settings_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_terms_of_service
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_theme_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_utc_subtitle
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_utc_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_version
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_available_body
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_available_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_connected
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_data_updates_body
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_data_updates_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_early_release
import spacelaunchnow_kmp.composeapp.generated.resources.settings_main_wear_early_release_body
import spacelaunchnow_kmp.composeapp.generated.resources.settings_theme_tab_label
import spacelaunchnow_kmp.composeapp.generated.resources.settings_theme_title

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onOpenSystemNotificationSettings: () -> Unit = { NotificationSettingsHelper.openSystemNotificationSettings() },
    onOpenDebugSettings: () -> Unit = {},
    onOpenAboutLibraries: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val appPreferences: AppPreferences = koinInject()
    val debugMenuUnlocked by appPreferences.debugMenuUnlockedFlow.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()

    // App rating for feedback dialog
    val appRatingViewModel: me.calebjones.spacelaunchnow.ui.viewmodel.AppRatingViewModel =
        koinViewModel()
    val shouldShowFeedback by appRatingViewModel.shouldShowFeedbackDialog.collectAsState()

    // Tap counter for debug unlock
    var tapCount by remember { mutableStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var useTotp by remember { mutableStateOf(false) } // Toggle between password and TOTP

    var notificationPermissionStatus by remember {
        mutableStateOf(NotificationPermissionStatus.GRANTED)
    }
    val hasNotificationPermission = notificationPermissionStatus == NotificationPermissionStatus.GRANTED

    LaunchedEffect(Unit) {
        notificationPermissionStatus = getPlatformNotificationPermissionStatus()
    }

    // Re-check when returning from settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    notificationPermissionStatus = getPlatformNotificationPermissionStatus()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val uriHandler = LocalUriHandler.current
    val onPrivacyPolicy = {
        uriHandler.openUri("https://spacelaunchnow.app/app/privacy")
    }
    val onTerms = {
        uriHandler.openUri("https://spacelaunchnow.app/app/tos")
    }
    val onAbout: () -> Unit = {
        onOpenAboutLibraries()
    }

    // Show error snackbar if there's an error
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar
            viewModel.clearError()
        }
    }

    Column {
        TopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                Text(
                    text = stringResource(Res.string.settings_main_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        )
        if (uiState.isLoading) {
            androidx.compose.material3.LinearWavyProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 0.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // GENERAL
            item {
                SectionHeaderText(stringResource(Res.string.settings_main_section_general))
                Spacer(Modifier.height(2.dp))
                SettingsCardRow {
                    Column(Modifier.fillMaxWidth()) {
                        SettingsToggleRow(
                            title = stringResource(Res.string.settings_main_utc_title),
                            subtitle = stringResource(Res.string.settings_main_utc_subtitle),
                            checked = uiState.useUtc,
                            onCheckedChange = viewModel::updateUseUtc
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        NotificationTopicToggle(
                            title = stringResource(Res.string.settings_main_hide_tbd_title),
                            description = stringResource(Res.string.settings_main_hide_tbd_subtitle),
                            checked = uiState.hideTbdLaunches,
                            onCheckedChange = viewModel::updateHideTbdLaunches
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_theme_title),
                            subtitle = stringResource(Res.string.settings_main_theme_subtitle),
                            onClick = { navController.navigate(ThemeCustomization) },
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_calendar_title),
                            subtitle = stringResource(Res.string.settings_main_calendar_subtitle),
                            onClick = { navController.navigate(CalendarSync) },
                        )
                    }
                }
            }
            // NOTIFICATIONS
            item {
                SectionHeaderText(stringResource(Res.string.settings_main_section_notifications))
                Spacer(Modifier.height(2.dp))
                SettingsCardRow {
                    Column(Modifier.fillMaxWidth()) {
                        if (!hasNotificationPermission) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stringResource(Res.string.settings_main_notifications_disabled_title),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = stringResource(Res.string.settings_main_notifications_disabled_subtitle),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                val permissionRemedy = notificationPermissionStatus.remedy()
                                TextButton(
                                    onClick = {
                                        when (permissionRemedy) {
                                            NotificationPermissionRemedy.REQUEST_PERMISSION ->
                                                coroutineScope.launch {
                                                    requestPlatformNotificationPermission()
                                                    notificationPermissionStatus =
                                                        getPlatformNotificationPermissionStatus()
                                                }
                                            else -> openPlatformNotificationSettings()
                                        }
                                    }
                                ) {
                                    Text(
                                        if (permissionRemedy == NotificationPermissionRemedy.REQUEST_PERMISSION) {
                                            stringResource(Res.string.settings_main_notifications_enable)
                                        } else {
                                            stringResource(Res.string.settings_main_notifications_open_settings)
                                        }
                                    )
                                }
                            }
                        } else {
                            SettingsToggleRow(
                                title = stringResource(Res.string.settings_main_notifications_enabled_title),
                                subtitle = stringResource(Res.string.settings_main_notifications_enabled_subtitle),
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = viewModel::updateNotificationsEnabled
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_main_notification_filters_title),
                            subtitle = stringResource(Res.string.settings_main_notification_filters_subtitle),
                            onClick = onOpenNotificationSettings,
                            enabled = hasNotificationPermission && uiState.notificationsEnabled
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_main_system_settings_title),
                            subtitle = stringResource(Res.string.settings_main_system_settings_subtitle),
                            onClick = onOpenSystemNotificationSettings,
                            enabled = hasNotificationPermission && uiState.notificationsEnabled
                        )
                    }
                }
            }

            // SUPPORT & MEMBERSHIP
            item {
                SectionHeaderText(stringResource(Res.string.settings_main_section_premium))
                Spacer(Modifier.height(2.dp))
                SettingsCardRow {
                    Column(Modifier.fillMaxWidth()) {
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_main_go_premium_title),
                            subtitle = stringResource(Res.string.settings_main_go_premium_subtitle),
                            onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.SupportUs(PaywallSource.SETTINGS_ROW.value)) },
                            icon = Icons.Filled.Star
                        )
                    }
                }
            }

            // WEAR OS — Android only
            if (isWearOsSupportedPlatform) {
                item {
                    var watchConnected by remember { mutableStateOf<Boolean?>(null) }
                    LaunchedEffect(Unit) {
                        watchConnected = isWearOsConnected()
                    }

                    SectionHeaderText(stringResource(Res.string.settings_main_section_wear_os))
                    Spacer(Modifier.height(2.dp))

                    when (watchConnected) {
                        // Still checking — show nothing to avoid flicker
                        null -> {}

                        // No watch paired/nearby — simple informational card
                        false -> {
                            SettingsCardRow {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Watch,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = stringResource(Res.string.settings_main_wear_available_title),
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = stringResource(Res.string.settings_main_wear_available_body),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Watch connected — full early-release info card
                        true -> {
                            SettingsCardRow {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Watch,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = stringResource(Res.string.settings_main_wear_early_release),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Text(
                                            text = stringResource(Res.string.settings_main_wear_connected),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(Res.string.settings_main_wear_early_release_body),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider()
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = stringResource(Res.string.settings_main_wear_data_updates_title),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(Res.string.settings_main_wear_data_updates_body),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // DIAGNOSTIC LOGGING
            item {
                val loggingPreferences: LoggingPreferences = koinInject()
                SectionHeaderText(stringResource(Res.string.settings_main_section_diagnostic))
                Spacer(Modifier.height(2.dp))
                LoggingSettingsSection(
                    loggingPreferences = loggingPreferences,
                    onOpenDiagnostics = { navController.navigate(Diagnostics) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // DEBUG
            if (BuildConfig.IS_DEBUG || debugMenuUnlocked) {
                item {
                    SectionHeaderText(stringResource(Res.string.settings_main_section_developer))
                    Spacer(Modifier.height(2.dp))
                    SettingsCardRow {
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_main_debug_title),
                            subtitle = stringResource(Res.string.settings_main_debug_subtitle),
                            onClick = onOpenDebugSettings,
                            icon = Icons.Filled.Warning
                        )
                    }
                }
            }

            // ABOUT
            item {
                SectionHeaderText(stringResource(Res.string.settings_main_section_about))
                Spacer(Modifier.height(2.dp))
                SettingsCardRow {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.settings_main_roadmap_title),
                        subtitle = stringResource(Res.string.settings_main_roadmap_subtitle),
                        onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Roadmap) }
                    )
                }
                Spacer(Modifier.height(4.dp))
                SettingsCardRow {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.settings_main_feedback_title),
                        subtitle = stringResource(Res.string.settings_main_feedback_subtitle),
                        onClick = { appRatingViewModel.showFeedbackDialog() }
                    )
                }
                Spacer(Modifier.height(4.dp))
                SettingsCardRow {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.settings_main_about_details),
                        onClick = onAbout
                    )
                }
                Spacer(Modifier.height(4.dp))
                SettingsCardRow {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.settings_main_shop_title),
                        subtitle = stringResource(Res.string.settings_main_shop_subtitle),
                        onClick = { uriHandler.openUri(ShopLink.URL) }
                    )
                }
                // Ad privacy / consent revocation
                val privacyOptionsRequired =
                    me.calebjones.spacelaunchnow.ui.ads.rememberPrivacyOptionsRequired()
                if (privacyOptionsRequired) {
                    Spacer(Modifier.height(4.dp))
                    SettingsCardRow {
                        val contextFactory =
                            me.calebjones.spacelaunchnow.LocalContextFactory.current
                        SettingsNavigationRow(
                            title = stringResource(Res.string.settings_main_ad_privacy),
                            onClick = {
                                me.calebjones.spacelaunchnow.ui.ads.showPrivacyOptionsForm(
                                    activity = contextFactory?.getActivity()
                                )
                            }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))
                SettingsCardRow {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.settings_main_privacy_policy),
                        onClick = onPrivacyPolicy
                    )
                }
                Spacer(Modifier.height(4.dp))
                SettingsCardRow {
                    SettingsNavigationRow(
                        title = stringResource(Res.string.settings_main_terms_of_service),
                        onClick = onTerms
                    )
                }


                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier
                            .clickable {
                                tapCount++
                                if (tapCount >= 7) {
                                    showPasswordDialog = true
                                    tapCount = 0
                                }
                            },
                        text = stringResource(Res.string.settings_main_version, BuildConfig.VERSION_NAME),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Password dialog for debug unlock
    if (showPasswordDialog) {
        AdCoveringAlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
                showPasswordError = false
                showPassword = false
                useTotp = false
            },
            title = { Text(if (useTotp) stringResource(Res.string.settings_main_debug_dialog_title_totp) else stringResource(Res.string.settings_main_debug_dialog_title_password)) },
            text = {
                Column {
                    Text(
                        if (useTotp)
                            stringResource(Res.string.settings_main_debug_dialog_message_totp)
                        else
                            stringResource(Res.string.settings_main_debug_dialog_message_password)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = {
                            // For TOTP, limit to 6 digits
                            passwordInput = if (useTotp) {
                                it.filter { char -> char.isDigit() }.take(6)
                            } else {
                                it
                            }
                            showPasswordError = false
                        },
                        label = { Text(if (useTotp) stringResource(Res.string.settings_main_debug_dialog_field_totp) else stringResource(Res.string.settings_main_debug_dialog_field_password)) },
                        visualTransformation = if (useTotp || showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = if (!useTotp) {
                            {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPassword) stringResource(Res.string.settings_main_debug_dialog_hide_password) else stringResource(Res.string.settings_main_debug_dialog_show_password)
                                    )
                                }
                            }
                        } else null,
                        isError = showPasswordError,
                        supportingText = if (showPasswordError) {
                            {
                                Text(
                                    if (useTotp) stringResource(Res.string.settings_main_debug_dialog_error_totp) else stringResource(Res.string.settings_main_debug_dialog_error_password),
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        } else null,
                        singleLine = true,
                        keyboardOptions = if (useTotp) {
                            KeyboardOptions(keyboardType = KeyboardType.Number)
                        } else {
                            KeyboardOptions.Default
                        }
                    )

                    // Toggle between password and TOTP
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(
                            onClick = {
                                useTotp = !useTotp
                                passwordInput = ""
                                showPasswordError = false
                            }
                        ) {
                            Text(
                                if (useTotp) stringResource(Res.string.settings_main_debug_dialog_use_password) else stringResource(Res.string.settings_main_debug_dialog_use_totp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            if (DebugUnlock.verifyPassword(passwordInput)) {
                                appPreferences.setDebugMenuUnlocked(true)
                                showPasswordDialog = false
                                passwordInput = ""
                                showPasswordError = false
                                useTotp = false
                            } else {
                                showPasswordError = true
                            }
                        }
                    }
                ) {
                    Text(stringResource(Res.string.settings_main_debug_dialog_unlock))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        passwordInput = ""
                        showPasswordError = false
                        useTotp = false
                    }
                ) {
                    Text(stringResource(Res.string.settings_main_debug_dialog_cancel))
                }
            }
        )
    }

    // Feedback dialog
    if (shouldShowFeedback) {
        me.calebjones.spacelaunchnow.ui.components.FeedbackDialog(
            onSendEmail = {
                appRatingViewModel.onFeedbackSent()
                me.calebjones.spacelaunchnow.util.ExternalLinkHandler.openEmail(
                    recipient = "hello@spacelaunchnow.app",
                    subject = "SpaceLaunchNow Feedback",
                    body = "I'd like to share feedback about SpaceLaunchNow:\n\n"
                )
            },
            onOpenGitHub = {
                appRatingViewModel.onFeedbackSent()
                uriHandler.openUri("https://github.com/space-launch-now/feedback/issues/new")
            },
            onOpenDiscord = {
                appRatingViewModel.onFeedbackSent()
                uriHandler.openUri("https://discord.gg/WVfzEDW")
            },
            onDismiss = { appRatingViewModel.dismissFeedbackDialog() }
        )
    }
}

@Composable
fun SectionHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp, end = 8.dp)
            .then(modifier)
    )
}

@Composable
fun SectionSubHeaderText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 8.dp, end = 8.dp)
            .then(modifier)
    )
}

@Composable
fun SettingsCardRow(
    content: @Composable RowScope.() -> Unit
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = enabled),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 10.dp)
            )
        }
        val titleColor =
            if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.4f
            )
        val subtitleColor =
            if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = 0.4f
            )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleColor,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSettingRow(
    selected: ThemeOption,
    onSelected: (ThemeOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.settings_theme_tab_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        AdCoveringExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}