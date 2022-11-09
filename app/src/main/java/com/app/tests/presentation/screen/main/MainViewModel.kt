package com.app.tests.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.usecase.GetCurrentUserUseCase
import com.app.tests.presentation.model.UIState
import com.app.tests.presentation.screen.main.model.MainScreenEvent
import com.app.tests.presentation.screen.main.model.MainScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<MainScreenUIState>>
        get() = _uiState

    val event: SharedFlow<MainScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<MainScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<MainScreenEvent>()

    private var screenUIState = MainScreenUIState()

    init {
        getUserInfo()
    }

    fun getUserInfo() {
        //emitEvent(MainScreenEvent.Loading)

        viewModelScope.launch {
            getCurrentUserUseCase().onSuccess {
                updateScreenState(
                    MainScreenUIState(
                        email = it.email,
                        username = it.username
                    )
                )
            }.onError {
                emitEvent(MainScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun updateScreenState(state: MainScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: MainScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}