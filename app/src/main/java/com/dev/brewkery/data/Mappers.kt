package com.dev.brewkery.data

import com.dev.brewkery.data.remote.CategoryDto
import com.dev.brewkery.data.remote.ItemDto
import com.dev.brewkery.data.remote.MenuResponseDto
import com.dev.brewkery.domain.Category
import com.dev.brewkery.domain.Menu
import com.dev.brewkery.domain.MenuItem
import com.dev.brewkery.domain.MilkOption
import com.dev.brewkery.domain.SizeOption
import com.dev.brewkery.domain.StoreMeta
import com.dev.brewkery.domain.SugarOption

// Matches hints like "Light Wildflower Honey (+0.40)" at the end of a sugar label.
private val PRICE_HINT = Regex("""\s*\(\+[$]?(\d+(?:\.\d+)?)\)\s*$""")

/** The API hides an extra price inside some sugar labels. Pull it out so the price is right. */
fun parseSugar(raw: String): SugarOption {
    val match = PRICE_HINT.find(raw) ?: return SugarOption(raw.trim())
    return SugarOption(
        label = raw.removeRange(match.range).trim(),
        extraPrice = match.groupValues[1].toDouble()
    )
}

fun MenuResponseDto.toDomain(): Menu {
    val defaults = StoreMeta()
    val m = this.meta
    val storeMeta = StoreMeta(
        appName = m?.app ?: defaults.appName,
        tagline = m?.tagline ?: defaults.tagline,
        deliveryFee = m?.deliveryFee ?: defaults.deliveryFee,
        taxRatePercent = m?.taxRatePercent ?: defaults.taxRatePercent,
        estimatedDelivery = m?.estimatedDeliveryTime ?: defaults.estimatedDelivery
    )
    return Menu(
        meta = storeMeta,
        categories = categories.orEmpty().mapNotNull { it.toDomainOrNull() },
        items = items.orEmpty().mapNotNull { it.toDomainOrNull() }
    )
}

fun CategoryDto.toDomainOrNull(): Category? {
    val id = id ?: return null
    val name = name ?: return null
    return Category(id, name, icon ?: "☕")
}

fun ItemDto.toDomainOrNull(): MenuItem? {
    val id = id ?: return null
    val name = name ?: return null
    val c = customizations
    return MenuItem(
        id = id,
        categoryId = categoryId.orEmpty(),
        name = name,
        tagline = tagline.orEmpty(),
        description = description.orEmpty(),
        basePrice = basePrice ?: 0.0,
        rating = rating ?: 0.0,
        reviewCount = reviewCount ?: 0,
        prepTime = prepTime.orEmpty(),
        calories = calories ?: 0,
        imageUrl = imageUrl.orEmpty(),
        badge = badge.orEmpty(),
        ingredients = ingredients.orEmpty(),
        sizes = c?.sizes.orEmpty().mapNotNull { s ->
            val sid = s.id ?: return@mapNotNull null
            SizeOption(sid, s.label ?: sid, s.extraPrice ?: 0.0)
        },
        sugarLevels = c?.sugarLevels.orEmpty().map { parseSugar(it) },
        milkOptions = c?.milkOptions.orEmpty().mapNotNull { m ->
            val mid = m.id ?: return@mapNotNull null
            MilkOption(mid, m.name ?: mid, m.extraPrice ?: 0.0)
        }
    )
}
