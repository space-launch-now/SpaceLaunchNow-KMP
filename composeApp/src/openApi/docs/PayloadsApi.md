# PayloadsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**payloadsDetailedList**](PayloadsApi.md#payloadsDetailedList) | **GET** /api/ll/2.4.0/payloads/detailed/ |  |
| [**payloadsList**](PayloadsApi.md#payloadsList) | **GET** /api/ll/2.4.0/payloads/ |  |
| [**payloadsMiniList**](PayloadsApi.md#payloadsMiniList) | **GET** /api/ll/2.4.0/payloads/mini/ |  |
| [**payloadsRetrieve**](PayloadsApi.md#payloadsRetrieve) | **GET** /api/ll/2.4.0/payloads/{id}/ |  |


<a id="payloadsDetailedList"></a>
# **payloadsDetailedList**
> PaginatedPayloadDetailedList payloadsDetailedList(limit, manufacturerId, manufacturerName, name, offset, `operator`, operatorId, operatorName, ordering, programId, search)



#### Filters Parameters - &#x60;manufacturer__id&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator&#x60;, &#x60;operator__id&#x60;, &#x60;operator__name&#x60;, &#x60;program__id&#x60;  Example - [/payloads/detailed/?program__id&#x3D;18](./?program__id&#x3D;18)  #### Search Fields searched - &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator__name&#x60;, &#x60;payloadflight__destination&#x60;, &#x60;payloadflight__rocket__launch__name&#x60;  Example - [/payloads/detailed/?search&#x3D;EarthCare](./?search&#x3D;EarthCare)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;, &#x60;payloadflight__rocket__launch__net&#x60;  Example - [/payloads/detailed/?ordering&#x3D;-payloadflight__rocket__launch__net](./?ordering&#x3D;-payloadflight__rocket__launch__net)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/payloads/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/payloads/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = PayloadsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val manufacturerId : kotlin.Int = 56 // kotlin.Int | 
val manufacturerName : kotlin.String = manufacturerName_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val `operator` : kotlin.Int = 56 // kotlin.Int | 
val operatorId : kotlin.Int = 56 // kotlin.Int | 
val operatorName : kotlin.String = operatorName_example // kotlin.String | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val programId : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedPayloadDetailedList = apiInstance.payloadsDetailedList(limit, manufacturerId, manufacturerName, name, offset, `operator`, operatorId, operatorName, ordering, programId, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PayloadsApi#payloadsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PayloadsApi#payloadsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **manufacturerId** | **kotlin.Int**|  | [optional] |
| **manufacturerName** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **&#x60;operator&#x60;** | **kotlin.Int**|  | [optional] |
| **operatorId** | **kotlin.Int**|  | [optional] |
| **operatorName** | **kotlin.String**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **programId** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedPayloadDetailedList**](PaginatedPayloadDetailedList.md)

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

<a id="payloadsList"></a>
# **payloadsList**
> PaginatedPayloadNormalList payloadsList(limit, manufacturerId, manufacturerName, name, offset, `operator`, operatorId, operatorName, ordering, programId, search)



#### Filters Parameters - &#x60;manufacturer__id&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator&#x60;, &#x60;operator__id&#x60;, &#x60;operator__name&#x60;, &#x60;program__id&#x60;  Example - [/payloads/?program__id&#x3D;18](./?program__id&#x3D;18)  #### Search Fields searched - &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator__name&#x60;, &#x60;payloadflight__destination&#x60;, &#x60;payloadflight__rocket__launch__name&#x60;  Example - [/payloads/?search&#x3D;EarthCare](./?search&#x3D;EarthCare)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;, &#x60;payloadflight__rocket__launch__net&#x60;  Example - [/payloads/?ordering&#x3D;-payloadflight__rocket__launch__net](./?ordering&#x3D;-payloadflight__rocket__launch__net)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/payloads/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/payloads/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = PayloadsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val manufacturerId : kotlin.Int = 56 // kotlin.Int | 
val manufacturerName : kotlin.String = manufacturerName_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val `operator` : kotlin.Int = 56 // kotlin.Int | 
val operatorId : kotlin.Int = 56 // kotlin.Int | 
val operatorName : kotlin.String = operatorName_example // kotlin.String | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val programId : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedPayloadNormalList = apiInstance.payloadsList(limit, manufacturerId, manufacturerName, name, offset, `operator`, operatorId, operatorName, ordering, programId, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PayloadsApi#payloadsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PayloadsApi#payloadsList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **manufacturerId** | **kotlin.Int**|  | [optional] |
| **manufacturerName** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **&#x60;operator&#x60;** | **kotlin.Int**|  | [optional] |
| **operatorId** | **kotlin.Int**|  | [optional] |
| **operatorName** | **kotlin.String**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **programId** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedPayloadNormalList**](PaginatedPayloadNormalList.md)

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

<a id="payloadsMiniList"></a>
# **payloadsMiniList**
> PaginatedPayloadMiniList payloadsMiniList(limit, manufacturerId, manufacturerName, name, offset, `operator`, operatorId, operatorName, ordering, programId, search)



#### Filters Parameters - &#x60;manufacturer__id&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator&#x60;, &#x60;operator__id&#x60;, &#x60;operator__name&#x60;, &#x60;program__id&#x60;  Example - [/payloads/mini/?program__id&#x3D;18](./?program__id&#x3D;18)  #### Search Fields searched - &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator__name&#x60;, &#x60;payloadflight__destination&#x60;, &#x60;payloadflight__rocket__launch__name&#x60;  Example - [/payloads/mini/?search&#x3D;EarthCare](./?search&#x3D;EarthCare)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;, &#x60;payloadflight__rocket__launch__net&#x60;  Example - [/payloads/mini/?ordering&#x3D;-payloadflight__rocket__launch__net](./?ordering&#x3D;-payloadflight__rocket__launch__net)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/payloads/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/payloads/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = PayloadsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val manufacturerId : kotlin.Int = 56 // kotlin.Int | 
val manufacturerName : kotlin.String = manufacturerName_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val `operator` : kotlin.Int = 56 // kotlin.Int | 
val operatorId : kotlin.Int = 56 // kotlin.Int | 
val operatorName : kotlin.String = operatorName_example // kotlin.String | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val programId : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedPayloadMiniList = apiInstance.payloadsMiniList(limit, manufacturerId, manufacturerName, name, offset, `operator`, operatorId, operatorName, ordering, programId, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PayloadsApi#payloadsMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PayloadsApi#payloadsMiniList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **manufacturerId** | **kotlin.Int**|  | [optional] |
| **manufacturerName** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **&#x60;operator&#x60;** | **kotlin.Int**|  | [optional] |
| **operatorId** | **kotlin.Int**|  | [optional] |
| **operatorName** | **kotlin.String**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **programId** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedPayloadMiniList**](PaginatedPayloadMiniList.md)

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

<a id="payloadsRetrieve"></a>
# **payloadsRetrieve**
> PayloadDetailed payloadsRetrieve(id)



#### Filters Parameters - &#x60;manufacturer__id&#x60;, &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator&#x60;, &#x60;operator__id&#x60;, &#x60;operator__name&#x60;, &#x60;program__id&#x60;  Example - [/payloads/?program__id&#x3D;18](./?program__id&#x3D;18)  #### Search Fields searched - &#x60;manufacturer__name&#x60;, &#x60;name&#x60;, &#x60;operator__name&#x60;, &#x60;payloadflight__destination&#x60;, &#x60;payloadflight__rocket__launch__name&#x60;  Example - [/payloads/?search&#x3D;EarthCare](./?search&#x3D;EarthCare)  #### Ordering Fields - &#x60;id&#x60;, &#x60;name&#x60;, &#x60;payloadflight__rocket__launch__net&#x60;  Example - [/payloads/?ordering&#x3D;-payloadflight__rocket__launch__net](./?ordering&#x3D;-payloadflight__rocket__launch__net)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/payloads/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/payloads/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = PayloadsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this payload.
try {
    val result : PayloadDetailed = apiInstance.payloadsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PayloadsApi#payloadsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PayloadsApi#payloadsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this payload. | |

### Return type

[**PayloadDetailed**](PayloadDetailed.md)

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

