package me.calebjones.spacelaunchnow.ui.home.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Solid
import compose.icons.fontawesomeicons.solid.Newspaper
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.LocalUseUtc
import me.calebjones.spacelaunchnow.ui.viewmodel.FeedViewModel
import me.calebjones.spacelaunchnow.util.DateTimeUtil
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant


@Composable
fun ArticlesView() {
    val feedViewModel = koinViewModel<FeedViewModel>()
    val state by feedViewModel.articlesState.collectAsStateWithLifecycle()

    // Load articles if not already loaded and no error
    LaunchedEffect(Unit) {
        if (state.data.isEmpty() && !state.isLoading && state.error == null) {
            feedViewModel.loadArticles(5)
        }
    }

    when {
        // STATE 4: Error State - show cached data with banner OR just error
        state.error != null -> {
            if (state.data.isNotEmpty()) {
                // Show stale data with error indicator
                Column {
                    // Error banner
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Showing cached data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    // Show stale articles
                    Column(modifier = Modifier.padding(vertical = 16.dp)) {
                        state.data.take(5).forEach { article ->
                            ArticleItem(article = article)
                        }
                    }
                }
            } else {
                // No cached data, just show error
                ArticleErrorCard(error = state.error!!)
            }
        }

        // STATE 2 & 3: Loading with existing data
        state.isLoading && state.data.isNotEmpty() -> {
            Box {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    state.data.take(5).forEach { article ->
                        ArticleItem(article = article)
                    }
                }

                // Show loading indicator
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(24.dp)
                )
            }
        }

        // Data available (not loading, no error)
        state.data.isNotEmpty() && state.error == null -> {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                state.data.take(5).forEach { article ->
                    ArticleItem(article = article)
                }
            }
        }

        // STATE 1: Fresh load, no data - show shimmer
        state.isLoading -> {
            Column {
                repeat(3) {
                    NewsItemShimmer()
                }
            }
        }

        // Fallback
        else -> {
            Column {
                repeat(3) {
                    NewsItemShimmer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleItem(article: Article) {
    val uriHandler = LocalUriHandler.current
    Card(
        onClick = { uriHandler.openUri(article.url) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val image = article.imageUrl
            SubcomposeAsyncImage(
                model = image,
                contentDescription = "Article Image",
                modifier = Modifier
                    .size(96.dp)
                    .clip(shape = RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                error = {
                    // Show Font Awesome newspaper icon as fallback
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Newspaper,
                            contentDescription = "Article Icon",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(96.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = article.summary.replace(".\n", " ").replace("\n", ""),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        maxLines = 3,
                        minLines = 3,
                        overflow = TextOverflow.Visible,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = article.newsSite,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.labelSmall
                    )
                    val useUtc = LocalUseUtc.current
                    Text(
                        text = formatPublishedDate(article.publishedAt, useUtc),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ArticleErrorCard(
    error: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Failed to load articles",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun NewsItemShimmer() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(14.dp),
            )
        }
    }
}

/**
 * Format published date to human readable format using locale-aware formatting
 */
private fun formatPublishedDate(instant: Instant, useUtc: Boolean): String {
    return try {
        DateTimeUtil.formatLaunchDate(instant, useUtc)
    } catch (e: Exception) {
        "Unknown date"
    }
}
