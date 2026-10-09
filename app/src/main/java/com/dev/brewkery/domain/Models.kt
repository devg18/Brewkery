package com.dev.brewkery.domain

data class StoreMeta(
    val appName: String = "Brewkery",
    val tagline: String = "Artisanal Coffee & Fresh Oven Bakes",
    val deliveryFee: Double = 2.50,
    val taxRatePercent: Double = 8.0,
    val estimatedDelivery: String = "20 - 30 mins"
)

data class Category(val id: String, val name: String, val icon: String)

data class SizeOption(val id: String, val label: String, val extraPrice: Double)

data class MilkOption(val id: String, val name: String, val extraPrice: Double)

data class SugarOption(val label: String, val extraPrice: Double = 0.0)

data class MenuItem(
    val id: Int,
    val categoryId: String,
    val name: String,
    val tagline: String,
    val description: String,
    val basePrice: Double,
    val rating: Double,
    val reviewCount: Int,
    val prepTime: String,
    val calories: Int,
    val imageUrl: String,
    val badge: String,
    val ingredients: List<String>,
    val sizes: List<SizeOption>,
    val sugarLevels: List<SugarOption>,
    val milkOptions: List<MilkOption>
)

data class Menu(
    val meta: StoreMeta,
    val categories: List<Category>,
    val items: List<MenuItem>
)

data class CartLine(
    val item: MenuItem,
    val size: SizeOption?,
    val sugar: SugarOption?,
    val milk: MilkOption?,
    val quantity: Int
) {
    /** Two lines with the same dish + same options are merged into one. */
    val key: String get() = "${item.id}|${size?.id}|${sugar?.label}|${milk?.id}"
    val unitPrice: Double get() = PriceCalculator.unitPrice(item, size, sugar, milk)
    val lineTotal: Double get() = PriceCalculator.round2(unitPrice * quantity)
}

data class CartTotals(
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val total: Double,
    val itemCount: Int
) {
    companion object {
        val EMPTY = CartTotals(0.0, 0.0, 0.0, 0.0, 0)
    }
}

enum class OrderStatus { PREPARING }

data class Order(
    val ticketId: String,
    val lines: List<CartLine>,
    val totals: CartTotals,
    val status: OrderStatus,
    val estimatedDelivery: String
)
