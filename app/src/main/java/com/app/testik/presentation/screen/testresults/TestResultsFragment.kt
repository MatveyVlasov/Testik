package com.app.testik.presentation.screen.testresults

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentTestResultsBinding
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.testresults.mapper.toDomain
import com.app.testik.presentation.screen.testresults.model.TestResultsScreenEvent
import com.app.testik.presentation.screen.testresults.model.TestResultsScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.INSERT_TEST_KEY
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TestResultsFragment : BaseFragment<FragmentTestResultsBinding>() {

    private val viewModel: TestResultsViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentTestResultsBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { navigateToTestsPassed() }
    }

    private fun initViews() {

        setupBottomNavigation(false)
        binding.apply {

        }
    }

    private fun initListeners() {
        binding.apply {
            btnOK.setOnClickListener { navigateToTestsPassed() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            renderUIState(data)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun renderUIState(data: TestResultsScreenUIState) {
        binding.apply {
            tvTitleData.text = data.title
            tvDateData.text = data.date
            tvTimeSpentData.text = data.timeSpent
            tvPoints.text = getString(R.string.points_earned, data.pointsEarned, data.pointsMax)
        }
        setLoadingState(false)
    }

    private fun handleEvent(event: TestResultsScreenEvent) {
        when (event) {
            is TestResultsScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is TestResultsScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is TestResultsScreenEvent.Loading -> Unit
        }
        setLoadingState(event is TestResultsScreenEvent.Loading)
    }

    private fun navigateToTestsPassed() {
        navController.popBackStack(R.id.mainFragment, inclusive = false)

        val testToInsert = viewModel.screenUIState.toDomain()
        setNavbarItem(R.id.testsPassedFragment, bundle = bundleOf(INSERT_TEST_KEY to testToInsert))
    }
}