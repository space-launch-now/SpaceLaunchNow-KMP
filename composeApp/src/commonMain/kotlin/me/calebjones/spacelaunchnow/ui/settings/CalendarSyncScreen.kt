package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.launcher_monochrome
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.subscription.PremiumBadge
import me.calebjones.spacelaunchnow.ui.subscription.PremiumPromptCard
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarSyncScreen(
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Calendar Sync",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (!uiState.hasCalendarSync) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Premium Feature",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Prompt or Premium Features
            if (!uiState.hasCalendarSync) {
                PremiumPromptCard(
                    modifier = Modifier.fillMaxWidth(),
                    title = "Unlock Calendar Sync",
                    description = "Upgrade to Premium to sync launches and events to your calendar.",
                    icon = Icons.Default.Star,
                    onUpgradeClick = {
                        navController.navigate(SupportUs)
                    }
                )
            }

            // Instructions section
            InstructionsCard()

            CalendarUrlsSection(
                uiState.hasCalendarSync,
                onCopyUrl = { url ->
                    clipboardManager.setText(AnnotatedString(url))
                    scope.launch {
                        snackbarHostState.showSnackbar("URL copied to clipboard!")
                    }
                }
            )

            // Setup instructions for Google Calendar
            GoogleCalendarSetupCard()

            Spacer(Modifier.height(64.dp))
        }
    }
}


@Composable
private fun InstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "How it works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Use the Calendar ICS URLs below to subscribe to Space Launch Now events and launches in your calendar app. Your calendar will automatically update with the latest information.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "These links will work on any calendar that supports ICS URL feeds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CalendarUrlsSection(
    hasPerms: Boolean,
    onCopyUrl: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Calendar Feed URLs",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Spacer(Modifier.width(4.dp))
                if (!hasPerms) {
                    PremiumBadge()
                }
            }
            // You can add a subtitle Text component here if needed (currently not present in source)
        }

        CalendarUrlCard(
            title = "Launches",
            description = "Subscribe to upcoming space launches",
            url = "https://spacelaunchnow.app/launches/latest/feed.ics",
            icon = null,
            showUrl = hasPerms,
            onCopyUrl = onCopyUrl
        )

        CalendarUrlCard(
            title = "Events",
            description = "Subscribe to space-related events",
            url = "https://spacelaunchnow.app/events/latest/feed.ics",
            icon = Icons.Default.Event,
            showUrl = hasPerms,
            onCopyUrl = onCopyUrl
        )
    }
}

@Composable
private fun CalendarUrlCard(
    title: String,
    description: String,
    url: String,
    showUrl: Boolean = false,
    icon: Any?,
    onCopyUrl: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon == null) {
                Image(
                    painter = painterResource(Res.drawable.launcher_monochrome),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
            } else {
                Icon(
                    imageVector = icon as ImageVector,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SelectionContainer {
                    Text(
                        text = if (showUrl) url else "* * * * * * * * * * * * *",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                IconButton(
                    onClick = { if (showUrl) onCopyUrl(url) },
                    enabled = showUrl
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy URL",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GoogleCalendarSetupCard() {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Google Calendar Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "To add these feeds to Google Calendar:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SetupStep(number = "1", text = "Copy the calendar feed URL above")
                    SetupStep(number = "2", text = "Open Google Calendar on your computer")
                    SetupStep(number = "3", text = "Click the \"+\" next to \"Other calendars\"")
                    SetupStep(number = "4", text = "Select \"From URL\"")
                    SetupStep(
                        number = "5",
                        text = "Paste the copied URL and click \"Add calendar\""
                    )
                    SetupStep(number = "6", text = "Repeat for both Launches and Events feeds")
                }

                Text(
                    text = "Your calendar will automatically update with new launches and events!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SetupStep(
    number: String,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Card(
            modifier = Modifier.size(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxSize().padding(vertical = 4.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}