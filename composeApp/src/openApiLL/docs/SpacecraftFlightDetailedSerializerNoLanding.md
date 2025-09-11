
# SpacecraftFlightDetailedSerializerNoLanding

## Properties
| Name | Type | Description | Notes |
| ------------ | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int** |  |  [readonly] |
| **url** | **kotlin.String** |  |  [readonly] |
| **destination** | **kotlin.String** |  |  |
| **missionEnd** | [**kotlinx.datetime.Instant**](kotlinx.datetime.Instant.md) |  |  |
| **spacecraft** | [**SpacecraftDetailed**](SpacecraftDetailed.md) |  |  |
| **launch** | [**LaunchNormal**](LaunchNormal.md) |  |  |
| **duration** | **kotlin.String** |  |  [readonly] |
| **turnAroundTime** | **kotlin.String** |  |  [readonly] |
| **responseMode** | **kotlin.String** |  |  [readonly] |
| **launchCrew** | [**kotlin.collections.List&lt;AstronautFlight&gt;**](AstronautFlight.md) |  |  |
| **onboardCrew** | [**kotlin.collections.List&lt;AstronautFlight&gt;**](AstronautFlight.md) |  |  |
| **landingCrew** | [**kotlin.collections.List&lt;AstronautFlight&gt;**](AstronautFlight.md) |  |  |
| **dockingEvents** | [**kotlin.collections.List&lt;DockingEventForChaserNormal&gt;**](DockingEventForChaserNormal.md) |  |  |



