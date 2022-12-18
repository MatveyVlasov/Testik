package com.app.testik.presentation.screen.createdtests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.createdtests.mapper.toCreatedTestItem
import com.app.testik.presentation.screen.createdtests.model.CreatedTestDelegateItem
import com.app.testik.presentation.screen.createdtests.model.CreatedTestsScreenEvent
import com.app.testik.presentation.screen.createdtests.model.CreatedTestsScreenUIState
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
class CreatedTestsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val getCurrentUserTestsUseCase: GetCurrentUserTestsUseCase,
    private val deleteTestUseCase: DeleteTestUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<CreatedTestsScreenUIState>>
        get() = _uiState

    val event: SharedFlow<CreatedTestsScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<CreatedTestsScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<CreatedTestsScreenEvent>()

    private var screenUIState = CreatedTestsScreenUIState()

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
                screenUIState = CreatedTestsScreenUIState()
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
            getCurrentUserTestsUseCase(snapshot).onSuccess {
                snapshot = it.snapshot
                postListItems(it.tests.map { test -> test.toCreatedTestItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    fun addTestToList(test: CreatedTestDelegateItem) {
        val tests = screenUIState.tests.map { it }.toMutableList().also {
            it.add(0, test)
        }
        updateScreenState(screenUIState.copy(tests = tests))
    }

    fun updateTest(test: CreatedTestDelegateItem, newTest: CreatedTestDelegateItem) {
        val pos = screenUIState.tests.indexOf(test)
        val tests = screenUIState.tests.map { it }.toMutableList().also {
            it[pos] = newTest
        }
        updateScreenState(screenUIState.copy(tests = tests))
    }

    fun deleteTestFromList(test: CreatedTestDelegateItem) {
        val tests = screenUIState.tests.map { it }.toMutableList().also {
            it.remove(test)
        }
        updateScreenState(screenUIState.copy(tests = tests))
    }

    fun deleteTest(test: CreatedTestDelegateItem?) {
        if (test == null) return
        emitEvent(CreatedTestsScreenEvent.Loading)

        viewModelScope.launch {
            deleteTestUseCase(testId = test.id).onSuccess {
                deleteTestFromList(test)
                emitEvent(CreatedTestsScreenEvent.SuccessTestDeletion(test))
            }.onError {
                emitEvent(CreatedTestsScreenEvent.ShowSnackbar(it))
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

    private fun updateScreenState(state: CreatedTestsScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: CreatedTestsScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}