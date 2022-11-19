package com.app.testik.presentation.screen.createdtests

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
import com.app.testik.databinding.FragmentCreatedTestsBinding
import com.app.testik.domain.model.TestModel
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.createdtests.adapter.CreatedTestDelegateAdapter
import com.app.testik.presentation.screen.createdtests.mapper.toCreatedTestItem
import com.app.testik.presentation.screen.createdtests.model.CreatedTestDelegateItem
import com.app.testik.presentation.screen.createdtests.model.CreatedTestsScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import com.app.testik.util.delegateadapter.DelegateAdapterItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CreatedTestsFragment : BaseFragment<FragmentCreatedTestsBinding>() {

    private val viewModel: CreatedTestsViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(CreatedTestDelegateAdapter (
                onClick = { navigateToTest(it) },
                onMoreClick = { view, testId ->
                    showMenu(view = view, menuRes= R.menu.test_menu, testId = testId)
                })
            )
            .build()
    }

    private var tests = mutableListOf<DelegateAdapterItem>()

    override fun createBinding(inflater: LayoutInflater) = FragmentCreatedTestsBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { showExitAlert() }

        observeResult<TestModel>(Constants.UPDATE_TEST_RESULT_KEY) {
            val item = getItem(it.id) ?: addItem(it.toCreatedTestItem())
            if (item is CreatedTestDelegateItem) updateItem(test = item, newTest = it.toCreatedTestItem())
        }

        observeResult<TestModel>(Constants.DELETE_TEST_RESULT_KEY) {
            val item = getItem(it.id)
            if (item is CreatedTestDelegateItem) deleteItem(test = it.toCreatedTestItem(), deleteFromViewModel = true)
        }
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
                            if (tests != data.tests) tests = data.tests.toMutableList()

                            testsAdapter.submitList(tests.toList())

                            val isListEmpty = tests.isEmpty()
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

    private fun handleEvent(event: CreatedTestsScreenEvent) {
        when (event) {
            is CreatedTestsScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is CreatedTestsScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is CreatedTestsScreenEvent.Loading -> Unit
            is CreatedTestsScreenEvent.SuccessTestDeletion -> {
                showSnackbar(R.string.delete_test_success)
                deleteItem(event.test)
            }
        }
        setLoadingState(event is CreatedTestsScreenEvent.Loading)
    }

    private fun showMenu(view: View, @MenuRes menuRes: Int, testId: String) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(menuRes, menu)

            setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> navigateToTest(testId)
                    R.id.demo -> Unit
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
            CreatedTestsFragmentDirections.toEditTest(testId)
        )
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

    private fun getItem(testId: String) = tests.find { it.id() == testId } as? CreatedTestDelegateItem

    private fun addItem(test: CreatedTestDelegateItem) {
        viewModel.addTestToList(test)
        tests.add(0, test)
        testsAdapter.submitList(tests.toList())
    }

    private fun updateItem(test: CreatedTestDelegateItem, newTest: CreatedTestDelegateItem) {
        viewModel.updateTest(test = test, newTest = newTest)
        val pos = tests.indexOf(test)
        tests[pos] = newTest
        testsAdapter.submitList(tests.toList())
    }

    private fun deleteItem(test: CreatedTestDelegateItem, deleteFromViewModel: Boolean = false) {
        if (deleteFromViewModel) viewModel.deleteTestFromList(test)
        tests.remove(test)
        testsAdapter.submitList(tests.toList())
    }
}