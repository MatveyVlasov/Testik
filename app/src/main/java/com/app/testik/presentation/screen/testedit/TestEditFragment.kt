package com.app.testik.presentation.screen.testedit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentTestEditBinding
import com.app.testik.domain.model.CategoryType
import com.app.testik.presentation.activity.ImageCropActivity
import com.app.testik.presentation.activity.ImageViewActivity
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testedit.model.TestEditScreenEvent
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.DELETE_TEST_RESULT_KEY
import com.app.testik.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_TITLE
import com.app.testik.util.Constants.UPDATE_TEST_RESULT_KEY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yanzhenjie.album.Album
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestEditFragment : BaseFragment<FragmentTestEditBinding>() {

    private val viewModel: TestEditViewModel by viewModels()

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
        initializeViews()

        binding.apply {

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.demo -> Unit
                    R.id.delete -> confirmDeletion()
                }
                return@setOnMenuItemClickListener true
            }

            ivImage.clipToOutline = true

            tvPublish.addInfoIcon { navigateToInfo(getString(R.string.publish_info)) }
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            ivImage.setOnClickListener { viewImage() }
            ivEditImage.setOnClickListener { onChangeImage() }
            etCategory.setOnClickListener { showChangeCategoryDialog() }

            switchPublish.setOnCheckedChangeListener { _, isChecked -> viewModel.onPublishChanged(isChecked) }

            btnSave.setOnClickListener { viewModel.save() }

            btnEditQuestions.setOnClickListener {
                if (viewModel.screenUIState.canSave) confirmExitWithoutSaving {
                    etTitle.clearFocus()
                    etDescription.clearFocus()
                    viewModel.discardChanges()
                    navigateToQuestionList()
                }
                else {
                    navigateToQuestionList()
                }
            }

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
            is TestEditScreenEvent.SuccessTestCreation -> {
                showSnackbar(R.string.create_test_success)
                initializeViews()
            }
            is TestEditScreenEvent.SuccessTestDeletion -> {
                showSnackbar(R.string.delete_test_success)
                setResult(DELETE_TEST_RESULT_KEY, event.test)
                navController.navigateUp()
            }
        }
        setLoadingState(event is TestEditScreenEvent.Loading)
    }

    private fun renderUIState(data: TestEditScreenUIState) {
        binding.apply {
            if (!etTitle.isFocused) etTitle.setText(data.title)
            if (!etDescription.isFocused) etDescription.setText(data.description)
            if (!etCategory.isFocused) etCategory.setText(data.category.description)

            tilTitle.error = getStringOrNull(data.titleError)
            tilDescription.error = getStringOrNull(data.descriptionError)
            tilCategory.error = getStringOrNull(data.categoryError)

            switchPublish.isChecked = data.isPublished

            btnSave.isEnabled = data.canSave
        }

        data.testUpdated?.let {
            setResult(UPDATE_TEST_RESULT_KEY, it)
        }
        loadImage(data.image)
        setLoadingState(false)
    }

    private fun initializeViews() {
        binding.apply {
            if (viewModel.screenUIState.id.isEmpty()) {
                toolbar.setTitle(R.string.test_creation)
                toolbar.menu.findItem(R.id.delete).isVisible = false
                toolbar.menu.findItem(R.id.demo).isVisible = false

                llPublish.isVisible = false
                btnSave.setText(R.string.create_test)
                btnEditQuestions.isVisible = false
            } else {
                toolbar.setTitle(R.string.test_settings)
                toolbar.menu.findItem(R.id.delete).isVisible = true
                toolbar.menu.findItem(R.id.demo).isVisible = true

                llPublish.isVisible = true
                btnSave.setText(R.string.save)
                btnEditQuestions.isVisible = true
            }
        }
    }

    private fun onBackPressed() {
        if (viewModel.screenUIState.canSave) confirmExitWithoutSaving()
        else navController.navigateUp()
    }

    private fun navigateToQuestionList() {
        navController.navigate(
            TestEditFragmentDirections.toQuestionList(viewModel.screenUIState.id)
        )
    }

    private fun navigateToInfo(text: String) {
        navController.navigate(
            TestEditFragmentDirections.toInfo(text)
        )
    }

    private fun loadImage(url: String) =
        loadTestImage(context = requireContext(), imageView = binding.ivImage, url = url)

    private fun onChangeImage() {
        if (viewModel.screenUIState.image.isEmpty()) pickImage()
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
        if (viewModel.screenUIState.image.isEmpty()) return pickImage()

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
        var selectedItem = viewModel.screenUIState.category.ordinal

        showSingleChoiceDialog(
            title = R.string.select_category,
            positive = R.string.confirm,
            negative = R.string.cancel,
            items = CategoryType.values().filter { it.title.isNotEmpty() }.map { it.description },
            selectedItem = selectedItem,
            onPositiveClick = { viewModel.onCategoryChanged(CategoryType.values()[selectedItem]) },
            onItemClick = { selectedItem = it }
        )
    }

    private fun confirmExitWithoutSaving(onPositiveClick: () -> Unit) {
        showAlert(
            title = R.string.unsaved_changes,
            message = R.string.unsaved_changes_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = { onPositiveClick() }
        )
    }

    private fun confirmDeletion() {
        showAlert(
            title = R.string.delete_test,
            message = R.string.delete_test_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = viewModel::deleteTest
        )
    }
}