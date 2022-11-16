package com.app.testik.presentation.screen.createdtests

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private val adapter by lazy {
        CompositeAdapter.Builder()
            .setOnUpdateCallback(viewModel::updateList)
            .add(LoadingDelegateAdapter())
            .add(ErrorDelegateAdapter())
            .add(CreatedTestDelegateAdapter())
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
    }


    private fun initViews() {

        setupBottomNavigation(true)
        binding.apply {
            rvTests.adapter = adapter
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
                            recyclerViewState = binding.rvTests.layoutManager?.onSaveInstanceState()
                            adapter.submitList(data.tests)

                            if (adapter.currentList.isNotEmpty()) {
                                binding.llInfo.isVisible = false
                                binding.rvTests.apply {
                                    isVisible = true
                                    postDelayed(100) {
                                        layoutManager?.onRestoreInstanceState(recyclerViewState)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}