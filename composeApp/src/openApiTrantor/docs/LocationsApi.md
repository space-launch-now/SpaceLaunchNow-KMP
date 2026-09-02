# LocationsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**listLocationsApiV1LocationsGet**](LocationsApi.md#listLocationsApiV1LocationsGet) | **GET** /api/v1/locations | List locations |


<a id="listLocationsApiV1LocationsGet"></a>
# **listLocationsApiV1LocationsGet**
> PaginatedResponseLocationList listLocationsApiV1LocationsGet(active, search, ordering, limit, offset)

List locations

Paginated list of launch site locations. Filter by active status or name search.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LocationsApi()
val active : kotlin.Boolean = true // kotlin.Boolean | Filter by active status
val search : kotlin.String = search_example // kotlin.String | Search by name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseLocationList = apiInstance.listLocationsApiV1LocationsGet(active, search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LocationsApi#listLocationsApiV1LocationsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LocationsApi#listLocationsApiV1LocationsGet")
    e.printStackTrace()
}
```

### Parameters
| **active** | **kotlin.Boolean**| Filter by active status | [optional] |
| **search** | **kotlin.String**| Search by name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseLocationList**](PaginatedResponseLocationList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

