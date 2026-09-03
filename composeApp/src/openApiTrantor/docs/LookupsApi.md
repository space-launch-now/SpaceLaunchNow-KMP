# LookupsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getLookupsApiV1LookupsGet**](LookupsApi.md#getLookupsApiV1LookupsGet) | **GET** /api/v1/lookups | All picker lookups in one payload |


<a id="getLookupsApiV1LookupsGet"></a>
# **getLookupsApiV1LookupsGet**
> LookupsResponse getLookupsApiV1LookupsGet()

All picker lookups in one payload

Every filter-picker list the app needs, in one long-cached response. Replaces five separate /config/_* calls. These tables change roughly never.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LookupsApi()
try {
    val result : LookupsResponse = apiInstance.getLookupsApiV1LookupsGet()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LookupsApi#getLookupsApiV1LookupsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LookupsApi#getLookupsApiV1LookupsGet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**LookupsResponse**](LookupsResponse.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

