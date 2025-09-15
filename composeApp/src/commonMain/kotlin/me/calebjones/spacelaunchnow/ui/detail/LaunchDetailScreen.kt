package me.calebjones.spacelaunchnow.ui.detail

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.min
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import me.calebjones.spacelaunchnow.api.launchlibrary.models.LaunchDetailed
import me.calebjones.spacelaunchnow.cache.LaunchCache
import me.calebjones.spacelaunchnow.ui.SharedElementKey
import me.calebjones.spacelaunchnow.ui.SharedElementType
import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailView
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalNavAnimatedVisibilityScope
import me.calebjones.spacelaunchnow.ui.layout.phone.LocalSharedTransitionScope
import me.calebjones.spacelaunchnow.ui.viewmodel.LaunchViewModel
import me.calebjones.spacelaunchnow.util.StatusColorUtil
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.max
import kotlin.math.min

private val BottomBarHeight = 56.dp
private val TitleHeight = 128.dp
private val GradientScroll = 180.dp
private val ImageOverlap = 115.dp
private val MinTitleOffset = 56.dp
private val MinImageOffset = 12.dp
private val MaxTitleOffset = ImageOverlap + MinTitleOffset + GradientScroll
private val ExpandedImageSize = 300.dp
private val CollapsedImageSize = 150.dp
private val HzPadding = Modifier.padding(horizontal = 24.dp)


fun <T> spatialExpressiveSpring() = spring<T>(
    dampingRatio = 0.8f,
    stiffness = 380f,
)

fun <T> nonSpatialExpressiveSpring() = spring<T>(
    dampingRatio = 1f,
    stiffness = 1600f,
)

@OptIn(ExperimentalSharedTransitionApi::class)
val snackDetailBoundsTransform = BoundsTransform { _, _ ->
    spatialExpressiveSpring()
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun LaunchDetailScreen(
    launchId: String,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<LaunchViewModel>()
    val launchCache = koinInject<LaunchCache>()
    
    // Check if we have pre-loaded detailed data in cache
    val cachedLaunchDetailed = remember(launchId) { launchCache.getCachedLaunchDetailed(launchId) }
    val launchDetails by viewModel.launchDetails.collectAsState()
    
    // Use preloaded data if available, otherwise use fetched data
    val currentLaunch = cachedLaunchDetailed ?: launchDetails
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

    with(sharedTransitionScope) {
        Box(
            Modifier
                .clip(RoundedCornerShape(roundedCornerAnim))
                .sharedBounds(
                    rememberSharedContentState(
                        key = SharedElementKey(
                            launchId = currentLaunch?.id ?: "unknown_id",
                            type = SharedElementType.Bounds,
                        ),
                    ),
                    animatedVisibilityScope,
                    clipInOverlayDuringTransition =
                        OverlayClip(RoundedCornerShape(roundedCornerAnim)),
                    boundsTransform = snackDetailBoundsTransform,
                    exit = fadeOut(nonSpatialExpressiveSpring()),
                    enter = fadeIn(nonSpatialExpressiveSpring()),
                )
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.surfaceContainer),
        ) {
            val scroll = rememberScrollState(0)
            Header(currentLaunch?.id ?: "unknown_id")
            Body(currentLaunch, scroll)
            Title(currentLaunch) { scroll.value }
            Image(currentLaunch) { scroll.value }
            Up(onNavigateBack)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Header(launchId: String) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalArgumentException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")

    with(sharedTransitionScope) {
        val brushColors = listOf<Color>(Color.Blue, Color.LightGray)

        val infiniteTransition = rememberInfiniteTransition(label = "background")
        val targetOffset = with(LocalDensity.current) {
            1000.dp.toPx()
        }
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = targetOffset,
            animationSpec = infiniteRepeatable(
                tween(50000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "offset",
        )
        Spacer(
            modifier = Modifier
                .sharedBounds(
                    rememberSharedContentState(
                        key = SharedElementKey(
                            launchId = launchId,
                            type = SharedElementType.Background,
                        ),
                    ),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = snackDetailBoundsTransform,
                    enter = fadeIn(nonSpatialExpressiveSpring()),
                    exit = fadeOut(nonSpatialExpressiveSpring()),
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(),
                )
                .height(280.dp)
                .fillMaxWidth()
                .blur(40.dp)
                .drawWithCache {
                    val brushSize = 400f
                    val brush = Brush.linearGradient(
                        colors = brushColors,
                        start = Offset(offset, offset),
                        end = Offset(offset + brushSize, offset + brushSize),
                        tileMode = TileMode.Mirror,
                    )
                    onDrawBehind {
                        drawRect(brush)
                    }
                },
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun SharedTransitionScope.Up(upPress: () -> Unit) {
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")
    with(animatedVisibilityScope) {
        IconButton(
            onClick = upPress,
            modifier = Modifier
                .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 3f)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .size(36.dp)
                .animateEnterExit(
                    enter = scaleIn(tween(300, delayMillis = 300)),
                    exit = scaleOut(tween(20)),
                )
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
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Body(launch: LaunchDetailed?, scroll: ScrollState) {
    val sharedTransitionScope =
        LocalSharedTransitionScope.current ?: throw IllegalStateException("No scope found")
    with(sharedTransitionScope) {
        Column(modifier = Modifier.skipToLookaheadSize()) {
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
                Surface(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                ) {
                    Column {
                        Spacer(Modifier.height(TitleHeight))
                        Text(
                            text = "Test",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = HzPadding,
                        )
                        Spacer(Modifier.height(16.dp))
                        var seeMore by remember { mutableStateOf(true) }
                        with(sharedTransitionScope) {
                            Text(
                                text = "Test",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = if (seeMore) 5 else Int.MAX_VALUE,
                                overflow = TextOverflow.Ellipsis,
                                modifier = HzPadding.skipToLookaheadSize(),

                                )
                        }


                        Text(
                            text = "See More",
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .heightIn(20.dp)
                                .fillMaxWidth()
                                .padding(top = 15.dp)
                                .clickable {
                                    seeMore = !seeMore
                                }
                                .skipToLookaheadSize(),
                        )
                        Spacer(
                            modifier = Modifier.height(1000.dp).fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Title(launch: LaunchDetailed?, scrollProvider: () -> Int) {
    val maxOffset = with(LocalDensity.current) { MaxTitleOffset.toPx() }
    val minOffset = with(LocalDensity.current) { MinTitleOffset.toPx() }
    val sharedTransitionScope = LocalSharedTransitionScope.current
        ?: throw IllegalArgumentException("No Scope found")
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
        ?: throw IllegalArgumentException("No Scope found")

    with(sharedTransitionScope) {
        Column(
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = TitleHeight)
                .statusBarsPadding()
                .offset {
                    val scroll = scrollProvider()
                    val offset = (maxOffset - scroll).coerceAtLeast(minOffset)
                    IntOffset(x = 0, y = offset.toInt())
                }
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            Spacer(Modifier.height(16.dp))
            Text(
                text = launch?.name ?: "",
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.headlineMedium,
                modifier = HzPadding
                    .sharedBounds(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                launchId = launch?.id ?: "unknown_id",
                                type = SharedElementType.Title,
                            ),
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = snackDetailBoundsTransform,
                    )
                    .wrapContentWidth(),
            )
            Text(
                text = "tag line",
                fontStyle = FontStyle.Italic,
                style = MaterialTheme.typography.titleSmall,
                modifier = HzPadding
                    .sharedBounds(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                launchId = launch?.id ?: "unknown_id",
                                type = SharedElementType.Tagline,
                            ),
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = snackDetailBoundsTransform,
                    )
                    .wrapContentWidth(),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun Image(
    launch: LaunchDetailed?,
    scrollProvider: () -> Int,
) {
    val collapseRange = with(LocalDensity.current) { (MaxTitleOffset - MinTitleOffset).toPx() }
    val collapseFractionProvider = {
        (scrollProvider() / collapseRange).coerceIn(0f, 1f)
    }

    CollapsingImageLayout(
        collapseFractionProvider = collapseFractionProvider,
        modifier = HzPadding.statusBarsPadding(),
    ) {
        val sharedTransitionScope = LocalSharedTransitionScope.current
            ?: throw IllegalStateException("No sharedTransitionScope found")
        val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current
            ?: throw IllegalStateException("No animatedVisibilityScope found")

        with(sharedTransitionScope) {
            AsyncImage(
                model = launch?.image?.imageUrl ?: "",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        2.dp,
                        StatusColorUtil.getLaunchStatusColor(launch?.status?.id),
                        CircleShape
                    )
                    .sharedBounds(
                        rememberSharedContentState(
                            key = SharedElementKey(
                                launchId = launch?.id ?: "unknown_id",
                                type = SharedElementType.Image,
                            ),
                        ),
                        animatedVisibilityScope = animatedVisibilityScope,
                        exit = fadeOut(),
                        enter = fadeIn(),
                        boundsTransform = snackDetailBoundsTransform,
                    )
                    .fillMaxSize(),

                )
        }
    }
}

@Composable
private fun CollapsingImageLayout(collapseFractionProvider: () -> Float, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        check(measurables.size == 1)

        val collapseFraction = collapseFractionProvider()

        val imageMaxSize = min(ExpandedImageSize.roundToPx(), constraints.maxWidth)
        val imageMinSize = max(CollapsedImageSize.roundToPx(), constraints.minWidth)
        val imageWidth = lerp(imageMaxSize, imageMinSize, collapseFraction)
        val imagePlaceable = measurables[0].measure(Constraints.fixed(imageWidth, imageWidth))

        val imageY = lerp(MinTitleOffset, MinImageOffset, collapseFraction).roundToPx()
        val imageX = lerp(
            (constraints.maxWidth - imageWidth) / 2, // centered when expanded
            constraints.maxWidth - imageWidth, // right aligned when collapsed
            collapseFraction,
        )
        layout(
            width = constraints.maxWidth,
            height = imageY + imageWidth,
        ) {
            imagePlaceable.placeRelative(imageX, imageY)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CartBottomBar(modifier: Modifier = Modifier) {
    val (count, updateCount) = remember { mutableIntStateOf(1) }
    val sharedTransitionScope =
        LocalSharedTransitionScope.current ?: throw IllegalStateException("No Shared scope")
    val animatedVisibilityScope =
        LocalNavAnimatedVisibilityScope.current ?: throw IllegalStateException("No Shared scope")
    with(sharedTransitionScope) {
        with(animatedVisibilityScope) {
            Surface(
                modifier = modifier
                    .renderInSharedTransitionScopeOverlay(zIndexInOverlay = 4f)
                    .animateEnterExit(
                        enter = slideInVertically(
                            tween(
                                300,
                                delayMillis = 300,
                            ),
                        ) { it } + fadeIn(tween(300, delayMillis = 300)),
                        exit = slideOutVertically(tween(50)) { it } +
                                fadeOut(tween(50)),
                    ),
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .navigationBarsPadding()
                            .then(HzPadding)
                            .heightIn(min = BottomBarHeight),
                    ) {
                        Text(
                            text = "Test",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .padding(end = 18.dp)
                                .align(Alignment.CenterVertically),
                        )
                        Spacer(Modifier.width(16.dp))
                        Button(
                            onClick = { /* todo */ },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = "Test",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
    }
}
