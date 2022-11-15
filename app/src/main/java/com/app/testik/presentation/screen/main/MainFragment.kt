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
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.main.model.MainScreenEvent
import com.app.testik.util.Constants.UPDATE_AVATAR_RESULT_KEY
import com.app.testik.util.addBackPressedCallback
import com.app.testik.util.setupBottomNavigation
import com.app.testik.util.showExitAlert
import com.app.testik.util.showSnackbar
import com.bumptech.glide.Glide
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

        addBackPressedCallback { showExitAlert() }
    }

    private fun initViews() {

        setupBottomNavigation(true)
        binding.apply {
            ivAvatar.clipToOutline = true
        }
    }

    private fun initListeners() {
        binding.apply {
            ivAvatar.setOnClickListener {
                navController.navigate(
                    MainFragmentDirections.toProfile()
                )
            }
        }

        observeResult<Boolean>(UPDATE_AVATAR_RESULT_KEY) { if (it) viewModel.getUserInfo() }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            loadAvatar(it.avatar)
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

    private fun loadAvatar(url: String) {
        val avatar = url.ifBlank { R.drawable.ic_profile_avatar }

        Glide.with(this)
            .load(avatar)
            .into(binding.ivAvatar)
    }
}