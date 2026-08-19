package me.calebjones.spacelaunchnow.ui.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.calebjones.spacelaunchnow.analytics.core.AnalyticsManagerImpl
import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.v6.V6ReconcileResult
import me.calebjones.spacelaunchnow.data.repository.FakeNotificationRepository
import me.calebjones.spacelaunchnow.data.repository.MockSubscriptionRepository
import me.calebjones.spacelaunchnow.data.storage.AppPreferences
import me.calebjones.spacelaunchnow.util.TestSpaceLoggerInit
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The unsaved-changes flag drives the bottom-anchored "Save & apply" bar. Under V6,
 * FCM reconciliation is save-triggered — so if this flag fails to rise, the user gets no
 * cue that their filter change hasn't reached FCM yet, which is exactly the "toggles look
 * applied but nothing subscribes" trap the bar exists to close.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelUnsavedChangesTest {

    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        TestSpaceLoggerInit.ensureInitialized()
        Dispatchers.setMain(testDispatcher)
        notificationRepository = FakeNotificationRepository()
        viewModel = SettingsViewModel(
            notificationRepository = notificationRepository,
            appSettingsViewModel = AppSettingsViewModel(
                AppPreferences(InMemoryPreferencesDataStore())
            ),
            subscriptionRepository = MockSubscriptionRepository(),
            analyticsManager = AnalyticsManagerImpl(emptyList())
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `starts with no unsaved changes`() = runTest {
        assertFalse(viewModel.hasUnsavedNotificationChanges.value)
    }

    @Test
    fun `every filter mutation raises the unsaved flag`() = runTest {
        viewModel.updateTopic(NotificationTopic.TEN_MINUTES, false)
        assertTrue(viewModel.hasUnsavedNotificationChanges.value, "topic toggle")

        resetFlagViaCleanSave()
        viewModel.toggleAgencySubscription(NotificationAgency.SPACEX)
        assertTrue(viewModel.hasUnsavedNotificationChanges.value, "agency toggle")

        resetFlagViaCleanSave()
        viewModel.updateFollowAllLaunches(true)
        assertTrue(viewModel.hasUnsavedNotificationChanges.value, "follow-all toggle")

        resetFlagViaCleanSave()
        viewModel.updateStrictMatching(true)
        assertTrue(viewModel.hasUnsavedNotificationChanges.value, "strict toggle")
    }

    @Test
    fun `clean save clears the flag`() = runTest {
        viewModel.updateTopic(NotificationTopic.SUCCESS, false)
        notificationRepository.nextReconcileResult = V6ReconcileResult(attempted = 5, failed = 0)

        viewModel.saveNotificationSettings()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.hasUnsavedNotificationChanges.value)
        assertEquals(1, notificationRepository.reconcileCalls)
    }

    @Test
    fun `partial reconcile failure keeps the flag up as the retry affordance`() = runTest {
        viewModel.updateTopic(NotificationTopic.SUCCESS, false)
        notificationRepository.nextReconcileResult = V6ReconcileResult(attempted = 10, failed = 2)

        viewModel.saveNotificationSettings()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(
            viewModel.hasUnsavedNotificationChanges.value,
            "the bar must stay so tapping Save again retries the failed rows"
        )

        // A later clean save clears it.
        notificationRepository.nextReconcileResult = V6ReconcileResult(attempted = 2, failed = 0)
        viewModel.saveNotificationSettings()
        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.hasUnsavedNotificationChanges.value)
    }

    @Test
    fun `skipped reconcile clears the flag - nothing will ever apply on this platform`() = runTest {
        viewModel.updateTopic(NotificationTopic.SUCCESS, false)
        notificationRepository.nextReconcileResult =
            V6ReconcileResult(attempted = 0, failed = 0, skipped = true)

        viewModel.saveNotificationSettings()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.hasUnsavedNotificationChanges.value)
    }

    @Test
    fun `the master kill switch does not raise the flag - it reconciles inline`() = runTest {
        viewModel.updateNotificationsEnabled(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(
            viewModel.hasUnsavedNotificationChanges.value,
            "kill switch applies immediately in the repository; showing Save would be a lie"
        )
    }

    private fun resetFlagViaCleanSave() {
        notificationRepository.nextReconcileResult = V6ReconcileResult(attempted = 1, failed = 0)
        viewModel.saveNotificationSettings()
        testDispatcher.scheduler.advanceUntilIdle()
        check(!viewModel.hasUnsavedNotificationChanges.value) { "flag should reset between cases" }
    }
}
