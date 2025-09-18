package me.calebjones.spacelaunchnow.ui.detail.compose

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.ui.SharedElementType
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Shared Transition Layout Constants (scoped to this file)
private val TitleHeight = 128.dp
private val GradientScroll = 180.dp
private val ImageOverlap = 115.dp
private val MinTitleOffset = 64.dp
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 100.dp
private val HzPadding = 24.dp

// A reusable spring used for bounds transform
@OptIn(ExperimentalSharedTransitionApi::class)
val sharedDetailBoundsTransform = BoundsTransform { _, _ ->
    spring(dampingRatio = 0.8f, stiffness = 380f)
}

// Provider for shared element keys; caller can return any key object (e.g., SharedElementKey or EventSharedElementKey)
typealias SharedKeyProvider = (SharedElementType) -> Any

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedDetailScaffold(
    keyProvider: SharedKeyProvider,
    titleText: String,
    taglineText: String?,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    backgroundColors: List<Color>? = null,
    content: @Composable () -> Unit,
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No Scope found")

    val roundedCornerAnim by animatedVisibilityScope.transition
        .animateDp(label = "rounded corner") { enterExit: EnterExitState ->
            when (enterExit) {
                EnterExitState.PreEnter -> 20.dp
                EnterExitState.Visible -> 0.dp
                EnterExitState.PostExit -> 20.dp
            }
        }

    val bgColors = backgroundColors ?: listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.surfaceVariant
    )

    with(sharedTransitionScope) {
        Box(
            Modifier
                .clip(RoundedCornerShape(roundedCornerAnim))
                .sharedBounds(
                    rememberSharedContentState(key = keyProvider(SharedElementType.Bounds)),
                    animatedVisibilityScope,
                    clipInOverlayDuringTransition =
                        OverlayClip(RoundedCornerShape(roundedCornerAnim)),
                    boundsTransform = sharedDetailBoundsTransform,
                )
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            SharedDetailBackground(keyProvider, bgColors)

            val scroll = rememberScrollState(0)
            SharedDetailBody(scroll, content)
            SharedDetailTitle(titleText, taglineText, keyProvider, scroll)
            SharedDetailImage(imageUrl, keyProvider, scroll)
            SharedDetailUp(onNavigateBack)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailBackground(keyProvider: SharedKeyProvider, colors: List<Color>) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No Scope found")

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val targetOffset = with(LocalDensity.current) { 5000.dp.toPx() }
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = targetOffset,
        animationSpec = infiniteRepeatable(
            tween(300000, easing = LinearEasing),
        ),
        label = "offset",
    )

    with(sharedTransitionScope) {
        Spacer(
            modifier = Modifier
                .sharedBounds(
                    rememberSharedContentState(key = keyProvider(SharedElementType.Background)),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = sharedDetailBoundsTransform,
                )
                .height(280.dp)
                .fillMaxWidth()
                .blur(40.dp)
                .drawWithCache {
                    val brushSize = 1200f
                    val brush = Brush.linearGradient(
                        colors = colors,
                        start = Offset(offset, offset),
                        end = Offset(offset + brushSize, offset + brushSize),
                        tileMode = TileMode.Mirror,
                    )
                    onDrawBehind { drawRect(brush) }
                },
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailBody(scroll: ScrollState, content: @Composable () -> Unit) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No scope found")
    with(sharedTransitionScope) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(MinTitleOffset),
            )

            Column(
                modifier = Modifier.verticalScroll(scroll),
            ) {
                Spacer(Modifier.height(GradientScroll))
                Spacer(Modifier.height(ImageOverlap))
                Spacer(Modifier.height(MinTitleOffset + 20.dp))
                Surface(
                    Modifier
                        .fillMaxWidth()
                ) {
                    content()
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailTitle(
    title: String,
    tagline: String?,
    keyProvider: SharedKeyProvider,
    scroll: ScrollState
) {
    val maxOffset = with(LocalDensity.current) { MaxTitleOffset.toPx() }
    val minOffset = with(LocalDensity.current) { MinTitleOffset.toPx() }
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalArgumentException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")

    // Calculate collapse fraction to determine image size and position
    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFraction = (scroll.value / collapseRange).coerceIn(0f, 1f)

    // Calculate current image size and position
    val imageMaxSize = with(LocalDensity.current) { ExpandedImageSize.toPx() }
    val imageMinSize = with(LocalDensity.current) { CollapsedImageSize.toPx() }
    val currentImageSize = lerp(imageMaxSize, imageMinSize, collapseFraction)

    // Calculate horizontal padding needed to avoid overlap with collapsed image
    val horizontalPaddingPx = with(LocalDensity.current) { HzPadding.toPx() }
    val rightPaddingPx = if (collapseFraction > 0.5f) {
        horizontalPaddingPx + currentImageSize + with(LocalDensity.current) { 16.dp.toPx() }
    } else {
        horizontalPaddingPx
    }

    with(sharedTransitionScope) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TitleHeight)
                .statusBarsPadding()
                .offset {
                    val offset = (maxOffset - scroll.value).coerceAtLeast(minOffset)
                    IntOffset(x = 0, y = offset.toInt())
                }
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 3,
                modifier = Modifier
                    .padding(
                        start = with(LocalDensity.current) { horizontalPaddingPx.toDp() },
                        end = with(LocalDensity.current) { rightPaddingPx.toDp() },
                    )
                    .sharedBounds(
                        rememberSharedContentState(key = keyProvider(SharedElementType.Title)),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = sharedDetailBoundsTransform,
                    )
                    .wrapContentWidth(),
            )
            tagline?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    modifier = Modifier
                        .padding(
                            start = with(LocalDensity.current) { horizontalPaddingPx.toDp() },
                            end = with(LocalDensity.current) { rightPaddingPx.toDp() },
                        )
                        .sharedBounds(
                            rememberSharedContentState(key = keyProvider(SharedElementType.Tagline)),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = sharedDetailBoundsTransform,
                        )
                        .wrapContentWidth(),
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailImage(
    imageUrl: String?,
    keyProvider: SharedKeyProvider,
    scroll: ScrollState
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No sharedTransitionScope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalStateException("No animatedVisibilityScope found")

    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFractionProvider = { (scroll.value / collapseRange).coerceIn(0f, 1f) }

    CollapsingImageLayout(
        collapseFractionProvider = collapseFractionProvider,
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = HzPadding)
    ) {
        with(sharedTransitionScope) {
            AsyncImage(
                model = imageUrl ?: "",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(4.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                    .sharedBounds(
                        rememberSharedContentState(key = keyProvider(SharedElementType.Image)),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = sharedDetailBoundsTransform,
                    )
                    .fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.SharedDetailUp(upPress: () -> Unit) {
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")
    with(animatedVisibilityScope) {
        IconButton(
            onClick = upPress,
            modifier = Modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 3f)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(CircleShape)
                .background(color = Color(0xff121212).copy(alpha = 0.32f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun CollapsingImageLayout(
    collapseFractionProvider: () -> Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val collapseFraction = collapseFractionProvider()

        val imageMaxSize = min(ExpandedImageSize.roundToPx(), constraints.maxWidth)
        val imageMinSize = max(CollapsedImageSize.roundToPx(), constraints.minWidth)
        val imageWidth =
            lerp(imageMaxSize.toFloat(), imageMinSize.toFloat(), collapseFraction).roundToInt()
        val imagePlaceable = measurables[0].measure(
            Constraints.fixed(
                imageWidth,
                imageWidth
            )
        )

        val imageY = lerp(
            MinTitleOffset.roundToPx().toFloat(),
            MinImageOffset.roundToPx().toFloat(),
            collapseFraction
        ).roundToInt()
        val imageX = lerp(
            ((constraints.maxWidth - imageWidth) / 2).toFloat(),
            (constraints.maxWidth - imageWidth).toFloat(),
            collapseFraction,
        ).roundToInt()
        layout(
            width = constraints.maxWidth,
            height = imageY + imageWidth,
        ) {
            imagePlaceable.placeRelative(imageX, imageY)
        }
    }
}
