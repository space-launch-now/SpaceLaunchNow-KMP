package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.calebjones.spacelaunchnow.data.billing.SubscriptionProducts
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.util.BuildConfig
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugSettingsScreen(
    onNavigateBack: () -> Unit = {},
    debugViewModel: DebugSettingsViewModel = koinInject(),
    settingsViewModel: SettingsViewModel = koinInject()
) {
    if (!BuildConfig.IS_DEBUG) {
        // Show a message that debug settings are not available
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Debug settings are only available in debug builds")
        }
        return
    }

    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val debugSettings by debugViewModel.debugSettings.collectAsStateWithLifecycle()
    val isLoading by debugViewModel.isLoading.collectAsStateWithLifecycle()
    val statusMessage by debugViewModel.statusMessage.collectAsStateWithLifecycle()
    val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
    val subscriptionState by subscriptionRepo.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            debugViewModel.clearStatusMessage()
        }
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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    text = "Notification Topics",
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
                                        "Using debug_v3 topics for testing"
                                    } else {
                                        "Using prod_v3 topics for production"
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
                            text = "💡 Toggle between debug_v3 (test notifications) and prod_v3 (live notifications)",
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

                        // Current state display
                        Surface(
                            color = if (subscriptionState.isSubscribed) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                text = "Current: ${
                                    if (subscriptionState.isSubscribed)
                                        "${subscriptionState.subscriptionType.name} (${subscriptionState.productId})"
                                    else "FREE"
                                }",
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
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
                                        subscriptionType = SubscriptionType.PREMIUM,
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
                                        subscriptionType = SubscriptionType.PREMIUM,
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
                                        subscriptionType = SubscriptionType.PREMIUM,
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
                            text = "FCM Topics (Device is currently subscribed to)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (uiState.notificationSettings.subscribedTopics.isEmpty()) {
                            Text(
                                text = "No active topics.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                uiState.notificationSettings.subscribedTopics.sorted()
                                    .forEach { topic ->
                                        Text(
                                            text = topic,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            ),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                            }
                        }
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