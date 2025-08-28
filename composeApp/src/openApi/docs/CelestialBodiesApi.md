# CelestialBodiesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**celestialBodiesDetailedList**](CelestialBodiesApi.md#celestialBodiesDetailedList) | **GET** /api/ll/2.4.0/celestial_bodies/detailed/ |  |
| [**celestialBodiesList**](CelestialBodiesApi.md#celestialBodiesList) | **GET** /api/ll/2.4.0/celestial_bodies/ |  |
| [**celestialBodiesMiniList**](CelestialBodiesApi.md#celestialBodiesMiniList) | **GET** /api/ll/2.4.0/celestial_bodies/mini/ |  |
| [**celestialBodiesRetrieve**](CelestialBodiesApi.md#celestialBodiesRetrieve) | **GET** /api/ll/2.4.0/celestial_bodies/{id}/ |  |


<a id="celestialBodiesDetailedList"></a>
# **celestialBodiesDetailedList**
> PaginatedCelestialBodyEndpointDetailedList celestialBodiesDetailedList(limit, offset, ordering, search)



#### Search Fields searched - &#x60;name&#x60;, &#x60;type__name&#x60;  Example - [/celestial_bodies/detailed/?search&#x3D;Mars](./?search&#x3D;Mars)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;  Example - [/celestial_bodies/detailed/?ordering&#x3D;-name](./?ordering&#x3D;-name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/celestial_bodies/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/celestial_bodies/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = CelestialBodiesApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedCelestialBodyEndpointDetailedList = apiInstance.celestialBodiesDetailedList(limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CelestialBodiesApi#celestialBodiesDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CelestialBodiesApi#celestialBodiesDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedCelestialBodyEndpointDetailedList**](PaginatedCelestialBodyEndpointDetailedList.md)

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

<a id="celestialBodiesList"></a>
# **celestialBodiesList**
> PaginatedCelestialBodyNormalList celestialBodiesList(limit, offset, ordering, search)



#### Search Fields searched - &#x60;name&#x60;, &#x60;type__name&#x60;  Example - [/celestial_bodies/?search&#x3D;Mars](./?search&#x3D;Mars)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;  Example - [/celestial_bodies/?ordering&#x3D;-name](./?ordering&#x3D;-name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/celestial_bodies/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/celestial_bodies/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = CelestialBodiesApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedCelestialBodyNormalList = apiInstance.celestialBodiesList(limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CelestialBodiesApi#celestialBodiesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CelestialBodiesApi#celestialBodiesList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedCelestialBodyNormalList**](PaginatedCelestialBodyNormalList.md)

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

<a id="celestialBodiesMiniList"></a>
# **celestialBodiesMiniList**
> PaginatedCelestialBodyMiniList celestialBodiesMiniList(limit, offset, ordering, search)



#### Search Fields searched - &#x60;name&#x60;, &#x60;type__name&#x60;  Example - [/celestial_bodies/mini/?search&#x3D;Mars](./?search&#x3D;Mars)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;  Example - [/celestial_bodies/mini/?ordering&#x3D;-name](./?ordering&#x3D;-name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/celestial_bodies/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/celestial_bodies/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = CelestialBodiesApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedCelestialBodyMiniList = apiInstance.celestialBodiesMiniList(limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CelestialBodiesApi#celestialBodiesMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CelestialBodiesApi#celestialBodiesMiniList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedCelestialBodyMiniList**](PaginatedCelestialBodyMiniList.md)

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

<a id="celestialBodiesRetrieve"></a>
# **celestialBodiesRetrieve**
> CelestialBodyEndpointDetailed celestialBodiesRetrieve(id)



#### Search Fields searched - &#x60;name&#x60;, &#x60;type__name&#x60;  Example - [/celestial_bodies/?search&#x3D;Mars](./?search&#x3D;Mars)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;  Example - [/celestial_bodies/?ordering&#x3D;-name](./?ordering&#x3D;-name)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/celestial_bodies/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/celestial_bodies/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = CelestialBodiesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Celestial Body.
try {
    val result : CelestialBodyEndpointDetailed = apiInstance.celestialBodiesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CelestialBodiesApi#celestialBodiesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CelestialBodiesApi#celestialBodiesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Celestial Body. | |

### Return type

[**CelestialBodyEndpointDetailed**](CelestialBodyEndpointDetailed.md)

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

