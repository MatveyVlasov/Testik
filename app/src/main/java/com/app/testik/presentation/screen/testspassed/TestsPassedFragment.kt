package com.app.testik.presentation.screen.testspassed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentTestsPassedBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testspassed.adapter.TestPassedDelegateAdapter
import com.app.testik.presentation.screen.testspassed.model.TestsPassedScreenEvent
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestsPassedFragment : BaseFragment<FragmentTestsPassedBinding>() {

    private val viewModel: TestsPassedViewModel by viewModels()

    private val testsAdapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(ErrorDelegateAdapter(viewModel::updateList))
            .add(LoadingDelegateAdapter())
            .add(
                TestPassedDelegateAdapter { navigateToTest(it) }
            )
            .build()
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentTestsPassedBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { showExitAlert() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkUser()
    }

    private fun initViews() {

        setupBottomNavigation(true)
        binding.apply {
            rvTests.adapter = testsAdapter
        }
    }

    private fun initListeners() {
        binding.apply {
            btnGoHome.setOnClickListener { navigateToMain() }
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

    private fun handleEvent(event: TestsPassedScreenEvent) {
        when (event) {
            is TestsPassedScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestsPassedScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestsPassedScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestsPassedScreenEvent.Loading)
    }

    private fun navigateToMain() {
        setNavbarItem(R.id.mainFragment)
    }

    private fun navigateToTest(testId: String = "") {
//        navController.navigate(
//
//        )
    }
}