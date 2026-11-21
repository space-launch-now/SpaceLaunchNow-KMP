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
 * - Correct tab title resource keys
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
        assertEquals("launch_detail_tab_overview", tab.titleRes.key)
    }

    @Test
    fun `fromIndex(1) returns Mission tab`() {
        val tab = LaunchDetailTab.fromIndex(1)
        assertIs<LaunchDetailTab.Mission>(tab)
        assertEquals("launch_detail_tab_mission", tab.titleRes.key)
    }

    @Test
    fun `fromIndex(2) returns Agency tab`() {
        val tab = LaunchDetailTab.fromIndex(2)
        assertIs<LaunchDetailTab.Agency>(tab)
        assertEquals("launch_detail_tab_agency", tab.titleRes.key)
    }

    @Test
    fun `fromIndex(3) returns Rocket tab`() {
        val tab = LaunchDetailTab.fromIndex(3)
        assertIs<LaunchDetailTab.Rocket>(tab)
        assertEquals("launch_detail_tab_rocket", tab.titleRes.key)
    }

    @Test
    fun `all tabs have unique display names`() {
        val tabs = LaunchDetailTab.values()
        val displayNames = tabs.map { it.titleRes.key }
        
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
                tab.titleRes.key.isNotBlank(),
                "Tab display name should not be blank: $tab"
            )
        }
    }

    @Test
    fun `Overview tab has correct display name`() {
        assertEquals("launch_detail_tab_overview", LaunchDetailTab.Overview.titleRes.key)
    }

    @Test
    fun `Mission tab has correct display name`() {
        assertEquals("launch_detail_tab_mission", LaunchDetailTab.Mission.titleRes.key)
    }

    @Test
    fun `Agency tab has correct display name`() {
        assertEquals("launch_detail_tab_agency", LaunchDetailTab.Agency.titleRes.key)
    }

    @Test
    fun `Rocket tab has correct display name`() {
        assertEquals("launch_detail_tab_rocket", LaunchDetailTab.Rocket.titleRes.key)
    }
}
