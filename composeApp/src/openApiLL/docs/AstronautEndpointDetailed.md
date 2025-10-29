
# AstronautEndpointDetailed

## Properties
| Name | Type | Description | Notes |
| ------------ | ------------- | ------------- | ------------- |
| **id** | **kotlin.Int** |  |  [readonly] |
| **url** | **kotlin.String** |  |  [readonly] |
| **responseMode** | **kotlin.String** |  |  [readonly] |
| **name** | **kotlin.String** |  |  |
| **status** | [**AstronautStatus**](AstronautStatus.md) |  |  |
| **agency** | [**AgencyMini**](AgencyMini.md) |  |  |
| **image** | [**Image**](Image.md) |  |  |
| **age** | **kotlin.Int** |  |  |
| **bio** | **kotlin.String** |  |  |
| **type** | [**AstronautType**](AstronautType.md) |  |  |
| **nationality** | [**kotlin.collections.List&lt;Country&gt;**](Country.md) |  |  |
| **inSpace** | **kotlin.Boolean** |  |  |
| **timeInSpace** | **kotlin.String** |  |  [readonly] |
| **evaTime** | **kotlin.String** |  |  [readonly] |
| **dateOfBirth** | [**kotlinx.datetime.LocalDate**](kotlinx.datetime.LocalDate.md) |  |  |
| **dateOfDeath** | [**kotlinx.datetime.LocalDate**](kotlinx.datetime.LocalDate.md) |  |  |
| **wiki** | **kotlin.String** |  |  |
| **lastFlight** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  |
| **firstFlight** | [**kotlin.time.Instant**](kotlin.time.Instant.md) |  |  |
| **socialMediaLinks** | [**kotlin.collections.List&lt;SocialMediaLink&gt;**](SocialMediaLink.md) |  |  |
| **flightsCount** | **kotlin.Int** |  |  |
| **landingsCount** | **kotlin.Int** |  |  |
| **spacewalksCount** | **kotlin.Int** |  |  |
| **flights** | [**kotlin.collections.List&lt;LaunchBasic&gt;**](LaunchBasic.md) |  |  |
| **landings** | [**kotlin.collections.List&lt;SpacecraftFlightNormal&gt;**](SpacecraftFlightNormal.md) |  |  |
| **spacewalks** | [**kotlin.collections.List&lt;SpacewalkNormal&gt;**](SpacewalkNormal.md) |  |  |



