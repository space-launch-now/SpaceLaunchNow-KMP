package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Discord

/**
 * Dialog that asks if user is enjoying the app before showing native review.
 * 
 * Flow:
 * - "Are you enjoying SpaceLaunchNow?"
 * - Yes -> Trigger native in-app review
 * - No -> Offer feedback options (email/GitHub)
 * - Not Now -> Dismiss and ask later
 */
@Composable
fun AppRatingDialog(
    onYesEnjoyingApp: () -> Unit,
    onNoNotEnjoying: () -> Unit,
    onNotNow: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = "Enjoying Space Launch Now?",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = "I'd love to hear your feedback! Are you enjoying using the app?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // "Not really" - Outlined button for secondary negative action
                OutlinedButton(
                    onClick = onNoNotEnjoying,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Not really")
                }
                
                // "Yes, I love it!" - Filled button for primary positive action
                Button(
                    onClick = onYesEnjoyingApp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yes, I love it!")
                }
            }
        },
        dismissButton = {
            // "Ask me later" - Text button for dismiss action
            TextButton(
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ask me later")
            }
        }
    )
}

/**
 * Dialog shown when user indicates they're not enjoying the app.
 * Offers feedback channels instead of pushing for a review.
 */
@Composable
fun FeedbackDialog(
    onSendEmail: () -> Unit,
    onOpenGitHub: () -> Unit,
    onOpenDiscord: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = "I'd Love Your Feedback",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = "Your feedback helps me improve! How would you like to share your thoughts?",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(Modifier.height(16.dp))
                
                // First row: GitHub and Email
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // "Open GitHub Issue" - Outlined button for secondary action
                    OutlinedButton(
                        onClick = onOpenGitHub,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("GitHub Issue")
                    }
                    
                    // "Send Email" - Filled button for primary action
                    Button(
                        onClick = onSendEmail,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Send Email")
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Second row: Discord
                OutlinedButton(
                    onClick = onOpenDiscord,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = FontAwesomeIcons.Brands.Discord,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Join Discord Community")
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Dismiss button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Maybe Later")
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
