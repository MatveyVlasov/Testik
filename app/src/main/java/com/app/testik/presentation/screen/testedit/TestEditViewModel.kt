package com.app.testik.presentation.screen.testedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.CategoryType
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testedit.mapper.toDomain
import com.app.testik.presentation.screen.testedit.model.TestEditScreenEvent
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.Constants.MAX_DESCRIPTION_LENGTH
import com.app.testik.util.removeExtraSpacesAndBreaks
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val createTestUseCase: CreateTestUseCase,
    private val updateTestUseCase: UpdateTestUseCase,
    private val getTestInfoUseCase: GetTestInfoUseCase,
    private val deleteTestUseCase: DeleteTestUseCase,
    private val updateTestPasswordUseCase: UpdateTestPasswordUseCase,
    private val getTestPasswordUseCase: GetTestPasswordUseCase
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

    private var oldScreenUIState: TestEditScreenUIState = screenUIState.copy()

    init {
        if (screenUIState.id.isEmpty()) updateScreenState(screenUIState)
        getTestInfo(source = Source.CACHE)
    }

    fun onTitleChanged(title: String) {
        if (title == screenUIState.title) return
        updateScreenState(screenUIState.copy(title = title.removeExtraSpacesAndBreaks(), titleError = null))
    }

    fun onDescriptionChanged(description: String) {
        if (description == screenUIState.description) return
        updateScreenState(screenUIState.copy(description = description, descriptionError = null))
    }

    fun onPasswordChanged(password: String) {
        if (password == screenUIState.password) return
        updateScreenState(screenUIState.copy(password = password))
    }

    fun onCategoryChanged(category: CategoryType) {
        if (category == screenUIState.category) return
        updateScreenState(screenUIState.copy(category = category, categoryError = null))
    }

    fun onPublishChanged(isPublished: Boolean) {
        if (isPublished == screenUIState.isPublished) return
        updateScreenState(screenUIState.copy(isPublished = isPublished))
    }

    fun onTestLinkEnabledChanged(isTestLinkEnabled: Boolean) {
        if (isTestLinkEnabled == screenUIState.isTestLinkEnabled) return
        updateScreenState(screenUIState.copy(isTestLinkEnabled = isTestLinkEnabled))
    }

    fun onRandomQuestionsChanged(isRandomQuestions: Boolean) {
        if (isRandomQuestions == screenUIState.isRandomQuestions) return
        updateScreenState(screenUIState.copy(isRandomQuestions = isRandomQuestions))
    }

    fun onRandomAnswersChanged(isRandomAnswers: Boolean) {
        if (isRandomAnswers == screenUIState.isRandomAnswers) return
        updateScreenState(screenUIState.copy(isRandomAnswers = isRandomAnswers))
    }

    fun onQuestionsNumChanged(questionsNum: Int) {
        if (questionsNum == screenUIState.questionsNum) return
        val state = screenUIState.copy(questionsNum = questionsNum)
        oldScreenUIState = state
        updateScreenState(state)
    }

    fun onShowMore() {
        oldScreenUIState = oldScreenUIState.copy(showMore = true)
        updateScreenState(screenUIState.copy(showMore = true))
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

    fun discardChanges() {
        updateScreenState(oldScreenUIState)
    }

    fun deleteTest() {
        if (screenUIState.id.isEmpty()) return
        emitEvent(TestEditScreenEvent.Loading)

        viewModelScope.launch {
            deleteTestUseCase(testId = screenUIState.id).onSuccess {
                emitEvent(TestEditScreenEvent.SuccessTestDeletion(screenUIState.toDomain()))
            }.onError {
                emitEvent(TestEditScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getTestInfo(source: Source = Source.DEFAULT, isUpdated: Boolean = false) {
        if (screenUIState.id.isEmpty()) return
        emitEvent(TestEditScreenEvent.Loading)

        viewModelScope.launch {
            getTestInfoUseCase(testId = screenUIState.id, source = source).onSuccess {
                var screenState = screenUIState.copy(
                    title = it.title,
                    description = it.description,
                    category = it.category,
                    image = it.image,
                    isPublished = it.isPublished,
                    isTestLinkEnabled = it.isLinkEnabled,
                    testLink = it.link,
                    isRandomQuestions = it.isRandomQuestions,
                    isRandomAnswers = it.isRandomAnswers,
                    questionsNum = it.questionsNum,
                )
                if (isUpdated) screenState = screenState.copy(testUpdated = screenState.toDomain())
                else getPassword()

                oldScreenUIState = screenState
                updateScreenState(screenState)
            }.onError {
                emitEvent(TestEditScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun createTest() {
        viewModelScope.launch {
            createTestUseCase(screenUIState.toDomain()).onSuccess {
                if (screenUIState.password.isNotEmpty()) updatePassword()

                var screenState = screenUIState.copy(id = it.id, testLink = it.link)
                screenState = screenState.copy(testUpdated = screenState.toDomain())
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
                updatePassword()
                getTestInfo(isUpdated = true)
                emitEvent(TestEditScreenEvent.ShowSnackbarByRes(R.string.saved))
            }.onError {
                handleError(it)
            }
        }
    }

    private fun updatePassword() {
        viewModelScope.launch {
            updateTestPasswordUseCase(testId = screenUIState.id, password = screenUIState.password).onError {
                handleError(it)
            }
        }
    }

    private fun getPassword() {
        viewModelScope.launch {
            getTestPasswordUseCase(testId = screenUIState.id).onSuccess {
                val screenState = screenUIState.copy(password = it)
                oldScreenUIState = screenState
                updateScreenState(screenState)
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
        return checkDescriptionLength() && checkTitleNotBlank() && checkCategoryNotBlank() && checkHasQuestions()
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
        return (screenUIState.category != CategoryType.NOT_SELECTED).also {
            if (!it) updateScreenState(screenUIState.copy(categoryError = R.string.blank_field_error))
        }
    }

    private fun checkHasQuestions(): Boolean {
        return (!screenUIState.isPublished || screenUIState.questionsNum > 0).also {
            if (!it) emitEvent(TestEditScreenEvent.ShowSnackbarByRes(R.string.publish_no_questions))
        }
    }

    private fun updateScreenState(state: TestEditScreenUIState) {
        screenUIState = state.copy(canSave = state != oldScreenUIState.copy(canSave = state.canSave))
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestEditScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}