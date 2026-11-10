
# SearchResponse

## Properties
| Name | Type | Description | Notes |
| ------------ | ------------- | ------------- | ------------- |
| **query** | **kotlin.String** | The query as the server read it: trimmed, spaces collapsed, lower case. |  |
| **top** | [**SearchTop**](SearchTop.md) | The best single result; &#x60;null&#x60; when nothing matched. |  |
| **counts** | [**SearchCounts**](SearchCounts.md) | Matches per requested type, not capped by &#x60;limit&#x60;. |  |
| **results** | [**SearchResults**](SearchResults.md) | Up to &#x60;limit&#x60; items per requested type, best first. |  |



