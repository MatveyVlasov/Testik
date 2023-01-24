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


    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
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
            btnSaveDraft.setOnClickListener { saveAnswers() }
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
                            if (data.questions.isNotEmpty() && binding.pager.adapter == null) {
                                binding.pager.adapter =
                                    QuestionAdapter(
                                        fragment = this@QuestionMainFragment,
                                        questions = data.questions,
                                        isReviewMode = data.isReviewMode
                                    )
                                questionsNumList = MutableList(data.questions.size) { num ->
                                    num.toQuestionNumber()
                                }
                                questionsNumAdapter.submitList(questionsNumList.toList())
                                renderUIState(data)
                                lifecycleScope.launch {
                                    delay(10)
                                    goToQuestion(data.startQuestion)
                                }
                            }
                            setLoadingState(false)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun renderUIState(data: QuestionMainScreenUIState) {
        binding.apply {
            llInfo.isVisible = !data.isReviewMode
            btnSaveDraft.isVisible = !data.isReviewMode
            btnGoBack.isVisible = data.isReviewMode

            val params = (pager.layoutParams as ViewGroup.MarginLayoutParams)
            params.updateMargins(top = llInfoHeight + rvQuestionsHeight, bottom = llActionsHeight)
        }
    }

    private fun handleEvent(event: QuestionMainScreenEvent) {
        when (event) {
            is QuestionMainScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionMainScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionMainScreenEvent.Loading -> Unit
            is QuestionMainScreenEvent.NavigateToResults -> navigateToResults(event.recordId)
            is QuestionMainScreenEvent.NavigateToTestsPassed -> navigateToTestsPassed()
            is QuestionMainScreenEvent.UnansweredQuestion -> {
                goToQuestion(event.num)
                showSnackbar(message = getString(R.string.question_unanswered, event.num + 1))
            }
        }
        setLoadingState(event is QuestionMainScreenEvent.Loading)
    }

    private fun goToQuestion(num: Int) {
        binding.pager.currentItem = num
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
        val adapter = binding.pager.adapter as? QuestionAdapter ?: return
        adapter.getFragment(pos).also {
            viewModel.updateAnswers(question = pos, answers = it.answers, enteredAnswer = it.enteredAnswer)
        }
    }

    private fun saveAnswers() {
        updateAnswers(binding.pager.currentItem)
        viewModel.saveAnswers(showInfo = true)
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

    private fun finishTest() {
        updateAnswers(binding.pager.currentItem)
        viewModel.finish()
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
        navController.popBackStack(R.id.mainFragment, inclusive = false)

        testToInsert = viewModel.testToInsert
        setNavbarItem(R.id.testsPassedFragment)
    }

    private fun navigateToResults(recordId: String) {
        navController.navigate(
            QuestionMainFragmentDirections.toResults(recordId)
        )
    }
}