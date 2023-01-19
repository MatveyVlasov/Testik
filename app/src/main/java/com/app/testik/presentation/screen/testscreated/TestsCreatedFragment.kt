package com.app.testik.presentation.screen.testscreated

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.FragmentTestsCreatedBinding
import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testscreated.adapter.TestCreatedDelegateAdapter
import com.app.testik.presentation.screen.testscreated.mapper.toTestCreatedItem
import com.app.testik.presentation.screen.testscreated.model.TestCreatedDelegateItem
import com.app.testik.presentation.screen.testscreated.model.TestsCreatedScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestsCreatedFragment : BaseFragment<FragmentTestsCreatedBinding>() {

    private val viewModel: TestsCreatedViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(TestCreatedDelegateAdapter (
                onClick = { navigateToTest(it) },
                onMoreClick = { view, testId ->
                    showMenu(view = view, menuRes= R.menu.test_menu, testId = testId)
                })
            )
            .build()
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentTestsCreatedBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { showExitAlert() }

        observeResult<TestModel>(Constants.UPDATE_TEST_RESULT_KEY) {
            val item = getItem(it.id) ?: viewModel.addTestToList(it.toTestCreatedItem())
            if (item is TestCreatedDelegateItem) viewModel.updateTest(test = item, newTest = it.toTestCreatedItem())
        }

        observeResult<TestModel>(Constants.DELETE_TEST_RESULT_KEY) {
            val item = getItem(it.id)
            if (item is TestCreatedDelegateItem) viewModel.deleteTestFromList(test = it.toTestCreatedItem())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkUser()
    }

    private fun initViews() {

        setupBottomNavigation(true)
        binding.apply {
            rvTests.apply {
                adapter = testsAdapter

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
            fabCreate.setOnClickListener { navigateToTest() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            testsAdapter.submitList(data.tests)

                            val isListEmpty = data.tests.isEmpty()
                            binding.llNoTests.isVisible = isListEmpty
                            binding.rvTests.isVisible = !isListEmpty
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: TestsCreatedScreenEvent) {
        when (event) {
            is TestsCreatedScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestsCreatedScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestsCreatedScreenEvent.Loading -> Unit
            is TestsCreatedScreenEvent.SuccessTestDeletion -> showSnackbar(R.string.delete_test_success)
        }
        setLoadingState(event is TestsCreatedScreenEvent.Loading)
    }

    private fun showMenu(view: View, @MenuRes menuRes: Int, testId: String) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(menuRes, menu)

            setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.editTest -> navigateToTest(testId)
                    R.id.editQuestions -> navigateToQuestionList(testId)
                    R.id.demo -> navigateToTestInfo(testId)
                    R.id.share -> shareLink(testId)
                    R.id.results -> navigateToResults(testId)
                    R.id.delete -> confirmDeletion(testId)
                }
                return@setOnMenuItemClickListener true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)
            show()
        }
    }

    private fun navigateToTest(testId: String = "") {
        navController.navigate(
            TestsCreatedFragmentDirections.toEditTest(testId)
        )
    }

    private fun navigateToQuestionList(testId: String) {
        navController.navigate(
            TestsCreatedFragmentDirections.toQuestionList(testId)
        )
    }

    private fun navigateToTestInfo(testId: String) {
        navController.navigate(
            TestsCreatedFragmentDirections.toTestInfo(testId = testId, isDemo = true)
        )
    }

    private fun navigateToResults(testId: String) {
        navController.navigate(
            TestsCreatedFragmentDirections.toResults(testId = testId)
        )
    }

    private fun shareLink(testId: String) {
        val test = getItem(testId) ?: return
        if (!test.isLinkEnabled) return showSnackbar(message = R.string.enable_test_link)
        shareTestLink(test.link)
    }

    private fun confirmDeletion(testId: String) {
        showAlert(
            title = R.string.delete_test,
            message = R.string.delete_test_confirmation,
            positive = R.string.confirm,
            negative = R.string.cancel,
            onPositiveClick = { viewModel.deleteTest(getItem(testId)) }
        )
    }

    private fun getItem(testId: String) = testsAdapter.currentList.find { it.id() == testId } as? TestCreatedDelegateItem

}