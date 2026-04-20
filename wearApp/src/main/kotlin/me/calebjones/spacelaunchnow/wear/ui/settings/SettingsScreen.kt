package me.calebjones.spacelaunchnow.wear.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight

@Composable
fun SettingsScreen() {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                ) {
                    Text(text = "Settings")
                }
            }
            item {
                Text(
                    text = "Space Launch Now Wear",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                Text(
                    text = "v${getAppVersion()}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                Text(
                    text = "\u26a0 Early Release",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .transformedHeight(this, transformationSpec)
                        .padding(top = 8.dp),
                )
            }
            item {
                Text(
                    text = "Wear OS support is in early release. More features and improvements are coming.",
                    style = MaterialTheme.typography.bodyExtraSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
        }
    }
}

private fun getAppVersion(): String {
    return try {
        "1.0.0"
    } catch (_: Exception) {
        "Unknown"
    }
}
