package me.calebjones.spacelaunchnow.data.notifications.v6

import me.calebjones.spacelaunchnow.data.notifications.PushMessaging

/**
 * The reconciler's FCM boundary. PushMessaging is an `expect class`, which a
 * common-source fake cannot implement -- this interface exists so tests can
 * record and order calls. PushMessaging itself needs no new methods (spec:
 * subscriptions bind to the installation, not the token; the existing three
 * methods suffice).
 */
interface TopicMessaging {
    suspend fun subscribe(topic: String): Result<Unit>
    suspend fun unsubscribe(topic: String): Result<Unit>
}

class PushTopicMessaging(private val pushMessaging: PushMessaging) : TopicMessaging {
    override suspend fun subscribe(topic: String): Result<Unit> =
        pushMessaging.subscribeToTopic(topic)

    override suspend fun unsubscribe(topic: String): Result<Unit> =
        pushMessaging.unsubscribeFromTopic(topic)
}
