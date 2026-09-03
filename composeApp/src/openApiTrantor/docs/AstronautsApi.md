# AstronautsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getAstronautApiV1AstronautsAstronautIdGet**](AstronautsApi.md#getAstronautApiV1AstronautsAstronautIdGet) | **GET** /api/v1/astronauts/{astronaut_id} | Get astronaut detail |
| [**listAstronautsApiV1AstronautsGet**](AstronautsApi.md#listAstronautsApiV1AstronautsGet) | **GET** /api/v1/astronauts | List astronauts |


<a id="getAstronautApiV1AstronautsAstronautIdGet"></a>
# **getAstronautApiV1AstronautsAstronautIdGet**
> AstronautDetail getAstronautApiV1AstronautsAstronautIdGet(astronautId)

Get astronaut detail

Full astronaut payload â€” bio, dates, and flight history from launch_crews, newest first.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = AstronautsApi()
val astronautId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : AstronautDetail = apiInstance.getAstronautApiV1AstronautsAstronautIdGet(astronautId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AstronautsApi#getAstronautApiV1AstronautsAstronautIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AstronautsApi#getAstronautApiV1AstronautsAstronautIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **astronautId** | **kotlin.Int**|  | |

### Return type

[**AstronautDetail**](AstronautDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listAstronautsApiV1AstronautsGet"></a>
# **listAstronautsApiV1AstronautsGet**
> PaginatedResponseAstronautList listAstronautsApiV1AstronautsGet(statusIds, agencyIds, hasFlown, inSpace, isHuman, search, ordering, limit, offset)

List astronauts

Paginated list of astronauts. Filter by status, agency, has_flown, in_space, is_human (excludes the &#39;Non-Human&#39; astronaut type by name), or name search.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = AstronautsApi()
val statusIds : kotlin.String = statusIds_example // kotlin.String | Comma-separated astronaut status ids
val agencyIds : kotlin.String = agencyIds_example // kotlin.String | Comma-separated agency ids
val hasFlown : kotlin.Boolean = true // kotlin.Boolean | true â†’ flights_count > 0; false â†’ 0 or unrecorded
val inSpace : kotlin.Boolean = true // kotlin.Boolean | Currently in space
val isHuman : kotlin.Boolean = true // kotlin.Boolean | true â†’ excludes astronaut type 'Non-Human'; false â†’ only that type. An astronaut with no recorded type counts as human.
val search : kotlin.String = search_example // kotlin.String | Search astronaut name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseAstronautList = apiInstance.listAstronautsApiV1AstronautsGet(statusIds, agencyIds, hasFlown, inSpace, isHuman, search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AstronautsApi#listAstronautsApiV1AstronautsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AstronautsApi#listAstronautsApiV1AstronautsGet")
    e.printStackTrace()
}
```

### Parameters
| **statusIds** | **kotlin.String**| Comma-separated astronaut status ids | [optional] |
| **agencyIds** | **kotlin.String**| Comma-separated agency ids | [optional] |
| **hasFlown** | **kotlin.Boolean**| true â†’ flights_count &gt; 0; false â†’ 0 or unrecorded | [optional] |
| **inSpace** | **kotlin.Boolean**| Currently in space | [optional] |
| **isHuman** | **kotlin.Boolean**| true â†’ excludes astronaut type &#39;Non-Human&#39;; false â†’ only that type. An astronaut with no recorded type counts as human. | [optional] |
| **search** | **kotlin.String**| Search astronaut name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseAstronautList**](PaginatedResponseAstronautList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

