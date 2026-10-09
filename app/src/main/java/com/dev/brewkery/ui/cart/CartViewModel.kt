package com.dev.brewkery.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.brewkery.data.MenuRepository
import com.dev.brewkery.domain.CartLine
import com.dev.brewkery.domain.CartTotals
import com.dev.brewkery.domain.MenuItem
import com.dev.brewkery.domain.MilkOption
import com.dev.brewkery.domain.Order
import com.dev.brewkery.domain.OrderStatus
import com.dev.brewkery.domain.PriceCalculator
import com.dev.brewkery.domain.SizeOption
import com.dev.brewkery.domain.StoreMeta
import com.dev.brewkery.domain.SugarOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlin.random.Random

/** Activity-scoped: the cart and the active order are shared by all screens. */
class CartViewModel(repository: MenuRepository) : ViewModel() {

    private val _lines = MutableStateFlow<List<CartLine>>(emptyList())
    val lines: StateFlow<List<CartLine>> = _lines.asStateFlow()

    val meta: StateFlow<StoreMeta> = repository.menu
        .map { it?.meta ?: StoreMeta() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, StoreMeta())

    val totals: StateFlow<CartTotals> = combine(_lines, meta) { lines, meta ->
        PriceCalculator.totals(lines, meta.deliveryFee, meta.taxRatePercent)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CartTotals.EMPTY)

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    fun add(item: MenuItem, size: SizeOption?, sugar: SugarOption?, milk: MilkOption?, quantity: Int) {
        val newLine = CartLine(item, size, sugar, milk, quantity)
        _lines.update { current ->
            val existing = current.firstOrNull { it.key == newLine.key }
            if (existing == null) {
                current + newLine
            } else {
                current.map {
                    if (it.key == newLine.key) it.copy(quantity = (it.quantity + quantity).coerceAtMost(20)) else it
                }
            }
        }
    }

    fun increment(key: String) = changeQuantity(key, +1)
    fun decrement(key: String) = changeQuantity(key, -1)

    fun remove(key: String) = _lines.update { list -> list.filterNot { it.key == key } }

    private fun changeQuantity(key: String, delta: Int) {
        _lines.update { list ->
            list.mapNotNull {
                if (it.key != key) it
                else {
                    val q = it.quantity + delta
                    if (q <= 0) null else it.copy(quantity = q.coerceAtMost(20))
                }
            }
        }
    }

    /** Clears the cart, creates a ticket and starts the order in PREPARING. */
    fun placeOrder(): Order? {
        val current = _lines.value
        if (current.isEmpty()) return null
        val m = meta.value
        val order = Order(
            ticketId = "BRW-${Random.nextInt(1000, 10000)}",
            lines = current,
            totals = PriceCalculator.totals(current, m.deliveryFee, m.taxRatePercent),
            status = OrderStatus.PREPARING,
            estimatedDelivery = m.estimatedDelivery
        )
        _order.value = order
        _lines.value = emptyList()
        return order
    }
}
