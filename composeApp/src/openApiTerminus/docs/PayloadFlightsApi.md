# PayloadFlightsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**payloadFlightsList**](PayloadFlightsApi.md#payloadFlightsList) | **GET** /api/v1/payload_flights/ |  |
| [**payloadFlightsRetrieve**](PayloadFlightsApi.md#payloadFlightsRetrieve) | **GET** /api/v1/payload_flights/{id}/ |  |


<a id="payloadFlightsList"></a>
# **payloadFlightsList**
> PaginatedPayloadFlightMiniList payloadFlightsList(limit, offset, ordering, payload, search)



#### Filters Parameters - &#x60;payload&#x60;  Example - [/payload_flights/?payload&#x3D;2](./?payload&#x3D;2)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/payload_flights/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/payload_flights/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = PayloadFlightsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val payload : kotlin.Int = 56 // kotlin.Int | 
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedPayloadFlightMiniList = apiInstance.payloadFlightsList(limit, offset, ordering, payload, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PayloadFlightsApi#payloadFlightsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PayloadFlightsApi#payloadFlightsList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **payload** | **kotlin.Int**|  | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedPayloadFlightMiniList**](PaginatedPayloadFlightMiniList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="payloadFlightsRetrieve"></a>
# **payloadFlightsRetrieve**
> PayloadFlightNormal payloadFlightsRetrieve(id)



#### Filters Parameters - &#x60;payload&#x60;  Example - [/payload_flights/?payload&#x3D;2](./?payload&#x3D;2)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/payload_flights/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/payload_flights/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [staging-terminus.spacelaunchnow.app/docs](https://staging-terminus.spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.terminus.infrastructure.*
//import me.calebjones.spacelaunchnow.api.terminus.models.*

val apiInstance = PayloadFlightsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Payload Flight.
try {
    val result : PayloadFlightNormal = apiInstance.payloadFlightsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling PayloadFlightsApi#payloadFlightsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling PayloadFlightsApi#payloadFlightsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Payload Flight. | |

### Return type

[**PayloadFlightNormal**](PayloadFlightNormal.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

