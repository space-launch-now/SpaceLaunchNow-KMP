package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic
import me.calebjones.spacelaunchnow.data.notifications.PushMessaging
import me.calebjones.spacelaunchnow.data.storage.DebugPreferences
import me.calebjones.spacelaunchnow.util.BuildConfig

class SubscriptionProcessor(
    private val pushMessaging: PushMessaging,
    private val debugPreferences: DebugPreferences?,
    private val coroutineScope: CoroutineScope,
    private val debounceMs: Long = 300L,
    private val onSubscriptionUpdate: (Set<String>) -> Unit = {}
) {
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
        println("=== SubscriptionProcessor: Starting FCM update (v4 simple topics) ===")

        try {
            // Master switch: if notifications disabled, unsubscribe from all
            if (!state.enableNotifications) {
                println("Notifications disabled - unsubscribing from all topics")
                unsubscribeFromAll(state.subscribedTopics)
                onSubscriptionUpdate(emptySet()) // Update state with empty subscriptions
                return
            }

            // Calculate required topics based on state (v4: just version topic)
            val requiredTopics = calculateRequiredTopics(state)
            println("Required topics: $requiredTopics")

            // Update subscriptions
            val currentTopics = state.subscribedTopics
            val topicsToAdd = requiredTopics - currentTopics
            val topicsToRemove = currentTopics - requiredTopics

            println("Topics to add: $topicsToAdd")
            println("Topics to remove: $topicsToRemove")

            val actualSubscribedTopics = currentTopics.toMutableSet()

            // Subscribe to new topics
            topicsToAdd.forEach { topic ->
                val result = pushMessaging.subscribeToTopic(topic)
                if (result.isSuccess) {
                    println("✅ Subscribed to: $topic")
                    actualSubscribedTopics.add(topic)
                } else {
                    println("❌ Failed to subscribe to: $topic - ${result.exceptionOrNull()?.message}")
                }
            }

            // Unsubscribe from removed topics
            topicsToRemove.forEach { topic ->
                val result = pushMessaging.unsubscribeFromTopic(topic)
                if (result.isSuccess) {
                    println("✅ Unsubscribed from: $topic")
                    actualSubscribedTopics.remove(topic)
                } else {
                    println("❌ Failed to unsubscribe from: $topic - ${result.exceptionOrNull()?.message}")
                }
            }

            // Update repository with actual subscribed topics
            onSubscriptionUpdate(actualSubscribedTopics)

        } catch (e: Exception) {
            println("❌ SubscriptionProcessor error: ${e.message}")
            e.printStackTrace()
        }

        println("=== SubscriptionProcessor: Complete ===")
    }

    private suspend fun unsubscribeFromAll(topics: Set<String>) {
        topics.forEach { topic ->
            val result = pushMessaging.unsubscribeFromTopic(topic)
            if (result.isSuccess) {
                println("✅ Unsubscribed from: $topic")
            } else {
                println("❌ Failed to unsubscribe from: $topic")
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun calculateRequiredTopics(state: NotificationState): Set<String> {
        /**
         * v4 Notification System:
         * - Subscribe to ONLY the version topic (k_prod_v4 or k_debug_v4)
         * - Server sends ALL notifications to these topics with full data payload
         * - Client filters notifications based on user preferences (agency, location, timing, etc.)
         * 
         * This eliminates the complex topic subscription logic and moves filtering to the client
         */
        val topics = mutableSetOf<String>()

        // Only subscribe to version topic - all filtering is done client-side
        val versionTopic = getVersionTopic()
        topics.add(versionTopic)
        
        println("📡 v4 Simple Topics: Subscribing only to '$versionTopic' (client-side filtering enabled)")

        return topics
    }

    private suspend fun getVersionTopic(): String {
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
}