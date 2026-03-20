package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.hasPlatformNotificationPermission
import me.calebjones.spacelaunchnow.data.repository.openPlatformNotificationSettings
import me.calebjones.spacelaunchnow.data.repository.requestPlatformNotificationPermission
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val spaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
)

/**
 * Onboarding page — notification permission request page.
 *
 * Adapts its UI based on the current permission state:
 * - Already granted: shows success state and auto-advances
 * - Not yet asked: shows "Enable Notifications" button to trigger the system dialog
 * - Denied (dialog can't be shown again): shows "Open Settings" button
 *
 * @param onPermissionResult Called with `true` if permission granted, `false` if denied or skipped.
 */
@Composable
fun NotificationPermissionPage(
    onPermissionResult: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onSkip: () -> Unit = { onPermissionResult(false) }
) {
    val scope = rememberCoroutineScope()
    var hasPermission by remember { mutableStateOf<Boolean?>(null) }
    var hasAttemptedRequest by remember { mutableStateOf(false) }

    // Check permission on first composition
    LaunchedEffect(Unit) {
        hasPermission = hasPlatformNotificationPermission()
    }

    // Re-check permission when returning from system settings
    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    val granted = hasPlatformNotificationPermission()
                    hasPermission = granted
                    // LaunchedEffect(hasPermission) handles the delayed auto-advance
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-advance if permission already granted on initial check
    LaunchedEffect(hasPermission) {
        if (hasPermission == true) {
            delay(3000L)
            onPermissionResult(true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(spaceGradient),
        contentAlignment = Alignment.Center
    ) {
    Column(
        modifier = Modifier
            .widthIn(max = 480.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Icon changes based on permission state
        Icon(
            imageVector = if (hasPermission == true) Icons.Default.NotificationsActive else Icons.Default.Notifications,
            contentDescription = "Notifications",
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = if (hasPermission == true) "You're All Set!" else "Stay in the Loop",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = if (hasPermission == true)
                "Notifications are enabled. You'll be notified before launches so you never miss a liftoff."
            else
                "Get notified before launches so you never miss a liftoff. You can customize which notifications you receive.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        when {
            // Permission already granted — no button needed (auto-advancing)
            hasPermission == true -> {
                // Nothing to show, LaunchedEffect will auto-advance
            }

            // User already tried the dialog and was denied — send to settings
            hasAttemptedRequest -> {
                Button(
                    onClick = { openPlatformNotificationSettings() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0A0E2A)
                    )
                ) {
                    Text(
                        text = "Open Settings",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Maybe Later button
                TextButton(onClick = { onSkip() }) {
                    Text(
                        text = "Maybe Later",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // First attempt — show the permission request button
            else -> {
                Button(
                    onClick = {
                        scope.launch {
                            val granted = requestPlatformNotificationPermission()
                            hasPermission = granted
                            if (granted) {
                                onPermissionResult(granted)
                            } else {
                                hasAttemptedRequest = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0A0E2A)
                    )
                ) {
                    Text(
                        text = "Enable Notifications",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Maybe Later button
                TextButton(onClick = { onSkip() }) {
                    Text(
                        text = "Maybe Later",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun NotificationPermissionPagePreview() {
    SpaceLaunchNowPreviewTheme {
        NotificationPermissionPage(onPermissionResult = {})
    }
}

@Preview
@Composable
private fun NotificationPermissionPageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        NotificationPermissionPage(onPermissionResult = {})
    }
}
