package com.app.testik.presentation.screen.questionmain

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.mapper.toDomain
import com.app.testik.presentation.mapper.toQuestionItem
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenEvent
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class QuestionMainViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestQuestionsUseCase: GetTestQuestionsUseCase,
    private val updateAnswersUseCase: UpdateAnswersUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<QuestionMainScreenUIState>>
        get() = _uiState

    val event: SharedFlow<QuestionMainScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<QuestionMainScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<QuestionMainScreenEvent>()

    private val args = QuestionMainFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private var screenUIState = QuestionMainScreenUIState(id = args.id, testId = args.testId)

    init {
        updateList()
    }

    fun updateAnswers(question: Int, answers: List<AnswerDelegateItem>) {
        val questions = screenUIState.questions.toMutableList().also {
            it[question] = it[question].copy(answers = answers)
        }
        screenUIState = screenUIState.copy(questions = questions)
    }

    fun saveAnswers(showInfo: Boolean = false) {
        if (showInfo) emitEvent(QuestionMainScreenEvent.Loading)
        viewModelScope.launch {
            updateAnswersUseCase(
                recordId = screenUIState.id,
                questions = screenUIState.questions.map { it.toDomain() }
            ).onSuccess {
                if (showInfo) emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.draft_saved))
            }.onError {
                handleError(it)
            }
        }
    }

    private fun updateList() {
        viewModelScope.launch {
            getTestQuestionsUseCase(screenUIState.testId).onSuccess { list ->
                updateScreenState(screenUIState.copy(questions = list.map { it.toQuestionItem() }))
                saveAnswers()
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            else -> emitEvent(QuestionMainScreenEvent.ShowSnackbar(error))
        }
    }

    private fun updateScreenState(state: QuestionMainScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: QuestionMainScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}