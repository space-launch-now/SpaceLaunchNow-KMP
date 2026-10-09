# SpacecraftApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getSpacecraftApiV1SpacecraftSpacecraftIdGet**](SpacecraftApi.md#getSpacecraftApiV1SpacecraftSpacecraftIdGet) | **GET** /api/v1/spacecraft/{spacecraft_id} | Get spacecraft detail |
| [**listSpacecraftApiV1SpacecraftGet**](SpacecraftApi.md#listSpacecraftApiV1SpacecraftGet) | **GET** /api/v1/spacecraft | List spacecraft |


<a id="getSpacecraftApiV1SpacecraftSpacecraftIdGet"></a>
# **getSpacecraftApiV1SpacecraftSpacecraftIdGet**
> SpacecraftFull getSpacecraftApiV1SpacecraftSpacecraftIdGet(spacecraftId)

Get spacecraft detail

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = SpacecraftApi()
val spacecraftId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : SpacecraftFull = apiInstance.getSpacecraftApiV1SpacecraftSpacecraftIdGet(spacecraftId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftApi#getSpacecraftApiV1SpacecraftSpacecraftIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftApi#getSpacecraftApiV1SpacecraftSpacecraftIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **spacecraftId** | **kotlin.Int**|  | |

### Return type

[**SpacecraftFull**](SpacecraftFull.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listSpacecraftApiV1SpacecraftGet"></a>
# **listSpacecraftApiV1SpacecraftGet**
> PaginatedResponseSpacecraftSummary listSpacecraftApiV1SpacecraftGet(configId, inSpace, isPlaceholder, search, limit, offset)

List spacecraft

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = SpacecraftApi()
val configId : kotlin.Int = 56 // kotlin.Int | Filter by spacecraft config id
val inSpace : kotlin.Boolean = true // kotlin.Boolean | Filter by currently-in-space
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | Filter by placeholder status
val search : kotlin.String = search_example // kotlin.String | Search by name
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseSpacecraftSummary = apiInstance.listSpacecraftApiV1SpacecraftGet(configId, inSpace, isPlaceholder, search, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftApi#listSpacecraftApiV1SpacecraftGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftApi#listSpacecraftApiV1SpacecraftGet")
    e.printStackTrace()
}
```

### Parameters
| **configId** | **kotlin.Int**| Filter by spacecraft config id | [optional] |
| **inSpace** | **kotlin.Boolean**| Filter by currently-in-space | [optional] |
| **isPlaceholder** | **kotlin.Boolean**| Filter by placeholder status | [optional] |
| **search** | **kotlin.String**| Search by name | [optional] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseSpacecraftSummary**](PaginatedResponseSpacecraftSummary.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

