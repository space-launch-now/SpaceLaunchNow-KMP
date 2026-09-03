# ProgramsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getProgramApiV1ProgramsProgramIdGet**](ProgramsApi.md#getProgramApiV1ProgramsProgramIdGet) | **GET** /api/v1/programs/{program_id} | Get program detail |
| [**listProgramsApiV1ProgramsGet**](ProgramsApi.md#listProgramsApiV1ProgramsGet) | **GET** /api/v1/programs | List programs |


<a id="getProgramApiV1ProgramsProgramIdGet"></a>
# **getProgramApiV1ProgramsProgramIdGet**
> ProgramDetail getProgramApiV1ProgramsProgramIdGet(programId)

Get program detail

Full program payload â€” list row plus description, info_url, and wiki_url.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = ProgramsApi()
val programId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : ProgramDetail = apiInstance.getProgramApiV1ProgramsProgramIdGet(programId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProgramsApi#getProgramApiV1ProgramsProgramIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProgramsApi#getProgramApiV1ProgramsProgramIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **programId** | **kotlin.Int**|  | |

### Return type

[**ProgramDetail**](ProgramDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listProgramsApiV1ProgramsGet"></a>
# **listProgramsApiV1ProgramsGet**
> PaginatedResponseProgramList listProgramsApiV1ProgramsGet(search, ordering, limit, offset)

List programs

Paginated list of programs, ordered by name. Filter by name search.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = ProgramsApi()
val search : kotlin.String = search_example // kotlin.String | Search program name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseProgramList = apiInstance.listProgramsApiV1ProgramsGet(search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProgramsApi#listProgramsApiV1ProgramsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProgramsApi#listProgramsApiV1ProgramsGet")
    e.printStackTrace()
}
```

### Parameters
| **search** | **kotlin.String**| Search program name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseProgramList**](PaginatedResponseProgramList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

