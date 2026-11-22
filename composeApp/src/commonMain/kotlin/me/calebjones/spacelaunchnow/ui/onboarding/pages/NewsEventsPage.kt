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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
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
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.common_latest_news
import spacelaunchnow_kmp.composeapp.generated.resources.common_news_and_events
import spacelaunchnow_kmp.composeapp.generated.resources.onboarding_news_subtitle

/**
 * Onboarding page — shows live news articles inside a device frame.
 */
@Composable
fun NewsEventsPage(
    modifier: Modifier = Modifier,
    articles: List<Article> = emptyList()
) {
    OnboardingPage(
        title = stringResource(Res.string.common_news_and_events),
        subtitle = stringResource(Res.string.onboarding_news_subtitle),
        icon = Icons.Default.Newspaper,
        modifier = modifier
    ) {
        NewsPreviewContent(articles = articles)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun NewsPreviewContent(articles: List<Article>) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(Res.string.common_latest_news),
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
                    LoadingIndicator()
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
