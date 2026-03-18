package me.calebjones.spacelaunchnow.ui.onboarding.pages

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.home.components.ArticleItem
import me.calebjones.spacelaunchnow.ui.onboarding.OnboardingPage
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Onboarding page — shows live news articles inside a device frame.
 */
@Composable
fun NewsEventsPage(
    modifier: Modifier = Modifier,
    articles: List<Article> = emptyList()
) {
    OnboardingPage(
        title = "News & Events",
        subtitle = "Read the latest space news and follow live events from launches to spacewalks.",
        icon = Icons.Default.Newspaper,
        modifier = modifier
    ) {
        NewsPreviewContent(articles = articles)
    }
}

@Composable
private fun NewsPreviewContent(articles: List<Article>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Latest News",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (articles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(articles) { article ->
                        ArticleItem(article = article, onClick = {})
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Previews
// ---------------------------------------------------------------------------

@Preview
@Composable
private fun NewsEventsPagePreview() {
    SpaceLaunchNowPreviewTheme {
        NewsEventsPage()
    }
}

@Preview
@Composable
private fun NewsEventsPageDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        NewsEventsPage()
    }
}
