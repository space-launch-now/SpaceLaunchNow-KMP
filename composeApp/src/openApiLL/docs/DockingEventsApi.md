# DockingEventsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**dockingEventsList**](DockingEventsApi.md#dockingEventsList) | **GET** /api/ll/2.4.0/docking_events/ |  |
| [**dockingEventsRetrieve**](DockingEventsApi.md#dockingEventsRetrieve) | **GET** /api/ll/2.4.0/docking_events/{id}/ |  |


<a id="dockingEventsList"></a>
# **dockingEventsList**
> PaginatedDockingEventEndpointNormalList dockingEventsList(dockingDay, dockingGt, dockingGte, dockingLt, dockingLte, dockingMonth, dockingYear, dockingLocationId, flightVehicleChaserId, limit, offset, ordering, search, spaceStationTargetId)



#### Filters Parameters - &#x60;docking__day&#x60;, &#x60;docking__gt&#x60;, &#x60;docking__gte&#x60;, &#x60;docking__lt&#x60;, &#x60;docking__lte&#x60;, &#x60;docking__month&#x60;, &#x60;docking__year&#x60;, &#x60;docking_location__id&#x60;, &#x60;flight_vehicle_chaser__id&#x60;, &#x60;space_station_target__id&#x60;  Example - [/docking_events/?space_station_target__id&#x3D;6](./?space_station_target__id&#x3D;6)  #### Search Fields searched - &#x60;docking_location__name&#x60;, &#x60;flight_vehicle_chaser__spacecraft__name&#x60;, &#x60;flight_vehicle_target__spacecraft__name&#x60;, &#x60;payload_flight_chaser__payload__name&#x60;, &#x60;payload_flight_target__payload__name&#x60;, &#x60;space_station_chaser__name&#x60;, &#x60;space_station_target__name&#x60;  Example - [/docking_events/?search&#x3D;Salyut](./?search&#x3D;Salyut)  #### Ordering Fields - &#x60;departure&#x60;, &#x60;docking&#x60;  Example - [/docking_events/?ordering&#x3D;-docking](./?ordering&#x3D;-docking)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/docking_events/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/docking_events/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = DockingEventsApi()
val dockingDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dockingGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Docking is greater than
val dockingGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Docking is greater than or equal to
val dockingLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Docking is less than
val dockingLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Docking is less than or equal to
val dockingMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dockingYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dockingLocationId : kotlin.Int = 56 // kotlin.Int | 
val flightVehicleChaserId : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spaceStationTargetId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedDockingEventEndpointNormalList = apiInstance.dockingEventsList(dockingDay, dockingGt, dockingGte, dockingLt, dockingLte, dockingMonth, dockingYear, dockingLocationId, flightVehicleChaserId, limit, offset, ordering, search, spaceStationTargetId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DockingEventsApi#dockingEventsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DockingEventsApi#dockingEventsList")
    e.printStackTrace()
}
```

### Parameters
| **dockingDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dockingGt** | **kotlinx.datetime.Instant**| Docking is greater than | [optional] |
| **dockingGte** | **kotlinx.datetime.Instant**| Docking is greater than or equal to | [optional] |
| **dockingLt** | **kotlinx.datetime.Instant**| Docking is less than | [optional] |
| **dockingLte** | **kotlinx.datetime.Instant**| Docking is less than or equal to | [optional] |
| **dockingMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dockingYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dockingLocationId** | **kotlin.Int**|  | [optional] |
| **flightVehicleChaserId** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **spaceStationTargetId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedDockingEventEndpointNormalList**](PaginatedDockingEventEndpointNormalList.md)

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

<a id="dockingEventsRetrieve"></a>
# **dockingEventsRetrieve**
> DockingEventEndpointNormal dockingEventsRetrieve(id)



#### Filters Parameters - &#x60;docking__day&#x60;, &#x60;docking__gt&#x60;, &#x60;docking__gte&#x60;, &#x60;docking__lt&#x60;, &#x60;docking__lte&#x60;, &#x60;docking__month&#x60;, &#x60;docking__year&#x60;, &#x60;docking_location__id&#x60;, &#x60;flight_vehicle_chaser__id&#x60;, &#x60;space_station_target__id&#x60;  Example - [/docking_events/?space_station_target__id&#x3D;6](./?space_station_target__id&#x3D;6)  #### Search Fields searched - &#x60;docking_location__name&#x60;, &#x60;flight_vehicle_chaser__spacecraft__name&#x60;, &#x60;flight_vehicle_target__spacecraft__name&#x60;, &#x60;payload_flight_chaser__payload__name&#x60;, &#x60;payload_flight_target__payload__name&#x60;, &#x60;space_station_chaser__name&#x60;, &#x60;space_station_target__name&#x60;  Example - [/docking_events/?search&#x3D;Salyut](./?search&#x3D;Salyut)  #### Ordering Fields - &#x60;departure&#x60;, &#x60;docking&#x60;  Example - [/docking_events/?ordering&#x3D;-docking](./?ordering&#x3D;-docking)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/docking_events/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/docking_events/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = DockingEventsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this docking event.
try {
    val result : DockingEventEndpointNormal = apiInstance.dockingEventsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DockingEventsApi#dockingEventsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DockingEventsApi#dockingEventsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this docking event. | |

### Return type

[**DockingEventEndpointNormal**](DockingEventEndpointNormal.md)

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

