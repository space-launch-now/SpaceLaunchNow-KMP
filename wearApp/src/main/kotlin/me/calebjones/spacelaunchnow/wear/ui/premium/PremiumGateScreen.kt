package me.calebjones.spacelaunchnow.wear.ui.premium

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.remote.interactions.RemoteActivityHelper
import co.touchlab.kermit.Logger
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch

@Composable
fun PremiumGateScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    ScreenScaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "🚀",
                style = MaterialTheme.typography.displayMedium,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Premium Feature",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Subscribe on your phone to unlock Wear OS features",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    scope.launch {
                        openPaywallOnPhone(context)
                    }
                },
                modifier = Modifier.fillMaxWidth(0.8f),
                label = { Text("Open on Phone") },
            )
        }
    }
}

private suspend fun openPaywallOnPhone(context: Context) {
    val log = Logger.withTag("PremiumGateScreen")
    try {
        val remoteActivityHelper = RemoteActivityHelper(context)
        val intent = Intent(Intent.ACTION_VIEW)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setData(Uri.parse("spacelaunchnow://premium"))
        remoteActivityHelper.startRemoteActivity(intent).await()
        log.i { "Opened paywall on phone" }
    } catch (e: Exception) {
        log.e(e) { "Failed to open paywall on phone" }
    }
}
