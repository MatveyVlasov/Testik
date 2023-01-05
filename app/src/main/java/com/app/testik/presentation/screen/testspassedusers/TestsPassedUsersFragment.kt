package com.app.testik.presentation.screen.testspassedusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.databinding.FragmentTestsPassedUsersBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testspassedusers.adapter.TestPassedUserDelegateAdapter
import com.app.testik.presentation.screen.testspassedusers.model.TestsPassedUsersScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestsPassedUsersFragment : BaseFragment<FragmentTestsPassedUsersBinding>() {

    private val viewModel: TestsPassedUsersViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                TestPassedUserDelegateAdapter { recordId, username ->
                    navigateToTestPassed(recordId = recordId, username = username)
                }
            )
            .build()
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentTestsPassedUsersBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {
            rvTests.adapter = testsAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { navController.navigateUp() }
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

    private fun handleEvent(event: TestsPassedUsersScreenEvent) {
        when (event) {
            is TestsPassedUsersScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestsPassedUsersScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestsPassedUsersScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestsPassedUsersScreenEvent.Loading)
    }


    private fun navigateToTestPassed(recordId: String, username: String) {
        navController.navigate(
            TestsPassedUsersFragmentDirections.toTestPassed(recordId = recordId, username = username)
        )
    }
}