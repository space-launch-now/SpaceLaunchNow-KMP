package me.calebjones.spacelaunchnow.ui.components.webview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.calebjones.spacelaunchnow.util.ExternalLinkHandler

/**
 * Shared fallback used by the iOS and Desktop [WebView] actuals, which have no in-app browser.
 *
 * It opens [url] in the system browser once on first composition and renders a simple
 * "open in browser" surface so the user has an explicit re-open affordance. Keeping this in
 * commonMain avoids duplicating the UI in each platform actual.
 */
@Composable
fun WebViewBrowserFallback(
    url: String,
    modifier: Modifier = Modifier,
) {
    // Open the article in the system browser as soon as the screen is shown.
    LaunchedEffect(url) {
        if (url.isNotBlank()) {
            ExternalLinkHandler.openUrl(url)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Opening article in your browser…",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = { if (url.isNotBlank()) ExternalLinkHandler.openUrl(url) },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = null,
            )
            Text(
                text = "Open in browser",
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}
