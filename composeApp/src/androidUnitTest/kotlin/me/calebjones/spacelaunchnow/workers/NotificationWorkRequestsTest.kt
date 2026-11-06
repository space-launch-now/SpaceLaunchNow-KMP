package me.calebjones.spacelaunchnow.workers

import androidx.work.Constraints
import androidx.work.Data
import androidx.work.OutOfQuotaPolicy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NotificationWorkRequestsTest {

    private val input = Data.Builder()
        .putString("lsp_id", "121")
        .putString("fcm_title", "T-1 minute")
        .build()

    @Test
    fun requestIsExpeditedWithNonExpeditedFallback() {
        val spec = buildNotificationWorkRequest(input).workSpec

        assertTrue(spec.expedited)
        assertEquals(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST, spec.outOfQuotaPolicy)
    }

    @Test
    fun requestHasNoConstraintsAndNoDelay() {
        val spec = buildNotificationWorkRequest(input).workSpec

        assertEquals(Constraints.NONE, spec.constraints)
        assertEquals(0L, spec.initialDelay)
    }

    @Test
    fun requestKeepsInputData() {
        val spec = buildNotificationWorkRequest(input).workSpec

        assertEquals("121", spec.input.getString("lsp_id"))
        assertEquals("T-1 minute", spec.input.getString("fcm_title"))
    }

    @Test
    fun requestTargetsNotificationWorker() {
        val spec = buildNotificationWorkRequest(input).workSpec

        assertEquals(NotificationWorker::class.java.name, spec.workerClassName)
    }
}
