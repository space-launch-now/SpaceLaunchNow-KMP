package me.calebjones.spacelaunchnow.ui.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.PremiumFeature
import me.calebjones.spacelaunchnow.data.repository.SubscriptionRepository
import org.koin.compose.koinInject

/**
 * Wrapper composable that gates content behind a premium feature
 *
 * Usage:
 * ```kotlin
 * PremiumFeatureGate(feature = PremiumFeature.ADVANCED_FILTERS) {
 *     // This content only shows if user has premium subscription
 *     AdvancedFiltersUI()
 * }
 * ```
 *
 * @param feature The premium feature required
 * @param verifyOnAccess If true, verifies with platform before showing content
 * @param paywallContent Custom paywall content (optional)
 * @param content The premium content to show when user has access
 */
@Composable
fun PremiumFeatureGate(
    feature: PremiumFeature,
    verifyOnAccess: Boolean = false,
    modifier: Modifier = Modifier,
    paywallContent: @Composable (PremiumFeature, () -> Unit) -> Unit = { feat, onUpgrade ->
        DefaultPaywallCard(feature = feat, onUpgrade = onUpgrade)
    },
    content: @Composable () -> Unit
) {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsState()
    val scope = rememberCoroutineScope()

    var hasAccessVerified by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }

    // Use cached state for instant feedback
    val hasCachedAccess = subscriptionState.hasFeature(feature)

    Box(modifier = modifier) {
        when {
            // User has cached access
            hasCachedAccess && !verifyOnAccess -> {
                content()
            }

            // Verifying access
            isVerifying -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            // Access verified
            hasAccessVerified -> {
                content()
            }

            // Show paywall
            else -> {
                paywallContent(feature) {
                    // On upgrade requested
                    scope.launch {
                        if (verifyOnAccess) {
                            isVerifying = true
                            val result = subscriptionRepo.verifySubscription(forceRefresh = true)
                            isVerifying = false

                            if (result.isSuccess && result.getOrNull()
                                    ?.hasFeature(feature) == true
                            ) {
                                hasAccessVerified = true
                            }
                        }
                    }
                }
            }
        }
    }

    // Verify on first composition if requested
    LaunchedEffect(feature, verifyOnAccess) {
        if (verifyOnAccess && hasCachedAccess) {
            isVerifying = true
            val result = subscriptionRepo.verifySubscription(forceRefresh = true)
            isVerifying = false

            hasAccessVerified = result.isSuccess && result.getOrNull()?.hasFeature(feature) == true
        }
    }
}

/**
 * Default paywall card shown when user doesn't have access
 */
@Composable
private fun DefaultPaywallCard(
    feature: PremiumFeature,
    onUpgrade: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.Lock,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Premium Feature",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = feature.getDescription(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onUpgrade,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Upgrade to Premium")
            }
        }
    }
}

@Composable
fun PremiumPromptCard(
    title: String,
    description: String,
    icon: ImageVector,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = modifier.padding(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 10.dp,
            hoveredElevation = 12.dp,
            focusedElevation = 16.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
        Button(
            onClick = onUpgradeClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Upgrade to Premium")
        }
    }
}

/**
 * Button that shows premium badge and handles verification
 *
 * Usage:
 * ```kotlin
 * PremiumFeatureButton(
 *     feature = PremiumFeature.ADVANCED_FILTERS,
 *     onClick = { showAdvancedFilters() }
 * ) {
 *     Text("Advanced Filters")
 * }
 * ```
 */
@Composable
fun PremiumFeatureButton(
    feature: PremiumFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    verifyBeforeAccess: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsState()
    val scope = rememberCoroutineScope()

    var isVerifying by remember { mutableStateOf(false) }
    val hasFeature = subscriptionState.hasFeature(feature)

    Button(
        onClick = {
            if (hasFeature) {
                if (verifyBeforeAccess) {
                    scope.launch {
                        isVerifying = true
                        val result = subscriptionRepo.verifySubscription(forceRefresh = true)
                        isVerifying = false

                        if (result.isSuccess && result.getOrNull()?.hasFeature(feature) == true) {
                            onClick()
                        } else {
                            // Show error or paywall
                        }
                    }
                } else {
                    onClick()
                }
            } else {
                // Show paywall or subscription screen
            }
        },
        modifier = modifier,
        enabled = enabled && !isVerifying
    ) {
        if (isVerifying) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(8.dp))
        }

        if (!hasFeature) {
            Icon(
                Icons.Default.Lock,
                contentDescription = "Premium",
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(4.dp))
        }

        content()
    }
}

/**
 * Badge to show premium status
 */
@Composable
fun PremiumBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = Modifier.padding(16.dp).then(modifier),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Premium",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Helper to check if user has premium subscription
 */
@Composable
fun rememberIsPremium(): State<Boolean> {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsState()
    return remember(subscriptionState) {
        derivedStateOf {
            subscriptionState.isSubscribed
        }
    }
}

/**
 * Helper to check if user has a specific feature
 */
@Composable
fun rememberHasFeature(feature: PremiumFeature): State<Boolean> {
    val subscriptionRepo = koinInject<SubscriptionRepository>()
    val subscriptionState by subscriptionRepo.state.collectAsState()
    return remember(subscriptionState, feature) {
        derivedStateOf {
            subscriptionState.hasFeature(feature)
        }
    }
}

// Extension: Get user-friendly description for features
fun PremiumFeature.getDescription(): String {
    return when (this) {
        PremiumFeature.AD_FREE -> "Enjoy an ad-free experience throughout the app"
        PremiumFeature.CUSTOM_THEMES -> "Personalize your app with custom color themes"
        PremiumFeature.ADVANCED_WIDGETS -> "Access the launch list widget"
        PremiumFeature.WIDGETS_CUSTOMIZATION -> "Customize your widgets look and feel"
        PremiumFeature.CAL_SYNC -> "Access to Calendar Sync Link for syncing launches and events"
        PremiumFeature.NOTIFICATION_CUSTOMIZATION -> "Customize your notification preferences"
    }
}

// Extension: Get user-friendly description for features
fun PremiumFeature.getTitle(): String {
    return when (this) {
        PremiumFeature.AD_FREE -> "Ad-Free Experience"
        PremiumFeature.CUSTOM_THEMES -> "Custom Themes"
        PremiumFeature.ADVANCED_WIDGETS -> "Advanced Widget"
        PremiumFeature.WIDGETS_CUSTOMIZATION -> "Widget Customization"
        PremiumFeature.CAL_SYNC -> "Calendar Sync"
        PremiumFeature.NOTIFICATION_CUSTOMIZATION -> "Notification Customization"
    }
}
