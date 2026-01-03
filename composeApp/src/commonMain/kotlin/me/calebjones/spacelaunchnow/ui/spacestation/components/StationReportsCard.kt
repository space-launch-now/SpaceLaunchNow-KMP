package me.calebjones.spacelaunchnow.ui.spacestation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.ui.detail.compose.components.RelatedNewsItem

/**
 * Card displaying station-related news reports from SNAPI
 * Uses Column with forEach instead of LazyColumn to avoid nested scrolling issues
 * Reuses the RelatedNewsItem component for consistent article display
 */
@Composable
fun StationReportsCard(
    articles: List<Article>,
    modifier: Modifier = Modifier
) {
    if (articles.isEmpty()) return

    var showAll by remember { mutableStateOf(false) }

    Text(
        text = "News and Reports",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp
    )

    Spacer(Modifier.height(8.dp))

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {

            // Display articles using RelatedNewsItem for consistent styling
            val displayedArticles = if (showAll) articles else articles.take(5)

            Column {
                displayedArticles.forEach { article ->
                    RelatedNewsItem(article = article)
                }
            }

            // Show "Load More" button if there are more than 5 articles
            if (articles.size > 5 && !showAll) {
                TextButton(
                    onClick = { showAll = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Load More (${articles.size - 5} more)",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Show "Show Less" button if currently showing all
            if (showAll && articles.size > 5) {
                TextButton(
                    onClick = { showAll = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Show Less",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
