package me.calebjones.spacelaunchnow.data.notifications.v6

/** Records every call in order; fails topics on the deny-lists. */
class FakeTopicMessaging : TopicMessaging {
    val calls = mutableListOf<String>()          // "sub:<topic>" / "unsub:<topic>"
    var failSubscribes: Set<String> = emptySet()
    var failUnsubscribes: Set<String> = emptySet()
    var failureMessages: Map<String, String> = emptyMap()   // per-topic failure text; default "boom"

    override suspend fun subscribe(topic: String): Result<Unit> {
        calls += "sub:$topic"
        return if (topic in failSubscribes) Result.failure(Exception(failureMessages[topic] ?: "boom")) else Result.success(Unit)
    }

    override suspend fun unsubscribe(topic: String): Result<Unit> {
        calls += "unsub:$topic"
        return if (topic in failUnsubscribes) Result.failure(Exception(failureMessages[topic] ?: "boom")) else Result.success(Unit)
    }
}
