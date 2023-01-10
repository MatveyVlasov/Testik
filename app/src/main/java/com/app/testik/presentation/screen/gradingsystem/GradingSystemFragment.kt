package com.app.testik.presentation.screen.gradingsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import com.app.testik.R
import com.app.testik.databinding.FragmentGradingSystemBinding
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.gradingsystem.adapter.GradeDelegateAdapter
import com.app.testik.presentation.screen.gradingsystem.model.GradingSystemScreenEvent
import com.app.testik.presentation.screen.gradingsystem.model.GradingSystemScreenUIState
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GradingSystemFragment : BaseFragment<FragmentGradingSystemBinding>() {

    private val viewModel: GradingSystemViewModel by viewModels()

    private val gradesAdapter by lazy {
        CompositeAdapter.Builder()
            .add(
                GradeDelegateAdapter(
                    onGradeTextChanged = { item, text -> viewModel.onGradeTextChanged(grade = item, text = text) },
                    onFromTextChanged = { item, text -> viewModel.onFromTextChanged(grade = item, text = text) },
                    onToTextChanged = { item, text -> viewModel.onToTextChanged(grade = item, text = text) },
                    onDeleteClick = { item -> viewModel.deleteGrade(grade = item) }
                )
            )
            .build()
    }

    private val itemTouchHelper by lazy {
        ItemTouchHelper(ItemTouchCallback { from, to -> viewModel.moveGrade(from, to) })
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentGradingSystemBinding.inflate(inflater)

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
            tvEnable.addInfoIcon { navigateToInfo(getString(R.string.grading_system_info)) }

            rvGrades.adapter = gradesAdapter
            itemTouchHelper.attachToRecyclerView(rvGrades)
        }
    }

    private fun initListeners() {
        binding.apply {
            toolbar.setNavigationOnClickListener { onBackPressed() }

            switchEnable.setOnCheckedChangeListener { _, isChecked -> viewModel.onEnableChanged(isChecked) }

            btnAddGrade.setOnClickListener {
                viewModel.addGrade()
                scrollView.fullScroll(View.FOCUS_DOWN)
            }

            btnSave.setOnClickListener { viewModel.saveGrades() }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess { data ->
                            gradesAdapter.submitList(data.grades)
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

    private fun handleEvent(event: GradingSystemScreenEvent) {
        when (event) {
            is GradingSystemScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is GradingSystemScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is GradingSystemScreenEvent.Loading -> Unit
            is GradingSystemScreenEvent.ErrorOverlappingIntervals -> {
                showSnackbar(getString(R.string.correct_grades_overlapping_intervals, event.num))
            }
        }
        setLoadingState(event is GradingSystemScreenEvent.Loading)
    }

    private fun renderUIState(data: GradingSystemScreenUIState) {
        binding.apply {
            switchEnable.isChecked = data.isEnabled
            rvGrades.isVisible = data.isEnabled
            btnAddGrade.isVisible = data.isEnabled
        }

        setLoadingState(false)
    }

    private fun onBackPressed() {
        if (viewModel.hasUnsavedChanges) confirmExitWithoutSaving()
        else navController.navigateUp()
    }

    private fun navigateToInfo(text: String) {
        navController.navigate(
            GradingSystemFragmentDirections.toInfo(text)
        )
    }
}