package com.app.tests.presentation.screen.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.app.tests.databinding.FragmentLoginBinding
import com.app.tests.presentation.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    override fun createBinding(inflater: LayoutInflater) = FragmentLoginBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {

        binding.apply {
            btnRegister.setOnClickListener {
                navController.navigate(
                    LoginFragmentDirections.toRegistration()
                )
            }
        }
    }
}