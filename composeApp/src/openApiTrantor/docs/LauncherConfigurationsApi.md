# LauncherConfigurationsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getConfigurationApiV1ConfigurationsConfigIdGet**](LauncherConfigurationsApi.md#getConfigurationApiV1ConfigurationsConfigIdGet) | **GET** /api/v1/configurations/{config_id} | Get launcher configuration detail |
| [**listConfigurationsApiV1ConfigurationsGet**](LauncherConfigurationsApi.md#listConfigurationsApiV1ConfigurationsGet) | **GET** /api/v1/configurations | List launcher configurations |


<a id="getConfigurationApiV1ConfigurationsConfigIdGet"></a>
# **getConfigurationApiV1ConfigurationsConfigIdGet**
> LauncherConfigFull getConfigurationApiV1ConfigurationsConfigIdGet(configId, expand)

Get launcher configuration detail

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LauncherConfigurationsApi()
val configId : kotlin.Int = 56 // kotlin.Int | 
val expand : kotlin.String = expand_example // kotlin.String | Comma-separated list of relations to expand. Dot notation for nested: provider,rocket.stages.launcher
try {
    val result : LauncherConfigFull = apiInstance.getConfigurationApiV1ConfigurationsConfigIdGet(configId, expand)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LauncherConfigurationsApi#getConfigurationApiV1ConfigurationsConfigIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LauncherConfigurationsApi#getConfigurationApiV1ConfigurationsConfigIdGet")
    e.printStackTrace()
}
```

### Parameters
| **configId** | **kotlin.Int**|  | |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **expand** | **kotlin.String**| Comma-separated list of relations to expand. Dot notation for nested: provider,rocket.stages.launcher | [optional] |

### Return type

[**LauncherConfigFull**](LauncherConfigFull.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listConfigurationsApiV1ConfigurationsGet"></a>
# **listConfigurationsApiV1ConfigurationsGet**
> PaginatedResponseLauncherConfigSummary listConfigurationsApiV1ConfigurationsGet(search, active, manufacturerId, reusable, familyIds, programIds, isPlaceholder, ordering, limit, offset)

List launcher configurations

Paginated list of launcher configurations (vehicle types). Filter by search, active, manufacturer, reusable, family, program, placeholder status, or order by name/maiden_flight/total_launch_count.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LauncherConfigurationsApi()
val search : kotlin.String = search_example // kotlin.String | Search by name
val active : kotlin.Boolean = true // kotlin.Boolean | Filter by active status
val manufacturerId : kotlin.Int = 56 // kotlin.Int | Filter by manufacturer agency ID
val reusable : kotlin.Boolean = true // kotlin.Boolean | Filter by reusable status
val familyIds : kotlin.String = familyIds_example // kotlin.String | Comma-separated rocket family ids
val programIds : kotlin.String = programIds_example // kotlin.String | Comma-separated program ids
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | Filter by placeholder status
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name`, `maiden_flight`, or `total_launch_count`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseLauncherConfigSummary = apiInstance.listConfigurationsApiV1ConfigurationsGet(search, active, manufacturerId, reusable, familyIds, programIds, isPlaceholder, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LauncherConfigurationsApi#listConfigurationsApiV1ConfigurationsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LauncherConfigurationsApi#listConfigurationsApiV1ConfigurationsGet")
    e.printStackTrace()
}
```

### Parameters
| **search** | **kotlin.String**| Search by name | [optional] |
| **active** | **kotlin.Boolean**| Filter by active status | [optional] |
| **manufacturerId** | **kotlin.Int**| Filter by manufacturer agency ID | [optional] |
| **reusable** | **kotlin.Boolean**| Filter by reusable status | [optional] |
| **familyIds** | **kotlin.String**| Comma-separated rocket family ids | [optional] |
| **programIds** | **kotlin.String**| Comma-separated program ids | [optional] |
| **isPlaceholder** | **kotlin.Boolean**| Filter by placeholder status | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60;, &#x60;maiden_flight&#x60;, or &#x60;total_launch_count&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseLauncherConfigSummary**](PaginatedResponseLauncherConfigSummary.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

