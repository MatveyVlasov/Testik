package com.app.testik.presentation.screen.questionmain

import android.os.CountDownTimer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.*
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.mapper.toDomain
import com.app.testik.presentation.mapper.toQuestionItem
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.model.answer.MatchingDelegateItem
import com.app.testik.presentation.model.answer.OrderingDelegateItem
import com.app.testik.presentation.model.answer.ShortAnswerDelegateItem
import com.app.testik.presentation.model.answer.copyCorrect
import com.app.testik.presentation.model.copy
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenEvent
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenUIState
import com.app.testik.util.toIntOrZero
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
    private val getTestPassedQuestionsUseCase: GetTestPassedQuestionsUseCase,
    private val updateAnswersUseCase: UpdateAnswersUseCase,
    private val finishTestUseCase: FinishTestUseCase,
    private val submitQuestionUseCase: SubmitQuestionUseCase,
    private val calculatePointsUseCase: CalculatePointsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<QuestionMainScreenUIState>>
        get() = _uiState

    val timeLeft: StateFlow<Long>
        get() = _timeLeft

    val event: SharedFlow<QuestionMainScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<QuestionMainScreenUIState>>(UIState.Loading)
    private val _timeLeft = MutableStateFlow<Long>(0L)
    private val _event = MutableSharedFlow<QuestionMainScreenEvent>()

    private val args = QuestionMainFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = QuestionMainScreenUIState(test = args.test)
        private set

    val testToInsert: TestPassedModel = args.test

    private var timer: CountDownTimer? = null

    init {
        getQuestions()
        initTimer()
    }

    fun navigateToResultsOnFinish() {
        updateScreenState(screenUIState.copy(navigateToResultsOnFinish = true))
    }

    fun onTimerFinishedHandledChanged(isHandled: Boolean) {
        updateScreenState(screenUIState.copy(isTimerFinishedHandled = isHandled))
    }

    fun updateAnswers(question: Int, answers: List<AnswerDelegateItem>, enteredAnswer: String) {
        val questions = screenUIState.questions.toMutableList().also {
            it[question] = it[question].copy(answers = answers, enteredAnswer = enteredAnswer)
        }
        screenUIState = screenUIState.copy(questions = questions)
    }

    fun submit() {
        if (screenUIState.test.isNavigationEnabled) saveAnswers(showInfo = true)
        else if (screenUIState.isReviewQuestionMode) navigateToNextQuestion()
        else submitQuestion()
    }

    fun saveAnswers(showInfo: Boolean = false, isExiting: Boolean = false) {
        if (screenUIState.isTimerEnabled && timeLeft.value == 0L) return
        if (showInfo || isExiting) emitEvent(QuestionMainScreenEvent.Loading)
        viewModelScope.launch {
            updateAnswersUseCase(
                recordId = screenUIState.test.recordId,
                questions = screenUIState.questions.map { it.toDomain() }
            ).onSuccess {
                if (showInfo) emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.draft_saved))
                if (isExiting) emitEvent(QuestionMainScreenEvent.NavigateToTestsPassed)
            }.onError {
                handleError(it)
            }
        }
    }

    fun finish(isTimerFinished: Boolean = false) {
        updateScreenState(screenUIState.copy(isNavigationToResultsAllowed = false))
        emitEvent(QuestionMainScreenEvent.Loading)
        timer?.cancel()

        viewModelScope.launch {
            finishTestUseCase(
                recordId = screenUIState.test.recordId,
                questions = screenUIState.questions.map { it.toDomain() },
                isTimerFinished = isTimerFinished
            ).onSuccess {
                updateScreenState(screenUIState.copy(isNavigationToResultsAllowed = true))
                if (!isTimerFinished || screenUIState.navigateToResultsOnFinish) {
                    emitEvent(QuestionMainScreenEvent.NavigateToResults)
                }
            }.onError {
                updateScreenState(screenUIState.copy(isNavigationToResultsAllowed = true))
                handleError(error = it, isTimerFinished = isTimerFinished)
            }
        }
    }

    fun submitQuestion(isTimerFinished: Boolean = false) {
        emitEvent(QuestionMainScreenEvent.Loading)
        if (screenUIState.isTimerQuestionEnabled) timer?.cancel()

        viewModelScope.launch {
            if (screenUIState.isReviewQuestionMode) {
                navigateToNextQuestion()
                emitEvent(QuestionMainScreenEvent.Loading)
            }
            val num = screenUIState.currentQuestion
            submitQuestionUseCase(
                recordId = screenUIState.test.recordId,
                question = screenUIState.questions[num].toDomain(),
                num = num,
                isTimerFinished = isTimerFinished
            ).onSuccess {
                if (it.isLateSubmission) emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.too_late_not_accepted))
                if (screenUIState.isTimerQuestionEnabled && num < screenUIState.questions.lastIndex) initTimer()
                if (it.answersCorrect.isNotEmpty()) updateResults(it)
                else navigateToNextQuestion()
            }.onError {
                handleError(it)
            }
        }
    }

    private fun getQuestions() {
        args.questions?.let {
            updateScreenState(
                screenUIState.copy(
                    questions = it.toList(),
                    isReviewMode = true,
                    startQuestion = args.startQuestion
                )
            )
            return
        }

        emitEvent(QuestionMainScreenEvent.Loading)
        viewModelScope.launch {
            getTestPassedQuestionsUseCase(screenUIState.test.recordId).onSuccess { list ->
                updateScreenState(screenUIState.copy(questions = list.map { it.toQuestionItem() }))
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String, isTimerFinished: Boolean = false) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("not logged in") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.not_logged_in))
            }
            msg.contains("no access") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.no_access))
            }
            msg.contains("test not found") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.test_not_found))
            }
            msg.contains("test already finished") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.test_already_finished))
            }
            msg.contains("too late submit") -> {
                emitEvent(QuestionMainScreenEvent.TooLate)
            }
            msg.contains("too late calculate") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.too_late))
            }
            msg.contains("too late") -> {
                calculatePoints(isTimerFinished = isTimerFinished)
            }
            msg.contains("incorrect number") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.incorrect_questions_number))
            }
            msg.contains("invalid data type") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.invalid_data_type))
            }
            msg.contains("incorrect question number") -> {
                emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.incorrect_question_number))
            }
            msg.contains("should be answered") -> {
                if (':' !in msg) {
                    emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.question_unanswered_current))
                } else {
                    val questionNum = msg.takeWhile { c -> c != ':' }.toIntOrZero()
                    emitEvent(QuestionMainScreenEvent.UnansweredQuestion(questionNum))
                }
            }
            else -> emitEvent(QuestionMainScreenEvent.ShowSnackbar(error))
        }
    }

    private fun calculatePoints(isTimerFinished: Boolean = false) {
        emitEvent(QuestionMainScreenEvent.Loading)

        viewModelScope.launch {
            calculatePointsUseCase(recordId = screenUIState.test.recordId).onSuccess {
                if (!isTimerFinished || screenUIState.navigateToResultsOnFinish) {
                    if (!isTimerFinished) emitEvent(QuestionMainScreenEvent.ShowSnackbarByRes(R.string.no_time_left))
                    emitEvent(QuestionMainScreenEvent.NavigateToResults)
                }
            }.onError {
                handleError(it)
            }
        }
    }

    private fun navigateToNextQuestion() {
        val pos = screenUIState.currentQuestion + 1
        if (pos > screenUIState.questions.lastIndex) {
            emitEvent(QuestionMainScreenEvent.NavigateToResults)
            return
        }
        updateScreenState(screenUIState.copy(isReviewQuestionMode = false, currentQuestion = pos))
        emitEvent(QuestionMainScreenEvent.NavigateToQuestion(pos))
    }

    private fun updateResults(results: AnswerResultsModel) {
        val num = screenUIState.currentQuestion
        val question = screenUIState.questions[num].copy(
            explanation = results.explanation,
            pointsEarned = results.points
        )

        val newQuestion = when (question.type) {
            QuestionType.SHORT_ANSWER -> {
                question.copy(
                    answers = results.answersCorrect.map { ShortAnswerDelegateItem(text = it.text) }
                )
            }
            QuestionType.MATCHING -> {
                question.copy(
                    answers = question.answers.mapIndexed { itemIndex, item ->
                        (item as MatchingDelegateItem).copyCorrect(textCorrect = results.answersCorrect[itemIndex].textMatching)
                    }
                )
            }
            QuestionType.ORDERING -> {
                question.copy(
                    answers = question.answers.mapIndexed { itemIndex, item ->
                        (item as OrderingDelegateItem).copyCorrect(textCorrect = results.answersCorrect[itemIndex].text)
                    }
                )
            }
            QuestionType.NUMBER -> {
                question.copy(correctNumber = results.answersCorrect[0].text.toDouble())
            }
            else -> {
                question.copy(
                    answers = question.answers.mapIndexed { itemIndex, item ->
                        item.copy(isCorrect = results.answersCorrect[itemIndex].isCorrect)
                    },
                )
            }
        }

        updateQuestion(pos = num, newQuestion = newQuestion, isReviewQuestionMode = true)
    }

    private fun updateQuestion(pos: Int, newQuestion: QuestionDelegateItem, isReviewQuestionMode: Boolean = false) {
        val questions = screenUIState.questions.map { it }.toMutableList().also {
            it[pos] = newQuestion
        }

        updateScreenState(screenUIState.copy(questions = questions, isReviewQuestionMode = isReviewQuestionMode))
    }

    private fun initTimer() {
        if (screenUIState.isReviewMode) return
        val timeLimit = screenUIState.test.timeLimitQuestion.let {
            if (it > 0) it else screenUIState.test.timeLimit
        }

        updateScreenState(
            screenUIState.copy(
                isTimerEnabled = screenUIState.test.timeLimit > 0,
                isTimerQuestionEnabled = screenUIState.test.timeLimitQuestion > 0,
                timeLimit = timeLimit
            )
        )
        if (!screenUIState.isTimerEnabled && !screenUIState.isTimerQuestionEnabled) return

        timer = object : CountDownTimer(screenUIState.timeLimit, COUNTDOWN_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                _timeLeft.value = millisUntilFinished
            }

            override fun onFinish() {
                onTimerFinishedHandledChanged(isHandled = false)
                emitEvent(QuestionMainScreenEvent.TimerFinished)
            }
        }

        timer?.start()
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

    companion object {
        const val COUNTDOWN_INTERVAL = 1000L
    }
}