package me.calebjones.spacelaunchnow.domain.mapper.trantor

import me.calebjones.spacelaunchnow.api.trantor.models.AgencyList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * E9(P5): the agencies list row carries `type`/`type_id`, `featured`, `country_codes`, and all
 * three images beside `id, name, abbrev` - the pre-E9 mapper nulled every one of these.
 */
class AgencyMappersTest {

    private fun testAgencyList(
        countryCodes: List<String>? = listOf("US"),
        imageUrl: String? = "https://example.com/image.png",
        logoUrl: String? = "https://example.com/logo.png",
        socialLogoUrl: String? = "https://example.com/social.png",
        type: String? = "Commercial",
        typeId: Int? = 1
    ) = AgencyList(
        abbrev = "SpX",
        id = 44,
        name = "SpaceX",
        countryCodes = countryCodes,
        featured = true,
        imageUrl = imageUrl,
        logoUrl = logoUrl,
        socialLogoUrl = socialLogoUrl,
        totalLaunchCount = 500,
        type = type,
        typeId = typeId
    )

    @Test
    fun toDomainAgencyFillsTypeCountriesAndAllThreeImages() {
        val domain = testAgencyList().toDomainAgency()
        assertEquals("Commercial", domain.typeName)
        assertEquals(1, domain.countries.size)
        assertEquals("US", domain.countries.first().alpha2Code)
        assertEquals("https://example.com/image.png", domain.imageUrl)
        assertEquals("https://example.com/logo.png", domain.logoUrl)
        assertEquals("https://example.com/social.png", domain.socialLogoUrl)
        assertEquals(true, domain.featured)
        assertEquals(500, domain.totalLaunchCount)
    }

    @Test
    fun toDomainAgencyLeavesFieldsNullWhenRowLacksThem() {
        val domain = testAgencyList(
            countryCodes = null,
            imageUrl = null,
            logoUrl = null,
            socialLogoUrl = null,
            type = null,
            typeId = null
        ).toDomainAgency()
        assertNull(domain.typeName)
        assertEquals(emptyList(), domain.countries)
        assertNull(domain.imageUrl)
        assertNull(domain.logoUrl)
        assertNull(domain.socialLogoUrl)
    }

    @Test
    fun toDomainMapsTheRowAsAProviderEmbed() {
        val domain = testAgencyList().toDomain()
        assertEquals(44, domain.id)
        assertEquals("SpaceX", domain.name)
        assertEquals("SpX", domain.abbrev)
        assertEquals("Commercial", domain.type)
        assertEquals("US", domain.countryCode)
        assertEquals("https://example.com/social.png", domain.socialLogo)
        assertEquals("https://example.com/logo.png", domain.logoUrl)
        assertEquals("https://example.com/image.png", domain.imageUrl)
    }
}
