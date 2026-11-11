package me.calebjones.spacelaunchnow.data.repository.terminus

import io.ktor.http.encodeURLParameter

/** Text pieces of a [LaunchListQuery.key]: percent-encoding and the short form of a long search. */
internal object LaunchListCacheKey {
    private const val MAX_KEY_SEARCH_CHARS = 64

    /** Percent-encodes everything but ASCII letters, digits and `-._~`, so `& = , % :` never appear raw. */
    fun encode(value: String): String = value.encodeURLParameter()

    /** The encoded search, or `h<length>:<FNV-1a 64 hash>` when it is long. `:` never appears encoded. */
    fun search(search: String): String {
        val encoded = encode(search)
        if (encoded.length <= MAX_KEY_SEARCH_CHARS) return encoded
        var hash = -0x340d631b7bdddcdbL
        for (byte in search.encodeToByteArray()) {
            hash = (hash xor (byte.toLong() and 0xFF)) * 0x100000001b3L
        }
        return "h${search.length}:${hash.toULong().toString(16)}"
    }
}

/**
 * Cache key of one request: the kind, then every set part in the given order. Values are
 * percent-encoded, so no value can forge another part. A search is encoded (long ones hashed) by
 * the caller through [LaunchListCacheKey.search]. Never log a key: it holds the search text.
 */
internal fun terminusCacheKey(kind: String, vararg parts: Pair<String, Any?>): String =
    kind + "?" + parts.filter { it.second != null }.joinToString("&") { "${it.first}=${it.second}" }

internal fun List<Int>?.normalizedIds(): List<Int>? = this?.takeIf { it.isNotEmpty() }?.distinct()?.sorted()
