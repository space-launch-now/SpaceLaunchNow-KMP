package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.hasPlatformNotificationPermission
import me.calebjones.spacelaunchnow.data.repository.openPlatformNotificationSettings
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.navigation.CalendarSync
import me.calebjones.spacelaunchnow.navigation.ThemeCustomization
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.DebugUnlock
import me.calebjones.spacelaunchnow.util.NotificationSettingsHelper
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    val appRatingViewModel: me.calebjones.spacelaunchnow.ui.viewmodel.AppRatingViewModel = koinViewModel()
    val shouldShowFeedback by appRatingViewModel.shouldShowFeedbackDialog.collectAsState()

    // Tap counter for debug unlock
    var tapCount by remember { mutableStateOf(0) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var showPasswordError by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var useTotp by remember { mutableStateOf(false) } // Toggle between password and TOTP

    var hasNotificationPermission by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        hasNotificationPermission = hasPlatformNotificationPermission()
    }

    // Re-check when returning from settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    hasNotificationPermission = hasPlatformNotificationPermission()
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

    if (uiState.isLoading) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.material3.CircularProgressIndicator()
        }
    } else {
        Column {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        fontSize = 36.sp,
                    )
                }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 0.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // GENERAL
                item {
                    SectionHeaderText("General")
                    Spacer(Modifier.height(2.dp))
                    SettingsCardRow {
                        Column(Modifier.fillMaxWidth()) {
                            SettingsToggleRow(
                                title = "Use UTC time",
                                subtitle = "Show dates/times in UTC instead of local timezone",
                                checked = uiState.useUtc,
                                onCheckedChange = viewModel::updateUseUtc
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            NotificationTopicToggle(
                                title = "Hide TBD Launches",
                                description = "Hide launches without confirmed dates",
                                checked = uiState.hideTbdLaunches,
                                onCheckedChange = viewModel::updateHideTbdLaunches
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SettingsNavigationRow(
                                title = "Theme Settings",
                                subtitle = "Appearance, colors, and palette customization",
                                onClick = { navController.navigate(ThemeCustomization) },
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SettingsNavigationRow(
                                title = "Calendar Sync",
                                subtitle = "Sync Launches and Events to your calendar",
                                onClick = { navController.navigate(CalendarSync) },
                            )
                        }
                    }
                }
                // NOTIFICATIONS
                item {
                    SectionHeaderText("Notifications")
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
                                            text = "Notifications Disabled",
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Permission required to send notifications",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                    TextButton(
                                        onClick = { openPlatformNotificationSettings() }
                                    ) {
                                        Text("Open Settings")
                                    }
                                }
                            } else {
                                SettingsToggleRow(
                                    title = "Notifications Enabled",
                                    subtitle = "Allow notifications for news, launches and events",
                                    checked = uiState.notificationsEnabled,
                                    onCheckedChange = viewModel::updateNotificationsEnabled
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsNavigationRow(
                                title = "Launch Filters",
                                subtitle = "Filter launches shown in app and notifications",
                                onClick = onOpenNotificationSettings,
                                enabled = hasNotificationPermission && uiState.notificationsEnabled
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsNavigationRow(
                                title = "System Settings",
                                subtitle = "Manage notification channels, sounds, and do-not-disturb",
                                onClick = onOpenSystemNotificationSettings,
                                enabled = hasNotificationPermission && uiState.notificationsEnabled
                            )
                        }
                    }
                }

                // SUPPORT & MEMBERSHIP
                item {
                    SectionHeaderText("Premium")
                    Spacer(Modifier.height(2.dp))
                    SettingsCardRow {
                        Column(Modifier.fillMaxWidth()) {
                            SettingsNavigationRow(
                                title = "Go Premium \u2728",
                                subtitle = "Ad-free, widgets, themes & more",
                                onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.SupportUs) },
                                icon = Icons.Filled.Star
                            )
                        }
                    }
                }

                // DIAGNOSTIC LOGGING
                item {
                    val loggingPreferences: LoggingPreferences = koinInject()
                    SectionHeaderText("Diagnostic")
                    Spacer(Modifier.height(2.dp))
                    LoggingSettingsSection(
                        loggingPreferences = loggingPreferences,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }

                // DEBUG
                if (BuildConfig.IS_DEBUG || debugMenuUnlocked) {
                    item {
                        SectionHeaderText("Developer")
                        Spacer(Modifier.height(2.dp))
                        SettingsCardRow {
                            SettingsNavigationRow(
                                title = "Debug Settings",
                                subtitle = "API URL switching, topic testing, and developer options",
                                onClick = onOpenDebugSettings,
                                icon = Icons.Filled.Warning
                            )
                        }
                    }
                }

                // ABOUT
                item {
                    SectionHeaderText("About")
                    Spacer(Modifier.height(2.dp))
                    SettingsCardRow {
                        SettingsNavigationRow(
                            title = "Roadmap",
                            subtitle = "View planned features and development timeline",
                            onClick = { navController.navigate(me.calebjones.spacelaunchnow.navigation.Roadmap) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    SettingsCardRow {
                        SettingsNavigationRow(
                            title = "Send Feedback",
                            subtitle = "Report issues or suggest improvements",
                            onClick = { appRatingViewModel.showFeedbackDialog() }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    SettingsCardRow {
                        SettingsNavigationRow(
                            title = "About Details",
                            onClick = onAbout
                        )
                    }
                    // Ad privacy / consent revocation
                    val privacyOptionsRequired = me.calebjones.spacelaunchnow.ui.ads.rememberPrivacyOptionsRequired()
                    if (privacyOptionsRequired) {
                        Spacer(Modifier.height(4.dp))
                        SettingsCardRow {
                            val contextFactory = me.calebjones.spacelaunchnow.LocalContextFactory.current
                            SettingsNavigationRow(
                                title = "Ad privacy settings",
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
                            title = "Privacy policy",
                            onClick = onPrivacyPolicy
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    SettingsCardRow {
                        SettingsNavigationRow(
                            title = "Terms of service",
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
                            text = "Version ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }

    // Password dialog for debug unlock
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showPasswordDialog = false
                passwordInput = ""
                showPasswordError = false
                showPassword = false
                useTotp = false
            },
            title = { Text(if (useTotp) "Enter TOTP Code" else "Enter Debug Password") },
            text = {
                Column {
                    Text(
                        if (useTotp)
                            "Enter the 6-digit code from your authenticator app:"
                        else
                            "Enter the password to unlock developer settings:"
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
                        label = { Text(if (useTotp) "TOTP Code" else "Password") },
                        visualTransformation = if (useTotp || showPassword)
                            VisualTransformation.None
                        else
                            PasswordVisualTransformation(),
                        trailingIcon = if (!useTotp) {
                            {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            }
                        } else null,
                        isError = showPasswordError,
                        supportingText = if (showPasswordError) {
                            {
                                Text(
                                    if (useTotp) "Invalid TOTP code" else "Incorrect password",
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
                                if (useTotp) "Use Password Instead" else "Use TOTP Instead",
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
                    Text("Unlock")
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
                    Text("Cancel")
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
            label = { Text("Theme") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
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