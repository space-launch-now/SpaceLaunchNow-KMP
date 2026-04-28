package me.calebjones.spacelaunchnow.wear.complication

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import co.touchlab.kermit.Logger

/**
 * Triggered by AlarmManager to force a complication refresh.
 * Used for the sub-1-hour rapid (5 min) update cycle.
 */
class ComplicationUpdateReceiver : BroadcastReceiver() {

    private val log = Logger.withTag("ComplicationUpdateReceiver")

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_UPDATE) return
        log.d { "Requesting complication update via alarm" }
        ComplicationDataSourceUpdateRequester
            .create(
                context = context,
                complicationDataSourceComponent = ComponentName(
                    context,
                    NextLaunchComplicationService::class.java
                )
            )
            .requestUpdateAll()
    }

    companion object {
        const val ACTION_UPDATE = "me.calebjones.spacelaunchnow.wear.ACTION_UPDATE_COMPLICATION"
    }
}

