# SpacecraftConfigurationFamiliesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**spacecraftConfigurationFamiliesDetailedList**](SpacecraftConfigurationFamiliesApi.md#spacecraftConfigurationFamiliesDetailedList) | **GET** /api/v1/spacecraft_configuration_families/detailed/ |  |
| [**spacecraftConfigurationFamiliesList**](SpacecraftConfigurationFamiliesApi.md#spacecraftConfigurationFamiliesList) | **GET** /api/v1/spacecraft_configuration_families/ |  |
| [**spacecraftConfigurationFamiliesMiniList**](SpacecraftConfigurationFamiliesApi.md#spacecraftConfigurationFamiliesMiniList) | **GET** /api/v1/spacecraft_configuration_families/mini/ |  |
| [**spacecraftConfigurationFamiliesRetrieve**](SpacecraftConfigurationFamiliesApi.md#spacecraftConfigurationFamiliesRetrieve) | **GET** /api/v1/spacecraft_configuration_families/{id}/ |  |


<a id="spacecraftConfigurationFamiliesDetailedList"></a>
# **spacecraftConfigurationFamiliesDetailedList**
> PaginatedSpacecraftConfigFamilyEndpointDetailedList spacecraftConfigurationFamiliesDetailedList(lastUpdatedGte, lastUpdatedLte, limit, manufacturer, name, offset, ordering, search)



#### Filters Parameters - &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;manufacturer&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/detailed/?manufacturer&#x3D;121](./?manufacturer&#x3D;121)  #### Search Fields searched - &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/detailed/?search&#x3D;Northrop](./?search&#x3D;Northrop)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/detailed/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configuration_families/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configuration_families/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationFamiliesApi()
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val manufacturer : kotlin.Int = 56 // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedSpacecraftConfigFamilyEndpointDetailedList = apiInstance.spacecraftConfigurationFamiliesDetailedList(lastUpdatedGte, lastUpdatedLte, limit, manufacturer, name, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **manufacturer** | **kotlin.Int**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedSpacecraftConfigFamilyEndpointDetailedList**](PaginatedSpacecraftConfigFamilyEndpointDetailedList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftConfigurationFamiliesList"></a>
# **spacecraftConfigurationFamiliesList**
> PaginatedSpacecraftConfigFamilyNormalList spacecraftConfigurationFamiliesList(lastUpdatedGte, lastUpdatedLte, limit, manufacturer, name, offset, ordering, search)



#### Filters Parameters - &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;manufacturer&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/?manufacturer&#x3D;121](./?manufacturer&#x3D;121)  #### Search Fields searched - &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/?search&#x3D;Northrop](./?search&#x3D;Northrop)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configuration_families/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configuration_families/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationFamiliesApi()
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val manufacturer : kotlin.Int = 56 // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedSpacecraftConfigFamilyNormalList = apiInstance.spacecraftConfigurationFamiliesList(lastUpdatedGte, lastUpdatedLte, limit, manufacturer, name, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesList")
    e.printStackTrace()
}
```

### Parameters
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **manufacturer** | **kotlin.Int**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedSpacecraftConfigFamilyNormalList**](PaginatedSpacecraftConfigFamilyNormalList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftConfigurationFamiliesMiniList"></a>
# **spacecraftConfigurationFamiliesMiniList**
> PaginatedSpacecraftConfigFamilyMiniList spacecraftConfigurationFamiliesMiniList(lastUpdatedGte, lastUpdatedLte, limit, manufacturer, name, offset, ordering, search)



#### Filters Parameters - &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;manufacturer&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/mini/?manufacturer&#x3D;121](./?manufacturer&#x3D;121)  #### Search Fields searched - &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/mini/?search&#x3D;Northrop](./?search&#x3D;Northrop)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/mini/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configuration_families/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configuration_families/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationFamiliesApi()
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val manufacturer : kotlin.Int = 56 // kotlin.Int | 
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedSpacecraftConfigFamilyMiniList = apiInstance.spacecraftConfigurationFamiliesMiniList(lastUpdatedGte, lastUpdatedLte, limit, manufacturer, name, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesMiniList")
    e.printStackTrace()
}
```

### Parameters
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **manufacturer** | **kotlin.Int**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedSpacecraftConfigFamilyMiniList**](PaginatedSpacecraftConfigFamilyMiniList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftConfigurationFamiliesRetrieve"></a>
# **spacecraftConfigurationFamiliesRetrieve**
> SpacecraftConfigFamilyEndpointDetailed spacecraftConfigurationFamiliesRetrieve(id)



#### Filters Parameters - &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;manufacturer&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/?manufacturer&#x3D;121](./?manufacturer&#x3D;121)  #### Search Fields searched - &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/?search&#x3D;Northrop](./?search&#x3D;Northrop)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configuration_families/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configuration_families/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configuration_families/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationFamiliesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Spacecraft Configuration Family.
try {
    val result : SpacecraftConfigFamilyEndpointDetailed = apiInstance.spacecraftConfigurationFamiliesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationFamiliesApi#spacecraftConfigurationFamiliesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Spacecraft Configuration Family. | |

### Return type

[**SpacecraftConfigFamilyEndpointDetailed**](SpacecraftConfigFamilyEndpointDetailed.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

