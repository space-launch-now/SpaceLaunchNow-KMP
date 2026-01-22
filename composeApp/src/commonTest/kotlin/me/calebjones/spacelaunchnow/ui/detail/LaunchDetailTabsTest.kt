package me.calebjones.spacelaunchnow.ui.detail

import me.calebjones.spacelaunchnow.ui.detail.compose.LaunchDetailTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Unit tests for the LaunchDetailTab sealed class and its companion functions.
 *
 * These tests verify:
 * - Correct number of tabs
 * - Correct tab order
 * - Correct index-to-tab mapping
 * - Correct tab display names
 */
class LaunchDetailTabsTest {

    @Test
    fun `values() returns exactly 4 tabs`() {
        val tabs = LaunchDetailTab.values()
        assertEquals(4, tabs.size, "Expected exactly 4 tabs")
    }

    @Test
    fun `values() returns tabs in correct order`() {
        val tabs = LaunchDetailTab.values()
        
        assertIs<LaunchDetailTab.Overview>(tabs[0], "First tab should be Overview")
        assertIs<LaunchDetailTab.Mission>(tabs[1], "Second tab should be Mission")
        assertIs<LaunchDetailTab.Agency>(tabs[2], "Third tab should be Agency")
        assertIs<LaunchDetailTab.Rocket>(tabs[3], "Fourth tab should be Rocket")
    }

    @Test
    fun `fromIndex(0) returns Overview tab`() {
        val tab = LaunchDetailTab.fromIndex(0)
        assertIs<LaunchDetailTab.Overview>(tab)
        assertEquals("Overview", tab.displayName)
    }

    @Test
    fun `fromIndex(1) returns Mission tab`() {
        val tab = LaunchDetailTab.fromIndex(1)
        assertIs<LaunchDetailTab.Mission>(tab)
        assertEquals("Mission", tab.displayName)
    }

    @Test
    fun `fromIndex(2) returns Agency tab`() {
        val tab = LaunchDetailTab.fromIndex(2)
        assertIs<LaunchDetailTab.Agency>(tab)
        assertEquals("Agency", tab.displayName)
    }

    @Test
    fun `fromIndex(3) returns Rocket tab`() {
        val tab = LaunchDetailTab.fromIndex(3)
        assertIs<LaunchDetailTab.Rocket>(tab)
        assertEquals("Rocket", tab.displayName)
    }

    @Test
    fun `all tabs have unique display names`() {
        val tabs = LaunchDetailTab.values()
        val displayNames = tabs.map { it.displayName }
        
        assertEquals(
            displayNames.size,
            displayNames.toSet().size,
            "All tabs should have unique display names"
        )
    }

    @Test
    fun `tab display names are not empty`() {
        val tabs = LaunchDetailTab.values()
        
        tabs.forEach { tab ->
            assertTrue(
                tab.displayName.isNotBlank(),
                "Tab display name should not be blank: $tab"
            )
        }
    }

    @Test
    fun `Overview tab has correct display name`() {
        assertEquals("Overview", LaunchDetailTab.Overview.displayName)
    }

    @Test
    fun `Mission tab has correct display name`() {
        assertEquals("Mission", LaunchDetailTab.Mission.displayName)
    }

    @Test
    fun `Agency tab has correct display name`() {
        assertEquals("Agency", LaunchDetailTab.Agency.displayName)
    }

    @Test
    fun `Rocket tab has correct display name`() {
        assertEquals("Rocket", LaunchDetailTab.Rocket.displayName)
    }
}
