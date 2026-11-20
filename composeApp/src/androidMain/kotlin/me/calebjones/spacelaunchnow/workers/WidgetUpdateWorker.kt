package me.calebjones.spacelaunchnow.workers

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import me.calebjones.spacelaunchnow.data.preferences.WidgetPreferences
import me.calebjones.spacelaunchnow.data.repository.LaunchRepository
import me.calebjones.spacelaunchnow.data.services.LaunchFilterService
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.util.logging.logger
import me.calebjones.spacelaunchnow.util.rethrowIfCancellation
import me.calebjones.spacelaunchnow.widgets.LaunchListWidget
import me.calebjones.spacelaunchnow.widgets.NextUpWidget
import me.calebjones.spacelaunchnow.widgets.WidgetLaunchSource
import me.calebjones.spacelaunchnow.widgets.WidgetRefreshPolicy
import org.koin.java.KoinJavaComponent.inject

/**
 * Runs every 15 minutes. It redraws the widgets, which is clock arithmetic over the cached list,
 * and first refreshes that list when [WidgetRefreshPolicy] says it is too old for how near the
 * next launch is. A failed refresh keeps the cached list and does not fail the redraw.
 */
class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val log = logger()

    override suspend fun doWork(): Result {
        return try {
            refreshLaunchesIfDue()

            // Update all instances of NextUpWidget
            NextUpWidget().updateAll(context)

            // Update all instances of LaunchListWidget
            LaunchListWidget().updateAll(context)

            log.i { "Widgets updated successfully" }
            Result.success()
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            log.e(e) { "Failed to update widgets: ${e.message}" }
            Result.retry()
        }
    }

    private suspend fun refreshLaunchesIfDue() {
        try {
            val glanceManager = GlanceAppWidgetManager(context)
            val widgetPreferences: WidgetPreferences by inject(WidgetPreferences::class.java)
            val needed = WidgetRefreshPolicy.widgetsNeedList(
                nextUpPlaced = glanceManager.getGlanceIds(NextUpWidget::class.java).isNotEmpty(),
                launchListPlaced = glanceManager.getGlanceIds(LaunchListWidget::class.java).isNotEmpty(),
                hasAccess = widgetPreferences.widgetAccessGrantedFlow.first()
            )
            if (!needed) return

            val launchRepository: LaunchRepository by inject(LaunchRepository::class.java)
            val notificationStateStorage: NotificationStateStorage by inject(NotificationStateStorage::class.java)
            val launchFilterService: LaunchFilterService by inject(LaunchFilterService::class.java)
            val state = notificationStateStorage.stateFlow.first()
            WidgetLaunchSource(launchRepository).refreshIfDue(
                agencyIds = launchFilterService.getAgencyIds(state),
                locationIds = launchFilterService.getLocationIds(state)
            )
        } catch (e: Exception) {
            e.rethrowIfCancellation()
            // The cached list still draws; the redraw goes on.
            log.e(e) { "Widget launch refresh failed" }
        }
    }
}
