package com.example.pulltorefreshcompose.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.pulltorefreshcompose.home.HomeContract.UiEffect
import com.example.pulltorefreshcompose.home.HomeContract.UiState
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect by lazy { Channel<UiEffect>() }
    val uiEffect: Flow<UiEffect> by lazy { _uiEffect.receiveAsFlow() }

    init {
        updateUiState { copy(itemList = (0..16).map { "Item $it" }) }
    }

    fun onAction(uiAction: HomeContract.UiAction) {
        when (uiAction) {
            is HomeContract.UiAction.Refresh -> getItemList()
        }
    }

    private fun getItemList() = viewModelScope.launch {
        updateUiState { copy(isRefreshing = true) }
        delay(3000)
        updateUiState { copy(isRefreshing = false, itemList = (0..16).map { "Item $it" }.shuffled()) }
    }

    private fun updateUiState(block: UiState.() -> UiState) {
        _uiState.update(block)
    }

    suspend fun emitUiEffect(uiEffect: UiEffect) {
        _uiEffect.send(uiEffect)
    }
}

object HomeContract {
    data class UiState(
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val itemList: List<String> = listOf(),
    )

    sealed class UiAction {
        data object Refresh : UiAction()
    }

    sealed class UiEffect
}