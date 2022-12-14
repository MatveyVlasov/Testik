package com.app.testik.presentation.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.CategoryType
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetCategoryTestsUseCase
import com.app.testik.domain.usecase.GetCurrentUserInfoUseCase
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.main.mapper.toTestItem
import com.app.testik.presentation.screen.main.model.MainScreenEvent
import com.app.testik.presentation.screen.main.model.MainScreenUIState
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenEvent
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getCurrentUserInfoUseCase: GetCurrentUserInfoUseCase,
    private val getCategoryTestsUseCase: GetCategoryTestsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<MainScreenUIState>>
        get() = _uiState

    val event: SharedFlow<MainScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<MainScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<MainScreenEvent>()

    private var screenUIState = MainScreenUIState()

    init {
        getUserInfo(Source.CACHE)
        getUserInfo()

        getTests()
    }

    fun getUserInfo(source: Source = Source.DEFAULT) {
        viewModelScope.launch {
            getCurrentUserInfoUseCase(source).onSuccess {
                updateScreenState(screenUIState.copy(avatar = it.avatar))
            }.onError {
                handleError(it)
            }
        }
    }

    fun getTests() {
        _uiState.value = UIState.Loading
        for (item in screenUIState.categoryTests) {
            getTestsByCategory(item.category)
        }
    }

    private fun getTestsByCategory(category: CategoryType) {
        viewModelScope.launch {
            getCategoryTestsUseCase(category).onSuccess { data ->
                val categoryTests = screenUIState.categoryTests.map { it }.toMutableList().also { list ->
                    val pos = category.ordinal
                    list[pos] = list[pos].copy(tests = data.tests.map { it.toTestItem() })
                }
                updateScreenState(state = screenUIState.copy(categoryTests = categoryTests))
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(MainScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(MainScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("failed to get document from cache") -> Unit
            else -> emitEvent(MainScreenEvent.ShowSnackbar(error))
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