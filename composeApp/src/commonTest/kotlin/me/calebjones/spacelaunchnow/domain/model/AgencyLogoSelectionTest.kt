package me.calebjones.spacelaunchnow.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Walks the per-surface agency-logo audit table (2026-09-05 trantor-home-screen-parity spec
 * §7 / analysis §7): given a [Provider] or [Agency] with all three images populated, each
 * surface's logo-selection expression - reproduced verbatim from its call site - must pick the
 * documented image. This is the client-side half of "every agency-logo surface renders the
 * same image on both backends" (goal G3): once the Trantor mappers stop nulling a field
 * (E12(P5)), the pre-existing UI selection formula is what makes the surface match LL.
 */
class AgencyLogoSelectionTest {

    private val image = "https://example.com/image.png"
    private val logo = "https://example.com/logo.png"
    private val socialLogo = "https://example.com/social.png"

    private val provider = Provider(
        id = 44,
        name = "SpaceX",
        abbrev = "SpX",
        type = "Commercial",
        countryCode = "US",
        logoUrl = logo,
        socialLogo = socialLogo,
        imageUrl = image
    )

    private val agency = Agency(
        id = 44,
        name = "SpaceX",
        abbrev = "SpX",
        typeName = "Commercial",
        countries = emptyList(),
        imageUrl = image,
        logoUrl = logo,
        socialLogoUrl = socialLogo,
        description = null,
        administrator = null,
        foundingYear = null
    )

    // Home hero - LaunchCardHeader.kt:128,130: social only, no fallback.
    @Test
    fun homeHeroPicksTheSocialLogo() {
        val resolved = provider.socialLogo
        assertEquals(socialLogo, resolved)
    }

    // Launch detail hero - LaunchDetailView.kt:172,245: social falls back to logo.
    @Test
    fun launchDetailHeroPicksTheSocialLogo() {
        val resolved = provider.socialLogo ?: provider.logoUrl
        assertEquals(socialLogo, resolved)
    }

    // Launch detail agency card - AgencyDetailsCard.kt:80: logo only, by design.
    @Test
    fun launchDetailAgencyCardPicksTheLogo() {
        val resolved = provider.logoUrl ?: ""
        assertEquals(logo, resolved)
    }

    // Event detail hero / agencies card - EventDetailView.kt:90,413: social falls back to logo.
    @Test
    fun eventDetailPicksTheSocialLogo() {
        val heroResolved = provider.socialLogo ?: provider.logoUrl
        val cardResolved = agency.socialLogoUrl ?: agency.logoUrl
        assertEquals(socialLogo, heroResolved)
        assertEquals(socialLogo, cardResolved)
    }

    // Agency list - AgencyListView.kt:156: social falls back to logo.
    @Test
    fun agencyListPicksTheSocialLogo() {
        val resolved = agency.socialLogoUrl ?: agency.logoUrl
        assertEquals(socialLogo, resolved)
    }

    // Agency detail hero - AgencyDetailView.kt:83: social only.
    @Test
    fun agencyDetailHeroPicksTheSocialLogo() {
        assertEquals(socialLogo, agency.socialLogoUrl)
    }

    // Agency detail body logo - AgencyDetailView.kt:240: logo only, by design.
    @Test
    fun agencyDetailBodyPicksTheLogo() {
        val resolved = agency.logoUrl ?: ""
        assertEquals(logo, resolved)
    }

    // Space-station owners card - OwnerAgenciesCard.kt:90: social only, no fallback.
    @Test
    fun stationOwnersCardPicksTheSocialLogo() {
        assertEquals(socialLogo, agency.socialLogoUrl)
    }
}
