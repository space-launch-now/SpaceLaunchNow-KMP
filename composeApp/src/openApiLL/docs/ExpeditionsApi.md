# ExpeditionsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**expeditionsDetailedList**](ExpeditionsApi.md#expeditionsDetailedList) | **GET** /api/ll/2.4.0/expeditions/detailed/ |  |
| [**expeditionsList**](ExpeditionsApi.md#expeditionsList) | **GET** /api/ll/2.4.0/expeditions/ |  |
| [**expeditionsRetrieve**](ExpeditionsApi.md#expeditionsRetrieve) | **GET** /api/ll/2.4.0/expeditions/{id}/ |  |


<a id="expeditionsDetailedList"></a>
# **expeditionsDetailedList**
> PaginatedExpeditionDetailedList expeditionsDetailedList(crewAstronaut, crewAstronautAgency, endGt, endGte, endLt, endLte, limit, name, offset, ordering, search, spaceStation, startGt, startGte, startLt, startLte)



#### Filters Parameters - &#x60;crew__astronaut&#x60;, &#x60;crew__astronaut__agency&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;name&#x60;, &#x60;space_station&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;  Example - [/expeditions/detailed/?space_station&#x3D;18](./?space_station&#x3D;18)  #### Search Fields searched - &#x60;crew__astronaut__agency__abbrev&#x60;, &#x60;crew__astronaut__agency__name&#x60;, &#x60;crew__astronaut__name&#x60;, &#x60;crew__astronaut__nationality__nationality_name&#x60;, &#x60;name&#x60;  Example - [/expeditions/detailed/?search&#x3D;Kelly](./?search&#x3D;Kelly)  #### Ordering Fields - &#x60;end&#x60;, &#x60;id&#x60;, &#x60;start&#x60;  Example - [/expeditions/detailed/?ordering&#x3D;-start](./?ordering&#x3D;-start)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/expeditions/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/expeditions/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = ExpeditionsApi()
val crewAstronaut : kotlin.Int = 56 // kotlin.Int | 
val crewAstronautAgency : kotlin.Int = 56 // kotlin.Int | 
val endGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is greater than
val endGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is greater than or equal to
val endLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is less than
val endLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is greater than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spaceStation : kotlin.Int = 56 // kotlin.Int | 
val startGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is greater than
val startGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is greater than or equal to
val startLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is less than
val startLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is greater than or equal to
try {
    val result : PaginatedExpeditionDetailedList = apiInstance.expeditionsDetailedList(crewAstronaut, crewAstronautAgency, endGt, endGte, endLt, endLte, limit, name, offset, ordering, search, spaceStation, startGt, startGte, startLt, startLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ExpeditionsApi#expeditionsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ExpeditionsApi#expeditionsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **crewAstronaut** | **kotlin.Int**|  | [optional] |
| **crewAstronautAgency** | **kotlin.Int**|  | [optional] |
| **endGt** | **kotlinx.datetime.Instant**| End is greater than | [optional] |
| **endGte** | **kotlinx.datetime.Instant**| End is greater than or equal to | [optional] |
| **endLt** | **kotlinx.datetime.Instant**| End is less than | [optional] |
| **endLte** | **kotlinx.datetime.Instant**| End is greater than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spaceStation** | **kotlin.Int**|  | [optional] |
| **startGt** | **kotlinx.datetime.Instant**| Start is greater than | [optional] |
| **startGte** | **kotlinx.datetime.Instant**| Start is greater than or equal to | [optional] |
| **startLt** | **kotlinx.datetime.Instant**| Start is less than | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **startLte** | **kotlinx.datetime.Instant**| Start is greater than or equal to | [optional] |

### Return type

[**PaginatedExpeditionDetailedList**](PaginatedExpeditionDetailedList.md)

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

<a id="expeditionsList"></a>
# **expeditionsList**
> PaginatedExpeditionNormalList expeditionsList(crewAstronaut, crewAstronautAgency, endGt, endGte, endLt, endLte, limit, name, offset, ordering, search, spaceStation, startGt, startGte, startLt, startLte)



#### Filters Parameters - &#x60;crew__astronaut&#x60;, &#x60;crew__astronaut__agency&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;name&#x60;, &#x60;space_station&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;  Example - [/expeditions/?space_station&#x3D;18](./?space_station&#x3D;18)  #### Search Fields searched - &#x60;crew__astronaut__agency__abbrev&#x60;, &#x60;crew__astronaut__agency__name&#x60;, &#x60;crew__astronaut__name&#x60;, &#x60;crew__astronaut__nationality__nationality_name&#x60;, &#x60;name&#x60;  Example - [/expeditions/?search&#x3D;Kelly](./?search&#x3D;Kelly)  #### Ordering Fields - &#x60;end&#x60;, &#x60;id&#x60;, &#x60;start&#x60;  Example - [/expeditions/?ordering&#x3D;-start](./?ordering&#x3D;-start)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/expeditions/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/expeditions/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = ExpeditionsApi()
val crewAstronaut : kotlin.Int = 56 // kotlin.Int | 
val crewAstronautAgency : kotlin.Int = 56 // kotlin.Int | 
val endGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is greater than
val endGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is greater than or equal to
val endLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is less than
val endLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | End is greater than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spaceStation : kotlin.Int = 56 // kotlin.Int | 
val startGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is greater than
val startGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is greater than or equal to
val startLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is less than
val startLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Start is greater than or equal to
try {
    val result : PaginatedExpeditionNormalList = apiInstance.expeditionsList(crewAstronaut, crewAstronautAgency, endGt, endGte, endLt, endLte, limit, name, offset, ordering, search, spaceStation, startGt, startGte, startLt, startLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ExpeditionsApi#expeditionsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ExpeditionsApi#expeditionsList")
    e.printStackTrace()
}
```

### Parameters
| **crewAstronaut** | **kotlin.Int**|  | [optional] |
| **crewAstronautAgency** | **kotlin.Int**|  | [optional] |
| **endGt** | **kotlinx.datetime.Instant**| End is greater than | [optional] |
| **endGte** | **kotlinx.datetime.Instant**| End is greater than or equal to | [optional] |
| **endLt** | **kotlinx.datetime.Instant**| End is less than | [optional] |
| **endLte** | **kotlinx.datetime.Instant**| End is greater than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spaceStation** | **kotlin.Int**|  | [optional] |
| **startGt** | **kotlinx.datetime.Instant**| Start is greater than | [optional] |
| **startGte** | **kotlinx.datetime.Instant**| Start is greater than or equal to | [optional] |
| **startLt** | **kotlinx.datetime.Instant**| Start is less than | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **startLte** | **kotlinx.datetime.Instant**| Start is greater than or equal to | [optional] |

### Return type

[**PaginatedExpeditionNormalList**](PaginatedExpeditionNormalList.md)

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

<a id="expeditionsRetrieve"></a>
# **expeditionsRetrieve**
> ExpeditionDetailed expeditionsRetrieve(id)



#### Filters Parameters - &#x60;crew__astronaut&#x60;, &#x60;crew__astronaut__agency&#x60;, &#x60;end__gt&#x60;, &#x60;end__gte&#x60;, &#x60;end__lt&#x60;, &#x60;end__lte&#x60;, &#x60;name&#x60;, &#x60;space_station&#x60;, &#x60;start__gt&#x60;, &#x60;start__gte&#x60;, &#x60;start__lt&#x60;, &#x60;start__lte&#x60;  Example - [/expeditions/?space_station&#x3D;18](./?space_station&#x3D;18)  #### Search Fields searched - &#x60;crew__astronaut__agency__abbrev&#x60;, &#x60;crew__astronaut__agency__name&#x60;, &#x60;crew__astronaut__name&#x60;, &#x60;crew__astronaut__nationality__nationality_name&#x60;, &#x60;name&#x60;  Example - [/expeditions/?search&#x3D;Kelly](./?search&#x3D;Kelly)  #### Ordering Fields - &#x60;end&#x60;, &#x60;id&#x60;, &#x60;start&#x60;  Example - [/expeditions/?ordering&#x3D;-start](./?ordering&#x3D;-start)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/expeditions/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/expeditions/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = ExpeditionsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this expedition.
try {
    val result : ExpeditionDetailed = apiInstance.expeditionsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ExpeditionsApi#expeditionsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ExpeditionsApi#expeditionsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this expedition. | |

### Return type

[**ExpeditionDetailed**](ExpeditionDetailed.md)

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

