package com.app.testik.presentation.screen.createdtests

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.app.testik.R
import com.app.testik.databinding.FragmentCreatedTestsBinding
import com.app.testik.presentation.adapter.ErrorDelegateAdapter
import com.app.testik.presentation.adapter.LoadingDelegateAdapter
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.createdtests.adapter.CreatedTestDelegateAdapter
import com.app.testik.util.*
import com.app.testik.util.delegateadapter.CompositeAdapter
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

    private var recyclerViewState: Parcelable? = null

    override fun createBinding(inflater: LayoutInflater) = FragmentCreatedTestsBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { showExitAlert() }
        observeResult<Boolean>(Constants.UPDATE_TEST_RESULT_KEY) {
            if (it) viewModel.updateList(firstUpdate = true)
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
            fabCreate.setOnClickListener {
                navController.navigate(
                    CreatedTestsFragmentDirections.toEditTest()
                )
            }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect {
                        it.onSuccess { data ->
                            recyclerViewState = binding.rvTests.layoutManager?.onSaveInstanceState()
                            testsAdapter.submitList(data.tests)

                            val isListEmpty = data.tests.isEmpty()
                            binding.llNoTests.isVisible = isListEmpty
                            binding.rvTests.apply {
                                isVisible = !isListEmpty
                                postDelayed(40) {
                                    layoutManager?.onRestoreInstanceState(recyclerViewState)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showMenu(view: View, @MenuRes menuRes: Int, testId: String) {
        PopupMenu(requireContext(), view).apply {
            menuInflater.inflate(menuRes, menu)

            setOnMenuItemClickListener { menuItem: MenuItem ->
                when (menuItem.itemId) {
                    R.id.edit -> navigateToTest(testId)
                    R.id.demo -> Unit
                    R.id.delete -> Unit
                }
                return@setOnMenuItemClickListener true
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) setForceShowIcon(true)
            show()
        }
    }

    private fun navigateToTest(testId: String) {
        navController.navigate(
            CreatedTestsFragmentDirections.toEditTest(testId)
        )
    }
}