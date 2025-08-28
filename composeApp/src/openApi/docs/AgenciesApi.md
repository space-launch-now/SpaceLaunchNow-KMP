# AgenciesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**agenciesDetailedList**](AgenciesApi.md#agenciesDetailedList) | **GET** /api/ll/2.4.0/agencies/detailed/ |  |
| [**agenciesList**](AgenciesApi.md#agenciesList) | **GET** /api/ll/2.4.0/agencies/ |  |
| [**agenciesMiniList**](AgenciesApi.md#agenciesMiniList) | **GET** /api/ll/2.4.0/agencies/mini/ |  |
| [**agenciesRetrieve**](AgenciesApi.md#agenciesRetrieve) | **GET** /api/ll/2.4.0/agencies/{id}/ |  |


<a id="agenciesDetailedList"></a>
# **agenciesDetailedList**
> PaginatedAgencyEndpointDetailedList agenciesDetailedList(abbrev, abbrevContains, attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, countryCode, description, descriptionContains, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, featured, foundingYear, foundingYearGt, foundingYearGte, foundingYearLt, foundingYearLte, id, limit, name, nameContains, offset, ordering, parentId, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, spacecraft, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte, typeId)



#### Filters Parameters - &#x60;abbrev&#x60;, &#x60;abbrev__contains&#x60;, &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_landings__gt&#x60;, &#x60;consecutive_successful_landings__gte&#x60;, &#x60;consecutive_successful_landings__lt&#x60;, &#x60;consecutive_successful_landings__lte&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;consecutive_successful_launches__gt&#x60;, &#x60;consecutive_successful_launches__gte&#x60;, &#x60;consecutive_successful_launches__lt&#x60;, &#x60;consecutive_successful_launches__lte&#x60;, &#x60;country_code&#x60;, &#x60;description&#x60;, &#x60;description__contains&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_landings__gt&#x60;, &#x60;failed_landings__gte&#x60;, &#x60;failed_landings__lt&#x60;, &#x60;failed_landings__lte&#x60;, &#x60;failed_launches&#x60;, &#x60;failed_launches__gt&#x60;, &#x60;failed_launches__gte&#x60;, &#x60;failed_launches__lt&#x60;, &#x60;failed_launches__lte&#x60;, &#x60;featured&#x60;, &#x60;founding_year&#x60;, &#x60;founding_year__gt&#x60;, &#x60;founding_year__gte&#x60;, &#x60;founding_year__lt&#x60;, &#x60;founding_year__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;parent__id&#x60;, &#x60;pending_launches&#x60;, &#x60;pending_launches__gt&#x60;, &#x60;pending_launches__gte&#x60;, &#x60;pending_launches__lt&#x60;, &#x60;pending_launches__lte&#x60;, &#x60;spacecraft&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;, &#x60;successful_launches&#x60;, &#x60;successful_launches__gt&#x60;, &#x60;successful_launches__gte&#x60;, &#x60;successful_launches__lt&#x60;, &#x60;successful_launches__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;, &#x60;type__id&#x60;  Example - [/agencies/detailed/?abbrev&#x3D;NASA](./?abbrev&#x3D;NASA)  #### Search Fields searched - &#x60;abbrev&#x60;, &#x60;name&#x60;  Example - [/agencies/detailed/?search&#x3D;SpaceX](./?search&#x3D;SpaceX)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_launches&#x60;, &#x60;featured&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;pending_launches&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_launches&#x60;, &#x60;total_launch_count&#x60;  Example - [/agencies/detailed/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/agencies/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/agencies/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AgenciesApi()
val abbrev : kotlin.String = abbrev_example // kotlin.String | 
val abbrevContains : kotlin.String = abbrevContains_example // kotlin.String | 
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
val countryCode : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val description : kotlin.String = description_example // kotlin.String | 
val descriptionContains : kotlin.String = descriptionContains_example // kotlin.String | 
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
val featured : kotlin.Boolean = true // kotlin.Boolean | 
val foundingYear : kotlin.Int = 56 // kotlin.Int | 
val foundingYearGt : kotlin.Int = 56 // kotlin.Int | 
val foundingYearGte : kotlin.Int = 56 // kotlin.Int | 
val foundingYearLt : kotlin.Int = 56 // kotlin.Int | 
val foundingYearLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val parentId : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunches : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraft : kotlin.Boolean = true // kotlin.Boolean | 
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
val typeId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedAgencyEndpointDetailedList = apiInstance.agenciesDetailedList(abbrev, abbrevContains, attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, countryCode, description, descriptionContains, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, featured, foundingYear, foundingYearGt, foundingYearGte, foundingYearLt, foundingYearLte, id, limit, name, nameContains, offset, ordering, parentId, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, spacecraft, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte, typeId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AgenciesApi#agenciesDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AgenciesApi#agenciesDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **abbrev** | **kotlin.String**|  | [optional] |
| **abbrevContains** | **kotlin.String**|  | [optional] |
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
| **countryCode** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **description** | **kotlin.String**|  | [optional] |
| **descriptionContains** | **kotlin.String**|  | [optional] |
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
| **featured** | **kotlin.Boolean**|  | [optional] |
| **foundingYear** | **kotlin.Int**|  | [optional] |
| **foundingYearGt** | **kotlin.Int**|  | [optional] |
| **foundingYearGte** | **kotlin.Int**|  | [optional] |
| **foundingYearLt** | **kotlin.Int**|  | [optional] |
| **foundingYearLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **parentId** | **kotlin.Int**|  | [optional] |
| **pendingLaunches** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGte** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLte** | **kotlin.Int**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraft** | **kotlin.Boolean**|  | [optional] |
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
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **typeId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedAgencyEndpointDetailedList**](PaginatedAgencyEndpointDetailedList.md)

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

<a id="agenciesList"></a>
# **agenciesList**
> PaginatedAgencyNormalList agenciesList(abbrev, abbrevContains, attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, countryCode, description, descriptionContains, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, featured, foundingYear, foundingYearGt, foundingYearGte, foundingYearLt, foundingYearLte, id, limit, name, nameContains, offset, ordering, parentId, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, spacecraft, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte, typeId)



#### Filters Parameters - &#x60;abbrev&#x60;, &#x60;abbrev__contains&#x60;, &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_landings__gt&#x60;, &#x60;consecutive_successful_landings__gte&#x60;, &#x60;consecutive_successful_landings__lt&#x60;, &#x60;consecutive_successful_landings__lte&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;consecutive_successful_launches__gt&#x60;, &#x60;consecutive_successful_launches__gte&#x60;, &#x60;consecutive_successful_launches__lt&#x60;, &#x60;consecutive_successful_launches__lte&#x60;, &#x60;country_code&#x60;, &#x60;description&#x60;, &#x60;description__contains&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_landings__gt&#x60;, &#x60;failed_landings__gte&#x60;, &#x60;failed_landings__lt&#x60;, &#x60;failed_landings__lte&#x60;, &#x60;failed_launches&#x60;, &#x60;failed_launches__gt&#x60;, &#x60;failed_launches__gte&#x60;, &#x60;failed_launches__lt&#x60;, &#x60;failed_launches__lte&#x60;, &#x60;featured&#x60;, &#x60;founding_year&#x60;, &#x60;founding_year__gt&#x60;, &#x60;founding_year__gte&#x60;, &#x60;founding_year__lt&#x60;, &#x60;founding_year__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;parent__id&#x60;, &#x60;pending_launches&#x60;, &#x60;pending_launches__gt&#x60;, &#x60;pending_launches__gte&#x60;, &#x60;pending_launches__lt&#x60;, &#x60;pending_launches__lte&#x60;, &#x60;spacecraft&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;, &#x60;successful_launches&#x60;, &#x60;successful_launches__gt&#x60;, &#x60;successful_launches__gte&#x60;, &#x60;successful_launches__lt&#x60;, &#x60;successful_launches__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;, &#x60;type__id&#x60;  Example - [/agencies/?abbrev&#x3D;NASA](./?abbrev&#x3D;NASA)  #### Search Fields searched - &#x60;abbrev&#x60;, &#x60;name&#x60;  Example - [/agencies/?search&#x3D;SpaceX](./?search&#x3D;SpaceX)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_launches&#x60;, &#x60;featured&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;pending_launches&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_launches&#x60;, &#x60;total_launch_count&#x60;  Example - [/agencies/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/agencies/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/agencies/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AgenciesApi()
val abbrev : kotlin.String = abbrev_example // kotlin.String | 
val abbrevContains : kotlin.String = abbrevContains_example // kotlin.String | 
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
val countryCode : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val description : kotlin.String = description_example // kotlin.String | 
val descriptionContains : kotlin.String = descriptionContains_example // kotlin.String | 
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
val featured : kotlin.Boolean = true // kotlin.Boolean | 
val foundingYear : kotlin.Int = 56 // kotlin.Int | 
val foundingYearGt : kotlin.Int = 56 // kotlin.Int | 
val foundingYearGte : kotlin.Int = 56 // kotlin.Int | 
val foundingYearLt : kotlin.Int = 56 // kotlin.Int | 
val foundingYearLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val parentId : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunches : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraft : kotlin.Boolean = true // kotlin.Boolean | 
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
val typeId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedAgencyNormalList = apiInstance.agenciesList(abbrev, abbrevContains, attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, countryCode, description, descriptionContains, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, featured, foundingYear, foundingYearGt, foundingYearGte, foundingYearLt, foundingYearLte, id, limit, name, nameContains, offset, ordering, parentId, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, spacecraft, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte, typeId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AgenciesApi#agenciesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AgenciesApi#agenciesList")
    e.printStackTrace()
}
```

### Parameters
| **abbrev** | **kotlin.String**|  | [optional] |
| **abbrevContains** | **kotlin.String**|  | [optional] |
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
| **countryCode** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **description** | **kotlin.String**|  | [optional] |
| **descriptionContains** | **kotlin.String**|  | [optional] |
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
| **featured** | **kotlin.Boolean**|  | [optional] |
| **foundingYear** | **kotlin.Int**|  | [optional] |
| **foundingYearGt** | **kotlin.Int**|  | [optional] |
| **foundingYearGte** | **kotlin.Int**|  | [optional] |
| **foundingYearLt** | **kotlin.Int**|  | [optional] |
| **foundingYearLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **parentId** | **kotlin.Int**|  | [optional] |
| **pendingLaunches** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGte** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLte** | **kotlin.Int**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraft** | **kotlin.Boolean**|  | [optional] |
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
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **typeId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedAgencyNormalList**](PaginatedAgencyNormalList.md)

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

<a id="agenciesMiniList"></a>
# **agenciesMiniList**
> PaginatedAgencyMiniList agenciesMiniList(abbrev, abbrevContains, attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, countryCode, description, descriptionContains, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, featured, foundingYear, foundingYearGt, foundingYearGte, foundingYearLt, foundingYearLte, id, limit, name, nameContains, offset, ordering, parentId, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, spacecraft, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte, typeId)



#### Filters Parameters - &#x60;abbrev&#x60;, &#x60;abbrev__contains&#x60;, &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_landings__gt&#x60;, &#x60;consecutive_successful_landings__gte&#x60;, &#x60;consecutive_successful_landings__lt&#x60;, &#x60;consecutive_successful_landings__lte&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;consecutive_successful_launches__gt&#x60;, &#x60;consecutive_successful_launches__gte&#x60;, &#x60;consecutive_successful_launches__lt&#x60;, &#x60;consecutive_successful_launches__lte&#x60;, &#x60;country_code&#x60;, &#x60;description&#x60;, &#x60;description__contains&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_landings__gt&#x60;, &#x60;failed_landings__gte&#x60;, &#x60;failed_landings__lt&#x60;, &#x60;failed_landings__lte&#x60;, &#x60;failed_launches&#x60;, &#x60;failed_launches__gt&#x60;, &#x60;failed_launches__gte&#x60;, &#x60;failed_launches__lt&#x60;, &#x60;failed_launches__lte&#x60;, &#x60;featured&#x60;, &#x60;founding_year&#x60;, &#x60;founding_year__gt&#x60;, &#x60;founding_year__gte&#x60;, &#x60;founding_year__lt&#x60;, &#x60;founding_year__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;parent__id&#x60;, &#x60;pending_launches&#x60;, &#x60;pending_launches__gt&#x60;, &#x60;pending_launches__gte&#x60;, &#x60;pending_launches__lt&#x60;, &#x60;pending_launches__lte&#x60;, &#x60;spacecraft&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;, &#x60;successful_launches&#x60;, &#x60;successful_launches__gt&#x60;, &#x60;successful_launches__gte&#x60;, &#x60;successful_launches__lt&#x60;, &#x60;successful_launches__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;, &#x60;type__id&#x60;  Example - [/agencies/mini/?abbrev&#x3D;NASA](./?abbrev&#x3D;NASA)  #### Search Fields searched - &#x60;abbrev&#x60;, &#x60;name&#x60;  Example - [/agencies/mini/?search&#x3D;SpaceX](./?search&#x3D;SpaceX)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_launches&#x60;, &#x60;featured&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;pending_launches&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_launches&#x60;, &#x60;total_launch_count&#x60;  Example - [/agencies/mini/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/agencies/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/agencies/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AgenciesApi()
val abbrev : kotlin.String = abbrev_example // kotlin.String | 
val abbrevContains : kotlin.String = abbrevContains_example // kotlin.String | 
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
val countryCode : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val description : kotlin.String = description_example // kotlin.String | 
val descriptionContains : kotlin.String = descriptionContains_example // kotlin.String | 
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
val featured : kotlin.Boolean = true // kotlin.Boolean | 
val foundingYear : kotlin.Int = 56 // kotlin.Int | 
val foundingYearGt : kotlin.Int = 56 // kotlin.Int | 
val foundingYearGte : kotlin.Int = 56 // kotlin.Int | 
val foundingYearLt : kotlin.Int = 56 // kotlin.Int | 
val foundingYearLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val parentId : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunches : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesGte : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLt : kotlin.Int = 56 // kotlin.Int | 
val pendingLaunchesLte : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraft : kotlin.Boolean = true // kotlin.Boolean | 
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
val typeId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedAgencyMiniList = apiInstance.agenciesMiniList(abbrev, abbrevContains, attemptedLandings, attemptedLandingsGt, attemptedLandingsGte, attemptedLandingsLt, attemptedLandingsLte, consecutiveSuccessfulLandings, consecutiveSuccessfulLandingsGt, consecutiveSuccessfulLandingsGte, consecutiveSuccessfulLandingsLt, consecutiveSuccessfulLandingsLte, consecutiveSuccessfulLaunches, consecutiveSuccessfulLaunchesGt, consecutiveSuccessfulLaunchesGte, consecutiveSuccessfulLaunchesLt, consecutiveSuccessfulLaunchesLte, countryCode, description, descriptionContains, failedLandings, failedLandingsGt, failedLandingsGte, failedLandingsLt, failedLandingsLte, failedLaunches, failedLaunchesGt, failedLaunchesGte, failedLaunchesLt, failedLaunchesLte, featured, foundingYear, foundingYearGt, foundingYearGte, foundingYearLt, foundingYearLte, id, limit, name, nameContains, offset, ordering, parentId, pendingLaunches, pendingLaunchesGt, pendingLaunchesGte, pendingLaunchesLt, pendingLaunchesLte, search, spacecraft, successfulLandings, successfulLandingsGt, successfulLandingsGte, successfulLandingsLt, successfulLandingsLte, successfulLaunches, successfulLaunchesGt, successfulLaunchesGte, successfulLaunchesLt, successfulLaunchesLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte, typeId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AgenciesApi#agenciesMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AgenciesApi#agenciesMiniList")
    e.printStackTrace()
}
```

### Parameters
| **abbrev** | **kotlin.String**|  | [optional] |
| **abbrevContains** | **kotlin.String**|  | [optional] |
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
| **countryCode** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **description** | **kotlin.String**|  | [optional] |
| **descriptionContains** | **kotlin.String**|  | [optional] |
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
| **featured** | **kotlin.Boolean**|  | [optional] |
| **foundingYear** | **kotlin.Int**|  | [optional] |
| **foundingYearGt** | **kotlin.Int**|  | [optional] |
| **foundingYearGte** | **kotlin.Int**|  | [optional] |
| **foundingYearLt** | **kotlin.Int**|  | [optional] |
| **foundingYearLte** | **kotlin.Int**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **parentId** | **kotlin.Int**|  | [optional] |
| **pendingLaunches** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesGte** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLt** | **kotlin.Int**|  | [optional] |
| **pendingLaunchesLte** | **kotlin.Int**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraft** | **kotlin.Boolean**|  | [optional] |
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
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **typeId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedAgencyMiniList**](PaginatedAgencyMiniList.md)

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

<a id="agenciesRetrieve"></a>
# **agenciesRetrieve**
> AgencyEndpointDetailed agenciesRetrieve(id)



#### Filters Parameters - &#x60;abbrev&#x60;, &#x60;abbrev__contains&#x60;, &#x60;attempted_landings&#x60;, &#x60;attempted_landings__gt&#x60;, &#x60;attempted_landings__gte&#x60;, &#x60;attempted_landings__lt&#x60;, &#x60;attempted_landings__lte&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_landings__gt&#x60;, &#x60;consecutive_successful_landings__gte&#x60;, &#x60;consecutive_successful_landings__lt&#x60;, &#x60;consecutive_successful_landings__lte&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;consecutive_successful_launches__gt&#x60;, &#x60;consecutive_successful_launches__gte&#x60;, &#x60;consecutive_successful_launches__lt&#x60;, &#x60;consecutive_successful_launches__lte&#x60;, &#x60;country_code&#x60;, &#x60;description&#x60;, &#x60;description__contains&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_landings__gt&#x60;, &#x60;failed_landings__gte&#x60;, &#x60;failed_landings__lt&#x60;, &#x60;failed_landings__lte&#x60;, &#x60;failed_launches&#x60;, &#x60;failed_launches__gt&#x60;, &#x60;failed_launches__gte&#x60;, &#x60;failed_launches__lt&#x60;, &#x60;failed_launches__lte&#x60;, &#x60;featured&#x60;, &#x60;founding_year&#x60;, &#x60;founding_year__gt&#x60;, &#x60;founding_year__gte&#x60;, &#x60;founding_year__lt&#x60;, &#x60;founding_year__lte&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;parent__id&#x60;, &#x60;pending_launches&#x60;, &#x60;pending_launches__gt&#x60;, &#x60;pending_launches__gte&#x60;, &#x60;pending_launches__lt&#x60;, &#x60;pending_launches__lte&#x60;, &#x60;spacecraft&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_landings__gt&#x60;, &#x60;successful_landings__gte&#x60;, &#x60;successful_landings__lt&#x60;, &#x60;successful_landings__lte&#x60;, &#x60;successful_launches&#x60;, &#x60;successful_launches__gt&#x60;, &#x60;successful_launches__gte&#x60;, &#x60;successful_launches__lt&#x60;, &#x60;successful_launches__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;, &#x60;type__id&#x60;  Example - [/agencies/?abbrev&#x3D;NASA](./?abbrev&#x3D;NASA)  #### Search Fields searched - &#x60;abbrev&#x60;, &#x60;name&#x60;  Example - [/agencies/?search&#x3D;SpaceX](./?search&#x3D;SpaceX)  #### Ordering Fields - &#x60;attempted_landings&#x60;, &#x60;consecutive_successful_landings&#x60;, &#x60;consecutive_successful_launches&#x60;, &#x60;failed_landings&#x60;, &#x60;failed_launches&#x60;, &#x60;featured&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;pending_launches&#x60;, &#x60;successful_landings&#x60;, &#x60;successful_launches&#x60;, &#x60;total_launch_count&#x60;  Example - [/agencies/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/agencies/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/agencies/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AgenciesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Agency.
try {
    val result : AgencyEndpointDetailed = apiInstance.agenciesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AgenciesApi#agenciesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AgenciesApi#agenciesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Agency. | |

### Return type

[**AgencyEndpointDetailed**](AgencyEndpointDetailed.md)

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

