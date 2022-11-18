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
import com.app.testik.presentation.screen.createdtests.model.CreatedTestsScreenEvent
import com.app.testik.presentation.screen.createdtests.model.CreatedTestsScreenUIState
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
class CreatedTestsViewModel @Inject constructor(
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

    private var snapshot: QuerySnapshot? = null
    private var job: Job? = null

    init {
        updateList(firstUpdate = true)
    }

    fun updateList(firstUpdate: Boolean = false) {
        if (job?.isActive == true) return
        if (firstUpdate) {
            snapshot = null
            updateScreenState(CreatedTestsScreenUIState())
        }

        postItem(LoadingItem)

        job = viewModelScope.launch {
            getCurrentUserTestsUseCase(snapshot).onSuccess {
                snapshot = it.snapshot
                postListItems(it.tests.map { test -> test.toCreatedTestItem() })
            }.onError {
                if (it.contains("empty")) postItem()
                else postItem(ErrorItem(it))
            }
        }
    }

    fun deleteTest(testId: String) {
        if (testId.isEmpty()) return
        emitEvent(CreatedTestsScreenEvent.Loading)

        viewModelScope.launch {
            deleteTestUseCase(testId = testId).onSuccess {
                emitEvent(CreatedTestsScreenEvent.SuccessTestDeletion)
            }.onError {
                emitEvent(CreatedTestsScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun postItem() = postListItems(emptyList())

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