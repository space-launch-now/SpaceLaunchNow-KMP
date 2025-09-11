# EventsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**eventsDetailedList**](EventsApi.md#eventsDetailedList) | **GET** /api/ll/2.4.0/events/detailed/ |  |
| [**eventsList**](EventsApi.md#eventsList) | **GET** /api/ll/2.4.0/events/ |  |
| [**eventsMiniList**](EventsApi.md#eventsMiniList) | **GET** /api/ll/2.4.0/events/mini/ |  |
| [**eventsRetrieve**](EventsApi.md#eventsRetrieve) | **GET** /api/ll/2.4.0/events/{id}/ |  |


<a id="eventsDetailedList"></a>
# **eventsDetailedList**
> PaginatedEventEndpointDetailedList eventsDetailedList(agencyIds, dateGt, dateGte, dateLt, dateLte, day, id, lastUpdatedGte, lastUpdatedLte, limit, month, offset, ordering, previous, program, search, slug, type, typeIds, upcoming, upcomingWithRecent, videoUrl, year)



#### Filters Parameters - &#x60;agency__ids&#x60;, &#x60;date__gt&#x60;, &#x60;date__gte&#x60;, &#x60;date__lt&#x60;, &#x60;date__lte&#x60;, &#x60;day&#x60;, &#x60;id&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;month&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;slug&#x60;, &#x60;type&#x60;, &#x60;type__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;year&#x60;  Example - [/events/detailed/?type__ids&#x3D;2,8](./?type__ids&#x3D;2,8)  #### Search Fields searched - &#x60;name&#x60;  Example - [/events/detailed/?search&#x3D;Flyby](./?search&#x3D;Flyby)  #### Ordering Fields - &#x60;date&#x60;, &#x60;last_updated&#x60;  Example - [/events/detailed/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/events/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/events/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = EventsApi()
val agencyIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs.
val dateGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is greater than
val dateGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is greater than or equal to
val dateLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is less than
val dateLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is less than or equal to
val day : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val id : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val lastUpdatedGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val month : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val previous : kotlin.Boolean = true // kotlin.Boolean | Filter for events that have already occurred (up to current time).
val program : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val slug : kotlin.String = slug_example // kotlin.String | 
val type : kotlin.Int = 56 // kotlin.Int | 
val typeIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated event type IDs.
val upcoming : kotlin.Boolean = true // kotlin.Boolean | Filter for events upcoming within the next period (up to current time).
val upcomingWithRecent : kotlin.Boolean = true // kotlin.Boolean | Filter for events upcoming within the next period (from 1 day ago onwards).
val videoUrl : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated video URLs.
val year : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
try {
    val result : PaginatedEventEndpointDetailedList = apiInstance.eventsDetailedList(agencyIds, dateGt, dateGte, dateLt, dateLte, day, id, lastUpdatedGte, lastUpdatedLte, limit, month, offset, ordering, previous, program, search, slug, type, typeIds, upcoming, upcomingWithRecent, videoUrl, year)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventsApi#eventsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventsApi#eventsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **agencyIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs. | [optional] |
| **dateGt** | **kotlinx.datetime.Instant**| Date is greater than | [optional] |
| **dateGte** | **kotlinx.datetime.Instant**| Date is greater than or equal to | [optional] |
| **dateLt** | **kotlinx.datetime.Instant**| Date is less than | [optional] |
| **dateLte** | **kotlinx.datetime.Instant**| Date is less than or equal to | [optional] |
| **day** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **id** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **lastUpdatedGte** | **kotlinx.datetime.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlinx.datetime.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **month** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **previous** | **kotlin.Boolean**| Filter for events that have already occurred (up to current time). | [optional] |
| **program** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **slug** | **kotlin.String**|  | [optional] |
| **type** | **kotlin.Int**|  | [optional] |
| **typeIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated event type IDs. | [optional] |
| **upcoming** | **kotlin.Boolean**| Filter for events upcoming within the next period (up to current time). | [optional] |
| **upcomingWithRecent** | **kotlin.Boolean**| Filter for events upcoming within the next period (from 1 day ago onwards). | [optional] |
| **videoUrl** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated video URLs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **year** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |

### Return type

[**PaginatedEventEndpointDetailedList**](PaginatedEventEndpointDetailedList.md)

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

<a id="eventsList"></a>
# **eventsList**
> PaginatedEventEndpointNormalList eventsList(agencyIds, dateGt, dateGte, dateLt, dateLte, day, id, lastUpdatedGte, lastUpdatedLte, limit, month, offset, ordering, previous, program, search, slug, type, typeIds, upcoming, upcomingWithRecent, videoUrl, year)



#### Filters Parameters - &#x60;agency__ids&#x60;, &#x60;date__gt&#x60;, &#x60;date__gte&#x60;, &#x60;date__lt&#x60;, &#x60;date__lte&#x60;, &#x60;day&#x60;, &#x60;id&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;month&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;slug&#x60;, &#x60;type&#x60;, &#x60;type__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;year&#x60;  Example - [/events/?type__ids&#x3D;2,8](./?type__ids&#x3D;2,8)  #### Search Fields searched - &#x60;name&#x60;  Example - [/events/?search&#x3D;Flyby](./?search&#x3D;Flyby)  #### Ordering Fields - &#x60;date&#x60;, &#x60;last_updated&#x60;  Example - [/events/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/events/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/events/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = EventsApi()
val agencyIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs.
val dateGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is greater than
val dateGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is greater than or equal to
val dateLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is less than
val dateLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is less than or equal to
val day : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val id : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val lastUpdatedGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val month : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val previous : kotlin.Boolean = true // kotlin.Boolean | Filter for events that have already occurred (up to current time).
val program : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val slug : kotlin.String = slug_example // kotlin.String | 
val type : kotlin.Int = 56 // kotlin.Int | 
val typeIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated event type IDs.
val upcoming : kotlin.Boolean = true // kotlin.Boolean | Filter for events upcoming within the next period (up to current time).
val upcomingWithRecent : kotlin.Boolean = true // kotlin.Boolean | Filter for events upcoming within the next period (from 1 day ago onwards).
val videoUrl : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated video URLs.
val year : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
try {
    val result : PaginatedEventEndpointNormalList = apiInstance.eventsList(agencyIds, dateGt, dateGte, dateLt, dateLte, day, id, lastUpdatedGte, lastUpdatedLte, limit, month, offset, ordering, previous, program, search, slug, type, typeIds, upcoming, upcomingWithRecent, videoUrl, year)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventsApi#eventsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventsApi#eventsList")
    e.printStackTrace()
}
```

### Parameters
| **agencyIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs. | [optional] |
| **dateGt** | **kotlinx.datetime.Instant**| Date is greater than | [optional] |
| **dateGte** | **kotlinx.datetime.Instant**| Date is greater than or equal to | [optional] |
| **dateLt** | **kotlinx.datetime.Instant**| Date is less than | [optional] |
| **dateLte** | **kotlinx.datetime.Instant**| Date is less than or equal to | [optional] |
| **day** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **id** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **lastUpdatedGte** | **kotlinx.datetime.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlinx.datetime.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **month** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **previous** | **kotlin.Boolean**| Filter for events that have already occurred (up to current time). | [optional] |
| **program** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **slug** | **kotlin.String**|  | [optional] |
| **type** | **kotlin.Int**|  | [optional] |
| **typeIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated event type IDs. | [optional] |
| **upcoming** | **kotlin.Boolean**| Filter for events upcoming within the next period (up to current time). | [optional] |
| **upcomingWithRecent** | **kotlin.Boolean**| Filter for events upcoming within the next period (from 1 day ago onwards). | [optional] |
| **videoUrl** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated video URLs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **year** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |

### Return type

[**PaginatedEventEndpointNormalList**](PaginatedEventEndpointNormalList.md)

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

<a id="eventsMiniList"></a>
# **eventsMiniList**
> PaginatedEventEndpointListList eventsMiniList(agencyIds, dateGt, dateGte, dateLt, dateLte, day, id, lastUpdatedGte, lastUpdatedLte, limit, month, offset, ordering, previous, program, search, slug, type, typeIds, upcoming, upcomingWithRecent, videoUrl, year)



#### Filters Parameters - &#x60;agency__ids&#x60;, &#x60;date__gt&#x60;, &#x60;date__gte&#x60;, &#x60;date__lt&#x60;, &#x60;date__lte&#x60;, &#x60;day&#x60;, &#x60;id&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;month&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;slug&#x60;, &#x60;type&#x60;, &#x60;type__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;year&#x60;  Example - [/events/mini/?type__ids&#x3D;2,8](./?type__ids&#x3D;2,8)  #### Search Fields searched - &#x60;name&#x60;  Example - [/events/mini/?search&#x3D;Flyby](./?search&#x3D;Flyby)  #### Ordering Fields - &#x60;date&#x60;, &#x60;last_updated&#x60;  Example - [/events/mini/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/events/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/events/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = EventsApi()
val agencyIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs.
val dateGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is greater than
val dateGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is greater than or equal to
val dateLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is less than
val dateLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Date is less than or equal to
val day : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val id : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val lastUpdatedGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is less than or equal to
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val month : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val previous : kotlin.Boolean = true // kotlin.Boolean | Filter for events that have already occurred (up to current time).
val program : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val slug : kotlin.String = slug_example // kotlin.String | 
val type : kotlin.Int = 56 // kotlin.Int | 
val typeIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated event type IDs.
val upcoming : kotlin.Boolean = true // kotlin.Boolean | Filter for events upcoming within the next period (up to current time).
val upcomingWithRecent : kotlin.Boolean = true // kotlin.Boolean | Filter for events upcoming within the next period (from 1 day ago onwards).
val videoUrl : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated video URLs.
val year : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
try {
    val result : PaginatedEventEndpointListList = apiInstance.eventsMiniList(agencyIds, dateGt, dateGte, dateLt, dateLte, day, id, lastUpdatedGte, lastUpdatedLte, limit, month, offset, ordering, previous, program, search, slug, type, typeIds, upcoming, upcomingWithRecent, videoUrl, year)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventsApi#eventsMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventsApi#eventsMiniList")
    e.printStackTrace()
}
```

### Parameters
| **agencyIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs. | [optional] |
| **dateGt** | **kotlinx.datetime.Instant**| Date is greater than | [optional] |
| **dateGte** | **kotlinx.datetime.Instant**| Date is greater than or equal to | [optional] |
| **dateLt** | **kotlinx.datetime.Instant**| Date is less than | [optional] |
| **dateLte** | **kotlinx.datetime.Instant**| Date is less than or equal to | [optional] |
| **day** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **id** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **lastUpdatedGte** | **kotlinx.datetime.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlinx.datetime.Instant**| Last Update is less than or equal to | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **month** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **previous** | **kotlin.Boolean**| Filter for events that have already occurred (up to current time). | [optional] |
| **program** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **slug** | **kotlin.String**|  | [optional] |
| **type** | **kotlin.Int**|  | [optional] |
| **typeIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated event type IDs. | [optional] |
| **upcoming** | **kotlin.Boolean**| Filter for events upcoming within the next period (up to current time). | [optional] |
| **upcomingWithRecent** | **kotlin.Boolean**| Filter for events upcoming within the next period (from 1 day ago onwards). | [optional] |
| **videoUrl** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated video URLs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **year** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |

### Return type

[**PaginatedEventEndpointListList**](PaginatedEventEndpointListList.md)

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

<a id="eventsRetrieve"></a>
# **eventsRetrieve**
> EventEndpointDetailed eventsRetrieve(id)



#### Filters Parameters - &#x60;agency__ids&#x60;, &#x60;date__gt&#x60;, &#x60;date__gte&#x60;, &#x60;date__lt&#x60;, &#x60;date__lte&#x60;, &#x60;day&#x60;, &#x60;id&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;month&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;slug&#x60;, &#x60;type&#x60;, &#x60;type__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;year&#x60;  Example - [/events/?type__ids&#x3D;2,8](./?type__ids&#x3D;2,8)  #### Search Fields searched - &#x60;name&#x60;  Example - [/events/?search&#x3D;Flyby](./?search&#x3D;Flyby)  #### Ordering Fields - &#x60;date&#x60;, &#x60;last_updated&#x60;  Example - [/events/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/events/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/events/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = EventsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Event.
try {
    val result : EventEndpointDetailed = apiInstance.eventsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventsApi#eventsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventsApi#eventsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Event. | |

### Return type

[**EventEndpointDetailed**](EventEndpointDetailed.md)

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

