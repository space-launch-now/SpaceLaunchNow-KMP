package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.SubscriptionType
import me.calebjones.spacelaunchnow.data.repository.SimpleSubscriptionRepository
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.DebugSettings
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.ui.viewmodel.DebugSettingsViewModel
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

enum class DebugTab {
    System,
    Notifications,
    Billing
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
    val selectedTab by debugViewModel.selectedTab.collectAsStateWithLifecycle()

    // Cache settings
    val debugShortCacheTtl by appPreferences.debugShortCacheTtlFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    // Tab pager state
    val pagerState = rememberPagerState(
        initialPage = when (selectedTab) {
            DebugTab.System -> 0
            DebugTab.Notifications -> 1
            DebugTab.Billing -> 2
        },
        pageCount = { 3 }
    )

    // Sync tab selection when user swipes pages
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            val newTab = when (pagerState.currentPage) {
                0 -> DebugTab.System
                1 -> DebugTab.Notifications
                2 -> DebugTab.Billing
                else -> DebugTab.System
            }
            if (newTab != selectedTab) {
                debugViewModel.selectTab(newTab)
            }
        }
    }

    // When user taps the tab, animate pager to page
    fun onTabSelected(tab: DebugTab) {
        val target = when (tab) {
            DebugTab.System -> 0
            DebugTab.Notifications -> 1
            DebugTab.Billing -> 2
        }
        if (pagerState.currentPage != target) {
            debugViewModel.selectTab(tab)
            scope.launch {
                pagerState.animateScrollToPage(
                    target,
                    animationSpec = tween(durationMillis = 550)
                )
            }
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            val selectedTabIndex = when (selectedTab) {
                DebugTab.System -> 0
                DebugTab.Notifications -> 1
                DebugTab.Billing -> 2
            }
            
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTab == DebugTab.System,
                    onClick = { onTabSelected(DebugTab.System) },
                    text = { Text("System") }
                )
                Tab(
                    selected = selectedTab == DebugTab.Notifications,
                    onClick = { onTabSelected(DebugTab.Notifications) },
                    text = { Text("Notifications") }
                )
                Tab(
                    selected = selectedTab == DebugTab.Billing,
                    onClick = { onTabSelected(DebugTab.Billing) },
                    text = { Text("Billing") }
                )
            }

            // Horizontal Pager for tab content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> SystemTabContent(
                        debugViewModel = debugViewModel,
                        debugSettings = debugSettings,
                        isLoading = isLoading,
                        customUrlText = customUrlText,
                        onCustomUrlTextChange = { customUrlText = it }
                    )
                    1 -> NotificationsTabContent(
                        debugViewModel = debugViewModel,
                        debugSettings = debugSettings,
                        isLoading = isLoading
                    )
                    2 -> BillingTabContent(
                        debugViewModel = debugViewModel,
                        debugSettings = debugSettings,
                        isLoading = isLoading
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemTabContent(
    debugViewModel: DebugSettingsViewModel,
    debugSettings: DebugSettings,
    isLoading: Boolean,
    customUrlText: String,
    onCustomUrlTextChange: (String) -> Unit
) {
    val loggingPreferences = koinInject<LoggingPreferences>()
    val appPreferences = koinInject<me.calebjones.spacelaunchnow.data.storage.AppPreferences>()
    val debugShortCacheTtl by appPreferences.debugShortCacheTtlFlow.collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning text
        item {
            Text(
                text = "⚠️ System Configuration - Changes may require app restart",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Section 1: Logging Configuration
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Logging Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                DebugLoggingSettings(
                    loggingPreferences = loggingPreferences
                )
            }
        }
        
        // Section 2: Datadog Sample Rate
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Datadog Sample Rate",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                DatadogSampleRateControl(
                    currentRate = debugSettings.datadogSampleRate,
                    onRateChange = { rate ->
                        debugViewModel.setDatadogSampleRate(rate)
                    }
                )
            }
        }
        
        // Section 3: Test Logging
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Test Logging",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TestLoggingButtons()
            }
        }
        
        // Section 4: API Configuration
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "API Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Custom URL Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Custom API URL",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Override the default API endpoint",
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
                        
                        // Custom URL Text Field
                        if (debugSettings.useCustomApiUrl) {
                            HorizontalDivider()
                            
                            OutlinedTextField(
                                value = customUrlText,
                                onValueChange = onCustomUrlTextChange,
                                label = { Text("Custom API Base URL") },
                                placeholder = { Text("https://api.example.com") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                                singleLine = true,
                                trailingIcon = {
                                    if (customUrlText != debugSettings.customApiBaseUrl) {
                                        IconButton(
                                            onClick = { 
                                                debugViewModel.setCustomApiUrl(customUrlText)
                                            }
                                        ) {
                                            Icon(
                                                Icons.Default.Refresh,
                                                contentDescription = "Apply URL"
                                            )
                                        }
                                    }
                                }
                            )
                            
                            // Quick preset buttons
                            HorizontalDivider()
                            
                            Text(
                                text = "Quick Switch:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
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
                            
                            // Current URL Display
                            Text(
                                text = "Current: ${debugSettings.customApiBaseUrl}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Section 5: Cache Configuration
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Cache Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Use Short Cache TTL",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Cache expires in 30 seconds instead of normal duration",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            Text(
                                text = "⚠️ Short cache TTL is enabled. Data will refresh more frequently, which is useful for testing but may increase API usage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
        
        // Section 6: Info Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "💡 Debug Menu Tips",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "• Most settings require app restart to take effect\n" +
                                   "• Custom API URLs must be valid HTTPS endpoints\n" +
                                   "• Short cache TTL increases API calls - use sparingly\n" +
                                   "• Datadog sample rate controls logging costs\n" +
                                   "• Test logging buttons verify your configuration",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationsTabContent(
    debugViewModel: DebugSettingsViewModel,
    debugSettings: DebugSettings,
    isLoading: Boolean
) {
    val scope = rememberCoroutineScope()
    val notificationRepository = koinInject<me.calebjones.spacelaunchnow.data.repository.NotificationRepository>()
    val fcmToken by debugViewModel.fcmToken.collectAsState()
    val notificationHistory by debugViewModel.notificationHistory.collectAsState()
    val notificationStats by debugViewModel.notificationStats.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val platform = me.calebjones.spacelaunchnow.getPlatform()
    
    // Test notification form state
    var selectedAgency by remember { mutableStateOf(me.calebjones.spacelaunchnow.data.model.NotificationAgency.SPACEX) }
    var selectedLocation by remember { mutableStateOf(me.calebjones.spacelaunchnow.data.model.NotificationLocation.FLORIDA) }
    var selectedType by remember { mutableStateOf("twentyFourHour") }
    var agencyDropdownExpanded by remember { mutableStateOf(false) }
    var locationDropdownExpanded by remember { mutableStateOf(false) }
    var typeDropdownExpanded by remember { mutableStateOf(false) }
    
    val availableAgencies = me.calebjones.spacelaunchnow.data.model.NotificationAgency.getAll()
    val availableLocations = me.calebjones.spacelaunchnow.data.model.NotificationLocation.getAll()
    val availableTypes = listOf(
        "twentyFourHour" to "24 Hour Notice",
        "oneHour" to "1 Hour Notice",
        "tenMinutes" to "10 Minute Notice",
        "oneMinute" to "1 Minute Notice",
        "netstampChanged" to "Schedule Changed",
        "inFlight" to "In Flight",
        "success" to "Launch Success",
        "failure" to "Launch Failure",
        "webcastLive" to "Webcast Live"
    )
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning text
        item {
            Text(
                text = "⚠️ Notification Debug Tools - For development and testing only",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Section 1: Notification Topics (v5)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Notification Topics (V5)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
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
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (debugSettings.useDebugTopics) {
                                        "Subscribed to: debug_v5_${platform.name.lowercase()}"
                                    } else {
                                        "Subscribed to: prod_v5_${platform.name.lowercase()}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = debugSettings.useDebugTopics,
                                onCheckedChange = { enabled ->
                                    debugViewModel.setUseDebugTopics(enabled)
                                },
                                enabled = !isLoading
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Text(
                            text = "💡 Debug topics receive test notifications only. Production topics receive real launch notifications.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        
        // Section 2: FCM Token
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "FCM Token",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Firebase Cloud Messaging Token",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        if (fcmToken != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Text(
                                    text = fcmToken ?: "No token",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(12.dp),
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        debugViewModel.fetchFcmToken()
                                    }
                                },
                                enabled = !isLoading,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(if (fcmToken == null) "Get Token" else "Refresh")
                            }
                            
                            if (fcmToken != null) {
                                OutlinedButton(
                                    onClick = {
                                        fcmToken?.let { token ->
                                            clipboardManager.setText(AnnotatedString(token))
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Copy")
                                }
                            }
                        }
                        
                        HorizontalDivider()
                        
                        Text(
                            text = "💡 Use this token to send test notifications from Firebase Console",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        
        // Section 3: Notification Permissions (Android)
        if (platform.name == "Android") {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Notification Permissions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Android 13+ Permissions",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            notificationRepository.requestNotificationPermission()
                                        }
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Request Permission")
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        debugViewModel.resetNotificationPermissionFlag()
                                    },
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Reset Flag")
                                }
                            }
                            
                            HorizontalDivider()
                            
                            Text(
                                text = "💡 Reset flag will prompt permission dialog again on next app launch",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
        
        // Section 4: Test Notifications (v4)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Test Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Send Test Notification",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = "Configure and send a test notification that goes through the same filtering as real FCM notifications",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        HorizontalDivider()
                        
                        // Agency Dropdown
                        ExposedDropdownMenuBox(
                            expanded = agencyDropdownExpanded,
                            onExpandedChange = { agencyDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedAgency.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Agency") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = agencyDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = agencyDropdownExpanded,
                                onDismissRequest = { agencyDropdownExpanded = false }
                            ) {
                                availableAgencies.forEach { agency ->
                                    DropdownMenuItem(
                                        text = { Text(agency.name) },
                                        onClick = {
                                            selectedAgency = agency
                                            agencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Location Dropdown
                        ExposedDropdownMenuBox(
                            expanded = locationDropdownExpanded,
                            onExpandedChange = { locationDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedLocation.name,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Location") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = locationDropdownExpanded,
                                onDismissRequest = { locationDropdownExpanded = false }
                            ) {
                                availableLocations.forEach { location ->
                                    DropdownMenuItem(
                                        text = { Text(location.name) },
                                        onClick = {
                                            selectedLocation = location
                                            locationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Notification Type Dropdown
                        ExposedDropdownMenuBox(
                            expanded = typeDropdownExpanded,
                            onExpandedChange = { typeDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = availableTypes.find { it.first == selectedType }?.second ?: selectedType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Notification Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                            ExposedDropdownMenu(
                                expanded = typeDropdownExpanded,
                                onDismissRequest = { typeDropdownExpanded = false }
                            ) {
                                availableTypes.forEach { (typeId, displayName) ->
                                    DropdownMenuItem(
                                        text = { Text(displayName) },
                                        onClick = {
                                            selectedType = typeId
                                            typeDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                debugViewModel.triggerTestNotification(
                                    agencyId = selectedAgency.id.toString(),
                                    locationId = selectedLocation.id.toString(),
                                    webcast = "true",
                                    notificationType = selectedType,
                                    launchImage = "https://spacelaunchnow-prod-east.nyc3.digitaloceanspaces.com/media/launch_images/falcon2520925_image_20230808174814.jpeg"
                                )
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Send Test Notification")
                        }
                        
                        HorizontalDivider()
                        
                        Text(
                            text = "💡 Test notifications go through the same client-side filtering as real FCM notifications. Check your notification settings if blocked.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
        
        // Section 5: Notification History
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Notification History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Last 100 Notifications",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            IconButton(
                                onClick = {
                                    debugViewModel.loadNotificationHistory()
                                },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh history"
                                )
                            }
                        }
                        
                        // Statistics
                        notificationStats?.let { stats ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "📊 Statistics",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Total Received:",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "${stats.totalReceived}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Displayed:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${stats.totalDisplayed}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Filtered:",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "${stats.totalFiltered}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        
                        // History list
                        if (notificationHistory.isNotEmpty()) {
                            HorizontalDivider()
                            
                            Text(
                                text = "Recent Notifications:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                notificationHistory.take(10).forEach { item ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (item.wasShown) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.errorContainer
                                            }
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = item.notificationType,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = if (item.wasShown) "✅ Shown" else "🔇 Filtered",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            
                                            item.launchName?.let { name ->
                                                Text(
                                                    text = name,
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            
                                            if (!item.wasShown && item.filterReason != null) {
                                                Text(
                                                    text = "Reason: ${item.filterReason}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No notification history available. Click refresh to load.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        
                        if (notificationHistory.isNotEmpty()) {
                            OutlinedButton(
                                onClick = {
                                    debugViewModel.clearNotificationHistory()
                                },
                                enabled = !isLoading,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear History")
                            }
                        }
                        
                        HorizontalDivider()
                        
                        Text(
                            text = "💡 History shows the last 100 notifications received, including filtered ones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BillingTabContent(
    debugViewModel: DebugSettingsViewModel,
    debugSettings: DebugSettings,
    isLoading: Boolean
) {
    val log = SpaceLogger.getLogger("BillingTabContent")
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsState()
    val scope = rememberCoroutineScope()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Warning text
        item {
            Text(
                text = "⚠️ Billing & RevenueCat - Debug Tools",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Section 1: Subscription Simulation
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "💳 Subscription Simulation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Test different subscription states without actual purchases",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    HorizontalDivider()
                    
                    // Current State Display
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Current State",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Subscription Type:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = subscriptionState.subscriptionType.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Is Subscribed:",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = if (subscriptionState.isSubscribed) "✅ Yes" else "❌ No",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Check if in debug mode
                            val simpleRepo = subscriptionRepo as? SimpleSubscriptionRepository
                            var isDebugMode by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(subscriptionState) {
                                isDebugMode = simpleRepo?.isInDebugMode() ?: false
                            }
                            
                            if (isDebugMode) {
                                Text(
                                    text = "🧪 Debug Mode Active",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    HorizontalDivider()
                    
                    // Simulation Buttons
                    Text(
                        text = "Simulate Subscription States:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // FREE State
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    log.d { "Setting debug subscription to FREE" }
                                    (subscriptionRepo as? SimpleSubscriptionRepository)?.setDebugSubscription(
                                        subscriptionType = SubscriptionType.FREE,
                                        productId = "",
                                        entitlements = emptySet()
                                    )
                                } catch (e: Exception) {
                                    log.e(e) { "Failed to set FREE subscription: ${e.message}" }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "🆓 FREE User",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No premium features",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // PREMIUM State
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    log.d { "Setting debug subscription to PREMIUM" }
                                    (subscriptionRepo as? SimpleSubscriptionRepository)?.setDebugSubscription(
                                        subscriptionType = SubscriptionType.PREMIUM,
                                        productId = "debug_monthly",
                                        entitlements = setOf("premium")
                                    )
                                } catch (e: Exception) {
                                    log.e(e) { "Failed to set PREMIUM subscription: ${e.message}" }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "⭐ PREMIUM Subscriber",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All premium features enabled",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // LIFETIME State
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    log.d { "Setting debug subscription to LIFETIME" }
                                    (subscriptionRepo as? SimpleSubscriptionRepository)?.setDebugSubscription(
                                        subscriptionType = SubscriptionType.LIFETIME,
                                        productId = "debug_lifetime",
                                        entitlements = setOf("premium", "lifetime")
                                    )
                                } catch (e: Exception) {
                                    log.e(e) { "Failed to set LIFETIME subscription: ${e.message}" }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "💎 LIFETIME Owner",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "One-time purchase, all features forever",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // LEGACY State
                    Button(
                        onClick = {
                            scope.launch {
                                try {
                                    log.d { "Setting debug subscription to LEGACY" }
                                    (subscriptionRepo as? SimpleSubscriptionRepository)?.setDebugSubscription(
                                        subscriptionType = SubscriptionType.LEGACY,
                                        productId = "2018_founder",
                                        entitlements = setOf("legacy")
                                    )
                                } catch (e: Exception) {
                                    log.e(e) { "Failed to set LEGACY subscription: ${e.message}" }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "🏅 LEGACY Supporter",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "2018 Founder - Basic premium features",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    HorizontalDivider()
                    
                    // Clear Debug State
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    log.d { "Clearing debug state" }
                                    (subscriptionRepo as? SimpleSubscriptionRepository)?.clearDebugState()
                                } catch (e: Exception) {
                                    log.e(e) { "Failed to clear debug state: ${e.message}" }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("🔄 Clear Debug State & Return to Real Subscription")
                    }
                    
                    Text(
                        text = "Note: Clearing debug state will restore your actual subscription status from RevenueCat/BillingManager",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
        
        // Section 2: RevenueCat Integration Testing
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "🔌 RevenueCat Integration Testing",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Test BillingManager integration (platform-agnostic)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    HorizontalDivider()
                    
                    // Test Buttons
                    Button(
                        onClick = { debugViewModel.checkBillingInitialization() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔍 Check Initialization")
                        }
                    }
                    
                    Button(
                        onClick = { debugViewModel.queryBillingProducts() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📦 Query Products")
                        }
                    }
                    
                    Button(
                        onClick = { debugViewModel.checkBillingEntitlements() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔐 Check Entitlements")
                        }
                    }
                    
                    Button(
                        onClick = { debugViewModel.testBillingRestore() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔄 Test Restore Purchases")
                        }
                    }
                    
                    HorizontalDivider()
                    
                    // Tips Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💡 Tips",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Text(
                                text = "• Check Initialization - Verify BillingManager is initialized and connected",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "• Query Products - Fetch available subscription products from store",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "• Check Entitlements - View current user entitlements and access level",
                                style = MaterialTheme.typography.bodySmall
                            )
                            
                            Text(
                                text = "• Test Restore - Restore previous purchases and verify they're recognized",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Warning about RevenueCat initialization
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚠️",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "BillingManager must be initialized via Application/App startup for these tests to work",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

// Preview Composables

@Preview
@Composable
private fun SystemTabPreview() {
    SpaceLaunchNowPreviewTheme {
        SystemTabContent(
            debugViewModel = DebugSettingsViewModel(),
            debugSettings = DebugSettings(
                useCustomApiUrl = false,
                customApiBaseUrl = "https://ll.thespacedevs.com/2.4.0/",
                useDebugTopics = false
            ),
            isLoading = false,
            customUrlText = "https://ll.thespacedevs.com/2.4.0/",
            onCustomUrlTextChange = {}
        )
    }
}

@Preview
@Composable
private fun SystemTabDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        SystemTabContent(
            debugViewModel = DebugSettingsViewModel(),
            debugSettings = DebugSettings(
                useCustomApiUrl = false,
                customApiBaseUrl = "https://ll.thespacedevs.com/2.4.0/",
                useDebugTopics = false
            ),
            isLoading = false,
            customUrlText = "https://ll.thespacedevs.com/2.4.0/",
            onCustomUrlTextChange = {}
        )
    }
}

@Preview
@Composable
private fun NotificationsTabPreview() {
    SpaceLaunchNowPreviewTheme {
        NotificationsTabContent(
            debugViewModel = DebugSettingsViewModel(),
            debugSettings = DebugSettings(
                useCustomApiUrl = false,
                customApiBaseUrl = "https://ll.thespacedevs.com/2.4.0/",
                useDebugTopics = false
            ),
            isLoading = false
        )
    }
}

@Preview
@Composable
private fun NotificationsTabDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        NotificationsTabContent(
            debugViewModel = DebugSettingsViewModel(),
            debugSettings = DebugSettings(
                useCustomApiUrl = false,
                customApiBaseUrl = "https://ll.thespacedevs.com/2.4.0/",
                useDebugTopics = false
            ),
            isLoading = false
        )
    }
}

@Preview
@Composable
private fun BillingTabPreview() {
    SpaceLaunchNowPreviewTheme {
        BillingTabContent(
            debugViewModel = DebugSettingsViewModel(),
            debugSettings = DebugSettings(
                useCustomApiUrl = false,
                customApiBaseUrl = "https://ll.thespacedevs.com/2.4.0/",
                useDebugTopics = false
            ),
            isLoading = false
        )
    }
}

@Preview
@Composable
private fun BillingTabDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        BillingTabContent(
            debugViewModel = DebugSettingsViewModel(),
            debugSettings = DebugSettings(
                useCustomApiUrl = false,
                customApiBaseUrl = "https://ll.thespacedevs.com/2.4.0/",
                useDebugTopics = false
            ),
            isLoading = false
        )
    }
}