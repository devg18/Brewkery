package com.dev.brewkery.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dev.brewkery.data.MenuRepository
import com.dev.brewkery.data.friendlyMessage
import com.dev.brewkery.domain.Menu
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Error(val message: String) : HomeUiState
    data class Success(val menu: Menu) : HomeUiState
}

class HomeViewModel(private val repository: MenuRepository) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            repository.refreshMenu().fold(
                onSuccess = { _state.value = HomeUiState.Success(it) },
                onFailure = { _state.value = HomeUiState.Error(it.friendlyMessage()) }
            )
        }
    }

    fun selectCategory(id: String?) {
        _selectedCategory.value = id
    }
}
