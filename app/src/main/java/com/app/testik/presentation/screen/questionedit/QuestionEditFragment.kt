package com.app.testik.presentation.screen.questionedit

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
import androidx.recyclerview.widget.ItemTouchHelper
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionEditBinding
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.activity.ImageCropActivity
import com.app.testik.presentation.adapter.answer.*
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.questionedit.mapper.toQuestionItem
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenEvent
import com.app.testik.presentation.screen.questionedit.model.QuestionEditScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.DELETE_QUESTION_RESULT_KEY
import com.app.testik.util.Constants.EXTRA_IMAGE_CROPPED_PATH
import com.app.testik.util.Constants.EXTRA_IMAGE_PATH
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
                SingleChoiceEditDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
                    onSelectClick = { item -> viewModel.onSelectClick(answer = item) },
                    onDeleteClick = { item -> viewModel.deleteAnswer(answer = item) },
                    isTrueFalse = { viewModel.screenUIState.type == QuestionType.TRUE_FALSE }
                )
            )
            .add(
                MultipleChoiceEditDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
                    onSelectClick = { item, isChecked -> viewModel.onSelectClick(answer = item, isChecked = isChecked) },
                    onDeleteClick = { item -> viewModel.deleteAnswer(answer = item) }
                )
            )
            .add(
                ShortAnswerEditDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
                    onDeleteClick = { item -> viewModel.deleteAnswer(answer = item) }
                )
            )
            .add(
                MatchingEditDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
                    onTextMatchingChanged = { item, text -> viewModel.onAnswerMatchingTextChanged(answer = item, text = text) },
                    onDeleteClick = { item -> viewModel.deleteAnswer(answer = item) }
                )
            )
            .add(
                OrderingEditDelegateAdapter(
                    onTextChanged = { item, text -> viewModel.onAnswerTextChanged(answer = item, text = text) },
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

            rvAnswers.adapter = answersAdapter
            itemTouchHelper.attachToRecyclerView(rvAnswers)

            tvMatch.addInfoIcon { navigateToInfo(getString(R.string.match_info)) }
            tvCaseSensitive.addInfoIcon { navigateToInfo(getString(R.string.case_sensitive_info)) }
            tvPercentageError.addInfoIcon { navigateToInfo(getString(R.string.percentage_error_info)) }
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
                scrollView.fullScroll(View.FOCUS_UP)
                viewModel.discardChanges()
            }

            etType.setOnClickListener { showChangeTypeDialog() }

            etTitle.addTextChangedListener { viewModel.onTitleChanged(it.toString()) }
            etDescription.addTextChangedListener { viewModel.onDescriptionChanged(it.toString()) }
            etExplanation.addTextChangedListener { viewModel.onExplanationChanged(it.toString()) }
            etPoints.addTextChangedListener { viewModel.onPointsChanged(it.toString()) }
            etCorrectNumber.addTextChangedListener { viewModel.onCorrectNumberChanged(it.toString()) }
            etPercentageError.addTextChangedListener { viewModel.onPercentageErrorChanged(it.toString()) }

            switchRequired.setOnCheckedChangeListener { _, isChecked -> viewModel.onRequiredChanged(isChecked) }
            switchMatch.setOnCheckedChangeListener { _, isChecked -> viewModel.onMatchChanged(isChecked) }
            switchCaseSensitive.setOnCheckedChangeListener { _, isChecked -> viewModel.onCaseSensitiveChanged(isChecked) }
            switchPercentageError.setOnCheckedChangeListener { _, isChecked -> viewModel.onPercentageErrorEnabledChanged(isChecked) }
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
            data.canDiscard.let {
                if (!it || !etTitle.isFocused) etTitle.setText(data.title)
                if (!it || !etDescription.isFocused) etDescription.setText(data.description)
                if (!it || !etExplanation.isFocused) etExplanation.setText(data.explanation)
                if (!it || !etPoints.isFocused) etPoints.setText(data.points)
                if (!it || !etType.isFocused) etType.setText(data.type.description)
                if (!it || !etCorrectNumber.isFocused) etCorrectNumber.setText(data.correctNumber)
                if (!it || !etPercentageError.isFocused) etPercentageError.setText(data.percentageError)
            }

            switchRequired.isChecked = data.isRequired
            switchMatch.isChecked = data.isMatch
            switchCaseSensitive.isChecked = data.isCaseSensitive
            switchPercentageError.isChecked = data.percentageError != null

            val isShortAnswerType = data.type == QuestionType.SHORT_ANSWER
            llMatch.isVisible = isShortAnswerType
            llCaseSensitive.isVisible = isShortAnswerType

            val isNumberType = data.type == QuestionType.NUMBER
            rvAnswers.isVisible = !isNumberType
            btnAddAnswer.isVisible = !isNumberType
            tilCorrectNumber.isVisible = isNumberType
            llPercentageError.isVisible = isNumberType
            tilPercentageError.isVisible = isNumberType && data.percentageError != null

            tilTitle.error = getStringOrNull(data.titleError)
            tilDescription.error = getStringOrNull(data.descriptionError)
            tilExplanation.error = getStringOrNull(data.explanationError)

            if (tilTitle.error != null || tilDescription.error != null || tilExplanation.error != null) {
                scrollView.fullScroll(View.FOCUS_UP)
            }

            btnAddAnswer.isEnabled = data.type != QuestionType.TRUE_FALSE
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

        viewImage(image = viewModel.screenUIState.image)
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
            title = R.string.select_question_type,
            positive = R.string.confirm,
            negative = R.string.cancel,
            items = QuestionType.values().map { it.description },
            selectedItem = selectedItem,
            onPositiveClick = { changeType(QuestionType.values()[selectedItem]) },
            onItemClick = { selectedItem = it }
        )
    }

    private fun changeType(type: QuestionType) {
        viewModel.onTypeChanged(type)
    }

    private fun navigateToInfo(text: String) {
        navController.navigate(
            QuestionEditFragmentDirections.toInfo(text)
        )
    }
}