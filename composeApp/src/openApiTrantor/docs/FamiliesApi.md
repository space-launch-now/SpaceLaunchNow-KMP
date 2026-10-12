# FamiliesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**listFamiliesApiV1FamiliesGet**](FamiliesApi.md#listFamiliesApiV1FamiliesGet) | **GET** /api/v1/families | List launcher-config families |


<a id="listFamiliesApiV1FamiliesGet"></a>
# **listFamiliesApiV1FamiliesGet**
> PaginatedResponseFamilyList listFamiliesApiV1FamiliesGet(search, ordering, limit, offset)

List launcher-config families

Paginated list of rocket families (e.g. Falcon), ordered by name. Filter by name search.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = FamiliesApi()
val search : kotlin.String = search_example // kotlin.String | Search family name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseFamilyList = apiInstance.listFamiliesApiV1FamiliesGet(search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling FamiliesApi#listFamiliesApiV1FamiliesGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling FamiliesApi#listFamiliesApiV1FamiliesGet")
    e.printStackTrace()
}
```

### Parameters
| **search** | **kotlin.String**| Search family name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseFamilyList**](PaginatedResponseFamilyList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

