package me.calebjones.spacelaunchnow.ui.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimePeriod
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.storage.TemporaryAccessStatus
import me.calebjones.spacelaunchnow.data.storage.TemporaryPremiumAccess
import me.calebjones.spacelaunchnow.ui.ads.RewardedAdHandler
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger

/**
 * Card component for temporary premium access via rewarded ads.
 * Shows current temporary access status and provides option to watch ad for 24h premium access.
 */
@Composable
fun TemporaryPremiumCard(
    temporaryPremiumAccess: TemporaryPremiumAccess,
    features: List<PremiumFeature> = listOf(
        PremiumFeature.CUSTOM_THEMES,
        PremiumFeature.ADVANCED_WIDGETS,
        PremiumFeature.WIDGETS_CUSTOMIZATION
    ),
    title: String = "24h Premium Access",
    description: String = "Watch an ad to unlock all premium features for 24 hours",
    icon: ImageVector,
    hasPermanentPremium: Boolean = false,  // New parameter to check permanent premium status
    modifier: Modifier = Modifier
) {
    var temporaryAccess by remember {
        mutableStateOf<Map<PremiumFeature, TemporaryAccessStatus?>>(
            emptyMap()
        )
    }
    var showRewardedAd by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val log = SpaceLogger.getLogger("TemporaryPremiumCard")

    // Check temporary access status for all features
    LaunchedEffect(features) {
        val accessMap = features.associateWith { feature ->
            temporaryPremiumAccess.getTemporaryAccessInfo(feature)
        }
        temporaryAccess = accessMap
    }

    // Update every minute to refresh time remaining
    LaunchedEffect(temporaryAccess) {
        if (temporaryAccess.values.any { it != null }) {
            while (true) {
                delay(60_000) // Update every minute
                val accessMap = features.associateWith { feature ->
                    temporaryPremiumAccess.getTemporaryAccessInfo(feature)
                }
                temporaryAccess = accessMap
            }
        }
    }

    // Check if user has any temporary access
    val hasAnyAccess = temporaryAccess.values.any { it != null }
    val earliestExpiration = temporaryAccess.values.filterNotNull().minByOrNull { accessInfo ->
        accessInfo.expiresAt ?: kotlinx.datetime.Instant.DISTANT_FUTURE
    }

    // Don't show the card if user has permanent premium and no temporary access
    if (hasPermanentPremium && !hasAnyAccess) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),  // Add external padding to match PremiumPromptCard
        colors = CardDefaults.cardColors(
            containerColor = if (hasAnyAccess)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 10.dp,
            hoveredElevation = 12.dp,
            focusedElevation = 16.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (hasAnyAccess)
                        MaterialTheme.colorScheme.onTertiaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasAnyAccess)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (hasAnyAccess)
                            MaterialTheme.colorScheme.onTertiaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Show current status and actions
            if (hasAnyAccess) {
                // User has temporary access - show time remaining
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Time remaining: ${formatTimeRemaining(earliestExpiration?.timeRemaining)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showRewardedAd = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Extend All Access (+24h)",
                        color = MaterialTheme.colorScheme.onTertiaryFixedVariant
                    )
                }
            } else {
                // User doesn't have temporary access - show option to get it
                Button(
                    onClick = { showRewardedAd = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Watch Ad for 24h Premium Access")
                }
            }
        }
    }

    // Handle rewarded ad
    if (showRewardedAd) {
        RewardedAdHandler(
            shouldShow = true,
            onRewardEarned = { rewardAmount, rewardType ->
                // Grant temporary access to all features when ad is completed
                coroutineScope.launch {
                    features.forEach { feature ->
                        temporaryPremiumAccess.grantTemporaryAccess(feature)
                    }
                    // Refresh the temporary access info
                    val accessMap = features.associateWith { feature ->
                        temporaryPremiumAccess.getTemporaryAccessInfo(feature)
                    }
                    temporaryAccess = accessMap
                }
                showRewardedAd = false
            },
            onAdShown = {
                // Ad started showing successfully
                log.i { "✅ Rewarded ad shown for temporary premium access" }
            },
            onAdFailed = { error ->
                // Ad failed to show
                log.e { "❌ Failed to show rewarded ad: $error" }
                showRewardedAd = false
            }
        )
    }
}

/**
 * Formats time remaining in a user-friendly format.
 * Examples: "23h 45m", "2h 15m", "45m", "5m"
 */
private fun formatTimeRemaining(period: DateTimePeriod?): String {
    if (period == null) return "0m"

    val hours = period.hours
    val minutes = period.minutes

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "Less than 1m"
    }
}