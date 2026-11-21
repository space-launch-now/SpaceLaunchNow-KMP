package me.calebjones.spacelaunchnow.ui.settings

import me.calebjones.spacelaunchnow.analytics.events.PaywallSource
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
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.navigation.SupportUs
import me.calebjones.spacelaunchnow.ui.subscription.PremiumBadge
import me.calebjones.spacelaunchnow.ui.subscription.PremiumPromptCard
import me.calebjones.spacelaunchnow.ui.viewmodel.SettingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher_monochrome
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.common_back
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_copy_url
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_feed_events_description
import spacelaunchnow_kmp.composeapp.generated.resources.common_events
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_feed_launches_description
import spacelaunchnow_kmp.composeapp.generated.resources.common_launches
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_feeds_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_how_body
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_how_note
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_how_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_done
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_intro
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_step_add
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_step_copy
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_step_from_url
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_step_open
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_step_paste
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_step_repeat
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_setup_title
import spacelaunchnow_kmp.composeapp.generated.resources.common_calendar_sync
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_unlock_body
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_unlock_title
import spacelaunchnow_kmp.composeapp.generated.resources.settings_calendar_url_copied
import spacelaunchnow_kmp.composeapp.generated.resources.common_premium_feature

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
                        text = stringResource(Res.string.common_calendar_sync),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(Res.string.common_back)
                        )
                    }
                },
                actions = {
                    if (!uiState.hasCalendarSync) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = stringResource(Res.string.common_premium_feature),
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
                    title = stringResource(Res.string.settings_calendar_unlock_title),
                    description = stringResource(Res.string.settings_calendar_unlock_body),
                    icon = Icons.Default.Star,
                    onUpgradeClick = {
                        navController.navigate(SupportUs(PaywallSource.CALENDAR_SYNC.value))
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
                        snackbarHostState.showSnackbar(getString(Res.string.settings_calendar_url_copied))
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
                    text = stringResource(Res.string.settings_calendar_how_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = stringResource(Res.string.settings_calendar_how_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(Res.string.settings_calendar_how_note),
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
                    text = stringResource(Res.string.settings_calendar_feeds_title),
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
            title = stringResource(Res.string.common_launches),
            description = stringResource(Res.string.settings_calendar_feed_launches_description),
            url = "https://spacelaunchnow.app/launches/latest/feed.ics",
            icon = null,
            showUrl = hasPerms,
            onCopyUrl = onCopyUrl
        )

        CalendarUrlCard(
            title = stringResource(Res.string.common_events),
            description = stringResource(Res.string.settings_calendar_feed_events_description),
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
                        contentDescription = stringResource(Res.string.settings_calendar_copy_url),
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
                    text = stringResource(Res.string.settings_calendar_setup_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.settings_calendar_setup_intro),
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
                    SetupStep(number = "1", text = stringResource(Res.string.settings_calendar_setup_step_copy))
                    SetupStep(number = "2", text = stringResource(Res.string.settings_calendar_setup_step_open))
                    SetupStep(number = "3", text = stringResource(Res.string.settings_calendar_setup_step_add))
                    SetupStep(number = "4", text = stringResource(Res.string.settings_calendar_setup_step_from_url))
                    SetupStep(
                        number = "5",
                        text = stringResource(Res.string.settings_calendar_setup_step_paste)
                    )
                    SetupStep(number = "6", text = stringResource(Res.string.settings_calendar_setup_step_repeat))
                }

                Text(
                    text = stringResource(Res.string.settings_calendar_setup_done),
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