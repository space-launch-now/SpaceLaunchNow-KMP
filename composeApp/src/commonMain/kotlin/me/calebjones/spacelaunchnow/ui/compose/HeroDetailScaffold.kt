package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer

private val HeroImageHeight = 340.dp
private val HzPadding = 20.dp

/**
 * Detail scaffold with a full-bleed hero image header.
 *
 * Alternative to [SharedDetailScaffold] for screens where the image is a wide
 * photograph rather than a logo/portrait: the image spans edge to edge under
 * the status bar, fades into the surface at its bottom edge, and the title
 * block sits below it. The image scrolls away with the content (with a slight
 * parallax) while the back button stays pinned.
 *
 * Content does NOT need a top spacer — [LocalDetailScaffoldCollapsed] is
 * provided as true so shared content composables skip their header offset.
 *
 * @param titleText Main title, wraps up to three lines
 * @param taglineText Optional subtitle shown under the title
 * @param imageUrl Hero image URL; shimmer placeholder while loading
 * @param logoUrl Optional agency logo shown to the left of the title block
 * @param onNavigateBack Callback for the pinned back button; hidden if null
 * @param scrollEnabled Whether scrolling is enabled (default: true)
 * @param content The scrollable content rendered below the title block
 */
@Composable
fun HeroDetailScaffold(
    titleText: String,
    taglineText: String?,
    imageUrl: String?,
    logoUrl: String? = null,
    onNavigateBack: (() -> Unit)? = null,
    scrollEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    val scroll = rememberScrollState(0)

    Box(
        Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .let { if (scrollEnabled) it.verticalScroll(scroll) else it }
        ) {
            HeroImage(imageUrl = imageUrl, scrollValueProvider = { scroll.value })

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = HzPadding, vertical = 16.dp)
            ) {
                if (!logoUrl.isNullOrBlank()) {
                    AgencyLogoBadge(logoUrl = logoUrl, size = 56.dp)
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(
                        text = titleText,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (!taglineText.isNullOrBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = taglineText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }

            CompositionLocalProvider(LocalDetailScaffoldCollapsed provides true) {
                content()
            }
        }

        if (onNavigateBack != null) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        color = Color(0xff121212).copy(alpha = 0.32f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroImage(
    imageUrl: String?,
    scrollValueProvider: () -> Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(HeroImageHeight)
            // Keep the parallax-shifted image from bleeding over the title block
            .clipToBounds()
    ) {
        if (imageUrl.isNullOrBlank()) {
            HeroShimmerPlaceholder()
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Parallax: image scrolls at half speed relative to content
                        translationY = scrollValueProvider() * 0.5f
                    },
                loading = { HeroShimmerPlaceholder() },
                error = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "Image placeholder",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
        }

        // Soft scrim so the image blends into the surface below
        HeroBottomScrim()
    }
}

@Composable
private fun HeroShimmerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            )
            .shimmer(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Image,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f)
        )
    }
}

@Composable
private fun BoxScope.HeroBottomScrim() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .align(Alignment.BottomCenter)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            )
    )
}
