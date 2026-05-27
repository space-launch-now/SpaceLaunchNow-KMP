# RocketsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**rocketsList**](RocketsApi.md#rocketsList) | **GET** /api/ll/2.4.0/rockets/ |  |
| [**rocketsRetrieve**](RocketsApi.md#rocketsRetrieve) | **GET** /api/ll/2.4.0/rockets/{id}/ |  |


<a id="rocketsList"></a>
# **rocketsList**
> PaginatedRocketStandaloneNormalList rocketsList(configurationFullName, configurationFullNameIcontains, configurationIds, configurationManufacturerName, configurationManufacturerNameIcontains, configurationName, configurationNameIcontains, configurationId, id, limit, offset, ordering, search)



#### Filters Parameters - &#x60;configuration__full_name&#x60;, &#x60;configuration__full_name__icontains&#x60;, &#x60;configuration__ids&#x60;, &#x60;configuration__manufacturer__name&#x60;, &#x60;configuration__manufacturer__name__icontains&#x60;, &#x60;configuration__name&#x60;, &#x60;configuration__name__icontains&#x60;, &#x60;configuration_id&#x60;, &#x60;id&#x60;  Example - [/rockets/?configuration_id&#x3D;164](./?configuration_id&#x3D;164)  #### Search Fields searched - &#x60;configuration__full_name&#x60;, &#x60;configuration__manufacturer__name&#x60;, &#x60;configuration__name&#x60;, &#x60;launch__name&#x60;  Example - [/rockets/?search&#x3D;Falcon](./?search&#x3D;Falcon)  #### Ordering Fields - &#x60;configuration__name&#x60;, &#x60;id&#x60;  Example - [/rockets/?ordering&#x3D;-id](./?ordering&#x3D;-id)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/rockets/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/rockets/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging.spacelaunchnow.app/docs](https://staging.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = RocketsApi()
val configurationFullName : kotlin.String = configurationFullName_example // kotlin.String | 
val configurationFullNameIcontains : kotlin.String = configurationFullNameIcontains_example // kotlin.String | 
val configurationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launcher configuration IDs.
val configurationManufacturerName : kotlin.String = configurationManufacturerName_example // kotlin.String | 
val configurationManufacturerNameIcontains : kotlin.String = configurationManufacturerNameIcontains_example // kotlin.String | 
val configurationName : kotlin.String = configurationName_example // kotlin.String | 
val configurationNameIcontains : kotlin.String = configurationNameIcontains_example // kotlin.String | 
val configurationId : kotlin.Int = 56 // kotlin.Int | Single launcher configuration ID.
val id : kotlin.Int = 56 // kotlin.Int | 
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedRocketStandaloneNormalList = apiInstance.rocketsList(configurationFullName, configurationFullNameIcontains, configurationIds, configurationManufacturerName, configurationManufacturerNameIcontains, configurationName, configurationNameIcontains, configurationId, id, limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling RocketsApi#rocketsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling RocketsApi#rocketsList")
    e.printStackTrace()
}
```

### Parameters
| **configurationFullName** | **kotlin.String**|  | [optional] |
| **configurationFullNameIcontains** | **kotlin.String**|  | [optional] |
| **configurationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launcher configuration IDs. | [optional] |
| **configurationManufacturerName** | **kotlin.String**|  | [optional] |
| **configurationManufacturerNameIcontains** | **kotlin.String**|  | [optional] |
| **configurationName** | **kotlin.String**|  | [optional] |
| **configurationNameIcontains** | **kotlin.String**|  | [optional] |
| **configurationId** | **kotlin.Int**| Single launcher configuration ID. | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedRocketStandaloneNormalList**](PaginatedRocketStandaloneNormalList.md)

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

<a id="rocketsRetrieve"></a>
# **rocketsRetrieve**
> RocketStandaloneDetailed rocketsRetrieve(id)



#### Filters Parameters - &#x60;configuration__full_name&#x60;, &#x60;configuration__full_name__icontains&#x60;, &#x60;configuration__ids&#x60;, &#x60;configuration__manufacturer__name&#x60;, &#x60;configuration__manufacturer__name__icontains&#x60;, &#x60;configuration__name&#x60;, &#x60;configuration__name__icontains&#x60;, &#x60;configuration_id&#x60;, &#x60;id&#x60;  Example - [/rockets/?configuration_id&#x3D;164](./?configuration_id&#x3D;164)  #### Search Fields searched - &#x60;configuration__full_name&#x60;, &#x60;configuration__manufacturer__name&#x60;, &#x60;configuration__name&#x60;, &#x60;launch__name&#x60;  Example - [/rockets/?search&#x3D;Falcon](./?search&#x3D;Falcon)  #### Ordering Fields - &#x60;configuration__name&#x60;, &#x60;id&#x60;  Example - [/rockets/?ordering&#x3D;-id](./?ordering&#x3D;-id)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/rockets/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/rockets/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging.spacelaunchnow.app/docs](https://staging.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = RocketsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this rocket.
try {
    val result : RocketStandaloneDetailed = apiInstance.rocketsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling RocketsApi#rocketsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling RocketsApi#rocketsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this rocket. | |

### Return type

[**RocketStandaloneDetailed**](RocketStandaloneDetailed.md)

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

