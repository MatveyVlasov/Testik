package com.app.tests.presentation.screen.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.tests.databinding.FragmentMainBinding
import com.app.tests.presentation.base.BaseFragment
import com.app.tests.presentation.model.onSuccess
import com.app.tests.presentation.screen.main.model.MainScreenEvent
import com.app.tests.util.showSnackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainFragment : BaseFragment<FragmentMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater) = FragmentMainBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
    }

    private fun initViews() {

        binding.apply {

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
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            binding.tvInfo.text = "${it.username} (${it.email})"
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
            is MainScreenEvent.ShowSnackbar -> {
                //setLoadingState(false)
                showSnackbar(message = event.message)
            }
            is MainScreenEvent.Loading -> Unit //setLoadingState(true)
        }
    }
}