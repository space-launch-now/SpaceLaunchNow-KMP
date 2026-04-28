package me.calebjones.spacelaunchnow.util

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

private object WearOsHelper : KoinComponent {
    val context: Context by inject()
}

/**
 * Android implementation: queries the Wearable NodeClient for connected nodes.
 * Returns true when at least one watch node reports isNearby == true
 * (Bluetooth range) or is otherwise reachable.
 */
actual suspend fun isWearOsConnected(): Boolean {
    return try {
        val nodes = Wearable.getNodeClient(WearOsHelper.context)
            .connectedNodes
            .await()
        nodes.isNotEmpty()
    } catch (e: Exception) {
        // Play Services unavailable or no paired watch
        false
    }
}

actual val isWearOsSupportedPlatform: Boolean = true

