# AgenciesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getAgencyApiV1AgenciesAgencyIdGet**](AgenciesApi.md#getAgencyApiV1AgenciesAgencyIdGet) | **GET** /api/v1/agencies/{agency_id} | Get agency detail |
| [**listAgenciesApiV1AgenciesGet**](AgenciesApi.md#listAgenciesApiV1AgenciesGet) | **GET** /api/v1/agencies | List agencies |


<a id="getAgencyApiV1AgenciesAgencyIdGet"></a>
# **getAgencyApiV1AgenciesAgencyIdGet**
> AgencyFull getAgencyApiV1AgenciesAgencyIdGet(agencyId, expand)

Get agency detail

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = AgenciesApi()
val agencyId : kotlin.Int = 56 // kotlin.Int | 
val expand : kotlin.String = expand_example // kotlin.String | Comma-separated list of relations to expand. Dot notation for nested: provider,rocket.stages.launcher
try {
    val result : AgencyFull = apiInstance.getAgencyApiV1AgenciesAgencyIdGet(agencyId, expand)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AgenciesApi#getAgencyApiV1AgenciesAgencyIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AgenciesApi#getAgencyApiV1AgenciesAgencyIdGet")
    e.printStackTrace()
}
```

### Parameters
| **agencyId** | **kotlin.Int**|  | |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **expand** | **kotlin.String**| Comma-separated list of relations to expand. Dot notation for nested: provider,rocket.stages.launcher | [optional] |

### Return type

[**AgencyFull**](AgencyFull.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listAgenciesApiV1AgenciesGet"></a>
# **listAgenciesApiV1AgenciesGet**
> PaginatedResponseAgencyList listAgenciesApiV1AgenciesGet(search, featured, typeIds, countryCode, ordering, limit, offset)

List agencies

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = AgenciesApi()
val search : kotlin.String = search_example // kotlin.String | Search agency name
val featured : kotlin.Boolean = true // kotlin.Boolean | Filter featured agencies
val typeIds : kotlin.String = typeIds_example // kotlin.String | Comma-separated agency type ids
val countryCode : kotlin.String = countryCode_example // kotlin.String | Agency operates from this country (ISO alpha-2 code)
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `name` or `total_launch_count`. Prefix - for descending. Unknown values fall back to name.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseAgencyList = apiInstance.listAgenciesApiV1AgenciesGet(search, featured, typeIds, countryCode, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling AgenciesApi#listAgenciesApiV1AgenciesGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling AgenciesApi#listAgenciesApiV1AgenciesGet")
    e.printStackTrace()
}
```

### Parameters
| **search** | **kotlin.String**| Search agency name | [optional] |
| **featured** | **kotlin.Boolean**| Filter featured agencies | [optional] |
| **typeIds** | **kotlin.String**| Comma-separated agency type ids | [optional] |
| **countryCode** | **kotlin.String**| Agency operates from this country (ISO alpha-2 code) | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;name&#x60; or &#x60;total_launch_count&#x60;. Prefix - for descending. Unknown values fall back to name. | [optional] [default to &quot;name&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseAgencyList**](PaginatedResponseAgencyList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

