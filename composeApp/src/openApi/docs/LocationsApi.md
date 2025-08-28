# LocationsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**locationsDetailedList**](LocationsApi.md#locationsDetailedList) | **GET** /api/ll/2.4.0/locations/detailed/ |  |
| [**locationsList**](LocationsApi.md#locationsList) | **GET** /api/ll/2.4.0/locations/ |  |
| [**locationsRetrieve**](LocationsApi.md#locationsRetrieve) | **GET** /api/ll/2.4.0/locations/{id}/ |  |


<a id="locationsDetailedList"></a>
# **locationsDetailedList**
> PaginatedLocationSerializerWithPadsList locationsDetailedList(active, countryCode, id, limit, name, nameContains, offset, ordering, search, totalLandingCount, totalLandingCountGt, totalLandingCountGte, totalLandingCountLt, totalLandingCountLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)



#### Filters Parameters - &#x60;active&#x60;, &#x60;country_code&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;total_landing_count&#x60;, &#x60;total_landing_count__gt&#x60;, &#x60;total_landing_count__gte&#x60;, &#x60;total_landing_count__lt&#x60;, &#x60;total_landing_count__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/locations/detailed/?country_code&#x3D;NZL](./?country_code&#x3D;NZL)  #### Search Fields searched - &#x60;country__alpha_3_code&#x60;, &#x60;name&#x60;  Example - [/locations/detailed/?search&#x3D;Cape Canaveral](./?search&#x3D;Cape Canaveral)  #### Ordering Fields - &#x60;total_landing_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/locations/detailed/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/locations/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/locations/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LocationsApi()
val active : kotlin.Boolean = true // kotlin.Boolean | 
val countryCode : kotlin.String = countryCode_example // kotlin.String | Country Code
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val totalLandingCount : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountGt : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountGte : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountLt : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountLte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCount : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedLocationSerializerWithPadsList = apiInstance.locationsDetailedList(active, countryCode, id, limit, name, nameContains, offset, ordering, search, totalLandingCount, totalLandingCountGt, totalLandingCountGte, totalLandingCountLt, totalLandingCountLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LocationsApi#locationsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LocationsApi#locationsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **active** | **kotlin.Boolean**|  | [optional] |
| **countryCode** | **kotlin.String**| Country Code | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **totalLandingCount** | **kotlin.Int**|  | [optional] |
| **totalLandingCountGt** | **kotlin.Int**|  | [optional] |
| **totalLandingCountGte** | **kotlin.Int**|  | [optional] |
| **totalLandingCountLt** | **kotlin.Int**|  | [optional] |
| **totalLandingCountLte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCount** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGt** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedLocationSerializerWithPadsList**](PaginatedLocationSerializerWithPadsList.md)

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

<a id="locationsList"></a>
# **locationsList**
> PaginatedLocationDetailedList locationsList(active, countryCode, id, limit, name, nameContains, offset, ordering, search, totalLandingCount, totalLandingCountGt, totalLandingCountGte, totalLandingCountLt, totalLandingCountLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)



#### Filters Parameters - &#x60;active&#x60;, &#x60;country_code&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;total_landing_count&#x60;, &#x60;total_landing_count__gt&#x60;, &#x60;total_landing_count__gte&#x60;, &#x60;total_landing_count__lt&#x60;, &#x60;total_landing_count__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/locations/?country_code&#x3D;NZL](./?country_code&#x3D;NZL)  #### Search Fields searched - &#x60;country__alpha_3_code&#x60;, &#x60;name&#x60;  Example - [/locations/?search&#x3D;Cape Canaveral](./?search&#x3D;Cape Canaveral)  #### Ordering Fields - &#x60;total_landing_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/locations/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/locations/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/locations/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LocationsApi()
val active : kotlin.Boolean = true // kotlin.Boolean | 
val countryCode : kotlin.String = countryCode_example // kotlin.String | Country Code
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val totalLandingCount : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountGt : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountGte : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountLt : kotlin.Int = 56 // kotlin.Int | 
val totalLandingCountLte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCount : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountGte : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLt : kotlin.Int = 56 // kotlin.Int | 
val totalLaunchCountLte : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedLocationDetailedList = apiInstance.locationsList(active, countryCode, id, limit, name, nameContains, offset, ordering, search, totalLandingCount, totalLandingCountGt, totalLandingCountGte, totalLandingCountLt, totalLandingCountLte, totalLaunchCount, totalLaunchCountGt, totalLaunchCountGte, totalLaunchCountLt, totalLaunchCountLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LocationsApi#locationsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LocationsApi#locationsList")
    e.printStackTrace()
}
```

### Parameters
| **active** | **kotlin.Boolean**|  | [optional] |
| **countryCode** | **kotlin.String**| Country Code | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **totalLandingCount** | **kotlin.Int**|  | [optional] |
| **totalLandingCountGt** | **kotlin.Int**|  | [optional] |
| **totalLandingCountGte** | **kotlin.Int**|  | [optional] |
| **totalLandingCountLt** | **kotlin.Int**|  | [optional] |
| **totalLandingCountLte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCount** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGt** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountGte** | **kotlin.Int**|  | [optional] |
| **totalLaunchCountLt** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **totalLaunchCountLte** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedLocationDetailedList**](PaginatedLocationDetailedList.md)

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

<a id="locationsRetrieve"></a>
# **locationsRetrieve**
> LocationSerializerWithPads locationsRetrieve(id)



#### Filters Parameters - &#x60;active&#x60;, &#x60;country_code&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;, &#x60;total_landing_count&#x60;, &#x60;total_landing_count__gt&#x60;, &#x60;total_landing_count__gte&#x60;, &#x60;total_landing_count__lt&#x60;, &#x60;total_landing_count__lte&#x60;, &#x60;total_launch_count&#x60;, &#x60;total_launch_count__gt&#x60;, &#x60;total_launch_count__gte&#x60;, &#x60;total_launch_count__lt&#x60;, &#x60;total_launch_count__lte&#x60;  Example - [/locations/?country_code&#x3D;NZL](./?country_code&#x3D;NZL)  #### Search Fields searched - &#x60;country__alpha_3_code&#x60;, &#x60;name&#x60;  Example - [/locations/?search&#x3D;Cape Canaveral](./?search&#x3D;Cape Canaveral)  #### Ordering Fields - &#x60;total_landing_count&#x60;, &#x60;total_launch_count&#x60;  Example - [/locations/?ordering&#x3D;-total_launch_count](./?ordering&#x3D;-total_launch_count)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/locations/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/locations/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LocationsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Location.
try {
    val result : LocationSerializerWithPads = apiInstance.locationsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LocationsApi#locationsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LocationsApi#locationsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Location. | |

### Return type

[**LocationSerializerWithPads**](LocationSerializerWithPads.md)

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

