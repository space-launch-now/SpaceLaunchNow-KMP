# LandingsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**landingsDetailedList**](LandingsApi.md#landingsDetailedList) | **GET** /api/ll/2.4.0/landings/detailed/ |  |
| [**landingsList**](LandingsApi.md#landingsList) | **GET** /api/ll/2.4.0/landings/ |  |
| [**landingsMiniList**](LandingsApi.md#landingsMiniList) | **GET** /api/ll/2.4.0/landings/mini/ |  |
| [**landingsRetrieve**](LandingsApi.md#landingsRetrieve) | **GET** /api/ll/2.4.0/landings/{id}/ |  |


<a id="landingsDetailedList"></a>
# **landingsDetailedList**
> PaginatedLandingEndpointDetailedList landingsDetailedList(attempt, firststageLaunchIds, landingLocationIds, landingTypeIds, launcherIds, launcherConfigIds, launcherSerialNumbers, limit, offset, ordering, search, spacecraftIds, spacecraftConfigIds, spacecraftLaunchIds, success)



#### Filters Parameters - &#x60;attempt&#x60;, &#x60;firststage_launch__ids&#x60;, &#x60;landing_location__ids&#x60;, &#x60;landing_type__ids&#x60;, &#x60;launcher__ids&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_serial_numbers&#x60;, &#x60;spacecraft__ids&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;spacecraft_launch__ids&#x60;, &#x60;success&#x60;  Example - [/landings/detailed/?spacecraft__ids&#x3D;39,37](./?spacecraft__ids&#x3D;39,37)  #### Search Fields searched - &#x60;firststage__launcher__launcher_config__name&#x60;, &#x60;firststage__launcher__serial_number&#x60;, &#x60;firststage__rocket__launch__name&#x60;, &#x60;landing_location__abbrev&#x60;, &#x60;landing_location__name&#x60;, &#x60;spacecraftflight__rocket__launch__name&#x60;, &#x60;spacecraftflight__spacecraft__name&#x60;, &#x60;spacecraftflight__spacecraft__serial_number&#x60;, &#x60;spacecraftflight__spacecraft__spacecraft_config__name&#x60;  Example - [/landings/detailed/?search&#x3D;B1059](./?search&#x3D;B1059)  #### Ordering Fields - &#x60;downrange_distance&#x60;, &#x60;id&#x60;  Example - [/landings/detailed/?ordering&#x3D;downrange_distance](./?ordering&#x3D;downrange_distance)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/landings/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/landings/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LandingsApi()
val attempt : kotlin.Boolean = true // kotlin.Boolean | 
val firststageLaunchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val landingLocationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val landingTypeIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherSerialNumbers : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraftIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val spacecraftConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val spacecraftLaunchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val success : kotlin.Boolean = true // kotlin.Boolean | 
try {
    val result : PaginatedLandingEndpointDetailedList = apiInstance.landingsDetailedList(attempt, firststageLaunchIds, landingLocationIds, landingTypeIds, launcherIds, launcherConfigIds, launcherSerialNumbers, limit, offset, ordering, search, spacecraftIds, spacecraftConfigIds, spacecraftLaunchIds, success)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LandingsApi#landingsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LandingsApi#landingsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **attempt** | **kotlin.Boolean**|  | [optional] |
| **firststageLaunchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **landingLocationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **landingTypeIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherSerialNumbers** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraftIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **spacecraftConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **spacecraftLaunchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **success** | **kotlin.Boolean**|  | [optional] |

### Return type

[**PaginatedLandingEndpointDetailedList**](PaginatedLandingEndpointDetailedList.md)

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

<a id="landingsList"></a>
# **landingsList**
> PaginatedLandingEndpointNormalList landingsList(attempt, firststageLaunchIds, landingLocationIds, landingTypeIds, launcherIds, launcherConfigIds, launcherSerialNumbers, limit, offset, ordering, search, spacecraftIds, spacecraftConfigIds, spacecraftLaunchIds, success)



#### Filters Parameters - &#x60;attempt&#x60;, &#x60;firststage_launch__ids&#x60;, &#x60;landing_location__ids&#x60;, &#x60;landing_type__ids&#x60;, &#x60;launcher__ids&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_serial_numbers&#x60;, &#x60;spacecraft__ids&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;spacecraft_launch__ids&#x60;, &#x60;success&#x60;  Example - [/landings/?spacecraft__ids&#x3D;39,37](./?spacecraft__ids&#x3D;39,37)  #### Search Fields searched - &#x60;firststage__launcher__launcher_config__name&#x60;, &#x60;firststage__launcher__serial_number&#x60;, &#x60;firststage__rocket__launch__name&#x60;, &#x60;landing_location__abbrev&#x60;, &#x60;landing_location__name&#x60;, &#x60;spacecraftflight__rocket__launch__name&#x60;, &#x60;spacecraftflight__spacecraft__name&#x60;, &#x60;spacecraftflight__spacecraft__serial_number&#x60;, &#x60;spacecraftflight__spacecraft__spacecraft_config__name&#x60;  Example - [/landings/?search&#x3D;B1059](./?search&#x3D;B1059)  #### Ordering Fields - &#x60;downrange_distance&#x60;, &#x60;id&#x60;  Example - [/landings/?ordering&#x3D;downrange_distance](./?ordering&#x3D;downrange_distance)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/landings/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/landings/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LandingsApi()
val attempt : kotlin.Boolean = true // kotlin.Boolean | 
val firststageLaunchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val landingLocationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val landingTypeIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherSerialNumbers : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraftIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val spacecraftConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val spacecraftLaunchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val success : kotlin.Boolean = true // kotlin.Boolean | 
try {
    val result : PaginatedLandingEndpointNormalList = apiInstance.landingsList(attempt, firststageLaunchIds, landingLocationIds, landingTypeIds, launcherIds, launcherConfigIds, launcherSerialNumbers, limit, offset, ordering, search, spacecraftIds, spacecraftConfigIds, spacecraftLaunchIds, success)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LandingsApi#landingsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LandingsApi#landingsList")
    e.printStackTrace()
}
```

### Parameters
| **attempt** | **kotlin.Boolean**|  | [optional] |
| **firststageLaunchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **landingLocationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **landingTypeIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherSerialNumbers** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraftIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **spacecraftConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **spacecraftLaunchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **success** | **kotlin.Boolean**|  | [optional] |

### Return type

[**PaginatedLandingEndpointNormalList**](PaginatedLandingEndpointNormalList.md)

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

<a id="landingsMiniList"></a>
# **landingsMiniList**
> PaginatedLandingEndpointListList landingsMiniList(attempt, firststageLaunchIds, landingLocationIds, landingTypeIds, launcherIds, launcherConfigIds, launcherSerialNumbers, limit, offset, ordering, search, spacecraftIds, spacecraftConfigIds, spacecraftLaunchIds, success)



#### Filters Parameters - &#x60;attempt&#x60;, &#x60;firststage_launch__ids&#x60;, &#x60;landing_location__ids&#x60;, &#x60;landing_type__ids&#x60;, &#x60;launcher__ids&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_serial_numbers&#x60;, &#x60;spacecraft__ids&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;spacecraft_launch__ids&#x60;, &#x60;success&#x60;  Example - [/landings/mini/?spacecraft__ids&#x3D;39,37](./?spacecraft__ids&#x3D;39,37)  #### Search Fields searched - &#x60;firststage__launcher__launcher_config__name&#x60;, &#x60;firststage__launcher__serial_number&#x60;, &#x60;firststage__rocket__launch__name&#x60;, &#x60;landing_location__abbrev&#x60;, &#x60;landing_location__name&#x60;, &#x60;spacecraftflight__rocket__launch__name&#x60;, &#x60;spacecraftflight__spacecraft__name&#x60;, &#x60;spacecraftflight__spacecraft__serial_number&#x60;, &#x60;spacecraftflight__spacecraft__spacecraft_config__name&#x60;  Example - [/landings/mini/?search&#x3D;B1059](./?search&#x3D;B1059)  #### Ordering Fields - &#x60;downrange_distance&#x60;, &#x60;id&#x60;  Example - [/landings/mini/?ordering&#x3D;downrange_distance](./?ordering&#x3D;downrange_distance)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/landings/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/landings/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LandingsApi()
val attempt : kotlin.Boolean = true // kotlin.Boolean | 
val firststageLaunchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val landingLocationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val landingTypeIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val launcherSerialNumbers : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
val spacecraftIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val spacecraftConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val spacecraftLaunchIds : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val success : kotlin.Boolean = true // kotlin.Boolean | 
try {
    val result : PaginatedLandingEndpointListList = apiInstance.landingsMiniList(attempt, firststageLaunchIds, landingLocationIds, landingTypeIds, launcherIds, launcherConfigIds, launcherSerialNumbers, limit, offset, ordering, search, spacecraftIds, spacecraftConfigIds, spacecraftLaunchIds, success)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LandingsApi#landingsMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LandingsApi#landingsMiniList")
    e.printStackTrace()
}
```

### Parameters
| **attempt** | **kotlin.Boolean**|  | [optional] |
| **firststageLaunchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **landingLocationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **landingTypeIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **launcherSerialNumbers** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **spacecraftIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **spacecraftConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **spacecraftLaunchIds** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **success** | **kotlin.Boolean**|  | [optional] |

### Return type

[**PaginatedLandingEndpointListList**](PaginatedLandingEndpointListList.md)

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

<a id="landingsRetrieve"></a>
# **landingsRetrieve**
> LandingEndpointDetailed landingsRetrieve(id)



#### Filters Parameters - &#x60;attempt&#x60;, &#x60;firststage_launch__ids&#x60;, &#x60;landing_location__ids&#x60;, &#x60;landing_type__ids&#x60;, &#x60;launcher__ids&#x60;, &#x60;launcher_config__ids&#x60;, &#x60;launcher_serial_numbers&#x60;, &#x60;spacecraft__ids&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;spacecraft_launch__ids&#x60;, &#x60;success&#x60;  Example - [/landings/?spacecraft__ids&#x3D;39,37](./?spacecraft__ids&#x3D;39,37)  #### Search Fields searched - &#x60;firststage__launcher__launcher_config__name&#x60;, &#x60;firststage__launcher__serial_number&#x60;, &#x60;firststage__rocket__launch__name&#x60;, &#x60;landing_location__abbrev&#x60;, &#x60;landing_location__name&#x60;, &#x60;spacecraftflight__rocket__launch__name&#x60;, &#x60;spacecraftflight__spacecraft__name&#x60;, &#x60;spacecraftflight__spacecraft__serial_number&#x60;, &#x60;spacecraftflight__spacecraft__spacecraft_config__name&#x60;  Example - [/landings/?search&#x3D;B1059](./?search&#x3D;B1059)  #### Ordering Fields - &#x60;downrange_distance&#x60;, &#x60;id&#x60;  Example - [/landings/?ordering&#x3D;downrange_distance](./?ordering&#x3D;downrange_distance)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/landings/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/landings/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = LandingsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this landing.
try {
    val result : LandingEndpointDetailed = apiInstance.landingsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LandingsApi#landingsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LandingsApi#landingsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this landing. | |

### Return type

[**LandingEndpointDetailed**](LandingEndpointDetailed.md)

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

