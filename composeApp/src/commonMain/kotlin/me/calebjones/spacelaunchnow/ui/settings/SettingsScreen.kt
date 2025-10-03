package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption
import me.calebjones.spacelaunchnow.util.BuildConfig
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    onOpenNotificationSettings: () -> Unit = {},
    onOpenDebugSettings: () -> Unit = {},
    onOpenAboutLibraries: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val onPrivacyPolicy = {
        uriHandler.openUri("https://spacelaunchnow.app/site/privacy")
    }
    val onTerms = {
        uriHandler.openUri("https://spacelaunchnow.app/site/tos")
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
                            ThemeSettingRow(
                                selected = uiState.theme,
                                onSelected = viewModel::updateTheme
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            SettingsToggleRow(
                                title = "Use UTC time",
                                subtitle = "Show dates/times in UTC instead of local timezone",
                                checked = uiState.useUtc,
                                onCheckedChange = viewModel::updateUseUtc
                            )
                        }
                    }
                }
                item {
                    SectionHeaderText("Home Page Filters")
                    Spacer(Modifier.height(2.dp))
                    SettingsCardRow {
                        Column(Modifier.fillMaxWidth()) {
                            NotificationTopicToggle(
                                title = "Hide TBD Launches",
                                description = "Hide launches without confirmed dates",
                                checked = uiState.hideTbdLaunches,
                                onCheckedChange = viewModel::updateHideTbdLaunches
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            NotificationTopicToggle(
                                title = "Keep Launches for 24 Hours",
                                description = "Show past launches for one day after completion",
                                checked = uiState.keepLaunchesFor24Hours,
                                onCheckedChange = viewModel::updateKeepLaunchesFor24Hours
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
                            SettingsToggleRow(
                                title = "Notifications Enabled",
                                subtitle = "Allow notifications for news, launches and events",
                                checked = uiState.notificationsEnabled,
                                onCheckedChange = viewModel::updateNotificationsEnabled
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            SettingsNavigationRow(
                                title = "Notification Settings",
                                subtitle = "Configure launch notifications and subscriptions",
                                onClick = onOpenNotificationSettings,
                                enabled = uiState.notificationsEnabled
                            )
                        }
                    }
                }
                // DEBUG
                if (BuildConfig.IS_DEBUG) {
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
                            title = "About Details",
                            onClick = onAbout
                        )
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
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, bottom = 4.dp, top = 10.dp)
    )
}

@Composable
private fun SettingsCardRow(
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
            .fillMaxWidth(),
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