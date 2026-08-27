@file:OptIn(kotlin.time.ExperimentalTime::class)

package me.calebjones.spacelaunchnow.data.repository

import kotlinx.coroutines.CompletableDeferred
import me.calebjones.spacelaunchnow.api.snapi.models.Article
import me.calebjones.spacelaunchnow.api.snapi.models.PaginatedArticleList
import me.calebjones.spacelaunchnow.data.model.DataResult
import me.calebjones.spacelaunchnow.data.model.DataSource
import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Instant

/**
 * Fake [ArticlesRepository] for ViewModel tests.
 *
 * [pagesByOffset] models an offset-paginated feed, so a test can hand back a page 2 that
 * overlaps page 1 — exactly what SNAPI does when the feed gains a row between two fetches.
 * Every requested offset is recorded in [offsetsRequested] so tests can assert that a page
 * was fetched once and only once.
 *
 * [gatesByOffset] holds a fetch open. Without it every method here returns without ever
 * suspending, so under `StandardTestDispatcher` a single `advanceUntilIdle()` runs each load
 * to completion before the next one starts — and any test about two loads overlapping
 * silently stops testing anything.
 */
class FakeArticlesRepository : ArticlesRepository {

    /** Page keyed by the offset that should serve it. Unmapped offsets return an empty page. */
    val pagesByOffset: MutableMap<Int, PaginatedArticleList> = mutableMapOf()

    /**
     * Offsets whose fetch suspends until the test completes the deferred. An offset with no
     * entry (or an already-completed one) returns immediately.
     */
    val gatesByOffset: MutableMap<Int, CompletableDeferred<Unit>> = mutableMapOf()

    val offsetsRequested: MutableList<Int> = mutableListOf()

    var shouldFail = false
    private val failureException = Exception("FakeArticlesRepository configured to fail")

    private val emptyPage = PaginatedArticleList(count = 0, results = emptyList(), next = null, previous = null)

    override suspend fun getArticlesPaginated(
        limit: Int,
        offset: Int,
        search: String?,
        newsSites: List<String>?,
        forceRefresh: Boolean
    ): Result<DataResult<PaginatedArticleList>> {
        offsetsRequested += offset
        // Cancellation is returned, not thrown, to match ArticlesRepositoryImpl: its
        // `catch (e: Exception)` swallows CancellationException into a Result.failure. A fake
        // that re-threw would test a contract production does not honour today.
        try {
            gatesByOffset[offset]?.await()
        } catch (cancellation: CancellationException) {
            return Result.failure(cancellation)
        }
        if (shouldFail) return Result.failure(failureException)
        return Result.success(DataResult(pagesByOffset[offset] ?: emptyPage, DataSource.NETWORK))
    }

    override suspend fun getArticles(limit: Int, forceRefresh: Boolean): Result<DataResult<PaginatedArticleList>> {
        if (shouldFail) return Result.failure(failureException)
        return Result.success(DataResult(pagesByOffset[0] ?: emptyPage, DataSource.NETWORK))
    }

    override suspend fun getFeaturedArticles(limit: Int): Result<PaginatedArticleList> =
        if (shouldFail) Result.failure(failureException) else Result.success(emptyPage)

    override suspend fun getArticlesByLaunch(launchIds: List<String>, limit: Int): Result<PaginatedArticleList> =
        if (shouldFail) Result.failure(failureException) else Result.success(emptyPage)

    override suspend fun getArticleById(id: Int): Result<Article> =
        Result.failure(NotImplementedError("Not wired in fake"))

    override suspend fun searchArticles(query: String, limit: Int): Result<PaginatedArticleList> =
        if (shouldFail) Result.failure(failureException) else Result.success(emptyPage)
}

/** Build a page of [count] articles with sequential ids starting at [startId]. */
fun articlePage(
    startId: Int,
    count: Int,
    next: String?,
    totalCount: Int = 100
): PaginatedArticleList = PaginatedArticleList(
    count = totalCount,
    results = (startId until startId + count).map { sampleArticle(id = it) },
    next = next,
    previous = null
)

fun sampleArticle(
    id: Int,
    title: String = "Article $id"
): Article = Article(
    id = id,
    title = title,
    authors = emptyList(),
    url = "https://example.test/articles/$id",
    imageUrl = "https://example.test/articles/$id.jpg",
    newsSite = "Test News",
    summary = "Summary for article $id",
    publishedAt = Instant.fromEpochSeconds(1_700_000_000L + id),
    updatedAt = Instant.fromEpochSeconds(1_700_000_000L + id),
    launches = emptyList(),
    events = emptyList(),
    featured = false
)
