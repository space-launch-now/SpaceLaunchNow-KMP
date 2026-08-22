package me.calebjones.spacelaunchnow.data.notifications

import kotlin.concurrent.Volatile
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import me.calebjones.spacelaunchnow.data.storage.NotificationHistoryStorage
import me.calebjones.spacelaunchnow.util.logging.SpaceLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSNotificationName

/**
 * Bridge that coordinates FCM operations between Kotlin and Swift.
 *
 * Protocol -- strictly one request at a time:
 * 1. Kotlin takes [requestMutex], parks the caller's continuation, publishes
 *    [pendingOperation] / [lastRequestedTopic] / [currentRequestId], and posts the
 *    "KotlinFCMRequestPending" NSNotification.
 * 2. Swift FCMBridge reads those properties, performs the Firebase call, and calls
 *    the matching provide*() function, echoing the request id it served.
 * 3. Kotlin resumes the continuation. A result carrying a stale request id is
 *    ignored, so a late completion can never resolve a later request.
 *
 * Every request completes. Firebase success or failure is delivered as-is, and a
 * completion that never arrives is failed by [REQUEST_TIMEOUT_MS]. A failure must
 * never be held back here: the V6 reconciler retries failed topics on its next
 * pass, but it cannot retry a call that never returns. (An earlier version kept
 * "APNS"-flavoured errors pending, waiting for APNs registration. On a cold
 * launch of an already-authorized device that registration never came, and the
 * reconciler hung forever -- mutex held -- on its first legacy unsubscribe.)
 *
 * Communication uses NSNotificationCenter to avoid cinterop complexity.
 * Swift FCMBridge listens for "KotlinFCMRequestPending" notifications.
 */
object IosPushMessagingBridge : KoinComponent {
    private val log = SpaceLogger.getLogger("IosPushMessagingBridge")

    private val historyStorage: NotificationHistoryStorage by inject()

    // Coroutine scope for async operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Notification name that Swift FCMBridge listens for
    private val KOTLIN_FCM_REQUEST_NOTIFICATION: NSNotificationName = "KotlinFCMRequestPending"

    /**
     * Upper bound on one Firebase call. Topic operations normally finish well
     * under a second; this only catches a completion that never comes back and
     * turns it into a failure the reconciler can retry.
     */
    private const val REQUEST_TIMEOUT_MS = 30_000L

    // Swift holds exactly one in-flight request (one topic, one result slot). The
    // lock makes the same invariant hold on the Kotlin side, so concurrent callers
    // queue instead of overwriting each other's slot.
    private val requestMutex = Mutex()
    private var requestSeq = 0L

    // Exactly one of these is non-null while a request is pending.
    @Volatile private var tokenContinuation: CancellableContinuation<Result<String>>? = null
    @Volatile private var topicContinuation: CancellableContinuation<Result<Unit>>? = null

    // Request state -- Swift reads these
    @Volatile var lastRequestedTopic: String? = null
        private set

    @Volatile var pendingOperation: Operation = Operation.NONE
        private set

    /** Monotonic id of the current request. Swift echoes it back in provide*(). */
    @Volatile var currentRequestId: Long = 0L
        private set

    enum class Operation {
        NONE,
        GET_TOKEN,
        SUBSCRIBE,
        UNSUBSCRIBE
    }

    /**
     * Post a notification to trigger Swift FCMBridge.processPendingKotlinRequests()
     * Swift's FCMBridge is already set up to listen for "KotlinFCMRequestPending" notifications.
     */
    private fun notifySwift() {
        log.d { "Posting NSNotification to trigger Swift FCMBridge" }
        platform.darwin.dispatch_async(platform.darwin.dispatch_get_main_queue()) {
            NSNotificationCenter.defaultCenter.postNotificationName(
                aName = KOTLIN_FCM_REQUEST_NOTIFICATION,
                `object` = null
            )
        }
    }

    // ===== Functions called by Kotlin (IosPushMessaging) =====

    suspend fun getToken(): Result<String> =
        request(Operation.GET_TOKEN, topic = null) { tokenContinuation = it }

    suspend fun subscribe(topic: String): Result<Unit> =
        request(Operation.SUBSCRIBE, topic) { topicContinuation = it }

    suspend fun unsubscribe(topic: String): Result<Unit> =
        request(Operation.UNSUBSCRIBE, topic) { topicContinuation = it }

    private suspend fun <T> request(
        operation: Operation,
        topic: String?,
        park: (CancellableContinuation<Result<T>>) -> Unit,
    ): Result<T> = requestMutex.withLock {
        val requestId = ++requestSeq
        val label = if (topic == null) operation.name else "${operation.name} $topic"
        log.i { "Requesting $label from Swift (request $requestId)" }
        val outcome = withTimeoutOrNull(REQUEST_TIMEOUT_MS) {
            suspendCancellableCoroutine<Result<T>> { cont ->
                park(cont)
                lastRequestedTopic = topic
                currentRequestId = requestId
                pendingOperation = operation
                cont.invokeOnCancellation { clearPending(requestId) }
                notifySwift()
            }
        }
        outcome ?: run {
            log.e { "$label timed out after ${REQUEST_TIMEOUT_MS}ms (request $requestId); reported as failure" }
            clearPending(requestId)
            Result.failure<T>(IllegalStateException("FCM $label timed out"))
        }
    }

    /** Drop the request state for [requestId] -- a no-op if a newer request has taken the slot. */
    private fun clearPending(requestId: Long) {
        if (requestId != currentRequestId) return
        tokenContinuation = null
        topicContinuation = null
        lastRequestedTopic = null
        pendingOperation = Operation.NONE
    }

    // ===== Functions called by Swift (FCMBridge) =====

    /**
     * Swift calls this with the FCM token -- or the error -- for [requestId].
     */
    fun provideToken(requestId: Long, token: String?, errorMessage: String?) {
        val cont = take(requestId, "token", tokenContinuation) ?: return
        if (token != null) {
            log.d { "Swift provided FCM token: ${token.take(20)}..." }
            cont.resume(Result.success(token))
        } else {
            log.w { "FCM token request failed: ${errorMessage ?: "unknown error"}" }
            cont.resume(Result.failure(Exception(errorMessage ?: "Failed to get FCM token")))
        }
    }

    /**
     * Swift calls this after attempting to subscribe to a topic for [requestId].
     */
    fun provideSubscribeResult(requestId: Long, errorMessage: String?) =
        completeTopic(requestId, "subscribe", errorMessage)

    /**
     * Swift calls this after attempting to unsubscribe from a topic for [requestId].
     */
    fun provideUnsubscribeResult(requestId: Long, errorMessage: String?) =
        completeTopic(requestId, "unsubscribe", errorMessage)

    private fun completeTopic(requestId: Long, kind: String, errorMessage: String?) {
        val topic = lastRequestedTopic
        val cont = take(requestId, kind, topicContinuation) ?: return
        if (errorMessage == null) {
            log.d { "Swift provided $kind result for $topic: success" }
            cont.resume(Result.success(Unit))
        } else {
            log.w { "FCM $kind failed for topic $topic: $errorMessage" }
            cont.resume(Result.failure(Exception(errorMessage)))
        }
    }

    /**
     * Validate a Swift result against the current request and detach its
     * continuation. Returns null (already logged) for a stale id or an empty slot,
     * so a request can never be resumed twice.
     */
    private fun <T> take(
        requestId: Long,
        kind: String,
        cont: CancellableContinuation<T>?,
    ): CancellableContinuation<T>? {
        if (requestId != currentRequestId) {
            log.w { "Ignoring stale $kind result for request $requestId (current is $currentRequestId)" }
            return null
        }
        if (cont == null) {
            log.w { "Received $kind result for request $requestId but no continuation is registered" }
            return null
        }
        clearPending(requestId)
        return cont
    }

    // ===== Notification History Support =====

    /**
     * Swift calls this to save a notification to history
     * This is called from AppDelegate when a notification is received
     */
    fun saveNotificationToHistory(
        notificationType: String,
        launchId: String?,
        launchUuid: String?,
        launchName: String?,
        launchImage: String?,
        launchNet: String?,
        launchLocation: String?,
        webcast: String?,
        webcastLive: String?,
        agencyId: String?,
        locationId: String?,
        displayedTitle: String?,
        displayedBody: String?,
        rawDataKeys: List<String>,
        rawDataValues: List<String>,
        wasFiltered: Boolean,
        filterReason: String?,
        wasShown: Boolean
    ) {
        // Convert parallel arrays to map
        val rawData = rawDataKeys.zip(rawDataValues).toMap()

        log.i { "📝 Saving notification to history:" }
        log.i { "  Launch: $launchName" }
        log.i { "  Filtered: $wasFiltered" }
        log.i { "  Shown: $wasShown" }
        log.i { "  Filter Reason: ${filterReason ?: "none"}" }
        log.i { "  Raw Data JSON: $rawData" }

        scope.launch {
            try {
                historyStorage.addNotification(
                    notificationType = notificationType,
                    launchId = launchId,
                    launchUuid = launchUuid,
                    launchName = launchName,
                    launchImage = launchImage,
                    launchNet = launchNet,
                    launchLocation = launchLocation,
                    webcast = webcast,
                    webcastLive = webcastLive,
                    agencyId = agencyId,
                    locationId = locationId,
                    displayedTitle = displayedTitle,
                    displayedBody = displayedBody,
                    rawData = rawData,
                    wasFiltered = wasFiltered,
                    filterReason = filterReason,
                    wasShown = wasShown
                )
                log.i { "✅ Saved notification to history: $launchName (filtered=$wasFiltered, shown=$wasShown)" }
            } catch (e: Exception) {
                log.e { "❌ Failed to save notification to history: ${e.message}" }
            }
        }
    }
}
