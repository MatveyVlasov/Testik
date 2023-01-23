package com.app.testik.presentation.screen.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.model.answer.MultipleChoiceDelegateItem
import com.app.testik.presentation.model.answer.SingleChoiceDelegateItem
import com.app.testik.presentation.screen.question.model.QuestionScreenEvent
import com.app.testik.presentation.screen.question.model.QuestionScreenUIState
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
            val screenState = screenUIState.copy(
                id = id,
                testId = testId,
                title = title,
                description = description,
                explanation = explanation,
                image = image,
                type = type,
                isRequired = isRequired,
                answers = answers,
                pointsMax = pointsMax,
                pointsEarned = pointsEarned
            )
            updateScreenState(screenState)
        }
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