package me.calebjones.spacelaunchnow.ui.newsevents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import me.calebjones.spacelaunchnow.ui.components.webview.WebView
import me.calebjones.spacelaunchnow.ui.theme.SpaceLaunchNowPreviewTheme
import me.calebjones.spacelaunchnow.util.ExternalLinkHandler
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * In-app news article viewer.
 *
 * Reached from a NEWS notification tap (routed through MainActivity into the [NewsDetail] route)
 * so the user lands inside the app rather than the external browser. The article renders in an
 * in-app [WebView] on Android; iOS/Desktop fall back to the system browser.
 *
 * Back behavior: if the embedded web view can navigate back within page history, the back button
 * does that first; otherwise it pops the screen, returning the user into the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    url: String,
    title: String,
    onNavigateBack: () -> Unit,
    navController: NavController? = null,
) {
    var isLoading by remember { mutableStateOf(true) }
    var canGoBack by remember { mutableStateOf(false) }
    // Incremented to request an in-page back navigation from the WebView actual.
    var backTrigger by remember { mutableIntStateOf(0) }

    val onBack: () -> Unit = {
        if (canGoBack) {
            backTrigger++
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = title.ifBlank { "Article" },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { if (url.isNotBlank()) ExternalLinkHandler.openUrl(url) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open in browser",
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            WebView(
                url = url,
                modifier = Modifier.fillMaxSize(),
                onLoadingChanged = { isLoading = it },
                onCanGoBackChanged = { canGoBack = it },
                backTrigger = backTrigger,
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun NewsDetailScreenPreview() {
    SpaceLaunchNowPreviewTheme {
        NewsDetailScreen(
            url = "https://example.com/article",
            title = "Starship completes integrated flight test",
            onNavigateBack = {},
        )
    }
}

@Preview
@Composable
private fun NewsDetailScreenDarkPreview() {
    SpaceLaunchNowPreviewTheme(isDark = true) {
        NewsDetailScreen(
            url = "https://example.com/article",
            title = "Starship completes integrated flight test",
            onNavigateBack = {},
        )
    }
}
