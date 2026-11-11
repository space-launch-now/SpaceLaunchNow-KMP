package me.calebjones.spacelaunchnow.data.repository.terminus

import kotlin.test.Test
import kotlin.test.assertEquals

/** [LaunchListCacheKey] and the key builders against literal strings, so a format change shows up here. */
class LaunchListCacheKeyTest {

    @Test
    fun encodeKeepsUnreservedCharactersAndEscapesTheRest() {
        assertEquals("rocket-1._~", LaunchListCacheKey.encode("rocket-1._~"))
        assertEquals("a%20b%26c%3Dd%2Ce%25f%3Ag", LaunchListCacheKey.encode("a b&c=d,e%f:g"))
    }

    @Test
    fun shortSearchIsJustEncoded() {
        assertEquals("falcon%209", LaunchListCacheKey.search("falcon 9"))
        assertEquals("a".repeat(64), LaunchListCacheKey.search("a".repeat(64)))
    }

    @Test
    fun longSearchIsLengthPlusFnvHash() {
        assertEquals("h70:505e1ef28863accb", LaunchListCacheKey.search("a".repeat(70)))
    }

    @Test
    fun terminusCacheKeySkipsNullPartsAndKeepsOrder() {
        assertEquals(
            "kind?b=2&a=1",
            terminusCacheKey("kind", "b" to 2, "skip" to null, "a" to 1)
        )
    }

    @Test
    fun launchListQueryKeyIsALiteralString() {
        assertEquals(
            "limit=3&upcoming=true&ordering=net",
            LaunchListQuery.of(limit = 3, upcoming = true, ordering = "net").key
        )
        assertEquals(
            "limit=5&offset=10&search=falcon%209&lsp__id=1,4",
            LaunchListQuery.of(limit = 5, offset = 10, search = "falcon 9", lspId = listOf(4, 1)).key
        )
    }

    @Test
    fun normalizedIdsSortsAndDeduplicates() {
        assertEquals(listOf(1, 4), listOf(4, 1, 4).normalizedIds())
        assertEquals(null, emptyList<Int>().normalizedIds())
    }
}
