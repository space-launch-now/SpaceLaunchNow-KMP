# LaunchesApi

All URIs are relative to *https://spacelaunchnow.me/api/ll/2.4.0*

| Method | HTTP request | Description |
| ------------- | ------------- | ------------- |
| [**launchesDetailedList**](LaunchesApi.md#launchesDetailedList) | **GET** /api/ll/2.4.0/launches/detailed/ |  |
| [**launchesList**](LaunchesApi.md#launchesList) | **GET** /api/ll/2.4.0/launches/ |  |
| [**launchesMiniList**](LaunchesApi.md#launchesMiniList) | **GET** /api/ll/2.4.0/launches/mini/ |  |
| [**launchesRetrieve**](LaunchesApi.md#launchesRetrieve) | **GET** /api/ll/2.4.0/launches/{id}/ |  |


<a id="launchesDetailedList"></a>
# **launchesDetailedList**
> PaginatedLaunchDetailedList launchesDetailedList(agencyLaunchAttemptCount, agencyLaunchAttemptCountGt, agencyLaunchAttemptCountGte, agencyLaunchAttemptCountLt, agencyLaunchAttemptCountLte, agencyLaunchAttemptCountYear, agencyLaunchAttemptCountYearGt, agencyLaunchAttemptCountYearGte, agencyLaunchAttemptCountYearLt, agencyLaunchAttemptCountYearLte, id, includeSuborbital, isCrewed, lastUpdatedGte, lastUpdatedLte, launchDesignator, launcherConfigId, limit, locationIds, locationLaunchAttemptCount, locationLaunchAttemptCountGt, locationLaunchAttemptCountGte, locationLaunchAttemptCountLt, locationLaunchAttemptCountLte, locationLaunchAttemptCountYear, locationLaunchAttemptCountYearGt, locationLaunchAttemptCountYearGte, locationLaunchAttemptCountYearLt, locationLaunchAttemptCountYearLte, lspId, lspName, missionOrbitCelestialBodyId, missionOrbitName, missionOrbitNameIcontains, name, netDay, netGt, netGte, netLt, netLte, netMonth, netYear, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, orbitalLaunchAttemptCountYear, orbitalLaunchAttemptCountYearGt, orbitalLaunchAttemptCountYearGte, orbitalLaunchAttemptCountYearLt, orbitalLaunchAttemptCountYearLte, ordering, pad, padLocation, padLocationCelestialBodyId, padLaunchAttemptCount, padLaunchAttemptCountGt, padLaunchAttemptCountGte, padLaunchAttemptCountLt, padLaunchAttemptCountLte, padLaunchAttemptCountYear, padLaunchAttemptCountYearGt, padLaunchAttemptCountYearGte, padLaunchAttemptCountYearLt, padLaunchAttemptCountYearLte, previous, program, relatedLspId, relatedLspName, rocketConfigurationFullName, rocketConfigurationFullNameIcontains, rocketConfigurationId, rocketConfigurationManufacturerName, rocketConfigurationManufacturerNameIcontains, rocketConfigurationName, rocketSpacecraftflightSpacecraftId, rocketSpacecraftflightSpacecraftName, rocketSpacecraftflightSpacecraftNameIcontains, search, serialNumber, slug, spacecraftConfigIds, status, statusIds, upcoming, upcomingWithRecent, videoUrl, windowEndGt, windowEndGte, windowEndLt, windowEndLte, windowStartGt, windowStartGte, windowStartLt, windowStartLte)



#### Filters Parameters - &#x60;agency_launch_attempt_count&#x60;, &#x60;agency_launch_attempt_count__gt&#x60;, &#x60;agency_launch_attempt_count__gte&#x60;, &#x60;agency_launch_attempt_count__lt&#x60;, &#x60;agency_launch_attempt_count__lte&#x60;, &#x60;agency_launch_attempt_count_year&#x60;, &#x60;agency_launch_attempt_count_year__gt&#x60;, &#x60;agency_launch_attempt_count_year__gte&#x60;, &#x60;agency_launch_attempt_count_year__lt&#x60;, &#x60;agency_launch_attempt_count_year__lte&#x60;, &#x60;id&#x60;, &#x60;include_suborbital&#x60;, &#x60;is_crewed&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;launch_designator&#x60;, &#x60;launcher_config__id&#x60;, &#x60;location__ids&#x60;, &#x60;location_launch_attempt_count&#x60;, &#x60;location_launch_attempt_count__gt&#x60;, &#x60;location_launch_attempt_count__gte&#x60;, &#x60;location_launch_attempt_count__lt&#x60;, &#x60;location_launch_attempt_count__lte&#x60;, &#x60;location_launch_attempt_count_year&#x60;, &#x60;location_launch_attempt_count_year__gt&#x60;, &#x60;location_launch_attempt_count_year__gte&#x60;, &#x60;location_launch_attempt_count_year__lt&#x60;, &#x60;location_launch_attempt_count_year__lte&#x60;, &#x60;lsp__id&#x60;, &#x60;lsp__name&#x60;, &#x60;mission__orbit__celestial_body__id&#x60;, &#x60;mission__orbit__name&#x60;, &#x60;mission__orbit__name__icontains&#x60;, &#x60;name&#x60;, &#x60;net__day&#x60;, &#x60;net__gt&#x60;, &#x60;net__gte&#x60;, &#x60;net__lt&#x60;, &#x60;net__lte&#x60;, &#x60;net__month&#x60;, &#x60;net__year&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;orbital_launch_attempt_count__gt&#x60;, &#x60;orbital_launch_attempt_count__gte&#x60;, &#x60;orbital_launch_attempt_count__lt&#x60;, &#x60;orbital_launch_attempt_count__lte&#x60;, &#x60;orbital_launch_attempt_count_year&#x60;, &#x60;orbital_launch_attempt_count_year__gt&#x60;, &#x60;orbital_launch_attempt_count_year__gte&#x60;, &#x60;orbital_launch_attempt_count_year__lt&#x60;, &#x60;orbital_launch_attempt_count_year__lte&#x60;, &#x60;pad&#x60;, &#x60;pad__location&#x60;, &#x60;pad__location__celestial_body__id&#x60;, &#x60;pad_launch_attempt_count&#x60;, &#x60;pad_launch_attempt_count__gt&#x60;, &#x60;pad_launch_attempt_count__gte&#x60;, &#x60;pad_launch_attempt_count__lt&#x60;, &#x60;pad_launch_attempt_count__lte&#x60;, &#x60;pad_launch_attempt_count_year&#x60;, &#x60;pad_launch_attempt_count_year__gt&#x60;, &#x60;pad_launch_attempt_count_year__gte&#x60;, &#x60;pad_launch_attempt_count_year__lt&#x60;, &#x60;pad_launch_attempt_count_year__lte&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;related_lsp__id&#x60;, &#x60;related_lsp__name&#x60;, &#x60;rocket__configuration__full_name&#x60;, &#x60;rocket__configuration__full_name__icontains&#x60;, &#x60;rocket__configuration__id&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__manufacturer__name__icontains&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__id&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name__icontains&#x60;, &#x60;serial_number&#x60;, &#x60;slug&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;window_end__gt&#x60;, &#x60;window_end__gte&#x60;, &#x60;window_end__lt&#x60;, &#x60;window_end__lte&#x60;, &#x60;window_start__gt&#x60;, &#x60;window_start__gte&#x60;, &#x60;window_start__lt&#x60;, &#x60;window_start__lte&#x60;  Example - [/launches/detailed/?pad__location&#x3D;13](./?pad__location&#x3D;13)  #### Search Fields searched - &#x60;launch_designator&#x60;, &#x60;launch_service_provider__name&#x60;, &#x60;mission__name&#x60;, &#x60;name&#x60;, &#x60;pad__location__name&#x60;, &#x60;pad__name&#x60;, &#x60;rocket__configuration__manufacturer__abbrev&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;  Example - [/launches/detailed/?search&#x3D;Starlink](./?search&#x3D;Starlink)  #### Ordering Fields - &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;name&#x60;, &#x60;net&#x60;  Example - [/launches/detailed/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launches/detailed/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launches/detailed/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchesApi()
val agencyLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val includeSuborbital : kotlin.Boolean = true // kotlin.Boolean | 
val isCrewed : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is less than or equal to
val launchDesignator : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated (COSPAR) international launch designators.
val launcherConfigId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val locationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated location IDs.
val locationLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val lspId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launch service provider (agency) IDs.
val lspName : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated launch service provider names.
val missionOrbitCelestialBodyId : kotlin.Int = 56 // kotlin.Int | 
val missionOrbitName : kotlin.String = missionOrbitName_example // kotlin.String | 
val missionOrbitNameIcontains : kotlin.String = missionOrbitNameIcontains_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val netDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val netGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is greater than
val netGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is greater than or equal to
val netLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is less than
val netLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is less than or equal to
val netMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val netYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val orbitalLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val pad : kotlin.Int = 56 // kotlin.Int | 
val padLocation : kotlin.Int = 56 // kotlin.Int | 
val padLocationCelestialBodyId : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val previous : kotlin.Boolean = true // kotlin.Boolean | Filter for launches that have already occurred (up to current time).
val program : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val relatedLspId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs related to the launch service provider.
val relatedLspName : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated agency names related to the launch service provider.
val rocketConfigurationFullName : kotlin.String = rocketConfigurationFullName_example // kotlin.String | 
val rocketConfigurationFullNameIcontains : kotlin.String = rocketConfigurationFullNameIcontains_example // kotlin.String | 
val rocketConfigurationId : kotlin.Int = 56 // kotlin.Int | 
val rocketConfigurationManufacturerName : kotlin.String = rocketConfigurationManufacturerName_example // kotlin.String | 
val rocketConfigurationManufacturerNameIcontains : kotlin.String = rocketConfigurationManufacturerNameIcontains_example // kotlin.String | 
val rocketConfigurationName : kotlin.String = rocketConfigurationName_example // kotlin.String | 
val rocketSpacecraftflightSpacecraftId : kotlin.Int = 56 // kotlin.Int | 
val rocketSpacecraftflightSpacecraftName : kotlin.String = rocketSpacecraftflightSpacecraftName_example // kotlin.String | 
val rocketSpacecraftflightSpacecraftNameIcontains : kotlin.String = rocketSpacecraftflightSpacecraftNameIcontains_example // kotlin.String | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val serialNumber : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated first stage booster serial numbers.
val slug : kotlin.String = slug_example // kotlin.String | 
val spacecraftConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val status : kotlin.Int = 56 // kotlin.Int | 
val statusIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val upcoming : kotlin.Boolean = true // kotlin.Boolean | Filter for launches upcoming within the next period (up to current time).
val upcomingWithRecent : kotlin.Boolean = true // kotlin.Boolean | Filter for launches upcoming within the next period (from 1 day ago onwards).
val videoUrl : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated video URLs.
val windowEndGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is greater than
val windowEndGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is greater than or equal to
val windowEndLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is less than
val windowEndLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is less than or equal to
val windowStartGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is greater than
val windowStartGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is greater than or equal to
val windowStartLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is less than
val windowStartLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is less than or equal to
try {
    val result : PaginatedLaunchDetailedList = apiInstance.launchesDetailedList(agencyLaunchAttemptCount, agencyLaunchAttemptCountGt, agencyLaunchAttemptCountGte, agencyLaunchAttemptCountLt, agencyLaunchAttemptCountLte, agencyLaunchAttemptCountYear, agencyLaunchAttemptCountYearGt, agencyLaunchAttemptCountYearGte, agencyLaunchAttemptCountYearLt, agencyLaunchAttemptCountYearLte, id, includeSuborbital, isCrewed, lastUpdatedGte, lastUpdatedLte, launchDesignator, launcherConfigId, limit, locationIds, locationLaunchAttemptCount, locationLaunchAttemptCountGt, locationLaunchAttemptCountGte, locationLaunchAttemptCountLt, locationLaunchAttemptCountLte, locationLaunchAttemptCountYear, locationLaunchAttemptCountYearGt, locationLaunchAttemptCountYearGte, locationLaunchAttemptCountYearLt, locationLaunchAttemptCountYearLte, lspId, lspName, missionOrbitCelestialBodyId, missionOrbitName, missionOrbitNameIcontains, name, netDay, netGt, netGte, netLt, netLte, netMonth, netYear, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, orbitalLaunchAttemptCountYear, orbitalLaunchAttemptCountYearGt, orbitalLaunchAttemptCountYearGte, orbitalLaunchAttemptCountYearLt, orbitalLaunchAttemptCountYearLte, ordering, pad, padLocation, padLocationCelestialBodyId, padLaunchAttemptCount, padLaunchAttemptCountGt, padLaunchAttemptCountGte, padLaunchAttemptCountLt, padLaunchAttemptCountLte, padLaunchAttemptCountYear, padLaunchAttemptCountYearGt, padLaunchAttemptCountYearGte, padLaunchAttemptCountYearLt, padLaunchAttemptCountYearLte, previous, program, relatedLspId, relatedLspName, rocketConfigurationFullName, rocketConfigurationFullNameIcontains, rocketConfigurationId, rocketConfigurationManufacturerName, rocketConfigurationManufacturerNameIcontains, rocketConfigurationName, rocketSpacecraftflightSpacecraftId, rocketSpacecraftflightSpacecraftName, rocketSpacecraftflightSpacecraftNameIcontains, search, serialNumber, slug, spacecraftConfigIds, status, statusIds, upcoming, upcomingWithRecent, videoUrl, windowEndGt, windowEndGte, windowEndLt, windowEndLte, windowStartGt, windowStartGte, windowStartLt, windowStartLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchesApi#launchesDetailedList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchesApi#launchesDetailedList")
    e.printStackTrace()
}
```

### Parameters
| **agencyLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **id** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **includeSuborbital** | **kotlin.Boolean**|  | [optional] |
| **isCrewed** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlinx.datetime.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlinx.datetime.Instant**| Last Update is less than or equal to | [optional] |
| **launchDesignator** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated (COSPAR) international launch designators. | [optional] |
| **launcherConfigId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **locationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated location IDs. | [optional] |
| **locationLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **lspId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launch service provider (agency) IDs. | [optional] |
| **lspName** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated launch service provider names. | [optional] |
| **missionOrbitCelestialBodyId** | **kotlin.Int**|  | [optional] |
| **missionOrbitName** | **kotlin.String**|  | [optional] |
| **missionOrbitNameIcontains** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **netDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **netGt** | **kotlinx.datetime.Instant**| NET is greater than | [optional] |
| **netGte** | **kotlinx.datetime.Instant**| NET is greater than or equal to | [optional] |
| **netLt** | **kotlinx.datetime.Instant**| NET is less than | [optional] |
| **netLte** | **kotlinx.datetime.Instant**| NET is less than or equal to | [optional] |
| **netMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **netYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **orbitalLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **pad** | **kotlin.Int**|  | [optional] |
| **padLocation** | **kotlin.Int**|  | [optional] |
| **padLocationCelestialBodyId** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **previous** | **kotlin.Boolean**| Filter for launches that have already occurred (up to current time). | [optional] |
| **program** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **relatedLspId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs related to the launch service provider. | [optional] |
| **relatedLspName** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated agency names related to the launch service provider. | [optional] |
| **rocketConfigurationFullName** | **kotlin.String**|  | [optional] |
| **rocketConfigurationFullNameIcontains** | **kotlin.String**|  | [optional] |
| **rocketConfigurationId** | **kotlin.Int**|  | [optional] |
| **rocketConfigurationManufacturerName** | **kotlin.String**|  | [optional] |
| **rocketConfigurationManufacturerNameIcontains** | **kotlin.String**|  | [optional] |
| **rocketConfigurationName** | **kotlin.String**|  | [optional] |
| **rocketSpacecraftflightSpacecraftId** | **kotlin.Int**|  | [optional] |
| **rocketSpacecraftflightSpacecraftName** | **kotlin.String**|  | [optional] |
| **rocketSpacecraftflightSpacecraftNameIcontains** | **kotlin.String**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **serialNumber** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated first stage booster serial numbers. | [optional] |
| **slug** | **kotlin.String**|  | [optional] |
| **spacecraftConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **upcoming** | **kotlin.Boolean**| Filter for launches upcoming within the next period (up to current time). | [optional] |
| **upcomingWithRecent** | **kotlin.Boolean**| Filter for launches upcoming within the next period (from 1 day ago onwards). | [optional] |
| **videoUrl** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated video URLs. | [optional] |
| **windowEndGt** | **kotlinx.datetime.Instant**| Window End is greater than | [optional] |
| **windowEndGte** | **kotlinx.datetime.Instant**| Window End is greater than or equal to | [optional] |
| **windowEndLt** | **kotlinx.datetime.Instant**| Window End is less than | [optional] |
| **windowEndLte** | **kotlinx.datetime.Instant**| Window End is less than or equal to | [optional] |
| **windowStartGt** | **kotlinx.datetime.Instant**| Window Start is greater than | [optional] |
| **windowStartGte** | **kotlinx.datetime.Instant**| Window Start is greater than or equal to | [optional] |
| **windowStartLt** | **kotlinx.datetime.Instant**| Window Start is less than | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **windowStartLte** | **kotlinx.datetime.Instant**| Window Start is less than or equal to | [optional] |

### Return type

[**PaginatedLaunchDetailedList**](PaginatedLaunchDetailedList.md)

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

<a id="launchesList"></a>
# **launchesList**
> PaginatedLaunchNormalList launchesList(agencyLaunchAttemptCount, agencyLaunchAttemptCountGt, agencyLaunchAttemptCountGte, agencyLaunchAttemptCountLt, agencyLaunchAttemptCountLte, agencyLaunchAttemptCountYear, agencyLaunchAttemptCountYearGt, agencyLaunchAttemptCountYearGte, agencyLaunchAttemptCountYearLt, agencyLaunchAttemptCountYearLte, id, includeSuborbital, isCrewed, lastUpdatedGte, lastUpdatedLte, launchDesignator, launcherConfigId, limit, locationIds, locationLaunchAttemptCount, locationLaunchAttemptCountGt, locationLaunchAttemptCountGte, locationLaunchAttemptCountLt, locationLaunchAttemptCountLte, locationLaunchAttemptCountYear, locationLaunchAttemptCountYearGt, locationLaunchAttemptCountYearGte, locationLaunchAttemptCountYearLt, locationLaunchAttemptCountYearLte, lspId, lspName, missionOrbitCelestialBodyId, missionOrbitName, missionOrbitNameIcontains, name, netDay, netGt, netGte, netLt, netLte, netMonth, netYear, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, orbitalLaunchAttemptCountYear, orbitalLaunchAttemptCountYearGt, orbitalLaunchAttemptCountYearGte, orbitalLaunchAttemptCountYearLt, orbitalLaunchAttemptCountYearLte, ordering, pad, padLocation, padLocationCelestialBodyId, padLaunchAttemptCount, padLaunchAttemptCountGt, padLaunchAttemptCountGte, padLaunchAttemptCountLt, padLaunchAttemptCountLte, padLaunchAttemptCountYear, padLaunchAttemptCountYearGt, padLaunchAttemptCountYearGte, padLaunchAttemptCountYearLt, padLaunchAttemptCountYearLte, previous, program, relatedLspId, relatedLspName, rocketConfigurationFullName, rocketConfigurationFullNameIcontains, rocketConfigurationId, rocketConfigurationManufacturerName, rocketConfigurationManufacturerNameIcontains, rocketConfigurationName, rocketSpacecraftflightSpacecraftId, rocketSpacecraftflightSpacecraftName, rocketSpacecraftflightSpacecraftNameIcontains, search, serialNumber, slug, spacecraftConfigIds, status, statusIds, upcoming, upcomingWithRecent, videoUrl, windowEndGt, windowEndGte, windowEndLt, windowEndLte, windowStartGt, windowStartGte, windowStartLt, windowStartLte)



#### Filters Parameters - &#x60;agency_launch_attempt_count&#x60;, &#x60;agency_launch_attempt_count__gt&#x60;, &#x60;agency_launch_attempt_count__gte&#x60;, &#x60;agency_launch_attempt_count__lt&#x60;, &#x60;agency_launch_attempt_count__lte&#x60;, &#x60;agency_launch_attempt_count_year&#x60;, &#x60;agency_launch_attempt_count_year__gt&#x60;, &#x60;agency_launch_attempt_count_year__gte&#x60;, &#x60;agency_launch_attempt_count_year__lt&#x60;, &#x60;agency_launch_attempt_count_year__lte&#x60;, &#x60;id&#x60;, &#x60;include_suborbital&#x60;, &#x60;is_crewed&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;launch_designator&#x60;, &#x60;launcher_config__id&#x60;, &#x60;location__ids&#x60;, &#x60;location_launch_attempt_count&#x60;, &#x60;location_launch_attempt_count__gt&#x60;, &#x60;location_launch_attempt_count__gte&#x60;, &#x60;location_launch_attempt_count__lt&#x60;, &#x60;location_launch_attempt_count__lte&#x60;, &#x60;location_launch_attempt_count_year&#x60;, &#x60;location_launch_attempt_count_year__gt&#x60;, &#x60;location_launch_attempt_count_year__gte&#x60;, &#x60;location_launch_attempt_count_year__lt&#x60;, &#x60;location_launch_attempt_count_year__lte&#x60;, &#x60;lsp__id&#x60;, &#x60;lsp__name&#x60;, &#x60;mission__orbit__celestial_body__id&#x60;, &#x60;mission__orbit__name&#x60;, &#x60;mission__orbit__name__icontains&#x60;, &#x60;name&#x60;, &#x60;net__day&#x60;, &#x60;net__gt&#x60;, &#x60;net__gte&#x60;, &#x60;net__lt&#x60;, &#x60;net__lte&#x60;, &#x60;net__month&#x60;, &#x60;net__year&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;orbital_launch_attempt_count__gt&#x60;, &#x60;orbital_launch_attempt_count__gte&#x60;, &#x60;orbital_launch_attempt_count__lt&#x60;, &#x60;orbital_launch_attempt_count__lte&#x60;, &#x60;orbital_launch_attempt_count_year&#x60;, &#x60;orbital_launch_attempt_count_year__gt&#x60;, &#x60;orbital_launch_attempt_count_year__gte&#x60;, &#x60;orbital_launch_attempt_count_year__lt&#x60;, &#x60;orbital_launch_attempt_count_year__lte&#x60;, &#x60;pad&#x60;, &#x60;pad__location&#x60;, &#x60;pad__location__celestial_body__id&#x60;, &#x60;pad_launch_attempt_count&#x60;, &#x60;pad_launch_attempt_count__gt&#x60;, &#x60;pad_launch_attempt_count__gte&#x60;, &#x60;pad_launch_attempt_count__lt&#x60;, &#x60;pad_launch_attempt_count__lte&#x60;, &#x60;pad_launch_attempt_count_year&#x60;, &#x60;pad_launch_attempt_count_year__gt&#x60;, &#x60;pad_launch_attempt_count_year__gte&#x60;, &#x60;pad_launch_attempt_count_year__lt&#x60;, &#x60;pad_launch_attempt_count_year__lte&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;related_lsp__id&#x60;, &#x60;related_lsp__name&#x60;, &#x60;rocket__configuration__full_name&#x60;, &#x60;rocket__configuration__full_name__icontains&#x60;, &#x60;rocket__configuration__id&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__manufacturer__name__icontains&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__id&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name__icontains&#x60;, &#x60;serial_number&#x60;, &#x60;slug&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;window_end__gt&#x60;, &#x60;window_end__gte&#x60;, &#x60;window_end__lt&#x60;, &#x60;window_end__lte&#x60;, &#x60;window_start__gt&#x60;, &#x60;window_start__gte&#x60;, &#x60;window_start__lt&#x60;, &#x60;window_start__lte&#x60;  Example - [/launches/?pad__location&#x3D;13](./?pad__location&#x3D;13)  #### Search Fields searched - &#x60;launch_designator&#x60;, &#x60;launch_service_provider__name&#x60;, &#x60;mission__name&#x60;, &#x60;name&#x60;, &#x60;pad__location__name&#x60;, &#x60;pad__name&#x60;, &#x60;rocket__configuration__manufacturer__abbrev&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;  Example - [/launches/?search&#x3D;Starlink](./?search&#x3D;Starlink)  #### Ordering Fields - &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;name&#x60;, &#x60;net&#x60;  Example - [/launches/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launches/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launches/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchesApi()
val agencyLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val includeSuborbital : kotlin.Boolean = true // kotlin.Boolean | 
val isCrewed : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is less than or equal to
val launchDesignator : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated (COSPAR) international launch designators.
val launcherConfigId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val locationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated location IDs.
val locationLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val lspId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launch service provider (agency) IDs.
val lspName : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated launch service provider names.
val missionOrbitCelestialBodyId : kotlin.Int = 56 // kotlin.Int | 
val missionOrbitName : kotlin.String = missionOrbitName_example // kotlin.String | 
val missionOrbitNameIcontains : kotlin.String = missionOrbitNameIcontains_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val netDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val netGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is greater than
val netGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is greater than or equal to
val netLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is less than
val netLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is less than or equal to
val netMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val netYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val orbitalLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val pad : kotlin.Int = 56 // kotlin.Int | 
val padLocation : kotlin.Int = 56 // kotlin.Int | 
val padLocationCelestialBodyId : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val previous : kotlin.Boolean = true // kotlin.Boolean | Filter for launches that have already occurred (up to current time).
val program : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val relatedLspId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs related to the launch service provider.
val relatedLspName : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated agency names related to the launch service provider.
val rocketConfigurationFullName : kotlin.String = rocketConfigurationFullName_example // kotlin.String | 
val rocketConfigurationFullNameIcontains : kotlin.String = rocketConfigurationFullNameIcontains_example // kotlin.String | 
val rocketConfigurationId : kotlin.Int = 56 // kotlin.Int | 
val rocketConfigurationManufacturerName : kotlin.String = rocketConfigurationManufacturerName_example // kotlin.String | 
val rocketConfigurationManufacturerNameIcontains : kotlin.String = rocketConfigurationManufacturerNameIcontains_example // kotlin.String | 
val rocketConfigurationName : kotlin.String = rocketConfigurationName_example // kotlin.String | 
val rocketSpacecraftflightSpacecraftId : kotlin.Int = 56 // kotlin.Int | 
val rocketSpacecraftflightSpacecraftName : kotlin.String = rocketSpacecraftflightSpacecraftName_example // kotlin.String | 
val rocketSpacecraftflightSpacecraftNameIcontains : kotlin.String = rocketSpacecraftflightSpacecraftNameIcontains_example // kotlin.String | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val serialNumber : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated first stage booster serial numbers.
val slug : kotlin.String = slug_example // kotlin.String | 
val spacecraftConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val status : kotlin.Int = 56 // kotlin.Int | 
val statusIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val upcoming : kotlin.Boolean = true // kotlin.Boolean | Filter for launches upcoming within the next period (up to current time).
val upcomingWithRecent : kotlin.Boolean = true // kotlin.Boolean | Filter for launches upcoming within the next period (from 1 day ago onwards).
val videoUrl : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated video URLs.
val windowEndGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is greater than
val windowEndGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is greater than or equal to
val windowEndLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is less than
val windowEndLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is less than or equal to
val windowStartGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is greater than
val windowStartGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is greater than or equal to
val windowStartLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is less than
val windowStartLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is less than or equal to
try {
    val result : PaginatedLaunchNormalList = apiInstance.launchesList(agencyLaunchAttemptCount, agencyLaunchAttemptCountGt, agencyLaunchAttemptCountGte, agencyLaunchAttemptCountLt, agencyLaunchAttemptCountLte, agencyLaunchAttemptCountYear, agencyLaunchAttemptCountYearGt, agencyLaunchAttemptCountYearGte, agencyLaunchAttemptCountYearLt, agencyLaunchAttemptCountYearLte, id, includeSuborbital, isCrewed, lastUpdatedGte, lastUpdatedLte, launchDesignator, launcherConfigId, limit, locationIds, locationLaunchAttemptCount, locationLaunchAttemptCountGt, locationLaunchAttemptCountGte, locationLaunchAttemptCountLt, locationLaunchAttemptCountLte, locationLaunchAttemptCountYear, locationLaunchAttemptCountYearGt, locationLaunchAttemptCountYearGte, locationLaunchAttemptCountYearLt, locationLaunchAttemptCountYearLte, lspId, lspName, missionOrbitCelestialBodyId, missionOrbitName, missionOrbitNameIcontains, name, netDay, netGt, netGte, netLt, netLte, netMonth, netYear, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, orbitalLaunchAttemptCountYear, orbitalLaunchAttemptCountYearGt, orbitalLaunchAttemptCountYearGte, orbitalLaunchAttemptCountYearLt, orbitalLaunchAttemptCountYearLte, ordering, pad, padLocation, padLocationCelestialBodyId, padLaunchAttemptCount, padLaunchAttemptCountGt, padLaunchAttemptCountGte, padLaunchAttemptCountLt, padLaunchAttemptCountLte, padLaunchAttemptCountYear, padLaunchAttemptCountYearGt, padLaunchAttemptCountYearGte, padLaunchAttemptCountYearLt, padLaunchAttemptCountYearLte, previous, program, relatedLspId, relatedLspName, rocketConfigurationFullName, rocketConfigurationFullNameIcontains, rocketConfigurationId, rocketConfigurationManufacturerName, rocketConfigurationManufacturerNameIcontains, rocketConfigurationName, rocketSpacecraftflightSpacecraftId, rocketSpacecraftflightSpacecraftName, rocketSpacecraftflightSpacecraftNameIcontains, search, serialNumber, slug, spacecraftConfigIds, status, statusIds, upcoming, upcomingWithRecent, videoUrl, windowEndGt, windowEndGte, windowEndLt, windowEndLte, windowStartGt, windowStartGte, windowStartLt, windowStartLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchesApi#launchesList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchesApi#launchesList")
    e.printStackTrace()
}
```

### Parameters
| **agencyLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **id** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **includeSuborbital** | **kotlin.Boolean**|  | [optional] |
| **isCrewed** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlinx.datetime.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlinx.datetime.Instant**| Last Update is less than or equal to | [optional] |
| **launchDesignator** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated (COSPAR) international launch designators. | [optional] |
| **launcherConfigId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **locationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated location IDs. | [optional] |
| **locationLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **lspId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launch service provider (agency) IDs. | [optional] |
| **lspName** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated launch service provider names. | [optional] |
| **missionOrbitCelestialBodyId** | **kotlin.Int**|  | [optional] |
| **missionOrbitName** | **kotlin.String**|  | [optional] |
| **missionOrbitNameIcontains** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **netDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **netGt** | **kotlinx.datetime.Instant**| NET is greater than | [optional] |
| **netGte** | **kotlinx.datetime.Instant**| NET is greater than or equal to | [optional] |
| **netLt** | **kotlinx.datetime.Instant**| NET is less than | [optional] |
| **netLte** | **kotlinx.datetime.Instant**| NET is less than or equal to | [optional] |
| **netMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **netYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **orbitalLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **pad** | **kotlin.Int**|  | [optional] |
| **padLocation** | **kotlin.Int**|  | [optional] |
| **padLocationCelestialBodyId** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **previous** | **kotlin.Boolean**| Filter for launches that have already occurred (up to current time). | [optional] |
| **program** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **relatedLspId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs related to the launch service provider. | [optional] |
| **relatedLspName** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated agency names related to the launch service provider. | [optional] |
| **rocketConfigurationFullName** | **kotlin.String**|  | [optional] |
| **rocketConfigurationFullNameIcontains** | **kotlin.String**|  | [optional] |
| **rocketConfigurationId** | **kotlin.Int**|  | [optional] |
| **rocketConfigurationManufacturerName** | **kotlin.String**|  | [optional] |
| **rocketConfigurationManufacturerNameIcontains** | **kotlin.String**|  | [optional] |
| **rocketConfigurationName** | **kotlin.String**|  | [optional] |
| **rocketSpacecraftflightSpacecraftId** | **kotlin.Int**|  | [optional] |
| **rocketSpacecraftflightSpacecraftName** | **kotlin.String**|  | [optional] |
| **rocketSpacecraftflightSpacecraftNameIcontains** | **kotlin.String**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **serialNumber** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated first stage booster serial numbers. | [optional] |
| **slug** | **kotlin.String**|  | [optional] |
| **spacecraftConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **upcoming** | **kotlin.Boolean**| Filter for launches upcoming within the next period (up to current time). | [optional] |
| **upcomingWithRecent** | **kotlin.Boolean**| Filter for launches upcoming within the next period (from 1 day ago onwards). | [optional] |
| **videoUrl** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated video URLs. | [optional] |
| **windowEndGt** | **kotlinx.datetime.Instant**| Window End is greater than | [optional] |
| **windowEndGte** | **kotlinx.datetime.Instant**| Window End is greater than or equal to | [optional] |
| **windowEndLt** | **kotlinx.datetime.Instant**| Window End is less than | [optional] |
| **windowEndLte** | **kotlinx.datetime.Instant**| Window End is less than or equal to | [optional] |
| **windowStartGt** | **kotlinx.datetime.Instant**| Window Start is greater than | [optional] |
| **windowStartGte** | **kotlinx.datetime.Instant**| Window Start is greater than or equal to | [optional] |
| **windowStartLt** | **kotlinx.datetime.Instant**| Window Start is less than | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **windowStartLte** | **kotlinx.datetime.Instant**| Window Start is less than or equal to | [optional] |

### Return type

[**PaginatedLaunchNormalList**](PaginatedLaunchNormalList.md)

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

<a id="launchesMiniList"></a>
# **launchesMiniList**
> PaginatedLaunchBasicList launchesMiniList(agencyLaunchAttemptCount, agencyLaunchAttemptCountGt, agencyLaunchAttemptCountGte, agencyLaunchAttemptCountLt, agencyLaunchAttemptCountLte, agencyLaunchAttemptCountYear, agencyLaunchAttemptCountYearGt, agencyLaunchAttemptCountYearGte, agencyLaunchAttemptCountYearLt, agencyLaunchAttemptCountYearLte, id, includeSuborbital, isCrewed, lastUpdatedGte, lastUpdatedLte, launchDesignator, launcherConfigId, limit, locationIds, locationLaunchAttemptCount, locationLaunchAttemptCountGt, locationLaunchAttemptCountGte, locationLaunchAttemptCountLt, locationLaunchAttemptCountLte, locationLaunchAttemptCountYear, locationLaunchAttemptCountYearGt, locationLaunchAttemptCountYearGte, locationLaunchAttemptCountYearLt, locationLaunchAttemptCountYearLte, lspId, lspName, missionOrbitCelestialBodyId, missionOrbitName, missionOrbitNameIcontains, name, netDay, netGt, netGte, netLt, netLte, netMonth, netYear, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, orbitalLaunchAttemptCountYear, orbitalLaunchAttemptCountYearGt, orbitalLaunchAttemptCountYearGte, orbitalLaunchAttemptCountYearLt, orbitalLaunchAttemptCountYearLte, ordering, pad, padLocation, padLocationCelestialBodyId, padLaunchAttemptCount, padLaunchAttemptCountGt, padLaunchAttemptCountGte, padLaunchAttemptCountLt, padLaunchAttemptCountLte, padLaunchAttemptCountYear, padLaunchAttemptCountYearGt, padLaunchAttemptCountYearGte, padLaunchAttemptCountYearLt, padLaunchAttemptCountYearLte, previous, program, relatedLspId, relatedLspName, rocketConfigurationFullName, rocketConfigurationFullNameIcontains, rocketConfigurationId, rocketConfigurationManufacturerName, rocketConfigurationManufacturerNameIcontains, rocketConfigurationName, rocketSpacecraftflightSpacecraftId, rocketSpacecraftflightSpacecraftName, rocketSpacecraftflightSpacecraftNameIcontains, search, serialNumber, slug, spacecraftConfigIds, status, statusIds, upcoming, upcomingWithRecent, videoUrl, windowEndGt, windowEndGte, windowEndLt, windowEndLte, windowStartGt, windowStartGte, windowStartLt, windowStartLte)



#### Filters Parameters - &#x60;agency_launch_attempt_count&#x60;, &#x60;agency_launch_attempt_count__gt&#x60;, &#x60;agency_launch_attempt_count__gte&#x60;, &#x60;agency_launch_attempt_count__lt&#x60;, &#x60;agency_launch_attempt_count__lte&#x60;, &#x60;agency_launch_attempt_count_year&#x60;, &#x60;agency_launch_attempt_count_year__gt&#x60;, &#x60;agency_launch_attempt_count_year__gte&#x60;, &#x60;agency_launch_attempt_count_year__lt&#x60;, &#x60;agency_launch_attempt_count_year__lte&#x60;, &#x60;id&#x60;, &#x60;include_suborbital&#x60;, &#x60;is_crewed&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;launch_designator&#x60;, &#x60;launcher_config__id&#x60;, &#x60;location__ids&#x60;, &#x60;location_launch_attempt_count&#x60;, &#x60;location_launch_attempt_count__gt&#x60;, &#x60;location_launch_attempt_count__gte&#x60;, &#x60;location_launch_attempt_count__lt&#x60;, &#x60;location_launch_attempt_count__lte&#x60;, &#x60;location_launch_attempt_count_year&#x60;, &#x60;location_launch_attempt_count_year__gt&#x60;, &#x60;location_launch_attempt_count_year__gte&#x60;, &#x60;location_launch_attempt_count_year__lt&#x60;, &#x60;location_launch_attempt_count_year__lte&#x60;, &#x60;lsp__id&#x60;, &#x60;lsp__name&#x60;, &#x60;mission__orbit__celestial_body__id&#x60;, &#x60;mission__orbit__name&#x60;, &#x60;mission__orbit__name__icontains&#x60;, &#x60;name&#x60;, &#x60;net__day&#x60;, &#x60;net__gt&#x60;, &#x60;net__gte&#x60;, &#x60;net__lt&#x60;, &#x60;net__lte&#x60;, &#x60;net__month&#x60;, &#x60;net__year&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;orbital_launch_attempt_count__gt&#x60;, &#x60;orbital_launch_attempt_count__gte&#x60;, &#x60;orbital_launch_attempt_count__lt&#x60;, &#x60;orbital_launch_attempt_count__lte&#x60;, &#x60;orbital_launch_attempt_count_year&#x60;, &#x60;orbital_launch_attempt_count_year__gt&#x60;, &#x60;orbital_launch_attempt_count_year__gte&#x60;, &#x60;orbital_launch_attempt_count_year__lt&#x60;, &#x60;orbital_launch_attempt_count_year__lte&#x60;, &#x60;pad&#x60;, &#x60;pad__location&#x60;, &#x60;pad__location__celestial_body__id&#x60;, &#x60;pad_launch_attempt_count&#x60;, &#x60;pad_launch_attempt_count__gt&#x60;, &#x60;pad_launch_attempt_count__gte&#x60;, &#x60;pad_launch_attempt_count__lt&#x60;, &#x60;pad_launch_attempt_count__lte&#x60;, &#x60;pad_launch_attempt_count_year&#x60;, &#x60;pad_launch_attempt_count_year__gt&#x60;, &#x60;pad_launch_attempt_count_year__gte&#x60;, &#x60;pad_launch_attempt_count_year__lt&#x60;, &#x60;pad_launch_attempt_count_year__lte&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;related_lsp__id&#x60;, &#x60;related_lsp__name&#x60;, &#x60;rocket__configuration__full_name&#x60;, &#x60;rocket__configuration__full_name__icontains&#x60;, &#x60;rocket__configuration__id&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__manufacturer__name__icontains&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__id&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name__icontains&#x60;, &#x60;serial_number&#x60;, &#x60;slug&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;window_end__gt&#x60;, &#x60;window_end__gte&#x60;, &#x60;window_end__lt&#x60;, &#x60;window_end__lte&#x60;, &#x60;window_start__gt&#x60;, &#x60;window_start__gte&#x60;, &#x60;window_start__lt&#x60;, &#x60;window_start__lte&#x60;  Example - [/launches/mini/?pad__location&#x3D;13](./?pad__location&#x3D;13)  #### Search Fields searched - &#x60;launch_designator&#x60;, &#x60;launch_service_provider__name&#x60;, &#x60;mission__name&#x60;, &#x60;name&#x60;, &#x60;pad__location__name&#x60;, &#x60;pad__name&#x60;, &#x60;rocket__configuration__manufacturer__abbrev&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;  Example - [/launches/mini/?search&#x3D;Starlink](./?search&#x3D;Starlink)  #### Ordering Fields - &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;name&#x60;, &#x60;net&#x60;  Example - [/launches/mini/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launches/mini/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launches/mini/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchesApi()
val agencyLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val agencyLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val id : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Multiple values may be separated by commas.
val includeSuborbital : kotlin.Boolean = true // kotlin.Boolean | 
val isCrewed : kotlin.Boolean = true // kotlin.Boolean | 
val lastUpdatedGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is greater than or equal to
val lastUpdatedLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Last Update is less than or equal to
val launchDesignator : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated (COSPAR) international launch designators.
val launcherConfigId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val limit : kotlin.Int = 56 // kotlin.Int | Number of results to return per page.
val locationIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated location IDs.
val locationLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val locationLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val lspId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated launch service provider (agency) IDs.
val lspName : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated launch service provider names.
val missionOrbitCelestialBodyId : kotlin.Int = 56 // kotlin.Int | 
val missionOrbitName : kotlin.String = missionOrbitName_example // kotlin.String | 
val missionOrbitNameIcontains : kotlin.String = missionOrbitNameIcontains_example // kotlin.String | 
val name : kotlin.String = name_example // kotlin.String | 
val netDay : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val netGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is greater than
val netGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is greater than or equal to
val netLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is less than
val netLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | NET is less than or equal to
val netMonth : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val netYear : kotlin.collections.List<kotlin.Double> =  // kotlin.collections.List<kotlin.Double> | Multiple values may be separated by commas.
val offset : kotlin.Int = 56 // kotlin.Int | The initial index from which to return the results.
val orbitalLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val orbitalLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val ordering : kotlin.String = ordering_example // kotlin.String | Which field to use when ordering the results.
val pad : kotlin.Int = 56 // kotlin.Int | 
val padLocation : kotlin.Int = 56 // kotlin.Int | 
val padLocationCelestialBodyId : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCount : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountGt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountGte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountLt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountLte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYear : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearGt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearGte : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearLt : kotlin.Int = 56 // kotlin.Int | 
val padLaunchAttemptCountYearLte : kotlin.Int = 56 // kotlin.Int | 
val previous : kotlin.Boolean = true // kotlin.Boolean | Filter for launches that have already occurred (up to current time).
val program : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | 
val relatedLspId : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Comma-separated agency IDs related to the launch service provider.
val relatedLspName : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated agency names related to the launch service provider.
val rocketConfigurationFullName : kotlin.String = rocketConfigurationFullName_example // kotlin.String | 
val rocketConfigurationFullNameIcontains : kotlin.String = rocketConfigurationFullNameIcontains_example // kotlin.String | 
val rocketConfigurationId : kotlin.Int = 56 // kotlin.Int | 
val rocketConfigurationManufacturerName : kotlin.String = rocketConfigurationManufacturerName_example // kotlin.String | 
val rocketConfigurationManufacturerNameIcontains : kotlin.String = rocketConfigurationManufacturerNameIcontains_example // kotlin.String | 
val rocketConfigurationName : kotlin.String = rocketConfigurationName_example // kotlin.String | 
val rocketSpacecraftflightSpacecraftId : kotlin.Int = 56 // kotlin.Int | 
val rocketSpacecraftflightSpacecraftName : kotlin.String = rocketSpacecraftflightSpacecraftName_example // kotlin.String | 
val rocketSpacecraftflightSpacecraftNameIcontains : kotlin.String = rocketSpacecraftflightSpacecraftNameIcontains_example // kotlin.String | 
val search : kotlin.String = search_example // kotlin.String | A search term.
val serialNumber : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated first stage booster serial numbers.
val slug : kotlin.String = slug_example // kotlin.String | 
val spacecraftConfigIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val status : kotlin.Int = 56 // kotlin.Int | 
val statusIds : kotlin.collections.List<kotlin.Int> =  // kotlin.collections.List<kotlin.Int> | Multiple values may be separated by commas.
val upcoming : kotlin.Boolean = true // kotlin.Boolean | Filter for launches upcoming within the next period (up to current time).
val upcomingWithRecent : kotlin.Boolean = true // kotlin.Boolean | Filter for launches upcoming within the next period (from 1 day ago onwards).
val videoUrl : kotlin.collections.List<kotlin.String> =  // kotlin.collections.List<kotlin.String> | Comma-separated video URLs.
val windowEndGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is greater than
val windowEndGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is greater than or equal to
val windowEndLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is less than
val windowEndLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window End is less than or equal to
val windowStartGt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is greater than
val windowStartGte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is greater than or equal to
val windowStartLt : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is less than
val windowStartLte : kotlinx.datetime.Instant = 2013-10-20T19:20:30+01:00 // kotlinx.datetime.Instant | Window Start is less than or equal to
try {
    val result : PaginatedLaunchBasicList = apiInstance.launchesMiniList(agencyLaunchAttemptCount, agencyLaunchAttemptCountGt, agencyLaunchAttemptCountGte, agencyLaunchAttemptCountLt, agencyLaunchAttemptCountLte, agencyLaunchAttemptCountYear, agencyLaunchAttemptCountYearGt, agencyLaunchAttemptCountYearGte, agencyLaunchAttemptCountYearLt, agencyLaunchAttemptCountYearLte, id, includeSuborbital, isCrewed, lastUpdatedGte, lastUpdatedLte, launchDesignator, launcherConfigId, limit, locationIds, locationLaunchAttemptCount, locationLaunchAttemptCountGt, locationLaunchAttemptCountGte, locationLaunchAttemptCountLt, locationLaunchAttemptCountLte, locationLaunchAttemptCountYear, locationLaunchAttemptCountYearGt, locationLaunchAttemptCountYearGte, locationLaunchAttemptCountYearLt, locationLaunchAttemptCountYearLte, lspId, lspName, missionOrbitCelestialBodyId, missionOrbitName, missionOrbitNameIcontains, name, netDay, netGt, netGte, netLt, netLte, netMonth, netYear, offset, orbitalLaunchAttemptCount, orbitalLaunchAttemptCountGt, orbitalLaunchAttemptCountGte, orbitalLaunchAttemptCountLt, orbitalLaunchAttemptCountLte, orbitalLaunchAttemptCountYear, orbitalLaunchAttemptCountYearGt, orbitalLaunchAttemptCountYearGte, orbitalLaunchAttemptCountYearLt, orbitalLaunchAttemptCountYearLte, ordering, pad, padLocation, padLocationCelestialBodyId, padLaunchAttemptCount, padLaunchAttemptCountGt, padLaunchAttemptCountGte, padLaunchAttemptCountLt, padLaunchAttemptCountLte, padLaunchAttemptCountYear, padLaunchAttemptCountYearGt, padLaunchAttemptCountYearGte, padLaunchAttemptCountYearLt, padLaunchAttemptCountYearLte, previous, program, relatedLspId, relatedLspName, rocketConfigurationFullName, rocketConfigurationFullNameIcontains, rocketConfigurationId, rocketConfigurationManufacturerName, rocketConfigurationManufacturerNameIcontains, rocketConfigurationName, rocketSpacecraftflightSpacecraftId, rocketSpacecraftflightSpacecraftName, rocketSpacecraftflightSpacecraftNameIcontains, search, serialNumber, slug, spacecraftConfigIds, status, statusIds, upcoming, upcomingWithRecent, videoUrl, windowEndGt, windowEndGte, windowEndLt, windowEndLte, windowStartGt, windowStartGte, windowStartLt, windowStartLte)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchesApi#launchesMiniList")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchesApi#launchesMiniList")
    e.printStackTrace()
}
```

### Parameters
| **agencyLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **agencyLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **id** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Multiple values may be separated by commas. | [optional] |
| **includeSuborbital** | **kotlin.Boolean**|  | [optional] |
| **isCrewed** | **kotlin.Boolean**|  | [optional] |
| **lastUpdatedGte** | **kotlinx.datetime.Instant**| Last Update is greater than or equal to | [optional] |
| **lastUpdatedLte** | **kotlinx.datetime.Instant**| Last Update is less than or equal to | [optional] |
| **launchDesignator** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated (COSPAR) international launch designators. | [optional] |
| **launcherConfigId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **limit** | **kotlin.Int**| Number of results to return per page. | [optional] |
| **locationIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated location IDs. | [optional] |
| **locationLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **locationLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **lspId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated launch service provider (agency) IDs. | [optional] |
| **lspName** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated launch service provider names. | [optional] |
| **missionOrbitCelestialBodyId** | **kotlin.Int**|  | [optional] |
| **missionOrbitName** | **kotlin.String**|  | [optional] |
| **missionOrbitNameIcontains** | **kotlin.String**|  | [optional] |
| **name** | **kotlin.String**|  | [optional] |
| **netDay** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **netGt** | **kotlinx.datetime.Instant**| NET is greater than | [optional] |
| **netGte** | **kotlinx.datetime.Instant**| NET is greater than or equal to | [optional] |
| **netLt** | **kotlinx.datetime.Instant**| NET is less than | [optional] |
| **netLte** | **kotlinx.datetime.Instant**| NET is less than or equal to | [optional] |
| **netMonth** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **netYear** | [**kotlin.collections.List&lt;kotlin.Double&gt;**](kotlin.Double.md)| Multiple values may be separated by commas. | [optional] |
| **offset** | **kotlin.Int**| The initial index from which to return the results. | [optional] |
| **orbitalLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **orbitalLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **ordering** | **kotlin.String**| Which field to use when ordering the results. | [optional] |
| **pad** | **kotlin.Int**|  | [optional] |
| **padLocation** | **kotlin.Int**|  | [optional] |
| **padLocationCelestialBodyId** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCount** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountGt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountGte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountLt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountLte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYear** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearGt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearGte** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearLt** | **kotlin.Int**|  | [optional] |
| **padLaunchAttemptCountYearLte** | **kotlin.Int**|  | [optional] |
| **previous** | **kotlin.Boolean**| Filter for launches that have already occurred (up to current time). | [optional] |
| **program** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)|  | [optional] |
| **relatedLspId** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Comma-separated agency IDs related to the launch service provider. | [optional] |
| **relatedLspName** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated agency names related to the launch service provider. | [optional] |
| **rocketConfigurationFullName** | **kotlin.String**|  | [optional] |
| **rocketConfigurationFullNameIcontains** | **kotlin.String**|  | [optional] |
| **rocketConfigurationId** | **kotlin.Int**|  | [optional] |
| **rocketConfigurationManufacturerName** | **kotlin.String**|  | [optional] |
| **rocketConfigurationManufacturerNameIcontains** | **kotlin.String**|  | [optional] |
| **rocketConfigurationName** | **kotlin.String**|  | [optional] |
| **rocketSpacecraftflightSpacecraftId** | **kotlin.Int**|  | [optional] |
| **rocketSpacecraftflightSpacecraftName** | **kotlin.String**|  | [optional] |
| **rocketSpacecraftflightSpacecraftNameIcontains** | **kotlin.String**|  | [optional] |
| **search** | **kotlin.String**| A search term. | [optional] |
| **serialNumber** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated first stage booster serial numbers. | [optional] |
| **slug** | **kotlin.String**|  | [optional] |
| **spacecraftConfigIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **status** | **kotlin.Int**|  | [optional] |
| **statusIds** | [**kotlin.collections.List&lt;kotlin.Int&gt;**](kotlin.Int.md)| Multiple values may be separated by commas. | [optional] |
| **upcoming** | **kotlin.Boolean**| Filter for launches upcoming within the next period (up to current time). | [optional] |
| **upcomingWithRecent** | **kotlin.Boolean**| Filter for launches upcoming within the next period (from 1 day ago onwards). | [optional] |
| **videoUrl** | [**kotlin.collections.List&lt;kotlin.String&gt;**](kotlin.String.md)| Comma-separated video URLs. | [optional] |
| **windowEndGt** | **kotlinx.datetime.Instant**| Window End is greater than | [optional] |
| **windowEndGte** | **kotlinx.datetime.Instant**| Window End is greater than or equal to | [optional] |
| **windowEndLt** | **kotlinx.datetime.Instant**| Window End is less than | [optional] |
| **windowEndLte** | **kotlinx.datetime.Instant**| Window End is less than or equal to | [optional] |
| **windowStartGt** | **kotlinx.datetime.Instant**| Window Start is greater than | [optional] |
| **windowStartGte** | **kotlinx.datetime.Instant**| Window Start is greater than or equal to | [optional] |
| **windowStartLt** | **kotlinx.datetime.Instant**| Window Start is less than | [optional] |
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **windowStartLte** | **kotlinx.datetime.Instant**| Window Start is less than or equal to | [optional] |

### Return type

[**PaginatedLaunchBasicList**](PaginatedLaunchBasicList.md)

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

<a id="launchesRetrieve"></a>
# **launchesRetrieve**
> LaunchDetailed launchesRetrieve(id)



#### Filters Parameters - &#x60;agency_launch_attempt_count&#x60;, &#x60;agency_launch_attempt_count__gt&#x60;, &#x60;agency_launch_attempt_count__gte&#x60;, &#x60;agency_launch_attempt_count__lt&#x60;, &#x60;agency_launch_attempt_count__lte&#x60;, &#x60;agency_launch_attempt_count_year&#x60;, &#x60;agency_launch_attempt_count_year__gt&#x60;, &#x60;agency_launch_attempt_count_year__gte&#x60;, &#x60;agency_launch_attempt_count_year__lt&#x60;, &#x60;agency_launch_attempt_count_year__lte&#x60;, &#x60;id&#x60;, &#x60;include_suborbital&#x60;, &#x60;is_crewed&#x60;, &#x60;last_updated__gte&#x60;, &#x60;last_updated__lte&#x60;, &#x60;launch_designator&#x60;, &#x60;launcher_config__id&#x60;, &#x60;location__ids&#x60;, &#x60;location_launch_attempt_count&#x60;, &#x60;location_launch_attempt_count__gt&#x60;, &#x60;location_launch_attempt_count__gte&#x60;, &#x60;location_launch_attempt_count__lt&#x60;, &#x60;location_launch_attempt_count__lte&#x60;, &#x60;location_launch_attempt_count_year&#x60;, &#x60;location_launch_attempt_count_year__gt&#x60;, &#x60;location_launch_attempt_count_year__gte&#x60;, &#x60;location_launch_attempt_count_year__lt&#x60;, &#x60;location_launch_attempt_count_year__lte&#x60;, &#x60;lsp__id&#x60;, &#x60;lsp__name&#x60;, &#x60;mission__orbit__celestial_body__id&#x60;, &#x60;mission__orbit__name&#x60;, &#x60;mission__orbit__name__icontains&#x60;, &#x60;name&#x60;, &#x60;net__day&#x60;, &#x60;net__gt&#x60;, &#x60;net__gte&#x60;, &#x60;net__lt&#x60;, &#x60;net__lte&#x60;, &#x60;net__month&#x60;, &#x60;net__year&#x60;, &#x60;orbital_launch_attempt_count&#x60;, &#x60;orbital_launch_attempt_count__gt&#x60;, &#x60;orbital_launch_attempt_count__gte&#x60;, &#x60;orbital_launch_attempt_count__lt&#x60;, &#x60;orbital_launch_attempt_count__lte&#x60;, &#x60;orbital_launch_attempt_count_year&#x60;, &#x60;orbital_launch_attempt_count_year__gt&#x60;, &#x60;orbital_launch_attempt_count_year__gte&#x60;, &#x60;orbital_launch_attempt_count_year__lt&#x60;, &#x60;orbital_launch_attempt_count_year__lte&#x60;, &#x60;pad&#x60;, &#x60;pad__location&#x60;, &#x60;pad__location__celestial_body__id&#x60;, &#x60;pad_launch_attempt_count&#x60;, &#x60;pad_launch_attempt_count__gt&#x60;, &#x60;pad_launch_attempt_count__gte&#x60;, &#x60;pad_launch_attempt_count__lt&#x60;, &#x60;pad_launch_attempt_count__lte&#x60;, &#x60;pad_launch_attempt_count_year&#x60;, &#x60;pad_launch_attempt_count_year__gt&#x60;, &#x60;pad_launch_attempt_count_year__gte&#x60;, &#x60;pad_launch_attempt_count_year__lt&#x60;, &#x60;pad_launch_attempt_count_year__lte&#x60;, &#x60;previous&#x60;, &#x60;program&#x60;, &#x60;related_lsp__id&#x60;, &#x60;related_lsp__name&#x60;, &#x60;rocket__configuration__full_name&#x60;, &#x60;rocket__configuration__full_name__icontains&#x60;, &#x60;rocket__configuration__id&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__manufacturer__name__icontains&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__id&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name__icontains&#x60;, &#x60;serial_number&#x60;, &#x60;slug&#x60;, &#x60;spacecraft_config__ids&#x60;, &#x60;status&#x60;, &#x60;status__ids&#x60;, &#x60;upcoming&#x60;, &#x60;upcoming_with_recent&#x60;, &#x60;video_url&#x60;, &#x60;window_end__gt&#x60;, &#x60;window_end__gte&#x60;, &#x60;window_end__lt&#x60;, &#x60;window_end__lte&#x60;, &#x60;window_start__gt&#x60;, &#x60;window_start__gte&#x60;, &#x60;window_start__lt&#x60;, &#x60;window_start__lte&#x60;  Example - [/launches/?pad__location&#x3D;13](./?pad__location&#x3D;13)  #### Search Fields searched - &#x60;launch_designator&#x60;, &#x60;launch_service_provider__name&#x60;, &#x60;mission__name&#x60;, &#x60;name&#x60;, &#x60;pad__location__name&#x60;, &#x60;pad__name&#x60;, &#x60;rocket__configuration__manufacturer__abbrev&#x60;, &#x60;rocket__configuration__manufacturer__name&#x60;, &#x60;rocket__configuration__name&#x60;, &#x60;rocket__spacecraftflight__spacecraft__name&#x60;  Example - [/launches/?search&#x3D;Starlink](./?search&#x3D;Starlink)  #### Ordering Fields - &#x60;id&#x60;, &#x60;last_updated&#x60;, &#x60;name&#x60;, &#x60;net&#x60;  Example - [/launches/?ordering&#x3D;-last_updated](./?ordering&#x3D;-last_updated)  #### Number of results Use &#x60;limit&#x60; to control the number of objects in the response (max 100)  Example - [/launches/?limit&#x3D;2](./?limit&#x3D;2)  #### Format Switch to JSON output - [/launches/?format&#x3D;json](./?format&#x3D;json)  #### Help Find all the FAQs and support links on the documentation homepage - [spacelaunchnow.app/docs](https://spacelaunchnow.app/docs/)

### Example
```kotlin
// Import classes:
//import me.calebjones.spacelaunchnow.api.launchlibrary.infrastructure.*
//import me.calebjones.spacelaunchnow.api.launchlibrary.models.*

val apiInstance = LaunchesApi()
val id : kotlin.String = 38400000-8cf0-11bd-b23e-10b96e4ef00d // kotlin.String | A UUID string identifying this Launch.
try {
    val result : LaunchDetailed = apiInstance.launchesRetrieve(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling LaunchesApi#launchesRetrieve")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling LaunchesApi#launchesRetrieve")
    e.printStackTrace()
}
```

### Parameters
| Name | Type | Description  | Notes |
| ------------- | ------------- | ------------- | ------------- |
| **id** | **kotlin.String**| A UUID string identifying this Launch. | |

### Return type

[**LaunchDetailed**](LaunchDetailed.md)

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

