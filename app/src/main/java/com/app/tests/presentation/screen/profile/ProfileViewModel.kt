package com.app.tests.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.tests.R
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
import com.app.tests.util.isUsername
import com.app.tests.util.loadedFromServer
import com.google.firebase.firestore.Source
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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

    private var oldScreenUIState: ProfileScreenUIState? = null

    init {
        getUserInfo(Source.CACHE)
    }

    fun onUsernameChanged(username: String) {
        if (username == screenUIState.username) return
        updateScreenState(screenUIState.copy(username = username, usernameError = null))
    }

    fun onFirstNameChanged(firstName: String) {
        if (firstName == screenUIState.firstName) return
        updateScreenState(screenUIState.copy(firstName = firstName, firstNameError = null))
    }

    fun onLastNameChanged(lastName: String) {
        if (lastName == screenUIState.lastName) return
        updateScreenState(screenUIState.copy(lastName = lastName, lastNameError = null))
    }

    fun getUserInfo(source: Source = Source.DEFAULT) {
        emitEvent(ProfileScreenEvent.Loading)

        viewModelScope.launch {
            getCurrentUserInfoUseCase(source).onSuccess {
                val screenState = ProfileScreenUIState(
                    email = it.email,
                    username = it.username,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    avatar = it.avatar
                )
                oldScreenUIState = screenState
                updateScreenState(state = screenState)
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
        if (!validateData()) return
        emitEvent(ProfileScreenEvent.Loading)

        val avatarUpdated = !screenUIState.avatar.loadedFromServer()

        viewModelScope.launch {
            updateUserUseCase(screenUIState.toDomain()).onSuccess {
                val screenState = screenUIState.copy(avatarUpdated = avatarUpdated)
                oldScreenUIState = screenState
                updateScreenState(screenState)
                emitEvent(ProfileScreenEvent.ShowSnackbar("Success"))
            }.onError {
                emitEvent(ProfileScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun validateData(): Boolean {
        return checkUsername()
    }

    private fun checkUsername(): Boolean {
        return (screenUIState.username.isUsername()).also {
            val error = if (it) null else R.string.username_badly_formatted
            updateScreenState(screenUIState.copy(usernameError = error))
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
            deleteUserUseCase(screenUIState.email).onSuccess {
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
        Timber.i("here state $state")
        Timber.i("here old $oldScreenUIState")
        screenUIState = state.copy(canSave = state != oldScreenUIState)
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: ProfileScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}