package com.app.testik.presentation.screen.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.*
import com.app.testik.presentation.model.answer.*
import com.app.testik.presentation.screen.question.model.QuestionScreenEvent
import com.app.testik.presentation.screen.question.model.QuestionScreenUIState
import com.app.testik.util.removeExtraSpaces
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class QuestionViewModel @Inject constructor(

) : ViewModel() {

    val uiState: StateFlow<UIState<QuestionScreenUIState>>
        get() = _uiState

    val event: SharedFlow<QuestionScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<QuestionScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<QuestionScreenEvent>()

    var screenUIState = QuestionScreenUIState()
        private set

    fun updateQuestion(question: QuestionDelegateItem) {
        question.apply {
            var screenState = screenUIState.copy(
                id = id,
                testId = testId,
                title = title,
                description = description,
                explanation = explanation,
                image = image,
                type = type,
                isRequired = isRequired,
                answers = answers,
                enteredAnswer = enteredAnswer,
                isMatch = isMatch,
                isCaseSensitive = isCaseSensitive,
                pointsMax = pointsMax,
                pointsEarned = pointsEarned
            )

            if (type == QuestionType.MATCHING) {
                val answersMatching = mutableListOf<AnswerDelegateItem>()

                screenState.answers.filterIsInstance<MatchingDelegateItem>().forEach {
                    answersMatching.add(MatchingLeftDelegateItem(text = it.text))
                    answersMatching.add(MatchingRightDelegateItem(textMatching = it.textMatching, textCorrect = it.textCorrect))
                }

                screenState = screenState.copy(answersMatching = answersMatching)
            }
            updateScreenState(screenState)
        }
    }

    fun onAnswerChanged(enteredAnswer: String) {
        if (enteredAnswer == screenUIState.enteredAnswer) return
        updateScreenState(screenUIState.copy(enteredAnswer = enteredAnswer.removeExtraSpaces()))
    }

    fun onSelectClick(answer: SingleChoiceDelegateItem) {
        val answers = mutableListOf<AnswerDelegateItem>()

        screenUIState.answers.filterIsInstance<SingleChoiceDelegateItem>().forEach {
            answers.add(it.copy(isSelected = it.id == answer.id))
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun onSelectClick(answer: MultipleChoiceDelegateItem, isChecked: Boolean) {
        val pos = screenUIState.answers.indexOf(answer)
        if (pos == -1) return
        val answers = screenUIState.answers.map { it }.toMutableList().also {
            it[pos] = (it[pos] as MultipleChoiceDelegateItem).copy(isSelected = isChecked)
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun moveAnswer(from: Int, to: Int) {
        if (from % 2 == 0 || to % 2 == 0) return // left column not movable
        val answersMatching = screenUIState.answersMatching.map { it }.toMutableList().also {
            val item = it[from]
            it[from] = it[to]
            it[to] = item
        }

        val answers = screenUIState.answers.map { it }.filterIsInstance<MatchingDelegateItem>().toMutableList().also {
            val from2 = from / 2
            val to2 = to / 2
            val item = it[from2]

            it[from2] = it[from2].copy(textMatching = it[to2].textMatching)
            it[to2] = it[to2].copy(textMatching = item.textMatching)
        }
        updateScreenState(screenUIState.copy(answersMatching = answersMatching, answers = answers))
    }

    private fun updateScreenState(state: QuestionScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: QuestionScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}