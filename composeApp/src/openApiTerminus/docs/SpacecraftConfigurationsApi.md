# SpacecraftConfigurationsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**spacecraftConfigurationsDetailedList**](SpacecraftConfigurationsApi.md#spacecraftConfigurationsDetailedList) | **GET** /api/v1/spacecraft_configurations/detailed/ |  |
| [**spacecraftConfigurationsList**](SpacecraftConfigurationsApi.md#spacecraftConfigurationsList) | **GET** /api/v1/spacecraft_configurations/ |  |
| [**spacecraftConfigurationsRetrieve**](SpacecraftConfigurationsApi.md#spacecraftConfigurationsRetrieve) | **GET** /api/v1/spacecraft_configurations/{id}/ |  |


<a id="spacecraftConfigurationsDetailedList"></a>
# **spacecraftConfigurationsDetailedList**
> PaginatedSpacecraftConfigDetailedList spacecraftConfigurationsDetailedList(agency, humanRated, inUse, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search)



#### Filters Parameters - &#x60;agency&#x60;, &#x60;human_rated&#x60;, &#x60;in_use&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/detailed/?human_rated&#x3D;True](./?human_rated&#x3D;True)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/detailed/?search&#x3D;Dragon](./?search&#x3D;Dragon)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/detailed/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configurations/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configurations/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationsApi()
val agency : kotlin.Int = 56 // kotlin.Int | 
val humanRated : kotlin.Boolean = true // kotlin.Boolean | 
val inUse : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedSpacecraftConfigDetailedList = apiInstance.spacecraftConfigurationsDetailedList(agency, humanRated, inUse, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationsApi#spacecraftConfigurationsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationsApi#spacecraftConfigurationsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **agency** | **kotlin.Int**|  | [optional] |
| **humanRated** | **kotlin.Boolean**|  | [optional] |
| **inUse** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedSpacecraftConfigDetailedList**](PaginatedSpacecraftConfigDetailedList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftConfigurationsList"></a>
# **spacecraftConfigurationsList**
> PaginatedSpacecraftConfigNormalList spacecraftConfigurationsList(agency, humanRated, inUse, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search)



#### Filters Parameters - &#x60;agency&#x60;, &#x60;human_rated&#x60;, &#x60;in_use&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/?human_rated&#x3D;True](./?human_rated&#x3D;True)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/?search&#x3D;Dragon](./?search&#x3D;Dragon)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configurations/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configurations/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationsApi()
val agency : kotlin.Int = 56 // kotlin.Int | 
val humanRated : kotlin.Boolean = true // kotlin.Boolean | 
val inUse : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedSpacecraftConfigNormalList = apiInstance.spacecraftConfigurationsList(agency, humanRated, inUse, lastUpdatedGte, lastUpdatedLte, limit, name, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationsApi#spacecraftConfigurationsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationsApi#spacecraftConfigurationsList")
    e.printStackTrace()
}
```

### Parameters
| **agency** | **kotlin.Int**|  | [optional] |
| **humanRated** | **kotlin.Boolean**|  | [optional] |
| **inUse** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlin.time.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlin.time.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedSpacecraftConfigNormalList**](PaginatedSpacecraftConfigNormalList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacecraftConfigurationsRetrieve"></a>
# **spacecraftConfigurationsRetrieve**
> SpacecraftConfigDetailed spacecraftConfigurationsRetrieve(id)



#### Filters Parameters - &#x60;agency&#x60;, &#x60;human_rated&#x60;, &#x60;in_use&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/?human_rated&#x3D;True](./?human_rated&#x3D;True)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/?search&#x3D;Dragon](./?search&#x3D;Dragon)  #### Ordering Fields - &#x60;last_updated&#x60;, &#x60;name&#x60;  Example - [/spacecraft_configurations/?ordering&#x3D;name](./?ordering&#x3D;name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_configurations/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_configurations/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = SpacecraftConfigurationsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Spacecraft Configuration.
try {
    val result : SpacecraftConfigDetailed = apiInstance.spacecraftConfigurationsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftConfigurationsApi#spacecraftConfigurationsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftConfigurationsApi#spacecraftConfigurationsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Spacecraft Configuration. | |

### Return type

[**SpacecraftConfigDetailed**](SpacecraftConfigDetailed.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

