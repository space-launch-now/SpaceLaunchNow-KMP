package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.valentinilk.shimmer.shimmer
import me.calebjones.spacelaunchnow.ui.compose.LocalDetailScaffoldCollapsed
import me.calebjones.spacelaunchnow.ui.compose.SharedDetailScaffold

/**
 * State views for LaunchDetailScreen
 *
 * Contains all loading and error state composables:
 * - LaunchDetailErrorView: Displays error message with retry button
 * - LaunchDetailLoadingView: Shows skeleton loading state
 * - ErrorCard: Reusable error card component
 * - LaunchDetailLoadingContent: Skeleton content matching detail layout
 * - LoadingCard: Individual shimmer card component
 */

// Keep only TitleHeight which is used for spacing
private val TitleHeight = 120.dp

/**
 * Error state view for launch details
 *
 * Displays an error message with a back button and retry action.
 * Used when fetching launch data fails.
 */
@Composable
fun LaunchDetailErrorView(
    errorMessage: String,
    onRetry: () -> Unit,
    onNavigateBack: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Back button
        if (onNavigateBack != null) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(16.dp)
                    .size(36.dp)
                    .background(
                        color = Color(0xff121212).copy(alpha = 0.32f),
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }
        }

        // Error content
        ErrorCard(
            errorMessage = errorMessage,
            onRetry = onRetry
        )
    }
}

/**
 * Loading state view for launch details
 *
 * Uses SharedDetailScaffold to match the responsive behavior of the actual detail view.
 * Shows skeleton loading cards that mimic the structure of launch detail content.
 */
@Composable
fun LaunchDetailLoadingView(onNavigateBack: (() -> Unit)? = null) {
    // Use SharedDetailScaffold to match the responsive behavior of the actual detail view
    SharedDetailScaffold(
        titleText = "",
        taglineText = null,
        imageUrl = null, // No image for loading state
        onNavigateBack = onNavigateBack,
        backgroundColors = listOf(
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        scrollEnabled = false,
    ) {
        // Loading content that matches the structure of LaunchDetailContentInBody
        LaunchDetailLoadingContent()
    }
}

/**
 * Reusable error card component
 *
 * Displays error icon, title, message, and retry button.
 * Used within LaunchDetailErrorView.
 */
@Composable
private fun ErrorCard(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Error",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = "Oops! Something went wrong",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}

/**
 * Loading skeleton content matching launch detail layout
 *
 * Creates shimmer cards that approximate the structure and spacing
 * of the actual launch detail content. Includes:
 * - Combined launch overview card
 * - Quick facts grid (2x2)
 * - Ad placeholder
 * - Video player
 * - Timeline
 * - Mission details
 * - Launch vehicle details
 * - Agency details
 */
@Composable
private fun LaunchDetailLoadingContent() {
    val isCollapsed = LocalDetailScaffoldCollapsed.current
    Column(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        if (!isCollapsed) {
            Spacer(Modifier.height(TitleHeight))
        }

        // 1. Combined Launch Overview Card shimmer
        LoadingCard(height = 200.dp)
        Spacer(Modifier.height(16.dp))

        // 2. Quick Stats Grid shimmer
        Text(
            text = "Quick Facts",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(16.dp))

        // Two rows of stat cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
            LoadingCard(modifier = Modifier.weight(1f), height = 96.dp)
        }
        Spacer(Modifier.height(16.dp))

        // Ad placeholder shimmer below Quick Facts
        LoadingCard(height = 100.dp)
        Spacer(Modifier.height(16.dp))

        // 3. Video Player Card shimmer (optional)
        LoadingCard(height = 250.dp)
        Spacer(Modifier.height(16.dp))

        // 4. Timeline Card shimmer
        LoadingCard(height = 180.dp)
        Spacer(Modifier.height(16.dp))

        // 5. Mission Details Card shimmer
        LoadingCard(height = 150.dp)
        Spacer(Modifier.height(16.dp))

        // 6. Launch Vehicle Details Card shimmer
        LoadingCard(height = 200.dp)
        Spacer(Modifier.height(16.dp))

        // 7. Agency Details Card shimmer
        LoadingCard(height = 180.dp)
        Spacer(Modifier.height(16.dp))

        // Bottom spacing
        Spacer(Modifier.height(200.dp))
    }
}

/**
 * Individual shimmer loading card
 *
 * A placeholder card with shimmer effect used to indicate loading state.
 * Height can be customized to match different card types.
 */
@Composable
private fun LoadingCard(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shimmer()
            .height(height),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        )
    }
}
