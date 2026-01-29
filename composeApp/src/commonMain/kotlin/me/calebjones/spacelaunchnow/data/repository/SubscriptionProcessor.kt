package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.PlatformType
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.getPlatform
import me.calebjones.spacelaunchnow.util.BuildConfig
import me.calebjones.spacelaunchnow.util.logging.logger

@OptIn(FlowPreview::class)
class SubscriptionProcessor(
    private val pushMessaging: PushMessaging,
    private val debugPreferences: DebugPreferences?,
    private val coroutineScope: CoroutineScope,
    private val debounceMs: Long = 300L,
    private val onSubscriptionUpdate: (Set<String>) -> Unit = {}
) {
    private val log = logger()
    private val updateRequests = Channel<NotificationState>(Channel.CONFLATED)

    init {
        coroutineScope.launch {
            updateRequests.receiveAsFlow()
                .debounce(debounceMs)
                .collect { state ->
                    updateFCMSubscriptions(state)
                }
        }
    }

    fun requestUpdate(state: NotificationState) {
        updateRequests.trySend(state)
    }

    private suspend fun updateFCMSubscriptions(state: NotificationState) {
        log.d { "=== SubscriptionProcessor: Starting FCM update (v5 topics only) ===" }

        try {
            // Master switch: if notifications disabled, unsubscribe from all
            if (!state.enableNotifications) {
                log.d { "Notifications disabled - unsubscribing from all topics" }
                unsubscribeFromAll(state.subscribedTopics)
                onSubscriptionUpdate(emptySet()) // Update state with empty subscriptions
                return
            }

            // Calculate required topics based on state (v4: just version topic)
            val requiredTopics = calculateRequiredTopics(state)
            log.d { "Required topics: $requiredTopics" }

            // Update subscriptions
            val currentTopics = state.subscribedTopics
            val topicsToAdd = requiredTopics - currentTopics
            val topicsToRemove = currentTopics - requiredTopics

            log.d { "Topics to add: $topicsToAdd" }
            log.d { "Topics to remove: $topicsToRemove" }

            val actualSubscribedTopics = currentTopics.toMutableSet()

            // Subscribe to new topics
            topicsToAdd.forEach { topic ->
                val result = pushMessaging.subscribeToTopic(topic)
                if (result.isSuccess) {
                    log.i { "✅ Subscribed to: $topic" }
                    actualSubscribedTopics.add(topic)
                } else {
                    log.w { "❌ Failed to subscribe to: $topic - ${result.exceptionOrNull()?.message}" }
                }
            }

            // Unsubscribe from removed topics
            topicsToRemove.forEach { topic ->
                val result = pushMessaging.unsubscribeFromTopic(topic)
                if (result.isSuccess) {
                    log.i { "✅ Unsubscribed from: $topic" }
                    actualSubscribedTopics.remove(topic)
                } else {
                    log.w { "❌ Failed to unsubscribe from: $topic - ${result.exceptionOrNull()?.message}" }
                }
            }

            // Update repository with actual subscribed topics
            onSubscriptionUpdate(actualSubscribedTopics)

        } catch (e: Exception) {
            log.e(e) { "❌ SubscriptionProcessor error: ${e.message}" }
        }

        log.d { "=== SubscriptionProcessor: Complete ===" }
    }

    private suspend fun unsubscribeFromAll(topics: Set<String>) {
        topics.forEach { topic ->
            val result = pushMessaging.unsubscribeFromTopic(topic)
            if (result.isSuccess) {
                log.i { "✅ Unsubscribed from: $topic" }
            } else {
                log.w { "❌ Failed to unsubscribe from: $topic" }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun calculateRequiredTopics(state: NotificationState): Set<String> {
        /**
         * v5 Notification System:
         * - Subscribe ONLY to V5 platform-specific topic (prod_v5_android/prod_v5_ios)
         * - V5 provides extended filtering with lsp_id, location_id, program_ids, etc.
         * - Automatically unsubscribes from old V4 topics (k_prod_v4, k_debug_v4)
         * - All filtering is done client-side with extended IDs
         */
        val topics = mutableSetOf<String>()

        // V5 topic - platform-specific with extended filtering
        val v5Topic = getV5VersionTopic()
        topics.add(v5Topic)
        log.d { "📡 v5 Topic: '$v5Topic' (client-side filtering with extended IDs)" }
        log.d { "🔄 Migrating: Old v4 topics (k_prod_v4, k_debug_v4) will be unsubscribed automatically" }

        return topics
    }

    private suspend fun getV4VersionTopic(): String {
        return if (BuildConfig.IS_DEBUG && debugPreferences != null) {
            try {
                val debugSettings = debugPreferences.getDebugSettings()
                if (debugSettings.useDebugTopics) "k_debug_v4" else "k_prod_v4"
            } catch (e: Exception) {
                "k_prod_v4"
            }
        } else {
            "k_prod_v4"
        }
    }

    private suspend fun getV5VersionTopic(): String {
        val isDebug = if (BuildConfig.IS_DEBUG && debugPreferences != null) {
            try {
                val debugSettings = debugPreferences.getDebugSettings()
                debugSettings.useDebugTopics
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }

        val platform = getPlatform()
        return getV5Topic(platform.type, isDebug)
    }

    private fun getV5Topic(platformType: PlatformType, isDebug: Boolean): String {
        val prefix = if (isDebug) "debug" else "prod"
        return when (platformType) {
            PlatformType.ANDROID -> "${prefix}_v5_android"
            PlatformType.IOS -> "${prefix}_v5_ios"
            PlatformType.DESKTOP -> "${prefix}_v5_desktop" // Placeholder, desktop doesn't use notifications
        }
    }
}