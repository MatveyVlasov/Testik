package com.app.testik.presentation.screen.questlionlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.questlionlist.mapper.toQuestionItem
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenEvent
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenUIState
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class QuestionListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestQuestionsUseCase: GetTestQuestionsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<QuestionListScreenUIState>>
        get() = _uiState

    val event: SharedFlow<QuestionListScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<QuestionListScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<QuestionListScreenEvent>()

    private val args = QuestionListFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private var screenUIState = QuestionListScreenUIState(testId = args.testId)

    init {
        updateList()
    }

    fun updateList() {
        postItem(LoadingItem)

        viewModelScope.launch {
            getTestQuestionsUseCase(screenUIState.testId).onSuccess {
                postListItems(it.map { question-> question.toQuestionItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

//    fun updateTest(test: CreatedTestDelegateItem, newTest: CreatedTestDelegateItem) {
//        val pos = screenUIState.tests.indexOf(test)
//        screenUIState.tests[pos] = newTest
//    }
//
//    fun deleteTestFromList(test: CreatedTestDelegateItem) {
//        screenUIState.tests.remove(test)
//    }
//
//    fun deleteTest(test: CreatedTestDelegateItem?) {
//        if (test == null) return
//        emitEvent(CreatedTestsScreenEvent.Loading)
//
//        viewModelScope.launch {
//            deleteTestUseCase(testId = test.id).onSuccess {
//                screenUIState.tests.remove(test)
//                emitEvent(CreatedTestsScreenEvent.SuccessTestDeletion(test))
//            }.onError {
//                emitEvent(CreatedTestsScreenEvent.ShowSnackbar(it))
//            }
//        }
//    }

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

    private fun updateScreenState(state: QuestionListScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: QuestionListScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}