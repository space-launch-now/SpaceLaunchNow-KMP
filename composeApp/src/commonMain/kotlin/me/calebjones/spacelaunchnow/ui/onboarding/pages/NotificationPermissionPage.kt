package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.requestPlatformNotificationPermission
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

private val spaceGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0A0E2A), Color(0xFF1A1040), Color(0xFF2A1060))
)

/**
 * Onboarding page 4 — notification permission request page.
 *
 * Unlike other pages, this one does **not** use a device frame. Instead it shows
 * a notification icon, explanatory text, a prominent "Enable Notifications" button,
 * and a "Maybe Later" skip option.
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(spaceGradient)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        // Notification icon
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notifications",
            modifier = Modifier.size(80.dp),
            tint = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "Stay in the Loop",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Subtitle
        Text(
            text = "Get notified before launches so you never miss a liftoff. You can customize which notifications you receive.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Enable Notifications button
        Button(
            onClick = {
                scope.launch {
                    val granted = requestPlatformNotificationPermission()
                    onPermissionResult(granted)
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
        TextButton(
            onClick = { onSkip() }
        ) {
            Text(
                text = "Maybe Later",
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.weight(1f))
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
