package com.app.testik.presentation.screen.testpasseddetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.GetTestInfoUseCase
import com.app.testik.domain.usecase.GetTestPassedInfoUseCase
import com.app.testik.domain.usecase.GetTestPassedQuestionsUseCase
import com.app.testik.presentation.mapper.toQuestionItem
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
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
    private val getTestPassedQuestionsUseCase: GetTestPassedQuestionsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestPassedDetailScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestPassedDetailScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestPassedDetailScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestPassedDetailScreenEvent>()

    private val args = TestPassedDetailFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestPassedDetailScreenUIState(recordId = args.recordId)
        private set

    init {
        getRecordInfo()
    }

    fun updateList(isFirstUpdate: Boolean = false) {
        if (!isFirstUpdate && screenUIState.questions.isNotEmpty()) return
        postItem(LoadingItem)

        viewModelScope.launch {
            getTestPassedQuestionsUseCase(screenUIState.recordId).onSuccess {
                postListItems(it.map { question -> question.toQuestionItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    private fun getRecordInfo() {
        emitEvent(TestPassedDetailScreenEvent.Loading)

        viewModelScope.launch {
            getTestPassedInfoUseCase(recordId = screenUIState.recordId, source = Source.CACHE).onSuccess {
                screenUIState = it.toUIState()
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
                    title = it.title,
                    image = it.image
                )
                updateList(isFirstUpdate = true)
            }.onError {
                emitEvent(TestPassedDetailScreenEvent.ShowSnackbar(it))
            }
        }
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