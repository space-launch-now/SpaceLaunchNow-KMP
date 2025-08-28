# AstronautsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**astronautsDetailedList**](AstronautsApi.md#astronautsDetailedList) | **GET** /api/ll/2.4.0/astronauts/detailed/ |  |
| [**astronautsList**](AstronautsApi.md#astronautsList) | **GET** /api/ll/2.4.0/astronauts/ |  |
| [**astronautsMiniList**](AstronautsApi.md#astronautsMiniList) | **GET** /api/ll/2.4.0/astronauts/mini/ |  |
| [**astronautsRetrieve**](AstronautsApi.md#astronautsRetrieve) | **GET** /api/ll/2.4.0/astronauts/{id}/ |  |


<a id="astronautsDetailedList"></a>
# **astronautsDetailedList**
> PaginatedAstronautEndpointDetailedList astronautsDetailedList(age, ageGt, ageGte, ageLt, ageLte, agencyIds, dateOfBirth, dateOfBirthDay, dateOfBirthGt, dateOfBirthGte, dateOfBirthLt, dateOfBirthLte, dateOfBirthMonth, dateOfBirthYear, dateOfDeath, dateOfDeathDay, dateOfDeathGt, dateOfDeathGte, dateOfDeathLt, dateOfDeathLte, dateOfDeathMonth, dateOfDeathYear, firstFlight, firstFlightGt, firstFlightGte, firstFlightLt, firstFlightLte, flightsCount, flightsCountGt, flightsCountGte, flightsCountLt, flightsCountLte, hasFlown, inSpace, isHuman, landingsCount, landingsCountGt, landingsCountGte, landingsCountLt, landingsCountLte, lastFlight, lastFlightGt, lastFlightGte, lastFlightLt, lastFlightLte, limit, nationality, offset, ordering, search, statusIds, typeId)



#### Filters Parameters - &#x60;age&#x60;, &#x60;age__gt&#x60;, &#x60;age__gte&#x60;, &#x60;age__lt&#x60;, &#x60;age__lte&#x60;, &#x60;agency_ids&#x60;, &#x60;date_of_birth&#x60;, &#x60;date_of_birth__day&#x60;, &#x60;date_of_birth__gt&#x60;, &#x60;date_of_birth__gte&#x60;, &#x60;date_of_birth__lt&#x60;, &#x60;date_of_birth__lte&#x60;, &#x60;date_of_birth__month&#x60;, &#x60;date_of_birth__year&#x60;, &#x60;date_of_death&#x60;, &#x60;date_of_death__day&#x60;, &#x60;date_of_death__gt&#x60;, &#x60;date_of_death__gte&#x60;, &#x60;date_of_death__lt&#x60;, &#x60;date_of_death__lte&#x60;, &#x60;date_of_death__month&#x60;, &#x60;date_of_death__year&#x60;, &#x60;first_flight&#x60;, &#x60;first_flight__gt&#x60;, &#x60;first_flight__gte&#x60;, &#x60;first_flight__lt&#x60;, &#x60;first_flight__lte&#x60;, &#x60;flights_count&#x60;, &#x60;flights_count__gt&#x60;, &#x60;flights_count__gte&#x60;, &#x60;flights_count__lt&#x60;, &#x60;flights_count__lte&#x60;, &#x60;has_flown&#x60;, &#x60;in_space&#x60;, &#x60;is_human&#x60;, &#x60;landings_count&#x60;, &#x60;landings_count__gt&#x60;, &#x60;landings_count__gte&#x60;, &#x60;landings_count__lt&#x60;, &#x60;landings_count__lte&#x60;, &#x60;last_flight&#x60;, &#x60;last_flight__gt&#x60;, &#x60;last_flight__gte&#x60;, &#x60;last_flight__lt&#x60;, &#x60;last_flight__lte&#x60;, &#x60;nationality&#x60;, &#x60;status_ids&#x60;, &#x60;type__id&#x60;  Example - [/astronauts/detailed/?has_flown&#x3D;true](./?has_flown&#x3D;true)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;, &#x60;nationality__nationality_name&#x60;  Example - [/astronauts/detailed/?search&#x3D;Pesquet](./?search&#x3D;Pesquet)  #### Ordering Fields - &#x60;age&#x60;, &#x60;date_of_birth&#x60;, &#x60;eva_time&#x60;, &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;landings_count&#x60;, &#x60;last_flight&#x60;, &#x60;name&#x60;, &#x60;spacewalks_count&#x60;, &#x60;status&#x60;, &#x60;time_in_space&#x60;  Example - [/astronauts/detailed/?ordering&#x3D;-time_in_space](./?ordering&#x3D;-time_in_space)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/astronauts/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/astronauts/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AstronautsApi()
val age : kotlin.Int = 56 // kotlin.Int | 
val ageGt : kotlin.Int = 56 // kotlin.Int | 
val ageGte : kotlin.Int = 56 // kotlin.Int | 
val ageLt : kotlin.Int = 56 // kotlin.Int | 
val ageLte : kotlin.Int = 56 // kotlin.Int | 
val agencyIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated agency IDs.
val dateOfBirth : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfBirthGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfBirthYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeath : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeathGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeathYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val firstFlight : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val flightsCount : kotlin.Int = 56 // kotlin.Int | 
val flightsCountGt : kotlin.Int = 56 // kotlin.Int | 
val flightsCountGte : kotlin.Int = 56 // kotlin.Int | 
val flightsCountLt : kotlin.Int = 56 // kotlin.Int | 
val flightsCountLte : kotlin.Int = 56 // kotlin.Int | 
val hasFlown : kotlin.Boolean = true // kotlin.Boolean | 
val inSpace : kotlin.Boolean = true // kotlin.Boolean | 
val isHuman : kotlin.Boolean = true // kotlin.Boolean | 
val landingsCount : kotlin.Int = 56 // kotlin.Int | 
val landingsCountGt : kotlin.Int = 56 // kotlin.Int | 
val landingsCountGte : kotlin.Int = 56 // kotlin.Int | 
val landingsCountLt : kotlin.Int = 56 // kotlin.Int | 
val landingsCountLte : kotlin.Int = 56 // kotlin.Int | 
val lastFlight : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val nationality : kotlin.String = nationality_example // kotlin.String | Nationality
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val statusIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated astronaut status IDs.
val typeId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedAstronautEndpointDetailedList = apiInstance.astronautsDetailedList(age, ageGt, ageGte, ageLt, ageLte, agencyIds, dateOfBirth, dateOfBirthDay, dateOfBirthGt, dateOfBirthGte, dateOfBirthLt, dateOfBirthLte, dateOfBirthMonth, dateOfBirthYear, dateOfDeath, dateOfDeathDay, dateOfDeathGt, dateOfDeathGte, dateOfDeathLt, dateOfDeathLte, dateOfDeathMonth, dateOfDeathYear, firstFlight, firstFlightGt, firstFlightGte, firstFlightLt, firstFlightLte, flightsCount, flightsCountGt, flightsCountGte, flightsCountLt, flightsCountLte, hasFlown, inSpace, isHuman, landingsCount, landingsCountGt, landingsCountGte, landingsCountLt, landingsCountLte, lastFlight, lastFlightGt, lastFlightGte, lastFlightLt, lastFlightLte, limit, nationality, offset, ordering, search, statusIds, typeId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AstronautsApi#astronautsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AstronautsApi#astronautsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **age** | **kotlin.Int**|  | [optional] |
| **ageGt** | **kotlin.Int**|  | [optional] |
| **ageGte** | **kotlin.Int**|  | [optional] |
| **ageLt** | **kotlin.Int**|  | [optional] |
| **ageLte** | **kotlin.Int**|  | [optional] |
| **agencyIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated agency IDs. | [optional] |
| **dateOfBirth** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfBirthGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfBirthYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeath** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeathGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeathYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **firstFlight** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **flightsCount** | **kotlin.Int**|  | [optional] |
| **flightsCountGt** | **kotlin.Int**|  | [optional] |
| **flightsCountGte** | **kotlin.Int**|  | [optional] |
| **flightsCountLt** | **kotlin.Int**|  | [optional] |
| **flightsCountLte** | **kotlin.Int**|  | [optional] |
| **hasFlown** | **kotlin.Boolean**|  | [optional] |
| **inSpace** | **kotlin.Boolean**|  | [optional] |
| **isHuman** | **kotlin.Boolean**|  | [optional] |
| **landingsCount** | **kotlin.Int**|  | [optional] |
| **landingsCountGt** | **kotlin.Int**|  | [optional] |
| **landingsCountGte** | **kotlin.Int**|  | [optional] |
| **landingsCountLt** | **kotlin.Int**|  | [optional] |
| **landingsCountLte** | **kotlin.Int**|  | [optional] |
| **lastFlight** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **nationality** | **kotlin.String**| Nationality | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated astronaut status IDs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **typeId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedAstronautEndpointDetailedList**](PaginatedAstronautEndpointDetailedList.md)

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

<a id="astronautsList"></a>
# **astronautsList**
> PaginatedAstronautEndpointNormalList astronautsList(age, ageGt, ageGte, ageLt, ageLte, agencyIds, dateOfBirth, dateOfBirthDay, dateOfBirthGt, dateOfBirthGte, dateOfBirthLt, dateOfBirthLte, dateOfBirthMonth, dateOfBirthYear, dateOfDeath, dateOfDeathDay, dateOfDeathGt, dateOfDeathGte, dateOfDeathLt, dateOfDeathLte, dateOfDeathMonth, dateOfDeathYear, firstFlight, firstFlightGt, firstFlightGte, firstFlightLt, firstFlightLte, flightsCount, flightsCountGt, flightsCountGte, flightsCountLt, flightsCountLte, hasFlown, inSpace, isHuman, landingsCount, landingsCountGt, landingsCountGte, landingsCountLt, landingsCountLte, lastFlight, lastFlightGt, lastFlightGte, lastFlightLt, lastFlightLte, limit, nationality, offset, ordering, search, statusIds, typeId)



#### Filters Parameters - &#x60;age&#x60;, &#x60;age__gt&#x60;, &#x60;age__gte&#x60;, &#x60;age__lt&#x60;, &#x60;age__lte&#x60;, &#x60;agency_ids&#x60;, &#x60;date_of_birth&#x60;, &#x60;date_of_birth__day&#x60;, &#x60;date_of_birth__gt&#x60;, &#x60;date_of_birth__gte&#x60;, &#x60;date_of_birth__lt&#x60;, &#x60;date_of_birth__lte&#x60;, &#x60;date_of_birth__month&#x60;, &#x60;date_of_birth__year&#x60;, &#x60;date_of_death&#x60;, &#x60;date_of_death__day&#x60;, &#x60;date_of_death__gt&#x60;, &#x60;date_of_death__gte&#x60;, &#x60;date_of_death__lt&#x60;, &#x60;date_of_death__lte&#x60;, &#x60;date_of_death__month&#x60;, &#x60;date_of_death__year&#x60;, &#x60;first_flight&#x60;, &#x60;first_flight__gt&#x60;, &#x60;first_flight__gte&#x60;, &#x60;first_flight__lt&#x60;, &#x60;first_flight__lte&#x60;, &#x60;flights_count&#x60;, &#x60;flights_count__gt&#x60;, &#x60;flights_count__gte&#x60;, &#x60;flights_count__lt&#x60;, &#x60;flights_count__lte&#x60;, &#x60;has_flown&#x60;, &#x60;in_space&#x60;, &#x60;is_human&#x60;, &#x60;landings_count&#x60;, &#x60;landings_count__gt&#x60;, &#x60;landings_count__gte&#x60;, &#x60;landings_count__lt&#x60;, &#x60;landings_count__lte&#x60;, &#x60;last_flight&#x60;, &#x60;last_flight__gt&#x60;, &#x60;last_flight__gte&#x60;, &#x60;last_flight__lt&#x60;, &#x60;last_flight__lte&#x60;, &#x60;nationality&#x60;, &#x60;status_ids&#x60;, &#x60;type__id&#x60;  Example - [/astronauts/?has_flown&#x3D;true](./?has_flown&#x3D;true)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;, &#x60;nationality__nationality_name&#x60;  Example - [/astronauts/?search&#x3D;Pesquet](./?search&#x3D;Pesquet)  #### Ordering Fields - &#x60;age&#x60;, &#x60;date_of_birth&#x60;, &#x60;eva_time&#x60;, &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;landings_count&#x60;, &#x60;last_flight&#x60;, &#x60;name&#x60;, &#x60;spacewalks_count&#x60;, &#x60;status&#x60;, &#x60;time_in_space&#x60;  Example - [/astronauts/?ordering&#x3D;-time_in_space](./?ordering&#x3D;-time_in_space)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/astronauts/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/astronauts/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AstronautsApi()
val age : kotlin.Int = 56 // kotlin.Int | 
val ageGt : kotlin.Int = 56 // kotlin.Int | 
val ageGte : kotlin.Int = 56 // kotlin.Int | 
val ageLt : kotlin.Int = 56 // kotlin.Int | 
val ageLte : kotlin.Int = 56 // kotlin.Int | 
val agencyIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated agency IDs.
val dateOfBirth : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfBirthGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfBirthYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeath : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeathGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeathYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val firstFlight : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val flightsCount : kotlin.Int = 56 // kotlin.Int | 
val flightsCountGt : kotlin.Int = 56 // kotlin.Int | 
val flightsCountGte : kotlin.Int = 56 // kotlin.Int | 
val flightsCountLt : kotlin.Int = 56 // kotlin.Int | 
val flightsCountLte : kotlin.Int = 56 // kotlin.Int | 
val hasFlown : kotlin.Boolean = true // kotlin.Boolean | 
val inSpace : kotlin.Boolean = true // kotlin.Boolean | 
val isHuman : kotlin.Boolean = true // kotlin.Boolean | 
val landingsCount : kotlin.Int = 56 // kotlin.Int | 
val landingsCountGt : kotlin.Int = 56 // kotlin.Int | 
val landingsCountGte : kotlin.Int = 56 // kotlin.Int | 
val landingsCountLt : kotlin.Int = 56 // kotlin.Int | 
val landingsCountLte : kotlin.Int = 56 // kotlin.Int | 
val lastFlight : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val nationality : kotlin.String = nationality_example // kotlin.String | Nationality
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val statusIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated astronaut status IDs.
val typeId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedAstronautEndpointNormalList = apiInstance.astronautsList(age, ageGt, ageGte, ageLt, ageLte, agencyIds, dateOfBirth, dateOfBirthDay, dateOfBirthGt, dateOfBirthGte, dateOfBirthLt, dateOfBirthLte, dateOfBirthMonth, dateOfBirthYear, dateOfDeath, dateOfDeathDay, dateOfDeathGt, dateOfDeathGte, dateOfDeathLt, dateOfDeathLte, dateOfDeathMonth, dateOfDeathYear, firstFlight, firstFlightGt, firstFlightGte, firstFlightLt, firstFlightLte, flightsCount, flightsCountGt, flightsCountGte, flightsCountLt, flightsCountLte, hasFlown, inSpace, isHuman, landingsCount, landingsCountGt, landingsCountGte, landingsCountLt, landingsCountLte, lastFlight, lastFlightGt, lastFlightGte, lastFlightLt, lastFlightLte, limit, nationality, offset, ordering, search, statusIds, typeId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AstronautsApi#astronautsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AstronautsApi#astronautsList")
    e.printStackTrace()
}
```

### Parameters
| **age** | **kotlin.Int**|  | [optional] |
| **ageGt** | **kotlin.Int**|  | [optional] |
| **ageGte** | **kotlin.Int**|  | [optional] |
| **ageLt** | **kotlin.Int**|  | [optional] |
| **ageLte** | **kotlin.Int**|  | [optional] |
| **agencyIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated agency IDs. | [optional] |
| **dateOfBirth** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfBirthGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfBirthYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeath** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeathGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeathYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **firstFlight** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **flightsCount** | **kotlin.Int**|  | [optional] |
| **flightsCountGt** | **kotlin.Int**|  | [optional] |
| **flightsCountGte** | **kotlin.Int**|  | [optional] |
| **flightsCountLt** | **kotlin.Int**|  | [optional] |
| **flightsCountLte** | **kotlin.Int**|  | [optional] |
| **hasFlown** | **kotlin.Boolean**|  | [optional] |
| **inSpace** | **kotlin.Boolean**|  | [optional] |
| **isHuman** | **kotlin.Boolean**|  | [optional] |
| **landingsCount** | **kotlin.Int**|  | [optional] |
| **landingsCountGt** | **kotlin.Int**|  | [optional] |
| **landingsCountGte** | **kotlin.Int**|  | [optional] |
| **landingsCountLt** | **kotlin.Int**|  | [optional] |
| **landingsCountLte** | **kotlin.Int**|  | [optional] |
| **lastFlight** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **nationality** | **kotlin.String**| Nationality | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated astronaut status IDs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **typeId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedAstronautEndpointNormalList**](PaginatedAstronautEndpointNormalList.md)

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

<a id="astronautsMiniList"></a>
# **astronautsMiniList**
> PaginatedAstronautEndpointListList astronautsMiniList(age, ageGt, ageGte, ageLt, ageLte, agencyIds, dateOfBirth, dateOfBirthDay, dateOfBirthGt, dateOfBirthGte, dateOfBirthLt, dateOfBirthLte, dateOfBirthMonth, dateOfBirthYear, dateOfDeath, dateOfDeathDay, dateOfDeathGt, dateOfDeathGte, dateOfDeathLt, dateOfDeathLte, dateOfDeathMonth, dateOfDeathYear, firstFlight, firstFlightGt, firstFlightGte, firstFlightLt, firstFlightLte, flightsCount, flightsCountGt, flightsCountGte, flightsCountLt, flightsCountLte, hasFlown, inSpace, isHuman, landingsCount, landingsCountGt, landingsCountGte, landingsCountLt, landingsCountLte, lastFlight, lastFlightGt, lastFlightGte, lastFlightLt, lastFlightLte, limit, nationality, offset, ordering, search, statusIds, typeId)



#### Filters Parameters - &#x60;age&#x60;, &#x60;age__gt&#x60;, &#x60;age__gte&#x60;, &#x60;age__lt&#x60;, &#x60;age__lte&#x60;, &#x60;agency_ids&#x60;, &#x60;date_of_birth&#x60;, &#x60;date_of_birth__day&#x60;, &#x60;date_of_birth__gt&#x60;, &#x60;date_of_birth__gte&#x60;, &#x60;date_of_birth__lt&#x60;, &#x60;date_of_birth__lte&#x60;, &#x60;date_of_birth__month&#x60;, &#x60;date_of_birth__year&#x60;, &#x60;date_of_death&#x60;, &#x60;date_of_death__day&#x60;, &#x60;date_of_death__gt&#x60;, &#x60;date_of_death__gte&#x60;, &#x60;date_of_death__lt&#x60;, &#x60;date_of_death__lte&#x60;, &#x60;date_of_death__month&#x60;, &#x60;date_of_death__year&#x60;, &#x60;first_flight&#x60;, &#x60;first_flight__gt&#x60;, &#x60;first_flight__gte&#x60;, &#x60;first_flight__lt&#x60;, &#x60;first_flight__lte&#x60;, &#x60;flights_count&#x60;, &#x60;flights_count__gt&#x60;, &#x60;flights_count__gte&#x60;, &#x60;flights_count__lt&#x60;, &#x60;flights_count__lte&#x60;, &#x60;has_flown&#x60;, &#x60;in_space&#x60;, &#x60;is_human&#x60;, &#x60;landings_count&#x60;, &#x60;landings_count__gt&#x60;, &#x60;landings_count__gte&#x60;, &#x60;landings_count__lt&#x60;, &#x60;landings_count__lte&#x60;, &#x60;last_flight&#x60;, &#x60;last_flight__gt&#x60;, &#x60;last_flight__gte&#x60;, &#x60;last_flight__lt&#x60;, &#x60;last_flight__lte&#x60;, &#x60;nationality&#x60;, &#x60;status_ids&#x60;, &#x60;type__id&#x60;  Example - [/astronauts/mini/?has_flown&#x3D;true](./?has_flown&#x3D;true)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;, &#x60;nationality__nationality_name&#x60;  Example - [/astronauts/mini/?search&#x3D;Pesquet](./?search&#x3D;Pesquet)  #### Ordering Fields - &#x60;age&#x60;, &#x60;date_of_birth&#x60;, &#x60;eva_time&#x60;, &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;landings_count&#x60;, &#x60;last_flight&#x60;, &#x60;name&#x60;, &#x60;spacewalks_count&#x60;, &#x60;status&#x60;, &#x60;time_in_space&#x60;  Example - [/astronauts/mini/?ordering&#x3D;-time_in_space](./?ordering&#x3D;-time_in_space)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/astronauts/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/astronauts/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AstronautsApi()
val age : kotlin.Int = 56 // kotlin.Int | 
val ageGt : kotlin.Int = 56 // kotlin.Int | 
val ageGte : kotlin.Int = 56 // kotlin.Int | 
val ageLt : kotlin.Int = 56 // kotlin.Int | 
val ageLte : kotlin.Int = 56 // kotlin.Int | 
val agencyIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated agency IDs.
val dateOfBirth : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfBirthGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfBirthMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfBirthYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeath : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeathGt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathGte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathLt : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathLte : kotlinx.datetime.LocalDate = 2013-10-20 // kotlinx.datetime.LocalDate | 
val dateOfDeathMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val dateOfDeathYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val firstFlight : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val firstFlightLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val flightsCount : kotlin.Int = 56 // kotlin.Int | 
val flightsCountGt : kotlin.Int = 56 // kotlin.Int | 
val flightsCountGte : kotlin.Int = 56 // kotlin.Int | 
val flightsCountLt : kotlin.Int = 56 // kotlin.Int | 
val flightsCountLte : kotlin.Int = 56 // kotlin.Int | 
val hasFlown : kotlin.Boolean = true // kotlin.Boolean | 
val inSpace : kotlin.Boolean = true // kotlin.Boolean | 
val isHuman : kotlin.Boolean = true // kotlin.Boolean | 
val landingsCount : kotlin.Int = 56 // kotlin.Int | 
val landingsCountGt : kotlin.Int = 56 // kotlin.Int | 
val landingsCountGte : kotlin.Int = 56 // kotlin.Int | 
val landingsCountLt : kotlin.Int = 56 // kotlin.Int | 
val landingsCountLte : kotlin.Int = 56 // kotlin.Int | 
val lastFlight : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val lastFlightLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val nationality : kotlin.String = nationality_example // kotlin.String | Nationality
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val statusIds : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Comma-separated astronaut status IDs.
val typeId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedAstronautEndpointListList = apiInstance.astronautsMiniList(age, ageGt, ageGte, ageLt, ageLte, agencyIds, dateOfBirth, dateOfBirthDay, dateOfBirthGt, dateOfBirthGte, dateOfBirthLt, dateOfBirthLte, dateOfBirthMonth, dateOfBirthYear, dateOfDeath, dateOfDeathDay, dateOfDeathGt, dateOfDeathGte, dateOfDeathLt, dateOfDeathLte, dateOfDeathMonth, dateOfDeathYear, firstFlight, firstFlightGt, firstFlightGte, firstFlightLt, firstFlightLte, flightsCount, flightsCountGt, flightsCountGte, flightsCountLt, flightsCountLte, hasFlown, inSpace, isHuman, landingsCount, landingsCountGt, landingsCountGte, landingsCountLt, landingsCountLte, lastFlight, lastFlightGt, lastFlightGte, lastFlightLt, lastFlightLte, limit, nationality, offset, ordering, search, statusIds, typeId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AstronautsApi#astronautsMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AstronautsApi#astronautsMiniList")
    e.printStackTrace()
}
```

### Parameters
| **age** | **kotlin.Int**|  | [optional] |
| **ageGt** | **kotlin.Int**|  | [optional] |
| **ageGte** | **kotlin.Int**|  | [optional] |
| **ageLt** | **kotlin.Int**|  | [optional] |
| **ageLte** | **kotlin.Int**|  | [optional] |
| **agencyIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated agency IDs. | [optional] |
| **dateOfBirth** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfBirthGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfBirthMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfBirthYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeath** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeathGt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathGte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathLt** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathLte** | **kotlinx.datetime.LocalDate**|  | [optional] |
| **dateOfDeathMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **dateOfDeathYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **firstFlight** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **firstFlightLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **flightsCount** | **kotlin.Int**|  | [optional] |
| **flightsCountGt** | **kotlin.Int**|  | [optional] |
| **flightsCountGte** | **kotlin.Int**|  | [optional] |
| **flightsCountLt** | **kotlin.Int**|  | [optional] |
| **flightsCountLte** | **kotlin.Int**|  | [optional] |
| **hasFlown** | **kotlin.Boolean**|  | [optional] |
| **inSpace** | **kotlin.Boolean**|  | [optional] |
| **isHuman** | **kotlin.Boolean**|  | [optional] |
| **landingsCount** | **kotlin.Int**|  | [optional] |
| **landingsCountGt** | **kotlin.Int**|  | [optional] |
| **landingsCountGte** | **kotlin.Int**|  | [optional] |
| **landingsCountLt** | **kotlin.Int**|  | [optional] |
| **landingsCountLte** | **kotlin.Int**|  | [optional] |
| **lastFlight** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightGt** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightGte** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightLt** | **kotlinx.datetime.Instant**|  | [optional] |
| **lastFlightLte** | **kotlinx.datetime.Instant**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **nationality** | **kotlin.String**| Nationality | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Comma-separated astronaut status IDs. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **typeId** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedAstronautEndpointListList**](PaginatedAstronautEndpointListList.md)

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

<a id="astronautsRetrieve"></a>
# **astronautsRetrieve**
> AstronautEndpointDetailed astronautsRetrieve(id)



#### Filters Parameters - &#x60;age&#x60;, &#x60;age__gt&#x60;, &#x60;age__gte&#x60;, &#x60;age__lt&#x60;, &#x60;age__lte&#x60;, &#x60;agency_ids&#x60;, &#x60;date_of_birth&#x60;, &#x60;date_of_birth__day&#x60;, &#x60;date_of_birth__gt&#x60;, &#x60;date_of_birth__gte&#x60;, &#x60;date_of_birth__lt&#x60;, &#x60;date_of_birth__lte&#x60;, &#x60;date_of_birth__month&#x60;, &#x60;date_of_birth__year&#x60;, &#x60;date_of_death&#x60;, &#x60;date_of_death__day&#x60;, &#x60;date_of_death__gt&#x60;, &#x60;date_of_death__gte&#x60;, &#x60;date_of_death__lt&#x60;, &#x60;date_of_death__lte&#x60;, &#x60;date_of_death__month&#x60;, &#x60;date_of_death__year&#x60;, &#x60;first_flight&#x60;, &#x60;first_flight__gt&#x60;, &#x60;first_flight__gte&#x60;, &#x60;first_flight__lt&#x60;, &#x60;first_flight__lte&#x60;, &#x60;flights_count&#x60;, &#x60;flights_count__gt&#x60;, &#x60;flights_count__gte&#x60;, &#x60;flights_count__lt&#x60;, &#x60;flights_count__lte&#x60;, &#x60;has_flown&#x60;, &#x60;in_space&#x60;, &#x60;is_human&#x60;, &#x60;landings_count&#x60;, &#x60;landings_count__gt&#x60;, &#x60;landings_count__gte&#x60;, &#x60;landings_count__lt&#x60;, &#x60;landings_count__lte&#x60;, &#x60;last_flight&#x60;, &#x60;last_flight__gt&#x60;, &#x60;last_flight__gte&#x60;, &#x60;last_flight__lt&#x60;, &#x60;last_flight__lte&#x60;, &#x60;nationality&#x60;, &#x60;status_ids&#x60;, &#x60;type__id&#x60;  Example - [/astronauts/?has_flown&#x3D;true](./?has_flown&#x3D;true)  #### Search Fields searched - &#x60;agency__abbrev&#x60;, &#x60;agency__name&#x60;, &#x60;name&#x60;, &#x60;nationality__nationality_name&#x60;  Example - [/astronauts/?search&#x3D;Pesquet](./?search&#x3D;Pesquet)  #### Ordering Fields - &#x60;age&#x60;, &#x60;date_of_birth&#x60;, &#x60;eva_time&#x60;, &#x60;flights_count&#x60;, &#x60;id&#x60;, &#x60;landings_count&#x60;, &#x60;last_flight&#x60;, &#x60;name&#x60;, &#x60;spacewalks_count&#x60;, &#x60;status&#x60;, &#x60;time_in_space&#x60;  Example - [/astronauts/?ordering&#x3D;-time_in_space](./?ordering&#x3D;-time_in_space)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/astronauts/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/astronauts/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = AstronautsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Astronaut.
try {
    val result : AstronautEndpointDetailed = apiInstance.astronautsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AstronautsApi#astronautsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AstronautsApi#astronautsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Astronaut. | |

### Return type

[**AstronautEndpointDetailed**](AstronautEndpointDetailed.md)

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

