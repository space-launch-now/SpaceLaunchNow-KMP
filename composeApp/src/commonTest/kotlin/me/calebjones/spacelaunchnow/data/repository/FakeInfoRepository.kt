package me.calebjones.spacelaunchnow.data.repository

/**
 * Fake [InfoRepository] for ViewModel tests — news sites are filter chrome, not the
 * subject of any pagination test.
 */
class FakeInfoRepository(
    private val newsSites: List<String> = emptyList()
) : InfoRepository {
    override suspend fun getNewsSites(): Result<List<String>> = Result.success(newsSites)
}
