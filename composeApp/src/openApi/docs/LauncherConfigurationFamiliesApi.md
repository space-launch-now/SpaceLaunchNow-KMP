# LauncherConfigurationFamiliesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**launcherConfigurationFamiliesList**](LauncherConfigurationFamiliesApi.md#launcherConfigurationFamiliesList) | **GET** /api/ll/2.4.0/launcher_configuration_families/ |  |
| [**launcherConfigurationFamiliesRetrieve**](LauncherConfigurationFamiliesApi.md#launcherConfigurationFamiliesRetrieve) | **GET** /api/ll/2.4.0/launcher_configuration_families/{id}/ |  |


<a id="launcherConfigurationFamiliesList"></a>
# **launcherConfigurationFamiliesList**
> PaginatedLauncherConfigFamilyNormalList launcherConfigurationFamiliesList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, limit, maidenFlight, maidenFlightGt, maidenFlightGte, maidenFlightLt, maidenFlightLte, manufacturerAbbrev, manufacturerAbbrevContains, manufacturerCountryCode, manufacturerId, manufacturerIdContains, manufacturerName, manufacturerNameContains, name, nameContains, offset, ordering, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)



#### Filters Parameters - &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_landings__gt&#x60;, &#x60;consecutive_successful_landings__gte&#x60;, &#x60;consecutive_successful_landings__lt&#x60;, &#x60;consecutive_successful_landings__lte&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;consecutive_successful_launches__gt&#x60;, &#x60;consecutive_successful_launches__gte&#x60;, &#x60;consecutive_successful_launches__lt&#x60;, &#x60;consecutive_successful_launches__lte&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_landings__gt&#x60;, &#x60;failed_landings__gte&#x60;, &#x60;failed_landings__lt&#x60;, &#x60;failed_landings__lte&#x60;, &#x60;failed_launches&#x60;, &#x60;failed_launches__gt&#x60;, &#x60;failed_launches__gte&#x60;, &#x60;failed_launches__lt&#x60;, &#x60;failed_launches__lte&#x60;, &#x60;maiden_flight&#x60;, &#x60;maiden_flight__gt&#x60;, &#x60;maiden_flight__gte&#x60;, &#x60;maiden_flight__lt&#x60;, &#x60;maiden_flight__lte&#x60;, &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__abbrev__contains&#x60;, &#x60;manufacturer__country_code&#x60;, &#x60;manufacturer__id&#x60;, &#x60;manufacturer__id__contains&#x60;, &#x60;manufacturer__name&#x60;, &#x60;manufacturer__name__contains&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;pending_launches&#x60;, &#x60;pending_launches__gt&#x60;, &#x60;pending_launches__gte&#x60;, &#x60;pending_launches__lt&#x60;, &#x60;pending_launches__lte&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;, &#x60;successful_launches&#x60;, &#x60;successful_launches__gt&#x60;, &#x60;successful_launches__gte&#x60;, &#x60;successful_launches__lt&#x60;, &#x60;successful_launches__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/launcher_configuration_families/?manufacturer__name&#x3D;SpaceX](./?manufacturer__name&#x3D;SpaceX)  #### Search Fields searched - &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;  Example - [/launcher_configuration_families/?search&#x3D;Ariane](./?search&#x3D;Ariane)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_launches&#x60;, &#x60;name&#x60;, &#x60;pending_launches&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_launches&#x60;, &#x60;total_launch_count&#x60;  Example - [/launcher_configuration_families/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launcher_configuration_families/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launcher_configuration_families/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LauncherConfigurationFamiliesApi()
val attemptedLandings : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val attemptedLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLandings : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLaunches : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val consecutiveSuccessfulLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val failedLandings : kotlin.Int = 56 // kotlin.Int | 
val failedLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val failedLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val failedLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val failedLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val failedLaunches : kotlin.Int = 56 // kotlin.Int | 
val failedLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val failedLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val failedLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val failedLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val maidenFlight : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val maidenFlightGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val maidenFlightGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val maidenFlightLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val maidenFlightLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val manufacturerAbbrev : kotlin.String = manufacturerAbbrev_example // kotlin.String | 
val manufacturerAbbrevContains : kotlin.String = manufacturerAbbrevContains_example // kotlin.String | 
val manufacturerCountryCode : kotlin.String = manufacturerCountryCode_example // kotlin.String | 
val manufacturerId : kotlin.Int = 56 // kotlin.Int | 
val manufacturerIdContains : kotlin.Int = 56 // kotlin.Int | 
val manufacturerName : kotlin.String = manufacturerName_example // kotlin.String | 
val manufacturerNameContains : kotlin.String = manufacturerNameContains_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val pendingLaunches : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val successfulLandings : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsGte : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLt : kotlin.Int = 56 // kotlin.Int | 
val successfulLandingsLte : kotlin.Int = 56 // kotlin.Int | 
val successfulLaunches : kotlin.Int = 56 // kotlin.Int | 
val successfulLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val successfulLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val successfulLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val successfulLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCount : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedLauncherConfigFamilyNormalList = apiInstance.launcherConfigurationFamiliesList(attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, limit, maidenFlight, maidenFlightGt, maidenFlightGte, maidenFlightLt, maidenFlightLte, manufacturerAbbrev, manufacturerAbbrevContains, manufacturerCountryCode, manufacturerId, manufacturerIdContains, manufacturerName, manufacturerNameContains, name, nameContains, offset, ordering, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LauncherConfigurationFamiliesApi#launcherConfigurationFamiliesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LauncherConfigurationFamiliesApi#launcherConfigurationFamiliesList")
    e.printStackTrace()
}
```

### Parameters
| **attemptedLandings** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsGte** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLt** | **kotlin.Int**|  | [optional] |
| **attemptedLandingsLte** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLandings** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLandingsGt** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLandingsGte** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLandingsLt** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLandingsLte** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLaunches** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLaunchesGt** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLaunchesGte** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLaunchesLt** | **kotlin.Int**|  | [optional] |
| **consecutiveSuccessfulLaunchesLte** | **kotlin.Int**|  | [optional] |
| **failedLandings** | **kotlin.Int**|  | [optional] |
| **failedLandingsGt** | **kotlin.Int**|  | [optional] |
| **failedLandingsGte** | **kotlin.Int**|  | [optional] |
| **failedLandingsLt** | **kotlin.Int**|  | [optional] |
| **failedLandingsLte** | **kotlin.Int**|  | [optional] |
| **failedLaunches** | **kotlin.Int**|  | [optional] |
| **failedLaunchesGt** | **kotlin.Int**|  | [optional] |
| **failedLaunchesGte** | **kotlin.Int**|  | [optional] |
| **failedLaunchesLt** | **kotlin.Int**|  | [optional] |
| **failedLaunchesLte** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **maidenFlight** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **maidenFlightGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **maidenFlightGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **maidenFlightLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **maidenFlightLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **manufacturerAbbrev** | **kotlin.String**|  | [optional] |
| **manufacturerAbbrevContains** | **kotlin.String**|  | [optional] |
| **manufacturerCountryCode** | **kotlin.String**|  | [optional] |
| **manufacturerId** | **kotlin.Int**|  | [optional] |
| **manufacturerIdContains** | **kotlin.Int**|  | [optional] |
| **manufacturerName** | **kotlin.String**|  | [optional] |
| **manufacturerNameContains** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **pendingLaunches** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGte** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLte** | **kotlin.Int**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **successfulLandings** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGt** | **kotlin.Int**|  | [optional] |
| **successfulLandingsGte** | **kotlin.Int**|  | [optional] |
| **successfulLandingsLt** | **kotlin.Int**|  | [optional] |
| **successfulLandingsLte** | **kotlin.Int**|  | [optional] |
| **successfulLaunches** | **kotlin.Int**|  | [optional] |
| **successfulLaunchesGt** | **kotlin.Int**|  | [optional] |
| **successfulLaunchesGte** | **kotlin.Int**|  | [optional] |
| **successfulLaunchesLt** | **kotlin.Int**|  | [optional] |
| **successfulLaunchesLte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCount** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGt** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedLauncherConfigFamilyNormalList**](PaginatedLauncherConfigFamilyNormalList.md)

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

<a id="launcherConfigurationFamiliesRetrieve"></a>
# **launcherConfigurationFamiliesRetrieve**
> LauncherConfigFamilyDetailed launcherConfigurationFamiliesRetrieve(id)



#### Filters Parameters - &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_landings__gt&#x60;, &#x60;consecutive_successful_landings__gte&#x60;, &#x60;consecutive_successful_landings__lt&#x60;, &#x60;consecutive_successful_landings__lte&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;consecutive_successful_launches__gt&#x60;, &#x60;consecutive_successful_launches__gte&#x60;, &#x60;consecutive_successful_launches__lt&#x60;, &#x60;consecutive_successful_launches__lte&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_landings__gt&#x60;, &#x60;failed_landings__gte&#x60;, &#x60;failed_landings__lt&#x60;, &#x60;failed_landings__lte&#x60;, &#x60;failed_launches&#x60;, &#x60;failed_launches__gt&#x60;, &#x60;failed_launches__gte&#x60;, &#x60;failed_launches__lt&#x60;, &#x60;failed_launches__lte&#x60;, &#x60;maiden_flight&#x60;, &#x60;maiden_flight__gt&#x60;, &#x60;maiden_flight__gte&#x60;, &#x60;maiden_flight__lt&#x60;, &#x60;maiden_flight__lte&#x60;, &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__abbrev__contains&#x60;, &#x60;manufacturer__country_code&#x60;, &#x60;manufacturer__id&#x60;, &#x60;manufacturer__id__contains&#x60;, &#x60;manufacturer__name&#x60;, &#x60;manufacturer__name__contains&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;pending_launches&#x60;, &#x60;pending_launches__gt&#x60;, &#x60;pending_launches__gte&#x60;, &#x60;pending_launches__lt&#x60;, &#x60;pending_launches__lte&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;, &#x60;successful_launches&#x60;, &#x60;successful_launches__gt&#x60;, &#x60;successful_launches__gte&#x60;, &#x60;successful_launches__lt&#x60;, &#x60;successful_launches__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/launcher_configuration_families/?manufacturer__name&#x3D;SpaceX](./?manufacturer__name&#x3D;SpaceX)  #### Search Fields searched - &#x60;manufacturer__abbrev&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;  Example - [/launcher_configuration_families/?search&#x3D;Ariane](./?search&#x3D;Ariane)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_launches&#x60;, &#x60;name&#x60;, &#x60;pending_launches&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_launches&#x60;, &#x60;total_launch_count&#x60;  Example - [/launcher_configuration_families/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launcher_configuration_families/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launcher_configuration_families/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LauncherConfigurationFamiliesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Launcher Configuration Family.
try {
    val result : LauncherConfigFamilyDetailed = apiInstance.launcherConfigurationFamiliesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LauncherConfigurationFamiliesApi#launcherConfigurationFamiliesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LauncherConfigurationFamiliesApi#launcherConfigurationFamiliesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Launcher Configuration Family. | |

### Return type

[**LauncherConfigFamilyDetailed**](LauncherConfigFamilyDetailed.md)

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

