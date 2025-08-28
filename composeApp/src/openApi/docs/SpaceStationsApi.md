# SpaceStationsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**spaceStationsDetailedList**](SpaceStationsApi.md#spaceStationsDetailedList) | **GET** /api/ll/2.4.0/space_stations/detailed/ |  |
| [**spaceStationsList**](SpaceStationsApi.md#spaceStationsList) | **GET** /api/ll/2.4.0/space_stations/ |  |
| [**spaceStationsRetrieve**](SpaceStationsApi.md#spaceStationsRetrieve) | **GET** /api/ll/2.4.0/space_stations/{id}/ |  |


<a id="spaceStationsDetailedList"></a>
# **spaceStationsDetailedList**
> PaginatedSpaceStationDetailedEndpointList spaceStationsDetailedList(dockedVehicles, dockedVehiclesGt, dockedVehiclesGte, dockedVehiclesLt, dockedVehiclesLte, id, limit, name, nameContains, offset, onboardCrew, onboardCrewGt, onboardCrewGte, onboardCrewLt, onboardCrewLte, orbit, ordering, ownerIds, owners, search, status, statusIds, type)



#### Filters Parameters - &#x60;docked_vehicles&#x60;, &#x60;docked_vehicles__gt&#x60;, &#x60;docked_vehicles__gte&#x60;, &#x60;docked_vehicles__lt&#x60;, &#x60;docked_vehicles__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;onboard_crew&#x60;, &#x60;onboard_crew__gt&#x60;, &#x60;onboard_crew__gte&#x60;, &#x60;onboard_crew__lt&#x60;, &#x60;onboard_crew__lte&#x60;, &#x60;orbit&#x60;, &#x60;owner__ids&#x60;, &#x60;owners&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;type&#x60;  Example - [/space_stations/detailed/?onboard_crew__gte&#x3D;1](./?onboard_crew__gte&#x3D;1)  #### Search Fields searched - &#x60;name&#x60;, &#x60;owners__abbrev&#x60;, &#x60;owners__name&#x60;  Example - [/space_stations/detailed/?search&#x3D;Salyut](./?search&#x3D;Salyut)  #### Ordering Fields - &#x60;docked_vehicles&#x60;, &#x60;founded&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;onboard_crew&#x60;, &#x60;status&#x60;, &#x60;type&#x60;, &#x60;volume&#x60;  Example - [/space_stations/detailed/?ordering&#x3D;founded](./?ordering&#x3D;founded)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/space_stations/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/space_stations/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = SpaceStationsApi()
val dockedVehicles : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesGt : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesGte : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesLt : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val onboardCrew : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewGt : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewGte : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewLt : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewLte : kotlin.Int = 56 // kotlin.Int | 
val orbit : kotlin.Int = 56 // kotlin.Int | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val ownerIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs.
val owners : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val status : kotlin.Int = 56 // kotlin.Int | 
val statusIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated spacestation status IDs.
val type : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpaceStationDetailedEndpointList = apiInstance.spaceStationsDetailedList(dockedVehicles, dockedVehiclesGt, dockedVehiclesGte, dockedVehiclesLt, dockedVehiclesLte, id, limit, name, nameContains, offset, onboardCrew, onboardCrewGt, onboardCrewGte, onboardCrewLt, onboardCrewLte, orbit, ordering, ownerIds, owners, search, status, statusIds, type)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpaceStationsApi#spaceStationsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpaceStationsApi#spaceStationsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **dockedVehicles** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesGt** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesGte** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesLt** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **onboardCrew** | **kotlin.Int**|  | [optional] |
| **onboardCrewGt** | **kotlin.Int**|  | [optional] |
| **onboardCrewGte** | **kotlin.Int**|  | [optional] |
| **onboardCrewLt** | **kotlin.Int**|  | [optional] |
| **onboardCrewLte** | **kotlin.Int**|  | [optional] |
| **orbit** | **kotlin.Int**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **ownerIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs. | [optional] |
| **owners** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated spacestation status IDs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **type** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpaceStationDetailedEndpointList**](PaginatedSpaceStationDetailedEndpointList.md)

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

<a id="spaceStationsList"></a>
# **spaceStationsList**
> PaginatedSpaceStationEndpointList spaceStationsList(dockedVehicles, dockedVehiclesGt, dockedVehiclesGte, dockedVehiclesLt, dockedVehiclesLte, id, limit, name, nameContains, offset, onboardCrew, onboardCrewGt, onboardCrewGte, onboardCrewLt, onboardCrewLte, orbit, ordering, ownerIds, owners, search, status, statusIds, type)



#### Filters Parameters - &#x60;docked_vehicles&#x60;, &#x60;docked_vehicles__gt&#x60;, &#x60;docked_vehicles__gte&#x60;, &#x60;docked_vehicles__lt&#x60;, &#x60;docked_vehicles__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;onboard_crew&#x60;, &#x60;onboard_crew__gt&#x60;, &#x60;onboard_crew__gte&#x60;, &#x60;onboard_crew__lt&#x60;, &#x60;onboard_crew__lte&#x60;, &#x60;orbit&#x60;, &#x60;owner__ids&#x60;, &#x60;owners&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;type&#x60;  Example - [/space_stations/?onboard_crew__gte&#x3D;1](./?onboard_crew__gte&#x3D;1)  #### Search Fields searched - &#x60;name&#x60;, &#x60;owners__abbrev&#x60;, &#x60;owners__name&#x60;  Example - [/space_stations/?search&#x3D;Salyut](./?search&#x3D;Salyut)  #### Ordering Fields - &#x60;docked_vehicles&#x60;, &#x60;founded&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;onboard_crew&#x60;, &#x60;status&#x60;, &#x60;type&#x60;, &#x60;volume&#x60;  Example - [/space_stations/?ordering&#x3D;founded](./?ordering&#x3D;founded)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/space_stations/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/space_stations/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = SpaceStationsApi()
val dockedVehicles : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesGt : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesGte : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesLt : kotlin.Int = 56 // kotlin.Int | 
val dockedVehiclesLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val onboardCrew : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewGt : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewGte : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewLt : kotlin.Int = 56 // kotlin.Int | 
val onboardCrewLte : kotlin.Int = 56 // kotlin.Int | 
val orbit : kotlin.Int = 56 // kotlin.Int | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val ownerIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs.
val owners : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val status : kotlin.Int = 56 // kotlin.Int | 
val statusIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated spacestation status IDs.
val type : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedSpaceStationEndpointList = apiInstance.spaceStationsList(dockedVehicles, dockedVehiclesGt, dockedVehiclesGte, dockedVehiclesLt, dockedVehiclesLte, id, limit, name, nameContains, offset, onboardCrew, onboardCrewGt, onboardCrewGte, onboardCrewLt, onboardCrewLte, orbit, ordering, ownerIds, owners, search, status, statusIds, type)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpaceStationsApi#spaceStationsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpaceStationsApi#spaceStationsList")
    e.printStackTrace()
}
```

### Parameters
| **dockedVehicles** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesGt** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesGte** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesLt** | **kotlin.Int**|  | [optional] |
| **dockedVehiclesLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **onboardCrew** | **kotlin.Int**|  | [optional] |
| **onboardCrewGt** | **kotlin.Int**|  | [optional] |
| **onboardCrewGte** | **kotlin.Int**|  | [optional] |
| **onboardCrewLt** | **kotlin.Int**|  | [optional] |
| **onboardCrewLte** | **kotlin.Int**|  | [optional] |
| **orbit** | **kotlin.Int**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **ownerIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs. | [optional] |
| **owners** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated spacestation status IDs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **type** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedSpaceStationEndpointList**](PaginatedSpaceStationEndpointList.md)

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

<a id="spaceStationsRetrieve"></a>
# **spaceStationsRetrieve**
> SpaceStationDetailedEndpoint spaceStationsRetrieve(id)



#### Filters Parameters - &#x60;docked_vehicles&#x60;, &#x60;docked_vehicles__gt&#x60;, &#x60;docked_vehicles__gte&#x60;, &#x60;docked_vehicles__lt&#x60;, &#x60;docked_vehicles__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;onboard_crew&#x60;, &#x60;onboard_crew__gt&#x60;, &#x60;onboard_crew__gte&#x60;, &#x60;onboard_crew__lt&#x60;, &#x60;onboard_crew__lte&#x60;, &#x60;orbit&#x60;, &#x60;owner__ids&#x60;, &#x60;owners&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;type&#x60;  Example - [/space_stations/?onboard_crew__gte&#x3D;1](./?onboard_crew__gte&#x3D;1)  #### Search Fields searched - &#x60;name&#x60;, &#x60;owners__abbrev&#x60;, &#x60;owners__name&#x60;  Example - [/space_stations/?search&#x3D;Salyut](./?search&#x3D;Salyut)  #### Ordering Fields - &#x60;docked_vehicles&#x60;, &#x60;founded&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;onboard_crew&#x60;, &#x60;status&#x60;, &#x60;type&#x60;, &#x60;volume&#x60;  Example - [/space_stations/?ordering&#x3D;founded](./?ordering&#x3D;founded)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/space_stations/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/space_stations/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = SpaceStationsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Space Station.
try {
    val result : SpaceStationDetailedEndpoint = apiInstance.spaceStationsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling SpaceStationsApi#spaceStationsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling SpaceStationsApi#spaceStationsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Space Station. | |

### Return type

[**SpaceStationDetailedEndpoint**](SpaceStationDetailedEndpoint.md)

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

