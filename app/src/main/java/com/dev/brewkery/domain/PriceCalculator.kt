package com.dev.brewkery.domain

/** All money maths lives here so it is easy to unit test. */
object PriceCalculator {

    fun round2(value: Double): Double = Math.round(value * 100.0) / 100.0

    fun unitPrice(
        item: MenuItem,
        size: SizeOption?,
        sugar: SugarOption?,
        milk: MilkOption?
    ): Double = round2(
        item.basePrice +
            (size?.extraPrice ?: 0.0) +
            (sugar?.extraPrice ?: 0.0) +
            (milk?.extraPrice ?: 0.0)
    )

    /** subtotal + delivery fee + tax (tax is charged on the subtotal). */
    fun totals(lines: List<CartLine>, deliveryFee: Double, taxRatePercent: Double): CartTotals {
        if (lines.isEmpty()) return CartTotals.EMPTY
        val subtotal = round2(lines.sumOf { it.lineTotal })
        val tax = round2(subtotal * taxRatePercent / 100.0)
        val total = round2(subtotal + deliveryFee + tax)
        return CartTotals(subtotal, deliveryFee, tax, total, lines.sumOf { it.quantity })
    }
}
