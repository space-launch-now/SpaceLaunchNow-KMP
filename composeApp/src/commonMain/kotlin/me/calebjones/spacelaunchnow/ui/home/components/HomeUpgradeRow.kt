package me.calebjones.spacelaunchnow.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.repository.RemoteConfigRepository
import me.calebjones.spacelaunchnow.data.storage.HomeUpgradeRowPreferences
import me.calebjones.spacelaunchnow.ui.subscription.rememberIsPremium
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.home_upgrade_row_action
import spacelaunchnow_kmp.composeapp.generated.resources.home_upgrade_row_close
import spacelaunchnow_kmp.composeapp.generated.resources.home_upgrade_row_title

/** The row shows for free users only, when the remote flag is on and the user has not closed it. */
internal fun shouldShowHomeUpgradeRow(flagEnabled: Boolean, isPremium: Boolean, dismissed: Boolean): Boolean =
    flagEnabled && !isPremium && !dismissed

/**
 * Quiet inline upgrade row for Home. Opens the paywall through [onClick]; the close button
 * hides it for good (persisted in [HomeUpgradeRowPreferences]).
 */
@Composable
fun HomeUpgradeRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remoteConfig = koinInject<RemoteConfigRepository>()
    val preferences = koinInject<HomeUpgradeRowPreferences>()
    val isPremium by rememberIsPremium()
    // Starts hidden until both the flag and the stored close are known, so the row never flashes.
    var flagEnabled by remember { mutableStateOf(false) }
    val dismissed by preferences.dismissedFlow.collectAsState(initial = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { flagEnabled = remoteConfig.getHomeUpgradeRowEnabled() }

    if (!shouldShowHomeUpgradeRow(flagEnabled, isPremium, dismissed)) return

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(Res.string.home_upgrade_row_title),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClick) {
                Text(stringResource(Res.string.home_upgrade_row_action))
            }
            IconButton(onClick = { scope.launch { preferences.dismiss() } }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.home_upgrade_row_close),
                )
            }
        }
    }
}
