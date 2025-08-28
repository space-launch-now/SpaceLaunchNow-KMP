package me.calebjones.spacelaunchnow.ui.compose

import kotlinx.datetime.Instant
import me.calebjones.spacelaunchnow.api.models.PaginatedUpdateEndpointList
import me.calebjones.spacelaunchnow.api.models.UpdateEndpoint
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for UI components related to Updates
 * 
 * Note: These are unit tests for the UI logic.
 * For full Compose UI testing, you would need the Compose testing framework.
 */
class LatestUpdatesViewTest {

    private fun createMockUpdate(
        id: Int = 1,
        comment: String? = "Test comment",
        createdBy: String? = "TestUser",
        profileImage: String? = "https://example.com/profile.jpg",
        createdOn: Instant? = null
    ): UpdateEndpoint {
        return UpdateEndpoint(
            id = id,
            profileImage = profileImage,
            comment = comment,
            infoUrl = null,
            createdBy = createdBy,
            launch = null,
            event = null,
            program = null,
            createdOn = createdOn
        )
    }

    @Test
    fun testUpdateDataMapping() {
        val update = createMockUpdate(
            id = 123,
            comment = "This is a test update about a launch delay",
            createdBy = "SpaceXAdmin",
            profileImage = "https://example.com/spacex-admin.jpg"
        )

        // Test that data is correctly mapped
        assertEquals(123, update.id)
        assertEquals("This is a test update about a launch delay", update.comment)
        assertEquals("SpaceXAdmin", update.createdBy)
        assertEquals("https://example.com/spacex-admin.jpg", update.profileImage)
    }

    @Test
    fun testUpdateWithNullFields() {
        val update = createMockUpdate(
            id = 456,
            comment = null,
            createdBy = null,
            profileImage = null
        )

        // Test that null fields are handled
        assertEquals(456, update.id)
        assertEquals(null, update.comment)
        assertEquals(null, update.createdBy)
        assertEquals(null, update.profileImage)
    }

    @Test
    fun testUpdateWithInstant() {
        val testInstant = Instant.parse("2025-08-27T10:30:00Z")
        val update = createMockUpdate(
            createdOn = testInstant
        )

        assertEquals(testInstant, update.createdOn)
    }

    @Test
    fun testPaginatedUpdatesList() {
        val updates = listOf(
            createMockUpdate(1, "First update", "User1"),
            createMockUpdate(2, "Second update", "User2"),
            createMockUpdate(3, "Third update", "User3")
        )

        val paginatedList = PaginatedUpdateEndpointList(
            count = 3,
            next = "https://api.example.com/updates?page=2",
            previous = null,
            results = updates
        )

        assertEquals(3, paginatedList.count)
        assertEquals(3, paginatedList.results.size)
        assertEquals("First update", paginatedList.results[0].comment)
        assertEquals("Second update", paginatedList.results[1].comment)
        assertEquals("Third update", paginatedList.results[2].comment)
        assertEquals("https://api.example.com/updates?page=2", paginatedList.next)
    }

    @Test
    fun testEmptyUpdatesList() {
        val paginatedList = PaginatedUpdateEndpointList(
            count = 0,
            next = null,
            previous = null,
            results = emptyList()
        )

        assertEquals(0, paginatedList.count)
        assertEquals(0, paginatedList.results.size)
        assertEquals(null, paginatedList.next)
        assertEquals(null, paginatedList.previous)
    }

    @Test
    fun testLongCommentTruncation() {
        val longComment = "This is a very long update comment that would exceed the maximum " +
                "display length in the UI and should be truncated to prevent layout issues " +
                "while still providing meaningful information to the user about the launch status."
        
        val update = createMockUpdate(comment = longComment)
        
        assertEquals(longComment, update.comment)
        
        // Test truncation logic (this would be in the actual UI component)
        val maxLength = 150
        val shouldTruncate = longComment.length > maxLength
        val displayComment = if (shouldTruncate) {
            longComment.take(maxLength) + "..."
        } else {
            longComment
        }
        
        assertEquals(true, shouldTruncate)
        assertEquals(153, displayComment.length) // 150 + "..."
    }

    @Test
    fun testUpdateSorting() {
        val instant1 = Instant.parse("2025-08-27T10:00:00Z")
        val instant2 = Instant.parse("2025-08-27T11:00:00Z")
        val instant3 = Instant.parse("2025-08-27T09:00:00Z")

        val updates = listOf(
            createMockUpdate(1, "First", "User1", createdOn = instant1),
            createMockUpdate(2, "Second", "User2", createdOn = instant2),
            createMockUpdate(3, "Third", "User3", createdOn = instant3)
        )

        // Test sorting by creation date (newest first)
        val sortedUpdates = updates.sortedByDescending { it.createdOn }
        
        assertEquals(instant2, sortedUpdates[0].createdOn) // Newest
        assertEquals(instant1, sortedUpdates[1].createdOn) // Middle
        assertEquals(instant3, sortedUpdates[2].createdOn) // Oldest
    }

    @Test
    fun testProfileImageFallback() {
        val updateWithImage = createMockUpdate(
            createdBy = "UserA",
            profileImage = "https://example.com/profile.jpg"
        )
        
        val updateWithoutImage = createMockUpdate(
            createdBy = "UserB", 
            profileImage = null
        )

        // Test that we have the expected data for fallback logic
        assertEquals("https://example.com/profile.jpg", updateWithImage.profileImage)
        assertEquals(null, updateWithoutImage.profileImage)
        
        // The actual fallback logic would be in the UI component
        val fallbackImageUrl = "https://via.placeholder.com/48x48/CCCCCC/000000?text=" +
                (updateWithoutImage.createdBy?.firstOrNull()?.uppercaseChar() ?: "?")
        
        assertEquals("https://via.placeholder.com/48x48/CCCCCC/000000?text=U", fallbackImageUrl)
    }
}
