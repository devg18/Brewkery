package com.dev.brewkery

import com.dev.brewkery.data.parseSugar
import com.dev.brewkery.data.remote.CustomizationsDto
import com.dev.brewkery.data.remote.ItemDto
import com.dev.brewkery.data.remote.MenuResponseDto
import com.dev.brewkery.data.remote.SizeDto
import com.dev.brewkery.data.toDomain
import com.dev.brewkery.data.toDomainOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MapperTest {

    @Test
    fun parseSugar_extractsPriceHiddenInLabel() {
        val s = parseSugar("Light Wildflower Honey (+0.40)")
        assertEquals("Light Wildflower Honey", s.label)
        assertEquals(0.40, s.extraPrice, 0.0001)
    }

    @Test
    fun parseSugar_leavesNormalLabelsAlone() {
        val s = parseSugar("100% Standard Sweet")
        assertEquals("100% Standard Sweet", s.label)
        assertEquals(0.0, s.extraPrice, 0.0001)
    }

    @Test
    fun itemWithoutIdOrName_isDropped() {
        val bad = ItemDto(null, null, "x", null, null, null, null, null, null, null, null, null, null, null)
        assertNull(bad.toDomainOrNull())
    }

    @Test
    fun menuWithMissingMeta_usesDefaultFeesFromSpec() {
        val item = ItemDto(
            id = 3, categoryId = "cat_bakery", name = "Croissant", tagline = null, description = null,
            basePrice = 3.90, rating = 4.9, reviewCount = 4, prepTime = null, calories = 290,
            imageUrl = null, badge = null, ingredients = null,
            customizations = CustomizationsDto(listOf(SizeDto("sz", "Single", 0.0)), null, null)
        )
        val menu = MenuResponseDto(meta = null, categories = null, items = listOf(item)).toDomain()

        assertEquals(2.50, menu.meta.deliveryFee, 0.0001)
        assertEquals(8.0, menu.meta.taxRatePercent, 0.0001)
        assertEquals(1, menu.items.size)
        assertEquals("Single", menu.items[0].sizes[0].label)
    }
}
