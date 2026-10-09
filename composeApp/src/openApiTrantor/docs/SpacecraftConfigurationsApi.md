# SpacecraftConfigurationsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet**](SpacecraftConfigurationsApi.md#getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet) | **GET** /api/v1/spacecraft_configurations/{config_id} | Get spacecraft configuration detail |
| [**listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet**](SpacecraftConfigurationsApi.md#listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet) | **GET** /api/v1/spacecraft_configurations | List spacecraft configurations |


<a id="getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet"></a>
# **getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet**
> SpacecraftConfigFull getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet(configId)

Get spacecraft configuration detail

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = SpacecraftConfigurationsApi()
val configId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : SpacecraftConfigFull = apiInstance.getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet(configId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationsApi#getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationsApi#getSpacecraftConfigurationApiV1SpacecraftConfigurationsConfigIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **configId** | **kotlin.Int**|  | |

### Return type

[**SpacecraftConfigFull**](SpacecraftConfigFull.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet"></a>
# **listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet**
> PaginatedResponseSpacecraftConfigSummary listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet(agencyId, search, limit, offset)

List spacecraft configurations

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = SpacecraftConfigurationsApi()
val agencyId : kotlin.Int = 56 // kotlin.Int | Filter by manufacturer agency id
val search : kotlin.String = search_example // kotlin.String | Search by name
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseSpacecraftConfigSummary = apiInstance.listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet(agencyId, search, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationsApi#listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationsApi#listSpacecraftConfigurationsApiV1SpacecraftConfigurationsGet")
    e.printStackTrace()
}
```

### Parameters
| **agencyId** | **kotlin.Int**| Filter by manufacturer agency id | [optional] |
| **search** | **kotlin.String**| Search by name | [optional] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseSpacecraftConfigSummary**](PaginatedResponseSpacecraftConfigSummary.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

