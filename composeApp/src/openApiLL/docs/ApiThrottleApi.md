# ApiThrottleApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**apiThrottleStatus**](ApiThrottleApi.md#apiThrottleStatus) | **GET** /api/ll/2.4.0/api-throttle/status/ | Get API throttle status |


<a id="apiThrottleStatus"></a>
# **apiThrottleStatus**
> APIThrottle apiThrottleStatus()

Get API throttle status

Returns throttle information for the current user

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = ApiThrottleApi()
try {
    val result : APIThrottle = apiInstance.apiThrottleStatus()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ApiThrottleApi#apiThrottleStatus")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ApiThrottleApi#apiThrottleStatus")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**APIThrottle**](APIThrottle.md)

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

