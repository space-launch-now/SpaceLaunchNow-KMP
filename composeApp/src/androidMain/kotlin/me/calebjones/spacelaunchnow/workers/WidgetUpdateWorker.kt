package me.calebjones.spacelaunchnow.workers

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.calebjones.spacelaunchnow.widgets.LaunchListWidget
import me.calebjones.spacelaunchnow.widgets.NextUpWidget

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Update all instances of NextUpWidget
            NextUpWidget().updateAll(context)

            // Update all instances of LaunchListWidget
            LaunchListWidget().updateAll(context)

            println("Widgets updated successfully")
            Result.success()
        } catch (e: Exception) {
            println("Failed to update widgets: ${e.message}")
            e.printStackTrace()
            Result.retry()
        }
    }
}
