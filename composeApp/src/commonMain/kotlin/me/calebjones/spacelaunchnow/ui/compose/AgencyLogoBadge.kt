package me.calebjones.spacelaunchnow.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.valentinilk.shimmer.shimmer

/**
 * Small square agency logo on a surface chip, for use next to titles.
 * Renders nothing when [logoUrl] is null or blank.
 */
@Composable
fun AgencyLogoBadge(
    logoUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
) {
    if (logoUrl.isNullOrBlank()) return
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.size(size),
    ) {
        SubcomposeAsyncImage(
            model = logoUrl,
            contentDescription = "Agency logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shimmer()
                )
            },
            error = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }
        )
    }
}
