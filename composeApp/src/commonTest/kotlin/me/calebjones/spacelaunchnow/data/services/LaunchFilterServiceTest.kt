package me.calebjones.spacelaunchnow.data.services

import me.calebjones.spacelaunchnow.data.model.NotificationState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LaunchFilterServiceTest {
    
    private val service = LaunchFilterService()
    
    @Test
    fun `getAgencyIds returns null when followAllLaunches is true`() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedAgencies = setOf("121", "44")
        )
        
        assertNull(service.getAgencyIds(state))
    }
    
    @Test
    fun `getAgencyIds returns null when no agencies selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet()
        )
        
        assertNull(service.getAgencyIds(state))
    }
    
    @Test
    fun `getAgencyIds converts string IDs to integers`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "44", "96")
        )
        
        val result = service.getAgencyIds(state)
        assertEquals(listOf(121, 44, 96), result)
    }
    
    @Test
    fun `getAgencyIds filters out invalid IDs`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "invalid", "44", "not-a-number")
        )
        
        val result = service.getAgencyIds(state)
        assertEquals(listOf(121, 44), result)
    }
    
    @Test
    fun `getLocationIds returns null when followAllLaunches is true`() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedLocations = setOf("27", "11")
        )
        
        assertNull(service.getLocationIds(state))
    }
    
    @Test
    fun `getLocationIds returns null when no locations selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedLocations = emptySet()
        )
        
        assertNull(service.getLocationIds(state))
    }
    
    @Test
    fun `getLocationIds converts string IDs to integers`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedLocations = setOf("11", "5")  // Use non-grouped locations
        )
        
        val result = service.getLocationIds(state)
        assertEquals(listOf(11, 5), result)
    }
    
    @Test
    fun `getLocationIds filters out Other location (ID 0)`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedLocations = setOf("0", "11", "5")  // Use non-grouped locations
        )
        
        val result = service.getLocationIds(state)
        assertEquals(listOf(11, 5), result)
    }
    
    @Test
    fun `getLocationIds filters out invalid IDs`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedLocations = setOf("11", "bad-id", "5")  // Use non-grouped locations
        )
        
        val result = service.getLocationIds(state)
        assertEquals(listOf(11, 5), result)
    }
    
    @Test
    fun `hasActiveFilters returns false when followAllLaunches is true`() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("27")
        )
        
        assertFalse(service.hasActiveFilters(state))
    }
    
    @Test
    fun `hasActiveFilters returns false when no filters selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(),
            subscribedLocations = emptySet()
        )
        
        assertFalse(service.hasActiveFilters(state))
    }
    
    @Test
    fun `hasActiveFilters returns true when agencies selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),
            subscribedLocations = emptySet()
        )
        
        assertTrue(service.hasActiveFilters(state))
    }
    
    @Test
    fun `hasActiveFilters returns true when locations selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(),
            subscribedLocations = setOf("27")
        )
        
        assertTrue(service.hasActiveFilters(state))
    }
    
    @Test
    fun `hasActiveFilters returns true when both selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("27")
        )
        
        assertTrue(service.hasActiveFilters(state))
    }
    
    @Test
    fun `willFilterEverything returns false when followAllLaunches is true`() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedAgencies = emptySet(),
            subscribedLocations = emptySet()
        )
        
        assertFalse(service.willFilterEverything(state))
    }
    
    @Test
    fun `willFilterEverything returns true when filtering enabled but nothing selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(),
            subscribedLocations = emptySet()
        )
        
        assertTrue(service.willFilterEverything(state))
    }
    
    @Test
    fun `willFilterEverything returns false when agencies selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),
            subscribedLocations = emptySet()
        )
        
        assertFalse(service.willFilterEverything(state))
    }
    
    @Test
    fun `willFilterEverything returns false when locations selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(),
            subscribedLocations = setOf("27")
        )
        
        assertFalse(service.willFilterEverything(state))
    }
    
    @Test
    fun `getFilterParams returns null filters when followAllLaunches is true`() {
        val state = NotificationState(
            followAllLaunches = true,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("27")
        )
        
        val params = service.getFilterParams(state)
        assertNull(params.agencyIds)
        assertNull(params.locationIds)
        assertFalse(params.requiresFlexibleMerge)
    }
    
    @Test
    fun `getFilterParams returns agency filter only when only agencies selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "44"),
            subscribedLocations = emptySet(),
            useStrictMatching = false
        )
        
        val params = service.getFilterParams(state)
        assertEquals(listOf(121, 44), params.agencyIds)
        assertNull(params.locationIds)
        assertFalse(params.requiresFlexibleMerge)
    }
    
    @Test
    fun `getFilterParams returns location filter only when only locations selected`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = emptySet(),
            subscribedLocations = setOf("27", "11"),
            useStrictMatching = false
        )
        
        val params = service.getFilterParams(state)
        assertNull(params.agencyIds)
        // Florida (27) expands to include Cape Canaveral (12), Vandenberg (11) has no additional IDs
        assertEquals(listOf(27, 12, 11), params.locationIds)
        assertFalse(params.requiresFlexibleMerge)
    }
    
    @Test
    fun `getFilterParams with both filters and strict mode does not require merge`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121"),
            subscribedLocations = setOf("27"),
            useStrictMatching = true
        )
        
        val params = service.getFilterParams(state)
        assertEquals(listOf(121), params.agencyIds)
        // Florida (27) expands to include Cape Canaveral (12)
        assertEquals(listOf(27, 12), params.locationIds)
        assertFalse(params.requiresFlexibleMerge)
    }
    
    @Test
    fun `getFilterParams with both filters and flexible mode requires merge`() {
        val state = NotificationState(
            followAllLaunches = false,
            subscribedAgencies = setOf("121", "44"),
            subscribedLocations = setOf("27", "11"),
            useStrictMatching = false
        )
        
        val params = service.getFilterParams(state)
        assertEquals(listOf(121, 44), params.agencyIds)
        // Florida (27) expands to include Cape Canaveral (12), Vandenberg (11) has no additional IDs
        assertEquals(listOf(27, 12, 11), params.locationIds)
        assertTrue(params.requiresFlexibleMerge)
    }
    
    @Test
    fun `FilterParams data class properties are accessible`() {
        val params = FilterParams(
            agencyIds = listOf(121, 44),
            locationIds = listOf(27),
            requiresFlexibleMerge = true
        )
        
        assertEquals(listOf(121, 44), params.agencyIds)
        assertEquals(listOf(27), params.locationIds)
        assertTrue(params.requiresFlexibleMerge)
    }
    
    @Test
    fun `FilterParams supports null values`() {
        val params = FilterParams(
            agencyIds = null,
            locationIds = null,
            requiresFlexibleMerge = false
        )
        
        assertNull(params.agencyIds)
        assertNull(params.locationIds)
        assertFalse(params.requiresFlexibleMerge)
    }
}
