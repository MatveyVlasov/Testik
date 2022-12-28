package com.app.testik.presentation.screen.testpasseddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetTestInfoUseCase
import com.app.testik.domain.usecase.GetTestPassedInfoUseCase
import com.app.testik.domain.usecase.GetTestPassedQuestionsUseCase
import com.app.testik.domain.usecase.GetTestResultsUseCase
import com.app.testik.presentation.mapper.toQuestionItem
import com.app.testik.presentation.model.*
import com.app.testik.presentation.screen.testpasseddetail.mapper.toUIState
import com.app.testik.presentation.screen.testpasseddetail.model.TestPassedDetailScreenEvent
import com.app.testik.presentation.screen.testpasseddetail.model.TestPassedDetailScreenUIState
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class TestPassedDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestInfoUseCase: GetTestInfoUseCase,
    private val getTestPassedInfoUseCase: GetTestPassedInfoUseCase,
    private val getTestPassedQuestionsUseCase: GetTestPassedQuestionsUseCase,
    private val getTestResultsUseCase: GetTestResultsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestPassedDetailScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestPassedDetailScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestPassedDetailScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestPassedDetailScreenEvent>()

    private val args = TestPassedDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestPassedDetailScreenUIState(recordId = args.recordId)
        private set

    init {
        getRecordInfo()
    }

    fun updateList(isFirstUpdate: Boolean = false) {
        if (!isFirstUpdate && screenUIState.questions.isNotEmpty()) return
        postItem(LoadingItem)

        viewModelScope.launch {
            getTestPassedQuestionsUseCase(screenUIState.recordId).onSuccess { list ->
                var questions = list.map { question -> question.toQuestionItem() }
                questions = updateResults(questions)
                postListItems(questions)
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    private fun getRecordInfo() {
        emitEvent(TestPassedDetailScreenEvent.Loading)

        viewModelScope.launch {
            getTestPassedInfoUseCase(recordId = screenUIState.recordId, source = Source.CACHE).onSuccess {
                screenUIState = it.toUIState()
                getTestInfo()
            }.onError {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getTestInfo() {
        emitEvent(TestPassedDetailScreenEvent.Loading)

        viewModelScope.launch {
            getTestInfoUseCase(testId = screenUIState.testId).onSuccess {
                screenUIState = screenUIState.copy(
                    title = it.title,
                    image = it.image
                )
                getResults()
            }.onError {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getResults() {
        viewModelScope.launch {
            getTestResultsUseCase(recordId = screenUIState.recordId).onSuccess {
                screenUIState = screenUIState.copy(results = it)
                updateList(isFirstUpdate = true)
            }.onError {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun updateResults(questions: List<QuestionDelegateItem>): List<QuestionDelegateItem> {
        val results = screenUIState.results ?: return questions

        if (results.answersCorrect.size < questions.size || results.pointsPerQuestion.size < questions.size) {
            emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.error_while_loading_results))
            return questions
        }

        val newList = mutableListOf<QuestionDelegateItem>()
        for ((index, question) in questions.withIndex()) {
            newList.add(
                question.copy(
                    answers = question.answers.mapIndexed { itemIndex, item ->
                        item.copy(isCorrect = results.answersCorrect[index][itemIndex].isCorrect)
                    },
                    pointsEarned = results.pointsPerQuestion[index]
                )
            )
        }
        return newList
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

    private fun updateScreenState(state: TestPassedDetailScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestPassedDetailScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}