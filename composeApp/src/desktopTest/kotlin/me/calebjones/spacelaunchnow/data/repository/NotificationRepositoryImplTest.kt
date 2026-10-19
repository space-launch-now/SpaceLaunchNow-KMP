package me.calebjones.spacelaunchnow.data.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.notifications.v6.ReconcileTrigger
import me.calebjones.spacelaunchnow.data.notifications.v6.TopicSubscriptionStore
import me.calebjones.spacelaunchnow.data.storage.NotificationStateStorage
import me.calebjones.spacelaunchnow.database.SpaceLaunchDatabase
import me.calebjones.spacelaunchnow.util.logging.LogConfig
import me.calebjones.spacelaunchnow.util.logging.PushDiagnostics
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import okio.Path.Companion.toPath
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Spec S3 D8: one push_summary per cold start, emitted after the app-start reconcile. */
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryImplTest {

    private class CapturingWriter : LogWriter() {
        val messages = mutableListOf<String>()
        override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
            messages += message
        }
    }

    private val writer = CapturingWriter()

    @BeforeTest
    fun setUp() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, listOf(writer)))
    }

    @AfterTest
    fun tearDown() {
        PushDiagnostics.reset()
        SpaceLogger.initialize(LogConfig(Severity.Verbose, emptyList()))
    }

    // Storage reads run on a real IO dispatcher, so virtual time alone does not drain the work.
    private fun TestScope.settle(realMillis: Long = 400) {
        val end = System.currentTimeMillis() + realMillis
        while (System.currentTimeMillis() < end) {
            runCurrent()
            Thread.sleep(10)
        }
    }

    private fun summaries() = writer.messages.filter { it.startsWith("Push registration summary") }

    private suspend fun TestScope.repository(platformName: String?): NotificationRepositoryImpl {
        val dir = Files.createTempDirectory("notif-repo-test")
        val storage = NotificationStateStorage(
            PreferenceDataStoreFactory.createWithPath(
                produceFile = { dir.resolve("test.preferences_pb").toString().toPath() },
            ),
        )
        storage.saveState(
            NotificationState(
                topicSettings = NotificationState.DEFAULT.topicSettings +
                    mapOf(NotificationTopic.ONE_HOUR.id to false, NotificationTopic.ONE_MINUTE.id to false),
            ),
        ).getOrThrow()
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        SpaceLaunchDatabase.Schema.create(driver)
        return NotificationRepositoryImpl(
            pushMessaging = PushMessaging(),
            storage = storage,
            topicSubscriptionStore = TopicSubscriptionStore(SpaceLaunchDatabase(driver)),
            repositoryScope = backgroundScope,
            workDispatcher = StandardTestDispatcher(testScheduler),
            platformName = platformName,
        )
    }

    @Test
    fun oneSummaryPerInitializeAfterTheAppStartReconcile() = runTest {
        PushDiagnostics.recordStartupRecordsReady()
        val repo = repository("android")

        repo.initialize()
        settle()
        advanceTimeBy(20_000)
        settle()

        val lines = summaries()
        assertEquals(1, lines.size, lines.toString())
        val line = lines.single()
        assertTrue(line.contains("push.reconcile_state=complete"), line)
        assertTrue(line.contains("push.audience_class=flex"), line)
        assertTrue(line.contains("push.enabled_types=twentyFourHour,tenMinutes,success,failure,partial_failure"), line)
        assertTrue(!line.contains("push.subscribed_topic_count=0 "), line)
    }

    @Test
    fun summaryWaitsForStartupRecordsThenEmitsOnce() = runTest {
        val repo = repository("android")

        repo.initialize()
        settle()
        advanceTimeBy(5_000)
        settle()
        assertEquals(0, summaries().size)

        PushDiagnostics.recordTokenSuccess("abcdefghijklmnop")
        PushDiagnostics.recordStartupRecordsReady()
        settle()
        advanceTimeBy(20_000)
        settle()

        val line = summaries().single()
        assertTrue(line.contains("push.token_present=true"), line)
        assertTrue(line.contains("push.reconcile_state=complete"), line)
    }

    @Test
    fun fallbackEmitsPendingAfter15SecondsWhenNoReconcileCompletes() = runTest {
        val repo = repository(platformName = null) // desktop: the reconcile is skipped

        repo.initialize()
        settle()
        advanceTimeBy(14_000)
        settle()
        assertEquals(0, summaries().size)

        advanceTimeBy(2_000)
        settle()
        val line = summaries().single()
        assertTrue(line.contains("push.reconcile_state=pending"), line)
        assertTrue(line.contains("push.audience_class=flex"), line)
    }

    @Test
    fun fallbackEmitsPendingWhenStartupRecordsNeverLand() = runTest {
        val repo = repository("android")

        repo.initialize()
        settle()
        advanceTimeBy(30_000)
        settle()

        val line = summaries().single()
        assertTrue(line.contains("push.reconcile_state=pending"), line)
    }

    @Test
    fun earlyTokenRefreshReconcileEmitsNoSummary() = runTest {
        PushDiagnostics.recordStartupRecordsReady()
        val repo = repository("android")

        repo.reconcileSubscriptions(ReconcileTrigger.TOKEN_REFRESH)
        settle()
        advanceTimeBy(30_000)
        settle()

        assertEquals(0, summaries().size)
        val reconcile = writer.messages.single { it.startsWith("[EVENT] push_reconcile") }
        assertTrue(reconcile.contains("push.reconcile_state=complete"), reconcile)
        assertTrue(reconcile.contains("push.enabled_types=twentyFourHour"), reconcile)
    }
}
