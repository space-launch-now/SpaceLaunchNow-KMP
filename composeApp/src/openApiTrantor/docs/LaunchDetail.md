
# LaunchDetail

## Properties
| Name | Type | Description | Notes |
| ------------ | ------------- | ------------- | ------------- |
| **id** | **kotlin.String** |  |  |
| **name** | **kotlin.String** |  |  |
| **slug** | **kotlin.String** |  |  |
| **status** | **kotlin.String** |  |  |
| **statusId** | **kotlin.Int** |  |  |
| **net** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  |
| **netPrecision** | **kotlin.String** |  |  [optional] |
| **windowStart** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  [optional] |
| **windowEnd** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  [optional] |
| **imageUrl** | **kotlin.String** |  |  [optional] |
| **providerId** | **kotlin.Int** |  |  [optional] |
| **providerName** | **kotlin.String** |  |  [optional] |
| **rocketId** | **kotlin.Int** |  |  [optional] |
| **missionId** | **kotlin.Int** |  |  [optional] |
| **padId** | **kotlin.Int** |  |  [optional] |
| **probability** | **kotlin.Int** |  |  [optional] |
| **weatherConcerns** | **kotlin.String** |  |  [optional] |
| **failreason** | **kotlin.String** |  |  [optional] |
| **webcastLive** | **kotlin.Boolean** |  |  [optional] |
| **padTurnaround** | **kotlin.String** |  |  [optional] |
| **flightclubUrl** | **kotlin.String** |  |  [optional] |
| **lastUpdated** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  [optional] |
| **provider** | [**AgencySummary**](AgencySummary.md) |  |  [optional] |
| **rocket** | [**Rocket**](Rocket.md) |  |  [optional] |
| **mission** | [**Mission**](Mission.md) |  |  [optional] |
| **pad** | [**PadSummary**](PadSummary.md) |  |  [optional] |
| **updates** | [**kotlin.collections.List&lt;LaunchUpdate&gt;**](LaunchUpdate.md) |  |  [optional] |
| **timeline** | [**kotlin.collections.List&lt;TimelineEvent&gt;**](TimelineEvent.md) |  |  [optional] |
| **infoUrls** | [**kotlin.collections.List&lt;InfoUrl&gt;**](InfoUrl.md) |  |  [optional] |
| **vidUrls** | [**kotlin.collections.List&lt;VidUrl&gt;**](VidUrl.md) |  |  [optional] |
| **missionPatches** | [**kotlin.collections.List&lt;MissionPatchSchema&gt;**](MissionPatchSchema.md) |  |  [optional] |



