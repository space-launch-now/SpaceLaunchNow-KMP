package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.data.model.NotificationAgency
import me.calebjones.spacelaunchnow.data.model.NotificationLocation
import me.calebjones.spacelaunchnow.data.model.NotificationState
import me.calebjones.spacelaunchnow.data.model.NotificationTopic

/**
 * Pure derivation of the V6 topic set. No I/O, no suspend, no FCM, no database:
 * given settings, the exact set of FCM topics this device should hold. Under V6
 * the subscription IS the filter, so this function is the whole client-side
 * complexity of the scheme. Topic vocabulary is pinned by V6TopicContractTest;
 * assembly is pinned by V6TopicsTest against literal strings.
 */
object V6Topics {

    // Exactly the contract's 10 notificationTypes. NOT getUserConfigurableTopics():
    // that list also carries webcastOnly (the class modifier) and the three
    // broadcast toggles, none of which is a type topic.
    private val LAUNCH_TYPE_TOPICS: List<NotificationTopic> = listOf(
        NotificationTopic.TWENTY_FOUR_HOUR,
        NotificationTopic.ONE_HOUR,
        NotificationTopic.TEN_MINUTES,
        NotificationTopic.ONE_MINUTE,
        NotificationTopic.NETSTAMP_CHANGED,
        NotificationTopic.WEBCAST_LIVE,
        NotificationTopic.IN_FLIGHT,
        NotificationTopic.SUCCESS,
        NotificationTopic.FAILURE,
        NotificationTopic.PARTIAL_FAILURE,
    )

    // Persisted setting id -> wire token. featured_news/announcements differ:
    // the ids are persisted map keys (renaming would reset every user's toggle),
    // while the server only ever sends to the token form.
    private val BROADCAST_TOKENS: Map<NotificationTopic, String> = mapOf(
        NotificationTopic.EVENTS to "events",
        NotificationTopic.FEATURED_NEWS to "news",
        NotificationTopic.ANNOUNCEMENTS to "announce",
    )

    /** Exactly one class per device. Follow-all wins over strict, matching the UI. */
    fun audienceClass(state: NotificationState): String {
        val webcastOnly = state.isTopicEnabled(NotificationTopic.WEBCAST_ONLY)
        return when {
            state.followAllLaunches -> if (webcastOnly) "all_w" else "all"
            state.useStrictMatching -> if (webcastOnly) "strict_w" else "strict"
            else -> if (webcastOnly) "flex_w" else "flex"
        }
    }

    fun requiredTopics(state: NotificationState, env: String, platform: String): Set<String> {
        // Unsubscribing is the real kill switch now, not a local check.
        if (!state.enableNotifications) return emptySet()

        val audienceClass = audienceClass(state)
        val topics = mutableSetOf<String>()

        LAUNCH_TYPE_TOPICS.filter { state.isTopicEnabled(it) }.forEach { type ->
            topics += "v6_${env}_${platform}_${audienceClass}_${type.id}"
        }

        // Follow-all conditions are the type topic alone; attribute
        // subscriptions under follow-all are dead weight that would leave a
        // later switch out of follow-all starting dirty.
        if (!state.followAllLaunches) {
            state.subscribedAgencies.forEach { id ->
                NotificationAgency.getAll().firstOrNull { it.id.toString() == id }
                    ?.let { topics += "v6_${env}_${it.topicName}" }
            }
            state.subscribedLocations.forEach { id ->
                NotificationLocation.getAll().firstOrNull { it.id.toString() == id }
                    ?.let { topics += "v6_${env}_${it.topicName}" }
            }
        }

        BROADCAST_TOKENS.forEach { (topic, token) ->
            if (state.isTopicEnabled(topic)) topics += "v6_${env}_${platform}_${token}"
        }

        return topics
    }
}
