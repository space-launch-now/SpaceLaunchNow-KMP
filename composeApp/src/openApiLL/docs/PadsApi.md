# PadsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**padsList**](PadsApi.md#padsList) | **GET** /api/ll/2.4.0/pads/ |  |
| [**padsRetrieve**](PadsApi.md#padsRetrieve) | **GET** /api/ll/2.4.0/pads/{id}/ |  |


<a id="padsList"></a>
# **padsList**
> PaginatedPadDetailedList padsList(active, agenciesIds, id, idContains, latitudeGt, latitudeGte, latitudeLt, latitudeLte, limit, locationId, locationName, locationNameContains, longitudeGt, longitudeGte, longitudeLt, longitudeLte, name, nameContains, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, ordering, search, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)



#### Filters Parameters - &#x60;active&#x60;, &#x60;agencies_ids&#x60;, &#x60;id&#x60;, &#x60;id__contains&#x60;, &#x60;latitude__gt&#x60;, &#x60;latitude__gte&#x60;, &#x60;latitude__lt&#x60;, &#x60;latitude__lte&#x60;, &#x60;location__id&#x60;, &#x60;location__name&#x60;, &#x60;location__name__contains&#x60;, &#x60;longitude__gt&#x60;, &#x60;longitude__gte&#x60;, &#x60;longitude__lt&#x60;, &#x60;longitude__lte&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;orbital_launch_attempt_count__gt&#x60;, &#x60;orbital_launch_attempt_count__gte&#x60;, &#x60;orbital_launch_attempt_count__lt&#x60;, &#x60;orbital_launch_attempt_count__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/pads/?location__id&#x3D;11](./?location__id&#x3D;11)  #### Search Fields searched - &#x60;location__name&#x60;, &#x60;name&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/pads/?search&#x3D;39A](./?search&#x3D;39A)  #### Ordering Fields - &#x60;id&#x60;, &#x60;location__id&#x60;, &#x60;location__name&#x60;, &#x60;name&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/pads/?ordering&#x3D;-orbital_launch_attempt_count](./?ordering&#x3D;-orbital_launch_attempt_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/pads/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/pads/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = PadsApi()
val active : kotlin.Boolean = true // kotlin.Boolean | 
val agenciesIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val id : kotlin.Int = 56 // kotlin.Int | 
val idContains : kotlin.Int = 56 // kotlin.Int | 
val latitudeGt : kotlin.Float = 3.4 // kotlin.Float | 
val latitudeGte : kotlin.Float = 3.4 // kotlin.Float | 
val latitudeLt : kotlin.Float = 3.4 // kotlin.Float | 
val latitudeLte : kotlin.Float = 3.4 // kotlin.Float | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val locationId : kotlin.Int = 56 // kotlin.Int | 
val locationName : kotlin.String = locationName_example // kotlin.String | 
val locationNameContains : kotlin.String = locationNameContains_example // kotlin.String | 
val longitudeGt : kotlin.Float = 3.4 // kotlin.Float | 
val longitudeGte : kotlin.Float = 3.4 // kotlin.Float | 
val longitudeLt : kotlin.Float = 3.4 // kotlin.Float | 
val longitudeLte : kotlin.Float = 3.4 // kotlin.Float | 
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val orbitalLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val totalLaunchCount : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedPadDetailedList = apiInstance.padsList(active, agenciesIds, id, idContains, latitudeGt, latitudeGte, latitudeLt, latitudeLte, limit, locationId, locationName, locationNameContains, longitudeGt, longitudeGte, longitudeLt, longitudeLte, name, nameContains, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, ordering, search, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PadsApi#padsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PadsApi#padsList")
    e.printStackTrace()
}
```

### Parameters
| **active** | **kotlin.Boolean**|  | [optional] |
| **agenciesIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **idContains** | **kotlin.Int**|  | [optional] |
| **latitudeGt** | **kotlin.Float**|  | [optional] |
| **latitudeGte** | **kotlin.Float**|  | [optional] |
| **latitudeLt** | **kotlin.Float**|  | [optional] |
| **latitudeLte** | **kotlin.Float**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **locationId** | **kotlin.Int**|  | [optional] |
| **locationName** | **kotlin.String**|  | [optional] |
| **locationNameContains** | **kotlin.String**|  | [optional] |
| **longitudeGt** | **kotlin.Float**|  | [optional] |
| **longitudeGte** | **kotlin.Float**|  | [optional] |
| **longitudeLt** | **kotlin.Float**|  | [optional] |
| **longitudeLte** | **kotlin.Float**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **orbitalLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **totalLaunchCount** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGt** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedPadDetailedList**](PaginatedPadDetailedList.md)

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

<a id="padsRetrieve"></a>
# **padsRetrieve**
> PadDetailed padsRetrieve(id)



#### Filters Parameters - &#x60;active&#x60;, &#x60;agencies_ids&#x60;, &#x60;id&#x60;, &#x60;id__contains&#x60;, &#x60;latitude__gt&#x60;, &#x60;latitude__gte&#x60;, &#x60;latitude__lt&#x60;, &#x60;latitude__lte&#x60;, &#x60;location__id&#x60;, &#x60;location__name&#x60;, &#x60;location__name__contains&#x60;, &#x60;longitude__gt&#x60;, &#x60;longitude__gte&#x60;, &#x60;longitude__lt&#x60;, &#x60;longitude__lte&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;orbital_launch_attempt_count__gt&#x60;, &#x60;orbital_launch_attempt_count__gte&#x60;, &#x60;orbital_launch_attempt_count__lt&#x60;, &#x60;orbital_launch_attempt_count__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/pads/?location__id&#x3D;11](./?location__id&#x3D;11)  #### Search Fields searched - &#x60;location__name&#x60;, &#x60;name&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/pads/?search&#x3D;39A](./?search&#x3D;39A)  #### Ordering Fields - &#x60;id&#x60;, &#x60;location__id&#x60;, &#x60;location__name&#x60;, &#x60;name&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/pads/?ordering&#x3D;-orbital_launch_attempt_count](./?ordering&#x3D;-orbital_launch_attempt_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/pads/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/pads/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = PadsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Pad.
try {
    val result : PadDetailed = apiInstance.padsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PadsApi#padsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PadsApi#padsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Pad. | |

### Return type

[**PadDetailed**](PadDetailed.md)

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

