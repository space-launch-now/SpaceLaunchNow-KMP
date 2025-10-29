# SpacecraftFlightsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**spacecraftFlightsDetailedList**](SpacecraftFlightsApi.md#spacecraftFlightsDetailedList) | **GET** /api/ll/2.4.0/spacecraft_flights/detailed/ |  |
| [**spacecraftFlightsList**](SpacecraftFlightsApi.md#spacecraftFlightsList) | **GET** /api/ll/2.4.0/spacecraft_flights/ |  |
| [**spacecraftFlightsMiniList**](SpacecraftFlightsApi.md#spacecraftFlightsMiniList) | **GET** /api/ll/2.4.0/spacecraft_flights/mini/ |  |
| [**spacecraftFlightsRetrieve**](SpacecraftFlightsApi.md#spacecraftFlightsRetrieve) | **GET** /api/ll/2.4.0/spacecraft_flights/{id}/ |  |


<a id="spacecraftFlightsDetailedList"></a>
# **spacecraftFlightsDetailedList**
> PaginatedSpacecraftFlightDetailedList spacecraftFlightsDetailedList(limit, missionEnd, missionEndDay, missionEndGt, missionEndGte, missionEndLt, missionEndLte, missionEndMonth, missionEndYear, offset, ordering, search, spacecraft)



#### Filters Parameters - &#x60;mission_end&#x60;, &#x60;mission_end__day&#x60;, &#x60;mission_end__gt&#x60;, &#x60;mission_end__gte&#x60;, &#x60;mission_end__lt&#x60;, &#x60;mission_end__lte&#x60;, &#x60;mission_end__month&#x60;, &#x60;mission_end__year&#x60;, &#x60;spacecraft&#x60;  Example - [/spacecraft_flights/detailed/?spacecraft&#x3D;289](./?spacecraft&#x3D;289)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_flights/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_flights/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacecraftFlightsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val missionEnd : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val missionEndGt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndLt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val missionEndYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraft : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpacecraftFlightDetailedList = apiInstance.spacecraftFlightsDetailedList(limit, missionEnd, missionEndDay, missionEndGt, missionEndGte, missionEndLt, missionEndLte, missionEndMonth, missionEndYear, offset, ordering, search, spacecraft)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftFlightsApi#spacecraftFlightsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftFlightsApi#spacecraftFlightsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **missionEnd** | **kotlin.time.Instant**|  | [optional] |
| **missionEndDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **missionEndGt** | **kotlin.time.Instant**|  | [optional] |
| **missionEndGte** | **kotlin.time.Instant**|  | [optional] |
| **missionEndLt** | **kotlin.time.Instant**|  | [optional] |
| **missionEndLte** | **kotlin.time.Instant**|  | [optional] |
| **missionEndMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **missionEndYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **spacecraft** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpacecraftFlightDetailedList**](PaginatedSpacecraftFlightDetailedList.md)

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

<a id="spacecraftFlightsList"></a>
# **spacecraftFlightsList**
> PaginatedSpacecraftFlightNormalList spacecraftFlightsList(limit, missionEnd, missionEndDay, missionEndGt, missionEndGte, missionEndLt, missionEndLte, missionEndMonth, missionEndYear, offset, ordering, search, spacecraft)



#### Filters Parameters - &#x60;mission_end&#x60;, &#x60;mission_end__day&#x60;, &#x60;mission_end__gt&#x60;, &#x60;mission_end__gte&#x60;, &#x60;mission_end__lt&#x60;, &#x60;mission_end__lte&#x60;, &#x60;mission_end__month&#x60;, &#x60;mission_end__year&#x60;, &#x60;spacecraft&#x60;  Example - [/spacecraft_flights/?spacecraft&#x3D;289](./?spacecraft&#x3D;289)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_flights/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_flights/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacecraftFlightsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val missionEnd : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val missionEndGt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndLt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val missionEndYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraft : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpacecraftFlightNormalList = apiInstance.spacecraftFlightsList(limit, missionEnd, missionEndDay, missionEndGt, missionEndGte, missionEndLt, missionEndLte, missionEndMonth, missionEndYear, offset, ordering, search, spacecraft)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftFlightsApi#spacecraftFlightsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftFlightsApi#spacecraftFlightsList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **missionEnd** | **kotlin.time.Instant**|  | [optional] |
| **missionEndDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **missionEndGt** | **kotlin.time.Instant**|  | [optional] |
| **missionEndGte** | **kotlin.time.Instant**|  | [optional] |
| **missionEndLt** | **kotlin.time.Instant**|  | [optional] |
| **missionEndLte** | **kotlin.time.Instant**|  | [optional] |
| **missionEndMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **missionEndYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **spacecraft** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpacecraftFlightNormalList**](PaginatedSpacecraftFlightNormalList.md)

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

<a id="spacecraftFlightsMiniList"></a>
# **spacecraftFlightsMiniList**
> PaginatedSpacecraftFlightMiniList spacecraftFlightsMiniList(limit, missionEnd, missionEndDay, missionEndGt, missionEndGte, missionEndLt, missionEndLte, missionEndMonth, missionEndYear, offset, ordering, search, spacecraft)



#### Filters Parameters - &#x60;mission_end&#x60;, &#x60;mission_end__day&#x60;, &#x60;mission_end__gt&#x60;, &#x60;mission_end__gte&#x60;, &#x60;mission_end__lt&#x60;, &#x60;mission_end__lte&#x60;, &#x60;mission_end__month&#x60;, &#x60;mission_end__year&#x60;, &#x60;spacecraft&#x60;  Example - [/spacecraft_flights/mini/?spacecraft&#x3D;289](./?spacecraft&#x3D;289)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_flights/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_flights/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacecraftFlightsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val missionEnd : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val missionEndGt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndGte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndLt : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndLte : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val missionEndMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val missionEndYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraft : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpacecraftFlightMiniList = apiInstance.spacecraftFlightsMiniList(limit, missionEnd, missionEndDay, missionEndGt, missionEndGte, missionEndLt, missionEndLte, missionEndMonth, missionEndYear, offset, ordering, search, spacecraft)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftFlightsApi#spacecraftFlightsMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftFlightsApi#spacecraftFlightsMiniList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **missionEnd** | **kotlin.time.Instant**|  | [optional] |
| **missionEndDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **missionEndGt** | **kotlin.time.Instant**|  | [optional] |
| **missionEndGte** | **kotlin.time.Instant**|  | [optional] |
| **missionEndLt** | **kotlin.time.Instant**|  | [optional] |
| **missionEndLte** | **kotlin.time.Instant**|  | [optional] |
| **missionEndMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **missionEndYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **spacecraft** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpacecraftFlightMiniList**](PaginatedSpacecraftFlightMiniList.md)

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

<a id="spacecraftFlightsRetrieve"></a>
# **spacecraftFlightsRetrieve**
> SpacecraftFlightDetailed spacecraftFlightsRetrieve(id)



#### Filters Parameters - &#x60;mission_end&#x60;, &#x60;mission_end__day&#x60;, &#x60;mission_end__gt&#x60;, &#x60;mission_end__gte&#x60;, &#x60;mission_end__lt&#x60;, &#x60;mission_end__lte&#x60;, &#x60;mission_end__month&#x60;, &#x60;mission_end__year&#x60;, &#x60;spacecraft&#x60;  Example - [/spacecraft_flights/?spacecraft&#x3D;289](./?spacecraft&#x3D;289)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/spacecraft_flights/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/spacecraft_flights/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = SpacecraftFlightsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Spacecraft Flight.
try {
    val result : SpacecraftFlightDetailed = apiInstance.spacecraftFlightsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpacecraftFlightsApi#spacecraftFlightsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpacecraftFlightsApi#spacecraftFlightsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Spacecraft Flight. | |

### Return type

[**SpacecraftFlightDetailed**](SpacecraftFlightDetailed.md)

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

