package me.calebjones.spacelaunchnow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.launcher

/**
 * Reusable component that displays the app launcher icon
 * wrapped in a circular box with a primary color background.
 *
 * @param modifier Modifier to be applied to the outer Box
 * @param boxSize Size of the outer circular box (default 112.dp)
 * @param iconSize Size of the app icon inside (default 90.dp)
 */
@Preview
@Composable
fun AppIconBox(
    modifier: Modifier = Modifier,
    boxSize: Dp = 112.dp,
    iconSize: Dp = 90.dp
) {
    Box(
        modifier = Modifier
            .size(125.dp)
            .shadow(elevation = 8.dp, shape = CircleShape, clip = false),
        contentAlignment = Alignment.Center
    ) {
        // Outer colorful sweep gradient border effect
        Box(
            modifier = Modifier
                .size(125.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary,
                        )
                    )
                )
                .then(Modifier)
        ) {}
        // Middle thin border
        Box(
            modifier = Modifier
                .size(121.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {}
        // Inner circle with app icon
        Box(
            modifier = modifier
                .size(boxSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.launcher),
                contentDescription = "Space Launch Now",
                modifier = Modifier
                    .size(iconSize)
                    .align(Alignment.Center)
            )
        }
    }
}

