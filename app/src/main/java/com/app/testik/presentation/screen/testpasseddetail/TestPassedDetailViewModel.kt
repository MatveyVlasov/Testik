package com.app.testik.presentation.screen.testpasseddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.*
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.mapper.toQuestionItem
import com.app.testik.presentation.model.*
import com.app.testik.presentation.model.answer.MatchingDelegateItem
import com.app.testik.presentation.model.answer.OrderingDelegateItem
import com.app.testik.presentation.model.answer.ShortAnswerDelegateItem
import com.app.testik.presentation.model.answer.copyCorrect
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
    private val getTestResultsUseCase: GetTestResultsUseCase,
    private val calculatePointsUseCase: CalculatePointsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestPassedDetailScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestPassedDetailScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestPassedDetailScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestPassedDetailScreenEvent>()

    private val args = TestPassedDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestPassedDetailScreenUIState(recordId = args.recordId, username = args.username)
        private set

    var testPassed: TestPassedModel? = null

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
            getTestPassedInfoUseCase(recordId = screenUIState.recordId, source = Source.DEFAULT).onSuccess {
                testPassed = it
                screenUIState = it.toUIState().copy(username = screenUIState.username)
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
                    image = it.image
                )
                getResults()
            }.onError {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getResults() {
        if (!screenUIState.isFinished && !screenUIState.pointsCalculated) return calculatePoints()

        viewModelScope.launch {
            getTestResultsUseCase(recordId = screenUIState.recordId).onSuccess {
                screenUIState = screenUIState.copy(results = it)
                updateList(isFirstUpdate = true)
            }.onError {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun calculatePoints() {
        emitEvent(TestPassedDetailScreenEvent.Loading)

        viewModelScope.launch {
            calculatePointsUseCase(recordId = screenUIState.recordId).onSuccess {
                screenUIState = screenUIState.copy(
                    pointsCalculated = true,
                    pointsEarned = it.pointsEarned,
                    gradeEarned = it.gradeEarned
                )
                getResults()
            }.onError {
                handleError(it)
                updateScreenState(screenUIState)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("not logged in") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.not_logged_in))
            }
            msg.contains("test not found") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.test_not_found))
            }
            msg.contains("points already calculated") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.points_already_calculated))
            }
            msg.contains("test already finished") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.test_already_finished))
            }
            msg.contains("incorrect number") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.incorrect_questions_number))
            }
            msg.contains("invalid data type") -> {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.invalid_data_type))
            }
            else -> emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(error))
        }
    }

    private fun updateResults(questions: List<QuestionDelegateItem>): List<QuestionDelegateItem> {
        val results = screenUIState.results ?: return questions

        if (results.answersCorrect.size < questions.size || results.explanations.size < questions.size) {
            emitEvent(TestPassedDetailScreenEvent.ShowSnackbarByRes(R.string.error_while_loading_results))
            return questions
        }

        val newList = mutableListOf<QuestionDelegateItem>()
        for ((index, question) in questions.withIndex()) {
            val newQuestion = when (question.type) {
                QuestionType.SHORT_ANSWER -> {
                    question.copy(
                        answers = results.answersCorrect[index].map { ShortAnswerDelegateItem(text = it.text) }
                    )
                }
                QuestionType.MATCHING -> {
                    question.copy(
                        answers = question.answers.mapIndexed { itemIndex, item ->
                            (item as MatchingDelegateItem).copyCorrect(textCorrect = results.answersCorrect[index][itemIndex].textMatching)
                        }
                    )
                }
                QuestionType.ORDERING -> {
                    question.copy(
                        answers = question.answers.mapIndexed { itemIndex, item ->
                            (item as OrderingDelegateItem).copyCorrect(textCorrect = results.answersCorrect[index][itemIndex].text)
                        }
                    )
                }
                QuestionType.NUMBER -> {
                    question.copy(correctNumber = results.answersCorrect[index][0].text.toDouble())
                }
                else -> {
                    question.copy(
                        answers = question.answers.mapIndexed { itemIndex, item ->
                            item.copy(isCorrect = results.answersCorrect[index][itemIndex].isCorrect)
                        }
                    )
                }
            }

            newList.add(
                newQuestion.copy(
                    pointsEarned = results.pointsPerQuestion.getOrElse(index) { 0 },
                    explanation = results.explanations[index]
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