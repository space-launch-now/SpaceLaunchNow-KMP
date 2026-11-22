package me.calebjones.spacelaunchnow.ui.components

import me.calebjones.spacelaunchnow.ui.ads.AdCoveringAlertDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.Discord
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_feedback_button_discord
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_feedback_button_email
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_feedback_button_github
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_feedback_message
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_feedback_title
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_rating_button_later
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_rating_button_no
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_rating_button_yes
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_rating_message
import spacelaunchnow_kmp.composeapp.generated.resources.app_components_rating_title
import spacelaunchnow_kmp.composeapp.generated.resources.common_maybe_later
import org.jetbrains.compose.resources.stringResource

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
    AdCoveringAlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(Res.string.app_components_rating_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(Res.string.app_components_rating_message),
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
                    Text(stringResource(Res.string.app_components_rating_button_no))
                }
                
                // "Yes, I love it!" - Filled button for primary positive action
                Button(
                    onClick = onYesEnjoyingApp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.app_components_rating_button_yes))
                }
            }
        },
        dismissButton = {
            // "Ask me later" - Text button for dismiss action
            TextButton(
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.app_components_rating_button_later))
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
    AdCoveringAlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        title = {
            Text(
                text = stringResource(Res.string.app_components_feedback_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                Text(
                    text = stringResource(Res.string.app_components_feedback_message),
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
                        Text(stringResource(Res.string.app_components_feedback_button_github))
                    }
                    
                    // "Send Email" - Filled button for primary action
                    Button(
                        onClick = onSendEmail,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(Res.string.app_components_feedback_button_email))
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
                    Text(stringResource(Res.string.app_components_feedback_button_discord))
                }
                
                Spacer(Modifier.height(8.dp))
                
                // Dismiss button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(Res.string.common_maybe_later))
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
}
