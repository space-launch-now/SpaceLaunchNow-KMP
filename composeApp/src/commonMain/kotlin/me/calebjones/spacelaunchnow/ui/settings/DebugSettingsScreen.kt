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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.LocalContextFactory
import me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.platform.billing.createDirectBillingClient
import me.calebjones.spacelaunchnow.platform.billing.isDirectBillingSupported
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.util.BuildConfig
import org.koin.compose.koinInject
import kotlin.time.Clock.System

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(
    onNavigateBack: () -> Unit = {},
    debugViewModel: DebugSettingsViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject()
) {
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

            // API URL Section
            item {
                Text(
                    text = "API Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
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
                        val agencies = listOf(
                            NotificationAgency.SPACEX,
                            NotificationAgency.NASA,
                            NotificationAgency.ROCKET_LAB,
                            NotificationAgency.BLUE_ORIGIN,
                            NotificationAgency.ULA,
                            NotificationAgency.ARIANESPACE,
                            NotificationAgency.ROSCOSMOS,
                            NotificationAgency.NORTHROP_GRUMMAN
                        )

                        val locations = listOf(
                            NotificationLocation.KSC,
                            NotificationLocation.TEXAS,
                            NotificationLocation.VANDENBERG,
                            NotificationLocation.WALLOPS,
                            NotificationLocation.NEW_ZEALAND,
                            NotificationLocation.FRENCH_GUIANA,
                            NotificationLocation.RUSSIA,
                            NotificationLocation.JAPAN,
                            NotificationLocation.INDIA,
                            NotificationLocation.CHINA,
                            NotificationLocation.KODIAK,
                            NotificationLocation.OTHER
                        )

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
                            text = "🎭 Simulate Subscription States",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Get billing client and coroutine scope
                        val billingClient =
                            koinInject<me.calebjones.spacelaunchnow.data.billing.BillingClient>()
                        val coroutineScope = rememberCoroutineScope()

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

                        // Free state
                        OutlinedButton(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.simulateSubscriptionState(
                                        isSubscribed = false,
                                        subscriptionType = SubscriptionType.FREE,
                                        productId = null
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🆓 Set FREE")
                        }

                        // Current subscription products
                        Button(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.simulateSubscriptionState(
                                        isSubscribed = true,
                                        subscriptionType = SubscriptionType.PREMIUM,
                                        productId = SubscriptionProducts.PRODUCT_ID
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("⭐ Set PREMIUM (Current Product)")
                        }

                        Button(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.simulateSubscriptionState(
                                        isSubscribed = true,
                                        subscriptionType = SubscriptionType.PREMIUM,
                                        productId = SubscriptionProducts.PRO_LIFETIME
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD700),
                                contentColor = Color(0xFF1C1C1C)
                            )
                        ) {
                            Text("✨ Set PRO LIFETIME")
                        }

                        // Legacy SKUs section
                        HorizontalDivider()

                        Text(
                            text = "Legacy/Unknown SKUs:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Button(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.simulateSubscriptionState(
                                        isSubscribed = true,
                                        subscriptionType = SubscriptionType.LEGACY,  // LEGACY = isLegacy: true
                                        productId = "spacelaunchnow_premium_legacy"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("🏛️ Legacy Premium SKU")
                        }

                        Button(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.simulateSubscriptionState(
                                        isSubscribed = true,
                                        subscriptionType = SubscriptionType.LEGACY,  // LEGACY = isLegacy: true
                                        productId = "space_launch_now_pro_v1"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Text("🗂️ Old Pro V1 SKU")
                        }

                        Button(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.simulateSubscriptionState(
                                        isSubscribed = true,
                                        subscriptionType = SubscriptionType.LEGACY,  // LEGACY = isLegacy: true
                                        productId = "unknown_premium_sku_12345"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("❓ Unknown SKU")
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Get context factory outside of onClick to avoid composable issues
                            val contextFactory = LocalContextFactory.current
                            
                            Button(
                                onClick = {
                                    if (customSku.isNotBlank()) {
                                        coroutineScope.launch {
                                            isPurchasing = true

                                            // Check if direct billing is supported
                                            if (!isDirectBillingSupported()) {
                                                snackbarHostState.showSnackbar(
                                                    "❌ Direct billing is only available on Android"
                                                )
                                                isPurchasing = false
                                                return@launch
                                            }

                                            // Get activity context for Android
                                            try {
                                                val activity = contextFactory?.getActivity()

                                                if (activity == null) {
                                                    snackbarHostState.showSnackbar(
                                                        "❌ Activity context not available"
                                                    )
                                                    isPurchasing = false
                                                    return@launch
                                                }

                                                // Create direct billing client using factory function
                                                val directBilling =
                                                    createDirectBillingClient(activity)

                                                // Initialize
                                                directBilling.initialize().fold(
                                                    onSuccess = {
                                                        // Launch purchase flow
                                                        val basePlan =
                                                            if (customProductType == "subs" && customBasePlan.isNotBlank()) {
                                                                customBasePlan
                                                            } else null

                                                        directBilling.launchPurchaseFlow(
                                                            productId = customSku,
                                                            productType = customProductType,
                                                            basePlanId = basePlan
                                                        ).fold(
                                                            onSuccess = {
                                                                snackbarHostState.showSnackbar(
                                                                    "✅ Purchase flow launched: $customSku"
                                                                )
                                                            },
                                                            onFailure = { error ->
                                                                snackbarHostState.showSnackbar(
                                                                    "❌ ${error.message}"
                                                                )
                                                            }
                                                        )
                                                        directBilling.disconnect()
                                                    },
                                                    onFailure = { error ->
                                                        snackbarHostState.showSnackbar(
                                                            "❌ Billing init failed: ${error.message}"
                                                        )
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                    "❌ Error: ${e.message}"
                                                )
                                            } finally {
                                                isPurchasing = false
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = customSku.isNotBlank() && !isLoading && !isPurchasing && isDirectBillingSupported(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                )
                            ) {
                                if (isPurchasing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (isPurchasing) "Purchasing..." else "💳 Buy Product (Direct)")
                            }
                        }

                        Text(
                            text = if (isDirectBillingSupported()) {
                                "💡 Uses native Android Billing Library:\n" +
                                        "• Works with ANY product in Google Play Console\n" +
                                        "• Doesn't require RevenueCat configuration\n" +
                                        "• Perfect for testing legacy or beta products"
                            } else {
                                "⚠️ Direct billing not supported on this platform\n" +
                                        "• Available on Android only\n" +
                                        "• iOS/Desktop: Use RevenueCat offerings instead"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )

                        // Advanced options
                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                        subscriptionRepo.simulateNeedsVerification(true)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⚠️ Needs Verification", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                        subscriptionRepo.simulateExpiredSubscription()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("⏰ Expired", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (subscriptionRepo is me.calebjones.spacelaunchnow.data.repository.SubscriptionRepositoryImpl) {
                                    subscriptionRepo.clearDebugSimulation()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("🔄 Clear Simulation (Use Real State)")
                        }

                        Text(
                            text = "💡 These states persist until you change them or restart the app. They override real billing client data. Use 'Clear Simulation' to return to real billing state.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            onClick = { debugViewModel.checkRevenueCatInitialization() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("✅ Check Initialization Status")
                        }

                        // Query Products
                        Button(
                            onClick = { debugViewModel.queryRevenueCatProducts() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("📦 Query Products/Offerings")
                        }

                        // Check Entitlements
                        Button(
                            onClick = { debugViewModel.checkRevenueCatEntitlements() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("🔐 Check Customer Entitlements")
                        }

                        // Test Restore
                        OutlinedButton(
                            onClick = { debugViewModel.testRevenueCatRestore() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text("🔄 Test Restore Purchases")
                        }

                        // View Offering Details
                        OutlinedButton(
                            onClick = { debugViewModel.viewRevenueCatOfferingDetails() },
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