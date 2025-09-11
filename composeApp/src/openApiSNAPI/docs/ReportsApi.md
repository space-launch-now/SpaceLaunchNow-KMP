# ReportsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**reportsList**](ReportsApi.md#reportsList) | **GET** /v4/reports/ |  |
| [**reportsRetrieve**](ReportsApi.md#reportsRetrieve) | **GET** /v4/reports/{id}/ |  |


<a id="reportsList"></a>
# **reportsList**
> PaginatedReportList reportsList(limit, newsSite, newsSiteExclude, offset, ordering, publishedAtGt, publishedAtGte, publishedAtLt, publishedAtLte, search, summaryContains, summaryContainsAll, summaryContainsOne, titleContains, titleContainsAll, titleContainsOne, updatedAtGt, updatedAtGte, updatedAtLt, updatedAtLte)



### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.snapi.infrastructure.*
//import me.calebjones.spacelaunchnow.api.snapi.models.*

val apiInstance = ReportsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val newsSite : kotlin.String = newsSite_example // kotlin.String | Search for documents with a news_site__name present in a list of comma-separated values. Case insensitive.
val newsSiteExclude : kotlin.String = newsSiteExclude_example // kotlin.String | Search for documents with a news_site__name not present in a list of comma-separated values. Case insensitive.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Order the result on `published_at, -published_at, updated_at, -updated_at`.  * `published_at` - Published at * `-published_at` - Published at (descending) * `updated_at` - Updated at * `-updated_at` - Updated at (descending)
val publishedAtGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents published after a given ISO8601 timestamp (excluded).
val publishedAtGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents published after a given ISO8601 timestamp (included).
val publishedAtLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents published before a given ISO8601 timestamp (excluded).
val publishedAtLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents published before a given ISO8601 timestamp (included).
val search : kotlin.String = search_example // kotlin.String | Search for documents with a specific phrase in the title or summary.
val summaryContains : kotlin.String = summaryContains_example // kotlin.String | Search for all documents with a specific phrase in the summary.
val summaryContainsAll : kotlin.String = summaryContainsAll_example // kotlin.String | Search for documents with a summary containing all keywords from comma-separated values.
val summaryContainsOne : kotlin.String = summaryContainsOne_example // kotlin.String | Search for documents with a summary containing at least one keyword from comma-separated values.
val titleContains : kotlin.String = titleContains_example // kotlin.String | Search for all documents with a specific phrase in the title.
val titleContainsAll : kotlin.String = titleContainsAll_example // kotlin.String | Search for documents with a title containing all keywords from comma-separated values.
val titleContainsOne : kotlin.String = titleContainsOne_example // kotlin.String | Search for documents with a title containing at least one keyword from comma-separated values.
val updatedAtGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents updated after a given ISO8601 timestamp (excluded).
val updatedAtGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents updated after a given ISO8601 timestamp (included).
val updatedAtLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents updated before a given ISO8601 timestamp (excluded).
val updatedAtLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Get all documents updated before a given ISO8601 timestamp (included).
try {
    val result : PaginatedReportList = apiInstance.reportsList(limit, newsSite, newsSiteExclude, offset, ordering, publishedAtGt, publishedAtGte, publishedAtLt, publishedAtLte, search, summaryContains, summaryContainsAll, summaryContainsOne, titleContains, titleContainsAll, titleContainsOne, updatedAtGt, updatedAtGte, updatedAtLt, updatedAtLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ReportsApi#reportsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ReportsApi#reportsList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **newsSite** | **kotlin.String**| Search for documents with a news_site__name present in a list of comma-separated values. Case insensitive. | [optional] |
| **newsSiteExclude** | **kotlin.String**| Search for documents with a news_site__name not present in a list of comma-separated values. Case insensitive. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Order the result on &#x60;published_at, -published_at, updated_at, -updated_at&#x60;.  * &#x60;published_at&#x60; - Published at * &#x60;-published_at&#x60; - Published at (descending) * &#x60;updated_at&#x60; - Updated at * &#x60;-updated_at&#x60; - Updated at (descending) | [optional] [enum: -published_at, -updated_at, published_at, updated_at] |
| **publishedAtGt** | **kotlinx.datetime.Instant**| Get all documents published after a given ISO8601 timestamp (excluded). | [optional] |
| **publishedAtGte** | **kotlinx.datetime.Instant**| Get all documents published after a given ISO8601 timestamp (included). | [optional] |
| **publishedAtLt** | **kotlinx.datetime.Instant**| Get all documents published before a given ISO8601 timestamp (excluded). | [optional] |
| **publishedAtLte** | **kotlinx.datetime.Instant**| Get all documents published before a given ISO8601 timestamp (included). | [optional] |
| **search** | **kotlin.String**| Search for documents with a specific phrase in the title or summary. | [optional] |
| **summaryContains** | **kotlin.String**| Search for all documents with a specific phrase in the summary. | [optional] |
| **summaryContainsAll** | **kotlin.String**| Search for documents with a summary containing all keywords from comma-separated values. | [optional] |
| **summaryContainsOne** | **kotlin.String**| Search for documents with a summary containing at least one keyword from comma-separated values. | [optional] |
| **titleContains** | **kotlin.String**| Search for all documents with a specific phrase in the title. | [optional] |
| **titleContainsAll** | **kotlin.String**| Search for documents with a title containing all keywords from comma-separated values. | [optional] |
| **titleContainsOne** | **kotlin.String**| Search for documents with a title containing at least one keyword from comma-separated values. | [optional] |
| **updatedAtGt** | **kotlinx.datetime.Instant**| Get all documents updated after a given ISO8601 timestamp (excluded). | [optional] |
| **updatedAtGte** | **kotlinx.datetime.Instant**| Get all documents updated after a given ISO8601 timestamp (included). | [optional] |
| **updatedAtLt** | **kotlinx.datetime.Instant**| Get all documents updated before a given ISO8601 timestamp (excluded). | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **updatedAtLte** | **kotlinx.datetime.Instant**| Get all documents updated before a given ISO8601 timestamp (included). | [optional] |

### Return type

[**PaginatedReportList**](PaginatedReportList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="reportsRetrieve"></a>
# **reportsRetrieve**
> Report reportsRetrieve(id)



### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.snapi.infrastructure.*
//import me.calebjones.spacelaunchnow.api.snapi.models.*

val apiInstance = ReportsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this report.
try {
    val result : Report = apiInstance.reportsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ReportsApi#reportsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ReportsApi#reportsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this report. | |

### Return type

[**Report**](Report.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

