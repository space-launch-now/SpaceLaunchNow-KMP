package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
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
import coil3.compose.SubcomposeAsyncImage
import me.calebjones.spacelaunchnow.isLargeScreen
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

// Shared Transition Layout Constants (scoped to this file)
private val TitleHeight = 128.dp
private val CompactTitleHeight = 88.dp // For landscape tablets
private val GradientScroll = 180.dp
private val ImageOverlap = 115.dp
private val MinTitleOffset = 64.dp
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 100.dp
private val HzPadding = 24.dp


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedDetailScaffold(
    titleText: String,
    taglineText: String?,
    imageUrl: String?,
    onNavigateBack: () -> Unit,
    backgroundColors: List<Color>? = null,
    scrollEnabled: Boolean = true,
    content: @Composable () -> Unit,
) {

    val isLargeScreen = isLargeScreen()

    // For tablets, always start collapsed and don't allow expansion
    val forceNoCollapse = isLargeScreen

    val bgColors = backgroundColors ?: listOf(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.surfaceVariant
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        SharedDetailBackground(bgColors, forceNoCollapse)

        val scroll = rememberScrollState(0)

        // For tablets: force collapsed layout, for phones: original collapsing layout
        SharedDetailBody(scroll, scrollEnabled, forceNoCollapse, content)
        SharedDetailTitle(
            titleText,
            taglineText,
            scroll,
            forceNoCollapse = forceNoCollapse
        )
        SharedDetailImage(imageUrl, scroll, forceNoCollapse = forceNoCollapse)
        SharedDetailUp(onNavigateBack)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailBackground(colors: List<Color>, forceNoCollapse: Boolean = false) {
    val isLargeScreen = isLargeScreen()

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

    // For collapsed layout, use smaller background height
    val backgroundHeight = if (forceNoCollapse) {
        if (isLargeScreen) CompactTitleHeight else TitleHeight
    } else 280.dp

    Spacer(
        modifier = Modifier
            .height(backgroundHeight)
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailBody(
    scroll: ScrollState,
    scrollEnabled: Boolean,
    forceNoCollapse: Boolean,
    content: @Composable () -> Unit
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalStateException("No scope found")
    val isLargeScreen = isLargeScreen()

    with(sharedTransitionScope) {
        Column {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(
                        if (forceNoCollapse) {
                            if (isLargeScreen) CompactTitleHeight else TitleHeight
                        } else MinTitleOffset
                    ),
            )

            Column(
                modifier = Modifier
                    .let { if (scrollEnabled) it.verticalScroll(scroll) else it }
            ) {
                // For collapsed layout, skip the gradient and image overlap space
                if (!forceNoCollapse) {
                    Spacer(Modifier.height(GradientScroll))
                    Spacer(Modifier.height(ImageOverlap))
                }
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
    scroll: ScrollState,
    forceNoCollapse: Boolean = false,
) {
    val isLargeScreen = isLargeScreen()

    val maxOffset = with(LocalDensity.current) { MaxTitleOffset.toPx() }
    val minOffset = with(LocalDensity.current) { MinTitleOffset.toPx() }
    // Calculate collapse fraction to determine image size and position
    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFraction =
        if (forceNoCollapse) 1f else (scroll.value / collapseRange).coerceIn(0f, 1f)

    // Calculate current image size and position
    val imageMaxSize = with(LocalDensity.current) { ExpandedImageSize.toPx() }
    // Use smaller collapsed size for large screen devices
    val baseCollapsedSize = if (isLargeScreen) 80.dp else CollapsedImageSize
    val imageMinSize = with(LocalDensity.current) { baseCollapsedSize.toPx() }
    val currentImageSize = lerp(imageMaxSize, imageMinSize, collapseFraction)

    // Calculate horizontal padding needed to avoid overlap with collapsed image and back button
    val horizontalPaddingPx = with(LocalDensity.current) { HzPadding.toPx() }
    val leftPaddingPx = if (forceNoCollapse && isLargeScreen) {
        horizontalPaddingPx + with(LocalDensity.current) { 48.dp.toPx() } // Space for back button
    } else {
        horizontalPaddingPx
    }
    val rightPaddingPx = if (forceNoCollapse || collapseFraction > 0.5f) {
        horizontalPaddingPx + currentImageSize + with(LocalDensity.current) { 16.dp.toPx() }
    } else {
        horizontalPaddingPx
    }

    Column(
        verticalArrangement = Arrangement.Bottom,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(
                min = if (forceNoCollapse) {
                    if (isLargeScreen) CompactTitleHeight else TitleHeight
                } else TitleHeight
            )
            .statusBarsPadding()
            .let {
                if (forceNoCollapse) it else it.offset {
                    val offset = (maxOffset - scroll.value).coerceAtLeast(minOffset)
                    IntOffset(x = 0, y = offset.toInt())
                }
            }
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier
                .padding(
                    start = with(LocalDensity.current) { leftPaddingPx.toDp() },
                    end = with(LocalDensity.current) { rightPaddingPx.toDp() },
                )
                .wrapContentWidth(),
        )
        tagline?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                modifier = Modifier
                    .padding(
                        start = with(LocalDensity.current) { leftPaddingPx.toDp() },
                        end = with(LocalDensity.current) { rightPaddingPx.toDp() },
                    )
                    .wrapContentWidth(),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailImage(
    imageUrl: String?,
    scroll: ScrollState,
    forceNoCollapse: Boolean = false,
) {

    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFractionProvider =
        { if (forceNoCollapse) 1f else (scroll.value / collapseRange).coerceIn(0f, 1f) }

    CollapsingImageLayout(
        collapseFractionProvider = collapseFractionProvider,
        modifier = Modifier
            .statusBarsPadding()
            .padding(horizontal = HzPadding)
    ) {
        SubcomposeAsyncImage(
            model = imageUrl ?: "",
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(CircleShape)
                .border(4.dp, MaterialTheme.colorScheme.onSurfaceVariant, CircleShape)
                .fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            error = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainer),
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedDetailUp(upPress: () -> Unit) {
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")
    val isLargeScreen = isLargeScreen()

    with(animatedVisibilityScope) {
        IconButton(
            onClick = upPress,
            modifier = Modifier
                .statusBarsPadding()
                .let {
                    if (isLargeScreen) {
                        // For landscape tablets, position relative to the compact title height
                        it.offset(y = (CompactTitleHeight - 36.dp) / 2)
                    } else {
                        it
                    }
                }
                .padding(
                    horizontal = if (isLargeScreen) 12.dp else 16.dp,
                    vertical = if (isLargeScreen) 0.dp else 10.dp
                )
                .size(if (isLargeScreen) 36.dp else 48.dp)
                .clip(CircleShape)
                .background(
                    color = if (isLargeScreen)
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    else
                        Color(0xff121212).copy(alpha = 0.32f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Back",
                tint = if (isLargeScreen)
                    MaterialTheme.colorScheme.onSurface
                else
                    Color.White,
                modifier = Modifier.size(if (isLargeScreen) 20.dp else 24.dp)
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
    val isLargeScreen = isLargeScreen()

    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val collapseFraction = collapseFractionProvider()

        val imageMaxSize = min(ExpandedImageSize.roundToPx(), constraints.maxWidth)
        // Use smaller collapsed size for landscape tablets
        val baseCollapsedSize = if (isLargeScreen) 80.dp else CollapsedImageSize
        val imageMinSize = max(baseCollapsedSize.roundToPx(), constraints.minWidth)
        val imageWidth =
            lerp(imageMaxSize.toFloat(), imageMinSize.toFloat(), collapseFraction).roundToInt()
        val imagePlaceable = measurables[0].measure(
            Constraints.fixed(
                imageWidth,
                imageWidth
            )
        )

        val imageY = if (isLargeScreen && collapseFraction == 1f) {
            // For landscape tablets in collapsed state, center the image in the compact title area
            val statusBarHeight = 0 // statusBarsPadding is handled by the parent
            val titleAreaHeight = CompactTitleHeight.roundToPx()
            statusBarHeight + (titleAreaHeight - imageWidth) / 2
        } else {
            lerp(
                MinTitleOffset.roundToPx().toFloat(),
                MinImageOffset.roundToPx().toFloat(),
                collapseFraction
            ).roundToInt()
        }

        val imageX = lerp(
            ((constraints.maxWidth - imageWidth) / 2).toFloat(),
            (constraints.maxWidth - imageWidth).toFloat(),
            collapseFraction,
        ).roundToInt()

        val layoutHeight = if (isLargeScreen && collapseFraction == 1f) {
            CompactTitleHeight.roundToPx()
        } else {
            imageY + imageWidth
        }

        layout(
            width = constraints.maxWidth,
            height = layoutHeight,
        ) {
            imagePlaceable.placeRelative(imageX, imageY)
        }
    }
}
