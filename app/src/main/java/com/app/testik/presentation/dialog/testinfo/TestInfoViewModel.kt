package com.app.testik.presentation.dialog.testinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.dialog.testinfo.mapper.toDomain
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogEvent
import com.app.testik.presentation.dialog.testinfo.model.TestInfoDialogUIState
import com.app.testik.presentation.model.UIState
import com.app.testik.util.getFullName
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestInfoUseCase: GetTestInfoUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val startTestUseCase: StartTestUseCase,
) : ViewModel() {

    val uiState: StateFlow<UIState<TestInfoDialogUIState>>
        get() = _uiState

    val event: SharedFlow<TestInfoDialogEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestInfoDialogUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestInfoDialogEvent>()

    private val args = TestInfoFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestInfoDialogUIState(id = args.testId, isDemo = args.isDemo)
        private set

    private var job: Job? = null

    init {
        getTestInfo(source = Source.CACHE)
    }

    fun onPasswordChanged(password: String) {
        if (password == screenUIState.password) return
        updateScreenState(screenUIState.copy(password = password))
    }

    fun startTest() {
        if (job?.isActive == true) return
        if (screenUIState.questionsNum < 1) {
            emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.no_questions))
            return
        }
        if (!validateData()) return

        emitEvent(TestInfoDialogEvent.Loading)

        job = viewModelScope.launch {
            startTestUseCase(data = screenUIState.toDomain(), password = screenUIState.password).onSuccess {
                emitEvent(TestInfoDialogEvent.SuccessTestCreation(it))
            }.onError {
                handleError(it)
            }
        }
    }

    private fun getTestInfo(source: Source = Source.DEFAULT) {
        if (screenUIState.id.isEmpty()) return
        _uiState.value = UIState.Loading

        viewModelScope.launch {
            getTestInfoUseCase(testId = screenUIState.id, source = source).onSuccess {
                val screenState = screenUIState.copy(
                    image = it.image,
                    title = it.title,
                    author = it.author,
                    description = it.description,
                    category = it.category,
                    questionsNum = it.questionsNum,
                    pointsMax = it.pointsMax,
                    isPasswordEnabled = it.isPasswordEnabled
                )
                updateScreenState(screenState)

                getAuthorInfo(it.author)
            }.onError {
                emitEvent(TestInfoDialogEvent.ShowSnackbar(it))
            }
        }
    }

    private fun getAuthorInfo(author: String) {
        viewModelScope.launch {
            getUserInfoUseCase(uid = author).onSuccess {
                updateScreenState(screenUIState.copy(authorName = it.getFullName()))
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("not logged in") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.not_logged_in))
            }
            msg.contains("no access") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.no_access))
            }
            msg.contains("incorrect password") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.incorrect_password))
            }
            msg.contains("test not found") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.test_not_found))
            }
            msg.contains("no questions") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.no_questions))
            }
            msg.contains("invalid data type") -> {
                emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.invalid_data_type))
            }
            else -> emitEvent(TestInfoDialogEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        return checkPasswordNotEmpty()
    }

    private fun checkPasswordNotEmpty(): Boolean {
        return (!screenUIState.isPasswordEnabled || screenUIState.password.isNotEmpty()).also {
            if (!it) emitEvent(TestInfoDialogEvent.ShowSnackbarByRes(R.string.empty_test_password))
        }
    }

    private fun updateScreenState(state: TestInfoDialogUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestInfoDialogEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}