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
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testedit.model.TestEditScreenEvent
import com.app.testik.presentation.screen.testedit.model.TestEditScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.DELETE_TEST_RESULT_KEY
import com.app.testik.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.UPDATE_TEST_RESULT_KEY
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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

        observeResult<Int>(Constants.UPDATE_QUESTION_LIST_RESULT_KEY) {
            viewModel.onQuestionsNumChanged(it)
        }
    }


    private fun initViews() {

        setupBottomNavigation(false)
        initializeViews()

        binding.apply {

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.demo -> navigateToTestInfo()
                    R.id.results -> navigateToResults()
                    R.id.delete -> confirmDeletion()
                }
                return@setOnMenuItemClickListener true
            }
            toolbar.showIcons()

            tvPublish.addInfoIcon { navigateToInfo(getString(R.string.publish_info)) }
            tvTestLink.addInfoIcon { navigateToInfo(getString(R.string.test_link_info)) }
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            ivImage.setOnClickListener { viewImage() }
            ivEditImage.setOnClickListener { onChangeImage() }
            etCategory.setOnClickListener { showChangeCategoryDialog() }

            switchPublish.setOnCheckedChangeListener { _, isChecked -> viewModel.onPublishChanged(isChecked) }
            switchTestLink.setOnCheckedChangeListener { _, isChecked -> viewModel.onTestLinkEnabledChanged(isChecked) }

            btnTestLinkCopy.setOnClickListener {
                copyToClipboard(label = "Test link", text = viewModel.screenUIState.testLink)
                showSnackbar(message = R.string.test_link_copied, duration = Snackbar.LENGTH_SHORT)
            }
            btnTestLinkShare.setOnClickListener { shareTestLink(viewModel.screenUIState.testLink) }

            tvMoreSettings.setOnClickListener { viewModel.onShowMore() }

            switchNavigateBetweenQuestions.setOnCheckedChangeListener { _, isChecked -> viewModel.onNavigationEnabledChanged(isChecked) }
            switchRandomizeQuestions.setOnCheckedChangeListener { _, isChecked -> viewModel.onRandomQuestionsChanged(isChecked) }
            switchRandomizeAnswers.setOnCheckedChangeListener { _, isChecked -> viewModel.onRandomAnswersChanged(isChecked) }

            btnSave.setOnClickListener { viewModel.save() }

            btnEditQuestions.setOnClickListener {
                if (viewModel.screenUIState.canSave) confirmExitWithoutSaving {
                    viewModel.discardChanges()
                    navigateToQuestionList()
                } else {
                    navigateToQuestionList()
                }
            }

            btnEditGradingSystem.setOnClickListener {
                if (viewModel.screenUIState.canSave) confirmExitWithoutSaving {
                    viewModel.discardChanges()
                    navigateToGradingSystem()
                } else {
                    navigateToGradingSystem()
                }
            }

            etTitle.addTextChangedListener { viewModel.onTitleChanged(it.toString()) }
            etDescription.addTextChangedListener { viewModel.onDescriptionChanged(it.toString()) }
            etPassword.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }
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
                setResult(UPDATE_TEST_RESULT_KEY, null)
                setResult(DELETE_TEST_RESULT_KEY, event.test)
                navController.navigateUp()
            }
        }
        setLoadingState(event is TestEditScreenEvent.Loading)
    }

    private fun renderUIState(data: TestEditScreenUIState) {
        binding.apply {
            viewModel.screenUIState.canSave.let {
                if (!it || !etTitle.isFocused) etTitle.setText(data.title)
                if (!it || !etDescription.isFocused) etDescription.setText(data.description)
                if (!it || !etPassword.isFocused) etPassword.setText(data.password)
                if (!it || !etCategory.isFocused) etCategory.setText(data.category.description)
            }

            tilTitle.error = getStringOrNull(data.titleError)
            tilDescription.error = getStringOrNull(data.descriptionError)
            tilCategory.error = getStringOrNull(data.categoryError)

            switchPublish.isChecked = data.isPublished

            val isLinkEnabled = data.isTestLinkEnabled
            switchTestLink.isChecked = isLinkEnabled
            llTestLinkData.isVisible = isLinkEnabled
            tvTestLinkData.text = data.testLink

            val showMore = data.showMore
            val isTestCreated = viewModel.screenUIState.id.isNotEmpty()
            tvMoreSettings.isVisible = !showMore && isTestCreated
            llNavigateBetweenQuestions.isVisible = showMore
            llRandomizeQuestions.isVisible = showMore
            llRandomizeAnswers.isVisible = showMore

            switchNavigateBetweenQuestions.isChecked = data.isNavigationEnabled
            switchRandomizeQuestions.isChecked = data.isRandomQuestions
            switchRandomizeAnswers.isChecked = data.isRandomAnswers

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
            val isTestCreated = viewModel.screenUIState.id.isNotEmpty()

            toolbar.setTitle(if (isTestCreated) R.string.test_settings else R.string.test_creation)
            btnSave.setText(if (isTestCreated) R.string.save else R.string.create_test)

            toolbar.menu.findItem(R.id.delete).isVisible = isTestCreated
            toolbar.menu.findItem(R.id.demo).isVisible = isTestCreated
            toolbar.menu.findItem(R.id.results).isVisible = isTestCreated

            llPublish.isVisible = isTestCreated
            llTestLink.isVisible = isTestCreated
            btnEditQuestions.isVisible = isTestCreated
            btnEditGradingSystem.isVisible = isTestCreated
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

    private fun navigateToTestInfo() {
        navController.navigate(
            TestEditFragmentDirections.toTestInfo(testId = viewModel.screenUIState.id, isDemo = true)
        )
    }

    private fun navigateToResults() {
        navController.navigate(
            TestEditFragmentDirections.toResults(testId = viewModel.screenUIState.id)
        )
    }

    private fun navigateToGradingSystem() {
        navController.navigate(
            TestEditFragmentDirections.toGradingSystem(testId = viewModel.screenUIState.id)
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

        viewImage(image = viewModel.screenUIState.image)
    }

    private fun deleteImage() {
        viewModel.deleteImage()
    }

    private fun showChangeCategoryDialog() {
        var selectedItem = viewModel.screenUIState.category.ordinal - 1

        showSingleChoiceDialog(
            title = R.string.select_category,
            positive = R.string.confirm,
            negative = R.string.cancel,
            items = CategoryType.values()
                    .filter { it.title.isNotEmpty() && !it.title.startsWith("_") }
                    .map { it.description },
            selectedItem = selectedItem,
            onPositiveClick = { viewModel.onCategoryChanged(CategoryType.values()[selectedItem + 1]) },
            onItemClick = { selectedItem = it }
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