package com.app.tests.presentation.screen.profile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.tests.R
import com.app.tests.databinding.FragmentProfileBinding
import com.app.tests.presentation.activity.ImageCropActivity
import com.app.tests.presentation.activity.ImageViewActivity
import com.app.tests.presentation.base.BaseFragment
import com.app.tests.presentation.model.onSuccess
import com.app.tests.presentation.screen.profile.model.ProfileScreenEvent
import com.app.tests.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.tests.util.Constants.EXTRA_IMAGE_PATH
import com.app.tests.util.Constants.EXTRA_IMAGE_TITLE
import com.app.tests.util.setupBottomNavigation
import com.app.tests.util.showAlert
import com.app.tests.util.showSnackbar
import com.bumptech.glide.Glide
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
    }

    private fun initViews() {

        setupBottomNavigation(true)
        binding.apply {
            ivAvatar.clipToOutline = true

            ivAvatar.setOnClickListener { viewAvatar() }
            btnChangeAvatar.setOnClickListener { onChangeAvatar() }
            btnSave.setOnClickListener { viewModel.save() }
            btnSignOut.setOnClickListener { confirmSignOut() }
            btnDeleteAccount.setOnClickListener { confirmDeleteAccount() }
        }
    }

    private fun initListeners() {
        binding.apply {

        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            loadAvatar(it.avatar)
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
            is ProfileScreenEvent.ShowSnackbar -> {
                //setLoadingState(false)
                showSnackbar(message = event.message)
            }
            is ProfileScreenEvent.Loading -> Unit //setLoadingState(true)
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
        }
    }

    private fun loadAvatar(url: String) {
        val avatar = url.ifBlank { R.drawable.ic_profile_avatar }

        Glide.with(this)
            .load(avatar)
            .into(binding.ivAvatar)
    }

    private fun onChangeAvatar() {
        if (viewModel.screenUIState.avatar.isBlank()) pickAvatar()
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
        if (viewModel.screenUIState.avatar.isBlank()) return pickAvatar()

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