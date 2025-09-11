# InfoApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**infoRetrieve**](InfoApi.md#infoRetrieve) | **GET** /v4/info/ |  |


<a id="infoRetrieve"></a>
# **infoRetrieve**
> Info infoRetrieve()



### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.snapi.infrastructure.*
//import me.calebjones.spacelaunchnow.api.snapi.models.*

val apiInstance = InfoApi()
try {
    val result : Info = apiInstance.infoRetrieve()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling InfoApi#infoRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling InfoApi#infoRetrieve")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Info**](Info.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

