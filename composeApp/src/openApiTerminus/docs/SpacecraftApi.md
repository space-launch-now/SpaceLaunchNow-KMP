# SpacecraftApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**spacecraftDetailedList**](SpacecraftApi.md#spacecraftDetailedList) | **GET** /api/v1/spacecraft/detailed/ |  |
| [**spacecraftList**](SpacecraftApi.md#spacecraftList) | **GET** /api/v1/spacecraft/ |  |
| [**spacecraftMiniList**](SpacecraftApi.md#spacecraftMiniList) | **GET** /api/v1/spacecraft/mini/ |  |
| [**spacecraftRetrieve**](SpacecraftApi.md#spacecraftRetrieve) | **GET** /api/v1/spacecraft/{id}/ |  |


<a id="spacecraftDetailedList"></a>
# **spacecraftDetailedList**
> PaginatedSpacecraftEndpointDetailedList spacecraftDetailedList(inSpace, isPlaceholder, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search, spacecraftConfig, status)



#### Filters Parameters - &#x60;in_space&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;, &#x60;spacecraft_config&#x60;, &#x60;status&#x60;  Example - [/spacecraft/detailed/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;name&#x60;, &#x60;spacecraft_config__name&#x60;  Example - [/spacecraft/detailed/?search&#x3D;Endeavour](./?search&#x3D;Endeavour)  #### Ordering Fields - &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;mission_ends_count&#x60;, &#x60;time_docked&#x60;, &#x60;time_in_space&#x60;  Example - [/spacecraft/detailed/?ordering&#x3D;-flights_count](./?ordering&#x3D;-flights_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftApi()
val inSpace : kotlin.Boolean = true // kotlin.Boolean | 
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraftConfig : kotlin.Int = 56 // kotlin.Int | 
val status : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpacecraftEndpointDetailedList = apiInstance.spacecraftDetailedList(inSpace, isPlaceholder, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search, spacecraftConfig, status)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftApi#spacecraftDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftApi#spacecraftDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **inSpace** | **kotlin.Boolean**|  | [optional] |
| **isPlaceholder** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraftConfig** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **status** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpacecraftEndpointDetailedList**](PaginatedSpacecraftEndpointDetailedList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftList"></a>
# **spacecraftList**
> PaginatedSpacecraftNormalList spacecraftList(inSpace, isPlaceholder, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search, spacecraftConfig, status)



#### Filters Parameters - &#x60;in_space&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;, &#x60;spacecraft_config&#x60;, &#x60;status&#x60;  Example - [/spacecraft/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;name&#x60;, &#x60;spacecraft_config__name&#x60;  Example - [/spacecraft/?search&#x3D;Endeavour](./?search&#x3D;Endeavour)  #### Ordering Fields - &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;mission_ends_count&#x60;, &#x60;time_docked&#x60;, &#x60;time_in_space&#x60;  Example - [/spacecraft/?ordering&#x3D;-flights_count](./?ordering&#x3D;-flights_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftApi()
val inSpace : kotlin.Boolean = true // kotlin.Boolean | 
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraftConfig : kotlin.Int = 56 // kotlin.Int | 
val status : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpacecraftNormalList = apiInstance.spacecraftList(inSpace, isPlaceholder, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search, spacecraftConfig, status)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftApi#spacecraftList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftApi#spacecraftList")
    e.printStackTrace()
}
```

### Parameters
| **inSpace** | **kotlin.Boolean**|  | [optional] |
| **isPlaceholder** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraftConfig** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **status** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpacecraftNormalList**](PaginatedSpacecraftNormalList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftMiniList"></a>
# **spacecraftMiniList**
> PaginatedSpacecraftNormalList spacecraftMiniList(inSpace, isPlaceholder, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search, spacecraftConfig, status)



#### Filters Parameters - &#x60;in_space&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;, &#x60;spacecraft_config&#x60;, &#x60;status&#x60;  Example - [/spacecraft/mini/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;name&#x60;, &#x60;spacecraft_config__name&#x60;  Example - [/spacecraft/mini/?search&#x3D;Endeavour](./?search&#x3D;Endeavour)  #### Ordering Fields - &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;mission_ends_count&#x60;, &#x60;time_docked&#x60;, &#x60;time_in_space&#x60;  Example - [/spacecraft/mini/?ordering&#x3D;-flights_count](./?ordering&#x3D;-flights_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftApi()
val inSpace : kotlin.Boolean = true // kotlin.Boolean | 
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraftConfig : kotlin.Int = 56 // kotlin.Int | 
val status : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpacecraftNormalList = apiInstance.spacecraftMiniList(inSpace, isPlaceholder, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search, spacecraftConfig, status)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftApi#spacecraftMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftApi#spacecraftMiniList")
    e.printStackTrace()
}
```

### Parameters
| **inSpace** | **kotlin.Boolean**|  | [optional] |
| **isPlaceholder** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraftConfig** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **status** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpacecraftNormalList**](PaginatedSpacecraftNormalList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftRetrieve"></a>
# **spacecraftRetrieve**
> SpacecraftEndpointDetailed spacecraftRetrieve(id)



#### Filters Parameters - &#x60;in_space&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;, &#x60;spacecraft_config&#x60;, &#x60;status&#x60;  Example - [/spacecraft/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;name&#x60;, &#x60;spacecraft_config__name&#x60;  Example - [/spacecraft/?search&#x3D;Endeavour](./?search&#x3D;Endeavour)  #### Ordering Fields - &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;mission_ends_count&#x60;, &#x60;time_docked&#x60;, &#x60;time_in_space&#x60;  Example - [/spacecraft/?ordering&#x3D;-flights_count](./?ordering&#x3D;-flights_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Spacecraft.
try {
    val result : SpacecraftEndpointDetailed = apiInstance.spacecraftRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftApi#spacecraftRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftApi#spacecraftRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Spacecraft. | |

### Return type

[**SpacecraftEndpointDetailed**](SpacecraftEndpointDetailed.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

