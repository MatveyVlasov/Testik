package com.app.tests.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.domain.model.onError
import com.app.tests.domain.model.onSuccess
import com.app.tests.domain.usecase.DeleteUserUseCase
import com.app.tests.domain.usecase.GetCurrentUserInfoUseCase
import com.app.tests.domain.usecase.SignOutUseCase
import com.app.tests.domain.usecase.UpdateUserUseCase
import com.app.tests.presentation.model.UIState
import com.app.tests.presentation.screen.profile.mapper.toDomain
import com.app.tests.presentation.screen.profile.model.ProfileScreenEvent
import com.app.tests.presentation.screen.profile.model.ProfileScreenUIState
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUserInfoUseCase: GetCurrentUserInfoUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    val uiState: StateFlow<UIState<ProfileScreenUIState>>
        get() = _uiState

    val event: SharedFlow<ProfileScreenEvent>
        get() = _event

    private val _uiState = MutableStateFlow<UIState<ProfileScreenUIState>>(UIState.Loading)
    private val _event = MutableSharedFlow<ProfileScreenEvent>()

    var screenUIState = ProfileScreenUIState()
        private set

    init {
        getUserInfo(Source.CACHE)
    }

    fun getUserInfo(source: Source = Source.DEFAULT) {
        //emitEvent(MainScreenEvent.Loading)

        viewModelScope.launch {
            getCurrentUserInfoUseCase(source).onSuccess {
                updateScreenState(
                    ProfileScreenUIState(
                        email = it.email,
                        username = it.username,
                        avatar = it.avatar
                    )
                )
            }.onError {
                emitEvent(ProfileScreenEvent.ShowSnackbar(it))
            }
        }
    }

    fun loadAvatar(avatar: String) {
        if (screenUIState.avatar == avatar) return
        updateScreenState(screenUIState.copy(avatar = avatar))
    }

    fun deleteAvatar() = loadAvatar("")

    fun save() {
        viewModelScope.launch {
            updateUserUseCase(screenUIState.toDomain()).onSuccess {
                emitEvent(ProfileScreenEvent.ShowSnackbar("Success"))
            }.onError {
                emitEvent(ProfileScreenEvent.ShowSnackbar(it))
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            signOutUseCase().onSuccess {
                emitEvent(ProfileScreenEvent.SuccessSignOut)
            }.onError {
                emitEvent(ProfileScreenEvent.ShowSnackbar(it))
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            deleteUserUseCase(screenUIState.email).onSuccess { // change to oldScreen
                signOutUseCase().onSuccess {
                    emitEvent(ProfileScreenEvent.SuccessAccountDeletion)
                }.onError {
                    emitEvent(ProfileScreenEvent.ShowSnackbar(it))
                }
            }.onError {
                emitEvent(ProfileScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun updateScreenState(state: ProfileScreenUIState) {
        screenUIState = state
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: ProfileScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}