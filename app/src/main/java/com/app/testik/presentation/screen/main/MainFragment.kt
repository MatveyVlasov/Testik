package com.app.testik.presentation.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentMainBinding
import com.app.testik.domain.model.CategoryType
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.main.adapter.TestsCategoryDelegateAdapter
import com.app.testik.presentation.screen.main.model.MainScreenEvent
import com.app.testik.util.*
import com.app.testik.util.Constants.UPDATE_AVATAR_RESULT_KEY
import com.app.testik.util.delegateadapter.CompositeAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    private val categoryTestsAdapter by lazy {
        CompositeAdapter.Builder()
            .add(
                TestsCategoryDelegateAdapter(
                    onTestClick = { navigateToTest(it) },
                    onMoreClick = { navigateToTestList(it) }
                )
            )
            .build()
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentMainBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()

        addBackPressedCallback { showExitAlert() }

        observeResult<Boolean>(UPDATE_AVATAR_RESULT_KEY) { if (it) viewModel.getUserInfo(fromCache = true) }
    }

    private fun initViews() {

        changeStatusBarColor(color = getColor(R.color.white), isLight = true)
        setupBottomNavigation(true)
        binding.apply {
            toolbar.setupAvatarItem { navigateToProfile() }

            val color = requireContext().getThemeColor(com.google.android.material.R.attr.colorSecondary)
            swipeRefresh.setColorSchemeColors(color)

            rvCategoryTests.adapter = categoryTestsAdapter
        }

        testToShow?.let {
            navigateToTest(it)
            testToShow = null
        }
    }

    private fun initListeners() {
        binding.apply {
            swipeRefresh.setOnRefreshListener { viewModel.getTests(fromCache = false) }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess { data ->
                            loadAvatar(data.avatar)
                            categoryTestsAdapter.submitList(
                                data.categoryTests.filter {
                                    it.tests.isNotEmpty() || it.category == CategoryType.ALL
                                }
                            )
                            binding.swipeRefresh.isRefreshing = false
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun handleEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is MainScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is MainScreenEvent.Loading -> Unit
        }
        setLoadingState(event is MainScreenEvent.Loading)
    }

    private fun loadAvatar(url: String) =
        loadAvatar(context = requireContext(), imageView = binding.toolbar.getAvatarItem(), url = url)

    private fun navigateToProfile() {
        navController.navigate(
            MainFragmentDirections.toProfile()
        )
    }

    private fun navigateToTest(testId: String) {
        navController.navigate(
            MainFragmentDirections.toTestInfo(testId)
        )
    }

    private fun navigateToTestList(category: CategoryType) {
        navController.navigate(
            MainFragmentDirections.toTestList(category)
        )
    }
}