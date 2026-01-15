package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.SimpleSubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import org.koin.compose.koinInject
import kotlin.time.Clock.System

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(
    onNavigateBack: () -> Unit = {},
    debugViewModel: DebugSettingsViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject()
) {
    val log = SpaceLogger.getLogger("DebugSettingsScreen")
    val appPreferences = koinInject<me.calebjones.spacelaunchnow.data.storage.AppPreferences>()
    val debugMenuUnlocked by appPreferences.debugMenuUnlockedFlow.collectAsState(initial = false)

    if (!BuildConfig.IS_DEBUG && !debugMenuUnlocked) {
        // Show a message that debug settings are not available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Debug settings are only available in debug builds or when unlocked")
        }
        return
    }

    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val debugSettings by debugViewModel.debugSettings.collectAsStateWithLifecycle()
    val isLoading by debugViewModel.isLoading.collectAsStateWithLifecycle()
    val statusMessage by debugViewModel.statusMessage.collectAsStateWithLifecycle()
    val detailedMessage by debugViewModel.detailedMessage.collectAsStateWithLifecycle()
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val subscriptionState by subscriptionRepo.state.collectAsState()

    // Cache settings
    val debugShortCacheTtl by appPreferences.debugShortCacheTtlFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDetailedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            // Show detailed dialog if there's a detailed message
            if (detailedMessage != null) {
                showDetailedDialog = true
            }
        }
    }

    // Detailed message dialog
    if (showDetailedDialog && detailedMessage != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showDetailedDialog = false
                debugViewModel.clearStatusMessage()
            },
            title = { Text(statusMessage ?: "Details") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = detailedMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showDetailedDialog = false
                        debugViewModel.clearStatusMessage()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }

    var customUrlText by remember { mutableStateOf(debugSettings.customApiBaseUrl) }

    // Update the text field when settings change
    LaunchedEffect(debugSettings.customApiBaseUrl) {
        customUrlText = debugSettings.customApiBaseUrl
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Debug Settings",
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "⚠️ These settings are for development and testing only!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Logging Configuration Section
            item {
                Text(
                    text = "Logging Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                val loggingPreferences: LoggingPreferences = koinInject()
                DebugLoggingSettings(
                    loggingPreferences = loggingPreferences
                )
            }

            // Test Logging Section
            item {
                Text(
                    text = "Test Logging",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                TestLoggingButtons()
            }

            // API URL Section
            item {
                Text(
                    text = "API Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Use Custom API URL Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Custom API URL",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = "Override the default API base URL",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = debugSettings.useCustomApiUrl,
                                onCheckedChange = { debugViewModel.setUseCustomApiUrl(it) },
                                enabled = !isLoading
                            )
                        }

                        if (debugSettings.useCustomApiUrl) {
                            // Custom URL Input
                            OutlinedTextField(
                                value = customUrlText,
                                onValueChange = { customUrlText = it },
                                label = { Text("Custom API Base URL") },
                                placeholder = { Text("https://example.com/api") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading,
                                trailingIcon = {
                                    if (customUrlText != debugSettings.customApiBaseUrl) {
                                        TextButton(
                                            onClick = { debugViewModel.setCustomApiUrl(customUrlText) }
                                        ) {
                                            Text("Save")
                                        }
                                    }
                                }
                            )

                            // Quick URL buttons
                            Text(
                                text = "Quick Options:",
                                style = MaterialTheme.typography.labelMedium
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { debugViewModel.switchToProdUrl() },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Prod", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { debugViewModel.switchToDevUrl() },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Dev", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = { debugViewModel.switchToLocalUrl() },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Local", fontSize = 12.sp)
                                }
                            }
                        }

                        // Current URL display
                        Text(
                            text = "Current URL: ${if (debugSettings.useCustomApiUrl) debugSettings.customApiBaseUrl else DebugPreferences.PROD_API_URL}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Cache Configuration Section
            item {
                Text(
                    text = "Cache Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Short Cache TTL (2 min)",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (debugShortCacheTtl) {
                                        "⚠️ Cache expires in 2 minutes instead of 1 hour"
                                    } else {
                                        "Normal cache duration: 1 hour for all data types"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (debugShortCacheTtl) {
                                        MaterialTheme.colorScheme.error
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            Switch(
                                checked = debugShortCacheTtl,
                                onCheckedChange = { enabled ->
                                    scope.launch {
                                        appPreferences.setDebugShortCacheTtl(enabled)
                                    }
                                }
                            )
                        }

                        if (debugShortCacheTtl) {
                            HorizontalDivider()

                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Debug Cache Durations:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "• All data types: 2 minutes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "• Check logs for cache age info",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Topics Section
            item {
                Text(
                    text = "Notification Topics (v4)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Debug Topics",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = if (debugSettings.useDebugTopics) {
                                        "Using k_debug_v4 topic for testing"
                                    } else {
                                        "Using k_prod_v4 topic for production"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = debugSettings.useDebugTopics,
                                onCheckedChange = { debugViewModel.setUseDebugTopics(it) },
                                enabled = !isLoading
                            )
                        }

                        Text(
                            text = "💡 v4 uses simple topics: k_debug_v4 (test) or k_prod_v4 (production)\n📱 All filtering is now done on the device (client-side)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // FCM Token Section
            item {
                Text(
                    text = "FCM Token",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                val fcmToken by debugViewModel.fcmToken.collectAsStateWithLifecycle()
                val clipboardManager = LocalClipboardManager.current

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📱 Device FCM Token",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Use this token to send test notifications from Firebase Console",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Token display
                        if (fcmToken != null) {
                            OutlinedTextField(
                                value = fcmToken ?: "",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    fontSize = 11.sp
                                ),
                                maxLines = 3
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        fcmToken?.let {
                                            clipboardManager.setText(AnnotatedString(it))
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Token copied to clipboard")
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = fcmToken != null && !fcmToken!!.startsWith("Error") && !fcmToken!!.startsWith("Exception")
                                ) {
                                    Text("Copy Token")
                                }

                                OutlinedButton(
                                    onClick = { debugViewModel.fetchFcmToken() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !isLoading
                                ) {
                                    Text("Refresh")
                                }
                            }
                        } else {
                            Button(
                                onClick = { debugViewModel.fetchFcmToken() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Get FCM Token")
                            }
                        }
                    }
                }
            }

            // Notification Permissions Section
            item {
                Text(
                    text = "Notification Permissions (Android)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🔐 Permission Testing",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Button(
                            onClick = { settingsViewModel.requestNotificationPermission() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("Request Notification Permission")
                        }

                        OutlinedButton(
                            onClick = {
                                debugViewModel.resetNotificationPermissionFlag()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("Reset 'Asked Permission' Flag")
                        }

                        Text(
                            text = "💡 First button triggers permission request. Second button resets the flag so permission will be requested on next app launch (like fresh install).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Test Notification Section
            item {
                Text(
                    text = "Test Notifications (v4)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🧪 Notification Testing",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Define available options
                        val agencies = NotificationAgency.getAll()

                        val locations = NotificationLocation.getAll()


                        val notificationTypes = listOf(
                            "netstampChanged" to "Launch Time Changed",
                            "twentyFourHour" to "24 Hours Before",
                            "oneHour" to "1 Hour Before",
                            "tenMinutes" to "10 Minutes Before",
                            "oneMinute" to "1 Minute Before",
                            "inFlight" to "In-Flight Update",
                            "success" to "Launch Success"
                        )

                        // Form state
                        var selectedAgency by remember { mutableStateOf(agencies[0]) }
                        var selectedLocation by remember { mutableStateOf(locations[0]) }
                        var webcast by remember { mutableStateOf("true") }
                        var selectedNotificationType by remember { mutableStateOf(notificationTypes[2].first) } // oneHour default
                        var launchImage by remember { mutableStateOf("https://thespacedevs-prod.nyc3.digitaloceanspaces.com/media/images/starship_on_the_image_20250111100520.jpg") }

                        // Dropdown states
                        var agencyExpanded by remember { mutableStateOf(false) }
                        var locationExpanded by remember { mutableStateOf(false) }
                        var notificationTypeExpanded by remember { mutableStateOf(false) }

                        // Agency Picker
                        ExposedDropdownMenuBox(
                            expanded = agencyExpanded,
                            onExpandedChange = { agencyExpanded = !agencyExpanded }
                        ) {
                            OutlinedTextField(
                                value = "${selectedAgency.name} (${selectedAgency.id})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Agency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agencyExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = !isLoading,
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = agencyExpanded,
                                onDismissRequest = { agencyExpanded = false }
                            ) {
                                agencies.forEach { agency ->
                                    DropdownMenuItem(
                                        text = { Text("${agency.name} (ID: ${agency.id})") },
                                        onClick = {
                                            selectedAgency = agency
                                            agencyExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Location Picker
                        ExposedDropdownMenuBox(
                            expanded = locationExpanded,
                            onExpandedChange = { locationExpanded = !locationExpanded }
                        ) {
                            OutlinedTextField(
                                value = "${selectedLocation.name} (${selectedLocation.id})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Location") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = !isLoading,
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = locationExpanded,
                                onDismissRequest = { locationExpanded = false }
                            ) {
                                locations.forEach { location ->
                                    DropdownMenuItem(
                                        text = { Text("${location.name} (ID: ${location.id})") },
                                        onClick = {
                                            selectedLocation = location
                                            locationExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Webcast toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Webcast Available")
                            Switch(
                                checked = webcast == "true",
                                onCheckedChange = { webcast = if (it) "true" else "false" },
                                enabled = !isLoading
                            )
                        }

                        // Notification Type Picker
                        ExposedDropdownMenuBox(
                            expanded = notificationTypeExpanded,
                            onExpandedChange = {
                                notificationTypeExpanded = !notificationTypeExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = notificationTypes.find { it.first == selectedNotificationType }?.second
                                    ?: selectedNotificationType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Notification Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = notificationTypeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = !isLoading,
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = notificationTypeExpanded,
                                onDismissRequest = { notificationTypeExpanded = false }
                            ) {
                                notificationTypes.forEach { (typeId, typeName) ->
                                    DropdownMenuItem(
                                        text = { Text(typeName) },
                                        onClick = {
                                            selectedNotificationType = typeId
                                            notificationTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Launch Image URL Field
                        OutlinedTextField(
                            value = launchImage,
                            onValueChange = { launchImage = it },
                            label = { Text("Launch Image URL") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = false,
                            maxLines = 3
                        )

                        Button(
                            onClick = {
                                debugViewModel.triggerTestNotification(
                                    agencyId = selectedAgency.id.toString(),
                                    locationId = selectedLocation.id.toString(),
                                    webcast = webcast,
                                    notificationType = selectedNotificationType,
                                    launchImage = launchImage
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Send Test Notification")
                        }

                        Text(
                            text = "💡 Select from available agencies, locations, and notification types to test v4 client-side filtering. Test notifications go through the same filtering logic as real FCM notifications.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }
            }

            // Notification History Section
            item {
                Text(
                    text = "Notification History",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                val notificationHistory by debugViewModel.notificationHistory.collectAsStateWithLifecycle()
                val notificationStats by debugViewModel.notificationStats.collectAsStateWithLifecycle()

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "📝 Received Notifications (Last 100)",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "View all notifications received by this device, including filtered ones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Stats display
                        notificationStats?.let { stats ->
                            HorizontalDivider()
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Statistics:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "• Total Received: ${stats.totalReceived}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "• Displayed: ${stats.totalDisplayed} ✅",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "• Filtered: ${stats.totalFiltered} 🔇",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { debugViewModel.loadNotificationHistory() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text("Load History")
                            }

                            OutlinedButton(
                                onClick = { debugViewModel.clearNotificationHistory() },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading && notificationHistory.isNotEmpty()
                            ) {
                                Text("Clear")
                            }
                        }

                        // History list
                        if (notificationHistory.isNotEmpty()) {
                            HorizontalDivider()
                            Text(
                                text = "Recent Notifications:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Show latest 5 notifications
                            notificationHistory.take(5).forEach { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (item.wasFiltered) {
                                            MaterialTheme.colorScheme.errorContainer
                                        } else {
                                            MaterialTheme.colorScheme.primaryContainer
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.launchName ?: "Unknown Launch",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    text = if (item.wasShown) "👁️" else "🚫",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = if (item.wasFiltered) "🔇" else "✅",
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Type: ${item.notificationType}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Received: ${item.receivedAt}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Filtered: ${if (item.wasFiltered) "Yes" else "No"} | Shown: ${if (item.wasShown) "Yes" else "No"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (item.wasShown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                        if (item.wasFiltered) {
                                            Text(
                                                text = "Reason: ${item.filterReason ?: "Unknown"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                        
                                        // Show raw JSON data
                                        if (item.rawData.isNotEmpty()) {
                                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                            Text(
                                                text = "📦 Raw FCM Data:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surface
                                                )
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(8.dp),
                                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    item.rawData.forEach { (key, value) ->
                                                        Text(
                                                            text = "$key: $value",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (notificationHistory.size > 5) {
                                Text(
                                    text = "... and ${notificationHistory.size - 5} more (total: ${notificationHistory.size})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "No notifications received yet. Send a test notification or wait for a real FCM notification.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Info Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "ℹ️ Information",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• API URL changes require app restart to take full effect\n" +
                                    "• Topic changes are applied immediately\n" +
                                    "• These settings are stored locally and persist between app launches",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Subscription Simulation Section
            item {
                Text(
                    text = "Subscription Simulation",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🎭 Subscription Simulation",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        val coroutineScope = rememberCoroutineScope()

                        // Cast to SimpleSubscriptionRepository to access debug methods
                        val simpleRepo = subscriptionRepo as? SimpleSubscriptionRepository
                        var isSimulationActive by remember { mutableStateOf(false) }

                        // Debug logging
                        LaunchedEffect(Unit) {
                            log.d { "🎭 SubscriptionRepository type: ${subscriptionRepo::class.simpleName}, SimpleSubscriptionRepository cast: ${if (simpleRepo != null) "✅ Success" else "❌ Failed"}" }
                        }

                        // Check if we're in debug mode
                        LaunchedEffect(subscriptionState) {
                            simpleRepo?.let {
                                isSimulationActive = it.isInDebugMode()
                                log.d { "🎭 Debug Mode Check: isSimulationActive = $isSimulationActive" }
                            }
                        }

                        // Show error if repository can't be cast
                        if (simpleRepo == null) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚠️ Subscription Simulation Not Available",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Repository type: ${subscriptionRepo::class.simpleName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Text(
                                        text = "Expected: SimpleSubscriptionRepository",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        } else {

                            // Current state display
                            Surface(
                                color = if (subscriptionState.isSubscribed) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = MaterialTheme.shapes.small
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "📊 Current State:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (subscriptionState.isSubscribed) {
                                            "${subscriptionState.subscriptionType.name} (${subscriptionState.productId ?: "Unknown"})"
                                        } else {
                                            "FREE"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )

                                    // Show features
                                    if (subscriptionState.features.isNotEmpty()) {
                                        Text(
                                            text = "Features: ${subscriptionState.features.joinToString { it.name }}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    // Show simulation status
                                    Text(
                                        text = if (isSimulationActive) "⚠️ SIMULATION ACTIVE" else "✅ Real Billing Data",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSimulationActive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            HorizontalDivider()

                            // Simulation Toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Simulation Mode:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSimulationActive) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                simpleRepo?.clearDebugState()
                                                isSimulationActive = false
                                            }
                                        }
                                    ) {
                                        Text("🔄 Use Real Data")
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            // Enable simulation with premium state as default
                                            coroutineScope.launch {
                                                simpleRepo?.setDebugSubscription(
                                                    subscriptionType = SubscriptionType.PREMIUM,
                                                    productId = "debug_premium",
                                                    entitlements = setOf("premium")
                                                )
                                                isSimulationActive = true
                                            }
                                        }
                                    ) {
                                        Text("🎭 Enable Simulation")
                                    }
                                }
                            }

                            // Simulation Controls (only show when simulation is active)
                            if (isSimulationActive && simpleRepo != null) {
                                HorizontalDivider()

                                Text(
                                    text = "Subscription Types:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                // Free State
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            simpleRepo.setDebugSubscription(SubscriptionType.FREE)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("🆓 FREE")
                                }

                                // Premium State
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            simpleRepo.setDebugSubscription(
                                                subscriptionType = SubscriptionType.PREMIUM,
                                                productId = "debug_premium",
                                                entitlements = setOf("premium")
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text("⭐ PREMIUM")
                                }

                                // Lifetime State
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            simpleRepo.setDebugSubscription(
                                                subscriptionType = SubscriptionType.LIFETIME,
                                                productId = "debug_lifetime",
                                                entitlements = setOf("premium")
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary,
                                        contentColor = MaterialTheme.colorScheme.onTertiary
                                    )
                                ) {
                                    Text("✨ LIFETIME")
                                }

                                // Legacy State
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            simpleRepo.setDebugSubscription(
                                                subscriptionType = SubscriptionType.LEGACY,
                                                productId = "debug_legacy",
                                                entitlements = setOf("legacy")
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {
                                    Text("🏛️ LEGACY")
                                }

                                HorizontalDivider()

                                // Force refresh widget access
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            val hasAccess =
                                                subscriptionRepo.forceRefreshWidgetAccess()
                                            log.i { "Widget access refreshed: $hasAccess" }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text("🔧 Refresh Widget Access")
                                }
                            }

                            // Close the else block for simpleRepo != null
                        }

                        // Restore Purchases Button (always available)
                        HorizontalDivider()

                        var isRestoring by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isRestoring = true
                                    try {
                                        val result = subscriptionRepo.restorePurchases()
                                        if (result.isSuccess) {
                                            log.i { "✅ Restore successful: ${result.getOrNull()}" }
                                        } else {
                                            log.w { "❌ Restore failed: ${result.exceptionOrNull()?.message}" }
                                        }
                                    } catch (e: Exception) {
                                        log.e(e) { "❌ Restore error: ${e.message}" }
                                    } finally {
                                        isRestoring = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isRestoring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (isRestoring) {
                                Text("🔄 Restoring...")
                            } else {
                                Text("🔄 Restore Purchases (Real)")
                            }
                        }

                        // Info text
                        Text(
                            text = if (isSimulationActive) {
                                "💡 Simulation mode overrides real billing data. Widget access will update automatically when you change subscription states. Use 'Use Real Data' to return to actual billing status."
                            } else {
                                "💡 Use 'Restore Purchases' to sync with your actual subscription status. Enable simulation to test different subscription states."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Restore Purchases Button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    isRestoring = true
                                    try {
                                        val result = subscriptionRepo.restorePurchases()
                                        if (result.isSuccess) {
                                            log.i { "✅ Restore successful: ${result.getOrNull()}" }
                                        } else {
                                            log.w { "❌ Restore failed: ${result.exceptionOrNull()?.message}" }
                                        }
                                    } catch (e: Exception) {
                                        log.e(e) { "❌ Restore error: ${e.message}" }
                                    } finally {
                                        isRestoring = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isRestoring,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            if (isRestoring) {
                                Text("🔄 Restoring...")
                            } else {
                                Text("🔄 Restore Purchases")
                            }
                        }

                        Text(
                            text = "💡 Use 'Restore Purchases' to sync with RevenueCat and get your real subscription status.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Get billing client
                        val billingClient =
                            koinInject<me.calebjones.spacelaunchnow.data.billing.BillingClient>()

                        // Current simulated state display
                        Surface(
                            color = if (subscriptionState.isSubscribed) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🎭 Simulated State:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (subscriptionState.isSubscribed)
                                        "${subscriptionState.subscriptionType.name} (${subscriptionState.productId})"
                                    else "FREE",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Actual owned products from billing client
                        var isQuerying by remember { mutableStateOf(false) }
                        var ownedProducts by remember {
                            mutableStateOf<List<me.calebjones.spacelaunchnow.data.model.PlatformPurchase>?>(
                                null
                            )
                        }
                        var queryError by remember { mutableStateOf<String?>(null) }

                        LaunchedEffect(Unit) {
                            // Auto-query on first load
                            isQuerying = true
                            queryError = null
                            billingClient.queryPurchases().fold(
                                onSuccess = { purchases ->
                                    ownedProducts = purchases
                                    isQuerying = false
                                },
                                onFailure = { error ->
                                    queryError = error.message ?: "Unknown error"
                                    isQuerying = false
                                }
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🛒 Real Owned Products:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )

                                    if (!isQuerying) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isQuerying = true
                                                    queryError = null
                                                    billingClient.queryPurchases().fold(
                                                        onSuccess = { purchases ->
                                                            ownedProducts = purchases
                                                            isQuerying = false
                                                        },
                                                        onFailure = { error ->
                                                            queryError =
                                                                error.message ?: "Unknown error"
                                                            isQuerying = false
                                                        }
                                                    )
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                when {
                                    isQuerying -> {
                                        Text(
                                            text = "⏳ Querying...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    }

                                    queryError != null -> {
                                        Text(
                                            text = "❌ Error: $queryError",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    ownedProducts?.isEmpty() == true -> {
                                        Text(
                                            text = "No products owned",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                alpha = 0.7f
                                            )
                                        )
                                    }

                                    ownedProducts != null -> {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            ownedProducts!!.forEach { purchase ->
                                                val subscriptionType =
                                                    me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts.getSubscriptionType(
                                                        purchase.productId
                                                    )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = purchase.productId,
                                                            style = MaterialTheme.typography.bodySmall.copy(
                                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                                            ),
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                                        )
                                                        Text(
                                                            text = subscriptionType.name,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(
                                                                alpha = 0.7f
                                                            )
                                                        )
                                                    }

                                                    val isExpired =
                                                        purchase.expiryTime?.let { expiry ->
                                                            System.now()
                                                                .toEpochMilliseconds() > expiry
                                                        } ?: false

                                                    Surface(
                                                        color = if (isExpired)
                                                            MaterialTheme.colorScheme.error
                                                        else
                                                            MaterialTheme.colorScheme.tertiary,
                                                        shape = MaterialTheme.shapes.extraSmall
                                                    ) {
                                                        Text(
                                                            text = if (isExpired) "EXPIRED" else "ACTIVE",
                                                            modifier = Modifier.padding(
                                                                horizontal = 6.dp,
                                                                vertical = 2.dp
                                                            ),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontSize = 10.sp,
                                                            color = if (isExpired)
                                                                MaterialTheme.colorScheme.onError
                                                            else
                                                                MaterialTheme.colorScheme.onTertiary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        // Custom SKU Input Section
                        HorizontalDivider()

                        var customSku by remember { mutableStateOf("") }
                        var customProductType by remember { mutableStateOf("inapp") }
                        var customBasePlan by remember { mutableStateOf("") }
                        var isPurchasing by remember { mutableStateOf(false) }

                        Text(
                            text = "Custom SKU Purchase (Direct Android Billing):",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Text(
                            text = "⚠️ This uses the native Android Billing Library directly (bypasses RevenueCat)!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = customSku,
                            onValueChange = { customSku = it },
                            label = { Text("Product ID / SKU") },
                            placeholder = { Text("e.g., spacelaunchnow_pro, 2020_super_fan") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && !isPurchasing,
                            singleLine = true,
                            supportingText = {
                                Text(
                                    "Required: Product ID from Google Play Console",
                                    fontSize = 11.sp
                                )
                            }
                        )

                        // Product Type Selector
                        var productTypeExpanded by remember { mutableStateOf(false) }
                        val productTypes =
                            listOf("inapp" to "In-App Product (one-time)", "subs" to "Subscription")

                        ExposedDropdownMenuBox(
                            expanded = productTypeExpanded,
                            onExpandedChange = { productTypeExpanded = !productTypeExpanded }
                        ) {
                            OutlinedTextField(
                                value = productTypes.find { it.first == customProductType }?.second
                                    ?: customProductType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Product Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productTypeExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                enabled = !isLoading && !isPurchasing,
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = productTypeExpanded,
                                onDismissRequest = { productTypeExpanded = false }
                            ) {
                                productTypes.forEach { (type, display) ->
                                    DropdownMenuItem(
                                        text = { Text(display) },
                                        onClick = {
                                            customProductType = type
                                            productTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (customProductType == "subs") {
                            OutlinedTextField(
                                value = customBasePlan,
                                onValueChange = { customBasePlan = it },
                                label = { Text("Base Plan ID (for subscriptions)") },
                                placeholder = { Text("e.g., base-plan, yearly") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && !isPurchasing,
                                singleLine = true,
                                supportingText = {
                                    Text(
                                        "Optional: Leave empty to use default plan",
                                        fontSize = 11.sp
                                    )
                                }
                            )
                        }

                        HorizontalDivider()

                        Text(
                            text = "💡 This queries Google Play Billing Library directly.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )

                    }
                }
            }

            // Subscribed Topics Card (for debug)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "FCM Topics (v4 Simple Subscription)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "v4 only subscribes to version topic. All filtering is client-side.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (uiState.notificationSettings.subscribedTopics.isEmpty()) {
                            Text(
                                text = "⏳ Not yet subscribed (initializing...)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                uiState.notificationSettings.subscribedTopics.sorted()
                                    .forEach { topic ->
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "✅",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Text(
                                                text = topic,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                            }
                        }
                    }
                }
            }

            // RevenueCat Integration Testing Section
            item {
                Text(
                    text = "RevenueCat Integration Testing",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "🧪 RevenueCat SDK Testing",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = "Test RevenueCat integration before deploying to production. These buttons will query the SDK and display results in snackbars.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Check Initialization
                        Button(
                            onClick = { debugViewModel.checkBillingInitialization() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("✅ Check Initialization Status")
                        }

                        // Query Products
                        Button(
                            onClick = { debugViewModel.queryBillingProducts() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("📦 Query Products/Offerings")
                        }

                        // Check Entitlements
                        Button(
                            onClick = { debugViewModel.checkBillingEntitlements() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("🔐 Check Customer Entitlements")
                        }

                        // Test Restore
                        OutlinedButton(
                            onClick = { debugViewModel.testBillingRestore() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("🔄 Test Restore Purchases")
                        }

                        // View Offering Details
                        OutlinedButton(
                            onClick = { debugViewModel.viewBillingProductDetails() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("🎁 View Offering Details")
                        }

                        HorizontalDivider()

                        Text(
                            text = "💡 Tips:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "• Run 'Check Initialization' first to verify SDK is configured\n" +
                                    "• 'Query Products' shows available packages and their prices\n" +
                                    "• 'Check Entitlements' shows what the user has access to\n" +
                                    "• 'Test Restore' simulates restoring purchases from stores\n" +
                                    "• Results will show in long snackbar messages at the bottom",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "⚠️ Note: RevenueCat must be initialized in MainApplication for these tests to work.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isLoading) {
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