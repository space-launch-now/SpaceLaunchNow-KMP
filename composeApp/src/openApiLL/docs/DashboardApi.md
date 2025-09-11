# DashboardApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**dashboardStarshipDetailedList**](DashboardApi.md#dashboardStarshipDetailedList) | **GET** /api/ll/2.4.0/dashboard/starship/detailed/ |  |
| [**dashboardStarshipList**](DashboardApi.md#dashboardStarshipList) | **GET** /api/ll/2.4.0/dashboard/starship/ |  |
| [**dashboardStarshipMiniList**](DashboardApi.md#dashboardStarshipMiniList) | **GET** /api/ll/2.4.0/dashboard/starship/mini/ |  |


<a id="dashboardStarshipDetailedList"></a>
# **dashboardStarshipDetailedList**
> kotlin.collections.List&lt;StarshipDashboardDetailed&gt; dashboardStarshipDetailedList()



#### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/dashboard/starship/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/dashboard/starship/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = DashboardApi()
try {
    val result : kotlin.collections.List<StarshipDashboardDetailed> = apiInstance.dashboardStarshipDetailedList()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DashboardApi#dashboardStarshipDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DashboardApi#dashboardStarshipDetailedList")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;StarshipDashboardDetailed&gt;**](StarshipDashboardDetailed.md)

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

<a id="dashboardStarshipList"></a>
# **dashboardStarshipList**
> kotlin.collections.List&lt;StarshipDashboardNormal&gt; dashboardStarshipList()



#### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/dashboard/starship/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/dashboard/starship/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = DashboardApi()
try {
    val result : kotlin.collections.List<StarshipDashboardNormal> = apiInstance.dashboardStarshipList()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DashboardApi#dashboardStarshipList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DashboardApi#dashboardStarshipList")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;StarshipDashboardNormal&gt;**](StarshipDashboardNormal.md)

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

<a id="dashboardStarshipMiniList"></a>
# **dashboardStarshipMiniList**
> kotlin.collections.List&lt;StarshipDashboardList&gt; dashboardStarshipMiniList()



#### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/dashboard/starship/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/dashboard/starship/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = DashboardApi()
try {
    val result : kotlin.collections.List<StarshipDashboardList> = apiInstance.dashboardStarshipMiniList()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DashboardApi#dashboardStarshipMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DashboardApi#dashboardStarshipMiniList")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;StarshipDashboardList&gt;**](StarshipDashboardList.md)

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

