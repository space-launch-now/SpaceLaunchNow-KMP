package me.calebjones.spacelaunchnow.api.snapi.infrastructure

import me.calebjones.spacelaunchnow.api.snapi.apis.ArticlesApi
import me.calebjones.spacelaunchnow.api.snapi.apis.BlogsApi
import me.calebjones.spacelaunchnow.api.snapi.apis.ReportsApi

typealias MultiValueMap = MutableMap<String, List<String>>

fun collectionDelimiter(collectionFormat: String): String = when (collectionFormat) {
    "csv" -> ","
    "tsv" -> "\t"
    "pipe" -> "|"
    "space" -> " "
    else -> ""
}

val defaultMultiValueConverter: (item: Any?) -> String = { item ->
    when (item) {
        // Handle known enum types with value property
        is ArticlesApi.OrderingArticlesList -> item.value
        is BlogsApi.OrderingBlogsList -> item.value
        is ReportsApi.OrderingReportsList -> item.value
        else -> "$item"
    }
}

fun <T : Any?> toMultiValue(
    items: Array<T>,
    collectionFormat: String,
    map: (item: T) -> String = defaultMultiValueConverter
): List<String> = toMultiValue(items.asIterable(), collectionFormat, map)

fun <T : Any?> toMultiValue(
    items: Iterable<T>,
    collectionFormat: String,
    map: (item: T) -> String = defaultMultiValueConverter
): List<String> {
    return when (collectionFormat) {
        "multi" -> items.map(map)
        else -> listOf(
            items.joinToString(
                separator = collectionDelimiter(collectionFormat),
                transform = map
            )
        )
    }
}
