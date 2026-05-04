package me.calebjones.spacelaunchnow.wear.ui.premium

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.remote.interactions.RemoteActivityHelper
import co.touchlab.kermit.Logger
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.wear.R

private val HorizontalContentPadding = 14.dp

@Composable
fun PremiumGateScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = columnState) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
                start = HorizontalContentPadding,
                end = HorizontalContentPadding,
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                ) {
                    Image(
                        painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(52.dp).clip(CircleShape),
                    )
                }
            }

            item { Spacer(Modifier.height(8.dp)) }

            item {
                Text(
                    text = "Premium Feature",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                )
            }

            item { Spacer(Modifier.height(4.dp)) }

            item {
                Text(
                    text = "Subscribe on your phone to unlock Wear OS features",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                )
            }

            item { Spacer(Modifier.height(12.dp)) }

            item {
                Button(
                    onClick = {
                        scope.launch { openPaywallOnPhone(context) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .transformedHeight(this, transformationSpec),
                    label = {
                        Text(
                            text = "Open App",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                        )
                    },
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

private suspend fun openPaywallOnPhone(context: Context) {
    val log = Logger.withTag("PremiumGateScreen")
    try {
        val remoteActivityHelper = RemoteActivityHelper(context)
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData("spacelaunchnow://premium".toUri())
        remoteActivityHelper.startRemoteActivity(intent).await()
        log.i { "Opened paywall on phone" }
    } catch (e: Exception) {
        log.e(e) { "Failed to open paywall on phone" }
    }
}
