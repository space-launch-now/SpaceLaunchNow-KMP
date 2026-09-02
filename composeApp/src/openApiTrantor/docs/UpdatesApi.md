# UpdatesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**listUpdatesApiV1UpdatesGet**](UpdatesApi.md#listUpdatesApiV1UpdatesGet) | **GET** /api/v1/updates | List updates |


<a id="listUpdatesApiV1UpdatesGet"></a>
# **listUpdatesApiV1UpdatesGet**
> PaginatedResponseUpdateList listUpdatesApiV1UpdatesGet(programIds, launchId, ordering, limit, offset)

List updates

Paginated standalone update feed, ordered by created_on. Filter by program (direct, or reachable through the update&#39;s launch or event program junctions â€” LL&#39;s all__program semantics) or launch. Multi-value &#x60;*_ids&#x60; params take comma-separated integers.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = UpdatesApi()
val programIds : kotlin.String = programIds_example // kotlin.String | Comma-separated program ids
val launchId : kotlin.String = launchId_example // kotlin.String | Launch id
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `created_on`. Prefix - for descending. Unknown values fall back to -created_on.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseUpdateList = apiInstance.listUpdatesApiV1UpdatesGet(programIds, launchId, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UpdatesApi#listUpdatesApiV1UpdatesGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UpdatesApi#listUpdatesApiV1UpdatesGet")
    e.printStackTrace()
}
```

### Parameters
| **programIds** | **kotlin.String**| Comma-separated program ids | [optional] |
| **launchId** | **kotlin.String**| Launch id | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;created_on&#x60;. Prefix - for descending. Unknown values fall back to -created_on. | [optional] [default to &quot;-created_on&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseUpdateList**](PaginatedResponseUpdateList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

