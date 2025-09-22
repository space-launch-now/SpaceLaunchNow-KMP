package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.ThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onOpenLicenses: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Show error snackbar if there's an error
    uiState.errorMessage?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item { SectionHeader(title = "General") }
            item {
                ThemeSettingRow(
                    selected = uiState.theme,
                    onSelected = viewModel::updateTheme
                )
            }
            item {
                SettingsToggleRow(
                    title = "Use UTC time",
                    subtitle = "Show dates/times in UTC instead of local timezone",
                    checked = uiState.useUtc,
                    onCheckedChange = viewModel::updateUseUtc
                )
            }

            item { SectionHeader(title = "Notifications") }
            item {
                SettingsToggleRow(
                    title = "Enable notifications",
                    subtitle = "Receive notifications for upcoming launches",
                    checked = uiState.notificationSettings.enableNotifications,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            viewModel.requestNotificationPermission()
                        }
                        viewModel.updateNotificationSettings(enabled)
                    }
                )
            }
            item {
                SettingsToggleRow(
                    title = "Daily summary",
                    subtitle = "Get a daily digest of the next launches",
                    checked = uiState.notificationSettings.notifyDailySummary,
                    onCheckedChange = viewModel::updateDailySummary,
                    enabled = uiState.notificationSettings.enableNotifications
                )
            }

            item { SectionHeader(title = "About") }
            item {
                SettingsNavigationRow(
                    title = "Open source licenses",
                    onClick = onOpenLicenses
                )
            }
            item {
                SettingsNavigationRow(
                    title = "Privacy policy",
                    onClick = onOpenPrivacyPolicy
                )
            }
            item {
                SettingsNavigationRow(
                    title = "Terms of service",
                    onClick = onOpenTerms
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsToggleRow(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle != null) Text(subtitle) },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    )
    Divider()
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { if (subtitle != null) Text(subtitle) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
    Divider()
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
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeOption.values().forEach { option ->
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
    Divider(modifier = Modifier.padding(top = 8.dp))
}