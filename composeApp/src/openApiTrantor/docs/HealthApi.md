# HealthApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**healthzHealthzGet**](HealthApi.md#healthzHealthzGet) | **GET** /healthz | Healthz |
| [**readyzReadyzGet**](HealthApi.md#readyzReadyzGet) | **GET** /readyz | Readyz |


<a id="healthzHealthzGet"></a>
# **healthzHealthzGet**
> kotlin.Any healthzHealthzGet()

Healthz

Liveness probe â€” no I/O.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = HealthApi()
try {
    val result : kotlin.Any = apiInstance.healthzHealthzGet()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling HealthApi#healthzHealthzGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling HealthApi#healthzHealthzGet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="readyzReadyzGet"></a>
# **readyzReadyzGet**
> kotlin.Any readyzReadyzGet()

Readyz

Readiness probe â€” checks DB pool and Redis (if configured).

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = HealthApi()
try {
    val result : kotlin.Any = apiInstance.readyzReadyzGet()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling HealthApi#readyzReadyzGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling HealthApi#readyzReadyzGet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

