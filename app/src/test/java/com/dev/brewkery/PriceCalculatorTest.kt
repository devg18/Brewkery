package com.dev.brewkery

import com.dev.brewkery.domain.CartLine
import com.dev.brewkery.domain.CartTotals
import com.dev.brewkery.domain.MenuItem
import com.dev.brewkery.domain.MilkOption
import com.dev.brewkery.domain.PriceCalculator
import com.dev.brewkery.domain.SizeOption
import com.dev.brewkery.domain.SugarOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class PriceCalculatorTest {

    private val size = SizeOption("sz_medium", "Grande (12 oz)", 0.65)
    private val milk = MilkOption("m_almond", "Roasted Almond Milk", 0.50)
    private val sugar = SugarOption("Light Wildflower Honey", 0.40)

    private fun item(basePrice: Double = 4.85) = MenuItem(
        id = 1, categoryId = "cat_hot_coffee", name = "Latte", tagline = "", description = "",
        basePrice = basePrice, rating = 4.9, reviewCount = 1, prepTime = "", calories = 100,
        imageUrl = "", badge = "", ingredients = emptyList(),
        sizes = listOf(size), sugarLevels = listOf(sugar), milkOptions = listOf(milk)
    )

    @Test
    fun unitPrice_addsEveryExtraToBasePrice() {
        // 4.85 + 0.65 + 0.40 + 0.50 = 6.40
        assertEquals(6.40, PriceCalculator.unitPrice(item(), size, sugar, milk), 0.0001)
    }

    @Test
    fun unitPrice_withNoSelection_isBasePrice() {
        assertEquals(4.85, PriceCalculator.unitPrice(item(), null, null, null), 0.0001)
    }

    @Test
    fun totals_addDeliveryFeeAndEightPercentTax() {
        val lines = listOf(CartLine(item(basePrice = 5.00), null, null, null, quantity = 2))
        val totals = PriceCalculator.totals(lines, deliveryFee = 2.50, taxRatePercent = 8.0)

        assertEquals(10.00, totals.subtotal, 0.0001)
        assertEquals(2.50, totals.deliveryFee, 0.0001)
        assertEquals(0.80, totals.tax, 0.0001)
        assertEquals(13.30, totals.total, 0.0001)
        assertEquals(2, totals.itemCount)
    }

    @Test
    fun totals_emptyCart_isAllZero_noDeliveryFee() {
        assertEquals(CartTotals.EMPTY, PriceCalculator.totals(emptyList(), 2.50, 8.0))
    }

    @Test
    fun cartLine_key_changesWhenOptionsChange() {
        val a = CartLine(item(), size, sugar, milk, 1)
        val b = a.copy(milk = MilkOption("m_oat", "Oat", 0.0))
        assertNotEquals(a.key, b.key)
        assertEquals(a.key, a.copy(quantity = 5).key)
    }
}
