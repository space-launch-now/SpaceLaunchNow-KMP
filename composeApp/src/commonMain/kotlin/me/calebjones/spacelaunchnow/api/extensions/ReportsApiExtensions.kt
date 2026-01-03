package me.calebjones.spacelaunchnow.api.extensions

import me.calebjones.spacelaunchnow.api.snapi.apis.ReportsApi
import me.calebjones.spacelaunchnow.api.snapi.infrastructure.HttpResponse
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedReportList

/**
 * Extension functions for ReportsApi (SNAPI) to provide cleaner interfaces
 * for fetching space-related news reports
 */

/**
 * Get list of reports with filtering options
 *
 * Note: ordering parameter is disabled due to a bug in the generated API code
 * where enum values are not correctly converted to their string values.
 * The API defaults to newest-first ordering.
 *
 * @param limit Number of results per page (default 10)
 * @param offset Pagination offset
 * @param search Search term in title and summary
 * @param titleContains Filter by title content
 * @param summaryContains Filter by summary content
 * @param newsSite Filter by news site name
 * @return HttpResponse containing PaginatedReportList
 */
suspend fun ReportsApi.getReportsList(
    limit: Int? = 10,
    offset: Int? = null,
    search: String? = null,
    titleContains: String? = null,
    summaryContains: String? = null,
    newsSite: String? = null
): HttpResponse<PaginatedReportList> = reportsList(
    limit = limit,
    offset = offset,
    search = search,
    titleContains = titleContains,
    summaryContains = summaryContains,
    newsSite = newsSite,
    ordering = null, // Disabled due to generated code bug - API defaults to newest first
    publishedAtGt = null,
    publishedAtGte = null,
    publishedAtLt = null,
    publishedAtLte = null,
    summaryContainsAll = null,
    summaryContainsOne = null,
    titleContainsAll = null,
    titleContainsOne = null,
    updatedAtGt = null,
    updatedAtGte = null,
    updatedAtLt = null,
    updatedAtLte = null
)
