package com.app.testik.presentation.screen.testedit

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
import androidx.navigation.fragment.navArgs
import com.app.testik.R
import com.app.testik.databinding.FragmentTestEditBinding
import com.app.testik.presentation.activity.ImageCropActivity
import com.app.testik.presentation.activity.ImageViewActivity
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testedit.model.TestEditScreenEvent
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.CATEGORIES
import com.app.testik.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_TITLE
import com.app.testik.util.Constants.UPDATE_AVATAR_RESULT_KEY
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yanzhenjie.album.Album
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestEditFragment : BaseFragment<FragmentTestEditBinding>() {

    private val viewModel: TestEditViewModel by viewModels()

    private val args: TestEditFragmentArgs by navArgs()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.extras?.getString(EXTRA_IMAGE_CROPPED_PATH).orEmpty()
            viewModel.loadImage(path)
        }
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentTestEditBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { onBackPressed() }
    }


    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {

            if (args.testId == -1) toolbar.setTitle(R.string.test_creation)

            ivImage.clipToOutline = true
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            ivImage.setOnClickListener { viewImage() }
            ivEditImage.setOnClickListener { onChangeImage() }
            etCategory.setOnClickListener { showChangeCategoryDialog() }
            btnSave.setOnClickListener { viewModel.save() }
//            btnEditQuestions.setOnClickListener {
//                navController.navigate(
//                    ProfileFragmentDirections.toPasswordChange()
//                )
//            }

            etTitle.addTextChangedListener { viewModel.onTitleChanged(it.toString()) }
            etDescription.addTextChangedListener { viewModel.onDescriptionChanged(it.toString()) }
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

    private fun handleEvent(event: TestEditScreenEvent) {
        when (event) {
            is TestEditScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestEditScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestEditScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestEditScreenEvent.Loading)
    }

    private fun renderUIState(data: TestEditScreenUIState) {
        binding.apply {
            if (!etTitle.isFocused) etTitle.setText(data.title)
            if (!etDescription.isFocused) etDescription.setText(data.description)
            if (!etCategory.isFocused) etCategory.setText(data.category)

            tilDescription.error = getStringOrNull(data.descriptionError)

            btnSave.isEnabled = data.canSave
        }

        setResult(UPDATE_AVATAR_RESULT_KEY, data.imageUpdated)
        loadImage(data.image)
        setLoadingState(false)
    }

    private fun onBackPressed() {
        if (viewModel.screenUIState.canSave) confirmExitWithoutSaving()
        else navController.navigateUp()
    }

    private fun loadImage(url: String) {
        val image = url.ifBlank { R.drawable.ic_profile_avatar } // change

        Glide.with(this)
            .load(image)
            .into(binding.ivImage)
    }

    private fun onChangeImage() {
        if (viewModel.screenUIState.image.isBlank()) pickImage()
        else changeImage()
    }

    private fun changeImage() {
        val items = listOf(R.string.load_image, R.string.crop_image, R.string.delete_image)
            .map { getString(it) }
            .toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setItems(items) { _, which ->
                when (which) {
                    0 -> pickImage()
                    1 -> cropImage()
                    2 -> deleteImage()
                }
            }
            .show()
    }

    private fun pickImage() {
        Album.image(requireContext())
            .singleChoice()
            .camera(true)
            .onResult {
                viewModel.loadImage(it.first().path)
            }
            .start()
    }

    private fun cropImage() {
        Intent(context, ImageCropActivity::class.java).also {
            it.putExtra(EXTRA_IMAGE_PATH, viewModel.screenUIState.image)
            launcher.launch(it)
        }
    }

    private fun viewImage() {
        if (viewModel.screenUIState.image.isBlank()) return pickImage()

        Intent(context, ImageViewActivity::class.java).also {
            it.putExtra(EXTRA_IMAGE_TITLE, getString(R.string.test_image))
            it.putExtra(EXTRA_IMAGE_PATH, viewModel.screenUIState.image)
            startActivity(it)
        }
    }

    private fun deleteImage() {
        viewModel.deleteImage()
    }

    private fun showChangeCategoryDialog() {
        var selectedItem = CATEGORIES.lastIndex

        showSingleChoiceDialog(
            title = R.string.select_category,
            positive = R.string.confirm,
            negative = R.string.cancel,
            items = CATEGORIES,
            selectedItem = selectedItem,
            onPositiveClick = { viewModel.onCategoryChanged(getString(CATEGORIES[selectedItem])) },
            onItemClick = { selectedItem = it }
        )
    }

    private fun confirmExitWithoutSaving() {
        showAlert(
            title = R.string.go_back,
            message = R.string.go_back_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = navController::navigateUp
        )
    }
}