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
        println("=== SubscriptionProcessor: Starting FCM update ===")

        try {
            // Master switch: if notifications disabled, unsubscribe from all
            if (!state.enableNotifications) {
                println("Notifications disabled - unsubscribing from all topics")
                unsubscribeFromAll(state.subscribedTopics)
                onSubscriptionUpdate(emptySet()) // Update state with empty subscriptions
                return
            }

            // Calculate required topics based on state
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

    private suspend fun calculateRequiredTopics(state: NotificationState): Set<String> {
        val topics = mutableSetOf<String>()

        // Version topic (prod_v3 or debug_v3)
        val versionTopic = getVersionTopic()
        topics.add(versionTopic)

        // Base notification type
        topics.add(NotificationTopic.LAUNCHES_ALL.id)
        topics.add(NotificationTopic.EVENTS.id)
        topics.add(NotificationTopic.FEATURED_NEWS.id)

        // Follow all or matching strategy
        if (state.followAllLaunches) {
            topics.add(NotificationTopic.ALL_LAUNCHES.id)
        } else {
            if (state.useStrictMatching) {
                topics.add(NotificationTopic.STRICT_MATCHING.id)
            } else {
                topics.add(NotificationTopic.NOT_STRICT_MATCHING.id)
            }
        }

        // User-configurable timing topics
        state.topicSettings.forEach { (topicId, enabled) ->
            if (enabled) {
                topics.add(topicId)
            }
        }

        // Agency topics - now using topic names directly
        state.subscribedAgencies.forEach { agencyTopicName ->
            topics.add(agencyTopicName)
        }

        // Location topics - now using topic names directly
        state.subscribedLocations.forEach { locationTopicName ->
            topics.add(locationTopicName)
        }

        return topics
    }

    private suspend fun getVersionTopic(): String {
        return if (BuildConfig.IS_DEBUG && debugPreferences != null) {
            try {
                val debugSettings = debugPreferences.getDebugSettings()
                if (debugSettings.useDebugTopics) "debug_v3" else "prod_v3"
            } catch (e: Exception) {
                "prod_v3"
            }
        } else {
            "prod_v3"
        }
    }
}