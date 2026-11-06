package me.calebjones.spacelaunchnow.workers

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy

/**
 * Builds the work request that turns one FCM push into a notification.
 *
 * Expedited so Doze and app-standby limits do not hold a timed push until the next
 * maintenance window. When the expedited quota is spent, the request runs as ordinary
 * work: late, but never dropped. Expedited work must not carry constraints or a delay.
 */
fun buildNotificationWorkRequest(data: Data): OneTimeWorkRequest =
    OneTimeWorkRequestBuilder<NotificationWorker>()
        .setInputData(data)
        .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        .build()
