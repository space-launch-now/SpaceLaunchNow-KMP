# SpacewalksApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**spacewalksDetailedList**](SpacewalksApi.md#spacewalksDetailedList) | **GET** /api/ll/2.4.0/spacewalks/detailed/ |  |
| [**spacewalksList**](SpacewalksApi.md#spacewalksList) | **GET** /api/ll/2.4.0/spacewalks/ |  |
| [**spacewalksMiniList**](SpacewalksApi.md#spacewalksMiniList) | **GET** /api/ll/2.4.0/spacewalks/mini/ |  |
| [**spacewalksRetrieve**](SpacewalksApi.md#spacewalksRetrieve) | **GET** /api/ll/2.4.0/spacewalks/{id}/ |  |


<a id="spacewalksDetailedList"></a>
# **spacewalksDetailedList**
> PaginatedSpacewalkEndpointDetailedList spacewalksDetailedList(astronautIds, day, end, endGt, endGte, endLt, endLte, eventIds, id, ids, launchIds, limit, month, name, nameContains, offset, ordering, ownerIds, programIds, programName, programNameContains, search, spacestationIds, startGt, startGte, startLt, startLte, year)



#### Filters Parameters - &#x60;astronaut__ids&#x60;, &#x60;day&#x60;, &#x60;end&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;event__ids&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;launch__ids&#x60;, &#x60;month&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;owner__ids&#x60;, &#x60;program__ids&#x60;, &#x60;program__name&#x60;, &#x60;program__name__contains&#x60;, &#x60;spacestation__ids&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;, &#x60;year&#x60;  Example - [/spacewalks/detailed/?program__name&#x3D;Apollo](./?program__name&#x3D;Apollo)  #### Search Fields searched - &#x60;crew__astronaut__name&#x60;, &#x60;location&#x60;, &#x60;name&#x60;, &#x60;program__name&#x60;  Example - [/spacewalks/detailed/?search&#x3D;Hubble](./?search&#x3D;Hubble)  #### Ordering Fields - &#x60;duration&#x60;, &#x60;end&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start&#x60;  Example - [/spacewalks/detailed/?ordering&#x3D;-duration](./?ordering&#x3D;-duration)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacewalks/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacewalks/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacewalksApi()
val astronautIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val day : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val end : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val eventIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val id : kotlin.Int = 56 // kotlin.Int | 
val ids : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val month : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val ownerIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val programIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val programName : kotlin.String = programName_example // kotlin.String | 
val programNameContains : kotlin.String = programNameContains_example // kotlin.String | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacestationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val startGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val year : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
try {
    val result : PaginatedSpacewalkEndpointDetailedList = apiInstance.spacewalksDetailedList(astronautIds, day, end, endGt, endGte, endLt, endLte, eventIds, id, ids, launchIds, limit, month, name, nameContains, offset, ordering, ownerIds, programIds, programName, programNameContains, search, spacestationIds, startGt, startGte, startLt, startLte, year)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacewalksApi#spacewalksDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacewalksApi#spacewalksDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **astronautIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **day** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **end** | **kotlinx.datetime.Instant**|  | [optional] |
| **endGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **endGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **endLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **endLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **eventIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **ids** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **month** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **ownerIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **programIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **programName** | **kotlin.String**|  | [optional] |
| **programNameContains** | **kotlin.String**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacestationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **startGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **startGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **startLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **startLte** | **kotlinx.datetime.Instant**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **year** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |

### Return type

[**PaginatedSpacewalkEndpointDetailedList**](PaginatedSpacewalkEndpointDetailedList.md)

### Authorization


Configure basicAuth:
    ApiClient.username = ""
    ApiClient.password = ""
Configure tokenAuth:
    ApiClient.apiKey["Authorization"] = ""
    ApiClient.apiKeyPrefix["Authorization"] = ""
Configure cookieAuth:
    ApiClient.apiKey["sessionid"] = ""
    ApiClient.apiKeyPrefix["sessionid"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacewalksList"></a>
# **spacewalksList**
> PaginatedSpacewalkEndpointNormalList spacewalksList(astronautIds, day, end, endGt, endGte, endLt, endLte, eventIds, id, ids, launchIds, limit, month, name, nameContains, offset, ordering, ownerIds, programIds, programName, programNameContains, search, spacestationIds, startGt, startGte, startLt, startLte, year)



#### Filters Parameters - &#x60;astronaut__ids&#x60;, &#x60;day&#x60;, &#x60;end&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;event__ids&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;launch__ids&#x60;, &#x60;month&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;owner__ids&#x60;, &#x60;program__ids&#x60;, &#x60;program__name&#x60;, &#x60;program__name__contains&#x60;, &#x60;spacestation__ids&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;, &#x60;year&#x60;  Example - [/spacewalks/?program__name&#x3D;Apollo](./?program__name&#x3D;Apollo)  #### Search Fields searched - &#x60;crew__astronaut__name&#x60;, &#x60;location&#x60;, &#x60;name&#x60;, &#x60;program__name&#x60;  Example - [/spacewalks/?search&#x3D;Hubble](./?search&#x3D;Hubble)  #### Ordering Fields - &#x60;duration&#x60;, &#x60;end&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start&#x60;  Example - [/spacewalks/?ordering&#x3D;-duration](./?ordering&#x3D;-duration)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacewalks/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacewalks/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacewalksApi()
val astronautIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val day : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val end : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val eventIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val id : kotlin.Int = 56 // kotlin.Int | 
val ids : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val month : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val ownerIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val programIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val programName : kotlin.String = programName_example // kotlin.String | 
val programNameContains : kotlin.String = programNameContains_example // kotlin.String | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacestationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val startGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val year : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
try {
    val result : PaginatedSpacewalkEndpointNormalList = apiInstance.spacewalksList(astronautIds, day, end, endGt, endGte, endLt, endLte, eventIds, id, ids, launchIds, limit, month, name, nameContains, offset, ordering, ownerIds, programIds, programName, programNameContains, search, spacestationIds, startGt, startGte, startLt, startLte, year)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacewalksApi#spacewalksList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacewalksApi#spacewalksList")
    e.printStackTrace()
}
```

### Parameters
| **astronautIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **day** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **end** | **kotlinx.datetime.Instant**|  | [optional] |
| **endGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **endGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **endLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **endLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **eventIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **ids** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **month** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **ownerIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **programIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **programName** | **kotlin.String**|  | [optional] |
| **programNameContains** | **kotlin.String**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacestationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **startGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **startGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **startLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **startLte** | **kotlinx.datetime.Instant**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **year** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |

### Return type

[**PaginatedSpacewalkEndpointNormalList**](PaginatedSpacewalkEndpointNormalList.md)

### Authorization


Configure basicAuth:
    ApiClient.username = ""
    ApiClient.password = ""
Configure tokenAuth:
    ApiClient.apiKey["Authorization"] = ""
    ApiClient.apiKeyPrefix["Authorization"] = ""
Configure cookieAuth:
    ApiClient.apiKey["sessionid"] = ""
    ApiClient.apiKeyPrefix["sessionid"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacewalksMiniList"></a>
# **spacewalksMiniList**
> PaginatedSpacewalkListList spacewalksMiniList(astronautIds, day, end, endGt, endGte, endLt, endLte, eventIds, id, ids, launchIds, limit, month, name, nameContains, offset, ordering, ownerIds, programIds, programName, programNameContains, search, spacestationIds, startGt, startGte, startLt, startLte, year)



#### Filters Parameters - &#x60;astronaut__ids&#x60;, &#x60;day&#x60;, &#x60;end&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;event__ids&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;launch__ids&#x60;, &#x60;month&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;owner__ids&#x60;, &#x60;program__ids&#x60;, &#x60;program__name&#x60;, &#x60;program__name__contains&#x60;, &#x60;spacestation__ids&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;, &#x60;year&#x60;  Example - [/spacewalks/mini/?program__name&#x3D;Apollo](./?program__name&#x3D;Apollo)  #### Search Fields searched - &#x60;crew__astronaut__name&#x60;, &#x60;location&#x60;, &#x60;name&#x60;, &#x60;program__name&#x60;  Example - [/spacewalks/mini/?search&#x3D;Hubble](./?search&#x3D;Hubble)  #### Ordering Fields - &#x60;duration&#x60;, &#x60;end&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start&#x60;  Example - [/spacewalks/mini/?ordering&#x3D;-duration](./?ordering&#x3D;-duration)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacewalks/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacewalks/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacewalksApi()
val astronautIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val day : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val end : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val endLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val eventIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val id : kotlin.Int = 56 // kotlin.Int | 
val ids : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val month : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val ownerIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val programIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val programName : kotlin.String = programName_example // kotlin.String | 
val programNameContains : kotlin.String = programNameContains_example // kotlin.String | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacestationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val startGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val startLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val year : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
try {
    val result : PaginatedSpacewalkListList = apiInstance.spacewalksMiniList(astronautIds, day, end, endGt, endGte, endLt, endLte, eventIds, id, ids, launchIds, limit, month, name, nameContains, offset, ordering, ownerIds, programIds, programName, programNameContains, search, spacestationIds, startGt, startGte, startLt, startLte, year)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacewalksApi#spacewalksMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacewalksApi#spacewalksMiniList")
    e.printStackTrace()
}
```

### Parameters
| **astronautIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **day** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **end** | **kotlinx.datetime.Instant**|  | [optional] |
| **endGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **endGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **endLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **endLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **eventIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **ids** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **month** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **ownerIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **programIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **programName** | **kotlin.String**|  | [optional] |
| **programNameContains** | **kotlin.String**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacestationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **startGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **startGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **startLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **startLte** | **kotlinx.datetime.Instant**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **year** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |

### Return type

[**PaginatedSpacewalkListList**](PaginatedSpacewalkListList.md)

### Authorization


Configure basicAuth:
    ApiClient.username = ""
    ApiClient.password = ""
Configure tokenAuth:
    ApiClient.apiKey["Authorization"] = ""
    ApiClient.apiKeyPrefix["Authorization"] = ""
Configure cookieAuth:
    ApiClient.apiKey["sessionid"] = ""
    ApiClient.apiKeyPrefix["sessionid"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="spacewalksRetrieve"></a>
# **spacewalksRetrieve**
> SpacewalkEndpointDetailed spacewalksRetrieve(id)



#### Filters Parameters - &#x60;astronaut__ids&#x60;, &#x60;day&#x60;, &#x60;end&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;event__ids&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;launch__ids&#x60;, &#x60;month&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;owner__ids&#x60;, &#x60;program__ids&#x60;, &#x60;program__name&#x60;, &#x60;program__name__contains&#x60;, &#x60;spacestation__ids&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;, &#x60;year&#x60;  Example - [/spacewalks/?program__name&#x3D;Apollo](./?program__name&#x3D;Apollo)  #### Search Fields searched - &#x60;crew__astronaut__name&#x60;, &#x60;location&#x60;, &#x60;name&#x60;, &#x60;program__name&#x60;  Example - [/spacewalks/?search&#x3D;Hubble](./?search&#x3D;Hubble)  #### Ordering Fields - &#x60;duration&#x60;, &#x60;end&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start&#x60;  Example - [/spacewalks/?ordering&#x3D;-duration](./?ordering&#x3D;-duration)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacewalks/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacewalks/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacewalksApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Spacewalk.
try {
    val result : SpacewalkEndpointDetailed = apiInstance.spacewalksRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacewalksApi#spacewalksRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacewalksApi#spacewalksRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Spacewalk. | |

### Return type

[**SpacewalkEndpointDetailed**](SpacewalkEndpointDetailed.md)

### Authorization


Configure basicAuth:
    ApiClient.username = ""
    ApiClient.password = ""
Configure tokenAuth:
    ApiClient.apiKey["Authorization"] = ""
    ApiClient.apiKeyPrefix["Authorization"] = ""
Configure cookieAuth:
    ApiClient.apiKey["sessionid"] = ""
    ApiClient.apiKeyPrefix["sessionid"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

