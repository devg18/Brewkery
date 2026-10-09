package com.dev.brewkery.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.brewkery.data.MenuRepository
import com.dev.brewkery.data.friendlyMessage
import com.dev.brewkery.domain.MenuItem
import com.dev.brewkery.domain.MilkOption
import com.dev.brewkery.domain.PriceCalculator
import com.dev.brewkery.domain.SizeOption
import com.dev.brewkery.domain.SugarOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val item: MenuItem? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val size: SizeOption? = null,
    val sugar: SugarOption? = null,
    val milk: MilkOption? = null,
    val quantity: Int = 1
) {
    val unitPrice: Double
        get() = item?.let { PriceCalculator.unitPrice(it, size, sugar, milk) } ?: 0.0
    val total: Double
        get() = PriceCalculator.round2(unitPrice * quantity)
}

class DetailViewModel(
    private val repository: MenuRepository,
    private val itemId: Int
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        // Show the cached item instantly (no spinner), then refresh from the single-item endpoint.
        repository.cachedItem(itemId)?.let { cached -> _state.value = withDefaults(cached) }
        load()
    }

    fun load() {
        val hasData = _state.value.item != null
        if (!hasData) _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.fetchItem(itemId).fold(
                onSuccess = { fresh -> _state.update { current -> applyFresh(current, fresh) } },
                onFailure = { e ->
                    _state.update { current ->
                        // If we already have cached data, stay quiet. Otherwise show the error.
                        if (current.item != null) current.copy(isLoading = false)
                        else current.copy(isLoading = false, error = e.friendlyMessage())
                    }
                }
            )
        }
    }

    private fun withDefaults(item: MenuItem) = DetailUiState(
        item = item,
        isLoading = false,
        size = item.sizes.firstOrNull(),
        sugar = item.sugarLevels.firstOrNull(),
        milk = item.milkOptions.firstOrNull()
    )

    private fun applyFresh(old: DetailUiState, fresh: MenuItem): DetailUiState = old.copy(
        item = fresh,
        isLoading = false,
        error = null,
        size = fresh.sizes.firstOrNull { it.id == old.size?.id } ?: fresh.sizes.firstOrNull(),
        sugar = fresh.sugarLevels.firstOrNull { it.label == old.sugar?.label } ?: fresh.sugarLevels.firstOrNull(),
        milk = fresh.milkOptions.firstOrNull { it.id == old.milk?.id } ?: fresh.milkOptions.firstOrNull()
    )

    fun selectSize(size: SizeOption) = _state.update { it.copy(size = size) }
    fun selectSugar(sugar: SugarOption) = _state.update { it.copy(sugar = sugar) }
    fun selectMilk(milk: MilkOption) = _state.update { it.copy(milk = milk) }
    fun increment() = _state.update { it.copy(quantity = (it.quantity + 1).coerceAtMost(20)) }
    fun decrement() = _state.update { it.copy(quantity = (it.quantity - 1).coerceAtLeast(1)) }
}
