package me.calebjones.spacelaunchnow.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

/**
 * Rows after which an inline ad slot goes in a paged list.
 *
 * One slot after row [firstSlotAfter] (or after the last row when the list is shorter), then one
 * after each full page: rows [pageSize], 2 * [pageSize] and so on, at most [maxPageSlots] of them.
 * A page-end slot shows only once the list holds that many rows, so the rows of the next page
 * load after the slot and the slot stays between the pages.
 *
 * Returns ascending row counts without duplicates. Empty for an empty list.
 */
fun inlineAdSlotRows(
    itemCount: Int,
    pageSize: Int,
    firstSlotAfter: Int,
    maxPageSlots: Int
): List<Int> {
    if (itemCount <= 0) return emptyList()
    val rows = mutableListOf(minOf(firstSlotAfter, itemCount))
    for (page in 1..maxPageSlots) {
        val row = pageSize * page
        if (row > itemCount) break
        rows.add(row)
    }
    return rows.distinct().sorted()
}

/**
 * Banner handlers for the inline ad slots of one screen, kept above the lazy list.
 *
 * A handler's AdView can have only one parent, so every slot needs its own handler. Handlers
 * live here instead of in the list items so that scrolling a slot off screen and back reuses the
 * same ad instead of requesting a new one. Each handler is created the first time its slot
 * composes. [release] drops them all.
 *
 * Handlers are opaque here: the platform SmartBannerAd creates and drives them.
 */
class InlineAdSlots {
    private val handlers = mutableMapOf<Any, Any>()

    /** The slot for [key], for example the tab name paired with the slot number. */
    fun slot(key: Any): InlineAdSlot = InlineAdSlot(this, key)

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Any> handlerFor(key: Any, create: () -> T): T =
        handlers.getOrPut(key, create) as T

    /** Destroys every handler and forgets its refresh and retry state. */
    fun release() {
        handlers.values.forEach { handler ->
            BannerRefreshTracker.forget(handler)
            BannerRetryPolicy.shared.forget(handler)
            destroyBannerHandler(handler)
        }
        handlers.clear()
    }
}

/** One slot of an [InlineAdSlots]; pass it to [SmartBannerAd]. */
class InlineAdSlot internal constructor(
    private val slots: InlineAdSlots,
    private val key: Any
) {
    internal fun <T : Any> handler(create: () -> T): T = slots.handlerFor(key, create)
}

/** Remembers an [InlineAdSlots] and releases its handlers when the caller leaves composition. */
@Composable
fun rememberInlineAdSlots(): InlineAdSlots {
    val slots = remember { InlineAdSlots() }
    DisposableEffect(slots) {
        onDispose { slots.release() }
    }
    return slots
}

/** Platform teardown of a banner handler made by SmartBannerAd. */
internal expect fun destroyBannerHandler(handler: Any)
