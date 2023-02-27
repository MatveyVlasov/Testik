package com.app.testik.presentation.screen.testspassedusers

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testspassedusers.mapper.toTestPassedUserItem
import com.app.testik.presentation.screen.testspassedusers.model.TestPassedUserDelegateItem
import com.app.testik.presentation.screen.testspassedusers.model.TestsPassedUsersScreenEvent
import com.app.testik.presentation.screen.testspassedusers.model.TestsPassedUsersScreenUIState
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import com.app.testik.util.getFullName
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
class TestsPassedUsersViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTestsPassedUseCase: GetTestsPassedUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestsPassedUsersScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestsPassedUsersScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestsPassedUsersScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestsPassedUsersScreenEvent>()

    private val args = TestsPassedUsersFragmentArgs.fromSavedStateHandle(savedStateHandle)

    private var screenUIState = TestsPassedUsersScreenUIState(testId = args.testId)

    private var snapshot: QuerySnapshot? = null
    private var job: Job? = null

    init {
        updateList()
    }


    fun updateList() {
        if (job?.isActive == true) return
        postItem(LoadingItem)

        job = viewModelScope.launch {
            getTestsPassedUseCase(testId = screenUIState.testId, snapshot = snapshot).onSuccess {
                snapshot = it.snapshot
                var tests = it.tests.map { test -> test.toTestPassedUserItem() }
                tests = updateUsersInfo(tests)
                postListItems(tests)
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    private suspend fun updateUsersInfo(tests: List<TestPassedUserDelegateItem>): List<TestPassedUserDelegateItem> {
        val newList = mutableListOf<TestPassedUserDelegateItem>()

        val job = viewModelScope.launch {
            tests.forEach { test ->
                getUserInfoUseCase(test.user).onSuccess {
                    newList.add(
                        test.copy(username = it.getFullName(), avatar = it.avatar)
                    )
                }.onError {
                    newList.add(
                        test.copy(username = test.user)
                    )
                }
            }
        }
        job.join()

        return newList
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

    private fun updateScreenState(state: TestsPassedUsersScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestsPassedUsersScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}