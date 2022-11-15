package com.app.testik.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetCurrentUserInfoUseCase
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.main.model.MainScreenEvent
import com.app.testik.presentation.screen.main.model.MainScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserInfoUseCase: GetCurrentUserInfoUseCase
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
            getCurrentUserInfoUseCase().onSuccess {
                updateScreenState(
                    MainScreenUIState(
                        avatar = it.avatar
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