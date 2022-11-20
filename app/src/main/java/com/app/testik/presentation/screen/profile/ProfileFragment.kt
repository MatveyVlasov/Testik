package com.app.testik.presentation.screen.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentProfileBinding
import com.app.testik.presentation.activity.ImageCropActivity
import com.app.testik.presentation.activity.ImageViewActivity
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.profile.model.ProfileScreenEvent
import com.app.testik.presentation.screen.profile.model.ProfileScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_TITLE
import com.app.testik.util.Constants.PASSWORD_CHANGED_RESULT_KEY
import com.app.testik.util.Constants.UPDATE_AVATAR_RESULT_KEY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yanzhenjie.album.Album
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment<FragmentProfileBinding>() {

    private val viewModel: ProfileViewModel by viewModels()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.extras?.getString(EXTRA_IMAGE_CROPPED_PATH).orEmpty()
            viewModel.loadAvatar(path)
        }
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentProfileBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { onBackPressed() }
        observeResult<Boolean>(PASSWORD_CHANGED_RESULT_KEY) {
            if (it) showSnackbar(message = R.string.change_password_success)
        }
    }


    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            toolbar.setupLanguageItem(getColor(R.color.black)) {
                showChangeLanguageDialog { viewModel.setLanguage(it) }
            }

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.deleteAccount -> confirmDeleteAccount()
                    R.id.signOut -> confirmSignOut()
                }
                return@setOnMenuItemClickListener true
            }

            ivAvatar.clipToOutline = true
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            ivAvatar.setOnClickListener { viewAvatar() }
            ivEditAvatar.setOnClickListener { onChangeAvatar() }
            btnSave.setOnClickListener { viewModel.save() }
            btnChangePassword.setOnClickListener {
                navController.navigate(
                    ProfileFragmentDirections.toPasswordChange()
                )
            }

            etUsername.addTextChangedListener { viewModel.onUsernameChanged(it.toString()) }
            etFirstName.addTextChangedListener { viewModel.onFirstNameChanged(it.toString()) }
            etLastName.addTextChangedListener { viewModel.onLastNameChanged(it.toString()) }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            renderUIState(it)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: ProfileScreenEvent) {
        when (event) {
            is ProfileScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is ProfileScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is ProfileScreenEvent.Loading -> Unit
            is ProfileScreenEvent.SuccessSignOut -> {
                showSnackbar(message = R.string.sign_out_success)
                navController.navigate(
                    ProfileFragmentDirections.toLogin()
                )
            }
            is ProfileScreenEvent.SuccessAccountDeletion -> {
                showSnackbar(message = R.string.delete_account_success)
                navController.navigate(
                    ProfileFragmentDirections.toLogin()
                )
            }
            is ProfileScreenEvent.Restart -> requireActivity().recreate()
        }
        setLoadingState(event is ProfileScreenEvent.Loading)
    }

    private fun renderUIState(data: ProfileScreenUIState) {
        binding.apply {
            if (!etEmail.isFocused) etEmail.setText(data.email)
            if (!etUsername.isFocused) etUsername.setText(data.username)
            if (!etFirstName.isFocused) etFirstName.setText(data.firstName)
            if (!etLastName.isFocused) etLastName.setText(data.lastName)

            tilUsername.error = getStringOrNull(data.usernameError)
            tilFirstName.error = getStringOrNull(data.firstNameError)
            tilLastName.error = getStringOrNull(data.lastNameError)

            btnSave.isEnabled = data.canSave
        }
        setResult(UPDATE_AVATAR_RESULT_KEY, data.avatarUpdated)
        loadAvatar(data.avatar)
        setLoadingState(false)
    }

    private fun onBackPressed() {
        if (viewModel.screenUIState.canSave) confirmExitWithoutSaving()
        else navController.navigateUp()
    }

    private fun loadAvatar(url: String) =
        loadAvatar(context = requireContext(), imageView = binding.ivAvatar, url = url)

    private fun onChangeAvatar() {
        if (viewModel.screenUIState.avatar.isEmpty()) pickAvatar()
        else changeAvatar()
    }

    private fun changeAvatar() {
        val items = listOf(R.string.load_avatar, R.string.crop_avatar, R.string.delete_avatar)
            .map { getString(it) }
            .toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setItems(items) { _, which ->
                when (which) {
                    0 -> pickAvatar()
                    1 -> cropAvatar()
                    2 -> deleteAvatar()
                }
            }
            .show()
    }

    private fun pickAvatar() {
        Album.image(requireContext())
            .singleChoice()
            .camera(true)
            .onResult {
                viewModel.loadAvatar(it.first().path)
            }
            .start()
    }

    private fun cropAvatar() {
        Intent(context, ImageCropActivity::class.java).also {
            it.putExtra(EXTRA_IMAGE_PATH, viewModel.screenUIState.avatar)
            launcher.launch(it)
        }
    }

    private fun viewAvatar() {
        if (viewModel.screenUIState.avatar.isEmpty()) return pickAvatar()

        Intent(context, ImageViewActivity::class.java).also {
            it.putExtra(EXTRA_IMAGE_TITLE, getString(R.string.avatar))
            it.putExtra(EXTRA_IMAGE_PATH, viewModel.screenUIState.avatar)
            startActivity(it)
        }
    }

    private fun deleteAvatar() {
        viewModel.deleteAvatar()
    }

    private fun confirmSignOut() {
        showAlert(
            title = R.string.sign_out,
            message = R.string.sign_out_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = viewModel::signOut
        )
    }

    private fun confirmDeleteAccount() {
        showAlert(
            title = R.string.delete_account,
            message = R.string.delete_account_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = viewModel::deleteAccount
        )
    }
}