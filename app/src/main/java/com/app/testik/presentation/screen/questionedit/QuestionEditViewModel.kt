package com.app.testik.presentation.screen.questionedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.answer.SingleChoiceDelegateItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.model.copy
import com.app.testik.presentation.model.answer.MultipleChoiceDelegateItem
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenEvent
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState
import com.app.testik.util.Constants.MAX_DESCRIPTION_LENGTH
import com.app.testik.util.removeExtraSpaces
import com.app.testik.util.removeExtraSpacesAndBreaks
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,

) : ViewModel() {

    val uiState: StateFlow<UIState<QuestionEditScreenUIState>>
        get() = _uiState

    val event: SharedFlow<QuestionEditScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<QuestionEditScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<QuestionEditScreenEvent>()

    private val args = QuestionEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = QuestionEditScreenUIState()
        private set

    private var oldScreenUIState: QuestionEditScreenUIState = screenUIState.copy()

    init {
        args.question.apply {
            val screenState = screenUIState.copy(
                id = id,
                testId = testId,
                title = title,
                description = description,
                points = pointsMax.toString(),
                image = image,
                type = type,
                answers = answers
            )
            oldScreenUIState = screenState
            updateScreenState(screenState)
        }
    }

    fun onTitleChanged(title: String) {
        if (title == screenUIState.title) return
        updateScreenState(screenUIState.copy(title = title.removeExtraSpacesAndBreaks(), titleError = null))
    }

    fun onDescriptionChanged(description: String) {
        if (description == screenUIState.description) return
        updateScreenState(screenUIState.copy(description = description, descriptionError = null))
    }

    fun onPointsChanged(points: String) {
        if (points == screenUIState.points) return
        updateScreenState(screenUIState.copy(points = points))
    }

    fun onTypeChanged(type: QuestionType) {
        if (type == screenUIState.type) return

        val answers =
            if (screenUIState.type == QuestionType.SINGLE_CHOICE && type == QuestionType.MULTIPLE_CHOICE) {
                screenUIState.answers.filterIsInstance<SingleChoiceDelegateItem>().map {
                    MultipleChoiceDelegateItem(id = it.id, text = it.text, isCorrect = it.isCorrect)
                }
            } else if (screenUIState.type == QuestionType.MULTIPLE_CHOICE && type == QuestionType.SINGLE_CHOICE) {
                screenUIState.answers.filterIsInstance<MultipleChoiceDelegateItem>().map {
                    SingleChoiceDelegateItem(id = it.id, text = it.text, isCorrect = false)
                }
            } else {
                emptyList()
            }


        updateScreenState(screenUIState.copy(type = type, answers = answers))
    }

    fun onAnswerTextChanged(answer: AnswerDelegateItem, text: String) {
        val item = screenUIState.answers.find { it.id == answer.id } ?: return
        if (text == item.text) return

        val pos = screenUIState.answers.indexOf(item)
        val answers = screenUIState.answers.map { it }.toMutableList().also {
            it[pos] = it[pos].copy(text = text.removeExtraSpaces())
        }
        screenUIState = screenUIState.copy(answers = answers, canDiscard = true)
        emitEvent(QuestionEditScreenEvent.EnableDiscardButton)
    }

    fun onSelectClick(answer: SingleChoiceDelegateItem) {
        val answers = mutableListOf<AnswerDelegateItem>()

        screenUIState.answers.filterIsInstance<SingleChoiceDelegateItem>().forEach {
            answers.add(it.copy(isCorrect = it.id == answer.id))
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun onSelectClick(answer: MultipleChoiceDelegateItem, isChecked: Boolean) {
        val item = screenUIState.answers.find { it.id == answer.id } ?: return
        val pos = screenUIState.answers.indexOf(item)

        val answers = screenUIState.answers.map { it }.toMutableList().also {
            it[pos] = (it[pos] as MultipleChoiceDelegateItem).copy(isCorrect = isChecked)
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun addAnswer() {
        val answers = screenUIState.answers.map { it }.toMutableList().also {
            it.add(
                when (screenUIState.type) {
                    QuestionType.MULTIPLE_CHOICE -> MultipleChoiceDelegateItem()
                    else -> SingleChoiceDelegateItem()
                }
            )
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun deleteAnswer(answer: AnswerDelegateItem) {
        val answers = screenUIState.answers.map { it }.toMutableList().also {
            it.removeIf { item -> item.id == answer.id }
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun moveAnswer(from: Int, to: Int) {
        if (from == to) return
        val answers = screenUIState.answers.map { it }.toMutableList().also {
            val item = it[from]
            it.removeAt(from)
            it.add(to, item)
        }
        updateScreenState(screenUIState.copy(answers = answers))
    }

    fun loadImage(image: String) {
        if (screenUIState.image == image) return
        updateScreenState(screenUIState.copy(image = image))
    }

    fun deleteImage() = loadImage("")

    fun discardChanges() {
        updateScreenState(screenUIState)
        viewModelScope.launch {
            delay(10)
            updateScreenState(oldScreenUIState)
        }
    }

    fun validateData(): Boolean {
        return checkDescriptionLength() && checkTitleNotBlank() && checkCorrectAnswers()
    }

    private fun checkDescriptionLength(): Boolean {
        return (screenUIState.description.length <= MAX_DESCRIPTION_LENGTH).also {
            if (!it) updateScreenState(screenUIState.copy(descriptionError = R.string.description_too_long))
        }
    }

    private fun checkTitleNotBlank(): Boolean {
        return (screenUIState.title.isNotBlank()).also {
            if (!it) updateScreenState(screenUIState.copy(titleError = R.string.blank_field_error))
        }
    }

    private fun checkCorrectAnswers(): Boolean {
        val hasCorrectAnswers = when (screenUIState.type) {
            QuestionType.SINGLE_CHOICE -> {
                screenUIState.answers.filterIsInstance<SingleChoiceDelegateItem>().any { it.isCorrect }
            }
            QuestionType.MULTIPLE_CHOICE -> {
                screenUIState.answers.filterIsInstance<MultipleChoiceDelegateItem>().any { it.isCorrect }
            }
        }
        return (hasCorrectAnswers).also {
            if (!it) emitEvent(QuestionEditScreenEvent.ShowSnackbarByRes(R.string.no_correct_answers))
        }
    }

    private fun updateScreenState(state: QuestionEditScreenUIState) {
        screenUIState = state.copy(canDiscard = state != oldScreenUIState)
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: QuestionEditScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}