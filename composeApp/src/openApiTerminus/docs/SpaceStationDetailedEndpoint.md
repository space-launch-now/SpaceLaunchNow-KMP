
# SpaceStationDetailedEndpoint

## Properties
| Name | Type | Description | Notes |
| ------------ | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int** |  |  [readonly] |
| **url** | **kotlin.String** |  |  [readonly] |
| **name** | **kotlin.String** |  |  |
| **image** | [**Image**](Image.md) |  |  |
| **status** | [**SpaceStationStatus**](SpaceStationStatus.md) |  |  |
| **founded** | [**kotlinx.datetime.LocalDate**](kotlinx.datetime.LocalDate.md) |  |  |
| **deorbited** | **kotlin.Boolean** |  |  |
| **deorbitedDate** | [**kotlinx.datetime.LocalDate**](kotlinx.datetime.LocalDate.md) |  |  |
| **description** | **kotlin.String** |  |  |
| **orbit** | **kotlin.String** |  |  [readonly] |
| **type** | [**SpaceStationType**](SpaceStationType.md) |  |  |
| **owners** | [**kotlin.collections.List&lt;AgencyNormal&gt;**](AgencyNormal.md) |  |  |
| **responseMode** | **kotlin.String** |  |  [readonly] |
| **activeExpeditions** | [**kotlin.collections.List&lt;ExpeditionMini&gt;**](ExpeditionMini.md) |  |  |
| **programs** | [**kotlin.collections.List&lt;ProgramLink&gt;**](ProgramLink.md) |  |  |
| **dockingLocation** | [**kotlin.collections.List&lt;DockingLocationSerializerForSpacestation&gt;**](DockingLocationSerializerForSpacestation.md) |  |  |
| **activeDockingEvents** | [**kotlin.collections.List&lt;DockingEventForChaserNormal&gt;**](DockingEventForChaserNormal.md) |  |  |
| **lastUpdated** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  [optional] |
| **height** | **kotlin.Double** |  |  [optional] |
| **width** | **kotlin.Double** |  |  [optional] |
| **mass** | **kotlin.Double** |  |  [optional] |
| **volume** | **kotlin.Int** |  |  [optional] |
| **onboardCrew** | **kotlin.Int** |  |  [optional] |
| **dockedVehicles** | **kotlin.Int** |  |  [optional] |



