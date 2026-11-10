# ViewingApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**viewingLaunchRetrieve**](ViewingApi.md#viewingLaunchRetrieve) | **GET** /api/v1/viewing/launches/{launch_id}/ | One launch with its flight path and viewing results |
| [**viewingPlacesSearch**](ViewingApi.md#viewingPlacesSearch) | **GET** /api/v1/viewing/places/search/ | Search places by name |
| [**viewingUpcomingList**](ViewingApi.md#viewingUpcomingList) | **GET** /api/v1/viewing/upcoming/ | Upcoming launches with a flight path |


<a id="viewingLaunchRetrieve"></a>
# **viewingLaunchRetrieve**
> ViewingDetail viewingLaunchRetrieve(launchId)

One launch with its flight path and viewing results

A &#x60;ready&#x60; or &#x60;past&#x60; row. 404 when the launch has no flight path. Cached for 30 seconds.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = ViewingApi()
val launchId : kotlin.String = 38400000-8cf0-11bd-b23e-10b96e4ef00d // kotlin.String | 
try {
    val result : ViewingDetail = apiInstance.viewingLaunchRetrieve(launchId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ViewingApi#viewingLaunchRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ViewingApi#viewingLaunchRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **launchId** | **kotlin.String**|  | |

### Return type

[**ViewingDetail**](ViewingDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="viewingPlacesSearch"></a>
# **viewingPlacesSearch**
> kotlin.collections.List&lt;PlaceResult&gt; viewingPlacesSearch(q)

Search places by name

Up to 10 cities (GeoNames) and viewing places whose name matches &#x60;q&#x60;, a name that starts with &#x60;q&#x60; first, then the larger population first. A plain list. Cached for 600 seconds. City data: GeoNames, CC BY 4.0; credit GeoNames where you show it.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = ViewingApi()
val q : kotlin.String = q_example // kotlin.String | The text, 2 to 100 characters.
try {
    val result : kotlin.collections.List<PlaceResult> = apiInstance.viewingPlacesSearch(q)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ViewingApi#viewingPlacesSearch")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ViewingApi#viewingPlacesSearch")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **q** | **kotlin.String**| The text, 2 to 100 characters. | |

### Return type

[**kotlin.collections.List&lt;PlaceResult&gt;**](PlaceResult.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="viewingUpcomingList"></a>
# **viewingUpcomingList**
> PaginatedViewingList viewingUpcomingList(limit, location, offset, pad)

Upcoming launches with a flight path

Launches with a ready flight path and a NET in the future, ordered by NET. &#x60;limit&#x60; is 25 at most. Cached for 30 seconds.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = ViewingApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val location : kotlin.Double = 8.14 // kotlin.Double | A location id.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val pad : kotlin.Double = 8.14 // kotlin.Double | A pad id.
try {
    val result : PaginatedViewingList = apiInstance.viewingUpcomingList(limit, location, offset, pad)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ViewingApi#viewingUpcomingList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ViewingApi#viewingUpcomingList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **location** | **kotlin.Double**| A location id. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **pad** | **kotlin.Double**| A pad id. | [optional] |

### Return type

[**PaginatedViewingList**](PaginatedViewingList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

