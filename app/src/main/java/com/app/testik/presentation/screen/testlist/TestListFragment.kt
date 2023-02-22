package com.app.testik.presentation.screen.testlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.databinding.FragmentTestListBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testlist.adapter.TestInfoDelegateAdapter
import com.app.testik.presentation.screen.testlist.model.TestListScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestListFragment : BaseFragment<FragmentTestListBinding>() {

    private val viewModel: TestListViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                TestInfoDelegateAdapter { navigateToTest(it) }
            )
            .build()
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentTestListBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            toolbar.setNavigationOnClickListener { navController.navigateUp() }

            rvTests.adapter = testsAdapter
        }
    }

    private fun initListeners() {
        binding.apply {

        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            binding.toolbar.setTitle(data.category.description)
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

    private fun handleEvent(event: TestListScreenEvent) {
        when (event) {
            is TestListScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestListScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestListScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestListScreenEvent.Loading)
    }

    private fun navigateToTest(testId: String) {
        navController.navigate(
            TestListFragmentDirections.toTestInfo(testId)
        )
    }
}