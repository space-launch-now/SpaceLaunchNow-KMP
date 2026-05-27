# StagesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**stagesList**](StagesApi.md#stagesList) | **GET** /api/ll/2.4.0/stages/ |  |
| [**stagesRetrieve**](StagesApi.md#stagesRetrieve) | **GET** /api/ll/2.4.0/stages/{id}/ |  |


<a id="stagesList"></a>
# **stagesList**
> PaginatedStageStandaloneNormalList stagesList(id, launcher, launcherIds, launcherSerialNumber, launcherSerialNumberIcontains, limit, offset, ordering, reused, rocket, rocketIds, search, type)



#### Filters Parameters - &#x60;id&#x60;, &#x60;launcher&#x60;, &#x60;launcher__ids&#x60;, &#x60;launcher__serial_number&#x60;, &#x60;launcher__serial_number__icontains&#x60;, &#x60;reused&#x60;, &#x60;rocket&#x60;, &#x60;rocket__ids&#x60;, &#x60;type&#x60;  Example - [/stages/?rocket__ids&#x3D;3007,3008](./?rocket__ids&#x3D;3007,3008)  #### Search Fields searched - &#x60;launcher__serial_number&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__launch__name&#x60;  Example - [/stages/?search&#x3D;B1058](./?search&#x3D;B1058)  #### Ordering Fields - &#x60;id&#x60;, &#x60;launcher_flight_number&#x60;, &#x60;previous_flight_date&#x60;  Example - [/stages/?ordering&#x3D;-previous_flight_date](./?ordering&#x3D;-previous_flight_date)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/stages/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/stages/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging.spacelaunchnow.app/docs](https://staging.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = StagesApi()
val id : kotlin.Int = 56 // kotlin.Int | 
val launcher : kotlin.Int = 56 // kotlin.Int | 
val launcherIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launcher IDs.
val launcherSerialNumber : kotlin.String = launcherSerialNumber_example // kotlin.String | 
val launcherSerialNumberIcontains : kotlin.String = launcherSerialNumberIcontains_example // kotlin.String | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val reused : kotlin.Boolean = true // kotlin.Boolean | 
val rocket : kotlin.Int = 56 // kotlin.Int | 
val rocketIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated rocket IDs.
val search : kotlin.String = search_example // kotlin.String | A search term.
val type : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : PaginatedStageStandaloneNormalList = apiInstance.stagesList(id, launcher, launcherIds, launcherSerialNumber, launcherSerialNumberIcontains, limit, offset, ordering, reused, rocket, rocketIds, search, type)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling StagesApi#stagesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling StagesApi#stagesList")
    e.printStackTrace()
}
```

### Parameters
| **id** | **kotlin.Int**|  | [optional] |
| **launcher** | **kotlin.Int**|  | [optional] |
| **launcherIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launcher IDs. | [optional] |
| **launcherSerialNumber** | **kotlin.String**|  | [optional] |
| **launcherSerialNumberIcontains** | **kotlin.String**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **reused** | **kotlin.Boolean**|  | [optional] |
| **rocket** | **kotlin.Int**|  | [optional] |
| **rocketIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated rocket IDs. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **type** | **kotlin.Int**|  | [optional] |

### Return type

[**PaginatedStageStandaloneNormalList**](PaginatedStageStandaloneNormalList.md)

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

<a id="stagesRetrieve"></a>
# **stagesRetrieve**
> StageStandaloneDetailed stagesRetrieve(id)



#### Filters Parameters - &#x60;id&#x60;, &#x60;launcher&#x60;, &#x60;launcher__ids&#x60;, &#x60;launcher__serial_number&#x60;, &#x60;launcher__serial_number__icontains&#x60;, &#x60;reused&#x60;, &#x60;rocket&#x60;, &#x60;rocket__ids&#x60;, &#x60;type&#x60;  Example - [/stages/?rocket__ids&#x3D;3007,3008](./?rocket__ids&#x3D;3007,3008)  #### Search Fields searched - &#x60;launcher__serial_number&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__launch__name&#x60;  Example - [/stages/?search&#x3D;B1058](./?search&#x3D;B1058)  #### Ordering Fields - &#x60;id&#x60;, &#x60;launcher_flight_number&#x60;, &#x60;previous_flight_date&#x60;  Example - [/stages/?ordering&#x3D;-previous_flight_date](./?ordering&#x3D;-previous_flight_date)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/stages/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/stages/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging.spacelaunchnow.app/docs](https://staging.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = StagesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this first stage.
try {
    val result : StageStandaloneDetailed = apiInstance.stagesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling StagesApi#stagesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling StagesApi#stagesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this first stage. | |

### Return type

[**StageStandaloneDetailed**](StageStandaloneDetailed.md)

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

