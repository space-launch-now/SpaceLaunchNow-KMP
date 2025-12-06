package me.calebjones.spacelaunchnow.ui.viewmodel

import me.calebjones.spacelaunchnow.data.model.DataSource

/**
 * Generic view state container for any data section.
 * Enables 4-state rendering: Loading, Success, Error, Stale+Loading
 *
 * @param data The actual data (can be a single object, list, or null)
 * @param isLoading True if currently loading data
 * @param isUserInitiated True if the load was triggered by user action (pull-to-refresh/retry)
 * @param error Error message if the load failed, null otherwise
 * @param dataSource Where the data originated from (network, cache, or stale cache)
 * @param cacheTimestamp When the data was originally cached (epoch milliseconds)
 */
data class ViewState<T>(
    val data: T,
    val isLoading: Boolean = false,
    val isUserInitiated: Boolean = false,
    val error: String? = null,
    val dataSource: DataSource = DataSource.NETWORK,
    val cacheTimestamp: Long? = null
) {
    /**
     * True if this is a fresh load with no data yet.
     * Use for showing shimmer/skeleton loading states.
     */
    val isInitialLoading: Boolean get() = isLoading && isEmpty
    
    /**
     * True if data is empty/null.
     * Handles both nullable objects and lists.
     */
    val isEmpty: Boolean get() = when (data) {
        null -> true
        is List<*> -> data.isEmpty()
        else -> false
    }
    
    /**
     * True if showing stale/expired data.
     * Use for showing staleness indicators.
     */
    val isStale: Boolean get() = dataSource == DataSource.STALE_CACHE
    
    /**
     * True if there's an error and no data to show.
     * Use for showing error views.
     */
    val hasErrorWithNoData: Boolean get() = error != null && isEmpty
    
    /**
     * True if we have data to display (regardless of staleness or loading state).
     */
    val hasData: Boolean get() = !isEmpty
    
    companion object {
        /**
         * Create an initial empty state for nullable data.
         */
        fun <T> empty(): ViewState<T?> = ViewState<T?>(data = null)
        
        /**
         * Create an initial empty state for list data.
         */
        fun <T> emptyList(): ViewState<List<T>> = ViewState<List<T>>(data = kotlin.collections.emptyList())
    }
}
