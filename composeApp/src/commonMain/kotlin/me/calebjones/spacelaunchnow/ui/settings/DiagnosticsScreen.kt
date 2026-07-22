package me.calebjones.spacelaunchnow.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.analytics.DatadogRuntime
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.DiagnosticLevel
import me.calebjones.spacelaunchnow.util.logging.DiagnosticSettings
import me.calebjones.spacelaunchnow.util.logging.DiagnosticsLog
import me.calebjones.spacelaunchnow.util.logging.LoggingPreferences
import me.calebjones.spacelaunchnow.util.logging.platformNotificationDiagnostics
import me.calebjones.spacelaunchnow.util.logging.recentNseBreadcrumbs
import me.calebjones.spacelaunchnow.util.logging.shareCopiesToClipboard
import me.calebjones.spacelaunchnow.util.sharePlainText
import org.koin.compose.koinInject

/**
 * Ground-truth diagnostics: live filter state, NSE App Group state (iOS),
 * recent delivery decisions, logging status, and log export.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticsScreen(
    onNavigateBack: () -> Unit,
) {
    val loggingPreferences: LoggingPreferences = koinInject()
    val notificationStateStorage: NotificationStateStorage = koinInject()
    val scope = rememberCoroutineScope()

    val diagnosticSettings by loggingPreferences.getDiagnosticSettings()
        .collectAsState(initial = DiagnosticSettings(DiagnosticLevel.OFF, null))
    val level = diagnosticSettings.level
    val liveState by produceState<NotificationState?>(initialValue = null) {
        value = notificationStateStorage.getState()
    }
    val platformRows = remember { platformNotificationDiagnostics() }
    val breadcrumbs = remember { recentNseBreadcrumbs() }
    val copyNotShare = remember { shareCopiesToClipboard() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostics") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
        ) {
            item {
                DiagnosticsCard("App") {
                    DiagRow("Version", "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                    DiagRow("Debug build", BuildConfig.IS_DEBUG.toString())
                }
            }
            item {
                DiagnosticsCard("Logging") {
                    DiagRow("Diagnostic level", level.name)
                    DiagRow("Datadog SDK initialized", DatadogRuntime.isSdkInitialized().toString())
                    DiagRow("Remote upload active", DatadogRuntime.isRemoteLoggingActive().toString())
                    diagnosticSettings.verboseExpiresAtEpochSeconds?.let { expiry ->
                        DiagRow("Verbose expires (epoch s)", expiry.toString())
                    }
                }
            }
            item {
                DiagnosticsCard("Live notification filters (in-app)") {
                    val s = liveState
                    if (s == null) {
                        DiagRow("State", "loading…")
                    } else {
                        DiagRow("Notifications enabled", s.enableNotifications.toString())
                        DiagRow("Follow all launches", s.followAllLaunches.toString())
                        DiagRow("Strict matching", s.useStrictMatching.toString())
                        DiagRow("Agencies", "${s.subscribedAgencies.size}: ${s.subscribedAgencies.take(12).joinToString(",")}")
                        DiagRow("Locations", "${s.subscribedLocations.size}: ${s.subscribedLocations.take(12).joinToString(",")}")
                    }
                }
            }
            if (platformRows.isNotEmpty()) {
                item {
                    DiagnosticsCard("Push & notification state") {
                        platformRows.forEach { (label, value) -> DiagRow(label, value) }
                    }
                }
            }
            if (breadcrumbs.isNotEmpty()) {
                item {
                    DiagnosticsCard("Recent NSE delivery decisions") {
                        breadcrumbs.reversed().forEach { crumb ->
                            Text(
                                "${crumb.timestampEpochSeconds} ${crumb.type} → ${crumb.decision} (${crumb.reason})",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        scope.launch {
                            val header = buildString {
                                appendLine("SpaceLaunchNow diagnostic logs")
                                appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                                appendLine("Diagnostic level: ${level.name}")
                                appendLine("----")
                            }
                            sharePlainText(header + DiagnosticsLog.export(), "SpaceLaunchNow Diagnostic Logs")
                        }
                    }
                ) { Text(if (copyNotShare) "Copy logs" else "Share logs") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val report = buildString {
                            appendLine("SpaceLaunchNow diagnostics report")
                            appendLine("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                            appendLine("Diagnostic level: ${level.name}")
                            appendLine("Datadog initialized: ${DatadogRuntime.isSdkInitialized()}, uploading: ${DatadogRuntime.isRemoteLoggingActive()}")
                            liveState?.let { s ->
                                appendLine("Live: enabled=${s.enableNotifications} followAll=${s.followAllLaunches} strict=${s.useStrictMatching} agencies=${s.subscribedAgencies.size} locations=${s.subscribedLocations.size}")
                                appendLine("Live agencies: ${s.subscribedAgencies.joinToString(",")}")
                                appendLine("Live locations: ${s.subscribedLocations.joinToString(",")}")
                            } ?: appendLine("Live: (still loading)")
                            platformRows.forEach { (l, v) -> appendLine("$l: $v") }
                            breadcrumbs.forEach { c ->
                                appendLine("NSE ${c.timestampEpochSeconds} ${c.type} ${c.decision} ${c.reason}")
                            }
                        }
                        sharePlainText(report, "SpaceLaunchNow Diagnostics Report")
                    }
                ) { Text(if (copyNotShare) "Copy diagnostics report" else "Share diagnostics report") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun DiagnosticsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
private fun DiagRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth(0.45f)
        )
        Text(value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
    }
}
