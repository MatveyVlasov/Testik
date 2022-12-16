package com.app.testik.presentation.dialog.testinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.dialog.testinfo.mapper.toDomain
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogEvent
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogUIState
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testedit.TestEditFragmentArgs
import com.app.testik.util.getFullName
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestInfoUseCase: GetTestInfoUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val createTestPassedUseCase: CreateTestPassedUseCase,
) : ViewModel() {

    val uiState: StateFlow<UIState<TestInfoDialogUIState>>
        get() = _uiState

    val event: SharedFlow<TestInfoDialogEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestInfoDialogUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestInfoDialogEvent>()

    private val args = TestEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestInfoDialogUIState(id = args.testId)
        private set

    init {
        getTestInfo(source = Source.CACHE)
    }

    fun createTestPassed() {
        viewModelScope.launch {
            createTestPassedUseCase(screenUIState.toDomain()).onSuccess {
                emitEvent(TestInfoDialogEvent.SuccessTestCreation(it))
            }.onError {
                handleError(it)
            }
        }
    }

    private fun getTestInfo(source: Source = Source.DEFAULT) {
        if (screenUIState.id.isEmpty()) return
        _uiState.value = UIState.Loading

        viewModelScope.launch {
            getTestInfoUseCase(testId = screenUIState.id, source = source).onSuccess {
                val screenState = TestInfoDialogUIState(
                    id = screenUIState.id,
                    image = it.image,
                    title = it.title,
                    author = it.author,
                    description = it.description,
                    category = it.category,
                    questionsNum = it.questionsNum
                )
                updateScreenState(screenState)

                getAuthorInfo(it.author)
            }.onError {
                emitEvent(TestInfoDialogEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getAuthorInfo(author: String) {
        viewModelScope.launch {
            getUserInfoUseCase(email = author).onSuccess {
                updateScreenState(screenUIState.copy(authorName = it.getFullName()))
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            else -> emitEvent(TestInfoDialogEvent.ShowSnackbar(error))
        }
    }

    private fun updateScreenState(state: TestInfoDialogUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestInfoDialogEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}