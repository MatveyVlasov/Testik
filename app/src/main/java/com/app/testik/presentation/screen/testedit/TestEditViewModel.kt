package com.app.testik.presentation.screen.testedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testedit.model.TestEditScreenEvent
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.Constants
import com.app.testik.util.loadedFromServer
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
    private val getCurrentUserInfoUseCase: GetCurrentUserInfoUseCase,
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
        updateScreenState(screenUIState.copy(title = title))
    }

    fun onDescriptionChanged(description: String) {
        if (description == screenUIState.description) return
        updateScreenState(screenUIState.copy(description = description, descriptionError = null))
    }

    fun onCategoryChanged(category: String) {
        if (category == screenUIState.category) return
        updateScreenState(screenUIState.copy(category = category))
    }

    fun getTestInfo(testId: Int, source: Source = Source.DEFAULT) {
        if (testId == -1) return
        emitEvent(TestEditScreenEvent.Loading)

        viewModelScope.launch {
//            getCurrentUserInfoUseCase(source).onSuccess {
//                val screenState = TestEditScreenUIState(
//                    email = it.email,
//                    username = it.username,
//                    firstName = it.firstName,
//                    lastName = it.lastName,
//                    avatar = it.avatar
//                )
//                oldScreenUIState = screenState
//                updateScreenState(state = screenState)
//            }.onError {
//                emitEvent(TestEditScreenEvent.ShowSnackbar(it))
//            }
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

        val imageUpdated = !screenUIState.image.loadedFromServer()

        viewModelScope.launch {
//            updateUserUseCase(screenUIState.toDomain()).onSuccess {
//                val screenState = screenUIState.copy(avatarUpdated = avatarUpdated)
//                oldScreenUIState = screenState
//                updateScreenState(screenState)
//                emitEvent(TestEditEvent.ShowSnackbarByRes(R.string.saved))
//            }.onError {
//                handleError(it)
//            }
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
        return checkDescriptionLength()
    }

    private fun checkDescriptionLength(): Boolean {
        return (screenUIState.description.length <= Constants.MIN_PASSWORD_LENGTH).also {
            val error = if (it) null else R.string.description_too_long
            updateScreenState(screenUIState.copy(descriptionError = error))
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