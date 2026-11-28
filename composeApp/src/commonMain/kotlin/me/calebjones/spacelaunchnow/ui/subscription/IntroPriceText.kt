package me.calebjones.spacelaunchnow.ui.subscription

import androidx.compose.runtime.Composable
import me.calebjones.spacelaunchnow.data.model.ProductInfo
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import spacelaunchnow_kmp.composeapp.generated.resources.Res
import spacelaunchnow_kmp.composeapp.generated.resources.paywall_intro_price_first_month
import spacelaunchnow_kmp.composeapp.generated.resources.paywall_intro_price_generic
import spacelaunchnow_kmp.composeapp.generated.resources.paywall_intro_price_months

/**
 * The localized intro-price line for a monthly [product], or null when it has no intro offer
 * the user can get. Both prices come from the store, never from hard-coded amounts.
 */
@Composable
fun introPriceText(product: ProductInfo?): String? {
    val intro = product?.introOfferPrice ?: return null
    return when {
        product.isFirstMonthIntroOffer ->
            stringResource(Res.string.paywall_intro_price_first_month, intro, product.formattedPrice)
        product.isMonthlyIntroOffer -> {
            val months = product.introOfferCycles
            pluralStringResource(Res.plurals.paywall_intro_price_months, months, intro, product.formattedPrice, months)
        }
        product.hasIntroOffer && !product.hasFreeTrial ->
            stringResource(Res.string.paywall_intro_price_generic, intro, product.formattedPrice)
        else -> null
    }
}
