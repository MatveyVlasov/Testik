package com.app.testik.presentation.screen.questionedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenEvent
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState
import com.app.testik.util.Constants.MAX_DESCRIPTION_LENGTH
import dagger.hilt.android.lifecycle.HiltViewModel
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

    private var oldScreenUIState: QuestionEditScreenUIState = screenUIState

    init {
        args.questionId.apply {
            val screenState = screenUIState.copy(
                id = id,
                testId = testId,
                title = title,
                description = description,
                image = image
            )
            oldScreenUIState = screenState
            updateScreenState(screenState)
        }
    }

    fun onTitleChanged(title: String) {
        if (title == screenUIState.title) return
        updateScreenState(screenUIState.copy(title = title, titleError = null))
    }

    fun onDescriptionChanged(description: String) {
        if (description == screenUIState.description) return
        updateScreenState(screenUIState.copy(description = description, descriptionError = null))
    }

    fun loadImage(image: String) {
        if (screenUIState.image == image) return
        updateScreenState(screenUIState.copy(image = image))
    }

    fun deleteImage() = loadImage("")

    fun discardChanges() {
        updateScreenState(oldScreenUIState)
    }

    fun validateData(): Boolean {
        return checkDescriptionLength() && checkTitleNotBlank()
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