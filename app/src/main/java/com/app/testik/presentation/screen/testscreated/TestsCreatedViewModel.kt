package com.app.testik.presentation.screen.testscreated

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testscreated.mapper.toTestCreatedItem
import com.app.testik.presentation.screen.testscreated.model.TestCreatedDelegateItem
import com.app.testik.presentation.screen.testscreated.model.TestsCreatedScreenEvent
import com.app.testik.presentation.screen.testscreated.model.TestsCreatedScreenUIState
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
class TestsCreatedViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserCreatedTestsUseCase: GetCurrentUserCreatedTestsUseCase,
    private val deleteTestUseCase: DeleteTestUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestsCreatedScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestsCreatedScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestsCreatedScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestsCreatedScreenEvent>()

    private var screenUIState = TestsCreatedScreenUIState()

    private var user: FirebaseUser? = null
    private var snapshot: QuerySnapshot? = null
    private var job: Job? = null

    fun checkUser() {
        viewModelScope.launch {
            val newUser = getCurrentUserUseCase()
            if (user != newUser) {
                screenUIState = TestsCreatedScreenUIState()
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
            getCurrentUserCreatedTestsUseCase(snapshot).onSuccess {
                snapshot = it.snapshot
                postListItems(it.tests.map { test -> test.toTestCreatedItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    fun addTestToList(test: TestCreatedDelegateItem) {
        val tests = screenUIState.tests.map { it }.toMutableList().also {
            it.add(0, test)
        }
        updateScreenState(screenUIState.copy(tests = tests))
    }

    fun updateTest(test: TestCreatedDelegateItem, newTest: TestCreatedDelegateItem) {
        val pos = screenUIState.tests.indexOf(test)
        if (pos == -1) return
        val tests = screenUIState.tests.map { it }.toMutableList().also {
            it[pos] = newTest
        }
        updateScreenState(screenUIState.copy(tests = tests))
    }

    fun deleteTestFromList(test: TestCreatedDelegateItem) {
        val tests = screenUIState.tests.map { it }.toMutableList().also {
            it.remove(test)
        }
        updateScreenState(screenUIState.copy(tests = tests))
    }

    fun deleteTest(test: TestCreatedDelegateItem?) {
        if (test == null) return
        emitEvent(TestsCreatedScreenEvent.Loading)

        viewModelScope.launch {
            deleteTestUseCase(testId = test.id).onSuccess {
                deleteTestFromList(test)
                emitEvent(TestsCreatedScreenEvent.SuccessTestDeletion(test))
            }.onError {
                emitEvent(TestsCreatedScreenEvent.ShowSnackbar(it))
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

    private fun updateScreenState(state: TestsCreatedScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestsCreatedScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}