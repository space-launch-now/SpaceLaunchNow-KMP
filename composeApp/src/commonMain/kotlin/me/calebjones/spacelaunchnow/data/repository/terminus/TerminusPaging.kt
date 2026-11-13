package me.calebjones.spacelaunchnow.data.repository.terminus

import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.domain.model.PaginatedResult

/** The most rows one Terminus list request may ask for (Tasks#1030). */
internal const val TERMINUS_MAX_PAGE_SIZE = 25

/**
 * Reads up to [limit] rows from a Terminus list in requests of at most [TERMINUS_MAX_PAGE_SIZE]
 * rows. A [limit] that fits one request makes exactly one request, as before. A larger [limit]
 * reads page after page until [limit] rows are in, `next` is null or a page comes back empty, so
 * the caller gets the same rows one large request would have given. The result keeps the first
 * page's `count` and `previous`, the last page's `next`, the first page's source and the oldest
 * page timestamp.
 */
internal suspend fun <T> terminusPagedList(
    limit: Int,
    fetchPage: suspend (pageLimit: Int, offset: Int) -> Result<DataResult<PaginatedResult<T>>>
): Result<DataResult<PaginatedResult<T>>> {
    if (limit <= TERMINUS_MAX_PAGE_SIZE) return fetchPage(limit, 0)

    val first = fetchPage(TERMINUS_MAX_PAGE_SIZE, 0).getOrElse { return Result.failure(it) }
    val rows = first.data.results.toMutableList()
    var last = first.data
    var oldest = first.timestamp
    var offset = TERMINUS_MAX_PAGE_SIZE
    while (last.next != null && last.results.isNotEmpty() && rows.size < limit) {
        val page = fetchPage(TERMINUS_MAX_PAGE_SIZE, offset).getOrElse { return Result.failure(it) }
        offset += TERMINUS_MAX_PAGE_SIZE
        rows += page.data.results
        last = page.data
        oldest = listOfNotNull(oldest, page.timestamp).minOrNull()
    }
    val merged = PaginatedResult(
        count = first.data.count,
        next = last.next,
        previous = first.data.previous,
        results = rows.take(limit)
    )
    return Result.success(DataResult(merged, first.source, oldest))
}
