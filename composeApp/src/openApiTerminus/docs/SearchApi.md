# SearchApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**searchList**](SearchApi.md#searchList) | **GET** /api/v1/search/ | Search across resources |


<a id="searchList"></a>
# **searchList**
> SearchResponse searchList(q, limit, types, `when`)

Search across resources

One query across launches, events, astronauts, rockets (launcher configurations), spacecraft, pads, agencies and programs: a count per type and the best &#x60;limit&#x60; items per type. &#x60;top&#x60; is an exact name match when there is one (program, rocket, spacecraft, astronaut, launch), else the next upcoming launch, else the most recent one. The crew of a matching launch joins &#x60;astronauts&#x60; with &#x60;via_launch&#x60; set to that launch. The answer is cached for 300 seconds.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SearchApi()
val q : kotlin.String = q_example // kotlin.String | The query, 2 to 100 characters.
val limit : kotlin.Int = 56 // kotlin.Int | Items per type, 1 to 10. Default 3.
val types : kotlin.String = types_example // kotlin.String | Comma list of: launches, events, astronauts, rockets, spacecraft, pads, agencies, programs. Default: all of them.
val `when` : kotlin.String = `when`_example // kotlin.String | Launches and events only. Default `any`.
try {
    val result : SearchResponse = apiInstance.searchList(q, limit, types, `when`)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SearchApi#searchList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SearchApi#searchList")
    e.printStackTrace()
}
```

### Parameters
| **q** | **kotlin.String**| The query, 2 to 100 characters. | |
| **limit** | **kotlin.Int**| Items per type, 1 to 10. Default 3. | [optional] |
| **types** | **kotlin.String**| Comma list of: launches, events, astronauts, rockets, spacecraft, pads, agencies, programs. Default: all of them. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **&#x60;when&#x60;** | **kotlin.String**| Launches and events only. Default &#x60;any&#x60;. | [optional] [enum: any, past, upcoming] |

### Return type

[**SearchResponse**](SearchResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

