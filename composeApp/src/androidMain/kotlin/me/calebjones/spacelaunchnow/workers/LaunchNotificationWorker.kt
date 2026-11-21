package me.calebjones.spacelaunchnow.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import me.calebjones.spacelaunchnow.platform.mainActivityIntent
import me.calebjones.spacelaunchnow.shared.R
import me.calebjones.spacelaunchnow.util.logging.logger

class LaunchNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val log = logger()

    companion object {
        private const val CHANNEL_ID = "launch_notifications"
        private const val NOTIFICATION_ID = 2
    }

    override suspend fun doWork(): Result {
        try {
            createNotificationChannel()

            val launchName = inputData.getString("launch_name") ?: context.getString(R.string.launch_alert_default_name)
            val launchTime = inputData.getString("launch_time") ?: context.getString(R.string.launch_alert_default_time)
            val launchId = inputData.getString("launch_id")

            log.i { "Showing launch notification - name: $launchName, time: $launchTime, id: $launchId" }
            showLaunchNotification(launchName, launchTime, launchId)

            return Result.success()
        } catch (e: Exception) {
            log.e(e) { "Failed to show launch notification: ${e.message}" }
            return Result.failure()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.launch_alert_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.launch_alert_channel_description)
                enableVibration(true)
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showLaunchNotification(launchName: String, launchTime: String, launchId: String?) {
        val intent = mainActivityIntent(context).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            launchId?.let { putExtra("launch_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            launchId?.hashCode() ?: 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(context.getString(R.string.launch_alert_title))
            .setContentText(context.getString(R.string.launch_alert_text, launchName, launchTime))
            .setSmallIcon(me.calebjones.spacelaunchnow.shared.R.mipmap.ic_launcher_monochrome)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(launchId?.hashCode() ?: NOTIFICATION_ID, notification)
    }
}