# ApiThrottleApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiThrottleList**](ApiThrottleApi.md#apiThrottleList) | **GET** /api/ll/2.4.0/api-throttle/ |  |


<a id="apiThrottleList"></a>
# **apiThrottleList**
> kotlin.collections.List&lt;APIThrottle&gt; apiThrottleList()



API endpoint that allows API Throttle information to be viewed.  GET: Returns a range of information about your API access

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = ApiThrottleApi()
try {
    val result : kotlin.collections.List<APIThrottle> = apiInstance.apiThrottleList()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ApiThrottleApi#apiThrottleList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiThrottleApi#apiThrottleList")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;APIThrottle&gt;**](APIThrottle.md)

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

