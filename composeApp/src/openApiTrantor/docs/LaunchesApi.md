# LaunchesApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getLaunchApiV1LaunchesLaunchIdGet**](LaunchesApi.md#getLaunchApiV1LaunchesLaunchIdGet) | **GET** /api/v1/launches/{launch_id} | Get launch detail |
| [**listLaunchesApiV1LaunchesGet**](LaunchesApi.md#listLaunchesApiV1LaunchesGet) | **GET** /api/v1/launches | List launches |


<a id="getLaunchApiV1LaunchesLaunchIdGet"></a>
# **getLaunchApiV1LaunchesLaunchIdGet**
> LaunchDetail getLaunchApiV1LaunchesLaunchIdGet(launchId)

Get launch detail

Full launch payload â€” provider, rocket (with stages, launcher, landing), mission (with agencies), pad (with location), updates, timeline, info_urls, vid_urls, and mission patches all included.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LaunchesApi()
val launchId : kotlin.String = launchId_example // kotlin.String | 
try {
    val result : LaunchDetail = apiInstance.getLaunchApiV1LaunchesLaunchIdGet(launchId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchesApi#getLaunchApiV1LaunchesLaunchIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchesApi#getLaunchApiV1LaunchesLaunchIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **launchId** | **kotlin.String**|  | |

### Return type

[**LaunchDetail**](LaunchDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listLaunchesApiV1LaunchesGet"></a>
# **listLaunchesApiV1LaunchesGet**
> PaginatedResponseLaunchList listLaunchesApiV1LaunchesGet(upcoming, netAfter, netBefore, netDay, netMonth, statusIds, providerIds, locationIds, programIds, orbitIds, missionTypeIds, familyIds, rocketConfigId, padId, isCrewed, includeSuborbital, search, ordering, limit, offset)

List launches

Paginated list of launches, ordered by NET. Filter by upcoming/previous, NET range or day/month, status, provider, location, program, orbit, mission type, launcher family, rocket configuration, pad, crew, suborbital inclusion, or name search. Multi-value &#x60;*_ids&#x60; params take comma-separated integers.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = LaunchesApi()
val upcoming : kotlin.Boolean = true // kotlin.Boolean | true â†’ NET now or later; false â†’ NET in the past. Pure filter â€” combine with ordering=net for soonest-first.
val netAfter : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | NET at or after this datetime (ISO 8601)
val netBefore : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | NET at or before this datetime (ISO 8601)
val netDay : kotlin.Int = 56 // kotlin.Int | Day of month of NET, evaluated in UTC
val netMonth : kotlin.Int = 56 // kotlin.Int | Month of NET, evaluated in UTC
val statusIds : kotlin.String = statusIds_example // kotlin.String | Comma-separated launch status ids
val providerIds : kotlin.String = providerIds_example // kotlin.String | Comma-separated launch service provider (agency) ids
val locationIds : kotlin.String = locationIds_example // kotlin.String | Comma-separated location ids (matched via the pad)
val programIds : kotlin.String = programIds_example // kotlin.String | Comma-separated program ids
val orbitIds : kotlin.String = orbitIds_example // kotlin.String | Comma-separated orbit ids (matched via the mission)
val missionTypeIds : kotlin.String = missionTypeIds_example // kotlin.String | Comma-separated mission type ids (matched via the mission)
val familyIds : kotlin.String = familyIds_example // kotlin.String | Comma-separated launcher-configuration family ids
val rocketConfigId : kotlin.Int = 56 // kotlin.Int | Launcher configuration id
val padId : kotlin.Int = 56 // kotlin.Int | Launch pad id
val isCrewed : kotlin.Boolean = true // kotlin.Boolean | true â†’ carries human crew; false â†’ no crew, or non-human passengers only
val includeSuborbital : kotlin.Boolean = true // kotlin.Boolean | false â†’ exclude suborbital launches. Omitted or true includes them.
val search : kotlin.String = search_example // kotlin.String | Search launch name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `net` or `name`. Prefix - for descending. Unknown values fall back to -net.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseLaunchList = apiInstance.listLaunchesApiV1LaunchesGet(upcoming, netAfter, netBefore, netDay, netMonth, statusIds, providerIds, locationIds, programIds, orbitIds, missionTypeIds, familyIds, rocketConfigId, padId, isCrewed, includeSuborbital, search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchesApi#listLaunchesApiV1LaunchesGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchesApi#listLaunchesApiV1LaunchesGet")
    e.printStackTrace()
}
```

### Parameters
| **upcoming** | **kotlin.Boolean**| true â†’ NET now or later; false â†’ NET in the past. Pure filter â€” combine with ordering&#x3D;net for soonest-first. | [optional] |
| **netAfter** | **kotlin.time.Instant**| NET at or after this datetime (ISO 8601) | [optional] |
| **netBefore** | **kotlin.time.Instant**| NET at or before this datetime (ISO 8601) | [optional] |
| **netDay** | **kotlin.Int**| Day of month of NET, evaluated in UTC | [optional] |
| **netMonth** | **kotlin.Int**| Month of NET, evaluated in UTC | [optional] |
| **statusIds** | **kotlin.String**| Comma-separated launch status ids | [optional] |
| **providerIds** | **kotlin.String**| Comma-separated launch service provider (agency) ids | [optional] |
| **locationIds** | **kotlin.String**| Comma-separated location ids (matched via the pad) | [optional] |
| **programIds** | **kotlin.String**| Comma-separated program ids | [optional] |
| **orbitIds** | **kotlin.String**| Comma-separated orbit ids (matched via the mission) | [optional] |
| **missionTypeIds** | **kotlin.String**| Comma-separated mission type ids (matched via the mission) | [optional] |
| **familyIds** | **kotlin.String**| Comma-separated launcher-configuration family ids | [optional] |
| **rocketConfigId** | **kotlin.Int**| Launcher configuration id | [optional] |
| **padId** | **kotlin.Int**| Launch pad id | [optional] |
| **isCrewed** | **kotlin.Boolean**| true â†’ carries human crew; false â†’ no crew, or non-human passengers only | [optional] |
| **includeSuborbital** | **kotlin.Boolean**| false â†’ exclude suborbital launches. Omitted or true includes them. | [optional] |
| **search** | **kotlin.String**| Search launch name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;net&#x60; or &#x60;name&#x60;. Prefix - for descending. Unknown values fall back to -net. | [optional] [default to &quot;-net&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseLaunchList**](PaginatedResponseLaunchList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

