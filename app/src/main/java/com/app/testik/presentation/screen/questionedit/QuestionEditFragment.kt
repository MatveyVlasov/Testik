package com.app.testik.presentation.screen.questionedit

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
import androidx.recyclerview.widget.ItemTouchHelper
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionEditBinding
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.activity.ImageCropActivity
import com.app.testik.presentation.activity.ImageViewActivity
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.questionedit.adapter.MultipleChoiceDelegateAdapter
import com.app.testik.presentation.screen.questionedit.adapter.SingleChoiceDelegateAdapter
import com.app.testik.presentation.screen.questionedit.mapper.toQuestionItem
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenEvent
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.DELETE_QUESTION_RESULT_KEY
import com.app.testik.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_TITLE
import com.app.testik.util.Constants.UPDATE_QUESTION_RESULT_KEY
import com.app.testik.util.delegateadapter.CompositeAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yanzhenjie.album.Album
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionEditFragment : BaseFragment<FragmentQuestionEditBinding>() {

    private val viewModel: QuestionEditViewModel by viewModels()

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val path = result.data?.extras?.getString(EXTRA_IMAGE_CROPPED_PATH).orEmpty()
            viewModel.loadImage(path)
        }
    }

    private val answersAdapter by lazy {
        CompositeAdapter.Builder()
            .add(
                SingleChoiceDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
                    onSelectClick = { item -> viewModel.onSelectClick(answer = item) },
                    onDeleteClick = { item -> viewModel.deleteAnswer(answer = item) }
                )
            )
            .add(
                MultipleChoiceDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
                    onSelectClick = { item, isChecked -> viewModel.onSelectClick(answer = item, isChecked = isChecked) },
                    onDeleteClick = { item -> viewModel.deleteAnswer(answer = item) }
                )
            )
            .build()
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback { from, to -> viewModel.moveAnswer(from, to) })
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentQuestionEditBinding.inflate(inflater)

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

            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.delete -> confirmDeletion()
                }
                return@setOnMenuItemClickListener true
            }

            ivImage.clipToOutline = true

            rvAnswers.adapter = answersAdapter
            itemTouchHelper.attachToRecyclerView(rvAnswers)
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            ivImage.setOnClickListener { viewImage() }
            ivEditImage.setOnClickListener { onChangeImage() }

            btnAddAnswer.setOnClickListener {
                viewModel.addAnswer()
                scrollView.fullScroll(View.FOCUS_DOWN)
            }

            btnDiscard.setOnClickListener {
                etTitle.clearFocus()
                etDescription.clearFocus()
                viewModel.discardChanges()
            }

            etType.setOnClickListener { showChangeTypeDialog() }

            etTitle.addTextChangedListener { viewModel.onTitleChanged(it.toString()) }
            etDescription.addTextChangedListener { viewModel.onDescriptionChanged(it.toString()) }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess { data ->
                            answersAdapter.submitList(data.answers)
                            renderUIState(data)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: QuestionEditScreenEvent) {
        when (event) {
            is QuestionEditScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionEditScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionEditScreenEvent.Loading -> Unit
            is QuestionEditScreenEvent.EnableDiscardButton -> binding.btnDiscard.isEnabled = true
        }
        setLoadingState(event is QuestionEditScreenEvent.Loading)
    }

    private fun renderUIState(data: QuestionEditScreenUIState) {
        binding.apply {
            if (!etTitle.isFocused) etTitle.setText(data.title)
            if (!etDescription.isFocused) etDescription.setText(data.description)
            if (!etType.isFocused) etType.setText(data.type.description)

            tilTitle.error = getStringOrNull(data.titleError)
            tilDescription.error = getStringOrNull(data.descriptionError)

            if (tilTitle.error != null || tilDescription.error != null) {
                scrollView.fullScroll(View.FOCUS_UP)
            }

            btnDiscard.isEnabled = data.canDiscard
        }

        loadImage(data.image)
        setLoadingState(false)
    }

    private fun onBackPressed() {
        if (!viewModel.validateData()) return
        if (viewModel.screenUIState.canDiscard) {
            var question = viewModel.screenUIState
            if (question.id.isEmpty()) question = question.copy(id = randomId)
            setResult(UPDATE_QUESTION_RESULT_KEY, question.toQuestionItem())
        }
        navController.navigateUp()
    }

    private fun loadImage(url: String) =
        loadQuestionImage(context = requireContext(), imageView = binding.ivImage, url = url)

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

    private fun confirmDeletion() {
        showAlert(
            title = R.string.delete_question,
            message = R.string.delete_question_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = { deleteQuestion() }
        )
    }

    private fun deleteQuestion() {
        showSnackbar(R.string.delete_question_success)
        setResult(DELETE_QUESTION_RESULT_KEY, viewModel.screenUIState.toQuestionItem())
        navController.navigateUp()
    }

    private fun showChangeTypeDialog() {
        var selectedItem = viewModel.screenUIState.type.ordinal

        showSingleChoiceDialog(
            title = R.string.select_category,
            positive = R.string.confirm,
            negative = R.string.cancel,
            items = QuestionType.values().map { it.description },
            selectedItem = selectedItem,
            onPositiveClick = { viewModel.onTypeChanged(QuestionType.values()[selectedItem]) },
            onItemClick = { selectedItem = it }
        )
    }
}