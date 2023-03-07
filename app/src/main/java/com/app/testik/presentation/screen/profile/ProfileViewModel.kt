package com.app.testik.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.testik.R
import com.app.testik.domain.model.onError
import com.app.testik.domain.model.onSuccess
import com.app.testik.domain.usecase.*
import com.app.testik.presentation.model.UIState
import com.app.testik.presentation.screen.profile.mapper.toDomain
import com.app.testik.presentation.screen.profile.model.ProfileScreenEvent
import com.app.testik.presentation.screen.profile.model.ProfileScreenUIState
import com.app.testik.util.Constants
import com.app.testik.util.isUsername
import com.app.testik.util.loadedFromServer
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
    private val deleteUserUseCase: DeleteUserUseCase,
    private val preferencesUseCase: PreferencesUseCase
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
        getUserInfo(fromCache = true)
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
                getUserInfo(isAvatarUpdated = avatarUpdated || screenUIState.avatarUpdated)
                emitEvent(ProfileScreenEvent.ShowSnackbarByRes(R.string.saved))
            }.onError {
                handleError(it)
            }
        }
    }

    fun setLanguage(lang: String) {
        preferencesUseCase.setLanguage(lang)
        emitEvent(ProfileScreenEvent.Restart)
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
            deleteUserUseCase().onSuccess {
                signOutUseCase().onSuccess {
                    emitEvent(ProfileScreenEvent.SuccessAccountDeletion)
                }.onError {
                    emitEvent(ProfileScreenEvent.ShowSnackbar(it))
                }
            }.onError {
                handleError(it)
            }
        }
    }

    private fun getUserInfo(fromCache: Boolean = false, isAvatarUpdated: Boolean = false) {
        emitEvent(ProfileScreenEvent.Loading)

        viewModelScope.launch {
            getCurrentUserInfoUseCase(fromCache = fromCache).onSuccess {
                val screenState = ProfileScreenUIState(
                    email = it.email,
                    username = it.username,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    avatar = it.avatar,
                    avatarUpdated = isAvatarUpdated
                )
                oldScreenUIState = screenState
                updateScreenState(state = screenState)
            }.onError {
                if (it.contains("document from cache")) getUserInfo()
                else emitEvent(ProfileScreenEvent.ShowSnackbar(it))
            }
        }
    }

    private fun handleError(error: String) {
        val msg = error.lowercase()
        when {
            msg.contains("no internet") -> {
                emitEvent(ProfileScreenEvent.ShowSnackbarByRes(R.string.no_internet))
            }
            msg.contains("error occurred") -> {
                emitEvent(ProfileScreenEvent.ShowSnackbarByRes(R.string.error_occurred))
            }
            msg.contains("requires recent authentication") -> {
                emitEvent(ProfileScreenEvent.ShowSnackbarByRes(R.string.delete_account_recent_auth_required))
            }
            msg.contains("error while saving image") -> {
                emitEvent(ProfileScreenEvent.ShowSnackbarByRes(R.string.error_while_saving_image))
            }
            msg.contains("username already taken") -> {
                updateScreenState(screenUIState.copy(usernameError = R.string.username_already_taken))
            }
            else -> emitEvent(ProfileScreenEvent.ShowSnackbar(error))
        }
    }

    private fun validateData(): Boolean {
        return checkUsername() && checkUsernameLength()
    }

    private fun checkUsername(): Boolean {
        return (screenUIState.username.isUsername()).also {
            if (!it) updateScreenState(screenUIState.copy(usernameError = R.string.username_badly_formatted))
        }
    }

    private fun checkUsernameLength(): Boolean {
        return (screenUIState.username.length >= Constants.MIN_USERNAME_LENGTH).also {
            if (!it) updateScreenState(screenUIState.copy(usernameError = R.string.username_too_short))
        }
    }

    private fun updateScreenState(state: ProfileScreenUIState) {
        screenUIState = state.copy(canSave = state != oldScreenUIState)
        _uiState.value = UIState.Success(screenUIState)
    }

    private fun emitEvent(event: ProfileScreenEvent) {
        viewModelScope.launch {
            _event.emit(event)
        }
    }
}