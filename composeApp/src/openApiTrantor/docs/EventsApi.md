# EventsApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**getEventApiV1EventsEventIdGet**](EventsApi.md#getEventApiV1EventsEventIdGet) | **GET** /api/v1/events/{event_id} | Get event detail |
| [**listEventsApiV1EventsGet**](EventsApi.md#listEventsApiV1EventsGet) | **GET** /api/v1/events | List events |


<a id="getEventApiV1EventsEventIdGet"></a>
# **getEventApiV1EventsEventIdGet**
> EventDetail getEventApiV1EventsEventIdGet(eventId)

Get event detail

Full event payload â€” description, info_urls/vid_urls (ordered by priority), related launch ids, and program ids.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = EventsApi()
val eventId : kotlin.Int = 56 // kotlin.Int | 
try {
    val result : EventDetail = apiInstance.getEventApiV1EventsEventIdGet(eventId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventsApi#getEventApiV1EventsEventIdGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventsApi#getEventApiV1EventsEventIdGet")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **eventId** | **kotlin.Int**|  | |

### Return type

[**EventDetail**](EventDetail.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="listEventsApiV1EventsGet"></a>
# **listEventsApiV1EventsGet**
> PaginatedResponseEventList listEventsApiV1EventsGet(upcoming, dateAfter, dateBefore, typeIds, programIds, launchId, search, ordering, limit, offset)

List events

Paginated list of events, ordered by date. Filter by upcoming/previous, date range, type, program, related launch, or name search.

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.trantor.infrastructure.*
//import me.calebjones.spacelaunchnow.api.trantor.models.*

val apiInstance = EventsApi()
val upcoming : kotlin.Boolean = true // kotlin.Boolean | true â†’ date now or later; false â†’ date in the past. Pure filter â€” combine with ordering=date for soonest-first.
val dateAfter : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Date at or after this datetime (ISO 8601)
val dateBefore : kotlin.time.Instant = 2013-10-20T19:20:30+01:00 // kotlin.time.Instant | Date at or before this datetime (ISO 8601)
val typeIds : kotlin.String = typeIds_example // kotlin.String | Comma-separated event type ids
val programIds : kotlin.String = programIds_example // kotlin.String | Comma-separated program ids
val launchId : kotlin.String = launchId_example // kotlin.String | Launch id â€” related events for a launch
val search : kotlin.String = search_example // kotlin.String | Search event name
val ordering : kotlin.String = ordering_example // kotlin.String | Order by `date`. Prefix - for descending. Unknown values fall back to -date.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results per page
val offset : kotlin.Int = 56 // kotlin.Int | Number of results to skip
try {
    val result : PaginatedResponseEventList = apiInstance.listEventsApiV1EventsGet(upcoming, dateAfter, dateBefore, typeIds, programIds, launchId, search, ordering, limit, offset)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling EventsApi#listEventsApiV1EventsGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling EventsApi#listEventsApiV1EventsGet")
    e.printStackTrace()
}
```

### Parameters
| **upcoming** | **kotlin.Boolean**| true â†’ date now or later; false â†’ date in the past. Pure filter â€” combine with ordering&#x3D;date for soonest-first. | [optional] |
| **dateAfter** | **kotlin.time.Instant**| Date at or after this datetime (ISO 8601) | [optional] |
| **dateBefore** | **kotlin.time.Instant**| Date at or before this datetime (ISO 8601) | [optional] |
| **typeIds** | **kotlin.String**| Comma-separated event type ids | [optional] |
| **programIds** | **kotlin.String**| Comma-separated program ids | [optional] |
| **launchId** | **kotlin.String**| Launch id â€” related events for a launch | [optional] |
| **search** | **kotlin.String**| Search event name | [optional] |
| **ordering** | **kotlin.String**| Order by &#x60;date&#x60;. Prefix - for descending. Unknown values fall back to -date. | [optional] [default to &quot;-date&quot;] |
| **limit** | **kotlin.Int**| Number of results per page | [optional] [default to 25] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **offset** | **kotlin.Int**| Number of results to skip | [optional] [default to 0] |

### Return type

[**PaginatedResponseEventList**](PaginatedResponseEventList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

