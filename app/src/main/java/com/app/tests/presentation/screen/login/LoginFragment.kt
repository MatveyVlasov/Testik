package com.app.tests.presentation.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.tests.R
import com.app.tests.databinding.FragmentLoginBinding
import com.app.tests.presentation.base.BaseFragment
import com.app.tests.presentation.model.onSuccess
import com.app.tests.presentation.screen.login.model.LoginScreenEvent
import com.app.tests.presentation.screen.login.model.LoginScreenUIState
import com.app.tests.util.hideKeyboard
import com.app.tests.util.showSnackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>() {

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleResult(task)
        }
    }

    override fun createBinding(inflater: LayoutInflater) = FragmentLoginBinding.inflate(inflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
        initListeners()
        collectData()
        createRequest()
    }

    private fun initViews() {

        binding.apply {
            btnLogin.setOnClickListener {
                viewModel.login()
                hideKeyboard()
            }

            btnLoginWithGoogle.setOnClickListener {
                loginWithGoogle()
            }

            tvRegister.setOnClickListener {
                navController.navigate(
                    LoginFragmentDirections.toRegistration()
                )
            }
        }
    }

    private fun initListeners() {
        binding.apply {
            etEmail.addTextChangedListener { viewModel.onEmailChanged(it.toString()) }
            etPassword.addTextChangedListener { viewModel.onPasswordChanged(it.toString()) }
        }
    }

    private fun collectData() {
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        state.onSuccess {
                            renderUIState(it)
                        }
                    }
                }

                launch {
                    viewModel.event.collect { handleEvent(it) }
                }
            }
        }
    }

    private fun renderUIState(data: LoginScreenUIState) {
        binding.apply {
            if (!etEmail.isFocused) etEmail.setText(data.email)
            if (!etPassword.isFocused) etPassword.setText(data.password)

            btnLogin.isEnabled = data.canLogin
        }
    }

    private fun setLoadingState(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
    }

    private fun handleEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.ShowSnackbar -> {
                setLoadingState(false)
                showSnackbar(message = event.message)
            }
            is LoginScreenEvent.Loading -> setLoadingState(true)
            is LoginScreenEvent.SuccessLogin -> {
                showSnackbar(message = "You successfully logged in")
                navController.navigate(LoginFragmentDirections.toMain())
            }
        }
    }

    private fun createRequest() {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), options)
    }

    private fun loginWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private fun handleResult(task: Task<GoogleSignInAccount>) {
        if (task.isSuccessful) {
            task.result?.let {
                val credential = GoogleAuthProvider.getCredential(it.idToken, null)
                val username = it.displayName + "#" + it.id!!.takeLast(USERNAME_ID_LENGTH)
                viewModel.loginWithGoogle(credential, it.email!!, username)
            }
        }
    }

    companion object {
        private const val USERNAME_ID_LENGTH = 4
    }
}