package com.app.testik.presentation.screen.testedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testedit.mapper.toDomain
import com.app.testik.presentation.screen.testedit.model.TestEditScreenEvent
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.Constants.MAX_DESCRIPTION_LENGTH
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TestEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createTestUseCase: CreateTestUseCase,
    private val updateTestUseCase: UpdateTestUseCase,
    private val getTestInfoUseCase: GetTestInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestEditScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestEditScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestEditScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestEditScreenEvent>()

    private val args = TestEditFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestEditScreenUIState(id = args.testId)
        private set

    private var oldScreenUIState: TestEditScreenUIState? = null

    init {
        getTestInfo(testId = screenUIState.id)
    }

    fun onTitleChanged(title: String) {
        if (title == screenUIState.title) return
        updateScreenState(screenUIState.copy(title = title, titleError = null))
    }

    fun onDescriptionChanged(description: String) {
        if (description == screenUIState.description) return
        updateScreenState(screenUIState.copy(description = description, descriptionError = null))
    }

    fun onCategoryChanged(category: String) {
        if (category == screenUIState.category) return
        updateScreenState(screenUIState.copy(category = category, categoryError = null))
    }

    fun getTestInfo(testId: String, source: Source = Source.DEFAULT) {
        if (testId.isEmpty()) return
        emitEvent(TestEditScreenEvent.Loading)

        viewModelScope.launch {
            getTestInfoUseCase(screenUIState.id).onSuccess {
                val screenState = TestEditScreenUIState(
                    id = screenUIState.id,
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    image = it.image
                )
                oldScreenUIState = screenState
                updateScreenState(state = screenState)
            }.onError {
                emitEvent(TestEditScreenEvent.ShowSnackbar(it))
            }
        }
    }

    fun loadImage(image: String) {
        if (screenUIState.image == image) return
        updateScreenState(screenUIState.copy(image = image))
    }

    fun deleteImage() = loadImage("")

    fun save() {
        if (!validateData()) return
        emitEvent(TestEditScreenEvent.Loading)

        if (screenUIState.id.isEmpty()) createTest()
        else updateTest()
    }

    private fun createTest() {
        viewModelScope.launch {
            createTestUseCase(screenUIState.toDomain()).onSuccess {
                val screenState = screenUIState.copy(id = it, testUpdated = true)
                oldScreenUIState = screenState
                updateScreenState(screenState)
                emitEvent(TestEditScreenEvent.SuccessTestCreation)
            }.onError {
                handleError(it)
            }
        }
    }

    private fun updateTest() {
        viewModelScope.launch {
            updateTestUseCase(screenUIState.toDomain()).onSuccess {
                val screenState = screenUIState.copy(testUpdated = true)
                oldScreenUIState = screenState
                updateScreenState(screenState)
                emitEvent(TestEditScreenEvent.ShowSnackbarByRes(R.string.saved))
            }.onError {
                handleError(it)
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(TestEditScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(TestEditScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("error while saving image") -> {
                emitEvent(TestEditScreenEvent.ShowSnackbarByRes(R.string.error_while_saving_image))
            }
            else -> emitEvent(TestEditScreenEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        return checkDescriptionLength() && checkTitleNotBlank() && checkCategoryNotBlank()
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

    private fun checkCategoryNotBlank(): Boolean {
        return (screenUIState.category.isNotBlank()).also {
            if (!it) updateScreenState(screenUIState.copy(categoryError = R.string.blank_field_error))
        }
    }

    private fun updateScreenState(state: TestEditScreenUIState) {
        screenUIState = state.copy(canSave = state != oldScreenUIState)
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestEditScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}