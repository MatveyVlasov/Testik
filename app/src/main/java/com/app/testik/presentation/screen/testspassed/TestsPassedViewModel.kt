package com.app.testik.presentation.screen.testspassed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testspassed.mapper.toTestPassedItem
import com.app.testik.presentation.screen.testspassed.model.TestsPassedScreenEvent
import com.app.testik.presentation.screen.testspassed.model.TestsPassedScreenUIState
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

import javax.inject.Inject

@HiltViewModel
class TestsPassedViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserPassedTestsUseCase: GetCurrentUserPassedTestsUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestsPassedScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestsPassedScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestsPassedScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestsPassedScreenEvent>()

    private var screenUIState = TestsPassedScreenUIState()

    private var user: FirebaseUser? = null
    private var snapshot: QuerySnapshot? = null
    private var job: Job? = null

    init {
        updateList()
    }

    fun checkUser() {
        viewModelScope.launch {
            val newUser = getCurrentUserUseCase()
            if (user != newUser) {
                screenUIState = TestsPassedScreenUIState()
                snapshot = null
                user = newUser
                updateList()
            }
        }
    }

    fun updateList() {
        if (job?.isActive == true) return

        postItem(LoadingItem)

        job = viewModelScope.launch {
            getCurrentUserPassedTestsUseCase(snapshot).onSuccess {
                snapshot = it.snapshot
                postListItems(it.tests.map { test -> test.toTestPassedItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    private fun postItem(data: DelegateAdapterItem) = postListItems(listOf(data))

    private fun postListItems(data: List<DelegateAdapterItem>) {
        updateScreenState(
            screenUIState.copy(
                tests = screenUIState.tests.toMutableList().apply {
                    removeAll { it is LoadingItem || it is ErrorItem }
                    addAll(data)
                }
            )
        )
    }

    private fun updateScreenState(state: TestsPassedScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestsPassedScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}