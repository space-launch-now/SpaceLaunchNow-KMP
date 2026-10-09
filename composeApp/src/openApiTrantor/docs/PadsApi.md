# PadsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getPadApiV1PadsPadIdGet**](PadsApi.md#getPadApiV1PadsPadIdGet) | **GET** /api/v1/pads/{pad_id} | Get pad detail |
| [**listPadsApiV1PadsGet**](PadsApi.md#listPadsApiV1PadsGet) | **GET** /api/v1/pads | List launch pads |


<a id="getPadApiV1PadsPadIdGet"></a>
# **getPadApiV1PadsPadIdGet**
> PadFull getPadApiV1PadsPadIdGet(padId, expand)

Get pad detail

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = PadsApi()
val padId : kotlin.Int = 56 // kotlin.Int | 
val expand : kotlin.String = expand_example // kotlin.String | Comma-separated list of relations to expand. Dot notation for nested: provider,rocket.stages.launcher
try {
    val result : PadFull = apiInstance.getPadApiV1PadsPadIdGet(padId, expand)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PadsApi#getPadApiV1PadsPadIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PadsApi#getPadApiV1PadsPadIdGet")
    e.printStackTrace()
}
```

### Parameters
| **padId** | **kotlin.Int**|  | |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **expand** | **kotlin.String**| Comma-separated list of relations to expand. Dot notation for nested: provider,rocket.stages.launcher | [optional] |

### Return type

[**PadFull**](PadFull.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listPadsApiV1PadsGet"></a>
# **listPadsApiV1PadsGet**
> PaginatedResponsePadSummary listPadsApiV1PadsGet(search, locationId, limit, offset)

List launch pads

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = PadsApi()
val search : kotlin.String = search_example // kotlin.String | Search pad name
val locationId : kotlin.Int = 56 // kotlin.Int | Filter by location ID
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponsePadSummary = apiInstance.listPadsApiV1PadsGet(search, locationId, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PadsApi#listPadsApiV1PadsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PadsApi#listPadsApiV1PadsGet")
    e.printStackTrace()
}
```

### Parameters
| **search** | **kotlin.String**| Search pad name | [optional] |
| **locationId** | **kotlin.Int**| Filter by location ID | [optional] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponsePadSummary**](PaginatedResponsePadSummary.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

