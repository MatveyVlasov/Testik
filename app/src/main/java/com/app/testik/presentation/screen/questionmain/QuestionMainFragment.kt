package com.app.testik.presentation.screen.questionmain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionMainBinding
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.questionmain.adapter.QuestionAdapter
import com.app.testik.presentation.screen.questionmain.adapter.QuestionNumberDelegateAdapter
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenEvent
import com.app.testik.presentation.screen.questionmain.model.QuestionMainScreenUIState
import com.app.testik.presentation.screen.questionmain.model.QuestionNumberDelegateItem
import com.app.testik.presentation.screen.questionmain.model.toQuestionNumber
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionMainFragment : BaseFragment<FragmentQuestionMainBinding>() {

    private val viewModel: QuestionMainViewModel by viewModels()

    private val questionsNumAdapter by lazy {
        CompositeAdapter.Builder()
            .add(QuestionNumberDelegateAdapter { goToQuestion(it) })
            .build()
    }

    private var questionsAdapter: QuestionAdapter? = null

    private var questionsNumList = mutableListOf<QuestionNumberDelegateItem>()
    private var selectedItem = -1
    private var toast: Toast? = null

    private val llInfoHeight
        get() = if (binding.llInfo.isVisible) getDimens(R.dimen.ll_info_height).toInt()
                else 0

    private val rvQuestionsHeight
        get() = if (binding.rvQuestions.isVisible) getDimens(R.dimen.rv_questions_height).toInt()
                else 0

    private val llActionsHeight
        get() = if (binding.llActions.isVisible) getDimens(R.dimen.ll_actions_height).toInt()
                else 0

    override fun createBinding(inflater: LayoutInflater) = FragmentQuestionMainBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { onBackPressed() }
    }

    override fun onStart() {
        super.onStart()

        if (!viewModel.screenUIState.isTimerFinishedHandled) {
            handleEvent(QuestionMainScreenEvent.TimerFinished)
        }
    }

    override fun setLoadingState(isLoading: Boolean) {
        super.setLoadingState(isLoading)
        binding.loading.isVisible = isLoading
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            loading.setOnClickListener { }

            rvQuestions.adapter = questionsNumAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (selectedItem == position) return
                    if (selectedItem != -1) {
                        updateAnswers(selectedItem)
                        questionsNumList[selectedItem] = questionsNumList[selectedItem].copy(isSelected = false)
                    }
                    selectedItem = position

                    questionsNumList[position] = questionsNumList[position].copy(isSelected = true)
                    questionsNumAdapter.submitList(questionsNumList.toList())

                    binding.rvQuestions.smoothScrollToPosition(position)
                }
            })

            btnExit.setOnClickListener { confirmExit() }
            btnFinish.setOnClickListener { confirmFinish() }
            btnSubmit.setOnClickListener { saveAnswers() }
            btnPrev.setOnClickListener { goToPreviousQuestion() }
            btnNext.setOnClickListener { goToNextQuestion() }
            btnGoBack.setOnClickListener { navController.navigateUp() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            if (questionsAdapter == null && data.questions.isNotEmpty()) {
                                questionsAdapter = QuestionAdapter(
                                    fragment = this@QuestionMainFragment,
                                    questions = data.questions,
                                    isReviewMode = data.isReviewMode
                                )
                                binding.pager.adapter = questionsAdapter

                                questionsNumList = MutableList(data.questions.size) { num ->
                                    num.toQuestionNumber()
                                }
                                questionsNumAdapter.submitList(questionsNumList.toList())
                                renderUIState(data)
                                lifecycleScope.launch {
                                    delay(10)
                                    goToQuestion(data.startQuestion)
                                }
                            } else if (data.isReviewQuestionMode) {
                                val pos = data.currentQuestion

                                questionsAdapter?.getFragment(pos)?.updateQuestion(data.questions[pos])
                                setTextButtonSubmit(isNext = true)
                            }
                        }
                        setLoadingState(false)
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }

                launch {
                    viewModel.timeLeft.collect { updateTimerText(it) }
                }
            }
        }
    }

    private fun renderUIState(data: QuestionMainScreenUIState) {
        binding.apply {
            val isReviewMode = data.isReviewMode
            llInfo.isVisible = !isReviewMode
            btnSubmit.isVisible = !isReviewMode
            btnGoBack.isVisible = isReviewMode

            val showTimer = data.isTimerEnabled || data.isTimerQuestionEnabled
            ivTimeIcon.isVisible = showTimer
            tvTimeLeft.isVisible = showTimer

            val isNavigationEnabled = data.test.isNavigationEnabled || isReviewMode
            pager.isUserInputEnabled = isNavigationEnabled
            rvQuestions.isVisible = isNavigationEnabled
            btnFinish.isVisible = isNavigationEnabled
            btnPrev.isVisible = isNavigationEnabled
            btnNext.isVisible = isNavigationEnabled

            val params = (pager.layoutParams as ViewGroup.MarginLayoutParams)
            params.updateMargins(top = llInfoHeight + rvQuestionsHeight, bottom = llActionsHeight)

            setTextButtonSubmit(isNext = data.isReviewQuestionMode)
        }
    }

    private fun handleEvent(event: QuestionMainScreenEvent) {
        when (event) {
            is QuestionMainScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionMainScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionMainScreenEvent.Loading -> Unit
            is QuestionMainScreenEvent.NavigateToResults -> navigateToResults()
            is QuestionMainScreenEvent.NavigateToQuestion -> goToQuestion(event.num)
            is QuestionMainScreenEvent.NavigateToTestsPassed -> navigateToTestsPassed()
            is QuestionMainScreenEvent.UnansweredQuestion -> {
                goToQuestion(event.num)
                showSnackbar(message = getString(R.string.question_unanswered, event.num + 1))
            }
            is QuestionMainScreenEvent.TimerFinished -> {
                viewModel.onTimerFinishedHandledChanged(isHandled = true)
                showTimerFinishedDialog()
            }
            is QuestionMainScreenEvent.TooLate -> showTimerFinishedDialog(isTooLate = true)
        }
        setLoadingState(event is QuestionMainScreenEvent.Loading)
    }

    private fun goToQuestion(num: Int) {
        binding.pager.currentItem = num
        setTextButtonSubmit()
    }

    private fun goToPreviousQuestion() {
        binding.pager.apply {
            if (currentItem > 0) currentItem -= 1
            else {
                toast?.cancel()
                toast = showToast(message = R.string.cannot_go_to_prev_question, duration = Toast.LENGTH_SHORT)
            }
        }
    }

    private fun goToNextQuestion() {
        binding.pager.apply {
            val pageCount = adapter?.itemCount ?: return
            if (currentItem < pageCount - 1) currentItem += 1
            else {
                toast?.cancel()
                toast = showToast(message = R.string.cannot_go_to_next_question, duration = Toast.LENGTH_SHORT)
            }
        }
    }

    private fun updateAnswers(pos: Int) {
        if (viewModel.screenUIState.isReviewMode) return
        val adapter = questionsAdapter ?: return

        adapter.getFragment(pos)?.also {
            viewModel.updateAnswers(question = pos, answers = it.answers, enteredAnswer = it.enteredAnswer)
        }
    }

    private fun saveAnswers() {
        updateAnswers(binding.pager.currentItem)
        viewModel.submit()
    }

    private fun confirmFinish() {
        showAlert(
            title = R.string.finish_test,
            message = R.string.finish_test_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = { finishTest() }
        )
    }

    private fun finishTest(isTimerFinished: Boolean = false) {
        updateAnswers(binding.pager.currentItem)
        viewModel.finish(isTimerFinished = isTimerFinished)
    }

    private fun submitQuestion(isTimerFinished: Boolean = false) {
        updateAnswers(binding.pager.currentItem)
        viewModel.submitQuestion(isTimerFinished = isTimerFinished)
    }

    private fun confirmExit() {
        showAlert(
            title = R.string.exit_test,
            message = R.string.exit_test_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = { exitTest() }
        )
    }

    private fun exitTest() {
        updateAnswers(binding.pager.currentItem)
        viewModel.saveAnswers(isExiting = true)
    }

    private fun onBackPressed() {
        if (viewModel.screenUIState.isReviewMode) navController.navigateUp()
        else confirmExit()
    }

    private fun navigateToTestsPassed() {
        navController.popBackStack(R.id.mainFragment, inclusive = false).let {
            if (it) setNavbarItem(R.id.testsPassedFragment)
            else navController.popBackStack(R.id.testsPassedFragment, inclusive = false)
        }

        testToInsert = viewModel.testToInsert
    }

    private fun navigateToResults() {
        if (!viewModel.screenUIState.isNavigationToResultsAllowed) {
            viewModel.navigateToResultsOnFinish()
            setLoadingState(true)
            showSnackbar(message = R.string.calculating_points)
            return
        }
        navController.navigate(
            QuestionMainFragmentDirections.toResults(viewModel.screenUIState.test.recordId)
        )
    }

    private fun updateTimerText(timeLeft: Long) {
        binding.tvTimeLeft.text = timeLeft.toTime()
    }

    private fun showTimerFinishedDialog(isTooLate: Boolean = false) {
        if (viewModel.screenUIState.isTimerQuestionEnabled) {
            submitQuestion(isTimerFinished = true)
            showSnackbar(R.string.no_time_left)
            return
        }
        if (!isTooLate) finishTest(isTimerFinished = true)

        showAlert(
            title = R.string.no_time_left,
            message = R.string.no_time_left_description,
            positive = R.string.go_to_results,
            onPositiveClick = { navigateToResults() }
        )
    }

    private fun setTextButtonSubmit(isNext: Boolean = false) {
        val textSubmit =
            if (viewModel.screenUIState.test.isNavigationEnabled) R.string.save_draft
            else if (isNext) R.string.next
            else R.string.submit

        binding.btnSubmit.setText(textSubmit)
    }
}