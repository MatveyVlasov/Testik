package com.app.testik.presentation.screen.questlionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionListBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.questlionlist.adapter.QuestionDelegateAdapter
import com.app.testik.presentation.model.QuestionDelegateItem
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenEvent
import com.app.testik.util.*
import com.app.testik.util.Constants.DELETE_QUESTION_RESULT_KEY
import com.app.testik.util.Constants.UPDATE_QUESTION_RESULT_KEY
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionListFragment : BaseFragment<FragmentQuestionListBinding>() {

    private val viewModel: QuestionListViewModel by viewModels()

    private val questionsAdapter by lazy {
        CompositeAdapter.Builder()
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                QuestionDelegateAdapter(
                    onClick = { navigateToQuestion(it) },
                    onDeleteClick = { confirmDeletion(it) }
                )
            )
            .build()
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback { from, to -> viewModel.moveQuestion(from, to) })
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentQuestionListBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { onBackPressed() }

        observeResult<QuestionDelegateItem>(UPDATE_QUESTION_RESULT_KEY) {
            val item = getItem(it.id) ?: viewModel.addQuestionToList(it)
            if (item is QuestionDelegateItem) viewModel.updateQuestion(question = item, newQuestion = it)
        }

        observeResult<QuestionDelegateItem>(DELETE_QUESTION_RESULT_KEY) {
            val item = getItem(it.id)
            if (item is QuestionDelegateItem) viewModel.deleteQuestionFromList(question = it)
        }
    }


    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            btnSave.setOnClickListener {
                setLoadingState(true)
                viewModel.saveQuestions()
            }

            rvQuestions.apply {
                adapter = questionsAdapter
                itemTouchHelper.attachToRecyclerView(this)

                clearOnScrollListeners()
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 1) fabCreate.hide()
                        else if (dy < 1) fabCreate.show()
                    }
                })
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            fabCreate.setOnClickListener { navigateToQuestion() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            questionsAdapter.submitList(data.questions)
                            renderUIState(data.questions.isEmpty())
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: QuestionListScreenEvent) {
        when (event) {
            is QuestionListScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is QuestionListScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is QuestionListScreenEvent.Loading -> Unit
            is QuestionListScreenEvent.SuccessQuestionsSaving -> showSnackbar(message = R.string.save_questions_success)
        }
        setLoadingState(event is QuestionListScreenEvent.Loading)
    }

    private fun renderUIState(isListEmpty: Boolean) {
        binding.apply {
            llNoQuestions.isVisible = isListEmpty
            rvQuestions.isVisible = !isListEmpty
            btnSave.isVisible = !isListEmpty
        }
    }

    private fun navigateToQuestion(question: QuestionDelegateItem = QuestionDelegateItem()) {
        navController.navigate(
            QuestionListFragmentDirections.toEditQuestion(question)
        )
    }

    private fun getItem(questionId: String) = questionsAdapter.currentList.find { it.id() == questionId } as? QuestionDelegateItem

    private fun confirmDeletion(question: QuestionDelegateItem) {
        showAlert(
            title = R.string.delete_question,
            message = R.string.delete_question_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = { viewModel.deleteQuestionFromList(question) }
        )
    }

    private fun onBackPressed() {
        if (viewModel.hasUnsavedChanges) confirmExitWithoutSaving()
        else navController.navigateUp()
    }
}