# LaunchersApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getLauncherApiV1LaunchersLauncherIdGet**](LaunchersApi.md#getLauncherApiV1LaunchersLauncherIdGet) | **GET** /api/v1/launchers/{launcher_id} | Get launcher (booster) detail |
| [**listLaunchersApiV1LaunchersGet**](LaunchersApi.md#listLaunchersApiV1LaunchersGet) | **GET** /api/v1/launchers | List launchers (boosters) |


<a id="getLauncherApiV1LaunchersLauncherIdGet"></a>
# **getLauncherApiV1LaunchersLauncherIdGet**
> LauncherDetail getLauncherApiV1LaunchersLauncherIdGet(launcherId)

Get launcher (booster) detail

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LaunchersApi()
val launcherId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : LauncherDetail = apiInstance.getLauncherApiV1LaunchersLauncherIdGet(launcherId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchersApi#getLauncherApiV1LaunchersLauncherIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchersApi#getLauncherApiV1LaunchersLauncherIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **launcherId** | **kotlin.Int**|  | |

### Return type

[**LauncherDetail**](LauncherDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listLaunchersApiV1LaunchersGet"></a>
# **listLaunchersApiV1LaunchersGet**
> PaginatedResponseLauncherListItem listLaunchersApiV1LaunchersGet(configIds, isPlaceholder, search, ordering, limit, offset)

List launchers (boosters)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LaunchersApi()
val configIds : kotlin.String = configIds_example // kotlin.String | Comma-separated launcher configuration ids
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | Filter by placeholder (unassigned hardware) status
val search : kotlin.String = search_example // kotlin.String | Search by serial number
val ordering : kotlin.String = ordering_example // kotlin.String | `flights` or `-flights` (default)
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseLauncherListItem = apiInstance.listLaunchersApiV1LaunchersGet(configIds, isPlaceholder, search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchersApi#listLaunchersApiV1LaunchersGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchersApi#listLaunchersApiV1LaunchersGet")
    e.printStackTrace()
}
```

### Parameters
| **configIds** | **kotlin.String**| Comma-separated launcher configuration ids | [optional] |
| **isPlaceholder** | **kotlin.Boolean**| Filter by placeholder (unassigned hardware) status | [optional] |
| **search** | **kotlin.String**| Search by serial number | [optional] |
| **ordering** | **kotlin.String**| &#x60;flights&#x60; or &#x60;-flights&#x60; (default) | [optional] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseLauncherListItem**](PaginatedResponseLauncherListItem.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

