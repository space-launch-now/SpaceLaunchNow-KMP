# MissionPatchesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**missionPatchesDetailedList**](MissionPatchesApi.md#missionPatchesDetailedList) | **GET** /api/ll/2.4.0/mission_patches/detailed/ |  |
| [**missionPatchesList**](MissionPatchesApi.md#missionPatchesList) | **GET** /api/ll/2.4.0/mission_patches/ |  |
| [**missionPatchesRetrieve**](MissionPatchesApi.md#missionPatchesRetrieve) | **GET** /api/ll/2.4.0/mission_patches/{id}/ |  |


<a id="missionPatchesDetailedList"></a>
# **missionPatchesDetailedList**
> PaginatedMissionPatchDetailedList missionPatchesDetailedList(agencyId, agencyName, agencyNameContains, id, ids, limit, name, nameContains, offset, ordering, search)



#### Filters Parameters - &#x60;agency__id&#x60;, &#x60;agency__name&#x60;, &#x60;agency__name__contains&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;  Example - [/mission_patches/detailed/?agency__id&#x3D;147](./?agency__id&#x3D;147)  #### Search Fields searched - &#x60;agency__name&#x60;, &#x60;name&#x60;  Example - [/mission_patches/detailed/?search&#x3D;Ariane](./?search&#x3D;Ariane)  #### Ordering Fields - &#x60;agency__name&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;priority&#x60;  Example - [/mission_patches/detailed/?ordering&#x3D;priority](./?ordering&#x3D;priority)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/mission_patches/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/mission_patches/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = MissionPatchesApi()
val agencyId : kotlin.Int = 56 // kotlin.Int | 
val agencyName : kotlin.String = agencyName_example // kotlin.String | 
val agencyNameContains : kotlin.String = agencyNameContains_example // kotlin.String | 
val id : kotlin.Int = 56 // kotlin.Int | 
val ids : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated mission patch IDs.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedMissionPatchDetailedList = apiInstance.missionPatchesDetailedList(agencyId, agencyName, agencyNameContains, id, ids, limit, name, nameContains, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling MissionPatchesApi#missionPatchesDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling MissionPatchesApi#missionPatchesDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **agencyId** | **kotlin.Int**|  | [optional] |
| **agencyName** | **kotlin.String**|  | [optional] |
| **agencyNameContains** | **kotlin.String**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **ids** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated mission patch IDs. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedMissionPatchDetailedList**](PaginatedMissionPatchDetailedList.md)

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

<a id="missionPatchesList"></a>
# **missionPatchesList**
> PaginatedMissionPatchList missionPatchesList(agencyId, agencyName, agencyNameContains, id, ids, limit, name, nameContains, offset, ordering, search)



#### Filters Parameters - &#x60;agency__id&#x60;, &#x60;agency__name&#x60;, &#x60;agency__name__contains&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;  Example - [/mission_patches/?agency__id&#x3D;147](./?agency__id&#x3D;147)  #### Search Fields searched - &#x60;agency__name&#x60;, &#x60;name&#x60;  Example - [/mission_patches/?search&#x3D;Ariane](./?search&#x3D;Ariane)  #### Ordering Fields - &#x60;agency__name&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;priority&#x60;  Example - [/mission_patches/?ordering&#x3D;priority](./?ordering&#x3D;priority)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/mission_patches/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/mission_patches/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = MissionPatchesApi()
val agencyId : kotlin.Int = 56 // kotlin.Int | 
val agencyName : kotlin.String = agencyName_example // kotlin.String | 
val agencyNameContains : kotlin.String = agencyNameContains_example // kotlin.String | 
val id : kotlin.Int = 56 // kotlin.Int | 
val ids : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated mission patch IDs.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val name : kotlin.String = name_example // kotlin.String | 
val nameContains : kotlin.String = nameContains_example // kotlin.String | 
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val search : kotlin.String = search_example // kotlin.String | A search term.
try {
    val result : PaginatedMissionPatchList = apiInstance.missionPatchesList(agencyId, agencyName, agencyNameContains, id, ids, limit, name, nameContains, offset, ordering, search)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling MissionPatchesApi#missionPatchesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling MissionPatchesApi#missionPatchesList")
    e.printStackTrace()
}
```

### Parameters
| **agencyId** | **kotlin.Int**|  | [optional] |
| **agencyName** | **kotlin.String**|  | [optional] |
| **agencyNameContains** | **kotlin.String**|  | [optional] |
| **id** | **kotlin.Int**|  | [optional] |
| **ids** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated mission patch IDs. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **nameContains** | **kotlin.String**|  | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **search** | **kotlin.String**| A search term. | [optional] |

### Return type

[**PaginatedMissionPatchList**](PaginatedMissionPatchList.md)

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

<a id="missionPatchesRetrieve"></a>
# **missionPatchesRetrieve**
> MissionPatchDetailed missionPatchesRetrieve(id)



#### Filters Parameters - &#x60;agency__id&#x60;, &#x60;agency__name&#x60;, &#x60;agency__name__contains&#x60;, &#x60;id&#x60;, &#x60;ids&#x60;, &#x60;name&#x60;, &#x60;name__contains&#x60;  Example - [/mission_patches/?agency__id&#x3D;147](./?agency__id&#x3D;147)  #### Search Fields searched - &#x60;agency__name&#x60;, &#x60;name&#x60;  Example - [/mission_patches/?search&#x3D;Ariane](./?search&#x3D;Ariane)  #### Ordering Fields - &#x60;agency__name&#x60;, &#x60;id&#x60;, &#x60;name&#x60;, &#x60;priority&#x60;  Example - [/mission_patches/?ordering&#x3D;priority](./?ordering&#x3D;priority)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/mission_patches/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/mission_patches/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.infrastructure.*
//import me.calebjones.spacelaunchnow.api.models.*

val apiInstance = MissionPatchesApi()
val id : kotlin.Int = 56 // kotlin.Int | A unique integer value identifying this Mission Patch.
try {
    val result : MissionPatchDetailed = apiInstance.missionPatchesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling MissionPatchesApi#missionPatchesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling MissionPatchesApi#missionPatchesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int**| A unique integer value identifying this Mission Patch. | |

### Return type

[**MissionPatchDetailed**](MissionPatchDetailed.md)

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

