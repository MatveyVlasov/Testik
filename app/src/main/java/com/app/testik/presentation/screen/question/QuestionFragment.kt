package com.app.testik.presentation.screen.question

import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionBinding
import com.app.testik.domain.model.QuestionType
import com.app.testik.presentation.adapter.answer.*
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.AnswerDelegateItem
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.model.answer.SingleChoiceDelegateItem
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.question.model.QuestionScreenEvent
import com.app.testik.presentation.screen.question.model.QuestionScreenUIState
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionFragment(
    private val question: QuestionDelegateItem,
    private val num: Int,
    private var isReviewMode: Boolean
) : BaseFragment<FragmentQuestionBinding>() {

    val answers: List<AnswerDelegateItem>
        get() = viewModel.screenUIState.answers

    val enteredAnswer: String
        get() = viewModel.screenUIState.enteredAnswer

    private val viewModel: QuestionViewModel by viewModels()

    private val answersAdapter by lazy {
        CompositeAdapter.Builder()
            .add(
                SingleChoiceDelegateAdapter(
                    onSelectClick = { item -> viewModel.onSelectClick(answer = item) },
                    isReviewMode = { isReviewMode }
                )
            )
            .add(
                MultipleChoiceDelegateAdapter(
                    onSelectClick = { item, isChecked -> viewModel.onSelectClick(answer = item, isChecked = isChecked) },
                    isReviewMode = { isReviewMode }
                )
            )
            .add(ShortAnswerDelegateAdapter())
            .add(MatchingLeftDelegateAdapter())
            .add(MatchingRightDelegateAdapter(isReviewMode = { isReviewMode }))
            .add(OrderingDelegateAdapter(isReviewMode = { isReviewMode }))
            .build()
    }

    private var answersList: List<AnswerDelegateItem> = emptyList()

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback { from, to -> viewModel.moveAnswer(from, to) })
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentQuestionBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        if (savedInstanceState == null) viewModel.updateQuestion(question)
    }

    fun updateQuestion(question: QuestionDelegateItem) {
        isReviewMode = true
        answersAdapter.submitList(null)
        viewModel.updateQuestion(question.copy(title = question.title + " ")) // data not collected if item the same
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            rvAnswers.adapter = answersAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            ivImage.setOnClickListener {
                viewImage(image = viewModel.screenUIState.image, title = R.string.question_image)
            }

            etAnswer.addTextChangedListener { viewModel.onAnswerChanged(it.toString()) }

            tvTrue.setOnClickListener {
                viewModel.onSelectClick(viewModel.screenUIState.answers[0] as SingleChoiceDelegateItem)
            }
            tvFalse.setOnClickListener {
                viewModel.onSelectClick(viewModel.screenUIState.answers[1] as SingleChoiceDelegateItem)
            }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            if (data.type == QuestionType.MATCHING) {
                                binding.rvAnswers.layoutManager = GridLayoutManager(requireContext(), 2)
                                answersList = data.answersMatching
                            } else {
                                answersList = data.answers
                            }
                            if (data.type == QuestionType.MATCHING || data.type == QuestionType.ORDERING) {
                                binding.rvAnswers.apply {
                                    itemTouchHelper.attachToRecyclerView(if (isReviewMode) null else this)
                                }
                            }
                            answersAdapter.submitList(answersList)
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

    private fun handleEvent(event: QuestionScreenEvent) {
        when (event) {
            is QuestionScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionScreenEvent.Loading -> Unit
        }
        setLoadingState(event is QuestionScreenEvent.Loading)
    }

    private fun renderUIState(data: QuestionScreenUIState) {
        binding.apply {
            val numDisplayed = num + 1
            tvQuestionData.text = numDisplayed.toString()
            tvRequired.isVisible = data.isRequired

            tvPointsData.text =
                if (isReviewMode) getString(R.string.points_earned, data.pointsEarned, data.pointsMax)
                else getString(R.string.num, data.pointsMax)

            tvTitle.text = data.title
            tvDescription.text = data.description
            tvDescription.isVisible = data.description.isNotEmpty()
            tvType.setText(data.type.instruction)
            tvType.isVisible = tvType.text.isNotEmpty()

            val isShortAnswerType = data.type == QuestionType.SHORT_ANSWER
            val isNumberType = data.type == QuestionType.NUMBER
            val isTrueFalseType = data.type == QuestionType.TRUE_FALSE

            rvAnswers.isVisible = (!isShortAnswerType || isReviewMode) && !isNumberType && !isTrueFalseType
            tilAnswer.isVisible = isShortAnswerType || isNumberType
            tilAnswer.isExpandedHintEnabled = !isReviewMode
            etAnswer.isActivated = data.pointsEarned > 0
            etAnswer.isEnabled = !isReviewMode
            if (isReviewMode) etAnswer.setText(data.enteredAnswer)
            if (isNumberType) {
                etAnswer.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
                etAnswer.filters += InputFilter.LengthFilter(getInteger(R.integer.number_max_length))
            }
            tvCorrectNumber.isVisible = isNumberType && isReviewMode && data.pointsEarned == 0
            tvCorrectNumber.text = data.correctNumber
            tvCorrectAnswers.isVisible = isShortAnswerType && isReviewMode

            tvFalse.isVisible = isTrueFalseType
            tvTrue.isVisible = isTrueFalseType

            if (isTrueFalseType && data.answers.size > 1) {
                val answerTrue = data.answers[0] as SingleChoiceDelegateItem
                val answerFalse = data.answers[1] as SingleChoiceDelegateItem

                answerTrue.text.let {
                    tvTrue.text = if (it == SingleChoiceDelegateItem.TRUE_DEFAULT) getString(R.string.true_text) else it
                }
                answerFalse.text.let {
                    tvFalse.text = if (it == SingleChoiceDelegateItem.FALSE_DEFAULT) getString(R.string.false_text) else it
                }

                tvTrue.isEnabled = !isReviewMode || (!answerTrue.isSelected && !answerTrue.isCorrect)
                tvFalse.isEnabled = !isReviewMode || (!answerFalse.isSelected && !answerFalse.isCorrect)

                tvTrue.isSelected = answerTrue.isSelected
                tvFalse.isSelected = answerFalse.isSelected

                tvTrue.isActivated = answerTrue.isCorrect
                tvFalse.isActivated = answerFalse.isCorrect
            }

            if (isShortAnswerType) {
                val msg = when {
                    data.isMatch && !data.isCaseSensitive -> R.string.correct_answers
                    data.isMatch && data.isCaseSensitive -> R.string.correct_answers_case
                    !data.isMatch && !data.isCaseSensitive -> R.string.correct_answers_contains
                    else -> R.string.correct_answers_contains_case
                }
                tvCorrectAnswers.setText(msg)
            }

            llExplanation.isVisible = isReviewMode && data.explanation.isNotEmpty()
            tvExplanationData.text = data.explanation
        }

        loadImage(question.image)
    }

    private fun loadImage(url: String) {
        loadQuestionImage(context = requireContext(), imageView = binding.ivImage, url = url)

        binding.ivImage.isVisible = url.isNotEmpty()
    }
}