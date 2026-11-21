package me.calebjones.spacelaunchnow.wear.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import me.calebjones.spacelaunchnow.wear.R
import me.calebjones.spacelaunchnow.wear.ui.theme.wearHorizontalPadding

@Composable
fun SettingsScreen() {
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()
    val hPadding = wearHorizontalPadding()
    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = hPadding,
                end = hPadding,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                ) {
                    Text(text = stringResource(R.string.settings))
                }
            }
            item {
                Text(
                    text = stringResource(R.string.wear_settings_app_name),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                Text(
                    text = stringResource(R.string.wear_settings_version, getAppVersion()),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.transformedHeight(this, transformationSpec),
                )
            }
            item {
                ListHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                ) {
                    Text(text = stringResource(R.string.wear_settings_about))
                }
            }
            item {
                Text(
                    text = stringResource(R.string.wear_settings_about_body),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
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
