# SpaceStationsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getStationApiV1SpaceStationsStationIdGet**](SpaceStationsApi.md#getStationApiV1SpaceStationsStationIdGet) | **GET** /api/v1/space_stations/{station_id} | Get space station detail |
| [**listStationsApiV1SpaceStationsGet**](SpaceStationsApi.md#listStationsApiV1SpaceStationsGet) | **GET** /api/v1/space_stations | List space stations |


<a id="getStationApiV1SpaceStationsStationIdGet"></a>
# **getStationApiV1SpaceStationsStationIdGet**
> StationDetail getStationApiV1SpaceStationsStationIdGet(stationId)

Get space station detail

Full station payload â€” description, physical measurements, owners (agencies), and expeditions with crew (no standalone /expeditions endpoint).

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = SpaceStationsApi()
val stationId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : StationDetail = apiInstance.getStationApiV1SpaceStationsStationIdGet(stationId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpaceStationsApi#getStationApiV1SpaceStationsStationIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpaceStationsApi#getStationApiV1SpaceStationsStationIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **stationId** | **kotlin.Int**|  | |

### Return type

[**StationDetail**](StationDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listStationsApiV1SpaceStationsGet"></a>
# **listStationsApiV1SpaceStationsGet**
> PaginatedResponseStationList listStationsApiV1SpaceStationsGet(statusId, search, ordering, limit, offset)

List space stations

Paginated list of space stations. Filter by status or name search, order by name.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = SpaceStationsApi()
val statusId : kotlin.Int = 56 // kotlin.Int | Space station status id
val search : kotlin.String = search_example // kotlin.String | Search station name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseStationList = apiInstance.listStationsApiV1SpaceStationsGet(statusId, search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpaceStationsApi#listStationsApiV1SpaceStationsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpaceStationsApi#listStationsApiV1SpaceStationsGet")
    e.printStackTrace()
}
```

### Parameters
| **statusId** | **kotlin.Int**| Space station status id | [optional] |
| **search** | **kotlin.String**| Search station name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseStationList**](PaginatedResponseStationList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

