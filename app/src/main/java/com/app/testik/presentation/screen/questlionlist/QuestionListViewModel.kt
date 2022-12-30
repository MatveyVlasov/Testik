package com.app.testik.presentation.screen.questlionlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.mapper.toDomain
import com.app.testik.presentation.mapper.toQuestionItem
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenEvent
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenUIState
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class QuestionListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestQuestionsUseCase: GetTestQuestionsUseCase,
    private val updateQuestionsUseCase: UpdateQuestionsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<QuestionListScreenUIState>>
        get() = _uiState

    val event: SharedFlow<QuestionListScreenEvent>
        get() = _event

    val hasUnsavedChanges
        get() = screenUIState != oldScreenUIState

    private val _uiState = MutableStateFlow<UIState<QuestionListScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<QuestionListScreenEvent>()

    private val args = QuestionListFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = QuestionListScreenUIState(testId = args.testId)
        private set

    private var oldScreenUIState = QuestionListScreenUIState(testId = args.testId)

    init {
        updateList(isFirstUpdate = true)
    }

    fun updateList(isFirstUpdate: Boolean = false) {
        if (!isFirstUpdate && screenUIState.questions.isNotEmpty()) return
        postItem(LoadingItem)

        viewModelScope.launch {
            getTestQuestionsUseCase(screenUIState.testId).onSuccess {
                postListItems(it.map { question -> question.toQuestionItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
            updateOldScreenState()
        }
    }

    fun saveQuestions() {
        emitEvent(QuestionListScreenEvent.Loading)

        viewModelScope.launch {
            val questions = screenUIState.questions
                .filterIsInstance<QuestionDelegateItem>()
                .map { it.toDomain() }

            updateQuestionsUseCase(screenUIState.testId, questions).onSuccess {
                updateList()
                emitEvent(QuestionListScreenEvent.SuccessQuestionsSaving(questions.size))
            }.onError {
                handleError(it)
            }
        }
    }

    fun addQuestionToList(question: QuestionDelegateItem) {
        val questions = screenUIState.questions.map { it }.toMutableList().also {
            it.add(question)
        }
        updateScreenState(screenUIState.copy(questions = questions))
    }

    fun updateQuestion(question: QuestionDelegateItem, newQuestion: QuestionDelegateItem) {
        val pos = screenUIState.questions.indexOf(question)
        if (pos == -1) return
        val questions = screenUIState.questions.map { it }.toMutableList().also {
            it[pos] = newQuestion
        }
        updateScreenState(screenUIState.copy(questions = questions))
    }

    fun deleteQuestionFromList(question: QuestionDelegateItem) {
        val questions = screenUIState.questions.map { it }.toMutableList().also {
            it.remove(question)
        }
        updateScreenState(screenUIState.copy(questions = questions))
    }

    fun moveQuestion(from: Int, to: Int) {
        if (from == to) return
        val questions = screenUIState.questions.map { it }.toMutableList().also {
            val item = it[from]
            it.removeAt(from)
            it.add(to, item)
        }
        updateScreenState(screenUIState.copy(questions = questions))
    }

    private fun updateList() {
        viewModelScope.launch {
            getTestQuestionsUseCase(screenUIState.testId).onSuccess { list ->
                updateScreenState(screenUIState.copy(questions = list.map { it.toQuestionItem() }))
            }
            updateOldScreenState()
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(QuestionListScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(QuestionListScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("images were not saved") -> {
                emitEvent(QuestionListScreenEvent.ShowSnackbarByRes(R.string.error_while_saving_images))
            }
            else -> emitEvent(QuestionListScreenEvent.ShowSnackbar(error))
        }
    }

    private fun postItem(data: DelegateAdapterItem) = postListItems(listOf(data))

    private fun postListItems(data: List<DelegateAdapterItem>) {
        updateScreenState(
            screenUIState.copy(
                questions = screenUIState.questions.toMutableList().apply {
                    removeAll { it is LoadingItem || it is ErrorItem }
                    addAll(data)
                }
            )
        )
    }

    private fun updateOldScreenState() {
        oldScreenUIState = screenUIState.copy(questions = screenUIState.questions.toMutableList())
    }

    private fun updateScreenState(state: QuestionListScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: QuestionListScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}