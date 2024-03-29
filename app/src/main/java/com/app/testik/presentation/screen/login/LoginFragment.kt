package com.app.testik.presentation.screen.login

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.app.testik.R
import com.app.testik.databinding.FragmentLoginBinding
import com.app.testik.presentation.base.BaseFragment
import com.app.testik.presentation.model.onSuccess
import com.app.testik.presentation.screen.login.model.LoginScreenEvent
import com.app.testik.presentation.screen.login.model.LoginScreenUIState
import com.app.testik.util.*
import com.app.testik.util.Constants.USERNAME_GOOGLE_DELIMITER
import com.app.testik.util.Constants.USERNAME_GOOGLE_ID_LENGTH
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

        addBackPressedCallback { showExitAlert() }
    }

    private fun initViews() {

        val statusBarColor = requireContext().getThemeColor(com.google.android.material.R.attr.colorPrimaryVariant)
        changeStatusBarColor(color = statusBarColor, isLight = false)
        setupBottomNavigation(false)
        binding.apply {
            toolbar.setupLanguageItem (getColor(R.color.blue_dark)) {
                showChangeLanguageDialog { viewModel.setLanguage(it) }
            }
        }
    }

    private fun initListeners() {
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

            tvResetPassword.setOnClickListener {
                navController.navigate(
                    LoginFragmentDirections.toPasswordReset()
                )
            }

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

            tilEmail.error = getStringOrNull(data.emailError)
            tilPassword.error = getStringOrNull(data.passwordError)

            btnLogin.isEnabled = data.canLogin
        }
        setLoadingState(false)
    }

    private fun handleEvent(event: LoginScreenEvent) {
        when (event) {
            is LoginScreenEvent.ShowSnackbar -> showSnackbar(message = event.message)
            is LoginScreenEvent.ShowSnackbarByRes -> showSnackbar(message = event.message)
            is LoginScreenEvent.Loading -> Unit
            is LoginScreenEvent.NavigateToMain -> {
                navController.navigate(LoginFragmentDirections.toMain())
            }
            is LoginScreenEvent.Restart -> requireActivity().recreate()
        }
        setLoadingState(event is LoginScreenEvent.Loading)
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
                val username =
                    (it.displayName + USERNAME_GOOGLE_DELIMITER + it.id!!.takeLast(USERNAME_GOOGLE_ID_LENGTH)).toUsername()
                val avatar = it.photoUrl.toAvatar()
                viewModel.loginWithGoogle(credential, it.email!!, username, avatar)
            }
        }
    }
}