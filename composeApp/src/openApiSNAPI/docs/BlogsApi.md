# BlogsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**blogsList**](BlogsApi.md#blogsList) | **GET** /v4/blogs/ |  |
| [**blogsRetrieve**](BlogsApi.md#blogsRetrieve) | **GET** /v4/blogs/{id}/ |  |


<a id="blogsList"></a>
# **blogsList**
> PaginatedBlogList blogsList(event, hasEvent, hasLaunch, isFeatured, launch, limit, newsSite, newsSiteExclude, offset, ordering, publishedAtGt, publishedAtGte, publishedAtLt, publishedAtLte, search, summaryContains, summaryContainsAll, summaryContainsOne, titleContains, titleContainsAll, titleContainsOne, updatedAtGt, updatedAtGte, updatedAtLt, updatedAtLte)



### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.snapi.infrastructure.*
//import me.calebjones.spacelaunchnow.api.snapi.models.*

val apiInstance = BlogsApi()
val event : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Search for all documents related to a specific event using its Launch Library 2 ID.
val hasEvent : kotlin.Boolean = true // kotlin.Boolean | Get all documents that have a related event.
val hasLaunch : kotlin.Boolean = true // kotlin.Boolean | Get all documents that have a related launch.
val isFeatured : kotlin.Boolean = true // kotlin.Boolean | Get all documents that are featured.
val launch : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Search for all documents related to a specific launch using its Launch Library 2 ID.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val newsSite : kotlin.String = newsSite_example // kotlin.String | Search for documents with a news_site__name present in a list of comma-separated values. Case insensitive.
val newsSiteExclude : kotlin.String = newsSiteExclude_example // kotlin.String | Search for documents with a news_site__name not present in a list of comma-separated values. Case insensitive.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Order the result on `published_at, -published_at, updated_at, -updated_at`.  * `published_at` - Published at * `-published_at` - Published at (descending) * `updated_at` - Updated at * `-updated_at` - Updated at (descending)
val publishedAtGt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents published after a given ISO8601 timestamp (excluded).
val publishedAtGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents published after a given ISO8601 timestamp (included).
val publishedAtLt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents published before a given ISO8601 timestamp (excluded).
val publishedAtLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents published before a given ISO8601 timestamp (included).
val search : kotlin.String = search_example // kotlin.String | Search for documents with a specific phrase in the title or summary.
val summaryContains : kotlin.String = summaryContains_example // kotlin.String | Search for all documents with a specific phrase in the summary.
val summaryContainsAll : kotlin.String = summaryContainsAll_example // kotlin.String | Search for documents with a summary containing all keywords from comma-separated values.
val summaryContainsOne : kotlin.String = summaryContainsOne_example // kotlin.String | Search for documents with a summary containing at least one keyword from comma-separated values.
val titleContains : kotlin.String = titleContains_example // kotlin.String | Search for all documents with a specific phrase in the title.
val titleContainsAll : kotlin.String = titleContainsAll_example // kotlin.String | Search for documents with a title containing all keywords from comma-separated values.
val titleContainsOne : kotlin.String = titleContainsOne_example // kotlin.String | Search for documents with a title containing at least one keyword from comma-separated values.
val updatedAtGt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents updated after a given ISO8601 timestamp (excluded).
val updatedAtGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents updated after a given ISO8601 timestamp (included).
val updatedAtLt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents updated before a given ISO8601 timestamp (excluded).
val updatedAtLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Get all documents updated before a given ISO8601 timestamp (included).
try {
    val result : PaginatedBlogList = apiInstance.blogsList(event, hasEvent, hasLaunch, isFeatured, launch, limit, newsSite, newsSiteExclude, offset, ordering, publishedAtGt, publishedAtGte, publishedAtLt, publishedAtLte, search, summaryContains, summaryContainsAll, summaryContainsOne, titleContains, titleContainsAll, titleContainsOne, updatedAtGt, updatedAtGte, updatedAtLt, updatedAtLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling BlogsApi#blogsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling BlogsApi#blogsList")
    e.printStackTrace()
}
```

### Parameters
| **event** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Search for all documents related to a specific event using its Launch Library 2 ID. | [optional] |
| **hasEvent** | **kotlin.Boolean**| Get all documents that have a related event. | [optional] |
| **hasLaunch** | **kotlin.Boolean**| Get all documents that have a related launch. | [optional] |
| **isFeatured** | **kotlin.Boolean**| Get all documents that are featured. | [optional] |
| **launch** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Search for all documents related to a specific launch using its Launch Library 2 ID. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **newsSite** | **kotlin.String**| Search for documents with a news_site__name present in a list of comma-separated values. Case insensitive. | [optional] |
| **newsSiteExclude** | **kotlin.String**| Search for documents with a news_site__name not present in a list of comma-separated values. Case insensitive. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Order the result on &#x60;published_at, -published_at, updated_at, -updated_at&#x60;.  * &#x60;published_at&#x60; - Published at * &#x60;-published_at&#x60; - Published at (descending) * &#x60;updated_at&#x60; - Updated at * &#x60;-updated_at&#x60; - Updated at (descending) | [optional] [enum: -published_at, -updated_at, published_at, updated_at] |
| **publishedAtGt** | **kotlin.time.Instant**| Get all documents published after a given ISO8601 timestamp (excluded). | [optional] |
| **publishedAtGte** | **kotlin.time.Instant**| Get all documents published after a given ISO8601 timestamp (included). | [optional] |
| **publishedAtLt** | **kotlin.time.Instant**| Get all documents published before a given ISO8601 timestamp (excluded). | [optional] |
| **publishedAtLte** | **kotlin.time.Instant**| Get all documents published before a given ISO8601 timestamp (included). | [optional] |
| **search** | **kotlin.String**| Search for documents with a specific phrase in the title or summary. | [optional] |
| **summaryContains** | **kotlin.String**| Search for all documents with a specific phrase in the summary. | [optional] |
| **summaryContainsAll** | **kotlin.String**| Search for documents with a summary containing all keywords from comma-separated values. | [optional] |
| **summaryContainsOne** | **kotlin.String**| Search for documents with a summary containing at least one keyword from comma-separated values. | [optional] |
| **titleContains** | **kotlin.String**| Search for all documents with a specific phrase in the title. | [optional] |
| **titleContainsAll** | **kotlin.String**| Search for documents with a title containing all keywords from comma-separated values. | [optional] |
| **titleContainsOne** | **kotlin.String**| Search for documents with a title containing at least one keyword from comma-separated values. | [optional] |
| **updatedAtGt** | **kotlin.time.Instant**| Get all documents updated after a given ISO8601 timestamp (excluded). | [optional] |
| **updatedAtGte** | **kotlin.time.Instant**| Get all documents updated after a given ISO8601 timestamp (included). | [optional] |
| **updatedAtLt** | **kotlin.time.Instant**| Get all documents updated before a given ISO8601 timestamp (excluded). | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **updatedAtLte** | **kotlin.time.Instant**| Get all documents updated before a given ISO8601 timestamp (included). | [optional] |

### Return type

[**PaginatedBlogList**](PaginatedBlogList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="blogsRetrieve"></a>
# **blogsRetrieve**
> Blog blogsRetrieve(id)



### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.snapi.infrastructure.*
//import me.calebjones.spacelaunchnow.api.snapi.models.*

val apiInstance = BlogsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this blog.
try {
    val result : Blog = apiInstance.blogsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling BlogsApi#blogsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling BlogsApi#blogsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this blog. | |

### Return type

[**Blog**](Blog.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

