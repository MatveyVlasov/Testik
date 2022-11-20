package com.app.testik.presentation.screen.questlionlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.FragmentQuestionListBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.questlionlist.adapter.QuestionDelegateAdapter
import com.app.testik.presentation.screen.questlionlist.model.QuestionListScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class QuestionListFragment : BaseFragment<FragmentQuestionListBinding>() {

    private val viewModel: QuestionListViewModel by viewModels()

    private val questionsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                QuestionDelegateAdapter(
                    onClick = { navigateToQuestion(it) },
                    onDeleteClick = { }
                )
            )
            .build()
    }

    private var questions = mutableListOf<DelegateAdapterItem>()

    override fun createBinding(inflater: LayoutInflater) = FragmentQuestionListBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

//        addBackPressedCallback { showExitAlert() }

//        observeResult<TestModel>(Constants.UPDATE_TEST_RESULT_KEY) {
//            val item = getItem(it.id) ?: addItem(it.toCreatedTestItem())
//            if (item is CreatedTestDelegateItem) updateItem(test = item, newTest = it.toCreatedTestItem())
//        }
//
//        observeResult<TestModel>(Constants.DELETE_TEST_RESULT_KEY) {
//            val item = getItem(it.id)
//            if (item is CreatedTestDelegateItem) deleteItem(test = it.toCreatedTestItem(), deleteFromViewModel = true)
//        }
    }


    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            toolbar.menu.findItem(R.id.save).apply {
                setActionView(R.layout.item_menu_text)
                actionView?.findViewById<TextView>(R.id.tv)?.setText(R.string.save)
            }

            rvQuestions.apply {
                adapter = questionsAdapter

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
                            if (questions != data.questions) questions = data.questions.toMutableList()

                            questionsAdapter.submitList(questions.toList())

                            val isListEmpty = questions.isEmpty()
                            binding.llNoQuestions.isVisible = isListEmpty
                            binding.rvQuestions.isVisible = !isListEmpty
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
            is QuestionListScreenEvent.SuccessQuestionDeletion -> {
                showSnackbar(R.string.delete_question_success)
                //viewModel.updateList(firstUpdate = true)
            }
        }
        setLoadingState(event is QuestionListScreenEvent.Loading)
    }

    private fun navigateToQuestion(questionId: String = "") {
//        navController.navigate(
//            QuestionListFragmentDirections.toEditTest(testId)
//        )
    }

//    private fun getItem(testId: String) = tests.find { it.id() == testId } as? CreatedTestDelegateItem
//
//    private fun addItem(test: CreatedTestDelegateItem) {
//        viewModel.addTestToList(test)
//        tests.add(0, test)
//        testsAdapter.submitList(tests.toList())
//    }
//
//    private fun updateItem(test: CreatedTestDelegateItem, newTest: CreatedTestDelegateItem) {
//        viewModel.updateTest(test = test, newTest = newTest)
//        val pos = tests.indexOf(test)
//        tests[pos] = newTest
//        testsAdapter.submitList(tests.toList())
//    }
//
//    private fun deleteItem(test: CreatedTestDelegateItem, deleteFromViewModel: Boolean = false) {
//        if (deleteFromViewModel) viewModel.deleteTestFromList(test)
//        tests.remove(test)
//        testsAdapter.submitList(tests.toList())
//    }

//    private fun confirmDeletion(questionId: String) {
//        showAlert(
//            title = R.string.delete_question,
//            message = R.string.delete_question_confirmation,
//            positive = R.string.confirm,
//            negative = R.string.cancel,
//            onPositiveClick = { viewModel.deleteQuestion(questionId) }
//        )
//    }
}