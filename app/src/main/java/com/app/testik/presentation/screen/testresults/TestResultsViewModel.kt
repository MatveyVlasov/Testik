package com.app.testik.presentation.screen.testresults

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.TestPassedModel
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetTestInfoUseCase
import com.app.testik.domain.usecase.GetTestPassedInfoUseCase
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testresults.mapper.toUIState
import com.app.testik.presentation.screen.testresults.model.TestResultsScreenEvent
import com.app.testik.presentation.screen.testresults.model.TestResultsScreenUIState
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class TestResultsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestInfoUseCase: GetTestInfoUseCase,
    private val getTestPassedInfoUseCase: GetTestPassedInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestResultsScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestResultsScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestResultsScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestResultsScreenEvent>()

    private val args = TestResultsFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private var screenUIState = TestResultsScreenUIState(recordId = args.recordId)

    var testToInsert: TestPassedModel? = null

    init {
        getRecordInfo()
    }

    private fun getRecordInfo() {
        emitEvent(TestResultsScreenEvent.Loading)

        viewModelScope.launch {
            getTestPassedInfoUseCase(recordId = screenUIState.recordId).onSuccess {
                testToInsert = it
                screenUIState = it.toUIState()
                getTestInfo()
            }.onError {
                emitEvent(TestResultsScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getTestInfo() {
        emitEvent(TestResultsScreenEvent.Loading)

        viewModelScope.launch {
            getTestInfoUseCase(testId = screenUIState.testId, source = Source.CACHE).onSuccess {
                val screenState = screenUIState.copy(
                    title = it.title,
                )
                updateScreenState(screenState)
            }.onError {
                emitEvent(TestResultsScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun updateScreenState(state: TestResultsScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestResultsScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}