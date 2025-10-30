# LaunchersApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**launchersDetailedList**](LaunchersApi.md#launchersDetailedList) | **GET** /api/ll/2.4.0/launchers/detailed/ |  |
| [**launchersList**](LaunchersApi.md#launchersList) | **GET** /api/ll/2.4.0/launchers/ |  |
| [**launchersMiniList**](LaunchersApi.md#launchersMiniList) | **GET** /api/ll/2.4.0/launchers/mini/ |  |
| [**launchersRetrieve**](LaunchersApi.md#launchersRetrieve) | **GET** /api/ll/2.4.0/launchers/{id}/ |  |


<a id="launchersDetailedList"></a>
# **launchersDetailedList**
> PaginatedLauncherDetailedList launchersDetailedList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, firstLaunchDate, firstLaunchDateDay, firstLaunchDateMonth, firstLaunchDateYear, flightProven, flights, flightsGt, flightsGte, flightsLt, flightsLte, id, idContains, isPlaceholder, lastLaunchDate, launcherConfigIds, launcherConfigManufacturerName, launcherConfigManufacturerNameContains, limit, offset, ordering, search, serialNumber, serialNumberContains, status, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte)



#### Filters Parameters - &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;first_launch_date&#x60;, &#x60;first_launch_date__day&#x60;, &#x60;first_launch_date__month&#x60;, &#x60;first_launch_date__year&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;flights__gt&#x60;, &#x60;flights__gte&#x60;, &#x60;flights__lt&#x60;, &#x60;flights__lte&#x60;, &#x60;id&#x60;, &#x60;id__contains&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_launch_date&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_config__manufacturer__name&#x60;, &#x60;launcher_config__manufacturer__name__contains&#x60;, &#x60;serial_number&#x60;, &#x60;serial_number__contains&#x60;, &#x60;status&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;  Example - [/launchers/detailed/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;serial_number&#x60;, &#x60;status__name&#x60;  Example - [/launchers/detailed/?search&#x3D;B1048](./?search&#x3D;B1048)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;id&#x60;, &#x60;successful_landings&#x60;  Example - [/launchers/detailed/?ordering&#x3D;-flights](./?ordering&#x3D;-flights)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launchers/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launchers/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchersApi()
val attemptedLandings : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val firstLaunchDate : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val firstLaunchDateDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date days.
val firstLaunchDateMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date months.
val firstLaunchDateYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date years.
val flightProven : kotlin.Boolean = true // kotlin.Boolean | 
val flights : kotlin.Int = 56 // kotlin.Int | 
val flightsGt : kotlin.Int = 56 // kotlin.Int | 
val flightsGte : kotlin.Int = 56 // kotlin.Int | 
val flightsLt : kotlin.Int = 56 // kotlin.Int | 
val flightsLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val idContains : kotlin.Int = 56 // kotlin.Int | 
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | 
val lastLaunchDate : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val launcherConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launcher config IDs.
val launcherConfigManufacturerName : kotlin.String = launcherConfigManufacturerName_example // kotlin.String | 
val launcherConfigManufacturerNameContains : kotlin.String = launcherConfigManufacturerNameContains_example // kotlin.String | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val serialNumber : kotlin.String = serialNumber_example // kotlin.String | 
val serialNumberContains : kotlin.String = serialNumberContains_example // kotlin.String | 
val status : kotlin.Int = 56 // kotlin.Int | 
val successfulLandings : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedLauncherDetailedList = apiInstance.launchersDetailedList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, firstLaunchDate, firstLaunchDateDay, firstLaunchDateMonth, firstLaunchDateYear, flightProven, flights, flightsGt, flightsGte, flightsLt, flightsLte, id, idContains, isPlaceholder, lastLaunchDate, launcherConfigIds, launcherConfigManufacturerName, launcherConfigManufacturerNameContains, limit, offset, ordering, search, serialNumber, serialNumberContains, status, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchersApi#launchersDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchersApi#launchersDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **attemptedLandings** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGte** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLte** | **kotlin.Int**|  | [optional] |
| **firstLaunchDate** | **kotlin.time.Instant**|  | [optional] |
| **firstLaunchDateDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date days. | [optional] |
| **firstLaunchDateMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date months. | [optional] |
| **firstLaunchDateYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date years. | [optional] |
| **flightProven** | **kotlin.Boolean**|  | [optional] |
| **flights** | **kotlin.Int**|  | [optional] |
| **flightsGt** | **kotlin.Int**|  | [optional] |
| **flightsGte** | **kotlin.Int**|  | [optional] |
| **flightsLt** | **kotlin.Int**|  | [optional] |
| **flightsLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **idContains** | **kotlin.Int**|  | [optional] |
| **isPlaceholder** | **kotlin.Boolean**|  | [optional] |
| **lastLaunchDate** | **kotlin.time.Instant**|  | [optional] |
| **launcherConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launcher config IDs. | [optional] |
| **launcherConfigManufacturerName** | **kotlin.String**|  | [optional] |
| **launcherConfigManufacturerNameContains** | **kotlin.String**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **serialNumber** | **kotlin.String**|  | [optional] |
| **serialNumberContains** | **kotlin.String**|  | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **successfulLandings** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGt** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGte** | **kotlin.Int**|  | [optional] |
| **successfulLandingsLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **successfulLandingsLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedLauncherDetailedList**](PaginatedLauncherDetailedList.md)

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

<a id="launchersList"></a>
# **launchersList**
> PaginatedLauncherNormalList launchersList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, firstLaunchDate, firstLaunchDateDay, firstLaunchDateMonth, firstLaunchDateYear, flightProven, flights, flightsGt, flightsGte, flightsLt, flightsLte, id, idContains, isPlaceholder, lastLaunchDate, launcherConfigIds, launcherConfigManufacturerName, launcherConfigManufacturerNameContains, limit, offset, ordering, search, serialNumber, serialNumberContains, status, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte)



#### Filters Parameters - &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;first_launch_date&#x60;, &#x60;first_launch_date__day&#x60;, &#x60;first_launch_date__month&#x60;, &#x60;first_launch_date__year&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;flights__gt&#x60;, &#x60;flights__gte&#x60;, &#x60;flights__lt&#x60;, &#x60;flights__lte&#x60;, &#x60;id&#x60;, &#x60;id__contains&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_launch_date&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_config__manufacturer__name&#x60;, &#x60;launcher_config__manufacturer__name__contains&#x60;, &#x60;serial_number&#x60;, &#x60;serial_number__contains&#x60;, &#x60;status&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;  Example - [/launchers/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;serial_number&#x60;, &#x60;status__name&#x60;  Example - [/launchers/?search&#x3D;B1048](./?search&#x3D;B1048)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;id&#x60;, &#x60;successful_landings&#x60;  Example - [/launchers/?ordering&#x3D;-flights](./?ordering&#x3D;-flights)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launchers/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launchers/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchersApi()
val attemptedLandings : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val firstLaunchDate : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val firstLaunchDateDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date days.
val firstLaunchDateMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date months.
val firstLaunchDateYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date years.
val flightProven : kotlin.Boolean = true // kotlin.Boolean | 
val flights : kotlin.Int = 56 // kotlin.Int | 
val flightsGt : kotlin.Int = 56 // kotlin.Int | 
val flightsGte : kotlin.Int = 56 // kotlin.Int | 
val flightsLt : kotlin.Int = 56 // kotlin.Int | 
val flightsLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val idContains : kotlin.Int = 56 // kotlin.Int | 
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | 
val lastLaunchDate : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val launcherConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launcher config IDs.
val launcherConfigManufacturerName : kotlin.String = launcherConfigManufacturerName_example // kotlin.String | 
val launcherConfigManufacturerNameContains : kotlin.String = launcherConfigManufacturerNameContains_example // kotlin.String | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val serialNumber : kotlin.String = serialNumber_example // kotlin.String | 
val serialNumberContains : kotlin.String = serialNumberContains_example // kotlin.String | 
val status : kotlin.Int = 56 // kotlin.Int | 
val successfulLandings : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedLauncherNormalList = apiInstance.launchersList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, firstLaunchDate, firstLaunchDateDay, firstLaunchDateMonth, firstLaunchDateYear, flightProven, flights, flightsGt, flightsGte, flightsLt, flightsLte, id, idContains, isPlaceholder, lastLaunchDate, launcherConfigIds, launcherConfigManufacturerName, launcherConfigManufacturerNameContains, limit, offset, ordering, search, serialNumber, serialNumberContains, status, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchersApi#launchersList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchersApi#launchersList")
    e.printStackTrace()
}
```

### Parameters
| **attemptedLandings** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGte** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLte** | **kotlin.Int**|  | [optional] |
| **firstLaunchDate** | **kotlin.time.Instant**|  | [optional] |
| **firstLaunchDateDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date days. | [optional] |
| **firstLaunchDateMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date months. | [optional] |
| **firstLaunchDateYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date years. | [optional] |
| **flightProven** | **kotlin.Boolean**|  | [optional] |
| **flights** | **kotlin.Int**|  | [optional] |
| **flightsGt** | **kotlin.Int**|  | [optional] |
| **flightsGte** | **kotlin.Int**|  | [optional] |
| **flightsLt** | **kotlin.Int**|  | [optional] |
| **flightsLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **idContains** | **kotlin.Int**|  | [optional] |
| **isPlaceholder** | **kotlin.Boolean**|  | [optional] |
| **lastLaunchDate** | **kotlin.time.Instant**|  | [optional] |
| **launcherConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launcher config IDs. | [optional] |
| **launcherConfigManufacturerName** | **kotlin.String**|  | [optional] |
| **launcherConfigManufacturerNameContains** | **kotlin.String**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **serialNumber** | **kotlin.String**|  | [optional] |
| **serialNumberContains** | **kotlin.String**|  | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **successfulLandings** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGt** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGte** | **kotlin.Int**|  | [optional] |
| **successfulLandingsLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **successfulLandingsLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedLauncherNormalList**](PaginatedLauncherNormalList.md)

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

<a id="launchersMiniList"></a>
# **launchersMiniList**
> PaginatedLauncherMiniList launchersMiniList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, firstLaunchDate, firstLaunchDateDay, firstLaunchDateMonth, firstLaunchDateYear, flightProven, flights, flightsGt, flightsGte, flightsLt, flightsLte, id, idContains, isPlaceholder, lastLaunchDate, launcherConfigIds, launcherConfigManufacturerName, launcherConfigManufacturerNameContains, limit, offset, ordering, search, serialNumber, serialNumberContains, status, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte)



#### Filters Parameters - &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;first_launch_date&#x60;, &#x60;first_launch_date__day&#x60;, &#x60;first_launch_date__month&#x60;, &#x60;first_launch_date__year&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;flights__gt&#x60;, &#x60;flights__gte&#x60;, &#x60;flights__lt&#x60;, &#x60;flights__lte&#x60;, &#x60;id&#x60;, &#x60;id__contains&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_launch_date&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_config__manufacturer__name&#x60;, &#x60;launcher_config__manufacturer__name__contains&#x60;, &#x60;serial_number&#x60;, &#x60;serial_number__contains&#x60;, &#x60;status&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;  Example - [/launchers/mini/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;serial_number&#x60;, &#x60;status__name&#x60;  Example - [/launchers/mini/?search&#x3D;B1048](./?search&#x3D;B1048)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;id&#x60;, &#x60;successful_landings&#x60;  Example - [/launchers/mini/?ordering&#x3D;-flights](./?ordering&#x3D;-flights)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launchers/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launchers/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchersApi()
val attemptedLandings : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val firstLaunchDate : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val firstLaunchDateDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date days.
val firstLaunchDateMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date months.
val firstLaunchDateYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated first launch date years.
val flightProven : kotlin.Boolean = true // kotlin.Boolean | 
val flights : kotlin.Int = 56 // kotlin.Int | 
val flightsGt : kotlin.Int = 56 // kotlin.Int | 
val flightsGte : kotlin.Int = 56 // kotlin.Int | 
val flightsLt : kotlin.Int = 56 // kotlin.Int | 
val flightsLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val idContains : kotlin.Int = 56 // kotlin.Int | 
val isPlaceholder : kotlin.Boolean = true // kotlin.Boolean | 
val lastLaunchDate : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val launcherConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launcher config IDs.
val launcherConfigManufacturerName : kotlin.String = launcherConfigManufacturerName_example // kotlin.String | 
val launcherConfigManufacturerNameContains : kotlin.String = launcherConfigManufacturerNameContains_example // kotlin.String | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val serialNumber : kotlin.String = serialNumber_example // kotlin.String | 
val serialNumberContains : kotlin.String = serialNumberContains_example // kotlin.String | 
val status : kotlin.Int = 56 // kotlin.Int | 
val successfulLandings : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedLauncherMiniList = apiInstance.launchersMiniList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, firstLaunchDate, firstLaunchDateDay, firstLaunchDateMonth, firstLaunchDateYear, flightProven, flights, flightsGt, flightsGte, flightsLt, flightsLte, id, idContains, isPlaceholder, lastLaunchDate, launcherConfigIds, launcherConfigManufacturerName, launcherConfigManufacturerNameContains, limit, offset, ordering, search, serialNumber, serialNumberContains, status, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchersApi#launchersMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchersApi#launchersMiniList")
    e.printStackTrace()
}
```

### Parameters
| **attemptedLandings** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGte** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLte** | **kotlin.Int**|  | [optional] |
| **firstLaunchDate** | **kotlin.time.Instant**|  | [optional] |
| **firstLaunchDateDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date days. | [optional] |
| **firstLaunchDateMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date months. | [optional] |
| **firstLaunchDateYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated first launch date years. | [optional] |
| **flightProven** | **kotlin.Boolean**|  | [optional] |
| **flights** | **kotlin.Int**|  | [optional] |
| **flightsGt** | **kotlin.Int**|  | [optional] |
| **flightsGte** | **kotlin.Int**|  | [optional] |
| **flightsLt** | **kotlin.Int**|  | [optional] |
| **flightsLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **idContains** | **kotlin.Int**|  | [optional] |
| **isPlaceholder** | **kotlin.Boolean**|  | [optional] |
| **lastLaunchDate** | **kotlin.time.Instant**|  | [optional] |
| **launcherConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launcher config IDs. | [optional] |
| **launcherConfigManufacturerName** | **kotlin.String**|  | [optional] |
| **launcherConfigManufacturerNameContains** | **kotlin.String**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **serialNumber** | **kotlin.String**|  | [optional] |
| **serialNumberContains** | **kotlin.String**|  | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **successfulLandings** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGt** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGte** | **kotlin.Int**|  | [optional] |
| **successfulLandingsLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **successfulLandingsLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedLauncherMiniList**](PaginatedLauncherMiniList.md)

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

<a id="launchersRetrieve"></a>
# **launchersRetrieve**
> LauncherDetailed launchersRetrieve(id)



#### Filters Parameters - &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;first_launch_date&#x60;, &#x60;first_launch_date__day&#x60;, &#x60;first_launch_date__month&#x60;, &#x60;first_launch_date__year&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;flights__gt&#x60;, &#x60;flights__gte&#x60;, &#x60;flights__lt&#x60;, &#x60;flights__lte&#x60;, &#x60;id&#x60;, &#x60;id__contains&#x60;, &#x60;is_placeholder&#x60;, &#x60;last_launch_date&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_config__manufacturer__name&#x60;, &#x60;launcher_config__manufacturer__name__contains&#x60;, &#x60;serial_number&#x60;, &#x60;serial_number__contains&#x60;, &#x60;status&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;  Example - [/launchers/?is_placeholder&#x3D;True](./?is_placeholder&#x3D;True)  #### Search Fields searched - &#x60;serial_number&#x60;, &#x60;status__name&#x60;  Example - [/launchers/?search&#x3D;B1048](./?search&#x3D;B1048)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;flight_proven&#x60;, &#x60;flights&#x60;, &#x60;id&#x60;, &#x60;successful_landings&#x60;  Example - [/launchers/?ordering&#x3D;-flights](./?ordering&#x3D;-flights)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launchers/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launchers/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchersApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Launch Vehicle.
try {
    val result : LauncherDetailed = apiInstance.launchersRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchersApi#launchersRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchersApi#launchersRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Launch Vehicle. | |

### Return type

[**LauncherDetailed**](LauncherDetailed.md)

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

