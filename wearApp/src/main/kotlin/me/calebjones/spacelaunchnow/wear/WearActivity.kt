package me.calebjones.spacelaunchnow.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import co.touchlab.kermit.Logger
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.wear.data.EntitlementSyncManager
import me.calebjones.spacelaunchnow.wear.ui.WearApp
import me.calebjones.spacelaunchnow.wear.ui.theme.WearTheme
import org.koin.android.ext.android.inject

class WearActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    private val entitlementSyncManager: EntitlementSyncManager by inject()
    private val activityScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val log = Logger.withTag("WearActivity")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val deepLinkLaunchId = intent?.getStringExtra(EXTRA_LAUNCH_ID)
        setContent {
            WearTheme {
                WearApp(deepLinkLaunchId = deepLinkLaunchId)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        log.d { "DataClient listener registered" }
    }

    override fun onPause() {
        Wearable.getDataClient(this).removeListener(this)
        log.d { "DataClient listener removed" }
        super.onPause()
    }

    override fun onDestroy() {
        activityScope.cancel()
        super.onDestroy()
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path ?: return@forEach
                if (path == PATH_ENTITLEMENT) {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val active = dataMap.getBoolean(KEY_ACTIVE, false)
                    val expiresAtStr = dataMap.getString(KEY_EXPIRES_AT, "")
                    val expiresAt = if (expiresAtStr.isNullOrEmpty()) null else {
                        try {
                            Instant.parse(expiresAtStr)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    log.i { "Received entitlement via DataClient: active=$active" }
                    activityScope.launch {
                        entitlementSyncManager.onEntitlementReceived(active, expiresAt)
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_LAUNCH_ID = "extra_launch_id"
        private const val PATH_ENTITLEMENT = "/spacelaunchnow/entitlement"
        private const val KEY_ACTIVE = "active"
        private const val KEY_EXPIRES_AT = "expiresAt"
    }
}
