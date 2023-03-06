package com.app.testik.presentation.screen.testlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.ErrorItem
import com.app.testik.presentation.model.LoadingItem
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.testlist.mapper.toTestInfoItem
import com.app.testik.presentation.screen.testlist.model.TestListScreenEvent
import com.app.testik.presentation.screen.testlist.model.TestListScreenUIState
import com.app.testik.util.delegateadapter.DelegateAdapterItem
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
class TestListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getCategoryTestsUseCase: GetCategoryTestsUseCase,
    private val getUsersUseCase: GetUsersUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<TestListScreenUIState>>
        get() = _uiState

    val event: SharedFlow<TestListScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<TestListScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<TestListScreenEvent>()

    private val args = TestListFragmentArgs.fromSavedStateHandle(savedStateHandle)

    var screenUIState = TestListScreenUIState(category = args.category)
        private set

    private var snapshot: QuerySnapshot? = null
    private var job: Job? = null
    private var searchJob: Job? = null


    init {
        updateList()
    }

    fun updateList() {
        if (job?.isActive == true) return
        if (snapshot == null && screenUIState.tests.isNotEmpty()) {
            screenUIState = screenUIState.copy(tests = emptyList())
        }

        postItem(LoadingItem)

        job = viewModelScope.launch {
            getCategoryTestsUseCase(
                category = screenUIState.category,
                snapshot = snapshot,
                author = screenUIState.userSelected?.uid
            ).onSuccess {
                snapshot = it.snapshot
                postListItems(it.tests.map { test -> test.toTestInfoItem() })
            }.onError {
                postItem(ErrorItem(it))
            }
        }
    }

    fun getUsers(query: String) {
        if (query.length < 3) {
            updateScreenState(screenUIState.copy(users = emptyList()))
            return
        }
        if (screenUIState.users.isEmpty() && query.length > screenUIState.lastQuery.length && screenUIState.lastQuery.isNotEmpty()) return
        if (searchJob?.isActive == true) return

        _uiState.value = UIState.Loading

        searchJob = viewModelScope.launch {
            getUsersUseCase(query = query).onSuccess {
                updateScreenState(screenUIState.copy(users = it, lastQuery = query))
            }
        }
    }

    fun selectUser(position: Int) {
        snapshot = null
        screenUIState = screenUIState.copy(userSelected = screenUIState.users[position])
        updateList()
    }

    fun deselectUser() {
        snapshot = null
        screenUIState = screenUIState.copy(userSelected = null)
        updateList()
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

    private fun updateScreenState(state: TestListScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: TestListScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}