package com.app.testik.presentation.screen.questionmain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
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
import com.app.testik.presentation.screen.questionmain.model.QuestionNumberDelegateItem
import com.app.testik.presentation.screen.questionmain.model.toQuestionNumber
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
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
                        questionsNumList[selectedItem] = questionsNumList[selectedItem].copy(isSelected = false)
                    }
                    selectedItem = position

                    questionsNumList[position] = questionsNumList[position].copy(isSelected = true)
                    questionsNumAdapter.submitList(questionsNumList.toList())

                    binding.rvQuestions.smoothScrollToPosition(position)
                }
            })

            btnPrev.setOnClickListener { goToPreviousQuestion() }
            btnNext.setOnClickListener { goToNextQuestion() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            if (data.questions.isNotEmpty()) {
                                binding.pager.adapter = QuestionAdapter(
                                    fragment = this@QuestionMainFragment,
                                    questions = data.questions
                                )
                                questionsNumList = MutableList(data.questions.size) { num ->
                                    num.toQuestionNumber()
                                }
                                questionsNumAdapter.submitList(questionsNumList.toList())
                            }
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: QuestionMainScreenEvent) {
        when (event) {
            is QuestionMainScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionMainScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionMainScreenEvent.Loading -> Unit
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

    private fun onBackPressed() {
        navController.navigateUp()
    }
}