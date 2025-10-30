# UpdatesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**updatesList**](UpdatesApi.md#updatesList) | **GET** /api/ll/2.4.0/updates/ |  |
| [**updatesRetrieve**](UpdatesApi.md#updatesRetrieve) | **GET** /api/ll/2.4.0/updates/{id}/ |  |


<a id="updatesList"></a>
# **updatesList**
> PaginatedUpdateEndpointList updatesList(createdOn, launch, launchLaunchServiceProvider, limit, offset, ordering, program, search)



#### Filters Parameters - &#x60;created_on&#x60;, &#x60;launch&#x60;, &#x60;launch__launch_service_provider&#x60;, &#x60;program&#x60;  Example - [/updates/?launch__launch_service_provider&#x3D;121](./?launch__launch_service_provider&#x3D;121)  #### Ordering Fields - &#x60;created_on&#x60;  Example - [/updates/?ordering&#x3D;-created_on](./?ordering&#x3D;-created_on)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/updates/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/updates/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = UpdatesApi()
val createdOn : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | 
val launch : kotlin.String = 38400000-8cf0-11bd-b23e-10b96e4ef00d // kotlin.String | 
val launchLaunchServiceProvider : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val program : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedUpdateEndpointList = apiInstance.updatesList(createdOn, launch, launchLaunchServiceProvider, limit, offset, ordering, program, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UpdatesApi#updatesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UpdatesApi#updatesList")
    e.printStackTrace()
}
```

### Parameters
| **createdOn** | **kotlin.time.Instant**|  | [optional] |
| **launch** | **kotlin.String**|  | [optional] |
| **launchLaunchServiceProvider** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **program** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedUpdateEndpointList**](PaginatedUpdateEndpointList.md)

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

<a id="updatesRetrieve"></a>
# **updatesRetrieve**
> UpdateEndpoint updatesRetrieve(id)



#### Filters Parameters - &#x60;created_on&#x60;, &#x60;launch&#x60;, &#x60;launch__launch_service_provider&#x60;, &#x60;program&#x60;  Example - [/updates/?launch__launch_service_provider&#x3D;121](./?launch__launch_service_provider&#x3D;121)  #### Ordering Fields - &#x60;created_on&#x60;  Example - [/updates/?ordering&#x3D;-created_on](./?ordering&#x3D;-created_on)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/updates/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/updates/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = UpdatesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Update.
try {
    val result : UpdateEndpoint = apiInstance.updatesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UpdatesApi#updatesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UpdatesApi#updatesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Update. | |

### Return type

[**UpdateEndpoint**](UpdateEndpoint.md)

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

