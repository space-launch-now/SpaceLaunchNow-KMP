# ProgramsApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**programsDetailedList**](ProgramsApi.md#programsDetailedList) | **GET** /api/ll/2.4.0/programs/detailed/ |  |
| [**programsList**](ProgramsApi.md#programsList) | **GET** /api/ll/2.4.0/programs/ |  |
| [**programsMiniList**](ProgramsApi.md#programsMiniList) | **GET** /api/ll/2.4.0/programs/mini/ |  |
| [**programsRetrieve**](ProgramsApi.md#programsRetrieve) | **GET** /api/ll/2.4.0/programs/{id}/ |  |


<a id="programsDetailedList"></a>
# **programsDetailedList**
> PaginatedProgramNormalList programsDetailedList(limit, offset, ordering, search)



#### Search Fields searched - &#x60;description&#x60;, &#x60;name&#x60;  Example - [/programs/detailed/?search&#x3D;Apollo](./?search&#x3D;Apollo)  #### Ordering Fields - &#x60;end_date&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start_date&#x60;  Example - [/programs/detailed/?ordering&#x3D;-start_date](./?ordering&#x3D;-start_date)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/programs/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/programs/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = ProgramsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedProgramNormalList = apiInstance.programsDetailedList(limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProgramsApi#programsDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProgramsApi#programsDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedProgramNormalList**](PaginatedProgramNormalList.md)

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

<a id="programsList"></a>
# **programsList**
> PaginatedProgramNormalList programsList(limit, offset, ordering, search)



#### Search Fields searched - &#x60;description&#x60;, &#x60;name&#x60;  Example - [/programs/?search&#x3D;Apollo](./?search&#x3D;Apollo)  #### Ordering Fields - &#x60;end_date&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start_date&#x60;  Example - [/programs/?ordering&#x3D;-start_date](./?ordering&#x3D;-start_date)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/programs/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/programs/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = ProgramsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedProgramNormalList = apiInstance.programsList(limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProgramsApi#programsList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProgramsApi#programsList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedProgramNormalList**](PaginatedProgramNormalList.md)

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

<a id="programsMiniList"></a>
# **programsMiniList**
> PaginatedProgramMiniList programsMiniList(limit, offset, ordering, search)



#### Search Fields searched - &#x60;description&#x60;, &#x60;name&#x60;  Example - [/programs/mini/?search&#x3D;Apollo](./?search&#x3D;Apollo)  #### Ordering Fields - &#x60;end_date&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start_date&#x60;  Example - [/programs/mini/?ordering&#x3D;-start_date](./?ordering&#x3D;-start_date)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/programs/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/programs/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = ProgramsApi()
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedProgramMiniList = apiInstance.programsMiniList(limit, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProgramsApi#programsMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProgramsApi#programsMiniList")
    e.printStackTrace()
}
```

### Parameters
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedProgramMiniList**](PaginatedProgramMiniList.md)

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

<a id="programsRetrieve"></a>
# **programsRetrieve**
> ProgramNormal programsRetrieve(id)



#### Search Fields searched - &#x60;description&#x60;, &#x60;name&#x60;  Example - [/programs/?search&#x3D;Apollo](./?search&#x3D;Apollo)  #### Ordering Fields - &#x60;end_date&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;start_date&#x60;  Example - [/programs/?ordering&#x3D;-start_date](./?ordering&#x3D;-start_date)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/programs/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/programs/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = ProgramsApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Program.
try {
    val result : ProgramNormal = apiInstance.programsRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProgramsApi#programsRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProgramsApi#programsRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Program. | |

### Return type

[**ProgramNormal**](ProgramNormal.md)

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

