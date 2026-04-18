package me.calebjones.spacelaunchnow.wear.worker

import android.content.ComponentName
import android.content.Context
import androidx.wear.tiles.TileService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import me.calebjones.spacelaunchnow.wear.complication.NextLaunchComplicationService
import me.calebjones.spacelaunchnow.wear.data.WatchLaunchRepository
import me.calebjones.spacelaunchnow.wear.tile.NextLaunchTileService
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class WatchDataRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {

    private val log = Logger.withTag("WatchDataRefreshWorker")
    private val watchLaunchRepository: WatchLaunchRepository by inject()

    override suspend fun doWork(): Result {
        log.d { "Starting periodic data refresh" }
        return try {
            val result = watchLaunchRepository.refreshLaunches()
            if (result.isSuccess) {
                log.i { "Data refresh completed successfully (${result.getOrNull()?.size} launches)" }
                requestComplicationUpdate()
                requestTileUpdate()
                Result.success()
            } else {
                log.w { "Data refresh failed: ${result.exceptionOrNull()?.message}" }
                Result.retry()
            }
        } catch (e: Exception) {
            log.e(e) { "Data refresh worker failed" }
            Result.retry()
        }
    }

    private fun requestComplicationUpdate() {
        try {
            val requester = ComplicationDataSourceUpdateRequester.create(
                applicationContext,
                ComponentName(applicationContext, NextLaunchComplicationService::class.java)
            )
            requester.requestUpdateAll()
            log.d { "Complication update requested" }
        } catch (e: Exception) {
            log.w(e) { "Failed to request complication update" }
        }
    }

    private fun requestTileUpdate() {
        try {
            TileService.getUpdater(applicationContext)
                .requestUpdate(NextLaunchTileService::class.java)
            log.d { "Tile update requested" }
        } catch (e: Exception) {
            log.w(e) { "Failed to request tile update" }
        }
    }
}
