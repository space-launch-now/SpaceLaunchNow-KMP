package me.calebjones.spacelaunchnow.api.extensions

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Rounds time filters so that all clients build the same request URL inside one bucket.
 *
 * A filter sent to the microsecond (`net__gt=2026-10-01T14:14:12.895063Z`) makes every request
 * a new URL, so no cache (Cloudflare, the nginx ingress, django-cachalot) can serve it.
 * Lower bounds round down and upper bounds round up, so a filter window only gets wider,
 * by less than one bucket.
 */
@OptIn(ExperimentalTime::class)
object TimeFilterRounding {
    /** Bucket size. Matches the nginx ingress cache TTL. */
    val BUCKET: Duration = 5.minutes

    /** Rounds down to the start of the bucket. Use for lower bounds (`__gt`, `__gte`). */
    fun floor(instant: Instant, bucket: Duration = BUCKET): Instant {
        val bucketSeconds = bucket.inWholeSeconds
        return Instant.fromEpochSeconds(instant.epochSeconds.floorDiv(bucketSeconds) * bucketSeconds)
    }

    /** Rounds up to the end of the bucket. Use for upper bounds (`__lt`, `__lte`). */
    fun ceil(instant: Instant, bucket: Duration = BUCKET): Instant {
        val floor = floor(instant, bucket)
        return if (floor == instant) floor else floor + bucket
    }
}
