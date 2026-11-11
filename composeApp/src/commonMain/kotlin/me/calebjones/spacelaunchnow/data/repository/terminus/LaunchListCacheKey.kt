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
